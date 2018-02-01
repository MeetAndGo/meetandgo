package com.meetandgo.meetandgo;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.firebase.client.Firebase;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity{

    private static final int RC_SIGN_IN = 123;
    private FirebaseAuth mAuth;
    private String uid;
    private boolean exists = false;

    List<AuthUI.IdpConfig> providers = Arrays.asList(new AuthUI.IdpConfig.Builder(AuthUI.EMAIL_PROVIDER).build(),
                                                    new AuthUI.IdpConfig.Builder(AuthUI.FACEBOOK_PROVIDER).build(),
                                                    new AuthUI.IdpConfig.Builder(AuthUI.GOOGLE_PROVIDER).build());

    private Button btn_signout;

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        // Update UI
        //gilLog.d("Authentication", "CurrentUser" + currentUser.getEmail());
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Get the debug keyhash for facebook login (Not useful on release version)
        get_debug_keyhash();

        mAuth = FirebaseAuth.getInstance();
        FirebaseApp.initializeApp(this);

        setContentView(R.layout.activity_main);

        btn_signout = findViewById(R.id.btn_signout); //link button to the button created in layout
        btn_signout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AuthUI.getInstance()
                        .signOut((FragmentActivity) view.getContext())
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            public void onComplete(@NonNull Task<Void> task) {
                                startActivityForResult(AuthUI.getInstance().createSignInIntentBuilder().setAvailableProviders(providers).build(), RC_SIGN_IN);
                            }
                        });
            }
        });



        startActivityForResult(AuthUI.getInstance().createSignInIntentBuilder().setAvailableProviders(providers).build(), RC_SIGN_IN);
    }

    // TODO: Delete on release version
    private void get_debug_keyhash() {
        try {
            PackageInfo info = getPackageManager().getPackageInfo(
                    "com.meetandgo.meetandgo",
                    PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                Log.d("KeyHash:", Base64.encodeToString(md.digest(), Base64.DEFAULT));
            }
        } catch (PackageManager.NameNotFoundException e) {

        } catch (NoSuchAlgorithmException e) {

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            IdpResponse response = IdpResponse.fromResultIntent(data);

            if (resultCode == RESULT_OK) {
                // Successfully signed in
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                uid = user.getUid();

                // Write a message to the database
                FirebaseDatabase database = FirebaseDatabase.getInstance();

                //Check if user already exists
                DatabaseReference myRef = database.getReference("users");
                myRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        if (snapshot.hasChild(uid)) {
                            exists = true;
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

                //Add user to database
                if(!exists) {
                    Map<String, User> users = new HashMap<>();
                    users.put(uid, new User(user.getDisplayName(), user.getEmail(), 0.0, null));
                    myRef.setValue(users);
                }



                // ...
                //startActivity();
            } else {
               Log.d("Authentication", "Sign in failed.");
            }
        }
    }

}
