package com.example.onyx.onyx.core.login;

import android.app.Activity;


public class LoginPresenter implements LoginInterface.Presenter, LoginInterface.OnLoginListener {
    private LoginInterface.View mLoginView;
    private LoginInteractor mLoginInteractor;

    public LoginPresenter(LoginInterface.View loginView) {
        this.mLoginView = loginView;
        mLoginInteractor = new LoginInteractor(this);
    }

    @Override
    public void login(Activity activity, String email, String password) {
        mLoginInteractor.performFirebaseLogin(activity, email, password);
    }

    @Override
    public void onSuccess(String message) {
        mLoginView.onLoginSuccess(message);
    }

    @Override
    public void onFailure(String message) {
        mLoginView.onLoginFailure(message);
    }
}
