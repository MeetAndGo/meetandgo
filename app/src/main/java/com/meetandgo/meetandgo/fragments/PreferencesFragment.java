package com.meetandgo.meetandgo.fragments;

import android.os.Bundle;
import android.preference.PreferenceFragment;

import com.meetandgo.meetandgo.R;

public class PreferencesFragment extends PreferenceFragment{
    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
    }
}
