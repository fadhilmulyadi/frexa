package com.diellabs.frexa.util;

import android.content.Context;
import android.content.SharedPreferences;

public class UserPrefs {
    private static final String PREFS_NAME = "frexa_prefs";
    private static final String KEY_LOGGED_IN = "is_logged_in";
    private static final String KEY_USER_NAME = "user_name";
    private static final String KEY_USER_EMAIL = "user_email";
    private static final String KEY_USER_PHONE = "user_phone";
    private static final String KEY_BALANCE = "virtual_balance";
    private static final String KEY_THEME = "theme_mode";
    private static final String KEY_ACTIVE_COIN = "active_coin_id";

    private final SharedPreferences prefs;

    public UserPrefs(Context context) {
        prefs = context.getApplicationContext()
                .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public boolean isLoggedIn() { return prefs.getBoolean(KEY_LOGGED_IN, false); }
    public void setLoggedIn(boolean v) { prefs.edit().putBoolean(KEY_LOGGED_IN, v).apply(); }

    public String getUserName() { return prefs.getString(KEY_USER_NAME, ""); }
    public void setUserName(String v) { prefs.edit().putString(KEY_USER_NAME, v).apply(); }

    public String getUserEmail() { return prefs.getString(KEY_USER_EMAIL, ""); }
    public void setUserEmail(String v) { prefs.edit().putString(KEY_USER_EMAIL, v).apply(); }

    public String getUserPhone() { return prefs.getString(KEY_USER_PHONE, ""); }
    public void setUserPhone(String v) { prefs.edit().putString(KEY_USER_PHONE, v).apply(); }

    public float getBalance() { return prefs.getFloat(KEY_BALANCE, 10000.0f); }
    public void setBalance(float v) { prefs.edit().putFloat(KEY_BALANCE, v).apply(); }

    public String getThemeMode() { return prefs.getString(KEY_THEME, "dark"); }
    public void setThemeMode(String v) { prefs.edit().putString(KEY_THEME, v).apply(); }

    public String getActiveCoinId() { return prefs.getString(KEY_ACTIVE_COIN, "bitcoin"); }
    public void setActiveCoinId(String v) { prefs.edit().putString(KEY_ACTIVE_COIN, v).apply(); }

    public void logout() { prefs.edit().clear().apply(); }
}
