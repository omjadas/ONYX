package com.example.onyx.onyx;

import android.location.Location;

import com.google.firebase.database.IgnoreExtraProperties;

import java.util.List;

@IgnoreExtraProperties
public class User {

    public String email;
    public String firstname;
    public String lastname;
    public List<String> contacts;
    public List<Location> favouriteplaces;
    public Boolean isCarer;

    public User() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public User(String email, String firstname, String lastname,
                List<String> contacts, List<Location> favouriteplaces, Boolean isCarer) {
        this.email = email;
        this.firstname = firstname;
        this.lastname = lastname;
        this.contacts = contacts;
        this.favouriteplaces = favouriteplaces;
        this.isCarer = isCarer;
    }

}