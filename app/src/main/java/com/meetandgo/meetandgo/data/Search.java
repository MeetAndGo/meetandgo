package com.meetandgo.meetandgo.data;

import com.meetandgo.meetandgo.FirebaseDB;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Class of a user search, to store in firebase
 */
public class Search {

    private String userId;
    //additional users are those added to combine search between several users
    private List<String> additionalUsers;
    private Preferences userPreferences;
    // TODO: Change locations to float  of lat lng  (Loc class is not being saved in firebase)
    private Loc startLocation = new Loc();
    private Loc endLocation = new Loc();
    private HashMap<String, Object> timeCreated;


    public Search(){}

    public Search(Preferences iPreferences, Loc iStartLocation, Loc iEndLocation) {
        this.userId = FirebaseDB.getCurrentUserUid();
        this.additionalUsers = new ArrayList<>();
        this.userPreferences = iPreferences;
        this.startLocation = iStartLocation;
        this.endLocation = iEndLocation;
        this.timeCreated = FirebaseDB.getServerTime();
    }

    // Test constructor
    public Search(Preferences iPreferences, Loc iStartLocation, Loc iEndLocation, String userId) {
        this.userId = userId;
        this.additionalUsers = new ArrayList<>();
        this.userPreferences = iPreferences;
        this.startLocation = iStartLocation;
        this.endLocation = iEndLocation;
        this.timeCreated = FirebaseDB.getServerTime();
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

}
