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
import com.meetandgo.meetandgo.FirebaseDB;
import com.meetandgo.meetandgo.R;

import java.util.Arrays;
import java.util.List;

public class BootActivity extends AppCompatActivity {

    private static final String TAG = "BootActivity";
    private static final int RC_SIGN_IN = 123;
    private static final int MAX_NUMBER_OF_PERMISSION_DIALOG = 1;

    private List<AuthUI.IdpConfig> providers = Arrays.asList(
            new AuthUI.IdpConfig.Builder(AuthUI.EMAIL_PROVIDER).build(),
            new AuthUI.IdpConfig.Builder(AuthUI.FACEBOOK_PROVIDER).build(),
            new AuthUI.IdpConfig.Builder(AuthUI.GOOGLE_PROVIDER).build());

    private int mAskPermissionCounter = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_boot);
        FirebaseDB.initializeApp(this);
        askForPermissions();
    }

    /**
     * OnActivityResult needed to handle the login activity by firebase.
     * @param requestCode
     * @param resultCode
     * @param data Intent with that data that comes back from the activity that was called
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // If the activity was called using the request code RC_SIGN_IN
        if (requestCode == RC_SIGN_IN) {
            // We start the main activity if the user was correctly logged in
            if (resultCode == RESULT_OK) {
                startMainActivity();
            } else {
                // TODO: Make a dialog letting the user know that the login failed
                Log.d(TAG, "Authentication -> Sign in failed.");
            }
        }
    }

    /**
     * Permission logic inside, the app only asks for location permission. Once that is given the
     * app goes to the runLoginLogic() method. If the user denies to accept the location permission
     * a dialog will be shown one time, telling him that is extremely important for the app to have
     * the location.
     */
    private void askForPermissions() {
        final AlertDialog dialog = setUpPermissionDialog();
        PermissionListener mLocationPermissionListener = new PermissionListener() {
            @Override
            public void onPermissionGranted(PermissionGrantedResponse response) {
                Log.d(TAG, "PermissionGranted -> " + response.getPermissionName());
                runLoginLogic();
            }

            @Override
            public void onPermissionDenied(PermissionDeniedResponse response) {
                Log.d(TAG, "PermissionDenied -> " + response.getPermissionName());
                // We first check if a dialog can be shown insisting the user for permission
                if (mAskPermissionCounter < MAX_NUMBER_OF_PERMISSION_DIALOG) dialog.show();
                else runLoginLogic();
            }

            @Override
            public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {
                Log.d(TAG, "OnPermissionRationaleShouldBeShown -> " + permission.getName());
                // This run the system default permission dialog
                token.continuePermissionRequest();
            }
        };
        // Check if the ACCESS_FINE_LOCATION permission was granted or not. Results will be shown in
        // the corresponding listener
        Dexter.withActivity(this).withPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                .withListener(mLocationPermissionListener).check();
    }

    /**
     * Creates the dialog that is needed for asking the user a second time if the location permission
     * can be granted. To create the dialog the AlertDialog builder is used.
     *
     * @return AlertDialog
     */
    private AlertDialog setUpPermissionDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        DialogInterface.OnClickListener dialogOnClickListener = new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                askForPermissions();
                mAskPermissionCounter++;
            }
        };
        builder.setMessage(R.string.dialog_needed_location_permission);
        builder.setPositiveButton(R.string.allow_permission, dialogOnClickListener);
        return builder.create();
    }

    /**
     * Logic behind the login activity system
     */
    private void runLoginLogic() {
        // If user is logged in the app will go directly to the MainActivity, if not the loginActivity
        // will be displayed
        if (FirebaseDB.getCurrentUserUid() != null) startMainActivity();
        else startLoginActivity();
    }

    /**
     * Starts the Login Logic in a different activity, the result of it can be checked onActivityResult
     */
    private void startLoginActivity() {
        // An intent is created with the built-in Firebase UI
        Intent loginIntent = AuthUI.getInstance().createSignInIntentBuilder().setAvailableProviders(providers).build();
        startActivityForResult(loginIntent, RC_SIGN_IN);
        overridePendingTransition(0, 0);
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
