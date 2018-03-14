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

    public Preferences(Gender preferred_gender, Mode mode, Gender user_gender){
        this.preferredGender = preferred_gender;
        this.mode = mode;
        this.userGender = user_gender;
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

    /**
     * @param m1 preferred mode of user 1
     * @param m2 preferred mode of user 2
     * @return true if mode ok false otherwise
     */
    public static boolean checkMode(Mode m1, Mode m2)
    {
        if (m1 == Mode.ANY || m2 == Mode.ANY || m1 == m2)
        {
            return true;
        }
        else{
            return false;
        }
    }

    /**
     * @param prefGender1 preferred gender of user 1
     * @param prefGender2 preferred gender of user 2
     * @param userGender1 gender of user 1
     * @param userGender2 gender of user 2
     * @return true if gender match preferences, false otherwise
     */
    public static boolean checkGender(Gender prefGender1, Gender prefGender2,
                                       Gender userGender1, Gender userGender2)
    {
        if (prefGender1 == Gender.ANY && prefGender2 == Gender.ANY) {
            return true;
        } else if (prefGender1 == Gender.ANY && prefGender2 == userGender1) {
            return true;
        } else if (prefGender1 == userGender2 && prefGender2 == Gender.ANY) {
            return true;
        } else if (prefGender1 == userGender2 && prefGender2 == userGender1) {
            return true;
        } else{
            return false;
        }
    }

    /**
     * Checks if other user matches our search criteria
     * @param otherPreferences other user preferences
     * @return boolean if match
     */
    public boolean checkPreferences(Preferences otherPreferences) {
        return (checkMode(this.mode, otherPreferences.mode)
                && checkGender(this.preferredGender, otherPreferences.preferredGender,
                this.userGender, otherPreferences.userGender));
    }
}
