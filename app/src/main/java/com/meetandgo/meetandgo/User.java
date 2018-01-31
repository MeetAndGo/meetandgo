package com.meetandgo.meetandgo;

import android.location.Location;

/**
 * Created by gilmarma on 1/31/2018.
 */
public class User {

        public String full_name;
        public String email;
        public double rating;
        public Location position;

        public User(String full_name, String email,Double rating, Location position) {
            this.full_name = full_name;
            this.email = email;
            this.rating = rating;
            this.position = position;
        }

}
