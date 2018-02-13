package com.meetandgo.meetandgo.data;

import android.location.Location;

/**
 * Class to save the preferences that the user sets for the journey search
 */
public class Preferences {

    enum Gender {ANY, MALE, FEMALE}
    enum Mode {ANY, WALK, CAR, TAXI}

    public Gender gender = Gender.ANY;
    public Mode mode = Mode.ANY;
    public Location startLocation = new Location("");
    public Location endLocation = new Location("");

    public Preferences(){

    }

    public Gender getGender() {
        return gender;
    }

    public void setGender(Gender gender) {
        this.gender = gender;
    }

    public Mode getMode() {
        return mode;
    }

    public void setMode(Mode mode) {
        this.mode = mode;
    }

    public Location getStartLocation() {
        return startLocation;
    }

    public void setStartLocation(Location startLocation) {
        this.startLocation = startLocation;
    }

    public Location getEndLocation() {
        return endLocation;
    }

    public void setEndLocation(Location endLocation) {
        this.endLocation = endLocation;
    }

    public boolean isCompleted(){
        if (gender == null) return false;
        if (mode == null) return false;
        if(startLocation.getLatitude() == 0 && startLocation.getLongitude() == 0 &&
                endLocation.getLongitude() == 0 && endLocation.getLatitude() ==0) return false;

        return true;
    }

}
