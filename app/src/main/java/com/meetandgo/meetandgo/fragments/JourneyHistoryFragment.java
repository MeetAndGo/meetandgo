package com.meetandgo.meetandgo.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.meetandgo.meetandgo.FirebaseDB;
import com.meetandgo.meetandgo.R;
import com.meetandgo.meetandgo.data.User;

import java.util.Arrays;
import java.util.List;

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


        mUser = FirebaseDB.getCurrentUser();
        List<String> userJourneyIDs = mUser.journeyIDs;
        userJourneyIDs.add("Viva Espana");
        userJourneyIDs.add("Balamory");
        userJourneyIDs.add("France Never Wins");
        Log.e(TAG, mUser.journeyIDs.get(0));
        if(userJourneyIDs.size() > 0) {
            Log.e(TAG, userJourneyIDs.get(0));
            adapter = new ArrayAdapter(getActivity().getApplicationContext(),
                    R.layout.journey_history_list_item, userJourneyIDs);
            listHistoryJourneys.setAdapter(adapter);

            //mView = inflater.inflate(R.layout.fragment_journey_history, container, false);
        }
        return mView;
    }

}
