package com.meetandgo.meetandgo.fragments;

import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.util.Log;

import com.meetandgo.meetandgo.R;


public class PreferencesFragment extends PreferenceFragment{

    private static final String TAG = "PreferencesFragment";

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
        Log.e(TAG,"I resumed");
        setRetainInstance(true);
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
    }
}
