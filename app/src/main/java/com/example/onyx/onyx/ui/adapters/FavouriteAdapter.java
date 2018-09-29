package com.example.onyx.onyx.ui.adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.example.onyx.onyx.FavouriteItemList;
import com.example.onyx.onyx.FavouriteRouteList;

public class
FavouriteAdapter extends FragmentStatePagerAdapter {

    int mNumOfTabs;

    public FavouriteAdapter(FragmentManager fm, int NumOfTabs) {
        super(fm);
        this.mNumOfTabs = NumOfTabs;
    }


    @Override
    public Fragment getItem(int position) {

        switch (position) {
            case 0:
                FavouriteItemList tab1 = new FavouriteItemList();
                return tab1;

            case 1:
                FavouriteRouteList tab2 = new FavouriteRouteList();
                return tab2;

                default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return mNumOfTabs;
    }
}