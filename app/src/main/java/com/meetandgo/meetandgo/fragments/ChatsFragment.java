package com.meetandgo.meetandgo.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.meetandgo.meetandgo.R;

public class ChatsFragment extends Fragment {

    private static final String TAG = "ChatsFragment";

    public ChatsFragment() {}

    public static Fragment newInstance() {
        ChatsFragment fragment = new ChatsFragment();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_chats, container, false);
    }

}
