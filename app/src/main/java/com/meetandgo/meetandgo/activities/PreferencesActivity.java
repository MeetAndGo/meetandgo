package com.meetandgo.meetandgo.activities;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.PreferenceFragment;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.widget.Toolbar;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import com.meetandgo.meetandgo.R;
import com.meetandgo.meetandgo.data.Preferences;
import com.meetandgo.meetandgo.fragments.PreferencesFragment;

import butterknife.BindView;
import butterknife.ButterKnife;

public class PreferencesActivity extends AppCompatActivity {

    private static final String TAG = "PreferencesActivity";
    private AppCompatDelegate mDelegate;

    @BindView(R.id.toolbar) Toolbar mToolbar;

    PreferenceFragment preferencesFragment;

    Preferences mPreferences = new Preferences();
    @Override
    protected void onStart() {
        super.onStart();
        mToolbar.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.toolbarColor));
        int statusBarColor = ContextCompat.getColor(getApplicationContext(), R.color.colorPrimaryDark);
        Window window = getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(statusBarColor);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preferences);
        ButterKnife.bind(this);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle(R.string.title_activity_preferences);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        preferencesFragment = new PreferencesFragment();
        getFragmentManager().beginTransaction().replace(R.id.content_frame, preferencesFragment).commit();


    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        getDelegate().onPostCreate(savedInstanceState);
    }
    public ActionBar getSupportActionBar() {
        return getDelegate().getSupportActionBar();
    }
    public void setSupportActionBar(@Nullable Toolbar toolbar) {
        getDelegate().setSupportActionBar(toolbar);
    }
    @Override
    public MenuInflater getMenuInflater() {
        return getDelegate().getMenuInflater();
    }

    @Override
    public void setContentView(View view) {
        getDelegate().setContentView(view);
    }
    @Override
    public void setContentView(View view, ViewGroup.LayoutParams params) {
        getDelegate().setContentView(view, params);
    }
    @Override
    public void addContentView(View view, ViewGroup.LayoutParams params) {
        getDelegate().addContentView(view, params);
    }
    @Override
    protected void onPostResume() {
        super.onPostResume();
        getDelegate().onPostResume();
    }
    @Override
    protected void onTitleChanged(CharSequence title, int color) {
        super.onTitleChanged(title, color);
        getDelegate().setTitle(title);
    }
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        getDelegate().onConfigurationChanged(newConfig);
    }
    @Override
    protected void onStop() {
        super.onStop();
        getDelegate().onStop();
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        getDelegate().onDestroy();
    }
    public void invalidateOptionsMenu() {
        getDelegate().invalidateOptionsMenu();
    }

    @Override public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(this,MainActivity.class);
        intent.putExtra("journeyPreferences", mPreferences);
        startActivity(intent);
        savePreferences();
        finish();
        overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right);
    }

    /**
     * Save Preferences from fragment into preference object
     */
    private void savePreferences() {

        ListPreference pref = (ListPreference) preferencesFragment.findPreference("genderType");
        if(pref.getEntry().equals("Male")) {
            mPreferences.setGender(Preferences.Gender.MALE);
        }
        else if(pref.getEntry().equals("Female")) {
            mPreferences.setGender(Preferences.Gender.FEMALE);
        }
        else{
            mPreferences.setGender(Preferences.Gender.ANY);
        }

        pref = (ListPreference) preferencesFragment.findPreference("journeyType");
        if(pref.getEntry().equals("Walk")) {
            mPreferences.setMode(Preferences.Mode.WALK);
        }
        else if(pref.getEntry().equals("Car")) {
            mPreferences.setMode(Preferences.Mode.CAR);
        }
        else if(pref.getEntry().equals("Taxi")) {
            mPreferences.setMode(Preferences.Mode.TAXI);
        }
        else{
            mPreferences.setMode(Preferences.Mode.ANY);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return false;
    }



}