package com.example.onyx.onyx;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.example.onyx.onyx.models.FBFav;
import com.example.onyx.onyx.models.User;
import com.example.onyx.onyx.ui.adapters.FavouriteItemRecyclerView;
import com.example.onyx.onyx.models.FavItemModel;
import com.example.onyx.onyx.utils.ItemClickSupport;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.QuerySnapshot;


public class FavouriteItemList extends Fragment implements ItemClickSupport.OnItemClickListener, SwipeRefreshLayout.OnRefreshListener{


    private FavouriteItemRecyclerView mFavItemAdapter;


    private View view;

    private LinearLayout linearLayout;

    private ArrayList<FavItemModel> favItemModels;

    private RecyclerView recyclerView;
    private FavouriteItemRecyclerView mAdapter;

    //date to inflate the fav fragment
    private Integer image[] = {R.drawable.square_img, R.drawable.square_img,R.drawable.square_img,R.drawable.square_img,R.drawable.square_img,R.drawable.square_img,R.drawable.square_img};
    private String number[] = {"1","2","3","4","5","6","7"};
    private String title[] = {"Melbourne Central","Collin Street Boutique","Gym","Club","Park","Apple Store","Fight Club"};
    private String distance[] = {"0.3KM","1.2KM","12.5KM","45.4KM","5.1KM","0.1KM","12.7KM"};
    private String frequency[] = {"Visited 8 time(s)","Visited 5 time(s)","Visited 4 time(s)","Visited 3 time(s)","Visited 11 time(s)","Visited 3 time(s)","Visited 8 time(s)"};
    private LatLng latlngs[] = {new LatLng(-37.8136,144.9631),
                                new LatLng(-37.8132,144.9631),
                                new LatLng(-37.8133,144.9631),
                                new LatLng(-37.8134,144.9631),
                                new LatLng(-37.8135,144.9631),
                                new LatLng(-37.8167,144.9631),
                                new LatLng(-37.8138,144.9631)};

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        //init();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_fav_item, container, false);

        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);




        recyclerView = (RecyclerView) view.findViewById(R.id.recyclerview);
        favItemModels = new ArrayList<>();



        for (int i = 0; i < image.length; i++) {
            FavItemModel fiModel = new FavItemModel(image[i],number[i],title[i], distance[i], frequency[i],latlngs[i],null);

            favItemModels.add(fiModel);
        }

        //db = FirebaseFirestore.getInstance();
        //get fav places for current user
        FirebaseFirestore.getInstance().collection("users").document(FirebaseAuth.getInstance().getCurrentUser().getUid()).collection("fav")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            List<DocumentSnapshot> myListOfDocuments = task.getResult().getDocuments();
                            //final List<User> users = new ArrayList<>();
                            //final List<String> uids = new ArrayList<>();

                            ArrayList<String> titles = new ArrayList<>();
                            ArrayList<Integer> numbers = new ArrayList<>();
                            ArrayList<Integer> freqs = new ArrayList<>();
                            ArrayList<LatLng> latslngs = new ArrayList<>();
                            ArrayList<String> addresses = new ArrayList<>();

                            //fav item number index
                            int i = 0;
                            for (DocumentSnapshot dss : myListOfDocuments) {

                                FBFav fav = dss.toObject(FBFav.class);
                                titles.add(fav.title);
                                freqs.add(fav.freq);
                                addresses.add(fav.address);
                                LatLng favLatLng = new LatLng(fav.latlng.getLatitude(),fav.latlng.getLongitude());
                                latslngs.add(favLatLng);
                                numbers.add(i);



                                numbers.add(i);
                                i+=1;

                            }


                        }
                    }
                });


        mAdapter = new FavouriteItemRecyclerView(getActivity(), favItemModels);

        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(mAdapter);


        ItemClickSupport.addTo(recyclerView)
                .setOnItemClickListener(this);
        return view;

    }

    @Override
    public void onRefresh() {

    }

    @Override
    public void onItemClicked(RecyclerView recyclerView, int position, View v) {
        String distance = mAdapter.getFavItem(position).getDistance();
        String title = mAdapter.getFavItem(position).getTitle();
        String num = mAdapter.getFavItem(position).getNumber();
        Log.d("favItemList","clicked "+num+"  title is: "+title +"   distance is: "+distance);


        LatLng latlng = mAdapter.getFavItem(position).getLatlng();
        ((MainActivity)getActivity()).FavStartMap(latlng.latitude+"", latlng.longitude+"", title);
    }
}


