package com.meetandgo.meetandgo.fragments;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.meetandgo.meetandgo.R;

public class Commute extends Fragment {

    public Commute() {
        // Required empty public constructor
    }

    public static Fragment newInstance() {
        Commute fragment = new Commute();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_commute, container, false);
    }

}
