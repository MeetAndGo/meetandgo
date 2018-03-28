package com.meetandgo.meetandgo.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.meetandgo.meetandgo.R;

/**
 * Meant to implement the commute mode screen
 */
public class CommuteFragment extends Fragment {

    private static final String TAG = "CommuteFragment";

    public CommuteFragment() {}

    public static Fragment newInstance() {
        CommuteFragment fragment = new CommuteFragment();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_commute, container, false);
    }

}
