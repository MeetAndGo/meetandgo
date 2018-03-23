package com.meetandgo.meetandgo;

import com.meetandgo.meetandgo.data.User;

import org.junit.Test;

import static junit.framework.Assert.assertTrue;

/**
 * For integration test with FireBase database.
 */

public class FireBaseDBTest {

    @Test
    public void testInitialise(){

    }

    @Test
    public void testAddUserToDB() {
        User user_one = new User("User One", "one@user.com");
        boolean test = FireBaseDB.addUser(user_one);
        assertTrue(test);
    }

    /*@Test
    public void testUserIsInDB() {
        User user_one = new User("User One", "one@user.com");
        User user_two = new User("User Two", "two@user.com");
        FireBaseDB.addUser(user_one);
        assertTrue(FireBaseDB.isUserInDB(user_one));
        assertFalse(FireBaseDB.isUserInDB(user_two));
    }

    @Test
    public void removeUserDB() {
        User user_one = new User("User One", "one@user.com");
        FireBaseDB.addUser(user_one);
        FireBaseDB.removeUser(user_one);
        assertFalse(FireBaseDB.isUserInDB(user_one));
    }*/
}
