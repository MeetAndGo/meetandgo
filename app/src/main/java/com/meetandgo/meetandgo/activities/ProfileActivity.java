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
    @BindView(R.id.user_name) TextView mTextViewUserName;
    @BindView(R.id.user_email) TextView mTextViewUserEmail;
    @BindView(R.id.number_of_ratings) TextView mTextViewNumberOfRatings;
    @BindView(R.id.rating) RatingBar mRatingBarRating;
    @BindView(R.id.toolbar) Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        ButterKnife.bind(this);
        setUpToolbar();

        final User currentUser = FirebaseDB.getCurrentUser();

        ValueEventListener valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                Log.e(TAG, "in ondatachange from event listener" + snapshot.toString());
                if (snapshot.getValue(User.class) != null) {
                    //Check if it its already logged in
                    mRatingBarRating.setRating((float) snapshot.getValue(User.class).mRating);
                    mTextViewNumberOfRatings.setText(snapshot.getValue(User.class).mNumOfRatings + "");
                    if (snapshot.getValue(User.class).mRating >= 4 && snapshot.getValue(User.class).mNumOfRatings >= 10) {
                        //mRatingBarRating.setProgressTintList(ColorStateList.valueOf(ContextCompat.getColor(getApplicationContext(), R.color.colorYellow)));
                        //mRatingBarRating.setSecondaryProgressTintList(ColorStateList.valueOf(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimaryDark)));
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        };
        FirebaseDB.isUserInDB(FirebaseDB.getCurrentUserUid(), valueEventListener);
        mTextViewUserName.setText(currentUser.mFullName);
        mTextViewUserEmail.setText(currentUser.mEmail);
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

    @Override public void onBackPressed() {
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
}
