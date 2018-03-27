package com.meetandgo.meetandgo;

import android.app.Activity;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
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

import static android.content.Context.MODE_PRIVATE;

/**
 * This static class is handling all the interactions with the FireBase database
 * In java only nested classes can be static so here all methods will be set to static
 * TODO: Massive refactor - divide functions by object type by creating e.g. UserFireBaseDB
 */
public class FireBaseDB {
    private static final String TAG = "FireBaseDB";
    private static FirebaseAuth sAuth;
    public static FirebaseDatabase sDatabase;
    private static boolean sInitialised;
    private static DatabaseReference sUsersDatabaseReference;
    private static DatabaseReference sSearchDatabase;
    private static DatabaseReference sJourneyDatabase;
    private static SharedPreferences mPrefs;

    public static void initializeApp(Activity activity) {
        FirebaseApp.initializeApp(activity);
        sAuth = FirebaseAuth.getInstance();
        initializeDatabaseReferences();
        sInitialised = true;

        mPrefs = activity.getSharedPreferences(Constants.SHARED_PREFERENCES_NAME, MODE_PRIVATE);
    }

    /**
     * Initializes Database References that are then also synced with local storage
     */
    private static void initializeDatabaseReferences() {
        String userID = getCurrentUserID();
        if (sDatabase == null) {
            sDatabase = FirebaseDatabase.getInstance();
            sDatabase.setPersistenceEnabled(true);
        }
        if (userID != null) {
            sUsersDatabaseReference = getUserDatabaseReference(userID);
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

    public static String getCurrentUserID() {
        if (!isFireBaseInitialized()) return null;

        FirebaseUser user = sAuth.getCurrentUser();
        if (user != null) return user.getUid();
        else return null;
    }

    /**
     * Give the current user, if the user does not exist on the database, we create the User with the
     * data from the FireBaseUser object.
     *
     * @return the current user
     */
    public static User getCurrentUser() {
        if (!isFireBaseInitialized()) return null;

        String uid = getCurrentUserID();
        FirebaseUser firebaseUser = sAuth.getCurrentUser();
        if (firebaseUser == null) return null;

        // Get Current user saved in the phone, if it doesn't exist use a new one created for this
        User currentUser = getLocalStorageUser();
        if (currentUser == null) {
            currentUser = new User(firebaseUser.getDisplayName(), firebaseUser.getEmail());
        }
        final User[] user = {currentUser};
        ValueEventListener valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                user[0] = snapshot.getValue(User.class);
                saveUserInLocalStorage(user[0]);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        };

        getUserDatabaseReference(uid).addListenerForSingleValueEvent(valueEventListener);
        return user[0];
    }

    /**
     * Using the SharedPreferences of the phone, it saves the user on the
     *
     * @param user
     */
    private static void saveUserInLocalStorage(User user) {
        SharedPreferences.Editor prefsEditor = mPrefs.edit();
        Gson gson = new Gson();
        String json = gson.toJson(user);
        prefsEditor.putString(Constants.CURRENT_USER, json);
        prefsEditor.apply();
    }


    /**
     * Give the current user, if the user does not exist on the database, we create the User with the
     * data from the FirebaseUser object.
     *
     * @param bus Event mBus handler when you want to do something after reading the user.
     * @return the current user
     */
    public static void getCurrentUser(final Bus bus) {
        if (!isFireBaseInitialized()) return;

        String uid = getCurrentUserID();
        FirebaseUser firebaseUser = sAuth.getCurrentUser();
        if (firebaseUser == null) return;
        ValueEventListener valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                if (user == null) return;
                bus.post(user);
                saveUserInLocalStorage(user);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        };

        getUserDatabaseReference(uid).addListenerForSingleValueEvent(valueEventListener);
    }

    @NonNull
    private static User getLocalStorageUser() {
        // Get Current user saved in the phone, if it doesn't exist use a new one created for this
        Gson gson = new Gson();
        String json = mPrefs.getString(Constants.CURRENT_USER, "");
        User currentUser = gson.fromJson(json, User.class);
        return currentUser;
    }

    /**
     * Add user to FireBase Database
     *
     * @return boolean, true if user successfully added, false otherwise
     */
    public static boolean addUser(User newUser) {
        if (!isFireBaseInitialized()) return false;
        if (newUser != null) {
            String uid = getCurrentUserID();
            getUserDatabaseReference(uid).setValue(newUser);
            getUserDatabaseReference(uid).child("userID").setValue(uid);
            return true;
        }
        return false;
    }

    /**
     * Add a user Search to FireBase Database
     *
     * @return String, return the key of this search in FireBase database, if unsuccessful in
     * adding Search return empty string
     */
    public static String addNewSearch(Search newSearch) {
        if (!isFireBaseInitialized()) return null;
        if (newSearch != null) {
            DatabaseReference searchEntry = sSearchDatabase.push();
            searchEntry.setValue(newSearch);
            // Update the search key element of the Search Object
            searchEntry.child("searchID").setValue(searchEntry.getKey());
            return searchEntry.getKey();
        }
        return null;
    }


    /**
     * Add a Journey to FireBase Database
     *
     * @return String, return the key of this search in FireBase database, if unsuccessful in
     * adding Journey return empty string
     */
    public static String addNewJourney(Journey journey) {
        if (!isFireBaseInitialized()) return null;
        if (journey != null) {
            DatabaseReference journeyEntry = sJourneyDatabase.push();
            journeyEntry.setValue(journey);
            // Update the journey key element of the Journey Object
            journeyEntry.child("journeyID").setValue(journeyEntry.getKey());
            return journeyEntry.getKey();

        }
        return null;
    }


    /**
     * Combine several users into one search and delete old searches
     *
     * @param searchID search ID
     * @param userID   user ID
     * @param deleteID search to delete
     * @return boolean, return true if addition of user to search successful
     */
    //TODO: this needs to be tested as soon as the matching is over
    public static boolean addUserToSearch(String searchID, String userID, String deleteID) {
        if (!isFireBaseInitialized()) return false;
        if (searchID != null && userID != null && deleteID != null) {
            DatabaseReference databaseReference = sDatabase.getReference("search/" + searchID + "/additionalUsers/");
            databaseReference.push().setValue(userID);
            //delete search with deleteID
            DatabaseReference searchReference = sDatabase.getReference("search/" + deleteID);
            searchReference.removeValue();
            return true;
        }
        return false;
    }

    /**
     * Remove search from database
     *
     * @param searchID search ID to remove
     * @return if outcome was successful
     */
    public static boolean deleteSearch(String searchID) {
        if (!isFireBaseInitialized()) return false;
        if (searchID != null) {
            DatabaseReference searchReference = sDatabase.getReference("search/" + searchID);
            searchReference.removeValue();
            return true;
        }
        return false;
    }

    /**
     * Add chat_message_item to the journey with the ID journeyID
     *
     * @param journeyID ID of the journey the chat_message_item is added to
     * @param message   to add to the journey
     * @return true if correctly added, false otherwise
     */
    public static boolean addMessageToJourney(String journeyID, ChatMessage message) {
        if (!isFireBaseInitialized()) return false;
        if (journeyID != null && message != null) {
            DatabaseReference databaseReference = sDatabase.getReference("journeys/" + journeyID + "/messages/");
            databaseReference.push().setValue(message);
            return true;
        }
        return false;
    }


    /**
     * Check to see if FireBase was initialized or not
     *
     * @return Boolean variable indicating if FireBase has been initialized or not
     */
    public static boolean isFireBaseInitialized() {
        if (sAuth == null) return false;
        if (sDatabase == null) return false;
        return sInitialised;
    }

    /**
     * Checks if the user is in database, not in the Authentication database but in the users one
     * //Do we really need that? Try doing callback one last time
     *
     * @param uid String corresponding to the user unique id that needs to be checked
     * @return Boolean value indicating if the user was found or not
     */
    public static void isUserInDB(String uid, ValueEventListener valueEventListener) {
        // Run the search for the user based on his userID
        getUserDatabaseReference(uid).addValueEventListener(valueEventListener);
    }

    /**
     * Removes a user from the FireBase Database
     *
     * @param userID ID of user to be removed
     */
    public static void removeCurrentUser(String userID) {
        // Remove user from the "users" database
        getUserDatabaseReference(userID).removeValue();
        // Remove user from the auth database
        FirebaseUser firebaseUser = sAuth.getCurrentUser();
        firebaseUser.delete();
    }


    /**
     * Adds rating to user uid
     * Note: if several addRating() launched sequentially, risk of over writing the previous call
     *
     * @param userID String corresponding to the user unique id that is rated
     * @param rating The rating that is given
     */
    public static void addRating(final String userID, final float rating) {
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
                getUserDatabaseReference(userID).setValue(userToRate);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        };
        getUserDatabaseReference(userID).addListenerForSingleValueEvent(valueEventListener);
    }

    public static void getRating(String userID, final User user) {
        ValueEventListener valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                User databaseUser = snapshot.getValue(User.class);
                if (databaseUser == null) return;
                user.setRating(databaseUser.getRating());
                user.setRating(databaseUser.getNumOfRatings());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        };
        getUserDatabaseReference(userID).addListenerForSingleValueEvent(valueEventListener);
    }

    /**
     * Whenever the class is sent to the server the timestamp is updated. See the data/Search class
     * for reference on how to use it
     *
     * @return HashMap<String, Object> where the object is the timestamp
     */
    public static HashMap<String, Object> getServerTime() {
        HashMap<String, Object> timestampNow = new HashMap<>();
        timestampNow.put("timestamp", ServerValue.TIMESTAMP);
        return timestampNow;
    }

    public static DatabaseReference getJourneyMessagesReference(String journeyID) {
        DatabaseReference journeyMessagesRef = sDatabase.getReference("journeys/" + journeyID + "/messages");
        journeyMessagesRef.keepSynced(true);
        return journeyMessagesRef;
    }

    /**
     * Retrieve every search from DB and calls the mBus post method once it's done
     */
    public static void retrieveSearchesBySearch(final Bus bus, final Search search) {
        final ArrayList<Search> searches = new ArrayList<>();
        if (!isFireBaseInitialized()) return;
        final Loc startLocation = search.getStartLocation();
        final Loc endLocation = search.getEndLocation();

        //TODO: make method to change from number to meters to e.g. restrict to 500 meters
        sSearchDatabase.orderByChild("startLocation/lat").startAt(startLocation.getLat() - Constants.SEARCH_RADIUS)
                .endAt(startLocation.getLat() + Constants.SEARCH_RADIUS).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String prevChildKey) {
                Search searchResult = dataSnapshot.getValue(Search.class);
                if (searchResult == null) return;
                if (searchResult.getSearchID() == null) return;
                if (!searchResult.getUserID().equals(search.getUserID())) {
                    searches.add(searchResult);
                    bus.post(searchResult);
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                Search searchResult = dataSnapshot.getValue(Search.class);
                if (searchResult == null) return;
                if (searchResult.getSearchID() == null) return;
                if (!searchResult.getUserID().equals(search.getUserID())) {
                    searches.add(searchResult);
                    bus.post(searchResult);
                }
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
    }

    /**
     * Add eventListeners to journeys
     *
     * @param journeyIDs         array of journeys to add listeners to
     * @param childEventListener the listener to be added to the journeys
     */
    public static void getJourneys(List<String> journeyIDs, ValueEventListener childEventListener) {
        for (String journeyID : journeyIDs) {
            sJourneyDatabase.child(journeyID).addValueEventListener(childEventListener);
        }

    }

    /**
     * Adds the journey to the user given the user id
     *
     * @param userID  User Id
     * @param journey Journey Object
     */
    public static void addJourneyToUser(final String userID, final Journey journey) {
        ValueEventListener valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                if (user == null) return;
                user.setNumOfTrips(user.getJourneyIDs().size() + 1);
                user.addJourneyID(journey.getJourneyID());
                updateUser(userID, user);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        };
        getUserDatabaseReference(userID).addListenerForSingleValueEvent(valueEventListener);
    }

    /**
     * Updates a user given the user id with a new user object
     *
     * @param userID String with the user ID of the user that you want to update
     */
    public static void updateUser(String userID, User user) {
        if (user == null) return;
        DatabaseReference userEntry = sDatabase.getReference("users/" + userID);
        userEntry.setValue(user);

    }

    public static void updateJourney(Journey journey) {
        if (journey == null) return;
        DatabaseReference journeyEntry = sJourneyDatabase.child(journey.getJourneyID());
        journeyEntry.setValue(journey);
    }

    public static void updateSearch(Search search) {
        if (search == null) return;
        DatabaseReference searchEntry = sSearchDatabase.child(search.getSearchID());
        searchEntry.setValue(search);
    }

    public static void deleteJourneyFromUser(String userID, String journeyID) {
        User user = getCurrentUser();
        if (user == null) return;
        user.removeJourney(journeyID);
        updateUser(userID, user);
    }

    /**
     * Gets the user object based on the user id
     *
     * @param userID
     * @param valueEventListener action that will be executed once its finished
     */
    public static void getUser(String userID, ValueEventListener valueEventListener) {
        getUserDatabaseReference(userID).addListenerForSingleValueEvent(valueEventListener);
    }

    /**
     * Gets one property of a user
     *
     * @param userID             ID of the user
     * @param property           Property that you want to get from the user
     * @param valueEventListener Action that will be executed on finish
     */
    public static void getUserProperty(String userID, String property, ValueEventListener valueEventListener) {
        getUserDatabaseReference(userID).child(property).addListenerForSingleValueEvent(valueEventListener);
    }

    public static void removeUserFromLocalStorage() {
        mPrefs.edit().remove(Constants.CURRENT_USER).commit();
    }


}