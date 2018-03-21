package com.meetandgo.meetandgo.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.meetandgo.meetandgo.FirebaseDB;
import com.meetandgo.meetandgo.R;
import com.meetandgo.meetandgo.data.Journey;
import com.meetandgo.meetandgo.data.User;
import com.meetandgo.meetandgo.views.JourneyHistoryAdapter;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;
import com.squareup.otto.ThreadEnforcer;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class JourneyHistoryFragment extends Fragment {

    private static final String TAG = JourneyHistoryFragment.class.getSimpleName();

    private View view;
    @BindView(R.id.recyclerView) RecyclerView mRecyclerView;
    @BindView(R.id.swiperefresh) SwipeRefreshLayout mSwipeRefreshLayout;
    private LinearLayoutManager mLayoutManager;
    private JourneyHistoryAdapter mAdapter;
    private ArrayList<Journey> mJourneyHistory = new ArrayList<>();
    public static Bus bus;
    private User mUser;
    private ValueEventListener valueEventListener;

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_journey_history, container, false);
        ButterKnife.bind(this, view);

        bus = new Bus(ThreadEnforcer.MAIN);
        bus.register(this);

        setUpEventListener();
        setUpUI();
        return view;
    }

    private void setUpEventListener() {
        valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(com.google.firebase.database.DataSnapshot dataSnapshot) {
                Log.d(TAG, dataSnapshot.toString());
                Journey journey = dataSnapshot.getValue(Journey.class);
                Log.d(TAG, String.valueOf(journey));
                mAdapter.add(journey);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }

        };
    }

    private void setUpUI() {

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mRecyclerView.setHasFixedSize(true);

        // use a linear layout manager
        mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);

        // specify an adapter (see also next example)
        mAdapter = new JourneyHistoryAdapter(mJourneyHistory);
        mRecyclerView.setAdapter(mAdapter);

        mUser = FirebaseDB.getCurrentUser(bus);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mUser = FirebaseDB.getCurrentUser(bus);
            }
        });
    }

    @Subscribe
    public void userLoadedListener(User user) {
        mUser = user;
        Log.d(TAG, "Something sklajdfasdfas: " + user.journeyIDs.toString());
        FirebaseDB.getJourneys(user.journeyIDs, valueEventListener);
        mSwipeRefreshLayout.setRefreshing(false);

    }


}
