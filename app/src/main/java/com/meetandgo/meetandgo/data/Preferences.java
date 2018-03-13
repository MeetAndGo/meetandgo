package com.meetandgo.meetandgo.data;

import com.google.firebase.database.Exclude;

import java.io.Serializable;

/**
 * Class to save the preferences that the user sets for the journey search
 */
public class Preferences implements Serializable {

    public enum Gender {ANY, MALE, FEMALE}
    public enum Mode {ANY, WALK, TAXI}

    public Gender preferredGender = Gender.ANY;
    public Gender userGender = Gender.ANY;
    public Mode mode = Mode.ANY;

    public Preferences(User currentUser){
        this.userGender = currentUser.gender;
    }

    public Preferences(Gender gender, Mode mode, User user){
        this.preferredGender = gender;
        this.mode = mode;
        this.userGender = user.gender;
    }

    public Gender getPreferredGender() {
        return preferredGender;
    }

    public void setPreferredGender(Gender preferredGender) {
        this.preferredGender = preferredGender;
    }

    public Gender getUserGender() {
        return userGender;
    }

    public void setUserGender(Gender userGender) {
        this.userGender = userGender;
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
        if (preferredGender == null) return false;
        if (mode == null) return false;
        //if(startLocation.getLatitude() == 0 && startLocation.getLongitude() == 0 &&
               // endLocation.getLongitude() == 0 && endLocation.getLatitude() ==0) return false;

        return true;
    }

    public boolean equals(Preferences other){
        if(this.preferredGender == other.getPreferredGender() && this.mode == other.getMode())
            return true;
        else
            return false;
    }

    /**
     * Checks if othor user matches our search criteria
     * @param otherPreferences other user preferences
     * @return boolean if match
     */
    public boolean match(Preferences otherPreferences) {
        if(preferredGender == Gender.ANY && mode == otherPreferences.mode)
            return true;
        else if(preferredGender.ordinal() == otherPreferences.userGender.ordinal() && mode == otherPreferences.mode)
            return true;
        else
            return false;
    }
}
