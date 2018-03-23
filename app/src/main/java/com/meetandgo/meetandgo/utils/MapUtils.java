package com.meetandgo.meetandgo.utils;

import android.graphics.Point;
import android.location.Location;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.model.LatLng;
import com.meetandgo.meetandgo.Constants;

public final class MapUtils {
    /**
     * Converts Latitude and Longitude to Pixel values
     *
     * @param latLng Latitude and Longitude object
     * @return Pixel point
     */
    public static Point convertLatLngToPixels(GoogleMap map, LatLng latLng) {
        Projection projection = map.getProjection();
        Point point = projection.toScreenLocation(latLng);
        return point;
    }

    /**
     * Converts pixel position to latitude and longitude
     *
     * @param point pixel position
     * @return LatLng object
     */
    public static LatLng convertPixelsToLatLng(GoogleMap map, Point point) {
        Projection projection = map.getProjection();
        return projection.fromScreenLocation(point);
    }

    /**
     * Converts latitude and longitude to location object
     *
     * @param latLng LatLng object
     * @return Location object
     */
    public static Location convertLatLngToLocation(LatLng latLng) {
        Location newLocation = new Location("");
        newLocation.setLatitude(latLng.latitude);
        newLocation.setLongitude(latLng.longitude);
        return newLocation;
    }
    /**
     * Animates the camera from current location to the new location
     *
     * @param location Location object the camera animates towards
     */
    public static void animateCameraToLocation(GoogleMap map, Location location) {
        LatLng newLocation = new LatLng(location.getLatitude(), location.getLongitude());
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(newLocation, Constants.DEFAULT_ZOOM));
    }

    /**
     * Move camera from current location to new location
     *
     * @param location Location object the camera moves to
     */
    public static void moveCameraToLocation(GoogleMap map, Location location) {
        LatLng newLocation = new LatLng(location.getLatitude(), location.getLongitude());
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(newLocation, Constants.DEFAULT_ZOOM));
    }
}
