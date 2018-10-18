package com.example.onyx.onyx.ui.fragments;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
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
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class UsersFragment extends Fragment implements GetUsersInterface.View, ItemClickSupport.OnItemClickListener, SwipeRefreshLayout.OnRefreshListener {
    public static final String ARG_TYPE = "type";
    public static final String TYPE_CHATS = "type_chats";
    public static final String TYPE_ALL = "type_all";

    private SwipeRefreshLayout mSwipeRefreshLayout;
    private RecyclerView mRecyclerViewAllUserListing;

    private UserListingRecyclerAdapter mUserListingRecyclerAdapter;

    private GetUsersPresenter mGetUsersPresenter;

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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d("Result: ","called");
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode,resultCode,data);
        if(result != null){
            if(result.getContents() != null){
                Log.d("Results: ","Exist");
                addContact(result.getContents());
                closeAddContactUI();
            }
            else{
                Log.d("Results: ","Null");
            }
        }
        else{
            Log.d("Results: ","Not exist");
            super.onActivityResult(requestCode,resultCode,data);
        }
    }

    private void init() {
        mGetUsersPresenter = new GetUsersPresenter(this);
        getUsers();
        mSwipeRefreshLayout.post(() -> mSwipeRefreshLayout.setRefreshing(true));

        ItemClickSupport.addTo(mRecyclerViewAllUserListing)
                .setOnItemClickListener(this);

        mSwipeRefreshLayout.setOnRefreshListener(this);
        addContactByEmail = (getView()).findViewById(R.id.addByEmailButton);
        addContact = (getView()).findViewById(R.id.addContactsButton);
        cancelAddContact = (getView()).findViewById(R.id.closeAddContactButton);
        showQR = (getView()).findViewById(R.id.showQRButton);
        openScanner = (getView()).findViewById(R.id.scanButton);
        imageQR = (getView()).findViewById(R.id.containerQR);

        addContact.setOnClickListener(view -> {
            openAddContactUI();
        });
        cancelAddContact.setOnClickListener(view -> {
            closeAddContactUI();
        });
        addContactByEmail.setOnClickListener(view -> {
            showEmailDialog();
        });
        showQR.setOnClickListener(view -> {
            showQR();
        });
        openScanner.setOnClickListener(view -> {
            doScan();
        });
    }

    public void showEmailDialog() {
        LayoutInflater li = LayoutInflater.from(getContext());
        View dialogView = li.inflate(R.layout.fragment_new_contact, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setView(dialogView);
        final EditText searchEmail = dialogView.findViewById(R.id.emailSearch);
        builder.setTitle("Add New Contact")
                .setMessage("Enter email of new contact")
                .setPositiveButton("Add", (dialogInterface, i) -> addContact(searchEmail.getText().toString()).addOnSuccessListener(s -> {
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

    public void openAddContactUI() {
        addContact.hide();
        addContactByEmail.show();
        cancelAddContact.show();
        showQR.show();
        openScanner.show();
    }

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

    private Task<String> addContact(String email) {
        Log.d("EMAIL", email);
        Map<String, Object> newRequest = new HashMap<>();
        newRequest.put("email", email);
        return mFunctions
                .getHttpsCallable("addContact")
                .call(newRequest)
                .continueWith(task -> (String) (task.getResult()).getData());
    }

    public void showQR(){
        FirebaseFirestore.getInstance().collection("users").document(FirebaseAuth.getInstance().getUid()).get().addOnSuccessListener(documentSnapshot -> {
           Bitmap bitmap = StringToBitmap(documentSnapshot.getData().get("QR").toString());
           imageQR.setImageBitmap(bitmap);
           mSwipeRefreshLayout.setVisibility(View.INVISIBLE);
           imageQR.setVisibility(View.VISIBLE);
        });
    }

    public Bitmap StringToBitmap(String encodedStr){
        byte[] encodedBytes = Base64.decode(encodedStr,Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(encodedBytes,0,encodedBytes.length);
    }

    public void doScan() {
        IntentIntegrator intentIntegrator = new IntentIntegrator(getActivity());
        intentIntegrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE_TYPES);
        intentIntegrator.setCameraId(0);
        intentIntegrator.setBeepEnabled(false);
        intentIntegrator.setBarcodeImageEnabled(false);
        intentIntegrator.forSupportFragment(UsersFragment.this).initiateScan();
    }
}
