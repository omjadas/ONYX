package com.example.onyx.onyx;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
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
import android.widget.TextView;

import com.example.onyx.onyx.models.FBFav;
import com.example.onyx.onyx.models.FavItemModel;
import com.example.onyx.onyx.ui.adapters.FavItemDragCallback;
import com.example.onyx.onyx.ui.adapters.FavouriteItemRecyclerView;
import com.example.onyx.onyx.ui.adapters.IDragListener;
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
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;


public class FavouriteItemList extends Fragment implements ItemClickSupport.OnItemClickListener, SwipeRefreshLayout.OnRefreshListener, IDragListener {


    private FavouriteItemRecyclerView mFavItemAdapter;


    private View view;

    private LinearLayout linearLayout;

    private ArrayList<FavItemModel> favItemModels;

    private RecyclerView recyclerView;
    private FavouriteItemRecyclerView mAdapter;

    private GeoDataClient mGeoDataClient;

    private int numOfFav = 999999;
    private ItemTouchHelper mItemTouchHelper;

    private boolean refreshing;

    private TextView fav_item_text_hint;


    //date to inflate the fav fragment
    private Integer image[] = {R.drawable.square_img, R.drawable.square_img, R.drawable.square_img,
            R.drawable.square_img, R.drawable.square_img, R.drawable.square_img, R.drawable.square_img};
    private SwipeRefreshLayout mSwipeRefreshLayout;

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        //init();
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mGeoDataClient = Places.getGeoDataClient(Objects.requireNonNull(getActivity()), null);

        view = inflater.inflate(R.layout.fragment_fav_item, container, false);


        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);


        recyclerView = view.findViewById(R.id.recyclerview);

        //attach listener to refreshlayout
        mSwipeRefreshLayout = view.findViewById(R.id.fav_swipe2);
        mSwipeRefreshLayout.setOnRefreshListener(this);

        //db = FirebaseFirestore.getInstance();
        //get fav places for current user
        //GetFavs();

        mAdapter = new FavouriteItemRecyclerView(getActivity(), favItemModels, this);

        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(mAdapter);


        ItemClickSupport.addTo(recyclerView)
                .setOnItemClickListener(this);


        ItemTouchHelper.Callback callback = new FavItemDragCallback(mAdapter);
        mItemTouchHelper = new ItemTouchHelper(callback);
        mItemTouchHelper.attachToRecyclerView(recyclerView);

        fav_item_text_hint = view.findViewById(R.id.fav_item_text_hint);

        checkUpdate();

        return view;

    }

    public void GetFavs() {
        if(refreshing)
        {
            //checking if other method already calling it;
            return;
        }
        else
        {
            //set to is refreshing
            refreshing =true;
        }

        if (favItemModels==null)
        {
            //need to create
            favItemModels = new ArrayList<>();

        }
        else
        {
            //don't create new, just clear it.
            favItemModels.clear();
        }


        FirebaseFirestore.getInstance().collection("users").document(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid()).collection("fav")
                .get()
                .addOnCompleteListener(task -> {
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
                        Log.d("place","number of list docs : " + numOfFav);

                        //no point going forward
                        if (numOfFav == 0) {
                            fav_item_text_hint.setVisibility(View.VISIBLE);
                            return;
                        } else {
                            fav_item_text_hint.setVisibility(View.INVISIBLE);
                        }
                        for (DocumentSnapshot dss : myListOfDocuments) {


                            if (dss.exists()) {

                                //firebase doc to fbfav class
                                FBFav fav = dss.toObject(FBFav.class);

                                //set up falg
                                boolean flag = true;
                                for (FavItemModel favModel : favItemModels) {
                                    if (favModel.getPlaceID().equals(Objects.requireNonNull(fav).placeID)) {
                                        //found duplicate,
                                        flag = false;
                                    }
                                }
                                if (flag) {
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
                                            i + 1 + "",
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
                    }
                });
    }


    private void checkUpdate() {

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

    /**
     * fill in default image
     *
     * @param place_id
     * @param fav
     */
    private void FillInDefaultFavItemObjectImage(String place_id, FavItemModel fav) {

        Bitmap defalutBitmap = BitmapFactory.decodeResource(Objects.requireNonNull(getContext()).getResources(),
                R.drawable.ic_img);
        fav.setImage(defalutBitmap);

        //add it to fav item list
        favItemModels.add(fav);

        if (numOfFav == favItemModels.size()) {
            //all done
            //sort it
            Collections.sort(favItemModels);

            mAdapter.favItem = favItemModels;

            mAdapter.notifyDataSetChanged();
            //recyclerView.setAdapter(mAdapter);

            //refreshing done
            refreshing =false;


        }
        return;
    }

    /**
     * gets the image for this place
     *
     * @param place_id
     * @param fav
     */
    private void FillInFavItemObjectImage(String place_id, FavItemModel fav) {

        Log.d("place id is ", place_id);
        final Task<PlacePhotoMetadataResponse> photoMetadataResponse = mGeoDataClient.getPlacePhotos(place_id);
        photoMetadataResponse.addOnCompleteListener(new OnCompleteListener<PlacePhotoMetadataResponse>() {
            @Override
            public void onComplete(@NonNull Task<PlacePhotoMetadataResponse> task) {
            // Get the list of photos.
            PlacePhotoMetadataResponse photos = task.getResult();
            if (photos == null) {   //checks if place has photo;

                Log.d("place id is ", "!if place has photo");
                FillInDefaultFavItemObjectImage(place_id, fav);
                return;
            }

            if (photos.getPhotoMetadata() == null) {   //checks if place has photo meta data;
                Log.d("place id is ", "no photo meta data");
                FillInDefaultFavItemObjectImage(place_id, fav);
                return;
            }

            // Get the PlacePhotoMetadataBuffer (metadata for all of the photos).
            PlacePhotoMetadataBuffer photoMetadataBuffer = photos.getPhotoMetadata();


            if(photoMetadataBuffer.getCount()<1) {   //checks if photoMetadataBuffer  is null or get 0 will be null;
                Log.d("place id is ", "buffer size 0");

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
                    //mAdapter = new FavouriteItemRecyclerView(getActivity(), favItemModels);

                    //sort it
                    Collections.sort(favItemModels);

                    mAdapter.favItem = favItemModels;
                    mAdapter.notifyDataSetChanged();
                    //recyclerView.setAdapter(mAdapter);

                    Log.d("favdup", favItemModels.toString() + "  " + favItemModels.size());

                    //finished with entire list
                    refreshing =false;


                }
            });
        };
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
    public void onStartDrag(RecyclerView.ViewHolder viewHolder) {
        mItemTouchHelper.startDrag(viewHolder);
    }


    @Override
    public void onItemClicked(RecyclerView recyclerView, int position, View v) {
        String distance = mAdapter.getFavItem(position).getAddress();
        String title = mAdapter.getFavItem(position).getTitle();
        String num = mAdapter.getFavItem(position).getNumber();
        String placeID = mAdapter.getFavItem(position).getPlaceID();

        Log.d("favItemList", "clicked " + num + "  title is: " + title + "   distance is: " + distance);

        //update visit freq in firebase
        final DocumentReference reference = FirebaseFirestore.getInstance()
                .collection("users")
                .document(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid());
        reference.collection("fav").document(placeID).get()
                .addOnCompleteListener(task0 -> {
                    if (task0.isSuccessful()) {
                        DocumentSnapshot document = task0.getResult();

                        //increase freq by 1
                        Integer freq = Integer.parseInt(Objects.requireNonNull(document.get("freq")).toString()) + 1;
                        if (task0.getResult().exists()) {
                            Log.d("saveFreq", "is there");
                            //only add to firebase if not exist
                            reference.collection("fav")
                                    .document(placeID)
                                    .update("freq", freq);
                            Log.d("saveFreq", "freq updated, now it's " + freq);
                        } else {
                            //not there , can't do much here
                            Log.d("addFreq", "not there");
                        }
                    }
                });//update visit freq in firebase done

        LatLng latlng = mAdapter.getFavItem(position).getLatlng();
        ((MainActivity) Objects.requireNonNull(getActivity())).FavStartMap(latlng.latitude + "", latlng.longitude + "", title, placeID);
    }
}


