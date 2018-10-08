package com.example.onyx.onyx.models;

import android.support.annotation.NonNull;

import com.google.firebase.database.IgnoreExtraProperties;

import java.util.Comparator;


@IgnoreExtraProperties
public class User implements Comparable<User> {
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

    public User(String uid, String email, String displayName, String photoUrl, String firebaseToken, String firstName, String lastName) {
        this.uid = uid;
        this.email = email;
        this.displayName = displayName;
        this.photoUrl = photoUrl;
        this.firebaseToken = firebaseToken;
        this.firstName = firstName;
        this.lastName = lastName;
    }


    @Override
    public int compareTo(@NonNull User user) {
        int result = this.lastName.toLowerCase().compareTo(user.lastName.toLowerCase());
        if (result == 0) {
            return this.firstName.toLowerCase().compareTo(user.firstName.toLowerCase());
        } else {
            return result;
        }
    }
}
