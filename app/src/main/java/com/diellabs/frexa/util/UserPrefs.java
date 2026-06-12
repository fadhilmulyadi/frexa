package com.diellabs.frexa.util;

import android.content.Context;
import android.content.SharedPreferences;
import java.text.SimpleDateFormat;
import java.util.*;
import org.json.JSONObject;

public class UserPrefs {
    private static final String PREFS_NAME = "frexa_prefs";
    private static final String KEY_LOGGED_IN = "is_logged_in";
    private static final String KEY_USER_NAME = "user_name";
    private static final String KEY_USER_EMAIL = "user_email";
    private static final String KEY_USER_PHONE = "user_phone";
    private static final String KEY_BALANCE = "virtual_balance";
    private static final String KEY_USD_TO_IDR = "usd_to_idr_rate";
    private static final String KEY_DAILY_SNAPSHOTS = "daily_snapshots";

    private static final float DEFAULT_BALANCE = 100_000_000f;
    private static final float DEFAULT_USD_TO_IDR = 16_200f;

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

    public float getBalance() { return prefs.getFloat(KEY_BALANCE, DEFAULT_BALANCE); }
    public void setBalance(float v) { prefs.edit().putFloat(KEY_BALANCE, v).apply(); }

    public float getUsdToIdr() { return prefs.getFloat(KEY_USD_TO_IDR, DEFAULT_USD_TO_IDR); }
    public void setUsdToIdr(float v) { prefs.edit().putFloat(KEY_USD_TO_IDR, v).apply(); }

    public void logout() { prefs.edit().clear().apply(); }

    /** Save today's portfolio total so we can build the 7-day strip. */
    public void saveTodaySnapshot(double portfolioTotal) {
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(new Date());
        try {
            JSONObject json = new JSONObject(prefs.getString(KEY_DAILY_SNAPSHOTS, "{}"));
            json.put(today, portfolioTotal);
            // Prune to last 8 days
            Iterator<String> keys = json.keys();
            List<String> allKeys = new ArrayList<>();
            while (keys.hasNext()) allKeys.add(keys.next());
            Collections.sort(allKeys);
            while (allKeys.size() > 8) {
                json.remove(allKeys.remove(0));
            }
            prefs.edit().putString(KEY_DAILY_SNAPSHOTS, json.toString()).apply();
        } catch (Exception ignored) {}
    }

    /** Returns date→value map for last 8 days, sorted ascending. */
    public Map<String, Double> getDailySnapshots() {
        Map<String, Double> result = new TreeMap<>();
        try {
            JSONObject json = new JSONObject(prefs.getString(KEY_DAILY_SNAPSHOTS, "{}"));
            Iterator<String> keys = json.keys();
            while (keys.hasNext()) {
                String k = keys.next();
                result.put(k, json.getDouble(k));
            }
        } catch (Exception ignored) {}
        return result;
    }
}
