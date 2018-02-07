package com.meetandgo.meetandgo;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;

/**
 * this static class is handling all the interactions with the Firebase database
 * In java only nested classes can be static so here all methods will be set to static
 *
 */

public class FirebaseDB {
    private FirebaseAuth mAuth;
    private FirebaseUser mFirebaseUser;
    private DatabaseReference myRef;

    /**
     * Add user to Firebase Database
     *
     * @param user: the user to be added
     * @return boolean, true if user successfully added, false otherwise
     */
    public static boolean addUser(User user)
    {
        return true;
    }

}
