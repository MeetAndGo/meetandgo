package com.meetandgo.meetandgo.activities;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.meetandgo.meetandgo.FirebaseDB;
import com.meetandgo.meetandgo.R;
import com.meetandgo.meetandgo.User;
import com.meetandgo.meetandgo.fragments.ChatsFragment;
import com.meetandgo.meetandgo.fragments.CommuteFragment;
import com.meetandgo.meetandgo.fragments.JourneyHistoryFragment;
import com.meetandgo.meetandgo.fragments.MapsFragment;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private String uid;
    private boolean exists = false;

    @BindView(R.id.drawer_layout) DrawerLayout mDrawerLayout;
    @BindView(R.id.navigation) NavigationView mNavView;
    @BindView(R.id.toolbar) Toolbar mToolbar;
    private View mHeaderLayout;
    private TextView mTextViewUserName;
    private TextView mTextViewUserEmail;
    private ActionBarDrawerToggle mDrawerToggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        FirebaseDB.initializeApp(this);
        setSupportActionBar(mToolbar);

        // Get the debug keyhash for facebook login (Not useful on release version)
        getDebugKeyHash();

        setupUI();

        // Add current user to the database
        User currentUser = FirebaseDB.getCurrentUser();
        FirebaseDB.addUser(currentUser);

        mTextViewUserName.setText(currentUser.full_name);
        mTextViewUserEmail.setText(currentUser.email);
    }

    private void setupUI() {
        mNavView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {
                Log.d(TAG, "MenuItemClicked -> MenuItem: " + menuItem.getTitle());
                Class fragmentClass = getFragmentClass(menuItem.getItemId());
                boolean result = setSelectedFragment(fragmentClass);
                checkMenuItem(menuItem);
                mDrawerLayout.closeDrawers();
                return result;
            }
        });

        // Set the color of the status bar
        int statusBarColor = ContextCompat.getColor(getApplicationContext(), R.color.colorPrimaryDark);
        mDrawerLayout.setStatusBarBackgroundColor(statusBarColor);

        // Set the header details based on the user information retrieved from the server
        mHeaderLayout = mNavView.getHeaderView(0);
        mTextViewUserName = mHeaderLayout.findViewById(R.id.user_name);
        mTextViewUserEmail = mHeaderLayout.findViewById(R.id.user_email);

        // SetUp the MDrawerToogle for the toolbar
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.drawer_open, R.string.drawer_close);
        mDrawerLayout.addDrawerListener(mDrawerToggle);
        mDrawerToggle.syncState();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            if (mDrawerLayout.isDrawerOpen(Gravity.START)) mDrawerLayout.closeDrawer(Gravity.START);
            else mDrawerLayout.openDrawer(Gravity.START);
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Set as checked the menu item selected by the user in the navigation menu. It also sets the
     * rest of the items as not checked so there is only one highlighted.
     *
     * @param menuItem MenuItem that was selected by the user
     */
    private void checkMenuItem(MenuItem menuItem) {
        int size = mNavView.getMenu().size();
        for (int i = 0; i < size; i++) {
            mNavView.getMenu().getItem(i).setChecked(false);
        }
        menuItem.setChecked(true);
        setTitle(menuItem.getTitle());
    }

    /**
     * Puts the selected fragment in the content frame layout
     *
     * @param fragmentClass the fragment class you want to put in the content FrameLayout
     * @return boolean, true if it succeeds and false if it couldn't create the fragment instance
     */
    private boolean setSelectedFragment(Class fragmentClass) {
        try {
            Fragment fragment = (Fragment) fragmentClass.newInstance();
            FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction().replace(R.id.content_frame, fragment).commit();
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    /**
     * Depending on the menu item selected it returns the Fragment Class. This method should be changed
     * when new items are added to the menu. The SignOut item does not return any fragment.
     *
     * @param menuItemId Item Id that was selected in the menu
     * @return The fragment class corresponding to the item selected or null if the menu item does
     * change the fragment of the content frame layout.
     */
    private Class getFragmentClass(int menuItemId) {
        switch (menuItemId) {
            case R.id.menu_item_1:
                return MapsFragment.class;
            case R.id.menu_item_2:
                return ChatsFragment.class;
            case R.id.menu_item_3:
                return CommuteFragment.class;
            case R.id.menu_item_4:
                return JourneyHistoryFragment.class;
            case R.id.navigation_sign_out:
                sign_out();
                return null;
            default:
                return MapsFragment.class;
        }
    }

    /**
     * Signs out the current user from the account
     */
    private void sign_out() {
        AuthUI.getInstance()
                .signOut(this)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    public void onComplete(@NonNull Task<Void> task) {
                        Intent bootActivityIntent = new Intent(MainActivity.this, BootActivity.class);
                        MainActivity.this.startActivity(bootActivityIntent);
                    }
                });
    }

    /**
     * Gets and prints on the logcat console the key hash needed for facebook integration
     * TODO: This method should be removed from the final release version
     */
    private void getDebugKeyHash() {
        try {
            PackageInfo info = getPackageManager().getPackageInfo("com.meetandgo.meetandgo",
                    PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                Log.d("KeyHash:", Base64.encodeToString(md.digest(), Base64.DEFAULT));
            }
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, "get_debug_keyhash() error: Package Manager name not found");
        } catch (NoSuchAlgorithmException e) {
            Log.e(TAG, "get_debug_keyhash() error: No such algorithm");
        }
    }
}
