package com.meetandgo.meetandgo.data;

/**
 * Class to save the preferences that the user sets for the journey search
 */
public class Preferences {

    enum Gender {ANY, MALE, FEMALE}
    enum Mode {ANY, WALK, CAR, TAXI}

    public Gender gender = Gender.ANY;
    public Mode mode = Mode.ANY;

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
}