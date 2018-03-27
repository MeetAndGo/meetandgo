package com.meetandgo.meetandgo;

import com.google.android.gms.maps.model.LatLng;

public final class Constants {
    public static final int SUCCESS_RESULT = 0;
    public static final int FAILURE_RESULT = 1;
    public static final String PACKAGE_NAME = "com.meetandgo.meetandgo";
    public static final String RECEIVER = PACKAGE_NAME + ".RECEIVER";
    public static final String RESULT_DATA_KEY = PACKAGE_NAME + ".RESULT_DATA_KEY";
    public static final String LOCATION_DATA_EXTRA = PACKAGE_NAME + ".LOCATION_DATA_EXTRA";
    public static final String PREFERENCES_EXTRA = "PREFERENCES_EXTRA";
    public static final String JOURNEY_EXTRA = "JOURNEY_EXTRA";
    public static final String CURRENT_USER = "CurrentUser";
    public static final String SHARED_PREFERENCES_NAME = "SHARED_PREFERENCES";
    public static final double SEARCH_RADIUS = 1000.0;
    public static final int MAX_SEARCH_LIST_SIZE = 5;
    public static final int JOURNEY_REQUEST_CODE = 1 ;
    public static final String JOURNEY_ACTIVITY_EXTRA = "JOURNEY_ACTIVITY_EXTRA" ;
    public static final LatLng DEFAULT_LOCATION = new LatLng(53.341563, -6.253010);;
    public static final float DEFAULT_ZOOM = 13;
    public static final String CURRENT_USER_SEARCH = "currentUserSearch";
    public static final String GOOGLE_MAPS_API_KEY = "AIzaSyAjb05BjiPsCwg-VcicFb5Ff4gNICO_YbY";
    public static final String CURRENT_JOURNEY = "CURRENT_JOURNEY";
}
