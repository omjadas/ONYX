package com.example.onyx.onyx.core.registration;

import android.app.Activity;

import com.google.firebase.auth.FirebaseUser;


public class RegisterPresenter implements RegisterInterface.Presenter, RegisterInterface.OnRegistrationListener {
    private RegisterInterface.View mRegisterView;
    private RegisterInteractor mRegisterInteractor;

    public RegisterPresenter(RegisterInterface.View registerView) {
        this.mRegisterView = registerView;
        mRegisterInteractor = new RegisterInteractor(this);
    }

    @Override
    public void register(Activity activity, String email, String password) {
        mRegisterInteractor.performFirebaseRegistration(activity, email, password);
    }

    @Override
    public void onSuccess(FirebaseUser firebaseUser) {
        mRegisterView.onRegistrationSuccess(firebaseUser);
    }

    @Override
    public void onFailure(String message) {
        mRegisterView.onRegistrationFailure(message);
    }
}
