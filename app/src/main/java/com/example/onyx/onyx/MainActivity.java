package com.example.onyx.onyx;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.example.onyx.onyx.ui.fragments.UsersFragment;
import com.example.onyx.onyx.ui.fragments.toggleFragment;
import com.example.onyx.onyx.utils.Constants;
import com.example.onyx.onyx.videochat.activity.CallFragment;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.iid.FirebaseInstanceId;
import com.roughike.bottombar.BottomBar;

import java.util.ArrayList;
import java.util.Objects;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, GoogleApiClient.OnConnectionFailedListener {

    public static final String ANONYMOUS = "anonymous";
    private static final String TAG = "MainActivity";
    public FrameLayout frameLayout;
    private String mUsername;
    private String mPhotoUrl;
    private SharedPreferences mSharedPreferences;
    private GoogleApiClient mGoogleApiClient;

    // Firebase instance variables
    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;
    // Firebase instance variables

    private FirebaseFunctions mFunctions;
    private final BroadcastReceiver mFallReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            sosRequest();
        }
    };
    private Intent locationService;
    private Intent fallService;
    private Fragment oldFragment;
    private FirebaseFirestore db;
    private boolean sosVisible;
    private boolean okVisible;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Permissions.getPermissions(this.getApplicationContext(), this);

        // Set default username is anonymous.
        mUsername = ANONYMOUS;
        // Initialize Firebase Auth
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();
        if (mFirebaseUser == null) {
            // Not signed in, launch the Sign In activity
            startActivity(new Intent(this, SignInActivity.class));
            finish();
            return;
        } else {
            mUsername = mFirebaseUser.getDisplayName();
            if (mFirebaseUser.getPhotoUrl() != null) {
                mPhotoUrl = mFirebaseUser.getPhotoUrl().toString();
            }
        }

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        mFirebaseAuth = FirebaseAuth.getInstance();

        db = FirebaseFirestore.getInstance();
        db.collection("users").document((mFirebaseAuth.getCurrentUser()).getUid()).update("isOnline", true);

        db.collection("users").document(mFirebaseUser.getUid()).get().addOnCompleteListener(task -> {
            if ((boolean) ((task.getResult()).getData()).get("isCarer")) {
                sosVisible = false;
                okVisible = false;
            } else {
                sosVisible = true;
                okVisible = false;
            }
            invalidateOptionsMenu();
        });

        // Register broadcast receivers
        LocalBroadcastManager.getInstance(this).registerReceiver((mFallReceiver),
                new IntentFilter("fall")
        );

        setContentView(R.layout.main_activity);
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        // Services
        locationService = new Intent(this, LocationService.class);
        fallService = new Intent(this, FallService.class);

        mFunctions = FirebaseFunctions.getInstance();

        //toolbar setup
        frameLayout = findViewById(R.id.framelayout);
        BottomBar bottomBar = findViewById(R.id.bottombar);
        for (int i = 0; i < bottomBar.getTabCount(); i++) {
            bottomBar.getTabAtPosition(i).setGravity(Gravity.CENTER_VERTICAL);
        }
        bottomBar.setOnTabSelectListener(this::fragChange);
    }

    public void fragChange(int tabId) {
        FragmentManager fragmentManager = getSupportFragmentManager();

        switch (tabId) {
            case R.id.toolmap:
                if (fragmentManager.findFragmentByTag("maps_fragment") != null) {
                    //if the fragment exists, show it.
                    Log.d("dddddd", "map already there");
                    fragmentManager.beginTransaction().show((fragmentManager.findFragmentByTag("maps_fragment"))).commit();
                } else {
                    Log.d("dddddd", "map frag not null, adding it ");
                    //if the fragment does not exist, add it to fragment manager.
                    add_fragment(MapsFragment.newInstance(MapsFragment.TYPE_ALL), "maps_fragment");
                    //fragmentManager.beginTransaction().add(R.id.container, MapsFragment.newInstance(MapsFragment.TYPE_ALL), "maps_fragment").commit();
                }
                if (fragmentManager.findFragmentByTag("fav_fragment") != null) {
                    //if the other fragment is visible, hide it.
                    fragmentManager.beginTransaction().hide((fragmentManager.findFragmentByTag("fav_fragment"))).commit();
                }

                find_and_hide_fragment("chat_fragment");
                find_and_hide_fragment("setting_fragment");
                find_and_hide_fragment("call_fragment");

                break;
            case R.id.toolfavs:
                if (fragmentManager.findFragmentByTag("fav_fragment") != null) {
                    //if the fragment exists, show it.
                    fragmentManager.beginTransaction().show((fragmentManager.findFragmentByTag("fav_fragment"))).commit();
                } else {
                    //if the fragment does not exist, add it to fragment manager.
                    //fragmentManager.beginTransaction().add(R.id.container,new FavouriteFragment(), "fav_fragment").commit();
                    add_fragment(new FavouriteFragment(), "fav_fragment");
                }
                if (fragmentManager.findFragmentByTag("maps_fragment") != null) {
                    //if the other fragment is visible, hide it.
                    fragmentManager.beginTransaction().hide((fragmentManager.findFragmentByTag("maps_fragment"))).commit();
                }
                find_and_hide_fragment("chat_fragment");
                find_and_hide_fragment("setting_fragment");
                find_and_hide_fragment("call_fragment");
                break;
            case R.id.toolcontact:
                if (fragmentManager.findFragmentByTag("chat_fragment") != null) {
                    //if the fragment exists, show it.
                    fragmentManager.beginTransaction().show((fragmentManager.findFragmentByTag("chat_fragment"))).commit();
                } else {
                    //if the fragment does not exist, add it to fragment manager.
                    //fragmentManager.beginTransaction().add(R.id.container,new FavouriteFragment(), "fav_fragment").commit();
                    add_fragment(UsersFragment.newInstance(UsersFragment.TYPE_ALL), "chat_fragment");
                }
                find_and_hide_fragment("maps_fragment");
                find_and_hide_fragment("fav_fragment");
                find_and_hide_fragment("setting_fragment");
                find_and_hide_fragment("call_fragment");
                break;
            case R.id.toolcall:
                if (fragmentManager.findFragmentByTag("call_fragment") != null) {
                    //if the fragment exists, show it.
                    fragmentManager.beginTransaction().show((fragmentManager.findFragmentByTag("call_fragment"))).commit();
                } else {
                    //if the fragment does not exist, add it to fragment manager.
                    //fragmentManager.beginTransaction().add(R.id.container,new FavouriteFragment(), "fav_fragment").commit();
                    add_fragment(CallFragment.newInstance(CallFragment.TYPE_ALL), "call_fragment");
                }
                find_and_hide_fragment("maps_fragment");
                find_and_hide_fragment("fav_fragment");
                find_and_hide_fragment("setting_fragment");
                find_and_hide_fragment("chat_fragment");
                break;
            case R.id.setting:
                if (fragmentManager.findFragmentByTag("setting_fragment") != null) {
                    //if the fragment exists, show it.
                    fragmentManager.beginTransaction().show((fragmentManager.findFragmentByTag("setting_fragment"))).commit();
                } else {
                    //if the fragment does not exist, add it to fragment manager.
                    //fragmentManager.beginTransaction().add(R.id.container,new FavouriteFragment(), "fav_fragment").commit();
                    add_fragment(toggleFragment.newInstance(toggleFragment.TYPE_ALL), "setting_fragment");
                }
                find_and_hide_fragment("maps_fragment");
                find_and_hide_fragment("fav_fragment");
                find_and_hide_fragment("call_fragment");
                find_and_hide_fragment("chat_fragment");
                break;
        }
    }


    public void find_and_hide_fragment(String tag) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        if (fragmentManager.findFragmentByTag(tag) != null) {
            //if the other fragment is visible, hide it.
            fragmentManager.beginTransaction().hide((fragmentManager.findFragmentByTag(tag))).commit();
        }

    }

    public void add_fragment(Fragment fragment, String tag) {

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.add(R.id.framelayout, fragment, tag);
        transaction.commit();
    }


    @Override
    public void onStart() {
        super.onStart();
        saveTokenToServer();
        startService(locationService);
        db.collection("users").document(mFirebaseUser.getUid()).get().addOnCompleteListener(task -> {
            if (!(boolean) ((task.getResult()).getData()).get("isCarer")) {
                startService(fallService);
            }
        });
    }

    private void saveTokenToServer() {
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            FirebaseInstanceId.getInstance().getInstanceId().addOnSuccessListener(instanceIdResult -> FirebaseFirestore.getInstance().collection("users")
                    .document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                    .update(Constants.ARG_FIREBASE_TOKEN, instanceIdResult.getToken())
                    .addOnSuccessListener(aVoid -> Log.d("token update done", "yep"))
                    .addOnFailureListener(e -> Log.d("TOKEN F", "nope")));
        }
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (locationService != null) {
            stopService(locationService);
            locationService = null;
        }
        if (fallService != null) {
            stopService(fallService);
            fallService = null;
        }

        LocalBroadcastManager.getInstance(this).unregisterReceiver(mFallReceiver);

        if (mFirebaseAuth.getCurrentUser() != null) {
            final DocumentReference reference = FirebaseFirestore.getInstance().collection("users").document((FirebaseAuth.getInstance().getCurrentUser()).getUid());
            //check if user is in database
            reference.get().
                    addOnCompleteListener(task0 -> {
                        //check task
                        if (task0.isSuccessful()) {
                            //check if document exists
                            if ((task0.getResult()).exists()) {
                                reference.update("isOnline", false);
                            }  //user does not exist
                        }
                    });
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);

        menu.findItem(R.id.sos_2).setVisible(false);
        menu.findItem(R.id.ok).setVisible(false);

        if (okVisible) {
            menu.findItem(R.id.ok).setVisible(true);
        } else {
            menu.findItem(R.id.ok).setVisible(false);
        }

        if (sosVisible) {
            menu.findItem(R.id.sos_2).setVisible(true);
        } else {
            menu.findItem(R.id.sos_2).setVisible(false);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.sign_out_menu:
                stopService(locationService);
                locationService = null;
                mFirebaseAuth.signOut();
                Auth.GoogleSignInApi.signOut(mGoogleApiClient);
                mUsername = ANONYMOUS;
                startActivity(new Intent(this, SignInActivity.class));
                finish();
                return true;
            case R.id.sos_2:
                // send SOS
                sosRequest();
                sosVisible = false;
                okVisible = true;
                invalidateOptionsMenu();
                return true;
            case R.id.ok:
                okRequest();
                sosVisible = true;
                okVisible = false;
                invalidateOptionsMenu();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    private Task<String> sendSOS() {
        return mFunctions
                .getHttpsCallable("sendSOS")
                .call()
                .continueWith(task -> (String) (task.getResult()).getData());
    }

    public void sosRequest() {
        Toast.makeText(this, "Sending SOS", Toast.LENGTH_SHORT).show();
        sendSOS().addOnSuccessListener(s -> Toast.makeText(this, s, Toast.LENGTH_SHORT).show());
    }

    private Task<String> sendOK() {
        return mFunctions
                .getHttpsCallable("sendOK")
                .call()
                .continueWith(task -> (String) (task.getResult()).getData());
    }

    public void okRequest() {
        Toast.makeText(this, "Notifying carers", Toast.LENGTH_SHORT).show();
        sendOK().addOnSuccessListener(s -> Toast.makeText(this, s, Toast.LENGTH_SHORT).show());
    }

    @Override
    public void onClick(View v) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        // An unresolvable error has occurred and Google APIs (including Sign-In) will not
        // be available.
        Log.d(TAG, "onConnectionFailed:" + connectionResult);
        Toast.makeText(this, "Google Play Services error.", Toast.LENGTH_SHORT).show();
    }

    public void FavStartMap(String lat, String lng, String favTitle, String place_id) {
        getIntent().putExtra("favTitle", favTitle);
        getIntent().putExtra("favLat", lat);
        getIntent().putExtra("favLng", lng);
        getIntent().putExtra("place_id", place_id);
        //replace_fragment( MapsFragment.newInstance(MapsFragment.TYPE_ALL) );

        //set tab to maps
        BottomBar bottomBar = findViewById(R.id.bottombar);
        bottomBar.selectTabAtPosition(0);
        FragmentManager fragmentManager = getSupportFragmentManager();
        if (fragmentManager.findFragmentByTag("maps_fragment") != null) {
            MapsFragment frag = (MapsFragment) fragmentManager.findFragmentByTag("maps_fragment");
            (frag).RouteToFavouriteLocation();
        }
    }


    //fav fragment calls this and main will open map and route to place[s]
    public void FavStartMapRoute(ArrayList<LatLng> wayPointStrings) {
        //save waypoints to extras and remove first and last []

        ArrayList<Object> pointString = new ArrayList<>();

        for (LatLng pt : wayPointStrings) {
            pointString.add(pt.latitude);
            pointString.add(pt.longitude);
        }

        Log.d("wayyy pointString", pointString.toString());
        getIntent().putExtra("favWayPoints", pointString.toString().substring(1, pointString.toString().length() - 1));

        Log.d("wayyy pointString", pointString.toString().substring(1, pointString.toString().length() - 1));

        //set tab to maps
        BottomBar bottomBar = findViewById(R.id.bottombar);
        bottomBar.selectTabAtPosition(0);
        FragmentManager fragmentManager = getSupportFragmentManager();
        if (fragmentManager.findFragmentByTag("maps_fragment") != null) {

            MapsFragment frag = (MapsFragment) fragmentManager.findFragmentByTag("maps_fragment");
            (frag).RouteToFavouriteRoute();
        }
    }
}