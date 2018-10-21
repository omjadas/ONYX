package com.example.onyx.onyx;

import android.support.test.espresso.Espresso;
import android.support.test.rule.ActivityTestRule;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;

public class UserFragmentInstrumentedTest {
    MainActivity mTestActivity;

    @Rule
    public ActivityTestRule<MainActivity> mainActivityActivityTestRule = new ActivityTestRule<>(MainActivity.class);


    @Before
    public void init() {
        mTestActivity = mainActivityActivityTestRule.getActivity();
    }

    @Test
    public void contactFragTest(){
        mTestActivity.fragChange(R.id.toolcontact);
        Espresso.onView(withId(R.id.swipe_refresh_layout)).check(matches(isDisplayed()));
        Espresso.onView(withId(R.id.addContactsButton)).check(matches(isDisplayed()));

        Espresso.onView(withId(R.id.addContactsButton)).perform(click());
        Espresso.onView(withId(R.id.scanButton)).check(matches(isDisplayed()));
        Espresso.onView(withId(R.id.addByEmailButton)).check(matches(isDisplayed()));
        Espresso.onView(withId(R.id.showQRButton)).check(matches(isDisplayed()));
        Espresso.onView(withId(R.id.showQRButton)).perform(click());
        //Espresso.onView(withId(R.id.containerQR)).check(matches(isDisplayed()));
        Espresso.onView(withId(R.id.closeAddContactButton)).check(matches(isDisplayed()));
        Espresso.onView(withId(R.id.closeAddContactButton)).perform(click());
    }
}
