package com.example.onyx.onyx.ui.adapters;

import android.support.v7.widget.RecyclerView;


public interface IFavRouteAdapter {


    void onItemMove(int fromPosition, int toPosition);


    void onItemDismiss(int position);
}