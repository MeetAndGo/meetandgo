package com.meetandgo.meetandgo.fragments;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.linearlistview.LinearListView;
import com.meetandgo.meetandgo.FirebaseDB;
import com.meetandgo.meetandgo.R;
import com.meetandgo.meetandgo.data.User;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import butterknife.BindArray;
import butterknife.BindView;
import butterknife.ButterKnife;

public class JourneyHistoryFragment extends Fragment {

    private static final String TAG = "JourneyHistoryFragment";
    private View mView;
    private User mUser;
    @BindView(R.id.listHistoryJourneys) ListView listHistoryJourneys;
    List<String>journey_history_ids;
    ArrayAdapter adapter;
    public JourneyHistoryFragment() {}

    public static Fragment newInstance() {
        JourneyHistoryFragment fragment = new JourneyHistoryFragment();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        journey_history_ids = Arrays.asList(getResources().getStringArray(R.array.history));
        mView = inflater.inflate(R.layout.fragment_journey_history, container, false);
        ButterKnife.bind(this, mView);
        //Testing
//        Loc l = new Loc(12,13);
//        long start = new Date().getTime();
//        List<String> users = new ArrayList<String>();
//        users.add("Hello");
//        ChatMessage m = new ChatMessage("Hello World!","Paddy");
//        ChatMessage m2 = new ChatMessage("Hello Paddy!","World");
//        List<ChatMessage> messages = new ArrayList<ChatMessage>();
//        messages.add(m);
//        Journey j = new Journey(l,start,users);
//        String journeyId = FirebaseDB.addNewJourney(j);
//        //FirebaseDB.addMessageToJourney(journeyId, m);
//        //FirebaseDB.addMessageToJourney(journeyId, m2);
//        j.addMessage(m);
//        j.addMessage(m2);
//        j.addUser("Paddy");
//        j.addUser("World");
//        FirebaseDB.updateJourney(journeyId, j);
        //Log.e(TAG, journey_history_ids[0]);
        mUser = FirebaseDB.getCurrentUser();
        List<String> userJourneyIDs = mUser.journeyIDs;
        Log.e(TAG,userJourneyIDs.get(0));
        adapter = new ArrayAdapter(getActivity().getApplicationContext(),
                R.layout.journey_history_list_item,journey_history_ids );
        listHistoryJourneys.setAdapter(adapter);
        //mView = inflater.inflate(R.layout.fragment_journey_history, container, false);
        return mView;
    }

}
