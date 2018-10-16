package com.example.onyx.onyx;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
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
import android.widget.ImageButton;
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
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.GeoDataClient;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBufferResponse;
import com.google.android.gms.location.places.PlaceDetectionClient;
import com.google.android.gms.location.places.PlaceLikelihood;
import com.google.android.gms.location.places.PlaceLikelihoodBufferResponse;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.location.places.ui.SupportPlaceAutocompleteFragment;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.maps.android.SphericalUtil;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static android.content.Context.LOCATION_SERVICE;

public class MapsFragment extends Fragment
        implements OnMapReadyCallback, LocationListener, GoogleMap.OnMarkerClickListener, RoutingListener {
    public static final String ARG_TYPE = "type";
    public static final String TYPE_CHATS = "type_chats";
    public static final String TYPE_ALL = "type_all";
    public static final int RADIUS = 1500;

    public static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private static final String TAG = MapsFragment.class.getSimpleName();
    private static final int DEFAULT_ZOOM = 15;
    // Keys for storing activity state.
    private static final String KEY_CAMERA_POSITION = "camera_position";
    private static final String KEY_LOCATION = "location";
    // Used for selecting the current place.
    private static final int M_MAX_ENTRIES = 5;
    //Used for annotating map
    private static final String CLEAR_CHARACTER = "*";
    private static final String POINT_SEPERATOR = "!";
    private static final String LAT_LNG_SEPERATOR = ",";
    private static final String USER_TAG = "person";
    private static View fragmentView;
    // A default location (Sydney, Australia) and default zoom to use when location permission is
    // not granted.
    private final LatLng mDefaultLocation = new LatLng(-33.8523341, 151.2106085);
    ArrayList<ArrayList<Integer>> mLikelyPlaceTypes;
    Marker mCurrLocationMarker;
    private GoogleMap mMap;
    private CameraPosition mCameraPosition;
    private Annotate annotations;
    // The entry points to the Places API.
    private GeoDataClient mGeoDataClient;
    private PlaceDetectionClient mPlaceDetectionClient;
    // The entry point to the Fused Location Provider.
    private FusedLocationProviderClient mFusedLocationProviderClient;
    // The geographical location where the device is currently located. That is, the last-known
    // location retrieved by the Fused Location Provider.
    private Location mLastKnownLocation;
    private String[] mLikelyPlaceNames;
    private String[] mLikelyPlaceAddresses;
    private String[] mLikelyPlaceAttributions;
    private LatLng[] mLikelyPlaceLatLngs;
    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;
    private FirebaseFirestore db;
    private MapView mapView;
    private FirebaseFunctions mFunctions;
    private FloatingActionButton annotateButton;
    private FloatingActionButton undoButton;
    private FloatingActionButton cancelButton;
    private FloatingActionButton clearButton;
    private FloatingActionButton sendButton;
    private Button requestButton;
    private Button disconnectButton;

    //Nearby buttons
    private ImageButton restaurantButton;
    private ImageButton cafeButton;
    private ImageButton taxiButton;
    private ImageButton stationButton;
    private ImageButton atmButton;
    private ImageButton hospitalButton;
    private Button exitNearby;
    private Button startNearby;


    //search bar autocomplete
    private SupportPlaceAutocompleteFragment placeAutoComplete;
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
    private final BroadcastReceiver mAnnotationReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String points = (intent.getExtras()).getString("points");
            awaitingPoints((points));
        }
    };

    private LatLng connectedUserLocation;
    private Marker connectedUserMarker;
    private String connectedUserName;


    private final BroadcastReceiver mLocationReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle bundle = intent.getParcelableExtra("bundle");

            connectedUserLocation = bundle.getParcelable("location");
            connectedUserName = intent.getStringExtra("name");

            Log.d(TAG, "location: " + connectedUserLocation);

            if (connectedUserMarker != null) {
                connectedUserMarker.setPosition(connectedUserLocation);
                connectedUserMarker.setTitle(connectedUserName);
            } else {
                connectedUserMarker = mMap.addMarker(new MarkerOptions()
                        .position(connectedUserLocation)
                        .title(connectedUserName)
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_person))
                );
                connectedUserMarker.setTag(USER_TAG);
            }
        }
    };

    private final BroadcastReceiver mDisconnectReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "Disconnecting from user");
            db.collection("users").document(mFirebaseUser.getUid()).get().addOnCompleteListener(task  -> {
                disconnectButton.setVisibility(View.GONE);
                connectedUserMarker.remove();
                connectedUserMarker = null;
                if (!(boolean) ((task.getResult()).getData()).get("isCarer")) {
                    requestButton.setVisibility(View.VISIBLE);
                } else {
                    hideAnnotationButtons(getView());
                }
            });
        }
    };

    private final BroadcastReceiver mConnectReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            db.collection("users").document(mFirebaseUser.getUid()).get().addOnCompleteListener(task  -> {
                requestButton.setVisibility(View.GONE);
                disconnectButton.setVisibility(View.VISIBLE);
                if ((boolean) (((task.getResult()).getData())).get("isCarer")) {
                    annotateButton.setVisibility(View.VISIBLE);
                }
            });
        }
    };

    //Recieve notification when map style is updated in toggle map section and r-edraw
    private final BroadcastReceiver mStyleReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            filterMap();
        }
    };

    private ArrayList<SOS> sosList = new ArrayList<>();

    private final BroadcastReceiver mSOSReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle bundle = intent.getParcelableExtra("bundle");

            String id = intent.getStringExtra("id");
            LatLng location = bundle.getParcelable("location");
            String name = intent.getStringExtra("name");

            Log.d(TAG, "location: " + location);

            for (SOS s : sosList) {
                if (s.id.equals(id)) {
                    return;
                }
            }

            SOS mySos = new SOS(id,
                    location,
                    intent.getStringExtra("name"),
                    mMap.addMarker(new MarkerOptions()
                            .position((location))
                            .title(name)
                            .icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_sos_marker))
                    ));
            mySos.marker.setTag(USER_TAG);

            sosList.add(mySos);
            destPlace = mySos.location;
            getRoutingPath();
        }
    };

    private final BroadcastReceiver mOKReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String id = intent.getStringExtra("id");
            for (SOS s : sosList) {
                if (s.id.equals(id)) {
                    s.marker.remove();
                    sosList.remove(s);
                    return;
                }
            }
        }
    };

    public static MapsFragment newInstance(String type) {
        Bundle args = new Bundle();
        args.putString(ARG_TYPE, type);
        MapsFragment fragment = new MapsFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onDestroyView() {
        if (getActivity() != null) {
            getChildFragmentManager().beginTransaction().remove(placeAutoComplete).commitAllowingStateLoss();
        }
        super.onDestroyView();

        // Unregister broadcast receivers
        LocalBroadcastManager.getInstance((this.getContext())).unregisterReceiver((mAnnotationReceiver));
        LocalBroadcastManager.getInstance(this.getContext()).unregisterReceiver((mLocationReceiver));
        LocalBroadcastManager.getInstance(this.getContext()).unregisterReceiver((mDisconnectReceiver));
        LocalBroadcastManager.getInstance(this.getContext()).unregisterReceiver((mConnectReceiver));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mapView != null) {
            mapView.onDestroy();
        }
        //placeAutoComplete.onDestroy();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            mLastKnownLocation = savedInstanceState.getParcelable(KEY_LOCATION);
            mCameraPosition = savedInstanceState.getParcelable(KEY_CAMERA_POSITION);
        }

        mFunctions = FirebaseFunctions.getInstance();

        if (fragmentView == null)
            fragmentView = inflater.inflate(R.layout.maps_fragment, container, false);
        bindViews(fragmentView);

        MapsInitializer.initialize((getActivity()));

        switch (GooglePlayServicesUtil.isGooglePlayServicesAvailable(getActivity())) {
            case ConnectionResult.SUCCESS:
                mapView = fragmentView.findViewById(R.id.map);
                mapView.onCreate(savedInstanceState);
                if (mapView != null) {
                    (mapView).getMapAsync(this);
                }
                break;
            case ConnectionResult.SERVICE_MISSING:
                //Missing
                break;
            default:
                //Something is wrong
                break;
        }

        placeAutoComplete = new SupportPlaceAutocompleteFragment();
        getChildFragmentManager().beginTransaction().
                replace(R.id.place_autocomplete_container, placeAutoComplete).
                commitAllowingStateLoss();

        placeAutoComplete.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                mMap.clear();
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
                snipArray.add(place.getId());
                snipArray.add((place.getAddress()).toString().replaceAll(",", " "));
                snipArray.add(place.getLatLng().latitude + "");
                snipArray.add(place.getLatLng().longitude + "");


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


        mFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        db = FirebaseFirestore.getInstance();

        //Initialise annotations
        annotations = new Annotate(mMap);

        //Annotation buttons
        annotateButton = fragmentView.findViewById(R.id.addAnnotations);
        undoButton = fragmentView.findViewById(R.id.undo_button);
        cancelButton = fragmentView.findViewById(R.id.cancel_button);
        clearButton = fragmentView.findViewById(R.id.clear_button);
        sendButton = fragmentView.findViewById(R.id.send_button);

        //Nearby buttons
        restaurantButton = fragmentView.findViewById(R.id.Restauarant);
        cafeButton = fragmentView.findViewById(R.id.Cafe);
        taxiButton = fragmentView.findViewById(R.id.Taxi);
        stationButton = fragmentView.findViewById(R.id.Station);
        atmButton = fragmentView.findViewById(R.id.ATM);
        hospitalButton = fragmentView.findViewById(R.id.Hospital);
        exitNearby = fragmentView.findViewById(R.id.ExitNearby);
        startNearby = fragmentView.findViewById(R.id.openNearbyButton);
        fragmentView.findViewById(R.id.NearbyConstraint).setVisibility(View.INVISIBLE);

        //Sets annotation buttons to invisible
        hideAnnotationButtons(getView());

        //Request carer button
        requestButton = fragmentView.findViewById(R.id.requestCarer);
        requestButton.setVisibility(View.GONE);

        //Disconnect Button
        disconnectButton = fragmentView.findViewById(R.id.disconnect);
        disconnectButton.setVisibility(View.GONE);

        //Shows buttons depending on what type of user
        db.collection("users").document(mFirebaseUser.getUid()).get().addOnCompleteListener(task  -> {
            if (!(boolean) ((task.getResult()).getData()).get("isCarer")) {
                hideAnnotationButtons(getView());
                requestButton.setVisibility(View.VISIBLE);
            }

            if (task.getResult().getData().get("connectedUser") != null) {
                if ((boolean) task.getResult().getData().get("isCarer")) {
                    annotateButton.setVisibility(View.VISIBLE);
                }
                disconnectButton.setVisibility(View.VISIBLE);
            }
        });

        // Button on click listeners
        annotateButton.setOnClickListener(this::annotateButtonClicked);
        undoButton.setOnClickListener(this::undoButtonClicked);
        cancelButton.setOnClickListener(this::cancelButtonClicked);
        clearButton.setOnClickListener(this::clearButtonClicked);
        sendButton.setOnClickListener(this::sendButtonClicked);
        requestButton.setOnClickListener(this::getCarer);
        disconnectButton.setOnClickListener(this::disconnectUser);

        //Nearby on click listeners
        restaurantButton.setOnClickListener(v  -> getNearby("restaurant"));
        cafeButton.setOnClickListener(v  -> getNearby("cafe"));
        taxiButton.setOnClickListener(v  -> getNearby("taxi_stand"));
        stationButton.setOnClickListener(v  -> getNearby("train_station"));
        atmButton.setOnClickListener(v  -> getNearby("atm"));
        hospitalButton.setOnClickListener(v  -> getNearby("hospital"));
        exitNearby.setOnClickListener(v  -> fragmentView.findViewById(R.id.NearbyConstraint).
                setVisibility(View.INVISIBLE));
        startNearby.setOnClickListener(v  -> fragmentView.findViewById(R.id.NearbyConstraint).
                setVisibility(View.VISIBLE));

        connectedUserMarker = null;
        connectedUserLocation = null;
        connectedUserName = null;

        // Register broadcast receivers
        LocalBroadcastManager.getInstance((this.getContext())).registerReceiver((mAnnotationReceiver),
                new IntentFilter("annotate")
        );
        LocalBroadcastManager.getInstance(this.getContext()).registerReceiver((mLocationReceiver),
                new IntentFilter("location")
        );
        LocalBroadcastManager.getInstance(this.getContext()).registerReceiver((mDisconnectReceiver),
                new IntentFilter("disconnect")
        );
        LocalBroadcastManager.getInstance(this.getContext()).registerReceiver((mConnectReceiver),
                new IntentFilter("connect")
        );
        LocalBroadcastManager.getInstance(this.getContext()).registerReceiver((mStyleReceiver),
                new IntentFilter("style")
        );
        LocalBroadcastManager.getInstance(this.getContext()).registerReceiver((mSOSReceiver),
                new IntentFilter("sos")
        );
        LocalBroadcastManager.getInstance(this.getContext()).registerReceiver((mOKReceiver),
                new IntentFilter("ok")
        );

        return fragmentView;
    }

    private void bindViews(View view) {
        mSwipeRefreshLayout = view.findViewById(R.id.swipe_refresh_layout);
        mRecyclerViewAllUserListing = view.findViewById(R.id.recycler_view_all_user_listing);
    }

    @Override
    public void onStop() {
        super.onStop();
        mapView.onStop();
        LocalBroadcastManager.getInstance((this.getContext())).unregisterReceiver(mAnnotationReceiver);
    }

    @Override
    public void onStart() {
        super.onStart();
        mapView.onStart();

        // Construct a GeoDataClient.
        mGeoDataClient = Places.getGeoDataClient((getActivity()), null);

        // Construct a PlaceDetectionClient.
        mPlaceDetectionClient = Places.getPlaceDetectionClient(getActivity(), null);

        // Construct a FusedLocationProviderClient.
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getActivity());
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        placeAutoComplete.onActivityResult(requestCode, resultCode, data);
    }

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


    public void RouteToFavouriteLocation() {
        Bundle extras = (getActivity()).getIntent().getExtras();
        if (extras == null || !extras.containsKey("favLat")) {
            Log.d("Map-Fav", "no extra key");
            return;
        }
        Double dLat = Double.parseDouble((getActivity().getIntent().getExtras()).getString("favLat"));
        Double dLng = Double.parseDouble(getActivity().getIntent().getExtras().getString("favLng"));
        String place_id = getActivity().getIntent().getExtras().getString("place_id").replaceAll(" ","" );
        mMap.clear();
        destPlace = new LatLng(dLat, dLng);

        final Task<PlaceBufferResponse> placeResponse = mGeoDataClient.getPlaceById(place_id);
        placeResponse.addOnCompleteListener(task  -> {
            if (task.isSuccessful()) {

                //dest Place obj is first one
                if ((task.getResult()).getCount() > 0)
                    dest = task.getResult().get(0);
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
        Bundle extras = (getActivity()).getIntent().getExtras();
        if (extras == null || !extras.containsKey("favWayPoints")) {
            Log.d("Map-Fav", "no favWayPoints key");
            return;
        }

        List<String> wayPointString = Arrays.asList(((getActivity().getIntent().getExtras()).getString("favWayPoints")).split(","));
        Log.d("wayyyyy", wayPointString.toString());

        ArrayList<LatLng> waypoints = new ArrayList<>();

        //convert pairs of string value into lat lng
        for (int i = 0; i < wayPointString.size() - 1; i += 2) {
            LatLng newLatLng = new LatLng(Double.parseDouble(wayPointString.get(i)), Double.parseDouble(wayPointString.get(i + 1)));
            waypoints.add(newLatLng);
        }

        Log.d("wayyyyy", waypoints.toString());

        destPlace = null;
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
                .title(((getActivity()).getIntent().getExtras()).getString("favTitle"))
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
        mapView.onResume();
        firstRefresh = false;
        //Ensure the GPS is ON and location permission enabled for the application.
        locationManager = (LocationManager) (getActivity()).getSystemService(LOCATION_SERVICE);
        if (ContextCompat.checkSelfPermission(getActivity().getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            try {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 5000, 0, this);
            } catch (Exception e) {
                Log.d("Map", e.toString());
                Toast.makeText(getActivity(), "ERROR: Cannot start location listener", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void onPause() {
        if (locationManager != null) {
            //Check needed in case of  API level 23.

            if (ContextCompat.checkSelfPermission((getActivity()), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_COARSE_LOCATION);
            }
            try {
                locationManager.removeUpdates((LocationListener) getActivity());
            } catch (Exception ignored) {
            }
        }
        locationManager = null;
        super.onPause();
        mapView.onPause();
    }


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
        filterMap();

        // Change the location of MYLocation button to bottom right location
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
                if (marker.getTag() == USER_TAG) {
                    return null;
                }
                // Inflate the layouts for the info window, title and snippet.
                View infoWindow = getLayoutInflater().inflate(R.layout.custom_info_contents,
                        (getView()).findViewById(R.id.map), false);

                TextView title = infoWindow.findViewById(R.id.title);
                title.setText(marker.getTitle());

                String snipData = marker.getSnippet().substring(1, marker.getSnippet().length() - 1);
                List<String> myList = new ArrayList<>(Arrays.asList(snipData.split(",")));

                if (marker.getSnippet() == null || myList.size() < 6) {
                    return infoWindow;
                }

                String ratingNum = myList.get(0);

                TextView snippet = infoWindow.findViewById(R.id.snippet);


                //snippet.setText("Rating: "+ratingNum+"/5.0 for "+dest.getAddress());
                snippet.setText(myList.get(3));


                RatingBar ratingbar = infoWindow.findViewById(R.id.ratingBar);
                ratingbar.setNumStars(5);
                ratingbar.setRating(Float.parseFloat(ratingNum));

                //Temporary location for addition of routes by clicking marker
                destPlace = marker.getPosition();
                getRoutingPath();
                //ratingbar.setRating(dest.getRating());

                return infoWindow;
            }
        });

        //save this place to firestore
        mMap.setOnInfoWindowClickListener(marker  -> {

            String snipData = marker.getSnippet().substring(1, marker.getSnippet().length() - 1);
            List<String> myList = new ArrayList<>(Arrays.asList(snipData.split(",")));

            if (marker.getSnippet() == null || myList.size() < 6) {
                return;
            }
            final String placeid = myList.get(2).replace(" ", "");//id of this place;
            Log.d("infowindow", myList.get(4) + "    " + myList.get(5) + " ");
            Log.d("Marker title: ", marker.getTitle());
            Log.d("snipArray: ", "place id for saving to fb is:" + placeid);
            FBFav fav = new FBFav(
                    placeid,
                    marker.getTitle(),
                    //destImage,
                    new GeoPoint(Double.parseDouble(myList.get(4)), Double.parseDouble(myList.get(5))),
                    myList.get(3),
                    1,
                    Timestamp.now().getSeconds()
            );

            final DocumentReference reference = FirebaseFirestore.getInstance().collection("users").document((FirebaseAuth.getInstance().getCurrentUser()).getUid());
            reference.collection("fav").document(placeid).get().
                    addOnCompleteListener(task0   -> {
                        Log.d("snipArray2: ", "place id for saving to fb is:" + placeid);
                        if (task0.isSuccessful()) {
                            if (!(task0.getResult()).exists()) {
                                Log.d("saveFav", "not there");
                                //only add to firebase if not exist

                                    reference.collection("fav")
                                            .document(placeid)
                                            .set(fav)
                                            .addOnCompleteListener(task2   -> {
                                                if (task2.isSuccessful()) {
                                                    Log.d("saveFav", "now added");
                                                }
                                            });


                            } else {
                                Log.d("saveFav", "already added");
                            }
                        }else{
                            Log.d("saveFav", "task failed");
                        }
                    });
        });

        // Prompt the user for permission.
        Permissions.getPermissions(getContext(), getActivity());

        // Turn on the My Location layer and the related control on the map.
        updateLocationUI();

        // Get the current location of the device and set the position of the map.
        getDeviceLocation();

        // On click listener for annotations
        mMap.setOnMapClickListener(arg0  -> {
            if (annotations.isAnnotating()) {
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
            if (Permissions.hasLocationPermission(getContext())) {
                Task<Location> locationResult = mFusedLocationProviderClient.getLastLocation();
                locationResult.addOnCompleteListener((getActivity()), task  -> {
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
                });
            }
        } catch (SecurityException e) {
            Log.e("Exception: %s", e.getMessage());
        }
    }

    /**
     * Prompts the user to select the current place from a list of likely places, and shows the
     * current place on the map - provided the user has granted location permission.
     */
    private void showCurrentPlace() {
        if (mMap == null) {
            return;
        }

        if (Permissions.hasLocationPermission(getContext())) {
            // Get the likely places - that is, the businesses and other points of interest that
            // are the best match for the device's current location.
            @SuppressWarnings("MissingPermission") final Task<PlaceLikelihoodBufferResponse> placeResult =
                    mPlaceDetectionClient.getCurrentPlace(null);
            placeResult.addOnCompleteListener
                    (task  -> {
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
            Permissions.getPermissions(getContext(), getActivity());
        }
    }

    /**
     * Displays a form allowing the user to select a place from a list of likely places.
     */
    private void openPlacesDialog() {
        // Ask the user to choose the place where they are now.
        DialogInterface.OnClickListener listener = (dialog, which)  -> {
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
        };


        // Display the dialog.
        new AlertDialog.Builder((getActivity()))
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
            mMap.addMarker(new MarkerOptions()
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
            if (Permissions.hasLocationPermission(getContext())) {
                mMap.setMyLocationEnabled(true);
                mMap.getUiSettings().setMyLocationButtonEnabled(true);
            } else {
                mMap.setMyLocationEnabled(false);
                mMap.getUiSettings().setMyLocationButtonEnabled(false);
                mLastKnownLocation = null;
                Permissions.getPermissions(getContext(), getActivity());
            }
        } catch (SecurityException e) {
            Log.e("Exception: %s", e.getMessage());
        }
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        if (marker != null) {
            Log.d("Marker: ", "Clicked");
            marker.showInfoWindow();
            destPlace = marker.getPosition();
            Log.d("Routing:", "Ready");
            getRoutingPath();
        }
        return true;
    }

    /**
     * Method to draw the google routed path.
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
     * Method to draw the google routed path that connects multiple waypoints.
     */
    private void getMultiRoutingPath(List<LatLng> wayPoints) {
        if (wayPoints == null)
            return;
        try {
            //insert current location into array
            LatLng myCurrentLocation = new LatLng(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude());
            wayPoints.add(0, myCurrentLocation);

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
            for (LatLng data : listPoints) {
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
        } catch (Exception ignored) {

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

        // Removes SOS tokens that are more than 1Km away from the user
        for (SOS mySos : sosList) {
            if (SphericalUtil.computeDistanceBetween(mySos.location, curLatLng) > 1000) {
                mySos.marker.remove();
                sosList.remove(mySos);
            }
        }

        if (firstRefresh && destMarker != null) {
            //Add Start Marker.
            firstRefresh = false;
            mMap.moveCamera(CameraUpdateFactory.newLatLng(curLatLng));
            getRoutingPath();
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
        requestCarer().addOnSuccessListener(s  -> Toast.makeText(getContext(), s, Toast.LENGTH_SHORT).show());
    }

    public void disconnectUser(View v) {
        Toast.makeText(getContext(), "Disconnecting from User", Toast.LENGTH_SHORT).show();
        clearButtonClicked(getView());
        disconnect().addOnSuccessListener(s  -> {
            Toast.makeText(getContext(), s, Toast.LENGTH_SHORT).show();
            disconnectButton.setVisibility(View.GONE);
            connectedUserMarker.remove();
            connectedUserMarker = null;
            db.collection("users").document(mFirebaseUser.getUid()).get().addOnCompleteListener(task  -> {
                if (!(boolean) ((task.getResult()).getData()).get("isCarer")) {
                    requestButton.setVisibility(View.VISIBLE);
                } else {
                    hideAnnotationButtons(v);
                }
            });
        });
    }

    //Hide buttons related to annotations
    private void hideAnnotationButtons(View v) {
        annotateButton.setVisibility(View.GONE);
        undoButton.setVisibility(View.GONE);
        cancelButton.setVisibility(View.GONE);
        clearButton.setVisibility(View.GONE);
        sendButton.setVisibility(View.GONE);
    }


    //ANNOTATION BUTTONS

    private void annotateButtonClicked(View v) {
        annotateButton.setVisibility(View.GONE);
        undoButton.setVisibility(View.VISIBLE);
        cancelButton.setVisibility(View.VISIBLE);
        clearButton.setVisibility(View.VISIBLE);
        sendButton.setVisibility(View.VISIBLE);
        annotations.setAnnotating(true);
    }

    private void cancelButtonClicked(View v) {
        hideAnnotationButtons(v);
        annotateButton.setVisibility(View.VISIBLE);
        annotations.setAnnotating(false);
    }

    private void undoButtonClicked(View v) {
        annotations.undo();
    }

    private void clearButtonClicked(View v) {
        annotations.clear();
        sendClearedAnnotations();

        //failsafe incase user was not connected when clear was sent
        annotations.setUndoHasOccurred(true);
    }

    public void sendButtonClicked(View v) {
        Toast.makeText(getContext(), "Sending annotation", Toast.LENGTH_SHORT).show();
        if (annotations.hasUndoOccurred()) {
            sendClearedAnnotations().addOnSuccessListener(s  -> sendAllAnnotations()).addOnFailureListener(f  -> Log.d("send button", "failure"));
        } else {
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
                .continueWith(task  -> (String) (task.getResult()).getData());
    }

    private void filterMap() {
        if (mMap != null) {
            try {
                //Attempt to open the file from device storage
                FileInputStream stream = (getActivity()).getApplicationContext().openFileInput("toggleMap");
                if (stream != null) {
                    Log.d("Stream: ", "not null");
                    //Read contents of file
                    BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
                    StringBuilder totalContent = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        Log.d("Line: ", line);
                        totalContent.append(line).append('\n');
                    }
                    //Pass JSON style string to maps style to hide components
                    MapStyleOptions style = new MapStyleOptions(totalContent.toString());
                    mMap.setMapStyle(style);
                }
            } catch (Resources.NotFoundException e) {
                Log.e(TAG, "Can't find style. Error: ", e);
            } catch (FileNotFoundException e) {
                Log.e(TAG, "File not found", e);
            } catch (IOException e) {
                Log.e(TAG, "File reading error", e);
            }
        }
    }


    /**
     * Create a URL for a request to the Google Places API
     *
     * @param latitude   LatLng describing location of the user
     * @param longitude  Radial distance to confine search
     * @param nearbyType Descriptor for kind of desired location
     * @return
     */
    private String buildUrl(double latitude, double longitude, String nearbyType) {
        Log.d("url: ", "Building");
        StringBuilder placeUrl = new StringBuilder("https://maps.googleapis.com/maps/api/place/nearbysearch/json?");
        placeUrl.append("location=").append(latitude).append(",").append(longitude);
        placeUrl.append("&radius=" + RADIUS);
        placeUrl.append("&type=").append(nearbyType);
        placeUrl.append("&key=AIzaSyCJJY5Qwt0Adki43NdMHWh9O88VR-dEByI");

        System.out.println(placeUrl.toString());

        return placeUrl.toString();
    }

    private Task<String> disconnect() {
        return mFunctions
                .getHttpsCallable("disconnect")
                .call()
                .continueWith(task  -> (String) (task.getResult()).getData());
    }

    //Send all annotations on carer's map to connected user
    private void sendAllAnnotations() {
        ArrayList<ArrayList<GeoPoint>> points = annotations.getAnnotations();
        if (points.size() > 0) {
            for (ArrayList<GeoPoint> p : points) {
                if (p.size() > 0)
                    sendAnnotation(p)
                            .addOnFailureListener(f  -> Log.d("send button", "sent annotation failed"))
                            //removes p from list of points to send next time
                            .addOnSuccessListener(f  -> annotations.successfulSend(p));
            }

        }
        //send empty list
        else {
            sendAnnotation(new ArrayList<>())
                    .addOnFailureListener(f  -> Log.d("send", "failure"));
        }
    }

    //Sends an individual annotation (or polyline) to the connected user
    private Task<String> sendAnnotation(ArrayList<GeoPoint> points) {
        Map<String, Object> newRequest = new HashMap<>();
        StringBuilder annotationToString = new StringBuilder(" ");

        //encode arraylist as string
        for (GeoPoint g : points) {
            annotationToString.append(Double.toString(g.getLatitude())).append(LAT_LNG_SEPERATOR);
            annotationToString.append(Double.toString(g.getLongitude())).append(POINT_SEPERATOR);
        }

        //call cloud function and send encoded points to connected user
        newRequest.put("points", annotationToString.toString());
        return mFunctions
                .getHttpsCallable("sendAnnotation")
                .call(newRequest)
                .continueWith(task  -> (String) Objects.requireNonNull(task.getResult()).getData());
    }

    //Clears the connected users map of all annotations
    private Task<String> sendClearedAnnotations() {
        //sends coded clear character to connected user
        Map<String, Object> newRequest = new HashMap<>();
        newRequest.put("points", CLEAR_CHARACTER);
        return mFunctions
                .getHttpsCallable("sendAnnotation")
                .call(newRequest)
                .continueWith(task  -> (String) Objects.requireNonNull(task.getResult()).getData());
    }


    /**
     * Get all nearby places of given TYPE within a certain RADIUS from a certain LOCATION as
     * specified in the buildUrl method
     * Executes asynchronous function
     *
     * @param type Google Places definition for kind of location
     */
    public void getNearby(String type) {
        mMap.clear();
        fragmentView.findViewById(R.id.NearbyConstraint).setVisibility(View.INVISIBLE);
        String Url = buildUrl(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude(), type);
        Object dataTransfer[] = new Object[2];
        dataTransfer[0] = mMap;
        dataTransfer[1] = Url;
        getNearbyPlaces getNearbyPlaces = new getNearbyPlaces();
        getNearbyPlaces.execute(dataTransfer);
    }
}