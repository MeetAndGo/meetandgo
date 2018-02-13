package com.meetandgo.meetandgo.data;

import android.location.Location;

public class User {
    public String mFullName;
    public String mEmail;
    public double mRating;
    public int mNumOfRatings;
    public Location mPosition;

    public User() {
        this.mFullName = "";
        this.mEmail = "";
        this.mRating = 0.0;
        this.mNumOfRatings = 0;
        this.mPosition = null;
    }

    public User(String full_name, String email) {
        this.mFullName = full_name;
        this.mEmail = email;
        this.mRating = 0.0;
        this.mNumOfRatings = 0;
        this.mPosition = null;
    }
}
