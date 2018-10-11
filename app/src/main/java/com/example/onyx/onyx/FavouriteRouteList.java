package com.example.onyx.onyx;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
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
import com.example.onyx.onyx.ui.adapters.FavRouteDragCallback;
import com.example.onyx.onyx.ui.adapters.FavouriteItemRecyclerView;
import com.example.onyx.onyx.ui.adapters.FavouriteRouteRecyclerView;
import com.example.onyx.onyx.ui.adapters.IDragListener;
import com.example.onyx.onyx.utils.ItemClickSupport;
import com.google.android.gms.location.places.GeoDataClient;
import com.google.android.gms.location.places.PlacePhotoMetadata;
import com.google.android.gms.location.places.PlacePhotoMetadataBuffer;
import com.google.android.gms.location.places.PlacePhotoMetadataResponse;
import com.google.android.gms.location.places.PlacePhotoResponse;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;


/**
 * lists of routes saved by user
 * a route is a list of waypoints to construct a safe walk route for users
 */
public class FavouriteRouteList extends Fragment implements ItemClickSupport.OnItemClickListener, SwipeRefreshLayout.OnRefreshListener, IDragListener {


    private FavouriteItemRecyclerView mFavItemAdapter;


    private View view;

    private LinearLayout linearLayout;

    private ArrayList<FavItemModel> favItemModels;

    private RecyclerView recyclerView;
    private FavouriteRouteRecyclerView mAdapter;

    private GeoDataClient mGeoDataClient;

    private int numOfFav = 999999;

    private SwipeRefreshLayout mSwipeRefreshLayout;

    private ItemTouchHelper mItemTouchHelper;
    private IDragListener dragListener;


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mGeoDataClient = Places.getGeoDataClient(Objects.requireNonNull(getActivity()), null);

        view = inflater.inflate(R.layout.fragment_fav_item_route, container, false);

        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);


        recyclerView = view.findViewById(R.id.recyclerview);

        //attach listener to refreshlayout
        mSwipeRefreshLayout = view.findViewById(R.id.fav_swipe2);
        mSwipeRefreshLayout.setOnRefreshListener(this);


        dragListener = this;
        mAdapter = new FavouriteRouteRecyclerView(getActivity(), favItemModels, this);

        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(mAdapter);


        //listener for click events
        ItemClickSupport.addTo(recyclerView)
                .setOnItemClickListener(this);

        ItemTouchHelper.Callback callback = new FavRouteDragCallback(mAdapter);
        mItemTouchHelper = new ItemTouchHelper(callback);
        mItemTouchHelper.attachToRecyclerView(recyclerView);


        //linstener for fab
        FloatingActionButton fab = view.findViewById(R.id.fav_route_go);
        fab.setOnClickListener(view -> {
            //Log.d("fab",mAdapter.favItem.toString());

            if (mAdapter == null || mAdapter.favItem == null)
                return;

            //converting an arraylist of favItemMOdel to lanlngs
            ArrayList<LatLng> routeWayPoint = new ArrayList<>();

            for (FavItemModel fav : mAdapter.favItem) {
                routeWayPoint.add(fav.getLatlng());
            }

            //let main tell map to compute route
            ((MainActivity) getActivity()).FavStartMapRoute(routeWayPoint);
        });

        checkFavUpdate();

        return view;
    }


    private void checkFavUpdate() {

        CollectionReference docRef = FirebaseFirestore.getInstance().collection("users").document(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid()).collection("fav");

        docRef
                .addSnapshotListener((queryDocumentSnapshots, e) -> {
                    if (e != null) {
                        System.err.println("Msg Listen failed:" + e);
                        return;
                    }

                    Objects.requireNonNull(queryDocumentSnapshots).getDocumentChanges();
                    if (queryDocumentSnapshots.getDocumentChanges().size() == 0) {
                        return;
                    }

                    DocumentChange dc = queryDocumentSnapshots.getDocumentChanges().get(0);

                    if (dc != null) {
                        switch (dc.getType()) {
                            case ADDED:
                                GetFavs();
                                break;
                            case MODIFIED:

                                break;
                            case REMOVED:
                                GetFavs();

                                break;
                            default:
                                break;
                        }
                    }
                });
    }

    public void GetFavs() {
        favItemModels = new ArrayList<>();
        FirebaseFirestore.getInstance().collection("users").document(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid()).collection("fav")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<DocumentSnapshot> myListOfDocuments = task.getResult().getDocuments();
                        ArrayList<String> titles = new ArrayList<>();
                        ArrayList<Integer> numbers = new ArrayList<>();
                        ArrayList<Integer> freqs = new ArrayList<>();
                        ArrayList<LatLng> latslngs = new ArrayList<>();
                        ArrayList<String> addresses = new ArrayList<>();

                        //fav item number index
                        int i = 1;
                        numOfFav = myListOfDocuments.size();
                        for (DocumentSnapshot dss : myListOfDocuments) {

                            if (dss.exists()) {

                                //firebase doc to fbfav class
                                FBFav fav = dss.toObject(FBFav.class);

                                //converting fbfav object into fav item object
                                //geopoint to latlng
                                LatLng favLatLng = new LatLng(Objects.requireNonNull(fav).latlng.getLatitude(), fav.latlng.getLongitude());

                                titles.add(fav.title);
                                freqs.add(fav.freq);
                                addresses.add(fav.address);
                                latslngs.add(favLatLng);
                                numbers.add(i);

                                FavItemModel fiModel = new FavItemModel(
                                        null,
                                        i + "",
                                        fav.title,
                                        fav.address,
                                        "Visited " + fav.freq + " time(s)",
                                        favLatLng,
                                        fav.placeID);

                                Log.d("favf", fav.placeID);
                                FillInFavItemObjectImage(fav.placeID, fiModel);

                                numbers.add(i);
                                i += 1;
                            }
                        }
                    }
                });
    }


    /**
     * fill in default image
     *
     * @param place_id
     * @param fav
     */
    private void FillInDefaultFavItemObjectImage(String place_id, FavItemModel fav) {

        Bitmap bitmap = BitmapFactory.decodeResource(Objects.requireNonNull(getContext()).getResources(),
                R.drawable.ic_img);
        fav.setImage(bitmap);

        //add it to fav item list
        favItemModels.add(fav);

        if (numOfFav == favItemModels.size()) {
            //all done
            //mAdapter = new FavouriteItemRecyclerView(getActivity(), favItemModels);
            //sort it
            Collections.sort(favItemModels);

            mAdapter.favItem = favItemModels;
            mAdapter.notifyDataSetChanged();

        }
    }


    /**
     * gets the image for this place
     *
     * @param place_id
     * @param fav
     */
    private void FillInFavItemObjectImage(String place_id, FavItemModel fav) {
        Log.d("favf", fav.toString());
        final Task<PlacePhotoMetadataResponse> photoMetadataResponse = mGeoDataClient.getPlacePhotos(place_id);
        photoMetadataResponse.addOnCompleteListener(task -> {
            // Get the list of photos.
            PlacePhotoMetadataResponse photos = task.getResult();
            if (photos == null) {   //checks if place has photo;
                FillInDefaultFavItemObjectImage(place_id, fav);
                return;
            }

            if (photos.getPhotoMetadata() == null) {   //checks if place has photo meta data;
                FillInDefaultFavItemObjectImage(place_id, fav);
                return;
            }

            // Get the PlacePhotoMetadataBuffer (metadata for all of the photos).
            PlacePhotoMetadataBuffer photoMetadataBuffer = photos.getPhotoMetadata();

            if (photoMetadataBuffer == null || photoMetadataBuffer.getCount() < 1) {   //checks if photoMetadataBuffer  is null or get 0 will be null;
                FillInDefaultFavItemObjectImage(place_id, fav);
                return;
            }

            // Get the first photo in the list.
            PlacePhotoMetadata photoMetadata = photoMetadataBuffer.get(0);

            if (photoMetadata == null || photoMetadata.getAttributions() == null) {   //checks if photoMetadataBuffer  is null or get 0 will be null;
                FillInDefaultFavItemObjectImage(place_id, fav);
                return;
            }

            // Get the attribution text.
            CharSequence attribution = photoMetadata.getAttributions();
            // Get a full-size bitmap for the photo.
            Task<PlacePhotoResponse> photoResponse = mGeoDataClient.getPhoto(photoMetadata);
            photoResponse.addOnCompleteListener(task1 -> {
                PlacePhotoResponse photo = task1.getResult();
                Bitmap bitmap = photo.getBitmap();
                //set the bitmap
                fav.setImage(bitmap);

                //add it to fav item list
                favItemModels.add(fav);

                if (numOfFav == favItemModels.size()) {
                    //all done

                    //sort it
                    Collections.sort(favItemModels);

                    mAdapter.favItem = favItemModels;
                    mAdapter.notifyDataSetChanged();

                }
            });
        });
    }

    @Override
    public void onRefresh() {
        GetFavs();
        Log.d("refreshtab", "rrrrrrrrrrrr");

        //remove spining icon after 1 second
        new Handler().postDelayed(() -> mSwipeRefreshLayout.setRefreshing(false), 1000);
    }

    @Override
    public void onItemClicked(RecyclerView recyclerView, int position, View v) {

    }

    @Override
    public void onStartDrag(RecyclerView.ViewHolder viewHolder) {
        mItemTouchHelper.startDrag(viewHolder);
    }
}


