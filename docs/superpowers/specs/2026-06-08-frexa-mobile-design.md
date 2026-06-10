# Frexa Mobile — Design Spec
_2026-06-08_

## Overview

Frexa adalah aplikasi Android (Java, minSdk 24) untuk prediksi harga kripto. Pengguna memilih aset kripto, menebak apakah harganya akan **naik** atau **turun** dalam waktu tertentu, dan memasang nominal taruhan virtual. Jika prediksi benar → profit tetap; jika salah → nominal hilang.

Data aset dan harga 100% dari **CoinGecko API** (free tier). Analisis AI menggunakan **Gemini API**. Tab Market dihapus total. Tidak ada data hardcoded untuk aset.

---

## 1. Architecture

**Pattern:** MVVM (Model–View–ViewModel)

**Language:** Java (bukan Kotlin)

**Activities (2):**

| Activity | Launcher | Fungsi |
|---|---|---|
| `MainActivity` | ✅ | Container NavHostFragment + BottomNavigationView |
| `TradeDetailActivity` | ❌ | Detail transaksi, dibuka via Intent dari TransaksiFragment |

`MainActivity.onCreate()` membaca `UserPrefs.isLoggedIn()`:
- `false` → NavController navigate ke `LoginFragment`, sembunyikan BottomNav
- `true` → NavController navigate ke `HomeFragment`, tampilkan BottomNav

`TradeDetailActivity` menerima `EXTRA_TRADE_ID` (String) via Intent, load trade dari Room.

---

## 2. Navigation

**Navigation Component** dengan `NavHostFragment` di `MainActivity`.

**NavGraph (`nav_graph.xml`):**

```
login_fragment          (startDestination jika belum login)
home_fragment           (startDestination setelah login)
  ├── profile_fragment
  ├── notification_fragment   (empty state saja, tanpa settings)
  ├── add_account_fragment
  │     └── add_account_name_fragment
  └── asset_detail_fragment
        └── quote_history_fragment
terminal_fragment
transaksi_fragment
bantuan_fragment
```

**BottomNavigationView:** 4 item — Home, Transaksi, Terminal, Bantuan. Disembunyikan (`GONE`) saat stack berisi fragment selain 4 tab utama dan LoginFragment.

**BottomSheetDialogFragments (tidak masuk NavGraph):**
- `AccountBottomSheetFragment` — daftar akun, menu titik-3
- `DepositBottomSheetFragment` — flow multi-step (main → method → nominal → confirm → QR)
- `OrderBottomSheetFragment` — order placement (di Terminal)
- `DurationBottomSheetFragment` — pilih durasi (di Terminal)
- `ChartSettingsBottomSheetFragment` — jenis chart & timeframe

**Intent flow:**
- `TransaksiFragment` → `Intent(context, TradeDetailActivity.class)` dengan `putExtra("trade_id", id)`
- `TradeDetailActivity` → tombol "Tampilkan di Grafik" → `Intent(this, MainActivity.class)` + `putExtra("open_terminal", coinId)`

---

## 3. Fragments (12)

| Fragment | Tab/Stack | RecyclerView |
|---|---|---|
| `LoginFragment` | pre-login | ❌ |
| `HomeFragment` | Home tab | ✅ Market Movers (horizontal), Asset List (vertical) |
| `TerminalFragment` | Terminal tab | ❌ (Canvas chart) |
| `TransaksiFragment` | Transaksi tab | ✅ Open Trades, Riwayat |
| `BantuanFragment` | Bantuan tab | ✅ Grid menu |
| `ProfileFragment` | stack dari Home | ✅ Statistik cards horizontal |
| `NotificationFragment` | stack | ❌ (empty state) |
| `NotifSettingsFragment` | ~~dihapus~~ | — |
| `AddAccountFragment` | stack | ✅ currency radio list |
| `AddAccountNameFragment` | stack | ❌ |
| `AssetDetailFragment` | stack | ✅ jadwal trading |
| `QuoteHistoryFragment` | stack | ✅ quote rows |

---

## 4. Data Layer

### 4.1 Networking — Retrofit

**Base URL:** `https://api.coingecko.com/api/v3/`

**`CoinGeckoService.java` endpoints:**

```java
// Daftar coin dengan harga, market cap, % change
@GET("coins/markets")
Call<List<CoinMarket>> getMarkets(
    @Query("vs_currency") String currency,   // "usd"
    @Query("order") String order,            // "market_cap_desc"
    @Query("per_page") int perPage,          // 50
    @Query("sparkline") boolean sparkline    // false
);

// OHLC data untuk candlestick chart
@GET("coins/{id}/ohlc")
Call<List<List<Double>>> getOhlc(
    @Path("id") String coinId,
    @Query("vs_currency") String currency,
    @Query("days") int days                  // 1
);

// Harga live multi-coin (polling)
@GET("simple/price")
Call<Map<String, Map<String, Double>>> getPrices(
    @Query("ids") String ids,                // comma-separated
    @Query("vs_currencies") String currencies,
    @Query("include_24hr_change") boolean change
);

// Historical chart untuk mini chart di TradeDetailActivity
@GET("coins/{id}/market_chart")
Call<MarketChart> getMarketChart(
    @Path("id") String coinId,
    @Query("vs_currency") String currency,
    @Query("days") int days                  // 1
);

// Detail coin untuk AssetDetailFragment
@GET("coins/{id}")
Call<CoinDetail> getCoinDetail(
    @Path("id") String coinId
);
```

**`GeminiService.java`:**
```
POST https://generativelanguage.googleapis.com/v1beta/models/gemini-pro:generateContent
```
Dipanggil dari `TradeDetailActivity` dengan prompt berisi data transaksi.

**API Key storage:** Gemini API key disimpan di `local.properties` sebagai `GEMINI_API_KEY=...` dan diakses via `BuildConfig.GEMINI_API_KEY` (didefinisikan di `build.gradle.kts` dengan `buildConfigField`). Tidak pernah di-hardcode di source code.

---

### 4.2 Room Database — `FrxDatabase.java`

**`TradeEntity.java`:**
```
id (String, PK)
coinId (String)
coinName (String)
coinSymbol (String)
direction (String: "UP" / "DOWN")
stakeAmount (double)
profitPercent (int)
entryPrice (double)
exitPrice (double)
durationLabel (String: "1 mnt", "5 mnt", dst)
durationSeconds (int)
openTime (long — epoch ms)
closeTime (long — epoch ms)
isWin (boolean)
pnl (double)
status (String: "OPEN" / "CLOSED")
```

**`CachedPriceEntity.java`:**
```
coinId (String, PK)
symbol (String)
name (String)
imageUrl (String)
currentPrice (double)
priceChangePercent24h (double)
marketCapRank (int)
lastUpdated (long — epoch ms)
```

**DAOs:** `TradeDao.java`, `CachedPriceDao.java`

---

### 4.3 SharedPreferences — `UserPrefs.java`

```
is_logged_in        boolean
user_name           String
user_email          String
user_phone          String
virtual_balance     float   (default: 10000.0 — saldo demo awal)
theme_mode          String  ("dark" / "light")
active_coin_id      String  (last opened coin di Terminal)
```

---

### 4.4 Background Thread — `AppExecutors.java`

Singleton dengan:
- `diskIO()` → `Executors.newSingleThreadExecutor()`
- `networkIO()` → `Executors.newFixedThreadPool(3)`
- `mainThread()` → `new MainThreadExecutor()` (Handler Looper.main)

**Price polling:** `ScheduledExecutorService` di `CryptoViewModel`, jadwalkan `/simple/price` tiap 10 detik selama TerminalFragment aktif. `onPause()` → `shutdown()`, `onResume()` → restart.

**Offline handling:** Jika Retrofit gagal (no network / timeout):
1. Load dari `CachedPriceEntity` (Room)
2. Tampilkan banner: *"Menampilkan data terakhir — Tidak ada jaringan"*
3. Tombol **Refresh** → ulangi Retrofit call

---

## 5. ViewModels

| ViewModel | Fragment/Activity | LiveData |
|---|---|---|
| `UserViewModel` | LoginFragment, ProfileFragment | `userData`, `isLoggedIn` |
| `CryptoViewModel` | HomeFragment, TerminalFragment | `coinList`, `livePrice`, `ohlcData`, `isLoading`, `errorMessage` |
| `TradingViewModel` | TerminalFragment, TransaksiFragment | `openTrades`, `tradeHistory`, `virtualBalance` |
| `TradeDetailViewModel` | TradeDetailActivity | `tradeDetail`, `miniChartData`, `aiAnalysis` |
| `AssetDetailViewModel` | AssetDetailFragment | `coinDetail`, `quoteHistory` |

---

## 6. Screen Detail

### LoginFragment
- Toggle tab: Masuk / Pendaftaran
- **Pilihan metode registrasi/login:** dua tombol dengan icon — 📧 Email dan 📱 Nomor HP. User memilih salah satu → field input menyesuaikan (input email atau input nomor HP) + field kata sandi (toggle show/hide)
- Tap Daftar/Masuk → validasi field tidak kosong → simpan ke SharedPreferences → `navController.navigate(R.id.homeFragment)`
- Tidak ada API call, tidak ada verifikasi, tidak ada social login

### HomeFragment
- **TopBar:** Avatar (→ ProfileFragment), AccountButton (nama akun + saldo virtual, → AccountBottomSheet), Bell (→ NotificationFragment)
- **Deposit button** → `DepositBottomSheetFragment`
- **Market Movers RecyclerView** (horizontal): top 5 coin by % change dari `CryptoViewModel.coinList`, sorted by `priceChangePercent24h` desc. Setiap card: icon, nama, harga, % change (hijau/merah)
- **Statistik Trading** (row 3 card): Total Transaksi, Win Rate %, Profit Terbaik — dari `TradingViewModel.tradeHistory`
- **Asset List** (vertical RecyclerView): tab **Top** (by market cap rank), **Gainers** (% change desc), **Losers** (% change asc) — semua dari `CryptoViewModel.coinList`
  - Setiap row: coin icon, nama, harga, % change, tombol ⓘ → AssetDetailFragment
  - Tap row → TerminalFragment dengan coin tersebut
- **Offline banner** jika no network

### TerminalFragment
- **TopBar:** Avatar, saldo + nama akun (→ AccountBottomSheet), tombol deposit (→ DepositBottomSheet)
- **Pair selector:** nama coin, harga live, profit % — tap → ganti coin (dari daftar asset). Profit% tiap coin = **85%** default, naik ke **90%** untuk top-10 by market cap, **80%** untuk rank 11–30, **75%** untuk rank 31+. Nilai ini disimpan di `CachedPriceEntity.profitPercent` saat data diambil dari CoinGecko.
- **Candlestick chart** (Canvas): OHLC dari CoinGecko, refresh tiap price tick
- **Mode & Profit label:** "Mode Fixed Time", profit potensial dihitung dari nominal × profit%
- **Stepper Durasi** + **Stepper Nominal**
- **Tombol Turun** (merah) + **Tombol Naik** (hijau) → buat TradeEntity (status OPEN), simpan ke Room, tambah ke `openTrades` LiveData
- Trade expire via `ScheduledExecutorService` → bandingkan `exitPrice` vs `entryPrice` → update TradeEntity (status CLOSED, isWin, pnl) → update `virtual_balance` di SharedPreferences

### TransaksiFragment
- Tab: Fixed Time / Forex / Stocks → hanya Fixed Time yang aktif karena semua aset dari CoinGecko (kripto). Tab Forex dan Stocks ditampilkan tapi disabled (opacity 50%, tidak bisa diklik) dengan label "Segera Hadir"
- Summary: total nominal open, estimasi PnL
- **Open trades RecyclerView:** countdown live via `Handler.postDelayed()`, direction indicator, nominal
- **Riwayat RecyclerView:** dari Room DB, diurutkan `closeTime DESC`, win → hijau, loss → merah
- Tap riwayat → `Intent` ke `TradeDetailActivity`

### TradeDetailActivity
- **Mini chart (Canvas):** data dari `/coins/{id}/market_chart?days=1`, plot line chart, marker entry dan exit
- **Tombol "Analisa dengan AI":** POST ke Gemini API di background thread, tampilkan hasil dalam dialog/card
- **Tombol "Tampilkan di Grafik":** Intent ke MainActivity + extra coinId, buka TerminalFragment
- **Detail rows:** Nominal, PnL, ID Transaksi, Status, Durasi, Waktu buka, Waktu tutup, Kuotasi pembukaan, Kuotasi penutupan

### BantuanFragment
- Grid 2×2: Dukungan (24/7), Pusat Bantuan, Edukasi, Tutorial Trading
- Static content, tap → AlertDialog dengan placeholder content

### ProfileFragment
- Avatar (ikon orang), nama user, ID user (dari SharedPreferences)
- Status bar (Basic, progress)
- **Statistik cards** (horizontal RecyclerView): Total Transaksi, Win Rate, Profit Terbaik
- Toggle tema Dark/Light → `AppCompatDelegate.setDefaultNightMode()` + simpan ke SharedPreferences
- Tidak ada tombol ke Pengaturan Notifikasi (dihapus)

---

## 7. Theme

- `values/themes.xml` → `Theme.Frexa` extends `Theme.Material3.Light.NoActionBar`
- `values-night/themes.xml` → `Theme.Frexa` extends `Theme.Material3.DayNight.NoActionBar`
- Color tokens sesuai desain:
  - Primary green: `#3EE87A`
  - Background dark: `#0A0B0D`
  - Surface: `#17181B`
  - Text primary: `#F4F5F7`
  - Up (profit): `#36E07A`
  - Down (loss): `#FF5D5D`
- Toggle tersimpan di SharedPreferences, diterapkan di `MainActivity.onCreate()` sebelum `setContentView`

---

## 8. Branch & Commit Strategy

**Branch model:**
```
main        ← merge dari develop setelah stable
develop     ← integration
  feature/project-setup
  feature/login
  feature/home
  feature/coingecko-api
  feature/terminal-chart
  feature/trading-engine
  feature/transaksi
  feature/trade-detail
  feature/ai-analysis
  feature/deposit-flow
  feature/offline-cache
  feature/theme
  feature/bantuan-profile
```

**Semantic commit format:**
```
<type>(<scope>): <deskripsi singkat>

feat(login): tambah validasi field email dan telepon
fix(api): perbaiki crash saat response CoinGecko null
refactor(trading): pisahkan logika expire trade ke TradingRepository
chore(deps): tambah dependensi Retrofit dan Room ke build.gradle
```

---

## 9. Dependencies (libs.versions.toml)

```toml
# Navigation
navigation = "2.7.7"
# Retrofit
retrofit = "2.9.0"
okhttp = "4.12.0"
# Room
room = "2.6.1"
# Glide (coin images)
glide = "4.16.0"
# Gson
gson = "2.10.1"
# MPAndroidChart (candlestick fallback jika Canvas terlalu kompleks)
# → custom Canvas implementation diutamakan
```

Semua plugin Java-compatible (tidak ada Kotlin-specific library).

---

## 10. Bare Minimum Checklist

| Requirement | Implementasi |
|---|---|
| ≥ 2 Activity | `MainActivity` + `TradeDetailActivity` |
| MainActivity = Launcher | ✅ `intent-filter MAIN/LAUNCHER` |
| Intent antar Activity | `TransaksiFragment → TradeDetailActivity`, `TradeDetailActivity → MainActivity` |
| RecyclerView | HomeFragment (2x), TerminalFragment assets, TransaksiFragment (2x), dll |
| ≥ 2 Fragment | ✅ 12 Fragment |
| Navigation Component | ✅ NavHostFragment + NavController |
| Background Thread | Executor (Room ops), ScheduledExecutor (price polling), Handler (UI update) |
| Networking Retrofit | CoinGecko API (harga, OHLC, market chart) + Gemini API |
| Tombol Refresh (offline) | ✅ Banner + Refresh button saat no network |
| Local Persistence | Room (trades, cached prices) + SharedPreferences (user, balance, theme) |
| Data tampil offline | ✅ dari CachedPriceEntity |
| Dark / Light Theme | ✅ `**values**-night/themes.xml` + toggle di Profile |****