package com.meetandgo.meetandgo.data;

import android.location.Location;
/**
 * Class to save the information of the user
 */
public class User {
    public String mFullName;
    public String mEmail;
    public double mRating;
    public int mNumOfRatings;
    public Location mPosition;
    public int mNumOfTrips;

    public User() {
        this.mFullName = "";
        this.mEmail = "";
        this.mRating = 0.0;
        this.mNumOfRatings = 0;
        this.mPosition = null;
        this.mNumOfTrips = 0;
    }

    public User(String full_name, String email) {
        this.mFullName = full_name;
        this.mEmail = email;
        this.mRating = 0.0;
        this.mNumOfRatings = 0;
        this.mPosition = null;
        this.mNumOfTrips = 0;
    }
}
