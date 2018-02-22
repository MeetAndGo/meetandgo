package com.meetandgo.meetandgo.data;

import com.meetandgo.meetandgo.FirebaseDB;

import java.util.Map;

/**
 * Class of a user search, to store in firebase
 */
public class Search {

    private String userId;
    private Preferences userPreferences;
    // TODO: Change locations to float  of lat lng  (Loc class is not being saved in firebase)
    private Loc startLocation = new Loc();
    private Loc endLocation = new Loc();
    private Map<String, String> timeCreated;


    public Search(Preferences iPreferences, Loc iStartLocation, Loc iEndLocation) {
        this.userId = FirebaseDB.getCurrentUserUid();
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

    public Map<String, String> getTimeCreated() {
        return timeCreated;
    }

    public void setTimeCreated(Map<String, String> time_created) {
        this.timeCreated = time_created;
    }

}
