package com.example.onyx.onyx;

import android.support.test.espresso.Espresso;
import android.support.test.rule.ActivityTestRule;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;

public class FavouriteFragmentTest {
    MainActivity mTestActivity;

    @Rule
    public ActivityTestRule<MainActivity> mainActivityActivityTestRule = new ActivityTestRule<>(MainActivity.class);


    @Before
    public void init() {
        mTestActivity = mainActivityActivityTestRule.getActivity();
    }

    @Test
    public void FavouriteFragmentTest(){
        mTestActivity.fragChange(R.id.toolfavs);
        Espresso.onView(withId(R.id.tab_layout)).check(matches(isDisplayed()));
        Espresso.onView(withId(R.id.pager)).check((matches(isDisplayed())));
    }
}
