package com.meetandgo.meetandgo;

import com.meetandgo.meetandgo.data.Loc;
import com.meetandgo.meetandgo.data.Preferences;
import com.meetandgo.meetandgo.utils.DataStructureUtils;
import com.meetandgo.meetandgo.utils.SearchUtil;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Created by barbierj on 3/6/2018.
 */
public class MatchingTest {

    DataStructureUtils utils = new DataStructureUtils();
    SearchUtil searchUtil = new SearchUtil();

    @Test
    public void checkPreferenceTest() throws Exception {
        Preferences curr_pref = new Preferences(Preferences.Gender.MALE, Preferences.Mode.ANY);
        Preferences search1_pref = new Preferences(Preferences.Gender.MALE, Preferences.Mode.ANY);
        Preferences search2_pref = new Preferences(Preferences.Gender.FEMALE, Preferences.Mode.ANY);
        Preferences search3_pref = new Preferences(Preferences.Gender.FEMALE, Preferences.Mode.TAXI);

        assertTrue(searchUtil.checkPreferences(curr_pref, search1_pref));
        assertFalse(searchUtil.checkPreferences(curr_pref, search2_pref));
        assertFalse(searchUtil.checkPreferences(curr_pref, search3_pref));
    }

    @Test
    public void calculateScoreTest() throws Exception {
        Loc user_start = new Loc(0.0,0.0);
        Loc user_end = new Loc(20.0,10.0);
        Loc search_start = new Loc(0.0,0.0);
        Loc search_end = new Loc(20.0,10.0);

        assertEquals(searchUtil.calculateScore(user_start,user_end,search_start,search_end), 0.0,0);

        search_start = new Loc(1.0,0.0);
        search_end = new Loc(20.0,10.0);

        assertEquals(searchUtil.calculateScore(user_start,user_end,search_start,search_end), 0.7,0);
    }

    @Test
    public void sortListTest() throws Exception{
        //2D Array that contains position in list and score.
        int resultsLength = 3;
        double[][] results = new double[resultsLength][2];

        //Result 1
        results[0][0] = 12.4; //Matching Score
        results[0][1] = 7; //Position in Database Array
        //Result 2
        results[1][0] = 3.4;
        results[1][1] = 2;
        //Result 3
        results[2][0] = 7.9;
        results[2][1] = 0;

        //Sort Array
        double[][] sortedArray = utils.sort2DArray(results);

        assertEquals(sortedArray[0][0],3.4,0);
        assertEquals(sortedArray[1][0],7.9,0);
        assertEquals(sortedArray[2][0],12.4,0);
    }
}