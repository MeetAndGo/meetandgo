package com.meetandgo.meetandgo;

import com.meetandgo.meetandgo.data.Loc;
import com.meetandgo.meetandgo.data.Preferences;
import com.meetandgo.meetandgo.data.Search;
import com.meetandgo.meetandgo.data.User;
import com.meetandgo.meetandgo.utils.DataStructureUtils;
import com.meetandgo.meetandgo.utils.SearchUtil;

import org.junit.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        Preferences curr_pref = new Preferences(Preferences.Gender.MALE, Preferences.Mode.ANY, Preferences.Gender.MALE);
        Preferences search1_pref = new Preferences(Preferences.Gender.MALE, Preferences.Mode.ANY, Preferences.Gender.FEMALE);
        Preferences search2_pref = new Preferences(Preferences.Gender.FEMALE, Preferences.Mode.ANY, Preferences.Gender.MALE);
        Preferences search3_pref = new Preferences(Preferences.Gender.MALE, Preferences.Mode.TAXI, Preferences.Gender.MALE);

        assertFalse(curr_pref.checkPreferences(search1_pref));
        assertFalse(curr_pref.checkPreferences(search2_pref));
        assertTrue(curr_pref.checkPreferences(search3_pref));
    }

    @Test
    public void calculateScoreTest() throws Exception {
        Loc user_start = new Loc(0.0,0.0);
        Loc user_end = new Loc(20.0,10.0);
        Loc search_start = new Loc(0.0,0.0);
        Loc search_end = new Loc(20.0,10.0);

        assertEquals(searchUtil.calculateScore(user_start,user_end,search_start,search_end), 0.0,0.0001);

        search_start = new Loc(1.0,0.0);
        search_end = new Loc(20.0,10.0);

        assertEquals(searchUtil.calculateScore(user_start,user_end,search_start,search_end), 0.7,0.0001);
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

    @Test
    public void calculateSearchTest() throws Exception {
        User test_user = new User("Tony", "t@com");
        Preferences user_prefs = new Preferences(Preferences.Gender.MALE, Preferences.Mode.ANY, Preferences.Gender.MALE);
        Loc user_start = new Loc(0.0,0.0);
        Loc user_end = new Loc(20.0,10.0);
        String user_id = "This is strange, indeed!";
        Search userSearch = new Search(user_prefs,user_start,user_end, user_id);

        // Firebase searches
        Preferences prefs1 = new Preferences(Preferences.Gender.FEMALE, Preferences.Mode.ANY, Preferences.Gender.MALE);
        Loc start1 = new Loc(0.0,0.0);
        Loc end1 = new Loc(20.0,10.0);
        String uid = "fjkhdzkfhsdjk";
        Search search1 = new Search(prefs1,start1,end1,uid);

        Preferences prefs2 = new Preferences(Preferences.Gender.MALE, Preferences.Mode.ANY, Preferences.Gender.MALE);
        Loc start2 = new Loc(0.0,0.0);
        Loc end2 = new Loc(25.0,10.0);
        String uid2 = "sgsdgd";
        Search search2 = new Search(prefs2,start2,end2,uid2);

        Preferences prefs3 = new Preferences(Preferences.Gender.MALE, Preferences.Mode.ANY, Preferences.Gender.MALE);
        Loc start3 = new Loc(0.0,0.0);
        Loc end3 = new Loc(100.0,10.0);
        String uid3 = "gxdfs";
        Search search3 = new Search(prefs3,start3,end3,uid3);

        Map<String,Object> searches = new HashMap<>();
        searches.put("SearchID1",search1);
        searches.put("SearchID2",search2);
        searches.put("SearchID3",search3);

        List<Search> results = searchUtil.calculateSearch(searches,userSearch);
        assertEquals(results.get(0),search2);
        assertEquals(results.get(1),search3);
        assertEquals(results.size(), 2);
    }
}