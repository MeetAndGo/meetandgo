package com.meetandgo.meetandgo.activities;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.firebase.ui.auth.AuthUI;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_boot);
       // startMainActivity();
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
