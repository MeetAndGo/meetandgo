package com.meetandgo.meetandgo.utils;

import com.meetandgo.meetandgo.data.Loc;
import com.meetandgo.meetandgo.data.Search;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class SearchUtil {
    /**
     * Calculates the Matching Score
     *
     * @param userStart   Location of User
     * @param userEnd     Destination of User
     * @param searchStart Other User Location
     * @param searchEnd   Other User Destination
     * @return Matching Score
     */
    public double calculateScore(Loc userStart, Loc userEnd, Loc searchStart, Loc searchEnd) {
        double score;

        //Get Distance From Start
        double distanceStart = Math.sqrt(Math.pow((searchStart.getLat() - userStart.getLat()), 2)
                + Math.pow((searchStart.getLng() - userStart.getLng()), 2));
        double distanceEnd = Math.sqrt(Math.pow((searchEnd.getLat() - userEnd.getLat()), 2)
                + Math.pow((searchEnd.getLng() - userEnd.getLng()), 2));
        score = (distanceStart * 0.7) + (distanceEnd * 0.3);
        return score;
    }

    public List<Search> calculateSearch(Map<String, Object> searches, Search currentUser) {

        List<Search> results = new ArrayList<Search>();
        double[][] resultOrder = new double[searches.size()][2];

        int count = 0;
        int validIndexes = 0;
        for (Map.Entry<String, Object> entry : searches.entrySet()) {
            Search search = (Search) entry.getValue();
            results.add(search);
            //PROCESS SEARCH
            if (!search.getUserId().equals(currentUser.getUserId())) {
                //Check Preference
                if (currentUser.getUserPreferences().checkPreferences(search.getUserPreferences())) {
                    //Calculate Score
                    double score = calculateScore(currentUser.getStartLocation(),
                            currentUser.getEndLocation(), search.getStartLocation(), search.getEndLocation());
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
        for (int i = sortedResults.length - validIndexes; i < sortedResults.length; i++) {
            list.add(results.get((int) sortedResults[i][1]));
        }
        return list;
    }
}
