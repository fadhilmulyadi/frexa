# Binance Real-Time Chart Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Unblock koneksi Binance (SSL fix) dan hapus double-subscription WebSocket agar real-time chart berfungsi penuh.

**Architecture:** Tambah `network_security_config.xml` agar Android mempercayai CA Binance, referensikan di manifest, lalu sederhanakan `startPricePolling()` di `CryptoViewModel` supaya tidak re-subscribe stream yang sudah aktif.

**Tech Stack:** Android (Java), OkHttp 4.x, Retrofit 2, Binance REST + WebSocket

---

## File Map

| File | Status | Perubahan |
|------|--------|-----------|
| `app/src/main/res/xml/network_security_config.xml` | Baru | Trust-anchor config untuk sistem + user CA (debug) |
| `app/src/main/AndroidManifest.xml` | Ubah | Tambah `android:networkSecurityConfig` ke `<application>` |
| `app/src/main/java/com/diellabs/frexa/viewmodel/CryptoViewModel.java` | Ubah | Fix `startPricePolling()` — tidak re-subscribe jika sudah connected |

---

## Task 1: Buat network_security_config.xml

**Files:**
- Create: `app/src/main/res/xml/network_security_config.xml`

- [ ] **Step 1: Buat direktori res/xml jika belum ada, lalu buat file**

Buat file `app/src/main/res/xml/network_security_config.xml` dengan isi:

```xml
<?xml version="1.0" encoding="utf-8"?>
<network-security-config>
    <base-config cleartextTrafficPermitted="false">
        <trust-anchors>
            <certificates src="system" />
        </trust-anchors>
    </base-config>
    <debug-overrides>
        <trust-anchors>
            <certificates src="system" />
            <certificates src="user" />
        </trust-anchors>
    </debug-overrides>
</network-security-config>
```

`base-config` mewajibkan HTTPS dan mempercayai CA sistem (mencakup DigiCert yang dipakai Binance).  
`debug-overrides` juga mempercayai CA yang diinstal user — berguna untuk Charles Proxy / debug di emulator.

- [ ] **Step 2: Commit**

```bash
git add app/src/main/res/xml/network_security_config.xml
git commit -m "fix(ssl): add network_security_config trusting system CAs"
```

---

## Task 2: Referensikan network_security_config di AndroidManifest.xml

**Files:**
- Modify: `app/src/main/AndroidManifest.xml`

- [ ] **Step 1: Edit tag `<application>` di AndroidManifest.xml**

Tambah atribut `android:networkSecurityConfig` ke tag `<application>`.

Sebelum:
```xml
<application
    android:allowBackup="true"
    android:icon="@mipmap/ic_launcher"
    android:label="@string/app_name"
    android:roundIcon="@mipmap/ic_launcher_round"
    android:supportsRtl="true"
    android:theme="@style/Theme.Frexa">
```

Sesudah:
```xml
<application
    android:allowBackup="true"
    android:icon="@mipmap/ic_launcher"
    android:label="@string/app_name"
    android:networkSecurityConfig="@xml/network_security_config"
    android:roundIcon="@mipmap/ic_launcher_round"
    android:supportsRtl="true"
    android:theme="@style/Theme.Frexa">
```

- [ ] **Step 2: Verifikasi build**

Jalankan build dan pastikan tidak ada error:

```
./gradlew assembleDebug
```

Expected output: `BUILD SUCCESSFUL`

Jika ada error `Resource not found` — pastikan file ada di `app/src/main/res/xml/network_security_config.xml` (bukan di sub-folder lain).

- [ ] **Step 3: Commit**

```bash
git add app/src/main/AndroidManifest.xml
git commit -m "fix(ssl): reference network_security_config in manifest"
```

---

## Task 3: Fix double-subscription di CryptoViewModel

**Files:**
- Modify: `app/src/main/java/com/diellabs/frexa/viewmodel/CryptoViewModel.java`

**Context:** `TerminalFragment.onViewCreated()` memanggil `setActiveCoin()` yang sudah memanggil `wsManager.connect()` + `wsManager.switchStream()`. Kemudian `onResume()` memanggil `startPricePolling()` yang memanggil `wsManager.connect()` + `wsManager.subscribe()` lagi, mengirim SUBSCRIBE duplikat ke Binance.

- [ ] **Step 1: Update method `startPricePolling()` di CryptoViewModel.java**

Cari method ini di file `app/src/main/java/com/diellabs/frexa/viewmodel/CryptoViewModel.java` (sekitar baris 104–110):

```java
public void startPricePolling() {
    wsManager.connect();
    if (activeBinanceSymbol != null && activeInterval != null) {
        String stream = CoinSymbolMapper.toStreamName(activeBinanceSymbol, activeInterval);
        wsManager.subscribe(stream);
    }
}
```

Ganti dengan:
```java
public void startPricePolling() {
    if (!wsManager.isConnected()) {
        wsManager.connect();
    }
}
```

Stream sudah di-subscribe via `setActiveCoin()` atau `setTimeframe()`. `startPricePolling()` cukup memastikan WebSocket reconnect jika putus (misalnya setelah `onPause()`).

- [ ] **Step 2: Verifikasi build**

```
./gradlew assembleDebug
```

Expected output: `BUILD SUCCESSFUL`

- [ ] **Step 3: Commit**

```bash
git add app/src/main/java/com/diellabs/frexa/viewmodel/CryptoViewModel.java
git commit -m "fix(websocket): remove double-subscription in startPricePolling"
```

---

## Verifikasi Manual Akhir

Setelah ketiga task selesai, jalankan app di emulator/device dan buka TerminalFragment:

1. Buka logcat filter `okhttp.OkHttpClient` — pastikan request ke `api.binance.com` berhasil (bukan SSL error)
2. Buka logcat filter `BinanceWS` — pastikan muncul `WebSocket connected` dan `Subscribed: btcusdt@kline_1m`
3. Pastikan chart bergerak (candle terakhir update setiap tick)
4. Pastikan tidak ada log `Subscribed:` yang muncul dua kali untuk stream yang sama saat pertama buka fragment
