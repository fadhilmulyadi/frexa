# Frexa UI Redesign — Design Spec
_2026-06-11_

## Goal
Align the entire Android app's visual appearance with the Frexa Mobile HTML prototype (design bundle at `api.anthropic.com/v1/design/h/PsvwpvHcKVVopZMesSWpCg`). The current v1.0 was built from a text description only; this redesign makes it match the pixel-level screenshots from that bundle.

## Branch
`feature/ui-redesign` branched from `main`

---

## 1. Theme & Color

**Always dark — no toggle.**
- Remove `values-night/themes.xml` (it'll be the only theme).
- Change `values/themes.xml` parent to `Theme.Material3.Dark.NoActionBar`.
- Set all window/status/nav bar colors to `#0A0B0D`.
- Delete dark-mode switch from ProfileFragment.

**Color tokens** (already in `colors.xml`, just verify they're used everywhere):

| Token | Hex |
|---|---|
| `frx_bg` | `#0A0B0D` |
| `frx_bg_2` | `#101113` |
| `frx_surface` | `#17181B` |
| `frx_surface_2` | `#1F2024` |
| `frx_surface_3` | `#292A2F` |
| `frx_text` | `#F4F5F7` |
| `frx_text_2` | `#9A9DA3` |
| `frx_text_3` | `#63666C` |
| `frx_green` | `#3EE87A` |
| `frx_lime` | `#BDF94E` |
| `frx_up` | `#36E07A` |
| `frx_down` | `#FF5D5D` |
| `frx_gold` | `#F0B429` |

Add to `colors.xml`:
- `frx_bg_2 = #101113`
- `frx_hairline = #FFFFFF12` (7% white)
- `frx_cta_text = #04210F` (dark text on green gradient button)

---

## 2. Typography

**Font: Plus Jakarta Sans** via Android Downloadable Fonts (Google Fonts).
- Add `res/font/plus_jakarta_sans.xml` (font family XML).
- Set as `android:fontFamily` in theme for all text.
- Weights used: 400 (regular), 500 (medium), 600 (semibold), 700 (bold), 800 (extrabold).

---

## 3. Drawables to Add

| File | Purpose |
|---|---|
| `drawable/btn_cta_grad.xml` | Gradient `#19E29A → #4EF05A → #7DFF4A` (100deg), 16dp radius — for Deposit + primary CTAs |
| `drawable/bg_surface.xml` | Solid `frx_surface` with 16dp radius — card background |
| `drawable/bg_surface_2.xml` | Solid `frx_surface_2` with 14dp radius — stepper/field background |
| `drawable/bg_chip_active.xml` | Solid `frx_lime` 20dp radius — active filter chip |
| `drawable/bg_chip_inactive.xml` | Transparent, 1dp stroke `frx_hairline`, 20dp radius — inactive chip |
| `drawable/ic_market.xml` | Vector icon for Market bottom nav tab (shopping bag outline) |

---

## 4. Screen-by-Screen Layout Changes

### 4.1 `activity_main.xml` + `MainActivity.java`
- Bottom nav background: `frx_bg` (not colorSurface).
- Bottom nav item text/icon: inactive = `frx_text_3`, active = `frx_text`.
- No shadow/elevation on bottom nav.
- Add Market item to `bottom_nav_menu.xml`.
- Remove dark mode AppCompatDelegate logic from MainActivity.

### 4.2 `fragment_home.xml`
Current top bar (avatar left, text center, bell right) → **new top bar**:
- Left: circular avatar button (36dp, `frx_surface` background, person icon in `frx_text_2`)
- Center (vertical stack): `tv_account_name` in `frx_text_2` 13sp + dropdown arrow, `tv_balance` in `frx_text` 24sp bold
- Right: bell icon button (24dp, `frx_text`)

Below top bar:
- `btn_deposit`: full-width, 56dp height, 16dp radius, gradient bg (`btn_cta_grad`), text `#04210F` 17sp 800-weight "Deposit"
- Remove "Market Movers" label → rename section to **Promo cards carousel** (horizontal `RecyclerView` with 160×100dp cards, `bg_surface` card background, placeholder gradient image + text label overlaid)
- Remove existing `TabLayout` for assets → replace with **horizontal chip row** (`HorizontalScrollView` + `LinearLayout` with chips: "Fixed Time", "Forex", "Stocks")
- Asset list (`rv_assets`) items keep same structure but use `bg_surface` cards

### 4.3 `fragment_terminal.xml`
New top bar (2 columns):
- Left: person avatar circle (36dp) + `frx_surface` bg
- Center: account name `frx_text_2` 13sp + balance `frx_text` 20sp bold (stacked)
- Right: wallet/bag icon button (36dp green bg)

Chart: keep `CandlestickChartView`, full remaining height.

Bottom controls (new structure):
- Row 1: "Mode Fixed Time" label left, "Profit: +Đ0,95" right (green)
- Row 2: two `Stepper` views side-by-side — Duration stepper (−/+, label "1 mnt") and Amount stepper (−/+, label "Đ1")
- Row 3: three buttons — Turun (red, 40% width), clock icon button (48dp square, `frx_surface_2`), Naik (green gradient, 40% width)

### 4.4 `fragment_transaksi.xml`
- Title "Transaksi" bold 22sp at top
- Tab row: "Fixed Time · N", "Forex", "Stocks" — underline style tabs
- Summary row: "Nominal total Đ1,00" left, "Profit dan rugi +Đ0,95" right (green)
- Open trades `RecyclerView` with live countdown
- "Riwayat" header + "Tampilkan Semua ›" right-aligned link
- History `RecyclerView`

### 4.5 `fragment_profile.xml`
Completely redesign:
- Back button top-left
- Avatar circle (80dp, `frx_surface_2` bg, person icon)
- Name bold 22sp, "ID XXXXXXXX 📋" copy button
- Status Basic card (`bg_surface`, green triangle icon, progress bar, arrow)
- Boost Cubes row card (`bg_surface`, cube icon, arrow)
- Program Rujukan row card (`bg_surface`, link icon, arrow)
- **Leaderboard** heading + "Peringkat Anda ›" right
- Two equal-width sub-cards: "Trading terbaik" + "Profit"
- Pengaturan full-width button at bottom (`bg_surface`, gear icon)
- **Remove** Mode Gelap switch entirely

### 4.6 `fragment_bantuan.xml`
- Title "Bantuan" bold 24sp
- 2×2 card grid (each card `bg_surface`, 16dp radius):
  - Dukungan: question-circle icon, title "Dukungan", subtitle "Kami hadir buat Anda 24/7"
  - Pusat Bantuan: info-circle icon, "Pusat Bantuan", "Memahami platform"
  - Edukasi: graduation cap icon, "Edukasi", "Menambah pengetahuan Anda"
  - Tutorial Trading: chart icon, "Tutorial Trading", "Cara buka transaksi"
- Icons: stroke-style vector drawables (not emoji)

### 4.7 `fragment_notification.xml`
- Title "Notifikasi" bold 24sp + gear icon top-right
- Toggle list (8 items): Terminal, Sinyal, Aktivitas, Hadiah, Platform, Edukasi, Berita Trading, Notifikasi Singkat
- Each item: title bold 15sp, subtitle `frx_text_2` 13sp, toggle right (green when on)
- Empty state (no notifications): center icon + "Tidak Ada Notifikasi Baru"

### 4.8 `bottom_sheet_account.xml`
- Grabber bar at top
- "Akun" heading
- Account list item: logo tile left, "IDR Akun Anda" + balance below, 3-dot menu right
- Selected account: shows "Deposit" button below the item
- "Tambah Akun" full-width button at bottom

### 4.9 `bottom_sheet_deposit.xml`
Full-screen sheet, 4-step internal navigation:
1. **Main**: Metode pembayaran field, Nominal field, Pilih Kode Promo field, "Selanjutnya" CTA, lock SSL note
2. **Method picker**: category chips (Semua/Direkomendasikan/Virtual Account/Kartu Bank/Kripto/E-Wallet), list of payment methods
3. **Nominal input**: amount input + quick-pick chips (USD 10/20/30/100/120)
4. **Confirmation**: payment icon, amount, info rows, "Konfirmasi" CTA → QR screen

### 4.10 New: `fragment_market.xml` + `MarketFragment.java`
- "Market" title + search icon top-right
- "Paling aktif" section: horizontal `RecyclerView` with 3 stock cards (colored tile icon, name, price, % change)
- Filter chips: Semua / Mata Uang / Saham / Kripto
- Asset list `RecyclerView` with pair badge icon, name, type, price, % change (green/red)

### 4.11 `item_*.xml` updates
- `item_coin.xml`: pair badge left (stacked flag icons), name+type, price right, % change right-bottom
- `item_open_trade.xml`: pair badge, name·profitability%, direction+amount, countdown timer, PnL green/red
- `item_history_trade.xml`: same structure but shows duration, final PnL

---

## 5. Icons
Use **Lucide icons** (preferred) or **Google Material Icons** — imported as vector XML drawables via Android Studio "Import Vector Asset". Do NOT draw icons from scratch.

Mapping:
| File | Lucide name | Material fallback |
|---|---|---|
| `ic_person.xml` | `user` | `person` |
| `ic_bell.xml` | `bell` | `notifications` |
| `ic_wallet.xml` | `wallet` | `account_balance_wallet` |
| `ic_market.xml` | `shopping-bag` | `storefront` |
| `ic_gear.xml` | `settings` | `settings` |
| `ic_chevron_right.xml` | `chevron-right` | `chevron_right` |
| `ic_clock.xml` | `clock` | `schedule` |
| `ic_plus.xml` | `plus` | `add` |
| `ic_minus.xml` | `minus` | `remove` |
| `ic_question_circle.xml` | `circle-help` | `help_outline` |
| `ic_info_circle.xml` | `info` | `info_outline` |
| `ic_graduation.xml` | `graduation-cap` | `school` |
| `ic_chart_line.xml` | `trending-up` | `trending_up` |
| `ic_copy.xml` | `copy` | `content_copy` |
| `ic_triangle_up.xml` | `triangle` | `arrow_drop_up` |

---

## 6. Out of Scope
- No changes to data layer, ViewModels, Room, Retrofit
- No changes to business logic in Fragment `.java` files beyond removing theme-toggle code
- Login screen layout: minor cleanup only (already close to design)

## 6b. In Scope (nav changes)
- `nav_graph.xml`: add `<fragment>` entry for `MarketFragment` (destination only, no new actions needed — bottom nav navigates to it directly)
- `bottom_nav_menu.xml`: add Market item pointing to the new destination

---

## 7. Success Criteria
- App launches always in dark mode
- Home screen: top bar with centered balance, gradient Deposit button, promo carousel, chip filter
- Terminal: correct 3-row bottom controls (mode label, steppers, Turun/clock/Naik)
- Transaksi: tabs + summary row + Riwayat with "Tampilkan Semua"
- Profile: matches design screenshot exactly (avatar, name/ID, status card, pengaturan button)
- Market: new functional tab
- Bantuan: 2×2 dark cards with vector icons
- All primary CTAs use green gradient button style
- Font is Plus Jakarta Sans throughout
