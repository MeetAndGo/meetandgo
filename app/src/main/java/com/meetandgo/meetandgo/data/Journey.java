package com.meetandgo.meetandgo.data;

import java.util.List;

public class Journey {

    private String journeyId;
    private Loc startLocation;

    private long startTime;
    private List<String> journeyUsers;
    private String chatId;

    public Journey() {
        this.journeyId = "";
        this.startLocation = null;
        this.startTime = -1;
        this.journeyUsers = null;
        this.chatId = "";
    }

    public Journey(Loc mStartPosition, long mStartTime, List<String> mUsers) {
        this.journeyId = "";
        this.startLocation = mStartPosition;
        this.startTime = mStartTime;
        this.journeyUsers = mUsers;
        this.chatId = "";
    }

    public String getJourneyId() {
        return journeyId;
    }

    public void setJourneyId(String journey_id) {
        this.journeyId = journey_id;
    }

    public Loc getStartLocation() {
        return startLocation;
    }

    public void setStartLocation(Loc start_location) {
        this.startLocation = start_location;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long start_time) {
        this.startTime = start_time;
    }

    public List<String> getJourneyUsers() {
        return journeyUsers;
    }

    public void setJourneyUsers(List<String> mUsers) {
        this.journeyUsers = mUsers;
    }

    public String getChatId() {
        return chatId;
    }

    public void setChatId(String chat_id) {
        this.chatId = chat_id;
    }

    public void assignId(String mJid) {
        this.journeyId = mJid;
    }

    public void setChatID(String newChatID) {
        this.chatId = newChatID;
    }

}
