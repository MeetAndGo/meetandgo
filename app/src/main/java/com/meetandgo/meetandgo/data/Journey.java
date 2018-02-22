package com.meetandgo.meetandgo.data;

import java.util.List;

/**
 * Created by pgeogheg on 2/15/2018.
 */

public class Journey {

    private String mJid;
    private Loc mStartLocation;

    private long mStartTime;
    private List<String> mJourneyUsers;
    private String mChatID;

    public Journey() {
        this.mJid = "";
        this.mStartLocation = null;
        this.mStartTime = -1;
        this.mJourneyUsers = null;
        this.mChatID = "";
    }

    public Journey(Loc mStartPosition, long mStartTime, List<String> mUsers) {
        this.mJid = "";
        this.mStartLocation = mStartPosition;
        this.mStartTime = mStartTime;
        this.mJourneyUsers = mUsers;
        this.mChatID = "";
    }

    public String getmJid() {
        return mJid;
    }

    public void setmJid(String mJid) {
        this.mJid = mJid;
    }

    public Loc getmStartLocation() {
        return mStartLocation;
    }

    public void setmStartLocation(Loc mStartLocation) {
        this.mStartLocation = mStartLocation;
    }

    public long getmStartTime() {
        return mStartTime;
    }

    public void setmStartTime(long mStartTime) {
        this.mStartTime = mStartTime;
    }

    public List<String> getmJourneyUsers() {
        return mJourneyUsers;
    }

    public void setmJourneyUsers(List<String> mUsers) {
        this.mJourneyUsers = mUsers;
    }

    public String getmChatID() {
        return mChatID;
    }

    public void setmChatID(String mChatID) {
        this.mChatID = mChatID;
    }

    public void assignId(String mJid) {
        this.mJid = mJid;
    }

    public void setChatID(String newChatID) {
        this.mChatID = newChatID;
    }

    public String toString() {
        return "";
    }

}
