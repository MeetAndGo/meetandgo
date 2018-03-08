package com.meetandgo.meetandgo.data;

import android.location.Location;
/**
 * Class to save the information of the user
 */
public class User {
    public enum Gender {MALE, FEMALE}
    public String fullName;
    public String email;
    public double rating;
    public int numOfRatings;
    public Location position;
    public int numOfTrips;
    public Gender mGender;

    public User() {
        this.fullName = "";
        this.email = "";
        this.rating = 0.0;
        this.numOfRatings = 0;
        this.position = null;
        this.numOfTrips = 0;
        this.mGender = null;
    }

    public User(String fullName, String email) {
        this.fullName = fullName;
        this.email = email;
        this.rating = 0.0;
        this.numOfRatings = 0;
        this.position = null;
        this.numOfTrips = 0;
        this.mGender = null;
    }
}
