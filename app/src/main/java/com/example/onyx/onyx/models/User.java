package com.example.onyx.onyx.models;

import com.google.firebase.database.IgnoreExtraProperties;



@IgnoreExtraProperties
public class User {
    public String uid;
    public String email;
    public String firebaseToken;
    public String displayName;
    public String photoUrl;
    public String firstName;
    public String lastName;
    public boolean isOnline;

    public User() {
    }

    public User(String uid, String email,String displayName,String photoUrl,String firebaseToken, String firstName, String lastName) {
        this.uid = uid;
        this.email = email;
        this.displayName = displayName;
        this.photoUrl = photoUrl;
        this.firebaseToken = firebaseToken;
        this.firstName = firstName;
        this.lastName = lastName;
    }
}
