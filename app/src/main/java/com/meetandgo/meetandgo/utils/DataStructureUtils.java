package com.meetandgo.meetandgo.utils;

/**
 * Utilities to perform tasks on data structures
 */

public final class DataStructureUtils {

    /**
     * Method to sort a 2 Dimensional array, sorting from smallest to biggest.
     *
     * @param array array to sort
     * @return sorted array
     */
    public double[][]  sort2DArray(double[][] array) {
        double[][] mArray = array;
        java.util.Arrays.sort(mArray, new java.util.Comparator<double[]>() {
            public int compare(double[] a, double[] b) {
                return Double.compare(a[0], b[0]);
            }
        });
        return mArray;
    }
}
