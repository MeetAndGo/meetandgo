package com.meetandgo.meetandgo.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.meetandgo.meetandgo.FireBaseDB;
import com.meetandgo.meetandgo.R;
import com.meetandgo.meetandgo.activities.MainActivity;
import com.meetandgo.meetandgo.data.Journey;
import com.meetandgo.meetandgo.data.User;
import com.meetandgo.meetandgo.views.JourneyHistoryAdapter;
import com.meetandgo.meetandgo.views.OnItemClickListener;
import com.meetandgo.meetandgo.views.RatingItemAdapter;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;
import com.squareup.otto.ThreadEnforcer;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import me.zhanghai.android.materialratingbar.MaterialRatingBar;

public class JourneyHistoryFragment extends Fragment implements View.OnCreateContextMenuListener {

    private static final String TAG = JourneyHistoryFragment.class.getSimpleName();

    private View view;
    @BindView(R.id.recyclerView)
    RecyclerView mRecyclerView;
    @BindView(R.id.swipeRefresh)
    SwipeRefreshLayout mSwipeRefreshLayout;
    private LinearLayoutManager mLayoutManager;
    private JourneyHistoryAdapter mAdapter;
    private ArrayList<Journey> mJourneyHistory = new ArrayList<>();
    private Journey mClickedJourney;
    public static Bus mBus;
    private User mUser;
    private ValueEventListener mValueEventListener;
    private ValueEventListener mNewUserValueEventListener;
    private OnItemClickListener mOnItemClickListener;
    private ContextMenu mContextMenu;
    private MaterialDialog mMenuDialog;
    private MaterialDialog mRatingDialog;
    private RatingItemAdapter mRatingAdapter;
    private LinearLayoutManager mLinearLayoutManager;


    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_journey_history, container, false);
        ButterKnife.bind(this, view);

        mBus = new Bus(ThreadEnforcer.MAIN);
        mBus.register(this);


        setUpEventListener();
        setUpUI();
        return view;
    }

    /**
     * Sets up the event listener for retrieving data from the Firebase
     */
    private void setUpEventListener() {
        mValueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(com.google.firebase.database.DataSnapshot dataSnapshot) {
                Journey journey = dataSnapshot.getValue(Journey.class);
                if (journey != null) mAdapter.add(journey);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }

        };

        mNewUserValueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                if (user == null) return;
                addUserToRatingAdapter(user);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
    }

    /**
     * Sets up UI by getting the user and updating the RecyclerView after it has been updated
     */
    private void setUpUI() {
        setUpRecyclerView();
        FireBaseDB.getCurrentUser(mBus);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                FireBaseDB.getCurrentUser(mBus);
                mSwipeRefreshLayout.setRefreshing(false);
            }
        });

        mRatingAdapter = new RatingItemAdapter(new ArrayList<User>());
        mLinearLayoutManager = new LinearLayoutManager(getContext());
    }

    /**
     * Sets up the RecyclerView element and the listener of its items
     */
    private void setUpRecyclerView() {
        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mRecyclerView.setHasFixedSize(true);

        // use a linear layout manager
        mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);

        mOnItemClickListener = new OnItemClickListener() {
            @Override
            public void onItemClick(Object object) {
                Journey journey = (Journey) object;
                startChatFragment(journey);
            }

            @Override
            public void onItemLongClick(Object object) {
                Journey journey = (Journey) object;
                openLongPressedDialog(journey);
            }
        };

        // specify an adapter (see also next example)
        mAdapter = new JourneyHistoryAdapter(mJourneyHistory, mOnItemClickListener);
        mRecyclerView.setAdapter(mAdapter);
    }

    /**
     * Opens the long pressed dialog with the options of the journey pressed
     *
     * @param journey Journey pressed on the list
     */
    public void openLongPressedDialog(final Journey journey) {
        mClickedJourney = journey;
        mMenuDialog = new MaterialDialog.Builder(getActivity())
                .customView(R.layout.journey_pressed_menu, true)
                .show();

        View customView = mMenuDialog.getCustomView();
        if (customView == null) return;
        setUpLongPressDialog(journey, customView);

    }

    /**
     * Setups all the information of the long press dialoag, including the hided items and the
     * on click listeners.
     *
     * @param journey    Journey pressed
     * @param customView View of the dialog
     */
    private void setUpLongPressDialog(final Journey journey, View customView) {
        // If the journey is active we dont show the rate option
        if (journey.isActive()) {
            customView.findViewById(R.id.rate_item).setVisibility(View.GONE);
        }

        // Listener that opens the rating dialog of the journey if it's active
        customView.findViewById(R.id.rate_item).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mRatingAdapter.clean();
                for (String userID : journey.getUsers()) {
                    if (!userID.equals(FireBaseDB.getCurrentUserID())) {
                        FireBaseDB.getUser(userID, mNewUserValueEventListener);
                    }
                }
                mMenuDialog.dismiss();
            }
        });
        // On Click listener for deleting a journey from your list
        customView.findViewById(R.id.delete_item).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mAdapter.deleteJourney(journey);
                FireBaseDB.deleteJourneyFromUser(FireBaseDB.getCurrentUserID(), journey.getJourneyID());
                FireBaseDB.getCurrentUser(mBus);
                mMenuDialog.dismiss();
            }
        });
    }

    /**
     * Opens the rating dialog, letting the user rate the different journey members
     */
    private void openRatingDialog() {
        mLinearLayoutManager = new LinearLayoutManager(getActivity());
        mRatingDialog = new MaterialDialog.Builder(getActivity())
                // second parameter is an optional layout manager. Must be a LinearLayoutManager or GridLayoutManager.
                .adapter(mRatingAdapter, mLinearLayoutManager)
                .positiveText(android.R.string.ok)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        rateUsersInJourney();
                        ((MainActivity) getActivity()).runKonfettiAnimation();
                        dialog.dismiss();
                    }
                })
                .negativeText(android.R.string.cancel)
                .show();
    }

    /**
     * Goes through all the users in the journey and adds them the rating that the user has
     * put in the rating bar for them.
     */
    private void rateUsersInJourney() {
        int itemPos = 0;
        for (String userID : mClickedJourney.getUsers()) {
            if (!userID.equals(FireBaseDB.getCurrentUserID())) {
                View view = mLinearLayoutManager.findViewByPosition(itemPos);
                MaterialRatingBar ratingBar = view.findViewById(R.id.rating);
                FireBaseDB.addRating(userID, ratingBar.getRating());
                itemPos++;
            }
        }
    }

    /**
     * Adds the user to the list of users in the material dialog adapter
     *
     * @param user User loaded
     */
    private void addUserToRatingAdapter(User user) {
        // We add the user to the list adapter of the Recycler View
        mRatingAdapter.addUser(user);
        // When all the users are loaded we run the dialog
        if (mRatingAdapter.getItemCount() == mClickedJourney.getUsers().size() - 1) {
            openRatingDialog();
        }
    }


    @Subscribe
    public void userLoadedListener(User user) {
        mUser = user;
        FireBaseDB.getJourneys(user.getJourneyIDs(), mValueEventListener);
        mSwipeRefreshLayout.setRefreshing(false);
    }

    private void startChatFragment(Journey journey) {
        ((MainActivity) getActivity()).openChatFragment(journey);
    }
}
