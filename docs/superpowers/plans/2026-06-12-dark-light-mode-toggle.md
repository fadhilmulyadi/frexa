# Dark/Light Mode Toggle Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add dark/light mode toggling to Frexa via a sun/moon icon in the HomeFragment header, persisted across sessions, defaulting to dark mode.

**Architecture:** Android's `values/` (light) + `values-night/` (dark) resource system handles automatic color swapping via existing `@color/frx_*` tokens — no drawable-night variants needed. A `ThemeManager` utility reads `UserPrefs` and calls `AppCompatDelegate.setDefaultNightMode()`. A `FrxApplication` class applies the saved theme at app startup before any Activity renders.

**Tech Stack:** AppCompatDelegate (AndroidX AppCompat), SharedPreferences via `UserPrefs`, XML vector drawables, Android resource qualifier `values-night/`

---

### Task 1: Add theme preference to UserPrefs

**Files:**
- Modify: `app/src/main/java/com/diellabs/frexa/util/UserPrefs.java`

- [ ] **Step 1: Add KEY_DARK_MODE constant and accessor methods**

Open `UserPrefs.java`. After the line:
```java
private static final String KEY_DAILY_SNAPSHOTS = "daily_snapshots";
```
Add:
```java
private static final String KEY_DARK_MODE = "is_dark_mode";
```

Then add these two methods anywhere in the public methods section:
```java
public boolean isDarkMode() { return prefs.getBoolean(KEY_DARK_MODE, true); }
public void setDarkMode(boolean v) { prefs.edit().putBoolean(KEY_DARK_MODE, v).apply(); }
```

Default is `true` so existing users stay in dark mode on first run.

- [ ] **Step 2: Commit**
```bash
git add app/src/main/java/com/diellabs/frexa/util/UserPrefs.java
git commit -m "feat: add dark mode preference to UserPrefs"
```

---

### Task 2: Create ThemeManager utility

**Files:**
- Create: `app/src/main/java/com/diellabs/frexa/util/ThemeManager.java`

- [ ] **Step 1: Create ThemeManager.java**

```java
package com.diellabs.frexa.util;

import androidx.appcompat.app.AppCompatDelegate;

public class ThemeManager {

    public static void apply(UserPrefs prefs) {
        AppCompatDelegate.setDefaultNightMode(
            prefs.isDarkMode()
                ? AppCompatDelegate.MODE_NIGHT_YES
                : AppCompatDelegate.MODE_NIGHT_NO
        );
    }

    public static void toggle(UserPrefs prefs) {
        boolean newDark = !prefs.isDarkMode();
        prefs.setDarkMode(newDark);
        AppCompatDelegate.setDefaultNightMode(
            newDark
                ? AppCompatDelegate.MODE_NIGHT_YES
                : AppCompatDelegate.MODE_NIGHT_NO
        );
    }
}
```

- [ ] **Step 2: Commit**
```bash
git add app/src/main/java/com/diellabs/frexa/util/ThemeManager.java
git commit -m "feat: add ThemeManager utility for day/night switching"
```

---

### Task 3: Create FrxApplication and register in manifest

**Files:**
- Create: `app/src/main/java/com/diellabs/frexa/FrxApplication.java`
- Modify: `app/src/main/AndroidManifest.xml`

- [ ] **Step 1: Create FrxApplication.java**

```java
package com.diellabs.frexa;

import android.app.Application;
import com.diellabs.frexa.util.ThemeManager;
import com.diellabs.frexa.util.UserPrefs;

public class FrxApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        ThemeManager.apply(new UserPrefs(this));
    }
}
```

- [ ] **Step 2: Register in AndroidManifest.xml**

In `app/src/main/AndroidManifest.xml`, add `android:name=".FrxApplication"` to the `<application` tag:

```xml
<application
    android:name=".FrxApplication"
    android:allowBackup="true"
    android:icon="@mipmap/ic_launcher"
    android:label="@string/app_name"
    android:networkSecurityConfig="@xml/network_security_config"
    android:roundIcon="@mipmap/ic_launcher_round"
    android:supportsRtl="true"
    android:theme="@style/Theme.Frexa">
```

- [ ] **Step 3: Commit**
```bash
git add app/src/main/java/com/diellabs/frexa/FrxApplication.java
git add app/src/main/AndroidManifest.xml
git commit -m "feat: add FrxApplication to apply saved theme before first render"
```

---

### Task 4: Split color tokens into light (values/) and dark (values-night/)

**Files:**
- Modify: `app/src/main/res/values/colors.xml` → light mode values
- Create: `app/src/main/res/values-night/colors.xml` → dark mode (current values)

- [ ] **Step 1: Create `app/src/main/res/values-night/` directory, then create `colors.xml`**

Contents of `app/src/main/res/values-night/colors.xml` — exact copy of current dark values:

```xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <!-- Brand -->
    <color name="frx_amber">#E8B25A</color>
    <color name="frx_amber_dark">#9A6A1F</color>

    <!-- Semantic -->
    <color name="frx_up">#3DD68C</color>
    <color name="frx_down">#FF6259</color>

    <!-- Dark surfaces -->
    <color name="frx_bg">#0B0D0F</color>
    <color name="frx_card">#13171B</color>
    <color name="frx_surface">#1B2025</color>

    <!-- Text -->
    <color name="white">#FFFFFF</color>
    <color name="frx_text">#EDF1F4</color>
    <color name="frx_text_2">#9AA4AD</color>
    <color name="frx_text_3">#5C666F</color>
    <color name="frx_hairline">#0FFFFFFF</color>
    <color name="frx_cta_text">#07130C</color>

    <!-- Opacity variants -->
    <color name="frx_up_12">#1F3DD68C</color>
    <color name="frx_down_12">#1FFF6259</color>
    <color name="frx_amber_07">#12E8B25A</color>
    <color name="frx_amber_22">#38E8B25A</color>

    <!-- Blue accent -->
    <color name="frx_blue_accent">#8AA7C4</color>
</resources>
```

- [ ] **Step 2: Replace `app/src/main/res/values/colors.xml` with light mode values**

```xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <!-- Brand -->
    <color name="frx_amber">#E8B25A</color>
    <color name="frx_amber_dark">#9A6A1F</color>

    <!-- Semantic — darkened for contrast on light background -->
    <color name="frx_up">#1F9D5B</color>
    <color name="frx_down">#E03B3B</color>

    <!-- Light surfaces -->
    <color name="frx_bg">#F7F8FA</color>
    <color name="frx_card">#FFFFFF</color>
    <color name="frx_surface">#EFF1F4</color>

    <!-- Text -->
    <color name="white">#FFFFFF</color>
    <color name="frx_text">#1B1D1F</color>
    <color name="frx_text_2">#6E747B</color>
    <color name="frx_text_3">#9AA4AD</color>
    <color name="frx_hairline">#12000000</color>
    <color name="frx_cta_text">#07130C</color>

    <!-- Opacity variants -->
    <color name="frx_up_12">#1F1F9D5B</color>
    <color name="frx_down_12">#1FE03B3B</color>
    <color name="frx_amber_07">#12E8B25A</color>
    <color name="frx_amber_22">#38E8B25A</color>

    <!-- Blue accent -->
    <color name="frx_blue_accent">#8AA7C4</color>
</resources>
```

- [ ] **Step 3: Commit**
```bash
git add app/src/main/res/values/colors.xml
git add app/src/main/res/values-night/colors.xml
git commit -m "feat: split color tokens into light (values/) and dark (values-night/) variants"
```

---

### Task 5: Split theme into day/night variants

**Files:**
- Modify: `app/src/main/res/values/themes.xml`
- Create: `app/src/main/res/values-night/themes.xml`

- [ ] **Step 1: Update `values/themes.xml` — change parent to DayNight**

```xml
<resources>
    <style name="Theme.Frexa" parent="Theme.Material3.DayNight.NoActionBar">
        <item name="android:fontFamily">@font/schibsted_grotesk</item>
        <item name="colorPrimary">@color/frx_amber</item>
        <item name="colorOnPrimary">@color/frx_bg</item>
        <item name="colorSurface">@color/frx_card</item>
        <item name="colorOnSurface">@color/frx_text</item>
        <item name="android:windowBackground">@color/frx_bg</item>
        <item name="android:statusBarColor">@color/frx_bg</item>
        <item name="android:navigationBarColor">@color/frx_bg</item>
    </style>
</resources>
```

- [ ] **Step 2: Create `app/src/main/res/values-night/themes.xml` — keep Dark parent for night**

```xml
<resources>
    <style name="Theme.Frexa" parent="Theme.Material3.Dark.NoActionBar">
        <item name="android:fontFamily">@font/schibsted_grotesk</item>
        <item name="colorPrimary">@color/frx_amber</item>
        <item name="colorOnPrimary">@color/frx_bg</item>
        <item name="colorSurface">@color/frx_card</item>
        <item name="colorOnSurface">@color/frx_text</item>
        <item name="android:windowBackground">@color/frx_bg</item>
        <item name="android:statusBarColor">@color/frx_bg</item>
        <item name="android:navigationBarColor">@color/frx_bg</item>
    </style>
</resources>
```

- [ ] **Step 3: Commit**
```bash
git add app/src/main/res/values/themes.xml
git add app/src/main/res/values-night/themes.xml
git commit -m "feat: add DayNight/Dark theme variants for light and dark mode"
```

---

### Task 6: Add hairline stroke to bg_card for light mode depth

**Files:**
- Modify: `app/src/main/res/drawable/bg_card.xml`

- [ ] **Step 1: Add adaptive stroke to bg_card.xml**

```xml
<?xml version="1.0" encoding="utf-8"?>
<shape xmlns:android="http://schemas.android.com/apk/res/android"
    android:shape="rectangle">
    <solid android:color="@color/frx_card" />
    <stroke android:width="1dp" android:color="@color/frx_hairline" />
    <corners android:radius="14dp" />
</shape>
```

In dark mode `frx_hairline` = `#0FFFFFFF` (6% white — nearly invisible, no visual change). In light mode `frx_hairline` = `#12000000` (7% black — subtle border separating white card from `#F7F8FA` background).

- [ ] **Step 2: Commit**
```bash
git add app/src/main/res/drawable/bg_card.xml
git commit -m "feat: add adaptive hairline stroke to bg_card for light mode depth"
```

---

### Task 7: Create sun and moon vector drawables

**Files:**
- Create: `app/src/main/res/drawable/ic_sun.xml`
- Create: `app/src/main/res/drawable/ic_moon.xml`

- [ ] **Step 1: Create `ic_sun.xml`** (shown when in dark mode — tap switches to light)

```xml
<?xml version="1.0" encoding="utf-8"?>
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="24dp"
    android:height="24dp"
    android:viewportWidth="24"
    android:viewportHeight="24">
    <path
        android:fillColor="@color/frx_text_2"
        android:pathData="M12,7c-2.76,0-5,2.24-5,5s2.24,5,5,5s5-2.24,5-5S14.76,7,12,7zM2,13h2c0.55,0,1-0.45,1-1s-0.45-1-1-1H2c-0.55,0-1,0.45-1,1S1.45,13,2,13zM20,13h2c0.55,0,1-0.45,1-1s-0.45-1-1-1h-2c-0.55,0-1,0.45-1,1S19.45,13,20,13zM11,2v2c0,0.55,0.45,1,1,1s1-0.45,1-1V2c0-0.55-0.45-1-1-1S11,1.45,11,2zM11,20v2c0,0.55,0.45,1,1,1s1-0.45,1-1v-2c0-0.55-0.45-1-1-1S11,19.45,11,20zM5.99,4.58c-0.39-0.39-1.03-0.39-1.41,0c-0.39,0.39-0.39,1.03,0,1.41l1.06,1.06c0.39,0.39,1.03,0.39,1.41,0s0.39-1.03,0-1.41L5.99,4.58zM18.36,16.95c-0.39-0.39-1.03-0.39-1.41,0c-0.39,0.39-0.39,1.03,0,1.41l1.06,1.06c0.39,0.39,1.03,0.39,1.41,0c0.39-0.39,0.39-1.03,0-1.41L18.36,16.95zM19.42,5.99c0.39-0.39,0.39-1.03,0-1.41c-0.39-0.39-1.03-0.39-1.41,0l-1.06,1.06c-0.39,0.39-0.39,1.03,0,1.41s1.03,0.39,1.41,0L19.42,5.99zM7.05,18.36c0.39-0.39,0.39-1.03,0-1.41c-0.39-0.39-1.03-0.39-1.41,0l-1.06,1.06c-0.39,0.39-0.39,1.03,0,1.41s1.03,0.39,1.41,0L7.05,18.36z" />
</vector>
```

- [ ] **Step 2: Create `ic_moon.xml`** (shown when in light mode — tap switches to dark)

```xml
<?xml version="1.0" encoding="utf-8"?>
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="24dp"
    android:height="24dp"
    android:viewportWidth="24"
    android:viewportHeight="24">
    <path
        android:fillColor="@color/frx_text_2"
        android:pathData="M12,3c-4.97,0-9,4.03-9,9s4.03,9,9,9s9-4.03,9-9c0-0.46-0.04-0.92-0.1-1.36c-0.98,1.37-2.58,2.26-4.4,2.26c-2.98,0-5.4-2.42-5.4-5.4c0-1.81,0.89-3.42,2.26-4.4C12.92,3.04,12.46,3,12,3z" />
</vector>
```

- [ ] **Step 3: Commit**
```bash
git add app/src/main/res/drawable/ic_sun.xml app/src/main/res/drawable/ic_moon.xml
git commit -m "feat: add sun and moon vector drawables for theme toggle"
```

---

### Task 8: Add toggle button to HomeFragment header

**Files:**
- Modify: `app/src/main/res/layout/fragment_home.xml`

- [ ] **Step 1: Replace the header LinearLayout in fragment_home.xml**

Find the `<!-- Header -->` section and replace it entirely with:

```xml
<!-- Header -->
<LinearLayout
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:gravity="center_vertical"
    android:orientation="horizontal">

    <View
        android:id="@+id/logo_icon"
        android:layout_width="28dp"
        android:layout_height="28dp"
        android:background="@drawable/bg_logo" />

    <TextView
        android:layout_width="0dp"
        android:layout_height="wrap_content"
        android:layout_weight="1"
        android:layout_marginStart="9dp"
        android:text="Frexa"
        android:textColor="@color/frx_text"
        android:textSize="17sp"
        android:fontFamily="@font/schibsted_grotesk"
        android:textStyle="bold" />

    <ImageView
        android:id="@+id/btn_theme"
        android:layout_width="32dp"
        android:layout_height="32dp"
        android:layout_marginEnd="8dp"
        android:background="@drawable/bg_surface"
        android:padding="6dp"
        android:scaleType="center"
        android:src="@drawable/ic_sun"
        android:contentDescription="Toggle theme" />

    <TextView
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="@string/label_live"
        android:textColor="@color/frx_up"
        android:textSize="11sp"
        android:fontFamily="@font/schibsted_grotesk"
        android:textStyle="bold"
        android:background="@drawable/bg_badge_live"
        android:paddingHorizontal="11dp"
        android:paddingVertical="5dp" />
</LinearLayout>
```

- [ ] **Step 2: Commit**
```bash
git add app/src/main/res/layout/fragment_home.xml
git commit -m "feat: add theme toggle icon to HomeFragment header"
```

---

### Task 9: Wire toggle button in HomeFragment

**Files:**
- Modify: `app/src/main/java/com/diellabs/frexa/ui/home/HomeFragment.java`

- [ ] **Step 1: Add import statements**

At the top of `HomeFragment.java`, add these imports if not already present:

```java
import android.widget.ImageView;
import com.diellabs.frexa.util.ThemeManager;
```

- [ ] **Step 2: Wire the toggle button in onViewCreated**

In `onViewCreated`, directly after the line `userPrefs = new UserPrefs(requireContext());`, add:

```java
ImageView btnTheme = view.findViewById(R.id.btn_theme);
btnTheme.setImageResource(userPrefs.isDarkMode() ? R.drawable.ic_sun : R.drawable.ic_moon);
btnTheme.setOnClickListener(v -> {
    ThemeManager.toggle(userPrefs);
    requireActivity().recreate();
});
```

Logic: dark mode → show sun (tap = go light); light mode → show moon (tap = go dark).

- [ ] **Step 3: Commit**
```bash
git add app/src/main/java/com/diellabs/frexa/ui/home/HomeFragment.java
git commit -m "feat: wire theme toggle in HomeFragment"
```

---

### Task 10: Build and verify

- [ ] **Step 1: Build the project**
```
./gradlew assembleDebug
```
Expected: BUILD SUCCESSFUL with no errors.

- [ ] **Step 2: Install and verify dark mode (default)**
Launch app on device/emulator. Verify:
- App opens in dark mode (dark background `#0B0D0F`, light text)
- HomeFragment header shows a sun icon (☀) next to the LIVE badge

- [ ] **Step 3: Toggle to light mode**
Tap the sun icon. Verify:
- App recreates and renders in light mode (`#F7F8FA` background, dark text `#1B1D1F`)
- Icon changes to moon (🌙)
- Cards appear white with a subtle border
- Green chips show `#1F9D5B`, red chips show `#E03B3B`
- Amber accents unchanged

- [ ] **Step 4: Verify persistence**
Kill the app. Reopen. Verify the app opens in the last-selected theme.

- [ ] **Step 5: Toggle back and verify all screens**
Switch back to dark mode. Navigate through Markets, Portfolio, Orders, Watchlist, Coin Detail — verify no broken colors or missing contrast in either theme.
