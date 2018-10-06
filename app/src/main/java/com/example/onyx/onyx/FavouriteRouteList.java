package com.example.onyx.onyx;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.LinearLayout;

import com.example.onyx.onyx.models.FBFav;
import com.example.onyx.onyx.models.FavItemModel;
import com.example.onyx.onyx.ui.adapters.FavouriteItemRecyclerView;
import com.example.onyx.onyx.ui.adapters.FavouriteRouteRecyclerView;
import com.example.onyx.onyx.ui.adapters.IDragListener;
import com.example.onyx.onyx.ui.adapters.FavRouteDragCallback;
import com.example.onyx.onyx.utils.ItemClickSupport;
import com.google.android.gms.location.places.GeoDataClient;
import com.google.android.gms.location.places.PlacePhotoMetadata;
import com.google.android.gms.location.places.PlacePhotoMetadataBuffer;
import com.google.android.gms.location.places.PlacePhotoMetadataResponse;
import com.google.android.gms.location.places.PlacePhotoResponse;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

/*
* lists of routes saved by user
* a route is a list of waypoints to construct a safe walk route for users
* */
public class FavouriteRouteList extends Fragment implements ItemClickSupport.OnItemClickListener, SwipeRefreshLayout.OnRefreshListener, IDragListener {



    private FavouriteItemRecyclerView mFavItemAdapter;


    private View view;

    private LinearLayout linearLayout;

    private ArrayList<FavItemModel> favItemModels;

    private RecyclerView recyclerView;
    private FavouriteRouteRecyclerView mAdapter;

    private GeoDataClient mGeoDataClient;

    private int numOfFav = 999999;

    //date to inflate the fav fragment
    private Integer image[] = {R.drawable.square_img, R.drawable.square_img,R.drawable.square_img,R.drawable.square_img};
    private String number[] = {"1","2","3","4"};
    private String title[] = {"Neighbourhood","Around The Lake","Shopping Walk","Morning Run"};
    private String distance[] = {"11.3KM","11.2KM","12.5KM","45.4KM","5.1KM","10.1KM","12.7KM"};
    private String frequency[] = {"Walked 8 time(s)","Walked 5 time(s)","Walked 4 time(s)","Walked 3 time(s)"};


    private SwipeRefreshLayout mSwipeRefreshLayout;

    private ItemTouchHelper mItemTouchHelper;
    private IDragListener dragListener;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mGeoDataClient = Places.getGeoDataClient(getActivity(), null);

        view = inflater.inflate(R.layout.fragment_fav_item, container, false);

        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);


        recyclerView = (RecyclerView) view.findViewById(R.id.recyclerview);

        //attach listener to refreshlayout
        mSwipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.fav_swipe2);
        mSwipeRefreshLayout.setOnRefreshListener(this);
        GetFavs();

        dragListener = this;
        mAdapter = new FavouriteRouteRecyclerView(getActivity(), favItemModels,this);

        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(mAdapter);


        ItemClickSupport.addTo(recyclerView)
                .setOnItemClickListener(this);

        ItemTouchHelper.Callback callback = new FavRouteDragCallback(mAdapter);
        mItemTouchHelper = new ItemTouchHelper(callback);
        mItemTouchHelper.attachToRecyclerView(recyclerView);

        return view;

    }

    public void GetFavs(){
        favItemModels = new ArrayList<>();
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
                            numOfFav = myListOfDocuments.size();
                            for (DocumentSnapshot dss : myListOfDocuments) {

                                if (dss.exists()){

                                    //firebase doc to fbfav class
                                    FBFav fav = dss.toObject(FBFav.class);

                                    //converting fbfav object into fav item object
                                    //geopoint to latlng
                                    LatLng favLatLng = new LatLng(fav.latlng.getLatitude(),fav.latlng.getLongitude());

                                    titles.add(fav.title);
                                    freqs.add(fav.freq);
                                    addresses.add(fav.address);
                                    latslngs.add(favLatLng);
                                    numbers.add(i);

                                    FavItemModel fiModel = new FavItemModel(
                                            null,
                                            i+"",
                                            fav.title,
                                            fav.address ,
                                            "Visited "+fav.freq + " time(s)",
                                            favLatLng,
                                            fav.placeID);

                                    Log.d("favf",fav.placeID);
                                    FillInFavItemObjectImage(fav.placeID,fiModel);



                                    numbers.add(i);
                                    i+=1;
                                }


                            }


                        }
                    }
                });

    }

    /*
    gets the image for this place
     */
    private void FillInFavItemObjectImage(String place_id, FavItemModel fav) {
        Log.d("favf",fav.toString());
        final String placeId = place_id;
        final Task<PlacePhotoMetadataResponse> photoMetadataResponse = mGeoDataClient.getPlacePhotos(placeId);
        photoMetadataResponse.addOnCompleteListener(new OnCompleteListener<PlacePhotoMetadataResponse>() {
            @Override
            public void onComplete(@NonNull Task<PlacePhotoMetadataResponse> task) {
                // Get the list of photos.
                PlacePhotoMetadataResponse photos = task.getResult();
                // Get the PlacePhotoMetadataBuffer (metadata for all of the photos).
                PlacePhotoMetadataBuffer photoMetadataBuffer = photos.getPhotoMetadata();
                // Get the first photo in the list.
                PlacePhotoMetadata photoMetadata = photoMetadataBuffer.get(0);
                // Get the attribution text.
                CharSequence attribution = photoMetadata.getAttributions();
                // Get a full-size bitmap for the photo.
                Task<PlacePhotoResponse> photoResponse = mGeoDataClient.getPhoto(photoMetadata);
                photoResponse.addOnCompleteListener(new OnCompleteListener<PlacePhotoResponse>() {
                    @Override
                    public void onComplete(@NonNull Task<PlacePhotoResponse> task) {
                        PlacePhotoResponse photo = task.getResult();
                        Bitmap bitmap = photo.getBitmap();
                        //set the bitmap
                        fav.setImage(bitmap);

                        //add it to fav item list
                        favItemModels.add(fav);

                        if (numOfFav == favItemModels.size()){
                            //all done
                            //mAdapter = new FavouriteRouteRecyclerView(getActivity(), favItemModels,dragListener);
                            mAdapter.favItem = favItemModels;
                            mAdapter.notifyDataSetChanged();
                            recyclerView.setAdapter(mAdapter);
                        }


                    }
                });
            }
        });
    }
    @Override
    public void onRefresh() {
        GetFavs();
        Log.d("refreshtab","rrrrrrrrrrrr");

        //remove spining icon after 1 second
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                mSwipeRefreshLayout.setRefreshing(false);
            }
        }, 1000);
    }

    @Override
    public void onItemClicked(RecyclerView recyclerView, int position, View v) {

        LatLng dest1 = new LatLng(-37.7964,144.9612);
        LatLng dest2 = new LatLng(-37.8098,144.9652);

        ArrayList<LatLng> waypoints = new ArrayList<>();
        waypoints.add(dest1);
        waypoints.add(dest2);
        ((MainActivity)getActivity()).FavStartMapRoute(waypoints);

    }

    @Override
    public void onStartDrag(RecyclerView.ViewHolder viewHolder) {
        mItemTouchHelper.startDrag(viewHolder);
    }
}


