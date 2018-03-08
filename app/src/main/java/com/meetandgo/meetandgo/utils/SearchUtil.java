package com.meetandgo.meetandgo.utils;

import com.meetandgo.meetandgo.data.Loc;
import com.meetandgo.meetandgo.data.Preferences;
import com.meetandgo.meetandgo.data.Search;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by gilmarma on 3/7/2018.
 */

public final class SearchUtil {
    /**
     * Calculates the Matching Score
     * @param user_start Location of User
     * @param user_end Destination of User
     * @param search_start Other User Location
     * @param search_end Other User Destination
     * @return Matching Score
     */
    public double calculateScore(Loc user_start, Loc user_end,Loc search_start,Loc search_end){
        double score;

        //Get Distance From Start
        double distanceStart = Math.sqrt(Math.pow((search_start.getLat()-user_start.getLat()),2)
                +Math.pow((search_start.getLng()-user_start.getLng()),2));
        double distanceEnd = Math.sqrt(Math.pow((search_end.getLat()-user_end.getLat()),2)
                +Math.pow((search_end.getLng()-user_end.getLng()),2));
        score = (distanceStart * 0.7) + (distanceEnd * 0.3);
        return score;
    }

    public boolean checkPreferences(Preferences user_prefs, Preferences search_prefs){
        return user_prefs.equals(search_prefs);
    }

    public List<Search> calculateSearch(Map<String,Object> searches, Search userSearch){

        List<Search> results = new ArrayList<Search>();
        double[][] resultOrder = new double[searches.size()][2];

        int count = 0;
        int validIndexes = 0;
        for(Map.Entry<String, Object> entry : searches.entrySet()) {
            Search search = (Search) entry.getValue();
            results.add(search);
            //PROCESS SEARCH
            if (!search.getUserId().equals(userSearch.getUserId())) {
                //Check Preference
                if (search.getUserPreferences().equals(userSearch.getUserPreferences())) {

                    //Calculate Score
                    double score = calculateScore(userSearch.getStartLocation(),
                            userSearch.getEndLocation(), search.getStartLocation(), search.getEndLocation());
                    //ADD TO RESULTS ORDER if Preference okay
                    resultOrder[count][0] = score;
                    resultOrder[count][1] = count;
                    validIndexes++;
                } else {
                    resultOrder[count][0] = -1.0;
                    resultOrder[count][1] = count;
                }
                count++;

            }

        }
        DataStructureUtils util = new DataStructureUtils();
        double[][] sortedResults = util.sort2DArray(resultOrder);

        //Create ArrayList of results
        List<Search> list = new ArrayList<>();
        for(int i = sortedResults.length - validIndexes; i < sortedResults.length; i++)
        {
            list.add(results.get((int)sortedResults[i][1]));
        }
        return list;
    }
}
