package com.example.onyx.onyx.models;

import com.google.firebase.database.IgnoreExtraProperties;



@IgnoreExtraProperties
public class User {
    public String uid;
    public String email;
    public String firebaseToken;
    public String firstName;
    public String lastName;
    public boolean isOnline;

    public User() {
    }

    public User(String uid, String email, String firebaseToken,String firstName,String lastName,boolean isOnline) {
        this.uid = uid;
        this.email = email;
        this.lastName = lastName;
        this.firstName = firstName;
        this.isOnline = isOnline;
        this.firebaseToken = firebaseToken;
    }
}
