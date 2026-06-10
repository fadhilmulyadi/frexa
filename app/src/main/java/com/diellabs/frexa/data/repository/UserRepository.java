package com.diellabs.frexa.data.repository;

import android.content.Context;
import com.diellabs.frexa.util.UserPrefs;

public class UserRepository {
    private final UserPrefs prefs;

    public UserRepository(Context ctx) { prefs = new UserPrefs(ctx); }

    public boolean isLoggedIn() { return prefs.isLoggedIn(); }

    public void login(String name, String cred, boolean isEmail) {
        prefs.setLoggedIn(true); prefs.setUserName(name);
        if (isEmail) prefs.setUserEmail(cred); else prefs.setUserPhone(cred);
    }

    public String getUserName() { return prefs.getUserName(); }
    public String getUserEmail() { return prefs.getUserEmail(); }
    public String getUserPhone() { return prefs.getUserPhone(); }
    public float getBalance() { return prefs.getBalance(); }
    public String getTheme() { return prefs.getThemeMode(); }
    public void setTheme(String m) { prefs.setThemeMode(m); }
    public void logout() { prefs.logout(); }
}
