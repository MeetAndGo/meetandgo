package com.meetandgo.meetandgo;

import android.app.Activity;
import android.util.Log;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

/**
 * This static class is handling all the interactions with the Firebase database
 * In java only nested classes can be static so here all methods will be set to static
 */
public class FirebaseDB {
    private static final String TAG = "FirebaseDB";
    private static FirebaseAuth sAuth;
    public static FirebaseDatabase sDatabase;
    private static boolean sInitialised;

    public static void initializeApp(Activity activity) {
        FirebaseApp.initializeApp(activity);
        sAuth = FirebaseAuth.getInstance();
        sDatabase = FirebaseDatabase.getInstance();
        sInitialised = true;
    }

    public static String getCurrentUserUid() {
        FirebaseUser user = sAuth.getCurrentUser();
        if(user != null) return user.getUid();
        else return null;
    }

    /**
     * Give the current user, if the user does not exist on the database, we create the User with the
     * data from the FirebaseUser object.
     * @return the current user
     */
    public static User getCurrentUser() {
        String uid = getCurrentUserUid();
        FirebaseUser firebaseUser = sAuth.getCurrentUser();
        if (firebaseUser == null) return null;

        User newUser = new User(firebaseUser.getDisplayName(), firebaseUser.getEmail(), 0.0);
        final User[] user = {newUser};
        DatabaseReference databaseReference = sDatabase.getReference("users/" + uid);
        ValueEventListener valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                Log.d(TAG, snapshot.toString());
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
     * @return Boolean variable indicating if firebase has been initialized or not
     */
    public static boolean isFirebaseInitialised() {
        return sInitialised;
    }

    /**
     * Checks if the user is in database, not in the Authentication database but in the users one
     * @param uid String corresponding to the user unique id that needs to be checked
     * @return Boolean value indicating if the user was found or not
     */
    public static boolean isUserInDB(String uid) {
        final boolean[] result = {false};
        // References to the Database with the given userId is created
        DatabaseReference databaseReference = sDatabase.getReference("users/" + uid);
        databaseReference.removeValue();

        // ValueEventListener needed to get the return of asking the database for the user
        ValueEventListener valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.getValue(User.class) == null)  result[0] = false;
                else  result[0] = true;
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        };

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
}
