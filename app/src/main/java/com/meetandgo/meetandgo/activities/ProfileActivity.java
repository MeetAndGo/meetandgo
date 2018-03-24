package com.meetandgo.meetandgo.activities;

import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;
import android.widget.RatingBar;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.meetandgo.meetandgo.FireBaseDB;
import com.meetandgo.meetandgo.R;
import com.meetandgo.meetandgo.data.User;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Profile Activity that shows user details
 */
public class ProfileActivity extends AppCompatActivity {
    private static final String TAG = "ProfileActivity";

    @BindView(R.id.user_name) TextView mTextViewUserName;
    @BindView(R.id.user_email) TextView mTextViewUserEmail;
    @BindView(R.id.number_of_ratings) TextView mTextViewNumberOfRatings;
    @BindView(R.id.rating) RatingBar mRatingBarRating;
    @BindView(R.id.toolbar) Toolbar mToolbar;
    @BindView(R.id.number_of_trips) TextView mNumberOfTrips;
    @BindView(R.id.user_add_to_group) TextView mAddToGroup;
    @BindView(R.id.initials) TextView mTextViewInitials;

    private ValueEventListener mUserValueEventListener;

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        ButterKnife.bind(this);
        setUpToolbar();
        setUpUser();
    }

    /**
     * Sets up the
     */
    private void setUpUser() {
        final User currentUser = FireBaseDB.getCurrentUser();

        mUserValueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                Log.e(TAG, "in OnDataChange from event listener" + snapshot.toString());
                User user = snapshot.getValue(User.class);
                if (user != null) {
                    setRatingBarInfo(user);
                    setNumberOfTrips(user);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        };

        // We check if the user is in the database and then execute the method that is in the
        // value event listener.
        FireBaseDB.isUserInDB(FireBaseDB.getCurrentUserID(), mUserValueEventListener);
        if (currentUser == null) return;
        mTextViewUserName.setText(currentUser.getFullName());
        mTextViewUserEmail.setText(currentUser.getEmail());

        // Set Initials of the image
        String initials = "";
        for (String s : currentUser.getFullName().split(" ")) {
            initials += s.charAt(0);
        }
        initials.substring(0, 2);
        mTextViewInitials.setText(initials);
        String gender = currentUser.getGender().toString();

        // Change string on the profile based on the gender
        switch (gender) {
            case "FEMALE":
                mAddToGroup.setText(R.string.gender_female);
                break;
            case "MALE":
                mAddToGroup.setText(R.string.gender_male);
                break;
            default:
                mAddToGroup.setText(R.string.any);
                break;
        }
    }

    /**
     * Sets up the info of the ratings of the user
     *
     * @param user User object to retrieve the ratings
     */
    private void setRatingBarInfo(User user) {
        //Check if it its already logged in
        mRatingBarRating.setRating((float) user.getRating());
        mTextViewNumberOfRatings.setText(getString(R.string.number_of_ratings, user.getNumOfRatings()));
    }

    /**
     * Sets the number of trips string in the profile page
     *
     * @param user User object with data
     */
    public void setNumberOfTrips(User user) {
        mNumberOfTrips.setText(getString(R.string.number_of_trips, user.getNumOfTrips()));
    }

    /**
     * Sets up the toolbar and all the components of it, like color and title
     */
    private void setUpToolbar() {
        setSupportActionBar(mToolbar);
        mToolbar.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.toolbarColor));
        int statusBarColor = ContextCompat.getColor(getApplicationContext(), R.color.colorPrimaryDark);
        Window window = getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(statusBarColor);
        if (getSupportActionBar() == null) return;
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle(R.string.profile);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
        overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right);
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
     * When we destroy the app, we remove the event listener in order to not update this activity
     * anymore with the information of the user.
     */
    @Override
    protected void onDestroy() {
        DatabaseReference databaseReference = FireBaseDB.getUserDatabaseReference(FireBaseDB.getCurrentUserID());
        databaseReference.removeEventListener(mUserValueEventListener);
        super.onDestroy();
    }

}
