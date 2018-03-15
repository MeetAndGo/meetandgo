package com.meetandgo.meetandgo.utils;

import com.meetandgo.meetandgo.data.Loc;
import com.meetandgo.meetandgo.data.Search;

import java.util.ArrayList;

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
    public static double calculateScore(Loc userStart, Loc userEnd, Loc searchStart, Loc searchEnd) {
        double score;

        //Get Distance From Start
        double distanceStart = Math.sqrt(Math.pow((searchStart.getLat() - userStart.getLat()), 2)
                + Math.pow((searchStart.getLng() - userStart.getLng()), 2));
        double distanceEnd = Math.sqrt(Math.pow((searchEnd.getLat() - userEnd.getLat()), 2)
                + Math.pow((searchEnd.getLng() - userEnd.getLng()), 2));
        score = (distanceStart * 0.7) + (distanceEnd * 0.3);
        return score;
    }

    public static ArrayList<Search> calculateSearch(ArrayList<Search> searches, Search currentUserSearch) {

        ArrayList<Search> results = new ArrayList<>();
        double[][] resultOrder = new double[searches.size()][2];

        int count = 0;
        int validIndexes = 0;
        for(Search search : searches){
            results.add(search);
            //PROCESS SEARCH
            if (!search.getUserId().equals(currentUserSearch.getUserId())) {
                //Check Preference
                if (currentUserSearch.getUserPreferences().checkPreferences(search.getUserPreferences())) {
                    //Calculate Score
                    double score = calculateScore(currentUserSearch.getStartLocation(),
                            currentUserSearch.getEndLocation(), search.getStartLocation(), search.getEndLocation());
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
        ArrayList<Search> list = new ArrayList<>();
        for (int i = sortedResults.length - validIndexes; i < sortedResults.length; i++) {
            list.add(results.get((int) sortedResults[i][1]));
        }
        return list;
    }
}
