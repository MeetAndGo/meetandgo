package com.meetandgo.meetandgo.data;

/**
 * Uploads latitude and longitude to FireBase
 */

public class Loc {

    private double lat;
    private double lng;

    public Loc() {
        this.lat = 0;
        this.lng = 0;
    }

    public Loc(double lat, double lng) {
        this.lat = lat;
        this.lng = lng;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLng() {
        return lng;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }

    public String toString() {
        return String.format("Lat: %s, Long: %s", getLat(), getLng());
    }

}
