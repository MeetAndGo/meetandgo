package com.meetandgo.meetandgo.activities;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.firebase.ui.auth.AuthUI;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;
import com.meetandgo.meetandgo.R;

import java.util.Arrays;
import java.util.List;

public class BootActivity extends AppCompatActivity {

    private static final String TAG = "BootActivity";
    private static final int RC_SIGN_IN = 123;

    private List<AuthUI.IdpConfig> providers = Arrays.asList(
            new AuthUI.IdpConfig.Builder(AuthUI.EMAIL_PROVIDER).build(),
            new AuthUI.IdpConfig.Builder(AuthUI.FACEBOOK_PROVIDER).build(),
            new AuthUI.IdpConfig.Builder(AuthUI.GOOGLE_PROVIDER).build());

    private PermissionListener mLocationPermissionListener;
    private int mAskPermissionCounter = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_boot);
        askForPermissions();
    }

    private void askForPermissions() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.dialog_needed_location_permission)
                .setPositiveButton(R.string.allow_permission, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                       askForPermissions();
                       mAskPermissionCounter++;
                    }
                });
        final AlertDialog dialog = builder.create();
        // Create the AlertDialog object and return it
        mLocationPermissionListener = new PermissionListener() {
            @Override
            public void onPermissionGranted(PermissionGrantedResponse response) {
                Log.d(TAG, "PermssionGranted -> " + response.getPermissionName());
                startLoginActivity();
            }

            @Override
            public void onPermissionDenied(PermissionDeniedResponse response) {
                Log.d(TAG, "PermssionDenied -> " + response.getPermissionName());
                if(mAskPermissionCounter < 1) dialog.show();
                else startLoginActivity();
            }

            @Override
            public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {
                Log.d(TAG, "OnPermissionRationaleShouldBeShown -> " + permission.getName());
                token.continuePermissionRequest();
            }
        };

        Dexter.withActivity(this)
                .withPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                .withListener(mLocationPermissionListener)
                .check();
    }

    private void startLoginActivity() {
        startActivityForResult(AuthUI.getInstance().createSignInIntentBuilder().setAvailableProviders(providers).build(), RC_SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            if (resultCode == RESULT_OK) {
                this.startMainActivity();
            } else {
                Log.d(TAG, "Authentication -> Sign in failed.");
            }
        }
    }

    /**
     * We start the main activity when and close the BootActivity so its not in the stack anymore
     */
    private void startMainActivity() {
        Intent mainActivityIntent = new Intent(BootActivity.this, MainActivity.class);
        BootActivity.this.startActivity(mainActivityIntent);
        finish();
    }

}
