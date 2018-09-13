package com.example.onyx.onyx;

import android.location.Location;

import java.util.List;

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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFirstname() {
        return firstname;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public String getLastname() {
        return lastname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    public List<String> getContacts() {
        return contacts;
    }

    public void setContacts(List<String> contacts) {
        this.contacts = contacts;
    }

    public List<Location> getFavouriteplaces() {
        return favouriteplaces;
    }

    public void setFavouriteplaces(List<Location> favouriteplaces) {
        this.favouriteplaces = favouriteplaces;
    }

    public boolean getIsCarer(boolean isCarer) {
        return isCarer;
    }



}