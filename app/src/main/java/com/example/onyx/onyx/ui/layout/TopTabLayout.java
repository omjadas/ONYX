package com.example.onyx.onyx.ui.layout;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.view.PagerAdapter;
import android.util.AttributeSet;

public class TopTabLayout extends TabLayout {
    public TopTabLayout(Context context) {
        super(context);
    }

    public TopTabLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public TopTabLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void setTabsFromPagerAdapter(@NonNull PagerAdapter adapter) {

        this.removeAllTabs();


    }
}
