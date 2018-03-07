package com.meetandgo.meetandgo.utils;

/**
 * Created by gilmarma on 3/7/2018.
 */

public final class DataStructureUtils {
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
