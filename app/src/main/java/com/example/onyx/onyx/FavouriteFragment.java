package com.example.onyx.onyx;

import android.graphics.Typeface;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.TextView;

import com.example.onyx.onyx.ui.adapters.FavouriteAdapter;


public class FavouriteFragment extends Fragment {



    private View view;

    private Toolbar toolbar;
    private ViewPager viewPager;
    Typeface mTypeface;



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_fav, container, false);

        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);


        TabLayout tabLayout = (TabLayout) view.findViewById(R.id.tab_layout);



        //2 tabs

        tabLayout.setTabMode(TabLayout.MODE_FIXED);

        tabLayout.addTab(tabLayout.newTab().setText("Favourite Places"));
        tabLayout.addTab(tabLayout.newTab().setText("Favourite Carers"));





        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);

        viewPager = (ViewPager) view.findViewById(R.id.pager);
        FavouriteAdapter adapter = new FavouriteAdapter
                (getChildFragmentManager(), tabLayout.getTabCount());
        viewPager.setAdapter(adapter);


        //viewPager.setOffscreenPageLimit(2);



        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
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


//    private void setToolbar() {
//
//        toolbar = (Toolbar) view.findViewById(R.id.toolbar);
//
//        setSupportActionBar(toolbar);
//        ActionBar actionBar = getSupportActionBar();
//        if (actionBar != null)
//            actionBar.setDisplayHomeAsUpEnabled(false);
//
//        actionBar.setTitle("");


    }





