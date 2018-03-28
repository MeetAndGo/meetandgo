package com.meetandgo.meetandgo.data;

import com.google.firebase.database.Exclude;
import com.meetandgo.meetandgo.FireBaseDB;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Stored in FirebaseDB
 * Enables system to compute the matching system algorithm and enables user selection
 */

public class Search implements Serializable {

    private String searchID;
    private String userID;
    private ArrayList<String> additionalUserIDs = new ArrayList<>();
    private Preferences userPreferences;
    private Loc startLocation = new Loc();
    private Loc endLocation = new Loc();
    private String startLocationString;
    private String endLocationString;
    private HashMap<String, Object> timeCreated;
    private String journeyID;

    public Search() {
    }

    public Search(Preferences preferences, String journeyID, Loc startLocation, Loc endLocation, String startLocationString, String endLocationString) {
        this.startLocationString = startLocationString;
        this.endLocationString = endLocationString;
        this.userID = FireBaseDB.getCurrentUserID();
        this.additionalUserIDs = new ArrayList<>();
        this.userPreferences = preferences;
        this.startLocation = startLocation;
        this.endLocation = endLocation;
        this.timeCreated = FireBaseDB.getServerTime();
        this.journeyID = journeyID;
    }

    // Test constructor
    public Search(Preferences preferences, Loc startLocation, Loc endLocation, String userID, String startLocationString, String endLocationString) {
        this.userID = userID;
        this.startLocationString = startLocationString;
        this.endLocationString = endLocationString;
        this.additionalUserIDs = new ArrayList<>();
        this.userPreferences = preferences;
        this.startLocation = startLocation;
        this.endLocation = endLocation;
        this.timeCreated = null;
        this.journeyID = "";
        this.searchID = "";
    }

    public String getUserID() {
        return this.userID;
    }

    public Preferences getUserPreferences() {
        return this.userPreferences;
    }

    public Loc getStartLocation() {
        return startLocation;
    }

    public void setStartLocation(Loc startLocation) {
        this.startLocation = startLocation;
    }

    public Loc getEndLocation() {
        return endLocation;
    }

    public void setEndLocation(Loc endLocation) {
        this.endLocation = endLocation;
    }

    public HashMap<String, Object> getTimeCreated() {
        return timeCreated;
    }

    public void setTimeCreated(HashMap<String, Object> time_created) {
        this.timeCreated = time_created;
    }

    public String getStartLocationString() {
        return startLocationString;
    }

    public void setStartLocationString(String startLocationString) {
        this.startLocationString = startLocationString;
    }

    public String getEndLocationString() {
        return endLocationString;
    }

    public void setEndLocationString(String endLocationString) {
        this.endLocationString = endLocationString;
    }

    public ArrayList<String> getAdditionalUsers() {
        return additionalUserIDs;
    }

    public void setAdditionalUsers(ArrayList<String> additionalUsers) {
        this.additionalUserIDs = additionalUsers;
    }

    public String getSearchID() {
        return searchID;
    }

    public void setSearchID(String searchID) {
        this.searchID = searchID;
    }

    public void addUser(String userId) {
        this.additionalUserIDs.add(userId);
    }

    public String getJourneyID() {
        return journeyID;
    }

    public void setJourneyID(String journeyID) {
        this.journeyID = journeyID;
    }

    @Exclude
    public boolean hasJourneyID() {
        return !journeyID.equals("");
    }
}
