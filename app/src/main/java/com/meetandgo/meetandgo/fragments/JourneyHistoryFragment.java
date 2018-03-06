package com.meetandgo.meetandgo.fragments;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.meetandgo.meetandgo.FirebaseDB;
import com.meetandgo.meetandgo.R;
import com.meetandgo.meetandgo.data.ChatMessage;
import com.meetandgo.meetandgo.data.Journey;
import com.meetandgo.meetandgo.data.Loc;
import com.meetandgo.meetandgo.data.User;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class JourneyHistoryFragment extends Fragment {

    private static final String TAG = "JourneyHistoryFragment";

    public JourneyHistoryFragment() {}

    public static Fragment newInstance() {
        JourneyHistoryFragment fragment = new JourneyHistoryFragment();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        //Testing
        Loc l = new Loc(12,13);
        long start = new Date().getTime();
        List<String> users = new ArrayList<String>();
        users.add("Hello");
        ChatMessage m = new ChatMessage("Hello World!","Paddy");
        ChatMessage m2 = new ChatMessage("Hello Paddy!","World");
        List<ChatMessage> messages = new ArrayList<ChatMessage>();
        messages.add(m);
        Journey j = new Journey(l,start,users);
        String journeyId = FirebaseDB.addNewJourney(j);
        //FirebaseDB.addMessageToJourney(journeyId, m);
        //FirebaseDB.addMessageToJourney(journeyId, m2);
        j.addMessage(m);
        j.addMessage(m2);
        j.addUser("Paddy");
        j.addUser("World");
        FirebaseDB.updateJourney(journeyId, j);
        return inflater.inflate(R.layout.fragment_journey_history, container, false);
    }

}
