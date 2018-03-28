package com.meetandgo.meetandgo.data;

import java.io.Serializable;

/**
 * Class to save the preferences that the user sets for the journey search
 */

public class Preferences implements Serializable {

    public enum Gender {ANY, MALE, FEMALE}

    public enum Mode {ANY, WALK, TAXI}

    private Gender preferredGender = Gender.ANY;
    private Gender userGender = Gender.ANY;
    private Mode mode = Mode.ANY;

    public Preferences() {
    }

    public Preferences(User currentUser) {
        this.userGender = currentUser.getGender();
    }

    public Preferences(Gender preferredGender, Mode mode, Gender userGender) {
        this.preferredGender = preferredGender;
        this.mode = mode;
        this.userGender = userGender;
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

    /**
     * Checks if modes match
     *
     * @param m1 preferred mode of user 1
     * @param m2 preferred mode of user 2
     * @return true if mode ok false otherwise
     */
    public static boolean checkMode(Mode m1, Mode m2) {
        return m1 == Mode.ANY || m2 == Mode.ANY || m1 == m2;
    }

    /**
     * Checks if genders match
     *
     * @param prefGender1 preferred gender of user 1
     * @param prefGender2 preferred gender of user 2
     * @param userGender1 gender of user 1
     * @param userGender2 gender of user 2
     * @return true if gender match preferences, false otherwise
     */
    public static boolean checkGender(Gender prefGender1, Gender prefGender2,
                                      Gender userGender1, Gender userGender2) {
        return prefGender1 == Gender.ANY && prefGender2 == Gender.ANY
                || prefGender1 == Gender.ANY && prefGender2 == userGender1
                || prefGender1 == userGender2 && prefGender2 == Gender.ANY
                || prefGender1 == userGender2 && prefGender2 == userGender1;
    }

    /**
     * Checks if other user matches our search criteria
     *
     * @param otherPreferences other user preferences
     * @return boolean if match
     */
    public boolean checkPreferences(Preferences otherPreferences) {
        return (checkMode(this.mode, otherPreferences.mode)
                && checkGender(this.preferredGender, otherPreferences.preferredGender,
                this.userGender, otherPreferences.userGender));
    }
}
