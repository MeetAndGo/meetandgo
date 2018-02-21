package com.meetandgo.meetandgo.data;

import android.location.Location;

import com.meetandgo.meetandgo.FirebaseDB;

/**
 * Class of a user search, to store in firebase
 * Created by gilmarma on 2/21/2018.
 */

public class Search {

    private String mUserID;
    private Preferences mUserPreferences;
    private Location mStartLocation = new Location("");
    private Location mEndLocation = new Location("");


    public Search(Preferences iPreferences, Location iStartLocation, Location iEndLocation) {
        mUserID = FirebaseDB.getCurrentUserUid();
        mUserPreferences = iPreferences;
        mStartLocation = iStartLocation;
        mEndLocation = iEndLocation;
    }

    public String getmUserID() { return this.mUserID;}

    public Preferences getmUserPreferences() {return this.mUserPreferences;}

    public Location getStartLocation() {
        return mStartLocation;
    }

    public void setStartLocation(Location startLocation) {
        this.mStartLocation = startLocation;
    }

    public Location getEndLocation() {
        return mEndLocation;
    }

    public void setEndLocation(Location endLocation) {
        this.mEndLocation = endLocation;
    }
}
