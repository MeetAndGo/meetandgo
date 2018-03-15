package com.meetandgo.meetandgo.data;

import android.location.Location;

import java.util.ArrayList;
import java.util.List;

/**
 * Class to save the information of the user
 */
public class User {
    public String fullName;
    public String email;
    public double rating;
    public int numOfRatings;
    public Location position;
    public int numOfTrips;
    public Preferences.Gender gender;
    public List<String> journeyIDs;

    public User() {
        this.fullName = "";
        this.email = "";
        this.rating = 0.0;
        this.numOfRatings = 0;
        this.position = null;
        this.numOfTrips = 0;
        this.gender = null;
        this.journeyIDs = new ArrayList<String>();
    }

    public User(String fullName, String email) {
        this.fullName = fullName;
        this.email = email;
        this.rating = 0.0;
        this.numOfRatings = 0;
        this.position = null;
        this.numOfTrips = 0;
        this.gender = null;
        this.journeyIDs = new ArrayList<String>();
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public double getRating() {
        return rating;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }

    public int getNumOfRatings() {
        return numOfRatings;
    }

    public void setNumOfRatings(int numOfRatings) {
        this.numOfRatings = numOfRatings;
    }

    public Location getPosition() {
        return position;
    }

    public void setPosition(Location position) {
        this.position = position;
    }

    public int getNumOfTrips() {
        return numOfTrips;
    }

    public void setNumOfTrips(int numOfTrips) {
        this.numOfTrips = numOfTrips;
    }

    public Preferences.Gender getGender() {
        return gender;
    }

    public void setGender(Preferences.Gender gender) {
        this.gender = gender;
    }

}
