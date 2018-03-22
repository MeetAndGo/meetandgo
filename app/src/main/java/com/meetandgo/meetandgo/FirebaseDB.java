package com.meetandgo.meetandgo;

import android.app.Activity;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.meetandgo.meetandgo.data.ChatMessage;
import com.meetandgo.meetandgo.data.Journey;
import com.meetandgo.meetandgo.data.Loc;
import com.meetandgo.meetandgo.data.Search;
import com.meetandgo.meetandgo.data.User;
import com.squareup.otto.Bus;

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
        if (sDatabase == null) {
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
        User currentUser = gson.fromJson(json, User.class);
        if (currentUser == null) {
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
     * Give the current user, if the user does not exist on the database, we create the User with the
     * data from the FirebaseUser object.
     *
     * @param bus Event bus handler when you want to do something after reading the user.
     * @return the current user
     */
    public static User getCurrentUser(final Bus bus) {
        if (!isFirebaseInitialised()) return null;

        String uid = getCurrentUserUid();
        FirebaseUser firebaseUser = sAuth.getCurrentUser();
        if (firebaseUser == null) return null;

        // Get Current user saved in the phone, if it doesn't exist use a new one created for this
        Gson gson = new Gson();
        String json = mPrefs.getString(Constants.CURRENT_USER, "");
        User currentUser = gson.fromJson(json, User.class);
        if (currentUser == null) {
            currentUser = new User(firebaseUser.getDisplayName(), firebaseUser.getEmail());
        }
        final User[] user = {currentUser};
        ValueEventListener valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                user[0] = snapshot.getValue(User.class);
                bus.post(user[0]);
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
     *
     * @param jID     ID that we want to link to the given journey
     * @param journey
     * @return
     */
    public static String updateJourney(String jID, Journey journey) {
        if (!isFirebaseInitialised()) return "";
        if (jID != null && journey != null) {
            DatabaseReference journeyEntry = sDatabase.getReference("journeys/" + jID);
            journey.setjId(jID);
            journeyEntry.setValue(journey);
            return journeyEntry.getKey();
        }
        return "";
    }

    /**
     * Combine several users into one search and delete old searches
     *
     * @param sID      search ID
     * @param uID      user ID
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

    public static boolean deleteSearch(String sID) {
        if (!isFirebaseInitialised()) return false;
        if (sID != null) {
            DatabaseReference searchReference = sDatabase.getReference("search/" + sID);
            searchReference.removeValue();
            return true;
        }
        return false;
    }

    /**
     * Add message to the journey with the ID jID
     *
     * @param jID     ID of the journey the message is added to
     * @param message to add to the journey
     * @return true if correctly added, false otherwise
     */
    public static boolean addMessageToJourney(String jID, ChatMessage message) {
        if (!isFirebaseInitialised()) return false;
        if (jID != null && message != null) {
            DatabaseReference databaseReference = sDatabase.getReference("journeys/" + jID + "/messages/");
            databaseReference.push().setValue(message);
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
                userToRate.setNumOfRatings(userToRate.getNumOfRatings() + 1);
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
     *
     * @return
     */
    public static HashMap<String, Object> getServerTime() {
        HashMap<String, Object> timestampNow = new HashMap<>();
        timestampNow.put("timestamp", ServerValue.TIMESTAMP);
        return timestampNow;
    }

    public static DatabaseReference getJourneyMessagesReference(String curr_journey) {
        DatabaseReference journeyMessagesRef = sDatabase.getReference("journeys/" + curr_journey + "/messages");
        journeyMessagesRef.keepSynced(true);
        return journeyMessagesRef;
    }

    /**
     * Retrieve every search from DB
     *
     * @return List of all searches
     */
    public static ArrayList<Search> retrieveSearchesBySearch(final Bus bus, final Search search) {
        final ArrayList<Search> searches = new ArrayList<>();
        if (!isFirebaseInitialised()) return null;
        final Loc startLocation = search.getStartLocation();
        final Loc endLocation = search.getEndLocation();

        //TODO: make method to change from number to meters to e.g. restrict to 500 meters
        sSearchDatabase.orderByChild("startLocation/lat").startAt(startLocation.getLat() - Constants.SEARCH_RADIUS)
                .endAt(startLocation.getLat() + Constants.SEARCH_RADIUS).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String prevChildKey) {
                Search searchResult = dataSnapshot.getValue(Search.class);
                Log.d(TAG, searchResult.getUserId());
                Log.d(TAG, search.getUserId());
                if (!searchResult.getUserId().equals(search.getUserId())) {
                    Log.d(TAG, "S-Lng: " + searchResult.getStartLocation().getLng());
                    Log.d(TAG, "S-Lat: " + searchResult.getStartLocation().getLat());
                    Log.d(TAG, "E-Lng: " + searchResult.getEndLocation().getLng());
                    Log.d(TAG, "E-Lat: " + searchResult.getEndLocation().getLat());
                    Log.d(TAG, "Previous Search ID: " + prevChildKey);
                    searches.add(searchResult);
                    bus.post(searchResult);
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }

        });

        Log.d(TAG, String.valueOf(searches.size()));
        return searches;
    }


    public static void getJourneys(List<String> journeyIDs, ValueEventListener childEventListener) {
        for (String journeyID : journeyIDs) {
            sJourneyDatabase.child(journeyID).addValueEventListener(childEventListener);
        }

    }

    /**
     * Adds the journey to the user given the user id
     *
     * @param uid     User Id
     * @param journey Journey Object
     */
    public static void addJourneyToUser(final String uid, final Journey journey) {
        ValueEventListener valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                user.numOfTrips = user.getJourneyIDs().size() + 1;
                if (user != null) {
                    user.journeyIDs.add(journey.getjId());
                    updateUser(uid, user);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        };
        Log.d(TAG, "uid");
        getUserDatabaseReference(uid).addListenerForSingleValueEvent(valueEventListener);
    }

    /**
     * Updates a user given the user id with a new user object
     *
     * @param userID
     * @param user
     */

    public static void updateUser(String userID, User user) {
        Log.e(TAG, userID);
        Log.e(TAG, user.toString());
        if (userID != null && user != null) {
            DatabaseReference userEntry = sDatabase.getReference("users/" + userID);
            userEntry.setValue(user);
        }
    }

    // TODO: Fix this function
//    /**
//     * Increment the uID number of journey after a journey is completed
//     * (by clicking on the journey is over button in the chat fragment)
//     *
//     * @param uID user ID with a new journey completed
//     */
//    public static void incrementUserNumberOfJourney(String uID) {
//        final DatabaseReference tempUserRef = sDatabase.getReference("users/" + uID +"/numOfTrips/");
//
//        Log.d(TAG, "in IncrementNumJourneys");
//
//        ValueEventListener valueEventListener = new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot snapshot) {
//                int tempNumberOfJourney = snapshot.getValue(int.class);
//                tempUserRef.setValue(tempNumberOfJourney + 1);
//            }
//
//            @Override
//            public void onCancelled(DatabaseError databaseError) {
//            }
//        };
//        tempUserRef.addListenerForSingleValueEvent(valueEventListener);
//
//    }
}

