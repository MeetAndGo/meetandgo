package com.meetandgo.meetandgo.data;

import com.meetandgo.meetandgo.FirebaseDB;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Class of a user search, to store in firebase
 */
public class Search implements Serializable {

    private String userId;
    //additional users are those added to combine search between several users
    private ArrayList<String> additionalUsers = new ArrayList<>();
    private Preferences userPreferences;
    private Loc startLocation = new Loc();
    private Loc endLocation = new Loc();
    private String startLocationString;
    private String endLocationString;
    private HashMap<String, Object> timeCreated;

    public Search() {
    }

    public Search(Preferences iPreferences, Loc iStartLocation, Loc iEndLocation, String startLocationString, String endLocationString) {
        this.startLocationString = startLocationString;
        this.endLocationString = endLocationString;
        this.userId = FirebaseDB.getCurrentUserUid();
        this.additionalUsers = new ArrayList<>();
        this.userPreferences = iPreferences;
        this.startLocation = iStartLocation;
        this.endLocation = iEndLocation;
        this.timeCreated = FirebaseDB.getServerTime();
    }

    // Test constructor
    public Search(Preferences iPreferences, Loc iStartLocation, Loc iEndLocation, String userId, String startLocationString, String endLocationString) {
        this.userId = userId;
        this.startLocationString = startLocationString;
        this.endLocationString = endLocationString;
        this.additionalUsers = new ArrayList<>();
        this.userPreferences = iPreferences;
        this.startLocation = iStartLocation;
        this.endLocation = iEndLocation;
        this.timeCreated = null;
    }

    public String getUserId() {
        return this.userId;
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

    public List<String> getAdditionalUsers() {
        return additionalUsers;
    }

    public void setAdditionalUsers(ArrayList<String> additionalUsers) {
        this.additionalUsers = additionalUsers;
    }
}
