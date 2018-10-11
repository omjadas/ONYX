package com.example.onyx.onyx.ui.adapters;

import android.support.v7.widget.RecyclerView;

public interface IDragListener {

    /**
     * Called view is dragging.
     */
    void onStartDrag(RecyclerView.ViewHolder viewHolder);
}