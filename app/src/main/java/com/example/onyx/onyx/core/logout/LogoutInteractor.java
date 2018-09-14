package com.example.onyx.onyx.core.logout;

import com.google.firebase.auth.FirebaseAuth;



public class LogoutInteractor implements LogoutInterface.Interactor {
    private LogoutInterface.OnLogoutListener mOnLogoutListener;

    public LogoutInteractor(LogoutInterface.OnLogoutListener onLogoutListener) {
        mOnLogoutListener = onLogoutListener;
    }

    @Override
    public void performFirebaseLogout() {
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            FirebaseAuth.getInstance().signOut();
            mOnLogoutListener.onSuccess("Successfully logged out!");
        } else {
            mOnLogoutListener.onFailure("No user logged in yet!");
        }
    }
}
