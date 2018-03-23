package com.meetandgo.meetandgo.data;

import android.location.Location;

import com.google.firebase.database.Exclude;

import java.util.ArrayList;

/**
 * Class to save the information of the user
 */
public class User {
    private String fullName;
    private String email;
    private double rating;
    private int numOfRatings;
    private Location position;
    private int numOfTrips;
    private Preferences.Gender gender;
    private ArrayList<String> journeyIDs;

    public User() {
        this.fullName = "";
        this.email = "";
        this.rating = 0.0;
        this.numOfRatings = 0;
        this.position = null;
        this.numOfTrips = 0;
        this.gender = null;
        this.journeyIDs = new ArrayList<>();
    }

    public User(String fullName, String email) {
        this.fullName = fullName;
        this.email = email;
        this.rating = 0.0;
        this.numOfRatings = 0;
        this.position = null;
        this.numOfTrips = 0;
        this.gender = null;
        this.journeyIDs = new ArrayList<>();
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

    public ArrayList<String> getJourneyIDs() { return journeyIDs; }

    public void setJourneyIDs(ArrayList<String> journeyIDs) { this.journeyIDs = journeyIDs; }

    @Exclude
    public void addJourneyID(String journeyID) {
        journeyIDs.add(journeyID);
    }

    @Exclude
    public void removeJourney(String journeyID) {
        journeyIDs.remove(journeyID);
    }
}
