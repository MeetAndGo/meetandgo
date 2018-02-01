package com.meetandgo.meetandgo;

import android.app.Application;

import com.firebase.client.Firebase;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;

/**
 * Created by gilmarma on 1/31/2018.
 */

public class FireApp extends Application {
    public void onCreate(){
        super.onCreate();
        Firebase.setAndroidContext(this);
    }
}
