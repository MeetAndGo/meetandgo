package com.meetandgo.meetandgo.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Journey {

    private String jId;
    private Loc startLocation;
    private String startLocationString;

    private long startTime;
    private List<String> users;
    private HashMap<String, ChatMessage> messages;
    private Preferences.Mode mode;
    private boolean active;

    public Journey() {
        this.jId = "";
        this.startLocation = null;
        this.startLocationString = "Location not found.";
        this.startTime = -1;
        this.users = new ArrayList<String>();
        this.messages = new HashMap<>();
        this.active =true;
        this.mode = Preferences.Mode.WALK;
    }

    public Journey(Loc mStartPosition, String startLocationString, long startTime, List<String> users) {
        this.startLocationString = startLocationString;
        this.jId = "";
        this.startLocation = mStartPosition;
        this.startTime = startTime;
        this.users = users;
        this.messages = new HashMap<>();
        this.active =true;
    }

    public void deactivateJourney(){
        this.active =false;

    }
    public String getjId() {
        return jId;
    }

    public void setjId(String journey_id) {
        this.jId = journey_id;
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

    public List<String> getUsers() {
        return users;
    }

    public void setUsers(List<String> users) {
        this.users = users;
    }

    public HashMap<String, ChatMessage> getMessages() { return messages; }

    public void setMessages(HashMap<String, ChatMessage> messages) { this.messages = messages; }

    public void addUser(String uId) {
        users.add(uId);
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

    /**
     * Updates the journey data with a passed in journey object
     *
     * @param o new journey object
     */
    public void update(Journey o) {
        startLocation = o.getStartLocation();
        startTime = o.getStartTime();
        users = o.getUsers();
        messages = o.getMessages();
        active = o.isActive();
    }
}
