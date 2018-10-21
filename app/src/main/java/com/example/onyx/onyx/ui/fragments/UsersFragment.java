package com.example.onyx.onyx.ui.fragments;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.onyx.onyx.R;
import com.example.onyx.onyx.core.users.get.all.GetUsersInterface;
import com.example.onyx.onyx.core.users.get.all.GetUsersPresenter;
import com.example.onyx.onyx.models.User;
import com.example.onyx.onyx.ui.activities.ChatActivity;
import com.example.onyx.onyx.ui.adapters.UserListingRecyclerAdapter;
import com.example.onyx.onyx.utils.ItemClickSupport;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
    Fragment that represents contacts on the user device
    Displayes contacts in a recyclerview
    Allows addition of contacts via email or QR code
    QR code management handled with zxing library
 */
public class UsersFragment extends Fragment implements GetUsersInterface.View, ItemClickSupport.OnItemClickListener, SwipeRefreshLayout.OnRefreshListener {
    public static final String ARG_TYPE = "type";
    public static final String TYPE_CHATS = "type_chats";
    public static final String TYPE_ALL = "type_all";

    private SwipeRefreshLayout mSwipeRefreshLayout;
    private RecyclerView mRecyclerViewAllUserListing;

    private UserListingRecyclerAdapter mUserListingRecyclerAdapter;

    private GetUsersPresenter mGetUsersPresenter;

    //Buttons for interface of contact addition
    private FloatingActionButton addContact;
    private FloatingActionButton addContactByEmail;
    private FloatingActionButton cancelAddContact;
    private FloatingActionButton openScanner;
    private FloatingActionButton showQR;
    private ImageView imageQR;

    private FirebaseFunctions mFunctions;

    public static UsersFragment newInstance(String type) {
        Bundle args = new Bundle();
        args.putString(ARG_TYPE, type);
        UsersFragment fragment = new UsersFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View fragmentView = inflater.inflate(R.layout.fragment_users, container, false);
        bindViews(fragmentView);
        mFunctions = FirebaseFunctions.getInstance();
        return fragmentView;
    }

    private void bindViews(View view) {
        mSwipeRefreshLayout = view.findViewById(R.id.swipe_refresh_layout);
        mRecyclerViewAllUserListing = view.findViewById(R.id.recycler_view_all_user_listing);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        init();
    }
    /*
        Initialise fragment
     */
    private void init() {
        mGetUsersPresenter = new GetUsersPresenter(this);
        getUsers();
        mSwipeRefreshLayout.post(() -> mSwipeRefreshLayout.setRefreshing(true));

        ItemClickSupport.addTo(mRecyclerViewAllUserListing)
                .setOnItemClickListener(this);

        mSwipeRefreshLayout.setOnRefreshListener(this);

        //Initialise contact addition interface
        addContactByEmail = (getView()).findViewById(R.id.addByEmailButton);
        addContact = (getView()).findViewById(R.id.addContactsButton);
        cancelAddContact = (getView()).findViewById(R.id.closeAddContactButton);
        showQR = (getView()).findViewById(R.id.showQRButton);
        openScanner = (getView()).findViewById(R.id.scanButton);
        imageQR = (getView()).findViewById(R.id.containerQR);

        //Set functionality for contact addition interface
        addContact.setOnClickListener(view -> openAddContactUI());
        cancelAddContact.setOnClickListener(view -> closeAddContactUI());
        addContactByEmail.setOnClickListener(view -> showEmailDialog());
        showQR.setOnClickListener(view -> showQR());
        openScanner.setOnClickListener(view -> doScan());
    }

    //Display UI for addition of contacts
    public void openAddContactUI() {
        addContact.hide();
        cancelAddContact.show();
        addContactByEmail.show();
        showQR.show();
        openScanner.show();
    }

    //Remove UI for addition of contacts
    public void closeAddContactUI() {
        addContact.show();
        addContactByEmail.hide();
        cancelAddContact.hide();
        showQR.hide();
        openScanner.hide();
        if(mSwipeRefreshLayout.getVisibility() == View.INVISIBLE){
            mSwipeRefreshLayout.setVisibility(View.VISIBLE);
        }
        if(imageQR.getVisibility() == View.VISIBLE){
            imageQR.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void onRefresh() {
        getUsers();
    }

    private void getUsers() {
        if (TextUtils.equals((getArguments()).getString(ARG_TYPE), TYPE_CHATS)) {

        } else if (TextUtils.equals(getArguments().getString(ARG_TYPE), TYPE_ALL)) {
            mGetUsersPresenter.getAllUsers();
        }
    }

    @Override
    public void onItemClicked(RecyclerView recyclerView, int position, View v) {
        ChatActivity.startActivity(getActivity(),
                mUserListingRecyclerAdapter.getUser(position).email,
                mUserListingRecyclerAdapter.getUser(position).uid);
    }

    @Override
    public void onGetAllUsersSuccess(List<User> users) {
        if (users == null || users.size() < 1) {
            Toast.makeText(this.getActivity(), "No Contacts! Please Add Some.", Toast.LENGTH_LONG).show();
        }
        mSwipeRefreshLayout.post(() -> mSwipeRefreshLayout.setRefreshing(false));
        mUserListingRecyclerAdapter = new UserListingRecyclerAdapter(users);
        mRecyclerViewAllUserListing.setAdapter(mUserListingRecyclerAdapter);
        mUserListingRecyclerAdapter.notifyDataSetChanged();
    }

    @Override
    public void onGetAllUsersFailure(String message) {
        mSwipeRefreshLayout.post(() -> mSwipeRefreshLayout.setRefreshing(false));
        Toast.makeText(getActivity(), "Error: " + message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onGetChatUsersSuccess(List<User> users) {

    }

    @Override
    public void onGetChatUsersFailure(String message) {

    }

    /**
     * Send cloud function for addition of contact
     * @param email
     * @return
     */
    private Task<String> addContact(String email) {
        Map<String, Object> newRequest = new HashMap<>();
        newRequest.put("email", email);
        return mFunctions
                .getHttpsCallable("addContact")
                .call(newRequest)
                .continueWith(task -> (String) (task.getResult()).getData());
    }

    //Generate QR code from user email and draw to screen
    public void showQR(){
        MultiFormatWriter multiFormatWriter = new MultiFormatWriter();
        //Get user data from firestore
        FirebaseFirestore.getInstance().collection("users")
                .document(FirebaseAuth.getInstance().getUid())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
            try{
                //Make QR code from user email
                BitMatrix bitMatrix = multiFormatWriter.encode(documentSnapshot.getData().get("email").toString(), BarcodeFormat.QR_CODE,800,800);
                BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
                Bitmap bitmap = barcodeEncoder.createBitmap(bitMatrix);
                //Draw to screen
                imageQR.setImageBitmap(bitmap);
            }
            catch (WriterException e) {
                e.printStackTrace();
            }
            mSwipeRefreshLayout.setVisibility(View.INVISIBLE);
            imageQR.setVisibility(View.VISIBLE);
        });
    }

    //Start camera for scanning a QR code
    public void doScan() {
        IntentIntegrator intentIntegrator = IntentIntegrator.forSupportFragment(UsersFragment.this);
        intentIntegrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE_TYPES);
        intentIntegrator.setPrompt("");
        intentIntegrator.setOrientationLocked(false);
        intentIntegrator.setCameraId(0);
        intentIntegrator.setBeepEnabled(false);
        intentIntegrator.setBarcodeImageEnabled(false);
        intentIntegrator.initiateScan();
    }

    //Overridden to receive data after a QR scan
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        //Get results of QR scan
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode,resultCode,data);
        if(result != null){
            if(result.getContents() != null){
                //If we have data, used it to add a contact
                addContact(result.getContents());
                closeAddContactUI();
            }
        }
        else{
            //Result is null, start default behaviour
            super.onActivityResult(requestCode,resultCode,data);
        }
    }

    //Dialog for addition of contacts by email
    public void showEmailDialog() {
        LayoutInflater li = LayoutInflater.from(getContext());

        //Get textview from layout
        View dialogView = li.inflate(R.layout.fragment_new_contact, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setView(dialogView);
        final EditText searchEmail = dialogView.findViewById(R.id.emailSearch);

        //Initialise dialog
        builder.setTitle("Add New Contact")
                .setMessage("Enter email of new contact")
                .setPositiveButton("Add", (dialogInterface, i) ->
                        addContact(searchEmail.getText().toString()).addOnSuccessListener(s -> {
                            mGetUsersPresenter.getAllUsers();
                            Toast.makeText(getContext(), s, Toast.LENGTH_SHORT).show();
                        }))
                .setNegativeButton("Cancel", (dialogInterface, i) -> {
                    dialogInterface.cancel();
                    closeAddContactUI();
                });
        AlertDialog newContactRequest = builder.create();
        newContactRequest.show();
    }
}
