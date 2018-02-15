package com.meetandgo.meetandgo.activities;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
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

public class ProfileActivity extends AppCompatActivity {
    private static final String TAG = "ProfileActivity";
    @BindView(R.id.user_name) TextView mTextViewUserName;
    @BindView(R.id.user_email) TextView mTextViewUserEmail;
    @BindView(R.id.number_of_ratings) TextView mTextViewNumberOfRatings;
    @BindView(R.id.rating) RatingBar mRatingBarRating;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        ButterKnife.bind(this);
        final User currentUser = FirebaseDB.getCurrentUser();


        ValueEventListener valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                Log.e(TAG, "in ondatachange from event listener" + snapshot.toString());
                if (snapshot.getValue(User.class) != null)
                {
                    //Check if it its already logged in
                    mRatingBarRating.setRating((float)snapshot.getValue(User.class).mRating);
                    mTextViewNumberOfRatings.setText(snapshot.getValue(User.class).mNumOfRatings + "");
                    if(snapshot.getValue(User.class).mRating >= 4 && snapshot.getValue(User.class).mNumOfRatings >= 10)
                    {
                        mRatingBarRating.setProgressTintList(ColorStateList.valueOf(Color.YELLOW));
                        mRatingBarRating.setSecondaryProgressTintList(ColorStateList.valueOf(Color.MAGENTA));
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


}
