package com.example.onyx.onyx.ui.adapters;

import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.onyx.onyx.R;
import com.example.onyx.onyx.models.FavItemModel;
import com.github.siyamed.shapeimageview.mask.PorterShapeImageView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class FavouriteItemRecyclerView extends RecyclerView.Adapter<FavouriteItemRecyclerView.MyViewHolder> implements IFavRouteAdapter {
    public List<FavItemModel> favItem;
    Context context;


    public FavouriteItemRecyclerView(Context mainActivityContacts, List<FavItemModel> favItem, IDragListener dragStartListener) {
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
        holder.address.setText(favItem.getFrequency());
        holder.visitedNumber.setText(favItem.getAddress());
        holder.number.setText(favItem.getNumber());
        holder.image.setImageBitmap(favItem.getImage());


    }

    @Override
    public int getItemCount() {
        return favItem.size();
    }

    public FavItemModel getFavItem(int position) {
        return favItem.get(position);
    }

    @Override
    public void onItemMove(int fromPosition, int toPosition) {
        FavItemModel prev = favItem.remove(fromPosition);
        favItem.add(toPosition > fromPosition ? toPosition - 1 : toPosition, prev);
        notifyItemMoved(fromPosition, toPosition);
    }

    @Override
    public void onItemDismiss(int position) {

        //Log.d("fav swipe del", "pos " + position);
        //Log.d("fav swipe del", "pos " + favItem.size());

        //need to check if places list got updated
        if (favItem == null || favItem.size() == 0 || position >= favItem.size()) {
            //out of bounds
            return;

        } else {
            notifyItemRemoved(position);
            Log.d("fav swipe", "sssssssssss" + favItem.get(position));

            deleteFav(favItem.get(position).getPlaceID());

            favItem.remove(position);
        }
    }

    private void deleteFav(String docID) {
        FirebaseFirestore.getInstance().collection("users").document(FirebaseAuth.getInstance().getCurrentUser().getUid()).collection("fav")
                .document(docID).delete();

    }

    public class MyViewHolder extends RecyclerView.ViewHolder implements
            IFavRouteViewHolder {


        public LinearLayout handleView;
        PorterShapeImageView image;
        TextView number, title, address, visitedNumber;

        public MyViewHolder(View view) {
            super(view);

            image = (PorterShapeImageView) view.findViewById(R.id.image);
            title = (TextView) view.findViewById(R.id.title);
            address = (TextView) view.findViewById(R.id.fav_item_address);
            visitedNumber = (TextView) view.findViewById(R.id.visited_number);
            number = (TextView) view.findViewById(R.id.number);
            handleView = view.findViewById(R.id.fav_item_linear);

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


