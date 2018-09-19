package com.example.onyx.onyx.models;

import com.google.firebase.database.IgnoreExtraProperties;



@IgnoreExtraProperties
public class User {
    public String uid;
    public String email;
    public String firebaseToken;
    public String displayName;
    public String photoUrl;
    public boolean isOnline;

    public User() {
    }

    public User(String uid, String emai,String displayName,String photoUrl,String firebaseToken) {
        this.uid = uid;
        this.email = email;
        this.displayName = displayName;
        this.photoUrl = photoUrl;
        this.firebaseToken = firebaseToken;
    }
}
