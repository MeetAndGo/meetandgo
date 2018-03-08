package com.meetandgo.meetandgo.data;

import com.google.firebase.database.Exclude;
import com.meetandgo.meetandgo.FirebaseDB;

import java.util.HashMap;

public class ChatMessage {

    private String messageText;
    private String messageUser;
    private HashMap<String, Object> timestampCreated;
    private String userID;
    private Long time;

    public ChatMessage(String messageText, String messageUser, String userID) {
        this.messageText = messageText;
        this.messageUser = messageUser;
        this.userID = userID;

        // Initialize to current time
        HashMap<String, Object> timestampNow = FirebaseDB.getServerTime();
        this.timestampCreated = timestampNow;
    }

    public ChatMessage(){}

    public String getMessageText() {
        return messageText;
    }

    public void setMessageText(String messageText) {
        this.messageText = messageText;
    }

    public String getMessageUser() {
        return messageUser;
    }

    public void setMessageUser(String messageUser) {
        this.messageUser = messageUser;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public HashMap<String, Object> getTimestampCreated(){
        return timestampCreated;
    }

    @Exclude
    public long getTimestampCreatedLong(){
        return (long)timestampCreated.get("timestamp");
    }
}