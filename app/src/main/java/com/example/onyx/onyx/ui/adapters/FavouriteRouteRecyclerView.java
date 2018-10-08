package com.example.onyx.onyx.ui.adapters;

import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v4.view.MotionEventCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.onyx.onyx.R;
import com.example.onyx.onyx.models.FavItemModel;
import com.github.siyamed.shapeimageview.mask.PorterShapeImageView;

import java.util.List;

public class FavouriteRouteRecyclerView extends RecyclerView.Adapter<FavouriteRouteRecyclerView.MyViewHolder> implements IFavRouteAdapter {
    public List<FavItemModel> favItem;
    Context context;
    private IDragListener mDragStartListener;

    public FavouriteRouteRecyclerView(Context mainActivityContacts, List<FavItemModel> favItem, IDragListener dragStartListener) {
        this.favItem = favItem;
        this.context = mainActivityContacts;
        this.mDragStartListener = dragStartListener;

    }

    @Override
    public void onItemMove(int fromPosition, int toPosition) {
        FavItemModel prev = favItem.remove(fromPosition);
        favItem.add(toPosition > fromPosition ? toPosition - 1 : toPosition, prev);
        notifyItemMoved(fromPosition, toPosition);
    }

    @Override
    public void onItemDismiss(int position) {
        if (favItem == null || favItem.size() == 0 || position >= favItem.size()) {
            //out of bounds
            return;

        }
        favItem.remove(position);
        notifyItemRemoved(position);
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_favourite_route, parent, false);


        return new MyViewHolder(itemView);


    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public void onBindViewHolder(final MyViewHolder holder, int position) {
        FavItemModel favItem = this.favItem.get(position);
        holder.title.setText(favItem.getTitle());
        holder.address.setText(favItem.getFrequency());
        holder.visitedNumber.setText(favItem.getAddress());
        holder.number.setText(favItem.getNumber());
        holder.image.setImageBitmap(favItem.getImage());


        holder.handleView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (MotionEventCompat.getActionMasked(event) ==
                        MotionEvent.ACTION_DOWN) {
                    mDragStartListener.onStartDrag(holder);
                }
                return false;
            }
        });

    }

    @Override
    public int getItemCount() {
        return favItem.size();
    }

    public FavItemModel getFavItem(int position) {
        return favItem.get(position);
    }

    public class MyViewHolder extends RecyclerView.ViewHolder implements
            IFavRouteViewHolder {


        public ImageView handleView;
        PorterShapeImageView image;
        TextView number, title, address, visitedNumber;


        public MyViewHolder(View view) {
            super(view);

            image = view.findViewById(R.id.image_route);
            title = view.findViewById(R.id.title_route);
            address = view.findViewById(R.id.fav_item_address_route);
            visitedNumber = view.findViewById(R.id.visited_number_route);
            number = view.findViewById(R.id.number_route);
            handleView = view.findViewById(R.id.handle);

        }

        @Override
        public void onItemSelected() {
            itemView.setBackgroundColor(Color.LTGRAY);
        }

        @Override
        public void onItemClear() {
            itemView.setBackgroundColor(0);
        }
    }


}


