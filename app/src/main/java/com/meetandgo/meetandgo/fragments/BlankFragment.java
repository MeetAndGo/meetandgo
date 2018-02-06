package com.meetandgo.meetandgo.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.meetandgo.meetandgo.R;

public class BlankFragment extends android.support.v4.app.Fragment {

    private View mView;

    public BlankFragment() {}

    public static Fragment newInstance() {
        BlankFragment fragment = new BlankFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_blank, container, false);
        return mView;
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }
}
