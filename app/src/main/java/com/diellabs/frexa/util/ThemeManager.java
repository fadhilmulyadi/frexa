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
