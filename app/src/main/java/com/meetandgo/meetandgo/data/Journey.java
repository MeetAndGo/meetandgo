package com.meetandgo.meetandgo.data;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Stored in FirebaseDB
 * Contains a group of matched users and the group chat
 */

public class Journey {

    private String journeyID;
    private String searchID;
    private Loc startLocation;
    private String startLocationString;

    private long startTime;
    private ArrayList<String> users;
    private HashMap<String, ChatMessage> messages;
    private Preferences.Mode mode;
    private boolean active;

    public Journey() {
        this.journeyID = "";
        this.searchID = "";
        this.startLocation = null;
        this.startLocationString = "Location not found.";
        this.startTime = -1;
        this.users = new ArrayList<>();
        this.messages = new HashMap<>();
        this.active = true;
        this.mode = Preferences.Mode.WALK;
    }

    public Journey(String searchID, Loc startLocation, String startLocationString, long startTime, ArrayList<String> users) {
        this.startLocationString = startLocationString;
        this.searchID = searchID;
        this.journeyID = "";
        this.startLocation = startLocation;
        this.startTime = startTime;
        this.users = users;
        this.messages = new HashMap<>();
        this.active = true;
    }

    public void deactivateJourney() {
        this.active = false;
    }

    public String getJourneyID() {
        return journeyID;
    }

    public void setJourneyID(String journeyID) {
        this.journeyID = journeyID;
    }

    public Loc getStartLocation() {
        return startLocation;
    }

    public void setStartLocation(Loc startLocation) {
        this.startLocation = startLocation;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public ArrayList<String> getUsers() {
        return users;
    }

    public void setUsers(ArrayList<String> users) {
        this.users = users;
    }

    public HashMap<String, ChatMessage> getMessages() {
        return messages;
    }

    public void setMessages(HashMap<String, ChatMessage> messages) {
        this.messages = messages;
    }

    public void addUser(String uID) {
        users.add(uID);
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public Preferences.Mode getMode() {
        return mode;
    }

    public void setMode(Preferences.Mode mode) {
        this.mode = mode;
    }

    public String getStartLocationString() {
        return startLocationString;
    }

    public void setStartLocationString(String startLocationString) {
        this.startLocationString = startLocationString;
    }

    public String getSearchID() {
        return searchID;
    }

    public void setSearchID(String searchID) {
        this.searchID = searchID;
    }

    /**
     * Updates the journey data with a passed in journey object
     *
     * @param journey new journey object
     */
    public void update(Journey journey) {
        startLocation = journey.getStartLocation();
        startTime = journey.getStartTime();
        users = journey.getUsers();
        messages = journey.getMessages();
        active = journey.isActive();
    }

}
