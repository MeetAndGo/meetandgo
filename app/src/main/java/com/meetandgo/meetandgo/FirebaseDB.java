package com.meetandgo.meetandgo;

import android.app.Activity;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.meetandgo.meetandgo.data.ChatMessage;
import com.meetandgo.meetandgo.data.Journey;
import com.meetandgo.meetandgo.data.Search;
import com.meetandgo.meetandgo.data.User;

import java.text.DecimalFormat;
import java.util.Map;
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
    private static DatabaseReference sUsersDatabaseReference;
    private static DatabaseReference sSearchDatabase;
    private static DatabaseReference sJourneyDatabase;
    private final CountDownLatch loginLatch = new CountDownLatch(1);

    public static void initializeApp(Activity activity) {
        FirebaseApp.initializeApp(activity);
        sAuth = FirebaseAuth.getInstance();
        initializeDatabaseReferences();
        sInitialised = true;
    }

    // TODO: Refactor
    private static void initializeDatabaseReferences() {
        String uid = getCurrentUserUid();
        if(sDatabase == null){
            sDatabase = FirebaseDatabase.getInstance();
            sDatabase.setPersistenceEnabled(true);
        }
        if (uid != null) {
            sUsersDatabaseReference = getUserDatabaseReference(uid);
            // Set the keepSync method to true in order to have local storage
            sUsersDatabaseReference.keepSynced(true);
        }

        sSearchDatabase = sDatabase.getReference("search");
        sSearchDatabase.keepSynced(true);

        sJourneyDatabase = sDatabase.getReference("journeys");
        sJourneyDatabase.keepSynced(true);
    }

    /**
     * Creates the reference to the database using the string user id
     *
     * @param uid
     * @return DatabaseReference that refers to the users/uid database
     */
    public static DatabaseReference getUserDatabaseReference(String uid) {
            sUsersDatabaseReference = sDatabase.getReference("users/" + uid);
        return sUsersDatabaseReference;
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
        ValueEventListener valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                user[0] = snapshot.getValue(User.class);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        };

        getUserDatabaseReference(uid).addListenerForSingleValueEvent(valueEventListener);
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
            getUserDatabaseReference(uid).setValue(newUser);
            return true;
        }
        return false;
    }

    /**
     * Add a user Search to Firebase Database
     *
     * @return String, return the key of this search in firebase database, if unsuccessful in
     * adding Search return empty string
     */
    public static String addSearch(Search newSearch) {
        if (!isFirebaseInitialised()) return null;
        if (newSearch != null) {
            sSearchDatabase.push().setValue(newSearch);
            return sSearchDatabase.getKey();
        }
        return null;
    }


    /**
     * Add a Journey to Firebase Database
     *
     * @return String, return the key of this search in firebase database, if unsuccessful in
     * adding Search return empty string
     */
    public static String addNewJourney(Journey journey) {
        if (!isFirebaseInitialised()) return "";
        if (journey != null) {
            DatabaseReference journeyEnrty = sJourneyDatabase.push();
            journeyEnrty.setValue(journey);
            return journeyEnrty.getKey();
        }
        return "";
    }

    /**
     * TODO: MAAAAAAAAAAAAAAAANUUUUUUUUUUU
     * @param journey
     * @return
     */
    public static String updateJourney(String jID, Journey journey) {
        if (!isFirebaseInitialised()) return "";
        if (jID != null && journey != null) {
            DatabaseReference journeyEnrty = sDatabase.getReference("journeys/" + jID);
            journeyEnrty.setValue(journey);
            return journeyEnrty.getKey();
        }
        return "";
    }

    /**
     * @param jID journey ID
     * @param uID user ID
     * @return boolean, return true if addition of user to journey successful
     */
    public static boolean addUserToJourney(String jID, String uID) {
        if (!isFirebaseInitialised()) return false;
        if (jID != null && uID != null) {
            DatabaseReference databaseReference = sDatabase.getReference("journeys/" + jID + "/mUsers/" + uID);
            databaseReference.setValue(true);
            //updateChatInUser(uid, databaseReference.getKey());

            return true;
        }
        return false;
    }

    /**
     * #TODO MAAAAANNNUUUUUUUU, can you buy cookies?
     * @param jID
     * @param message
     * @return
     */
    public static boolean addMessageToJourney(String jID, ChatMessage message) {
        if (!isFirebaseInitialised()) return false;
        if (jID != null && message != null) {
            DatabaseReference databaseReference = sDatabase.getReference("journeys/" + jID + "/mMessages/");
            databaseReference.push().setValue(message);
            //updateChatInUser(uid, databaseReference.getKey());

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
     *
     * @param uid String corresponding to the user unique id that needs to be checked
     * @return Boolean value indicating if the user was found or not
     */
    public static boolean isUserInDB(String uid, ValueEventListener valueEventListener) {
        final boolean[] result = {false};
        // Run the search for the user based on his userId
        getUserDatabaseReference(uid).addValueEventListener(valueEventListener);
        return result[0];
    }

    /**
     * Removes a user from the Firebase Database
     *
     * @param uid ID of user to be removed
     */
    public static void removeUser(String uid) {
        // Remove user from the "users" database
        getUserDatabaseReference(uid).removeValue();
        // Remove user from the auth database
        FirebaseUser firebaseUser = sAuth.getCurrentUser();
        firebaseUser.delete();
    }


    /**
     * Adds rating to user uid
     * Note: if several addRating() launched sequentially, risk of over writing the previous call
     *
     * @param uid    String corresponding to the user unique id that is rated
     * @param rating The rating that is given
     */
    public static void addRating(final String uid, final int rating) {
        if (uid == null) return;
        ValueEventListener valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                User userToRate = snapshot.getValue(User.class);
                userToRate.numOfRatings++;
                userToRate.rating = ((userToRate.rating * (userToRate.numOfRatings - 1))
                        + rating) / userToRate.numOfRatings;
                //trim the rating number to 2 decimal values
                DecimalFormat df = new DecimalFormat("#.##");
                userToRate.rating = Double.parseDouble(df.format(userToRate.rating));
                getUserDatabaseReference(uid).setValue(userToRate);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        };
        getUserDatabaseReference(uid).addListenerForSingleValueEvent(valueEventListener);
    }

    public static void getRating(String uid, final User user) {

        ValueEventListener valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                User DatabaseUser = snapshot.getValue(User.class);
                user.rating = DatabaseUser.rating;
                user.numOfRatings = DatabaseUser.numOfRatings;
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        };
        getUserDatabaseReference(uid).addListenerForSingleValueEvent(valueEventListener);
    }

    /**
     * Whenever the class is sent to the server the timestamp is updated. See the data/Search class
     * for reference on how to use it
     * @return
     */
    public static Map<String,String> getServerTime() {
        return ServerValue.TIMESTAMP;
    }
}
