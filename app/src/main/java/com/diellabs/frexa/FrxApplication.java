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
