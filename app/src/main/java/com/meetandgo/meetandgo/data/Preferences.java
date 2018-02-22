package com.meetandgo.meetandgo.data;

import com.google.firebase.database.Exclude;

import java.io.Serializable;

/**
 * Class to save the preferences that the user sets for the journey search
 */
public class Preferences implements Serializable {

    public enum Gender {ANY, MALE, FEMALE}
    public enum Mode {ANY, WALK, CAR, TAXI}

    public Gender gender = Gender.ANY;
    public Mode mode = Mode.ANY;

    public Preferences(){}

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

    // We exclude the methods from the database that are not useful for defining the preferences object
    @Exclude
    public boolean isCompleted(){
        if (gender == null) return false;
        if (mode == null) return false;
        //if(startLocation.getLatitude() == 0 && startLocation.getLongitude() == 0 &&
               // endLocation.getLongitude() == 0 && endLocation.getLatitude() ==0) return false;

        return true;
    }

}
