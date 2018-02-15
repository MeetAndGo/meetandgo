package com.meetandgo.meetandgo.data;

import android.location.Location;

import java.util.Date;

/**
 * Created by pgeogheg on 2/15/2018.
 */

public class Journey {

    private String mJid;
    private Location mStartPosition;
    private Date mStartTime;
    private User mUsers[];

    public Journey() {
        mJid = "";
        mStartPosition = null;
        mStartTime = null;
        mUsers = new User[0];
    }

    public Journey(Location mStartPosition, Date mStartTime, User mUsers[]) {
        this.mJid = "";
        this.mStartPosition = mStartPosition;
        this.mStartTime = mStartTime;
        this.mUsers = mUsers;
    }

    public void assignId(String mJid) {
        this.mJid = mJid;
    }


}
