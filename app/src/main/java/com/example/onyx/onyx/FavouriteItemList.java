package com.example.onyx.onyx;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.LinearLayout;

import java.util.ArrayList;

import com.example.onyx.onyx.ui.adapters.FavouriteItemRecyclerView;
import com.example.onyx.onyx.models.FavItemModel;


public class FavouriteItemList extends Fragment {



    private View view;

    private LinearLayout linearLayout;

    private ArrayList<FavItemModel> favItemModels;

    private RecyclerView recyclerView;
    private FavouriteItemRecyclerView mAdapter;


    private Integer image[] = {R.drawable.square_img, R.drawable.square_img,R.drawable.square_img,R.drawable.square_img,R.drawable.square_img,R.drawable.square_img,R.drawable.square_img};
    private String number[] = {"1","2","3","4","5","6","7"};
    private String title[] = {"Melbourne Central","Collin Street Boutique","Gym","Club","Park","Apple Store","Fight Club"};
    private String view1[] = {"0.3KM","1.2KM","12.5KM","45.4KM","5.1KM","0.1KM","12.7KM"};
    private String install[] = {"Visited 8 time(s)","Visited 5 time(s)","Visited 4 time(s)","Visited 3 time(s)","Visited 11 time(s)","Visited 3 time(s)","Visited 8 time(s)"};





    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_fav_item, container, false);

        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);



        /*Recyclerview  code is here*/


        recyclerView = (RecyclerView) view.findViewById(R.id.recyclerview);
        favItemModels = new ArrayList<>();



        for (int i = 0; i < image.length; i++) {
            FavItemModel beanClassForRecyclerView_contacts = new FavItemModel(image[i],number[i],title[i],view1[i],install[i]);

            favItemModels.add(beanClassForRecyclerView_contacts);
        }


        mAdapter = new FavouriteItemRecyclerView(getActivity(), favItemModels);

        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(mAdapter);




        return view;



    }

}


