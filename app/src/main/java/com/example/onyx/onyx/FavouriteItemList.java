package com.example.onyx.onyx;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
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
import java.util.List;

import com.example.onyx.onyx.models.FBFav;
import com.example.onyx.onyx.ui.adapters.FavouriteItemRecyclerView;
import com.example.onyx.onyx.models.FavItemModel;
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
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;


public class FavouriteItemList extends Fragment implements ItemClickSupport.OnItemClickListener, SwipeRefreshLayout.OnRefreshListener{


    private FavouriteItemRecyclerView mFavItemAdapter;


    private View view;

    private LinearLayout linearLayout;

    private ArrayList<FavItemModel> favItemModels;

    private RecyclerView recyclerView;
    private FavouriteItemRecyclerView mAdapter;

    private GeoDataClient mGeoDataClient;

    private int numOfFav = 999999;

    //date to inflate the fav fragment
    private Integer image[] = {R.drawable.square_img, R.drawable.square_img,R.drawable.square_img,
            R.drawable.square_img,R.drawable.square_img,R.drawable.square_img,R.drawable.square_img};
    private String number[] = {"1","2","3","4","5","6","7"};
    private String title[] = {"Melbourne Central","Collin Street Boutique","Gym","Club","Park",
            "Apple Store","Fight Club"};
    private String distance[] = {"0.3KM","1.2KM","12.5KM","45.4KM","5.1KM","0.1KM","12.7KM"};
    private String frequency[] = {"Visited 8 time(s)","Visited 5 time(s)","Visited 4 time(s)",
            "Visited 3 time(s)","Visited 11 time(s)","Visited 3 time(s)","Visited 8 time(s)"};
    private LatLng latlngs[] = {new LatLng(-37.8136,144.9631),
                                new LatLng(-37.8132,144.9631),
                                new LatLng(-37.8133,144.9631),
                                new LatLng(-37.8134,144.9631),
                                new LatLng(-37.8135,144.9631),
                                new LatLng(-37.8167,144.9631),
                                new LatLng(-37.8138,144.9631)};

    private SwipeRefreshLayout mSwipeRefreshLayout;

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        //init();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mGeoDataClient = Places.getGeoDataClient(getActivity(), null);

        view = inflater.inflate(R.layout.fragment_fav_item, container, false);


        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);


        recyclerView = (RecyclerView) view.findViewById(R.id.recyclerview);

        //attach listener to refreshlayout
        mSwipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.fav_swipe2);
        mSwipeRefreshLayout.setOnRefreshListener(this);






        for (int i = 0; i < image.length; i++) {
            //FavItemModel fiModel = new FavItemModel(image[i],number[i],title[i], distance[i], frequency[i],latlngs[i]);

            //favItemModels.add(fiModel);
        }

        //db = FirebaseFirestore.getInstance();
        //get fav places for current user
        GetFavs();

        mAdapter = new FavouriteItemRecyclerView(getActivity(), favItemModels);

        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(mAdapter);


        ItemClickSupport.addTo(recyclerView)
                .setOnItemClickListener(this);
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
                                            number[i],
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
                            mAdapter = new FavouriteItemRecyclerView(getActivity(), favItemModels);
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
        String distance = mAdapter.getFavItem(position).getAddress();
        String title = mAdapter.getFavItem(position).getTitle();
        String num = mAdapter.getFavItem(position).getNumber();
        String placeID = mAdapter.getFavItem(position).getPlaceID();

        Log.d("favItemList","clicked "+num+"  title is: "+title +"   distance is: "+distance);

        //update visit freq in firebase
        final DocumentReference reference = FirebaseFirestore.getInstance().collection("users").document(FirebaseAuth.getInstance().getCurrentUser().getUid());
        reference.collection("fav").document(placeID).get().
                addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task0) {

                        if(task0.isSuccessful()){
                            DocumentSnapshot document = task0.getResult();
                            Integer freq = Integer.parseInt(document.get("freq").toString());
                            if(task0.getResult().exists()){
                                Log.d("saveFreq","is there");
                                //only add to firebase if not exist
                                reference.get().
                                        addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                if(task.isSuccessful()){
                                                    DocumentSnapshot document = task.getResult();
                                                    reference.collection("fav").document(placeID).update("freq",freq);
                                                    Log.d("saveFav","now added");
                                                }
                                            }
                                        });
                            }
                            else{
                                //not there , can't do much here
                                Log.d("addFreq","not there");
                            }

                        }

                    }
                });//update visit freq in firebase done

        LatLng latlng = mAdapter.getFavItem(position).getLatlng();
        ((MainActivity)getActivity()).FavStartMap(latlng.latitude+"", latlng.longitude+"", title);
    }
}


