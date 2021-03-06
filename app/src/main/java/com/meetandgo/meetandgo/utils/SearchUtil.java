package com.meetandgo.meetandgo.utils;

import com.meetandgo.meetandgo.Constants;
import com.meetandgo.meetandgo.data.Loc;
import com.meetandgo.meetandgo.data.Search;

import java.util.ArrayList;
import java.util.Objects;

/**
 * Utilities to manage matching calculations
 */
public final class SearchUtil {

    private static final String TAG = "SearchUtil"; //SearchUtil.class.getSimpleName();

    /**
     * Calculates the Matching Score for each input search
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

    /**
     * Calculates the list of searches to be displayed in the results along with sorting them.
     * --This is the main matching algorithm--
     *
     * @param searches          List of searches to process.
     * @param currentUserSearch User search data to identify compatibility
     * @return sorted and processed list based on the matching algorithm
     */
    public static ArrayList<Search> calculateSearch(ArrayList<Search> searches, Search currentUserSearch) {

        double[][] resultOrder = new double[searches.size()][2];

        int count = 0;
        int validIndexes = 0;
        for (Search search : searches) {
            //Process search
            if (!search.getUserID().equals(currentUserSearch.getUserID())) {
                boolean isEqual = false;
                for (int i = 0; i < search.getAdditionalUsers().size(); i++) {
                    if (Objects.equals(currentUserSearch.getUserID(), search.getAdditionalUsers().get(i)))
                        isEqual = true;
                }
                if (isEqual) {
                    resultOrder[count][0] = -1.0;
                    resultOrder[count][1] = count;
                    count++;
                    continue;
                }
                //Check Preference
                if (currentUserSearch.getUserPreferences().checkPreferences(search.getUserPreferences())) {
                    //Calculate Score
                    double score = calculateScore(currentUserSearch.getStartLocation(),
                            currentUserSearch.getEndLocation(), search.getStartLocation(), search.getEndLocation());
                    //Add to results if preferences match
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
        int resultCount = 0;
        for (int i = sortedResults.length - validIndexes; i < sortedResults.length && resultCount < Constants.MAX_SEARCH_LIST_SIZE; i++, resultCount++) {
            list.add(searches.get((int) sortedResults[i][1]));
        }
        return list;
    }
}
