package com.meetandgo.meetandgo.data;

import com.google.firebase.database.Exclude;
import com.meetandgo.meetandgo.FirebaseDB;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Class of a user search, to store in firebase
 */
public class Search implements Serializable {

    private String sID;
    private String userId;
    //additional users are those added to combine search between several users
    private ArrayList<String> additionalUsers = new ArrayList<>();
    private Preferences userPreferences;
    private Loc startLocation = new Loc();
    private Loc endLocation = new Loc();
    private String startLocationString;
    private String endLocationString;
    private HashMap<String, Object> timeCreated;
    private String journeyID;

    public Search() {
    }

    public Search(Preferences iPreferences, String journeyID, Loc iStartLocation, Loc iEndLocation, String startLocationString, String endLocationString) {
        this.startLocationString = startLocationString;
        this.endLocationString = endLocationString;
        this.userId = FirebaseDB.getCurrentUserUid();
        this.additionalUsers = new ArrayList<>();
        this.userPreferences = iPreferences;
        this.startLocation = iStartLocation;
        this.endLocation = iEndLocation;
        this.timeCreated = FirebaseDB.getServerTime();
        this.journeyID = journeyID;
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
        this.journeyID = "";
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

    public ArrayList<String> getAdditionalUsers() {
        return additionalUsers;
    }

    public void setAdditionalUsers(ArrayList<String> additionalUsers) {
        this.additionalUsers = additionalUsers;
    }

    public String getsID() {
        return sID;
    }

    public void setsID(String sID) {
        this.sID = sID;
    }

    public void addUser(String userId) {
        this.additionalUsers.add(userId);
    }

    public String getJourneyID() {
        return journeyID;
    }

    public void setJourneyID(String journeyID) {
        this.journeyID = journeyID;
    }

    @Exclude
    public boolean hasJourney(){
        return !journeyID.equals("");
    }
}
