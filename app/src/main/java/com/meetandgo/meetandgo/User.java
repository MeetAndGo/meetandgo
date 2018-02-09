package com.meetandgo.meetandgo;

import android.location.Location;

public class User {
    public String full_name;
    public String email;
    public double rating;
    public Location position;

    public User() {
        this.full_name = "";
        this.email = "";
        this.rating = 0.0;
        this.position = null;
    }

    public User(String full_name, String email, Double rating) {
        this.full_name = full_name;
        this.email = email;
        this.rating = rating;
        this.position = null;
    }
}
