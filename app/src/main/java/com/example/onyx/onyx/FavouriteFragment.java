package com.example.onyx.onyx;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import com.example.onyx.onyx.ui.adapters.FavouriteAdapter;

import java.util.Objects;


public class FavouriteFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {


    private View view;

    private Toolbar toolbar;
    private ViewPager viewPager;

    private SwipeRefreshLayout mSwipeRefreshLayout;


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_fav, container, false);

        Objects.requireNonNull(getActivity()).getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        TabLayout tabLayout = view.findViewById(R.id.tab_layout);

        //2 tabs

        tabLayout.setTabMode(TabLayout.MODE_FIXED);

        tabLayout.addTab(tabLayout.newTab().setText("Favourite Places"));
        tabLayout.addTab(tabLayout.newTab().setText("Custom Route"));

        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);

        viewPager = view.findViewById(R.id.pager);
        FavouriteAdapter adapter = new FavouriteAdapter
                (getChildFragmentManager(), tabLayout.getTabCount());
        viewPager.setAdapter(adapter);
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout) {
            @Override
            public void onPageScrollStateChanged(int state) {
                Log.d("refreshtab3333333", "rrrrrrrrrrrr");
                if (mSwipeRefreshLayout != null)
                    new Handler().postDelayed(() -> mSwipeRefreshLayout.setRefreshing(false), 1000);
            }
        });

        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
                Log.d("Fav", "tab selected" + tab);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
        return view;
    }

    public void setCurrentTab(int i) {
        viewPager.setCurrentItem(i);
    }


    @Override
    public void onRefresh() {
        Log.d("refreshtab22222222222", "rrrrrrrrrrrr");
        new Handler().postDelayed(() -> mSwipeRefreshLayout.setRefreshing(false), 1000);
    }
}





