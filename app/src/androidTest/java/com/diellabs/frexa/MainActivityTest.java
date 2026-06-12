package com.diellabs.frexa;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import com.diellabs.frexa.util.UserPrefs;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

@RunWith(AndroidJUnit4.class)
public class MainActivityTest {

    @Rule
    public ActivityScenarioRule<MainActivity> activityRule = 
        new ActivityScenarioRule<>(MainActivity.class);

    @Test
    public void testRedirectToLoginIfNotLoggedIn() {
        // Set user to logged out
        UserPrefs prefs = new UserPrefs(InstrumentationRegistry.getInstrumentation().getTargetContext());
        prefs.setLoggedIn(false);

        // Verify we are on login screen
        onView(withId(R.id.tv_logo)).check(matches(isDisplayed()));
    }
}
