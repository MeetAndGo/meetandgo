package com.meetandgo.meetandgo.data;


public class Chat {
    private String chatId;
    private User[] users;
    private ChatMessage[] messages;

    public Chat(){
        this.chatId="";
        this.users = new User[0];
        this.messages = new ChatMessage[0];
    }

    public Chat(User[] iUsers) {
        this.chatId="";
        this.users = iUsers;
        this.messages = new ChatMessage[0];
    }
}
