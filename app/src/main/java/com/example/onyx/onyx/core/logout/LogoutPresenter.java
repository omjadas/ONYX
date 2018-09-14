package com.example.onyx.onyx.core.logout;

public class LogoutPresenter implements LogoutInterface.Presenter, LogoutInterface.OnLogoutListener {
    private LogoutInterface.View mLogoutView;
    private LogoutInteractor mLogoutInteractor;

    public LogoutPresenter(LogoutInterface.View logoutView) {
        mLogoutView = logoutView;
        mLogoutInteractor = new LogoutInteractor(this);
    }

    @Override
    public void logout() {
        mLogoutInteractor.performFirebaseLogout();
    }

    @Override
    public void onSuccess(String message) {
        mLogoutView.onLogoutSuccess(message);
    }

    @Override
    public void onFailure(String message) {
        mLogoutView.onLogoutFailure(message);
    }
}
