package com.example.onyx.onyx.core.users.add;

import android.content.Context;

import com.google.firebase.auth.FirebaseUser;


public class AddUserPresenter implements AddUserInterface.Presenter, AddUserInterface.OnUserDatabaseListener {
    private AddUserInterface.View mView;
    private AddUserInteractor mAddUserInteractor;

    public AddUserPresenter(AddUserInterface.View view) {
        this.mView = view;
        mAddUserInteractor = new AddUserInteractor(this);
    }

    @Override
    public void addUser(Context context, FirebaseUser firebaseUser) {
        mAddUserInteractor.addUserToDatabase(context, firebaseUser);
    }

    @Override
    public void onSuccess(String message) {
        mView.onAddUserSuccess(message);
    }

    @Override
    public void onFailure(String message) {
        mView.onAddUserFailure(message);
    }
}
