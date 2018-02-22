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
import android.support.v4.app.FragmentTransaction;
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
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.meetandgo.meetandgo.FirebaseDB;
import com.meetandgo.meetandgo.R;
import com.meetandgo.meetandgo.data.Preferences;
import com.meetandgo.meetandgo.data.User;
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

    @BindView(R.id.drawer_layout) DrawerLayout mDrawerLayout;
    @BindView(R.id.navigation) NavigationView mNavView;
    @BindView(R.id.toolbar) Toolbar mToolbar;
    private View mHeaderLayout;
    private TextView mTextViewUserName;
    private TextView mTextViewUserEmail;
    private TextView mTextViewNumberOfRatings;
    private RatingBar mRatingBarRating;
    private ActionBarDrawerToggle mDrawerToggle;

    private Fragment mCurrentFragment;
    private Fragment mMapFragment;
    private Fragment mChatsFragment;
    private Fragment mJourneyHistoryFragment;
    private Fragment mCommuteFragment;

    private Toast mToast;
    private ValueEventListener mUserValueEventListener;

    @Override
    protected void onStart() {
        super.onStart();
        mToolbar.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.toolbarColor));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        setSupportActionBar(mToolbar);

        // Get the debug KeyHash for facebook login (Not useful on release version)
        getDebugKeyHash();
        setUpMenuFragments();
        setupUI();
        setUpUser();

        // Set the maps fragment as a default fragment on Start
        setSelectedFragmentByMenuItem(R.id.menu_item_1);
    }

    /**
     * SetUp the actions related to setting up the user when the main activity is started and customize
     * the UI for the current user
     */
    private void setUpUser() {
        final User currentUser = FirebaseDB.getCurrentUser();
        // ValueEventListener needed to get the return of asking the database for the user
        mUserValueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                Log.e(TAG, "in ondatachange from event listener" + snapshot.toString());
                if (snapshot.getValue(User.class) == null) FirebaseDB.addUser(currentUser);
                else if (FirebaseDB.getCurrentUserUid() == null) startBootActivity();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        };
        // Add current user to the database
        FirebaseDB.isUserInDB(FirebaseDB.getCurrentUserUid(), mUserValueEventListener);

        // UpdateUI based on the current user that is using the app
        mTextViewUserName.setText(currentUser.mFullName);
        mTextViewUserEmail.setText(currentUser.mEmail);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle the selection of the burger toggle that manages the drawer layout
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            if (mDrawerLayout.isDrawerOpen(Gravity.START)) mDrawerLayout.closeDrawer(Gravity.START);
            else mDrawerLayout.openDrawer(Gravity.START);
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Setup the fragments of the Menu in order to recycle them later and not create new ones on
     * the go.
     */
    private void setUpMenuFragments() {
        try {
            mMapFragment = MapsFragment.class.newInstance();
            mChatsFragment = ChatsFragment.class.newInstance();
            mJourneyHistoryFragment = JourneyHistoryFragment.class.newInstance();
            mCommuteFragment = CommuteFragment.class.newInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void startBootActivity() {
        Intent bootActivityIntent = new Intent(this, BootActivity.class);
        MainActivity.this.startActivity(bootActivityIntent);
        // After starting the boot activity we clear this one
        finish();
    }

    private void setupUI() {
        mNavView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {
                Log.d(TAG, "MenuItemClicked -> MenuItem: " + menuItem.getTitle());
                boolean result = setSelectedFragmentByMenuItem(menuItem.getItemId());
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
        mTextViewNumberOfRatings = mHeaderLayout.findViewById(R.id.number_of_ratings);
        mRatingBarRating = mHeaderLayout.findViewById(R.id.rating);

        mHeaderLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent profileActivityIntent = new Intent(MainActivity.this, ProfileActivity.class);
                MainActivity.this.startActivity(profileActivityIntent);
            }
        });


        // SetUp the MDrawerToogle for the toolbar
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.drawer_open, R.string.drawer_close);
        mDrawerLayout.addDrawerListener(mDrawerToggle);
        mDrawerToggle.syncState();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        // SetUp the Toast for showing information to the user
        mToast = Toast.makeText(this, "", Toast.LENGTH_SHORT);
    }

    /**
     * Sets in the content view the fragment given the one of the menu item id available
     *
     * @param menuItemId Id of the menu item that corresponds to the selected fragment
     * @return boolean value if the task succeeded or not
     */
    private boolean setSelectedFragmentByMenuItem(int menuItemId) {
        Fragment fragment = getFragment(menuItemId);
        boolean result = setSelectedFragment(fragment);
        checkMenuItem(menuItemId);
        return result;
    }

    /**
     * Set as checked the menu item selected by the user in the navigation menu. It also sets the
     * rest of the items as not checked so there is only one highlighted.
     *
     * @param menuItemId MenuItemId that was selected by the user
     */
    private void checkMenuItem(int menuItemId) {
        int size = mNavView.getMenu().size();
        // First of all we set all the menu items to false, and then we set the one that we want
        // as true
        for (int i = 0; i < size; i++) {
            mNavView.getMenu().getItem(i).setChecked(false);
        }
        MenuItem menuItem = mNavView.getMenu().findItem(menuItemId);
        menuItem.setChecked(true);
        setTitle(menuItem.getTitle());
    }

    /**
     * Puts the selected fragment in the content frame layout, selecting the same fragment that was
     * selected does nothing
     *
     * @param fragment the fragment you want to put in the content FrameLayout
     * @return boolean, true if it succeeds and false if it couldn't create the fragment instance
     */
    private boolean setSelectedFragment(Fragment fragment) {
        try {
            String tag = fragment.getClass().getName();
            if (mCurrentFragment != null && tag.equals(mCurrentFragment.getTag())) return false;

            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

            // We check if the fragment is on the stack, if is not we add it and show it
            if (fragmentManager.findFragmentByTag(tag) == null) {
                fragmentTransaction.add(R.id.content_frame, fragment, tag);
            } else {
                fragmentTransaction.show(fragment);
            }
            // We hid the current fragment that it is being shown
            if (mCurrentFragment != null) {
                fragmentTransaction.hide(mCurrentFragment);
            }
            // Finally we commit the transaction that has to be made and save the new fragment as
            // currentFragment
            fragmentTransaction.commit();
            mCurrentFragment = fragment;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * Depending on the menu item selected it returns the Fragment. This method should be changed
     * when new items are added to the menu. The SignOut item does not return any fragment.
     *
     * @param menuItemId Item Id that was selected in the menu
     * @return The fragment corresponding to the item selected or null if the menu item does
     * change the fragment of the content frame layout.
     */
    private Fragment getFragment(int menuItemId) {
        switch (menuItemId) {
            case R.id.menu_item_1:
                return mMapFragment;
            case R.id.menu_item_2:
                return mChatsFragment;
            case R.id.menu_item_3:
                return mCommuteFragment;
            case R.id.menu_item_4:
                return mJourneyHistoryFragment;
            case R.id.navigation_sign_out:
                sign_out();
                return null;
            default:
                return mMapFragment;
        }
    }

    /**
     * Signs out the current user from the account
     */
    private void sign_out() {
        AuthUI.getInstance().signOut(this).addOnCompleteListener(new OnCompleteListener<Void>() {
            public void onComplete(@NonNull Task<Void> task) {
                startBootActivity();
                Preferences mPreferences = (Preferences) getIntent().getSerializableExtra("journeyPreferences");
                Log.e(TAG,mPreferences.toString());
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

    public Toast getToast() {
        return mToast;
    }

    public MapsFragment getMapsFragment() {
        return (MapsFragment) mMapFragment;
    }

}
