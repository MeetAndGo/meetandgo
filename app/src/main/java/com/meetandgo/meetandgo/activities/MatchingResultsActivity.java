package com.meetandgo.meetandgo.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.firebase.client.Firebase;
import com.google.gson.Gson;
import com.meetandgo.meetandgo.Constants;
import com.meetandgo.meetandgo.FireBaseDB;
import com.meetandgo.meetandgo.R;
import com.meetandgo.meetandgo.data.Journey;
import com.meetandgo.meetandgo.data.Search;
import com.meetandgo.meetandgo.utils.SearchUtil;
import com.meetandgo.meetandgo.views.MatchingResultsAdapter;
import com.meetandgo.meetandgo.views.OnItemClickListener;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;
import com.squareup.otto.ThreadEnforcer;

import java.util.ArrayList;
import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.meetandgo.meetandgo.fragments.JourneyHistoryFragment.mBus;

public class MatchingResultsActivity extends AppCompatActivity {

    private static final String TAG = MatchingResultsActivity.class.getSimpleName();
    @BindView(R.id.recyclerView)
    RecyclerView mRecyclerView;
    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.swipeRefresh)
    SwipeRefreshLayout mSwipeRefreshLayout;
    private MatchingResultsAdapter mAdapter;
    private ArrayList<Search> mOrderedSearches = new ArrayList<>();
    private Search mCurrentUserSearch;
    private ArrayList<Search> mSearches = new ArrayList<>();


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_matching_results);
        ButterKnife.bind(this);

        // Register the mBus that will be called when a new search is found
        mBus = new Bus(ThreadEnforcer.MAIN);
        mBus.register(this);

        mCurrentUserSearch = getSearchFromCallingActivity();
        //Log.d(TAG, mCurrentUserSearch.getUserID());
        setUpUI();

        FireBaseDB.retrieveSearchesBySearch(mBus, mCurrentUserSearch);

    }

    /**
     * Set Ups all the UI elements of the activity, and all the listeners of the views
     */
    private void setUpUI() {
        setUpRecyclerView();
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mBus = new Bus(ThreadEnforcer.MAIN);
                mBus.register(this);

                FireBaseDB.retrieveSearchesBySearch(mBus, mCurrentUserSearch);
                mSwipeRefreshLayout.setRefreshing(false);
            }
        });
        setUpToolbar();
    }

    /**
     * Starts and setups the RecyclerView and click listeners of the elements on the list
     */
    private void setUpRecyclerView() {
        // Use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mRecyclerView.setHasFixedSize(true);

        // use a linear layout manager
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(layoutManager);

        // specify an adapter (see also next example)
        OnItemClickListener listener = new OnItemClickListener() {
            @Override
            public void onItemClick(Object object) {
                Search search = (Search) object;
                askConfirmation(search);
            }

            @Override
            public void onItemLongClick(Object object) {
            }
        };
        mAdapter = new MatchingResultsAdapter(mOrderedSearches, listener);
        mRecyclerView.setAdapter(mAdapter);
    }

    /**
     * From the activity that is calling this one, we should get the search that was made to start
     * the matching results activity.
     *
     * @return Search object that was sent to this activity
     */
    private Search getSearchFromCallingActivity() {
        String json = getIntent().getStringExtra(Constants.CURRENT_USER_SEARCH);
        Gson gson = new Gson();
        return gson.fromJson(json, Search.class);
    }

    /**
     * We add the journey id to the users that are currently in the journey, this is useful for having
     * the journey in the history of journeys fragment and be able to access the chats.
     *
     * @param journey
     */
    private void updateJourneyUsers(Journey journey) {
        for (String userID : journey.getUsers()) {
            FireBaseDB.addJourneyToUser(userID, journey);
        }
    }

    /**
     * Starts the MainActivity and from there the ChatFragment is activated, and also sends the
     * journey object that was created when clicking the search.
     *
     * @param journey
     */
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
     *
     * @param search Search object in order to combine the searches into just one
     */
    private void askConfirmation(final Search search) {
        new MaterialDialog.Builder(this)
                .content(R.string.confirmation_message)
                .positiveText(android.R.string.yes)
                .negativeText(android.R.string.no)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        Log.d(TAG, search.getJourneyID());
                        Journey journey = createJourney(search);
                        search.setJourneyID(journey.getJourneyID());
                        updateJourneyUsers(journey);
                        combineAndUpdateSearch(search, journey);
                        startChatFragment(journey);
                    }
                })
                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        dialog.dismiss();
                    }
                })
                .show();
    }

    /**
     * Create journey (when clicking on match), when the search has a journey attached to it, it won't
     * create a new journey but it will update the current journey adding the user to it.
     *
     * @param joinedSearch (selected match)
     */
    public Journey createJourney(Search joinedSearch) {
        ArrayList<String> users = new ArrayList<>();
        users.add(joinedSearch.getUserID());
        users.add(mCurrentUserSearch.getUserID());
        String journeyKey;
        Journey journey = new Journey(joinedSearch.getSearchID(), joinedSearch.getStartLocation(), joinedSearch.getStartLocationString(),
                new Date().getTime(), users);
        // If there is more than two users, and the journey has already been created
        if (joinedSearch.hasJourneyID()) {
            journeyKey = joinedSearch.getJourneyID();
            FireBaseDB.addUserToJourney(journeyKey, FireBaseDB.getCurrentUserID());
        } else {
            journeyKey = FireBaseDB.addNewJourney(journey);
        }
        journey.setJourneyID(journeyKey);
        return journey;
    }

    /**
     * Combines the two searches from the two or more users that are in the search adding the new user
     * and deleting the previous user search.
     */
    private void combineAndUpdateSearch(Search joinedSearch, Journey newJourney) {
        joinedSearch.addUser(mCurrentUserSearch.getUserID());
        joinedSearch.setJourneyID(newJourney.getJourneyID());
        FireBaseDB.deleteSearch(mCurrentUserSearch.getSearchID());
        FireBaseDB.updateSearch(joinedSearch);
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
     * The method with the @Subscribe decorator is called when we post from the mBus object
     *
     * @param search
     */
    @Subscribe
    public void nextMethod(Search search) {
            // If already exists on the list we dont add it.
            for (Search s : mSearches) {
                    if (s.getSearchID().equals(search.getSearchID())) return;
            }
            mSearches.add(search);
            mOrderedSearches = SearchUtil.calculateSearch(mSearches, mCurrentUserSearch);
            mAdapter.setListOfSearches(mOrderedSearches);
    }

    /**
     * Sets up the toolbar, the color and all its components
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
