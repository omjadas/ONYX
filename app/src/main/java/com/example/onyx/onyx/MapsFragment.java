package com.example.onyx.onyx;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.directions.route.Route;
import com.directions.route.RouteException;
import com.directions.route.Routing;
import com.directions.route.RoutingListener;
import com.example.onyx.onyx.models.FBFav;
import com.example.onyx.onyx.ui.activities.UserListingActivity;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.GeoDataClient;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBufferResponse;
import com.google.android.gms.location.places.PlaceDetectionClient;
import com.google.android.gms.location.places.PlaceLikelihood;
import com.google.android.gms.location.places.PlaceLikelihoodBufferResponse;
import com.google.android.gms.location.places.PlacePhotoMetadata;
import com.google.android.gms.location.places.PlacePhotoMetadataBuffer;
import com.google.android.gms.location.places.PlacePhotoMetadataResponse;
import com.google.android.gms.location.places.PlacePhotoResponse;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.location.places.ui.SupportPlaceAutocompleteFragment;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.functions.FirebaseFunctions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static android.content.Context.LOCATION_SERVICE;

public class MapsFragment extends Fragment
        implements OnMapReadyCallback, LocationListener, GoogleMap.OnMarkerClickListener, RoutingListener {
    public static final String ARG_TYPE = "type";
    public static final String TYPE_CHATS = "type_chats";
    public static final String TYPE_ALL = "type_all";

    private static final String TAG = MapsFragment.class.getSimpleName();
    private GoogleMap mMap;
    private CameraPosition mCameraPosition;

    private Annotate annotations;

    // The entry points to the Places API.
    private GeoDataClient mGeoDataClient;
    private PlaceDetectionClient mPlaceDetectionClient;

    // The entry point to the Fused Location Provider.
    private FusedLocationProviderClient mFusedLocationProviderClient;

    // A default location (Sydney, Australia) and default zoom to use when location permission is
    // not granted.
    private final LatLng mDefaultLocation = new LatLng(-33.8523341, 151.2106085);
    private static final int DEFAULT_ZOOM = 15;
    public static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private boolean mLocationPermissionGranted;

    // The geographical location where the device is currently located. That is, the last-known
    // location retrieved by the Fused Location Provider.
    private Location mLastKnownLocation;

    // Keys for storing activity state.
    private static final String KEY_CAMERA_POSITION = "camera_position";
    private static final String KEY_LOCATION = "location";

    // Used for selecting the current place.
    private static final int M_MAX_ENTRIES = 5;
    private String[] mLikelyPlaceNames;
    private String[] mLikelyPlaceAddresses;
    private String[] mLikelyPlaceAttributions;
    ArrayList<ArrayList<Integer>> mLikelyPlaceTypes;
    private LatLng[] mLikelyPlaceLatLngs;

    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;
    private FirebaseFirestore db;
    private View mapView;
    Marker mCurrLocationMarker;

    private FirebaseFunctions mFunctions;

    //Used for annotating map
    private static final String CLEAR_CHARACTER = "*";
    private static final String POINT_SEPERATOR = "!";
    private static final String LAT_LNG_SEPERATOR = ",";

    private Button annotateButton;
    private Button undoButton;
    private Button cancelButton;
    private Button clearButton;
    private Button sendButton;


    //search bar autocomplete
    private PlaceAutocompleteFragment placeAutoComplete;
    private LatLng destPlace;
    private Place dest;
    private String destAddress;
    private Bitmap destImage;
    private Marker destMarker;
    private ArrayList<Marker> destRouteMarker = new ArrayList<>();
    private Polyline line = null;
    private TextView txtDistance, txtTime;

    private LocationManager locationManager = null;

    //Global flags
    private boolean firstRefresh = false;

    private SwipeRefreshLayout mSwipeRefreshLayout;
    private RecyclerView mRecyclerViewAllUserListing;

    private SupportPlaceAutocompleteFragment autocompleteFragment;
    private static View fragmentView;


    public static MapsFragment newInstance(String type) {
        Bundle args = new Bundle();
        args.putString(ARG_TYPE, type);
        MapsFragment fragment = new MapsFragment();
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        PlaceAutocompleteFragment f = (PlaceAutocompleteFragment) getActivity().getFragmentManager().findFragmentById(R.id.place_autocomplete);

        Log.d("aaaaaaaaaaaaaa1", String.valueOf(f == null));

        if (f != null && getActivity() != null && !getActivity().isFinishing()) {
            getActivity().getFragmentManager().beginTransaction().remove(f).commit();
        }

        /*FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        Fragment fragment = fragmentManager.findFragmentById(R.id.place_autocomplete);
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.remove(fragment);
        fragmentTransaction.commit();*/


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            mLastKnownLocation = savedInstanceState.getParcelable(KEY_LOCATION);
            mCameraPosition = savedInstanceState.getParcelable(KEY_CAMERA_POSITION);
        }
        /*
        if (fragmentView != null) {
            ViewGroup parent = (ViewGroup) fragmentView.getParent();
            if (parent != null)
                parent.removeView(fragmentView);
        }
        */
        mFunctions = FirebaseFunctions.getInstance();

        if (fragmentView == null)
            fragmentView = inflater.inflate(R.layout.maps_fragment, container, false);
        bindViews(fragmentView);

        mFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        db = FirebaseFirestore.getInstance();

        //Initialise annotations
        annotations = new Annotate(mMap);

        //Annotation buttons
        annotateButton = (Button) fragmentView.findViewById(R.id.addAnnotations);
        undoButton = (Button) fragmentView.findViewById(R.id.undo_button);
        cancelButton = (Button) fragmentView.findViewById(R.id.cancel_button);
        clearButton = (Button) fragmentView.findViewById(R.id.clear_button);
        sendButton = (Button) fragmentView.findViewById(R.id.send_button);

        //Sets annotation buttons to invisible
        hideAnnotationButtons(getView());

        //Request carer button
        Button requestCarerButton = fragmentView.findViewById(R.id.requestCarer);
        requestCarerButton.setVisibility(View.GONE);

        //Shows buttons depending on what type of user
        db.collection("users").document(mFirebaseUser.getUid()).get().addOnCompleteListener(task -> {
            if (!(boolean) task.getResult().getData().get("isCarer")) {
                hideAnnotationButtons(getView());
                requestCarerButton.setVisibility(View.VISIBLE);
            }

            //Carers who have a connected user have tools to annotate the users map
            else if(task.getResult().getData().get("connectedUser") != null){
                annotateButton.setVisibility(View.VISIBLE);
            }
        });


        annotateButton.setOnClickListener (this::annotateButtonClicked);
        undoButton.setOnClickListener(this::undoButtonClicked);
        cancelButton.setOnClickListener(this::cancelButtonClicked);
        clearButton.setOnClickListener(this::clearButtonClicked);
        sendButton.setOnClickListener(this::sendButtonClicked);

        requestCarerButton.setOnClickListener(this::getCarer);




        return fragmentView;
    }

    private void bindViews(View view) {
        mSwipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipe_refresh_layout);
        mRecyclerViewAllUserListing = (RecyclerView) view.findViewById(R.id.recycler_view_all_user_listing);
    }

    @Override
    public void onStop() {
        super.onStop();
        LocalBroadcastManager.getInstance(this.getContext()).unregisterReceiver(mMessageReceiver);
    }

    @Override
    public void onStart() {
        super.onStart();

        LocalBroadcastManager.getInstance(this.getContext()).registerReceiver((mMessageReceiver),
                new IntentFilter("MyData")
        );

        // Construct a GeoDataClient.
        mGeoDataClient = Places.getGeoDataClient(getActivity(), null);

        // Construct a PlaceDetectionClient.
        mPlaceDetectionClient = Places.getPlaceDetectionClient(getActivity(), null);

        // Construct a FusedLocationProviderClient.
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getActivity());


        // Build the map.
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        txtDistance = (TextView) getView().findViewById(R.id.txt_distance);
        txtTime = (TextView) getView().findViewById(R.id.txt_time);

        mapView = mapFragment.getView();


        //autocomplete search bar
        placeAutoComplete = (PlaceAutocompleteFragment) getActivity().getFragmentManager().findFragmentById(R.id.place_autocomplete);

        placeAutoComplete.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {

                dest = place;
                destPlace = place.getLatLng();
                Log.d("placeAutoComplete", "Place selected: " + place.getLatLng());

                Log.d("placeAutoComplete", "Current Location: " + mLastKnownLocation.getLatitude() + "   " + mLastKnownLocation.getLongitude());
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                        new LatLng(place.getLatLng().latitude,
                                place.getLatLng().longitude), DEFAULT_ZOOM));

                //remove old marker
                if (destMarker != null)
                    destMarker.remove();
                removeDestRouteMarker();
                // add marker to Destination

                ArrayList<String> snipArray = new ArrayList<>();
                snipArray.add(String.format("%,.1f", place.getRating()));
                snipArray.add("Tap to add this place to favrourites!");

                destAddress = place.getAddress().toString();
                //getPlacePhotos(place.getId());

                destMarker = mMap.addMarker(new MarkerOptions()
                        .position(place.getLatLng())
                        .title(place.getName().toString())
                        .snippet(snipArray.toString())
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
                firstRefresh = true;
                getRoutingPath();
            }

            @Override
            public void onError(Status status) {
                Log.d("Maps", "An error occurred: " + status);
            }
        });

    }

    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String points = intent.getExtras().getString("points");
            if(!points.contains("=")) {
                awaitingPoints(points);
            }else {
                Log.d("chad","bill");
                annotateButton.setVisibility(View.VISIBLE);
            }
        }
    };

    private void awaitingPoints(String pointsAsString) {

        //Prepare annotaion for new polyline
        annotations.setMap(mMap);
        annotations.newAnnotation();

        //clear annotations if containing clear character
        if (pointsAsString.contains(CLEAR_CHARACTER)) {
            annotations.clear();
        }
        //otherwise parse string to array list of LatLngs
        else {
            //split string between points
            String[] pointsAsStringArray = pointsAsString.split(POINT_SEPERATOR);
            ArrayList<LatLng> points = new ArrayList<>();

            for (String p : pointsAsStringArray) {
                //split string into latitude and longitude
                String[] latLong = p.split(LAT_LNG_SEPERATOR);

                //test for correct format
                if (p.length() >= 2) {
                    LatLng point = new LatLng(Double.parseDouble(latLong[0]), Double.parseDouble(latLong[1]));
                    points.add(point);
                }
            }

            //once parsed, draw the lines on map
            annotations.drawMultipleLines(points);
        }
    }
    /*
    gets the image for this place
     */
    private void getPlacePhotos(String place_id) {
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
                        destImage = bitmap;
                    }
                });
            }
        });
    }

    public void RouteToFavouriteLocation() {
        Bundle extras = getActivity().getIntent().getExtras();
        if (extras == null || !extras.containsKey("favLat")) {
            Log.d("Map-Fav", "no extra key");
            return;
        }
        Double dLat = Double.parseDouble(getActivity().getIntent().getExtras().getString("favLat"));
        Double dLng = Double.parseDouble(getActivity().getIntent().getExtras().getString("favLng"));
        String place_id = getActivity().getIntent().getExtras().getString("place_id");

        destPlace = new LatLng(dLat, dLng);

        final Task<PlaceBufferResponse> placeResponse = mGeoDataClient.getPlaceById(place_id);
        placeResponse.addOnCompleteListener(new OnCompleteListener<PlaceBufferResponse>() {
            @Override
            public void onComplete(@NonNull Task<PlaceBufferResponse> task) {
                if (task.isSuccessful()) {
                    //dest Place obj is first one
                    dest = task.getResult().get(0);
                }


            }
        });
        Log.d("Map-Fav", destPlace.toString());


        addFavLocationMarker();

        firstRefresh = true;
        getRoutingPath();
        //getMultiRoutingPath();
    }


    //calc route ,called from main
    public void RouteToFavouriteRoute() {
        Bundle extras = getActivity().getIntent().getExtras();
        if (extras == null || !extras.containsKey("favWayPoints")) {
            Log.d("Map-Fav", "no favWayPoints key");
            return;
        }

        List<String> wayPointString = Arrays.asList(getActivity().getIntent().getExtras().getString("favWayPoints").split(","));
        Log.d("wayyyyy", wayPointString.toString());

        ArrayList<LatLng> waypoints = new ArrayList<>();

        //convert pairs of string value into lat lng
        for (int i = 0; i < wayPointString.size() - 1; i += 2) {
            LatLng newLatLng = new LatLng(Double.parseDouble(wayPointString.get(i)), Double.parseDouble(wayPointString.get(i + 1)));
            waypoints.add(newLatLng);
        }

        Log.d("wayyyyy", waypoints.toString());

        destPlace = null;


        //Log.d("Map-Fav",destPlace.toString());


        //addFavLocationMarker();
        addFavLocationRouteMarker(waypoints);

        firstRefresh = true;

        getMultiRoutingPath(waypoints);
    }

    private void addFavLocationMarker() {
        if (destMarker != null)
            destMarker.remove();
        removeDestRouteMarker();
        // add marker to Destination
        destMarker = mMap.addMarker(new MarkerOptions()
                .position(destPlace)
                .title(getActivity().getIntent().getExtras().getString("favTitle"))
                .snippet("and snippet")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ROSE)));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                destPlace, DEFAULT_ZOOM));
    }

    private void addFavLocationRouteMarker(ArrayList<LatLng> waypoints) {
        if (destMarker != null)
            destMarker.remove();
        removeDestRouteMarker();
        // add marker to Destination

        int index = 1;
        for (LatLng pt : waypoints) {

            destRouteMarker.add(
                    mMap.addMarker(
                            new MarkerOptions()
                                    .position(pt)
                                    .title("The #" + index + " Destination")
                                    .snippet("from your favourite route")
                                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET)))
            );
            index++;
        }


    }

    private void removeDestRouteMarker() {
        if (destRouteMarker == null)
            return;

        for (Marker m : destRouteMarker) {
            m.remove();
        }
    }

    public void onResume() {
        super.onResume();
        firstRefresh = false;
        //Ensure the GPS is ON and location permission enabled for the application.
        locationManager = (LocationManager) getActivity().getSystemService(LOCATION_SERVICE);
        if (ContextCompat.checkSelfPermission(getActivity().getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            //Toast.makeText(getActivity(), "Fetching Location", Toast.LENGTH_SHORT).show();
            try {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, (LocationListener) this);
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 5000, 0, (LocationListener) this);
            } catch (Exception e) {
                Log.d("Map", e.toString());
                Toast.makeText(getActivity(), "ERROR: Cannot start location listener", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void onPause() {
        if (locationManager != null) {
            //Check needed in case of  API level 23.

            if (ContextCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            }
            try {
                locationManager.removeUpdates((LocationListener) getActivity());
            } catch (Exception e) {
            }
        }
        locationManager = null;
        super.onPause();
    }


    /**
     * Sets up the options menu.
     * @param menu The options menu.
     * @return Boolean.
     */


    /**
     * Handles a click on the menu option to get a place.
     *
     * @param item The menu item to handle.
     * @return Boolean.
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.option_get_place) {
            showCurrentPlace();
        } else if (item.getItemId() == R.id.map_to_chat) {
            startActivity(new Intent(getActivity(), MainActivity.class));

        } else if (item.getItemId() == R.id.menu_to_contacts) {
            UserListingActivity.startActivity(getActivity(),
                    Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);

        }


        return true;
    }

    /**
     * Manipulates the map when it's available.
     * This callback is triggered when the map is ready to be used.
     */
    @Override
    public void onMapReady(GoogleMap map) {
        mMap = map;
        /*try {
            String filePath = "toggleMap";
            FileInputStream stream = getActivity().getApplicationContext().openFileInput(filePath);
            if(stream != null){
                BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
                StringBuilder totalContent = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null){
                    totalContent.append(line).append('\n');
                }
                MapStyleOptions style = new MapStyleOptions(totalContent.toString());
                mMap.setMapStyle(style);
            }
        }
        catch (Resources.NotFoundException e) {
            Log.e(TAG, "Can't find style. Error: ", e);
        }
        catch (FileNotFoundException e){
            Log.e(TAG,"File not found",e);
        }
        catch (IOException e){
            Log.e(TAG,"File reading error",e);
        }*/


        /*
         * change the location of MYLocation button to bottom right location*/
        if (mapView != null &&
                mapView.findViewById(Integer.parseInt("1")) != null) {
            // Get the button view
            View locationButton = ((View) mapView.findViewById(Integer.parseInt("1")).getParent()).findViewById(Integer.parseInt("2"));
            // and next place it, on bottom right (as Google Maps app)
            RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams)
                    locationButton.getLayoutParams();
            // position on right bottom
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0);
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
            layoutParams.setMargins(0, 0, 30, 30);


        }

        // Use a custom info window adapter to handle multiple lines of text in the
        // info window contents.
        mMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {

            @Override
            // Return null here, so that getInfoContents() is called next.
            public View getInfoWindow(Marker arg0) {
                return null;
            }

            @Override
            public View getInfoContents(Marker marker) {
                // Inflate the layouts for the info window, title and snippet.
                View infoWindow = getLayoutInflater().inflate(R.layout.custom_info_contents,
                        (FrameLayout) getView().findViewById(R.id.map), false);

                TextView title = ((TextView) infoWindow.findViewById(R.id.title));
                title.setText(marker.getTitle());


                if (dest == null) {
                    return infoWindow;
                }

                String snipData = marker.getSnippet().substring(1, marker.getSnippet().length() - 1);
                List<String> myList = new ArrayList<String>(Arrays.asList(snipData.split(",")));

                String ratingNum = myList.get(0);

                TextView snippet = ((TextView) infoWindow.findViewById(R.id.snippet));


                //snippet.setText("Rating: "+ratingNum+"/5.0 for "+dest.getAddress());
                snippet.setText(dest.getAddress());


                RatingBar ratingbar = ((RatingBar) infoWindow.findViewById(R.id.ratingBar));
                ratingbar.setNumStars(5);
                //ratingbar.setRating(Float.parseFloat(ratingNum));
                ratingbar.setRating(dest.getRating());

                return infoWindow;
            }
        });

        //save this place to firestore
        mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {

                if (dest == null) {
                    return;
                }
                Log.d("infowindow", "clickedddddddddddddd");
                FBFav fav = new FBFav(
                        dest.getId().toString(),
                        dest.getName().toString(),
                        //destImage,
                        new GeoPoint(destPlace.latitude, destPlace.longitude),
                        dest.getAddress().toString(),
                        1,
                        (long) (Timestamp.now().getSeconds())
                );

                final DocumentReference reference = FirebaseFirestore.getInstance().collection("users").document(FirebaseAuth.getInstance().getCurrentUser().getUid());
                reference.collection("fav").document(dest.getId().toString()).get().
                        addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task0) {

                                if (task0.isSuccessful()) {
                                    if (!task0.getResult().exists()) {
                                        Log.d("saveFav", "not there");
                                        //only add to firebase if not exist
                                        reference.get().
                                                addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                        if (task.isSuccessful()) {
                                                            DocumentSnapshot document = task.getResult();
                                                            reference.collection("fav").document(dest.getId().toString()).set(fav);
                                                            Log.d("saveFav", "now added");
                                                        }
                                                    }
                                                });
                                    } else {
                                        Log.d("saveFav", "already added");
                                    }

                                }

                            }
                        });


            }
        });

        // Prompt the user for permission.
        getLocationPermission();

        // Turn on the My Location layer and the related control on the map.
        updateLocationUI();

        // Get the current location of the device and set the position of the map.
        getDeviceLocation();

        // On click listener for annotations
        mMap.setOnMapClickListener(arg0 -> {
            if(annotations.isAnnotating()) {
                annotations.setMap(mMap);
                annotations.drawLine(arg0);
            }
        });


    }

    /**
     * Gets the current location of the device, and positions the map's camera.
     */
    private void getDeviceLocation() {
        /*
         * Get the best and most recent location of the device, which may be null in rare
         * cases when a location is not available.
         */
        try {
            if (mLocationPermissionGranted) {
                Task<Location> locationResult = mFusedLocationProviderClient.getLastLocation();
                locationResult.addOnCompleteListener(getActivity(), new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        if (task.isSuccessful() && task.getResult() != null) {
                            // Set the map's camera position to the current location of the device.
                            mLastKnownLocation = task.getResult();
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                                    new LatLng(mLastKnownLocation.getLatitude(),
                                            mLastKnownLocation.getLongitude()), DEFAULT_ZOOM));

                            //route to fav place if excist
                            RouteToFavouriteLocation();

                            placeAutoComplete.setBoundsBias(new LatLngBounds(
                                    new LatLng(mLastKnownLocation.getLatitude() - 0.1, mLastKnownLocation.getLongitude() - 0.1),
                                    new LatLng(mLastKnownLocation.getLatitude() + 0.1, mLastKnownLocation.getLongitude() + 0.1)));
                        } else {
                            Log.d(TAG, "Current location is null. Using defaults.");
                            Log.e(TAG, "Exception: %s", task.getException());
                            mMap.moveCamera(CameraUpdateFactory
                                    .newLatLngZoom(mDefaultLocation, DEFAULT_ZOOM));
                            mMap.getUiSettings().setMyLocationButtonEnabled(false);


                        }
                    }
                });
            }
        } catch (SecurityException e) {
            Log.e("Exception: %s", e.getMessage());
        }
    }


    /**
     * Prompts the user for permission to use the device location.
     */
    private void getLocationPermission() {
        /*
         * Request location permission, so that we can get the location of the
         * device. The result of the permission request is handled by a callback,
         * onRequestPermissionsResult.
         */
        if (ContextCompat.checkSelfPermission(getActivity().getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;
        } else {
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);

        }
    }

    /**
     * Handles the result of the request for location permissions.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        mLocationPermissionGranted = false;
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mLocationPermissionGranted = true;
                }
            }
        }
        updateLocationUI();
    }

    /**
     * Prompts the user to select the current place from a list of likely places, and shows the
     * current place on the map - provided the user has granted location permission.
     */
    private void showCurrentPlace() {
        if (mMap == null) {
            return;
        }

        if (mLocationPermissionGranted) {
            // Get the likely places - that is, the businesses and other points of interest that
            // are the best match for the device's current location.
            @SuppressWarnings("MissingPermission") final Task<PlaceLikelihoodBufferResponse> placeResult =
                    mPlaceDetectionClient.getCurrentPlace(null);
            placeResult.addOnCompleteListener
                    (new OnCompleteListener<PlaceLikelihoodBufferResponse>() {
                        @Override
                        public void onComplete(@NonNull Task<PlaceLikelihoodBufferResponse> task) {
                            if (task.isSuccessful() && task.getResult() != null) {
                                PlaceLikelihoodBufferResponse likelyPlaces = task.getResult();

                                // Set the count, handling cases where less than 5 entries are returned.
                                int count;
                                if (likelyPlaces.getCount() < M_MAX_ENTRIES) {
                                    count = likelyPlaces.getCount();
                                } else {
                                    count = M_MAX_ENTRIES;
                                }

                                int i = 0;
                                mLikelyPlaceNames = new String[count];
                                mLikelyPlaceAddresses = new String[count];
                                mLikelyPlaceAttributions = new String[count];
                                mLikelyPlaceLatLngs = new LatLng[count];

                                for (PlaceLikelihood placeLikelihood : likelyPlaces) {
                                    // Build a list of likely places to show the user.
                                    mLikelyPlaceNames[i] = (String) placeLikelihood.getPlace().getName();
                                    mLikelyPlaceAddresses[i] = (String) placeLikelihood.getPlace()
                                            .getAddress();
                                    mLikelyPlaceAttributions[i] = (String) placeLikelihood.getPlace()
                                            .getAttributions();
                                    mLikelyPlaceLatLngs[i] = placeLikelihood.getPlace().getLatLng();

                                    i++;
                                    if (i > (count - 1)) {
                                        break;
                                    }
                                }

                                // Release the place likelihood buffer, to avoid memory leaks.
                                likelyPlaces.release();

                                // Show a dialog offering the user the list of likely places, and add a
                                // marker at the selected place.
                                openPlacesDialog();

                            } else {
                                Log.e(TAG, "Exception: %s", task.getException());
                            }
                        }
                    });
        } else {
            // The user has not granted permission.
            Log.i(TAG, "The user did not grant location permission.");

            // Add a default marker, because the user hasn't selected a place.
            mMap.addMarker(new MarkerOptions()
                    .title(getString(R.string.default_info_title))
                    .position(mDefaultLocation)
                    .snippet(getString(R.string.default_info_snippet)));

            // Prompt the user for permission.
            getLocationPermission();
        }
    }

    /**
     * Displays a form allowing the user to select a place from a list of likely places.
     */
    private void openPlacesDialog() {
        // Ask the user to choose the place where they are now.
        DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // The "which" argument contains the position of the selected item.
                LatLng markerLatLng = mLikelyPlaceLatLngs[which];
                String markerSnippet = mLikelyPlaceAddresses[which];
                if (mLikelyPlaceAttributions[which] != null) {
                    markerSnippet = markerSnippet + "\n" + mLikelyPlaceAttributions[which];
                }

                // Add a marker for the selected place, with an info window
                // showing information about that place.
                destPlace = markerLatLng;
                if (destMarker != null)
                    destMarker.remove();
                destMarker = mMap.addMarker(new MarkerOptions()
                        .position(markerLatLng)
                        .title(mLikelyPlaceNames[which])
                        .snippet("and snippet")
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
                firstRefresh = true;

                // Position the map's camera at the location of the marker.
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(markerLatLng,
                        DEFAULT_ZOOM));
                Log.d("Map", "get placccccccccccccccccccc");
                getRoutingPath();
            }
        };


        // Display the dialog.
        AlertDialog dialog = new AlertDialog.Builder(getActivity())
                .setTitle(R.string.pick_place)
                .setItems(mLikelyPlaceNames, listener)
                .show();
    }


    /**
     * Displays a form allowing the user to select a place from a list of likely places.
     */
    private void togglePlaces(int index) {
        LatLng markerLatLng = mLikelyPlaceLatLngs[index];
        String markerSnippet = mLikelyPlaceAddresses[index];
        if (mLikelyPlaceAttributions[index] != null) {
            markerSnippet = markerSnippet + "\n" + mLikelyPlaceAttributions[index];
        }

        // Add a marker for the selected place, with an info window
        // showing information about that place.
        if (markerLatLng != null) {
            Marker toggleMarker = mMap.addMarker(new MarkerOptions()
                    .position(markerLatLng)
                    .title(mLikelyPlaceNames[index])
                    .snippet("and snippet")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
        }
    }

    /**
     * Updates the map's UI settings based on whether the user has granted location permission.
     */
    private void updateLocationUI() {
        if (mMap == null) {
            return;
        }
        try {
            if (mLocationPermissionGranted) {
                mMap.setMyLocationEnabled(true);
                mMap.getUiSettings().setMyLocationButtonEnabled(true);
            } else {
                mMap.setMyLocationEnabled(false);
                mMap.getUiSettings().setMyLocationButtonEnabled(false);
                mLastKnownLocation = null;
                getLocationPermission();
            }
        } catch (SecurityException e) {
            Log.e("Exception: %s", e.getMessage());
        }
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        if (destMarker != null) {
            destMarker.showInfoWindow();
        }
        return true;
    }

    /**
     * @method getRoutingPath
     * @desc Method to draw the google routed path.
     */
    private void getRoutingPath() {
        if (mLastKnownLocation == null || destPlace == null)
            return;
        try {

            //Do Routing
            Routing routing = new Routing.Builder()
                    .key("AIzaSyCJJY5Qwt0Adki43NdMHWh9O88VR-dEByI")
                    .travelMode(Routing.TravelMode.WALKING)
                    .withListener(this)
                    .waypoints(new LatLng(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude()), destPlace)
                    .build();
            routing.execute();
        } catch (Exception e) {
            Log.d("Map", "getRoutingPath faillllllllllll");
        }
    }

    /**
     * @method getMultiRoutingPath
     * @desc Method to draw the google routed path that connects multiple waypoints.
     */
    private void getMultiRoutingPath(List<LatLng> wayPoints) {
        if (wayPoints == null)
            return;
        try {

            //LatLng dest1 = new LatLng(-37.7964,144.9612);
            //LatLng dest2 = new LatLng(-37.8098,144.9652);

            //insert current location into array
            LatLng myCurrentLocation = new LatLng(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude());
            wayPoints.add(0, myCurrentLocation);

            //List<LatLng> waypts = new ArrayList<>();
            //waypts.add(dest1);
            //waypts.add(dest2);


            //Do Routing
            Routing routing = new Routing.Builder()
                    .key("AIzaSyCJJY5Qwt0Adki43NdMHWh9O88VR-dEByI")
                    .travelMode(Routing.TravelMode.WALKING)
                    .withListener(this)
                    .waypoints(wayPoints)
                    .alternativeRoutes(true)
                    .build();
            routing.execute();
        } catch (Exception e) {
            Log.d("Map", "getRoutingPath faillllllllllll");
        }
    }

    @Override
    public void onRoutingFailure(RouteException e) {
        Toast.makeText(getActivity(), "Routing Failed", Toast.LENGTH_SHORT).show();

    }

    @Override
    public void onRoutingStart() {

    }

    @Override
    public void onRoutingSuccess(ArrayList<Route> list, int i) {
        try {
            //Get all points and plot the polyLine route.
            List<LatLng> listPoints = list.get(0).getPoints();
            PolylineOptions options = new PolylineOptions().width(15).color(Color.rgb(51, 153, 255)).geodesic(true);
            Iterator<LatLng> iterator = listPoints.iterator();
            while (iterator.hasNext()) {
                LatLng data = iterator.next();
                options.add(data);
            }

            //If line not null then remove old polyline routing.
            if (line != null) {
                line.remove();
            }
            line = mMap.addPolyline(options);

            //Show distance and duration.
            txtDistance.setText("Distance: " + list.get(0).getDistanceText());
            txtTime.setText("Duration: " + list.get(0).getDurationText());

            //Focus on map bounds
            mMap.moveCamera(CameraUpdateFactory.newLatLng(list.get(0).getLatLgnBounds().getCenter()));
            LatLngBounds.Builder builder = new LatLngBounds.Builder();
            builder.include(new LatLng(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude()));
            builder.include(destPlace);
            LatLngBounds bounds = builder.build();
            mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 50));
        } catch (Exception e) {

        }

    }

    @Override
    public void onRoutingCancelled() {

    }

    @Override
    public void onLocationChanged(Location location) {
        double lat = location.getLatitude();
        double lng = location.getLongitude();

        LatLng curLatLng = new LatLng(lat, lng);
        if (firstRefresh && destMarker != null) {
            //Add Start Marker.
            //mCurrLocationMarker = mMap.addMarker(new MarkerOptions().position(new LatLng(lat,lng)).title("Current Position"));//.icon(BitmapDescriptorFactory.fromResource(R.drawable.location)));
            firstRefresh = false;
            //destMarker = mMap.addMarker(new MarkerOptions().position(curLatLng).title("Destination"));//.icon(BitmapDescriptorFactory.fromResource(R.drawable.location)));
            mMap.moveCamera(CameraUpdateFactory.newLatLng(curLatLng));
            //mMap.animateCamera(CameraUpdateFactory.zoomTo(15));
            getRoutingPath();
        } else {
            //mCurrLocationMarker.setPosition(new LatLng(mLastKnownLocation.getLatitude(),mLastKnownLocation.getLongitude()));
        }
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }

    public void getCarer(View v) {
        Toast.makeText(getContext(), "Requesting a carer", Toast.LENGTH_SHORT).show();
        requestCarer().addOnSuccessListener(s -> {
            Toast.makeText(getContext(), s, Toast.LENGTH_SHORT).show();
        });
    }

    //Hide buttons related to annotations
    private void hideAnnotationButtons(View v){
        annotateButton.setVisibility(v.GONE);
        undoButton.setVisibility(v.GONE);
        cancelButton.setVisibility(v.GONE);
        clearButton.setVisibility(v.GONE);
        sendButton.setVisibility(v.GONE);
    }


    //ANNOTATION BUTTONS

    private void annotateButtonClicked(View v){
        annotateButton.setVisibility(v.GONE);
        undoButton.setVisibility(v.VISIBLE);
        cancelButton.setVisibility(v.VISIBLE);
        clearButton.setVisibility(v.VISIBLE);
        sendButton.setVisibility(v.VISIBLE);
        annotations.setAnnotating(true);
    }

    private void cancelButtonClicked(View v){
        hideAnnotationButtons(v);
        annotateButton.setVisibility(v.VISIBLE);
        annotations.setAnnotating(false);
    }

    private void undoButtonClicked(View v){
        annotations.undo();
    }

    private void clearButtonClicked(View v){
        annotations.clear();
        sendClearedAnnotations();

        //failsafe incase user was not connected when clear was sent
        annotations.setUndoHasOccurred(true);
    }

    public void sendButtonClicked(View v) {
        Toast.makeText(getContext(), "Sending annotation", Toast.LENGTH_SHORT).show();
        if(annotations.hasUndoOccurred()) {
            sendClearedAnnotations().addOnSuccessListener(s -> {
                sendAllAnnotations();
            }).addOnFailureListener(f -> Log.d("send button", "failure"));
        }else{
            sendAllAnnotations();
        }
        annotations.newAnnotation();
        annotations.setUndoHasOccurred(false);
    }

    //CLOUD FUNCTION CALLS

    private Task<String> requestCarer() {
        return mFunctions
                .getHttpsCallable("requestCarer")
                .call()
                .continueWith(task -> (String) task.getResult().getData());
    }

    //Send all annotations on carer's map to connected user
    private void sendAllAnnotations() {
        ArrayList<ArrayList<GeoPoint>> points = annotations.getAnnotations();
        if (points.size() > 0) {
            for (ArrayList<GeoPoint> p :points) {
                if(p.size()>0)
                    sendAnnotation(p)
                            .addOnFailureListener(f -> Log.d("send button", "sent annotation failed"))
                            //removes p from list of points to send next time
                            .addOnSuccessListener(f -> annotations.successfulSend(p));
            }

        }
        //send empty list
        else{
            sendAnnotation(new ArrayList<>())
                    .addOnFailureListener(f -> Log.d("send", "failure"));
        }
    }

    //Sends an individual annotation (or polyline) to the connected user
    private Task<String> sendAnnotation(ArrayList<GeoPoint> points) {
        Map<String, Object> newRequest = new HashMap<>();
        String annotationToString = " ";

        //encode arraylist as string
        for(GeoPoint g : points){
            annotationToString = annotationToString + Double.toString(g.getLatitude()) + LAT_LNG_SEPERATOR;
            annotationToString = annotationToString + Double.toString(g.getLongitude()) + POINT_SEPERATOR;
        }

        //call cloud function and send encoded points to connected user
        newRequest.put("points",annotationToString);
        return mFunctions
                .getHttpsCallable("sendAnnotation")
                .call(newRequest)
                .continueWith(task -> (String) task.getResult().getData());
    }

    //Clears the connected users map of all annotations
    private Task<String> sendClearedAnnotations() {
        //sends coded clear character to connected user
        Map<String, Object> newRequest = new HashMap<>();
        newRequest.put("points", CLEAR_CHARACTER);
        return mFunctions
                .getHttpsCallable("sendAnnotation")
                .call(newRequest)
                .continueWith(task -> (String) task.getResult().getData());
    }
}