package com.meetandgo.meetandgo;

import com.meetandgo.meetandgo.data.User;

import org.junit.Test;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

/**
 * For integration test with Firebase database.
 */

public class FirebaseDBTest {

    @Test
    public void testInitialise(){

    }

    @Test
    public void testAddUserToDB() {
        User user_one = new User("User One", "one@user.com", 0.0);
        boolean test = FirebaseDB.addUser(user_one);
        assertTrue(test);
    }

    @Test
    public void testUserIsInDB() {
        User user_one = new User("User One", "one@user.com", 0.0);
        User user_two = new User("User Two", "two@user.com", 0.0);
        FirebaseDB.addUser(user_one);
        assertTrue(FirebaseDB.isUserInDB(user_one));
        assertFalse(FirebaseDB.isUserInDB(user_two));
    }

    @Test
    public void removeUserDB() {
        User user_one = new User("User One", "one@user.com", 0.0);
        FirebaseDB.addUser(user_one);
        FirebaseDB.removeUser(user_one);
        assertFalse(FirebaseDB.isUserInDB(user_one));
    }
}
