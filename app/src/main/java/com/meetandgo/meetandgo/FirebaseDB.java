package com.meetandgo.meetandgo;

import android.app.Activity;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.meetandgo.meetandgo.data.User;

import java.util.concurrent.CountDownLatch;

/**
 * This static class is handling all the interactions with the Firebase database
 * In java only nested classes can be static so here all methods will be set to static
 */
public class FirebaseDB {
    private static final String TAG = "FirebaseDB";
    private static FirebaseAuth sAuth;
    public static FirebaseDatabase sDatabase;
    private static boolean sInitialised;
    private final CountDownLatch loginLatch = new CountDownLatch (1);

    public static void initializeApp(Activity activity) {
        FirebaseApp.initializeApp(activity);
        sAuth = FirebaseAuth.getInstance();
        sDatabase = FirebaseDatabase.getInstance();
        sInitialised = true;
    }

    public static String getCurrentUserUid() {
        if (!isFirebaseInitialised()) return null;

        FirebaseUser user = sAuth.getCurrentUser();
        if (user != null) return user.getUid();
        else return null;
    }

    /**
     * Give the current user, if the user does not exist on the database, we create the User with the
     * data from the FirebaseUser object.
     *
     * @return the current user
     */
    public static User getCurrentUser() {
        if (!isFirebaseInitialised()) return null;

        String uid = getCurrentUserUid();
        FirebaseUser firebaseUser = sAuth.getCurrentUser();
        if (firebaseUser == null) return null;

        User newUser = new User(firebaseUser.getDisplayName(), firebaseUser.getEmail());
        final User[] user = {newUser};
        DatabaseReference databaseReference = sDatabase.getReference("users/" + uid);
        ValueEventListener valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                //Log.d(TAG, snapshot.toString());
                user[0] = snapshot.getValue(User.class);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        };

        databaseReference.addListenerForSingleValueEvent(valueEventListener);
        return user[0];
    }

    /**
     * Add user to Firebase Database
     *
     * @return boolean, true if user successfully added, false otherwise
     */
    public static boolean addUser(User newUser) {
        if (!isFirebaseInitialised()) return false;
        if (newUser != null) {
            String uid = getCurrentUserUid();
            DatabaseReference databaseReference = sDatabase.getReference("users/" + uid);
            databaseReference.setValue(newUser);
            return true;
        }
        return false;
    }


    /**
     * Check to see if firebase was initialized or not
     *
     * @return Boolean variable indicating if firebase has been initialized or not
     */
    public static boolean isFirebaseInitialised() {
        if (sAuth == null) return false;
        if (sDatabase == null) return false;
        return sInitialised;
    }

    /**
     * Checks if the user is in database, not in the Authentication database but in the users one
     * TODO: The result is running in other thread, the return will always be false.
     * //Do we really need that? Try doing callback one last time
     * @param uid String corresponding to the user unique id that needs to be checked
     * @return Boolean value indicating if the user was found or not
     */
    public static boolean isUserInDB(String uid, ValueEventListener valueEventListener) {

        final boolean[] result = {false};
        // References to the Database with the given userId is created
        DatabaseReference databaseReference = sDatabase.getReference("users/" + uid);
        // Run the search for the user based on his userId
        databaseReference.addListenerForSingleValueEvent(valueEventListener);
        return result[0];
    }

    public static void removeUser(String uid) {
        // Remove user from the "users" database
        DatabaseReference databaseReference = sDatabase.getReference("users/" + uid);
        databaseReference.removeValue();
        // Remove user from the auth database
        FirebaseUser firebaseUser = sAuth.getCurrentUser();
        firebaseUser.delete();
    }

    /**
     * Adds rating to user uid
     * Note: if several addRating() launched sequentially, risk of over writing the previous call
     * @param uid String corresponding to the user unique id that is rated
     * @param rating The rating that is given
     */
    public static void addRating(String uid, final int rating)
    {
        final DatabaseReference databaseReference = sDatabase.getReference("users/" + uid);
        ValueEventListener valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                User userToRate = snapshot.getValue(User.class);
                userToRate.mNumOfRatings ++;
                userToRate.mRating = ((userToRate.mRating * (userToRate.mNumOfRatings-1))
                        + rating)/userToRate.mNumOfRatings;
                databaseReference.setValue(userToRate);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        };
        databaseReference.addListenerForSingleValueEvent(valueEventListener);
    }
}
