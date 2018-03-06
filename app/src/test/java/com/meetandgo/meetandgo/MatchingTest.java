package com.meetandgo.meetandgo;

import com.meetandgo.meetandgo.data.Loc;
import com.meetandgo.meetandgo.data.Preferences;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Created by barbierj on 3/6/2018.
 */

public class MatchingTest {

    @Test
    public void checkPreferenceTest() throws Exception {
        Preferences curr_pref = new Preferences(Preferences.Gender.MALE, Preferences.Mode.ANY);
        Preferences search1_pref = new Preferences(Preferences.Gender.MALE, Preferences.Mode.ANY);
        Preferences search2_pref = new Preferences(Preferences.Gender.FEMALE, Preferences.Mode.ANY);
        Preferences search3_pref = new Preferences(Preferences.Gender.FEMALE, Preferences.Mode.TAXI);

        assertTrue(checkPreferences(curr_pref, search1_pref));
        assertFalse(checkPreferences(curr_pref, search2_pref));
        assertFalse(checkPreferences(curr_pref, search3_pref));
    }

    @Test
    public void calculateScoreTest() throws Exception {
        Loc user_start = new Loc(0.0,0.0);
        Loc user_end = new Loc(20.0,10.0);
        Loc search_start = new Loc(0.0,0.0);
        Loc search_end = new Loc(20.0,10.0);

        assertEquals(calculateScore(user_start,user_end,search_start,search_end), 0.0);

        search_start = new Loc(1.0,0.0);
        search_end = new Loc(20.0,10.0);

        assertEquals(calculateScore(user_start,user_end,search_start,search_end), 0.7);
    }

    //TODO: create the sortListTest
}
