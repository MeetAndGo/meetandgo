package com.meetandgo.meetandgo;

import android.app.Activity;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.meetandgo.meetandgo.data.ChatMessage;
import com.meetandgo.meetandgo.data.Journey;
import com.meetandgo.meetandgo.data.Search;
import com.meetandgo.meetandgo.data.User;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import static android.content.Context.MODE_PRIVATE;

/**
 * This static class is handling all the interactions with the Firebase database
 * In java only nested classes can be static so here all methods will be set to static
 * TODO: Massive refactor - divide functions by object type by creating e.g. UserFirebaseDB
 */
public class FirebaseDB {
    private static final String TAG = "FirebaseDB";
    private static FirebaseAuth sAuth;
    public static FirebaseDatabase sDatabase;
    private static boolean sInitialised;
    private static DatabaseReference sUsersDatabaseReference;
    private static DatabaseReference sSearchDatabase;
    private static DatabaseReference sJourneyDatabase;
    private static SharedPreferences mPrefs;
    private final CountDownLatch loginLatch = new CountDownLatch(1);

    public static void initializeApp(Activity activity) {
        FirebaseApp.initializeApp(activity);
        sAuth = FirebaseAuth.getInstance();
        initializeDatabaseReferences();
        sInitialised = true;

        mPrefs = activity.getSharedPreferences(Constants.SHARED_PREFERENCES_NAME, MODE_PRIVATE);
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

        // Get Current user saved in the phone, if it doesn't exist use a new one created for this
        Gson gson = new Gson();
        String json = mPrefs.getString(Constants.CURRENT_USER, "");
        Log.d(TAG, json);
        User currentUser = gson.fromJson(json, User.class);
        if (currentUser == null){
            currentUser = new User(firebaseUser.getDisplayName(), firebaseUser.getEmail());
        }
        final User[] user = {currentUser};
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
            DatabaseReference journeyEntry = sJourneyDatabase.push();
            journeyEntry.setValue(journey);
            return journeyEntry.getKey();
        }
        return "";
    }

    /**
     * Set the given journey to the given journey ID in firebaseDB
     * @param jID ID that we want to link to the given journey
     * @param journey
     * @return
     */
    public static String updateJourney(String jID, Journey journey) {
        if (!isFirebaseInitialised()) return "";
        if (jID != null && journey != null) {
            DatabaseReference journeyEntry = sDatabase.getReference("journeys/" + jID);
            journeyEntry.setValue(journey);
            return journeyEntry.getKey();
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
     * Combine several users into one search and delete old searches
     * @param sID search ID
     * @param uID user ID
     * @param deleteID search to delete
     * @return boolean, return true if addition of user to search successful
     */
    //TODO: this needs to be tested as soon as the matching is over
    public static boolean addUserToSearch(String sID, String uID, String deleteID) {
        if (!isFirebaseInitialised()) return false;
        if (sID != null && uID != null && deleteID != null) {
            DatabaseReference databaseReference = sDatabase.getReference("search/" + sID + "/additionalUsers/");
            databaseReference.push().setValue(uID);
            //delete search with deleteID
            DatabaseReference searchReference = sDatabase.getReference("search/" + deleteID);
            searchReference.removeValue();
            return true;
        }
        return false;
    }

    public static boolean deleteSearch(String sID)
    {
        if (!isFirebaseInitialised()) return false;
        if(sID != null) {
            DatabaseReference searchReference = sDatabase.getReference("search/" + sID);
            searchReference.removeValue();
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
                userToRate.setNumOfRatings(userToRate.getNumOfRatings()+1);
                double userRating = ((userToRate.getRating() * (userToRate.getNumOfRatings() - 1))
                        + rating) / userToRate.getNumOfRatings();
                userToRate.setRating(userRating);
                //trim the rating number to 2 decimal values
                DecimalFormat df = new DecimalFormat("#.##");
                userToRate.setRating(Double.parseDouble(df.format(userToRate.getRating())));
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
                user.setRating(DatabaseUser.getRating());
                user.setRating(DatabaseUser.getNumOfRatings());
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
    public static HashMap<String, Object> getServerTime() {
        HashMap<String, Object> timestampNow = new HashMap<>();
        timestampNow.put("timestamp", ServerValue.TIMESTAMP);
        return timestampNow;
    }

    public static DatabaseReference getJourneyMessagesReference(String curr_journey) {

        DatabaseReference journeyMessagesRef = sDatabase.getReference("journeys/" + curr_journey + "/mMessages");
        journeyMessagesRef.keepSynced(true);
        return journeyMessagesRef;
    }

    /**
     * Retrieve every search from DB
     * @return List of all searches
     */
    public List<Search> retrieveAllSearches() {
        List<Search> searches = new ArrayList<>();
        if (!isFirebaseInitialised()) return null;
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("searches");
        ref.addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        //Get map of users in datasnapshot
                        Log.e(TAG,dataSnapshot.getValue().toString());
                        //calculateSearch((Map<String,Object>) dataSnapshot.getValue(),Search userSearch);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        //handle databaseError
                    }
                });

        return searches;
    }



}


