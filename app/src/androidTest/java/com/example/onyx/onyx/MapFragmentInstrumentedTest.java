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

public class MapFragmentInstrumentedTest {
    MainActivity mTestActivity;

    @Rule
    public ActivityTestRule<MainActivity> mainActivityActivityTestRule = new ActivityTestRule<>(MainActivity.class);


    @Before
    public void init() {
        mTestActivity = mainActivityActivityTestRule.getActivity();
    }

    @Test
    public void mapFragTest(){
        mTestActivity.fragChange(R.id.toolmap);
        Espresso.onView(withId(R.id.map)).check(matches(isDisplayed()));
        Espresso.onView(withId(R.id.place_autocomplete_container)).check(matches(isDisplayed()));
        Espresso.onView(withId(R.id.openNearbyButton)).check(matches(isDisplayed()));

        Espresso.onView(withId(R.id.openNearbyButton)).perform(click());
        Espresso.onView(withId(R.id.Restauarant)).check(matches(isDisplayed()));
        Espresso.onView(withId(R.id.Cafe)).check(matches(isDisplayed()));
        Espresso.onView(withId(R.id.Taxi)).check(matches(isDisplayed()));
        Espresso.onView(withId(R.id.Station)).check(matches(isDisplayed()));
        Espresso.onView(withId(R.id.ATM)).check(matches(isDisplayed()));
        Espresso.onView(withId(R.id.Hospital)).check(matches(isDisplayed()));
        Espresso.onView(withId(R.id.closeNearbyButton)).check(matches(isDisplayed()));
        Espresso.onView(withId(R.id.closeNearbyButton)).perform(click());
    }
}
