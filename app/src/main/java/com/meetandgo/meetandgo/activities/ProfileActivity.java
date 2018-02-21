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
import com.meetandgo.meetandgo.FirebaseDB;
import com.meetandgo.meetandgo.R;
import com.meetandgo.meetandgo.data.User;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Profile Activity that shows user details
 */
public class ProfileActivity extends AppCompatActivity {
    private static final String TAG = "ProfileActivity";
    @BindView(R.id.user_name)    TextView mTextViewUserName;
    @BindView(R.id.user_email)    TextView mTextViewUserEmail;
    @BindView(R.id.number_of_ratings)
    TextView mTextViewNumberOfRatings;
    @BindView(R.id.rating)
    RatingBar mRatingBarRating;
    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.number_of_trips) TextView mNumberOfTrips;
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

    private void setUpUser() {
        final User currentUser = FirebaseDB.getCurrentUser();

        mUserValueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                Log.e(TAG, "in ondatachange from event listener" + snapshot.toString());
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
        FirebaseDB.isUserInDB(FirebaseDB.getCurrentUserUid(), mUserValueEventListener);
        mTextViewUserName.setText(currentUser.mFullName);
        mTextViewUserEmail.setText(currentUser.mEmail);
    }

    private void setRatingBarInfo(User user) {
        //Check if it its already logged in
        mRatingBarRating.setRating((float) user.mRating);
        mTextViewNumberOfRatings.setText(getString(R.string.number_of_ratings, user.mNumOfRatings));
        if (user.mRating >= 4 && user.mNumOfRatings >= 10) {
            //mRatingBarRating.setProgressTintList(ColorStateList.valueOf(ContextCompat.getColor(getApplicationContext(), R.color.colorYellow)));
            //mRatingBarRating.setSecondaryProgressTintList(ColorStateList.valueOf(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimaryDark)));

        }
    }

    public void setNumberOfTrips(User user) {
        mNumberOfTrips.setText(getString(R.string.number_of_trips, user.mNumOfTrips));
    }

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

    @Override
    protected void onDestroy() {
        DatabaseReference databaseReference = FirebaseDB.getUserDatabaseReference(FirebaseDB.getCurrentUserUid());
        databaseReference.removeEventListener(mUserValueEventListener);
        super.onDestroy();
    }

}
