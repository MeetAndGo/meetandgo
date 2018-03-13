package com.meetandgo.meetandgo.data;

import java.util.ArrayList;
import java.util.List;

public class Journey {

    private String mJid;
    private Loc mStartLocation;

    private long mStartTime;
    private List<String> mUsers;
    private List<ChatMessage> mMessages;
    private boolean mActive;

    public Journey() {
        this.mJid = "";
        this.mStartLocation = null;
        this.mStartTime = -1;
        this.mUsers = new ArrayList<String>();
        this.mMessages = new ArrayList<ChatMessage>();
        this.mActive=true;
    }

    public Journey(Loc mStartPosition, long mStartTime, List<String> mUsers) {
        this.mJid = "";
        this.mStartLocation = mStartPosition;
        this.mStartTime = mStartTime;
        this.mUsers = mUsers;
        this.mMessages = new ArrayList<ChatMessage>();
        this.mActive=true;
    }

    public Journey(Loc mStartPosition, long mStartTime, List<String> mUsers, List<ChatMessage> mMessages) {
        this.mJid = "";
        this.mStartLocation = mStartPosition;
        this.mStartTime = mStartTime;
        this.mUsers = mUsers;
        this.mMessages = mMessages ;
        this.mActive=true;
    }

    public void deactivateJourney(){
        this.mActive=false;

    }
    public String getmJid() {
        return mJid;
    }

    public void setmJid(String journey_id) {
        this.mJid = journey_id;
    }

    public Loc getmStartLocation() {
        return mStartLocation;
    }

    public void setmStartLocation(Loc start_location) {
        this.mStartLocation = start_location;
    }

    public long getmStartTime() {
        return mStartTime;
    }

    public void setmStartTime(long start_time) {
        this.mStartTime = start_time;
    }

    public List<String> getmUsers() {
        return mUsers;
    }

    public void setmUsers(List<String> mUsers) {
        this.mUsers = mUsers;
    }

    public List<ChatMessage> getmMessages() { return mMessages; }

    public void setmMessages(List<ChatMessage> mMessages) { this.mMessages = mMessages; }

    public void addUser(String uId) {
        mUsers.add(uId);
    }

    public void addMessage(ChatMessage message) {
        mMessages.add(message);
    }

}
