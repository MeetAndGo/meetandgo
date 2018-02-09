package com.meetandgo.meetandgo.fragments;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.meetandgo.meetandgo.R;

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
        return inflater.inflate(R.layout.fragment_journey_history, container, false);
    }

}
