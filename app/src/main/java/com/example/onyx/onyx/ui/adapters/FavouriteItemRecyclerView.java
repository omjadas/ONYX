package com.example.onyx.onyx.ui.adapters;

import android.content.Context;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


import java.util.List;

import com.example.onyx.onyx.R;
import com.example.onyx.onyx.models.FavItemModel;
import com.github.siyamed.shapeimageview.mask.PorterShapeImageView;

public class FavouriteItemRecyclerView extends RecyclerView.Adapter<FavouriteItemRecyclerView.MyViewHolder> {
    Context context;


    private List<FavItemModel> favItem;


    public class MyViewHolder extends RecyclerView.ViewHolder {


        PorterShapeImageView image;
        TextView number, title, distance, visitedNumber;


        public MyViewHolder(View view) {
            super(view);

            image = (PorterShapeImageView) view.findViewById(R.id.image);
            title = (TextView) view.findViewById(R.id.title);
            distance = (TextView) view.findViewById(R.id.fav_item_distance);
            visitedNumber = (TextView) view.findViewById(R.id.visited_number);
            number = (TextView) view.findViewById(R.id.number);


        }

    }


    public FavouriteItemRecyclerView(Context mainActivityContacts, List<FavItemModel> favItem) {
        this.favItem = favItem;
        this.context = mainActivityContacts;

    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_favourite, parent, false);


        return new MyViewHolder(itemView);


    }


    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public void onBindViewHolder(final MyViewHolder holder, int position) {
        FavItemModel favItem = this.favItem.get(position);
        holder.title.setText(favItem.getTitle());
        holder.distance.setText(favItem.getFrequency());
        holder.visitedNumber.setText(favItem.getAddress());
        holder.number.setText(favItem.getNumber());
        holder.image.setImageResource(favItem.getImage());


    }

    @Override
    public int getItemCount() {
        return favItem.size();
    }

    public FavItemModel getFavItem(int position) {
        return favItem.get(position);
    }


}


