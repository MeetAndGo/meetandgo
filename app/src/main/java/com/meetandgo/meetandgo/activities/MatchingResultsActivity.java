package com.meetandgo.meetandgo.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.google.gson.Gson;
import com.meetandgo.meetandgo.Constants;
import com.meetandgo.meetandgo.FirebaseDB;
import com.meetandgo.meetandgo.R;
import com.meetandgo.meetandgo.data.Journey;
import com.meetandgo.meetandgo.data.Search;
import com.meetandgo.meetandgo.utils.SearchUtil;
import com.meetandgo.meetandgo.views.MatchingResultsAdapter;
import com.meetandgo.meetandgo.views.OnItemClickListener;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;
import com.squareup.otto.ThreadEnforcer;
import com.yarolegovich.lovelydialog.LovelyStandardDialog;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MatchingResultsActivity extends AppCompatActivity {

    private static final String TAG = MatchingResultsActivity.class.getSimpleName();
    @BindView(R.id.recyclerView)
    RecyclerView mRecyclerView;
    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    private LinearLayoutManager mLayoutManager;
    private MatchingResultsAdapter mAdapter;
    private ArrayList<Search> orderedSearches = new ArrayList<>();
    private OnItemClickListener listener;
    public static Bus bus;
    private Search mCurrentUserSearch;
    private ArrayList<Search> mSearches = new ArrayList<>();


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_matching_results);
        ButterKnife.bind(this);

        bus = new Bus(ThreadEnforcer.MAIN);
        bus.register(this);

        String json = getIntent().getStringExtra("currentUserSearch");

        Gson gson = new Gson();
        mCurrentUserSearch = gson.fromJson(json, Search.class);
        Log.d(TAG, mCurrentUserSearch.getUserId());

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mRecyclerView.setHasFixedSize(true);

        // use a linear layout manager
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        // specify an adapter (see also next example)
        listener = new OnItemClickListener() {
            @Override
            public void onItemClick(Object object) {
                Search search = (Search) object;
                Log.d(TAG, "clicked " + search.getUserId());
                askConfirmation(search);

            }
        };
        mAdapter = new MatchingResultsAdapter(orderedSearches, listener);
        mRecyclerView.setAdapter(mAdapter);

        //Matching algorithm
        FirebaseDB.retrieveSearchesBySearch(bus, mCurrentUserSearch);

        setUpToolbar();
    }

    /**
     * We add the journey id to the users that are currently in the journey, this is useful for having
     * the journey in the history of journeys fragment and be able to access the chats.
     *
     * @param journey
     */
    private void updateJourneyUsers(Journey journey) {
        for(String userID : journey.getUsers()){
            FirebaseDB.addJourneyToUser(userID, journey);
        }
    }

    private void startChatFragment(Journey journey) {
        Intent mainActivityIntent = new Intent(this, MainActivity.class);
        Gson gson = new Gson();
        String json = gson.toJson(journey);
        mainActivityIntent.putExtra(Constants.JOURNEY_EXTRA, json);
        mainActivityIntent.putExtra(Constants.JOURNEY_ACTIVITY_EXTRA, "journey_activity");
        startActivity(mainActivityIntent);
        finish();
    }

    /**
     * Get user's confirmation to join journey
     * @param search
     */
    private void askConfirmation(final Search search) {

        new LovelyStandardDialog(this, LovelyStandardDialog.ButtonLayout.HORIZONTAL)
                .setTopColorRes(R.color.colorPrimaryDark)
                .setButtonsColorRes(R.color.colorPrimary)
                // TODO: Change Icon to something related to journeys
                .setIcon(R.drawable.ic_face_white_48dp)
                .setMessage(R.string.confirmation_message)
                .setPositiveButton(android.R.string.yes, new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Journey journey = createJourney(search);
                        updateJourneyUsers(journey);
                        startChatFragment(journey);

                    }
                })
                .setNegativeButton(android.R.string.no, null)
                .show();
    }

    /**
     * Create journey (when clicking on match)
     *
     * @param search (selected match)
     */
    public Journey createJourney(Search search) {
        List<String> users = new ArrayList<String>();
        users.add(search.getUserId());
        users.add(mCurrentUserSearch.getUserId());
        Journey journey = new Journey(search.getStartLocation(), mCurrentUserSearch.getStartLocationString(),
                new Date().getTime(), users);
        String journeyKey = FirebaseDB.addNewJourney(journey);
        FirebaseDB.updateJourney(journeyKey, journey);
        return journey;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return false;
    }

    /**
     * The method with the @Subscribe decorator is called when we post from the bus object
     *
     * @param search
     */
    @Subscribe
    public void nextMethod(Search search) {
        Log.e(TAG, search.getUserId());
        mSearches.add(search);

        orderedSearches = SearchUtil.calculateSearch(mSearches, mCurrentUserSearch);
        mAdapter.setListOfSearches(orderedSearches);

        Log.e(TAG, String.valueOf(orderedSearches.size()));

    }

    /**
     * Sets up the toolbar
     */
    private void setUpToolbar() {
        setSupportActionBar(mToolbar);
        mToolbar.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.toolbarColor));
        int statusBarColor = ContextCompat.getColor(getApplicationContext(), R.color.colorPrimaryDark);
        Window window = getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(statusBarColor);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle(R.string.matching_results);
    }


}
