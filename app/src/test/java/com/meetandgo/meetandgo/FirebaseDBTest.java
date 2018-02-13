package com.meetandgo.meetandgo;

import android.app.Activity;

import com.meetandgo.meetandgo.activities.MainActivity;

import org.junit.Test;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

/**
 * For integration test with Firebase database.
 * As Firebase is a cloud service, automated tests pose challenges for which we need to implement a
 * Mock Firebase.
 */

public class FirebaseDBTest {

    @Test
    public void testInitialise(){
        FirebaseDB.initializeApp(new Activity());
    }

    @Test
    public void testAddUserToDB() {
        FirebaseDB.initializeApp(new Activity());
        User user_one = new User("User One", "one@user.com");
        boolean test = FirebaseDB.addUser(user_one);
        assertTrue(test);
    }

    @Test
    public void testUserIsInDB() {
        User user_one = new User("User One", "one@user.com");
        User user_two = new User("User Two", "two@user.com");
        FirebaseDB.addUser(user_one);
        //assertTrue(FirebaseDB.isUserInDB(user_one));
        //assertFalse(FirebaseDB.isUserInDB(user_two));
    }

    @Test
    public void removeUserDB() {
        User user_one = new User("User One", "one@user.com");
        FirebaseDB.addUser(user_one);
        //FirebaseDB.removeUser(user_one);
        //assertFalse(FirebaseDB.isUserInDB(user_one));
    }
}
