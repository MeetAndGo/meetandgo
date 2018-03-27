package com.meetandgo.meetandgo.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.meetandgo.meetandgo.Constants;
import com.meetandgo.meetandgo.FireBaseDB;
import com.meetandgo.meetandgo.R;
import com.meetandgo.meetandgo.data.Journey;
import com.meetandgo.meetandgo.data.Preferences;
import com.meetandgo.meetandgo.data.User;
import com.meetandgo.meetandgo.fragments.ChatsFragment;
import com.meetandgo.meetandgo.fragments.CommuteFragment;
import com.meetandgo.meetandgo.fragments.JourneyHistoryFragment;
import com.meetandgo.meetandgo.fragments.MapsFragment;
import com.yarolegovich.lovelydialog.LovelyStandardDialog;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import butterknife.BindView;
import butterknife.ButterKnife;
import nl.dionsegijn.konfetti.KonfettiView;
import nl.dionsegijn.konfetti.models.Shape;
import nl.dionsegijn.konfetti.models.Size;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    @BindView(R.id.drawer_layout)
    DrawerLayout mDrawerLayout;
    @BindView(R.id.navigation)
    NavigationView mNavView;
    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.konfetti_view)
    KonfettiView mKonfettiView;

    private TextView mTextViewUserName;
    private TextView mTextViewInitials;
    private TextView mTextViewUserEmail;
    private ActionBarDrawerToggle mDrawerToggle;

    private Fragment mCurrentFragment;
    private Fragment mMapFragment;
    private Fragment mChatsFragment;
    private Fragment mJourneyHistoryFragment;
    private Fragment mCommuteFragment;

    private Toast mToast;
    private SharedPreferences mPrefs;

    @Override
    protected void onStart() {
        super.onStart();
        mToolbar.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.toolbarColor));
        String comingFromJourney = getIntent().getStringExtra(Constants.JOURNEY_ACTIVITY_EXTRA);
        if (comingFromJourney != null && comingFromJourney.equals("journey_activity")) {
            // Get the journey from the intent
            String json = getIntent().getStringExtra(Constants.JOURNEY_EXTRA);
            Gson gson = new Gson();
            Journey journey = gson.fromJson(json, Journey.class);
            if (journey != null) {
                setSelectedFragmentByMenuItem(R.id.menu_item_chat);
                ((ChatsFragment) mChatsFragment).setJourney(journey);
            }

        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPrefs = getSharedPreferences(Constants.SHARED_PREFERENCES_NAME, MODE_PRIVATE);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        setSupportActionBar(mToolbar);

        // Get the debug KeyHash for facebook login (Not useful on release version)
        getDebugKeyHash();
        setUpMenuFragments();
        FireBaseDB.initializeApp(this);

        setupUI();
        setUpUser();

        // Set the maps fragment as a default fragment on Start
        setSelectedFragmentByMenuItem(R.id.menu_item_map);

    }

    /**
     * SetUp the actions related to setting up the user when the main activity is started and customize
     * the UI for the current user
     */
    private void setUpUser() {
        final User currentUser = FireBaseDB.getCurrentUser();
        // ValueEventListener needed to get the return of asking the database for the user
        ValueEventListener mUserValueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                Log.e(TAG, "in OnDataChange from event listener" + snapshot.toString());
                if (snapshot.getValue(User.class) == null) {
                    askGender(currentUser);
                } else if (FireBaseDB.getCurrentUserID() == null) startBootActivity();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        };
        // Add current user to the database
        FireBaseDB.isUserInDB(FireBaseDB.getCurrentUserID(), mUserValueEventListener);


        // UpdateUI based on the current user that is using the app
        mTextViewUserName.setText(currentUser.getFullName());
        mTextViewUserEmail.setText(currentUser.getEmail());
        // Set Initials of the image
        String initials = "";
        for (String s : currentUser.getFullName().split(" ")) {
            initials += s.charAt(0);
        }
        initials = initials.substring(0, 2);
        mTextViewInitials.setText(initials);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle the selection of the burger toggle that manages the drawer layout
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            if (mDrawerLayout.isDrawerOpen(Gravity.START)) mDrawerLayout.closeDrawer(Gravity.START);
            else mDrawerLayout.openDrawer(Gravity.START);
            hideKeyboard();
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Hides the users phone keyboard
     */
    private void hideKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    /**
     * Checks what preferredGender the user is.
     */
    private void askGender(final User mUser) {
        new LovelyStandardDialog(this, LovelyStandardDialog.ButtonLayout.HORIZONTAL)
                .setTopColorRes(R.color.colorPrimaryDark)
                .setButtonsColorRes(R.color.colorPrimary)
                .setIcon(R.drawable.ic_face_white_48dp)
                .setMessage(R.string.gender_message)
                .setPositiveButton(R.string.gender_male, new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        mUser.setGender(Preferences.Gender.MALE);
                        addUserToDatabase(mUser);

                    }
                })
                .setNeutralButton(R.string.other, new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        mUser.setGender(Preferences.Gender.ANY);
                        addUserToDatabase(mUser);

                    }
                })
                .setNegativeButton(R.string.gender_female, new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        mUser.setGender(Preferences.Gender.FEMALE);
                        addUserToDatabase(mUser);
                    }
                })
                .show();
    }

    /**
     * Adds the user to the database, and also saves the user to the local storage in order to be
     * easier to retrieve once there is no internet connection.
     *
     * @param user The user to be added
     */
    private void addUserToDatabase(User user) {
        // Save it to database
        FireBaseDB.addUser(user);
        // Save a copy to the local storage
        SharedPreferences.Editor prefsEditor = mPrefs.edit();
        Gson gson = new Gson();
        String json = gson.toJson(user);
        prefsEditor.putString(Constants.CURRENT_USER, json);
        prefsEditor.apply();
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

    /**
     * Sets up all the different elements of the UI
     */
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
        View mHeaderLayout = mNavView.getHeaderView(0);
        mTextViewUserName = mHeaderLayout.findViewById(R.id.user_name);
        mTextViewUserEmail = mHeaderLayout.findViewById(R.id.user_email);
        mTextViewInitials = mHeaderLayout.findViewById(R.id.initials);

        mHeaderLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent profileActivityIntent = new Intent(MainActivity.this, ProfileActivity.class);
                MainActivity.this.startActivity(profileActivityIntent);
            }
        });


        // SetUp the MDrawerToogle for the toolbar
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.drawer_open, R.string.drawer_close) {
            @Override
            public void onDrawerStateChanged(int newState) {
                hideKeyboard();
            }
        };
        
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
    public boolean setSelectedFragmentByMenuItem(int menuItemId) {
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
    public boolean setSelectedFragment(Fragment fragment) {
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
            case R.id.menu_item_map:
                return mMapFragment;
            case R.id.menu_item_chat:
                return mChatsFragment;
            case R.id.menu_item_daily_commute:
                return mCommuteFragment;
            case R.id.menu_item_journey_history:
                return mJourneyHistoryFragment;
            case R.id.navigation_sign_out:
                signOut();
                return null;
            default:
                return mMapFragment;
        }
    }

    /**
     * Signs out the current user from the account
     */
    private void signOut() {
        AuthUI.getInstance().signOut(this).addOnCompleteListener(new OnCompleteListener<Void>() {
            public void onComplete(@NonNull Task<Void> task) {
                FireBaseDB.removeUserFromLocalStorage();
                startBootActivity();
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

    /**
     * Opens the chat fragment substituting the chat to the journey chat
     *
     * @param journey
     */
    public void openChatFragment(Journey journey) {
        setSelectedFragmentByMenuItem(R.id.menu_item_chat);
        ((ChatsFragment) mChatsFragment).setJourney(journey);
    }

    /**
     * Sets the visibility of the chat menu item to either true or false depending if there is any
     * active chat.
     *
     * @param b
     */
    public void setChatMenuItemVisibility(boolean b) {
        Menu nav_Menu = mNavView.getMenu();
        nav_Menu.findItem(R.id.menu_item_chat).setVisible(b);
    }

    public void runKonfettiAnimation() {
        mKonfettiView.build()
                .addColors(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimary),
                        ContextCompat.getColor(getApplicationContext(), R.color.activeJourney),
                        ContextCompat.getColor(getApplicationContext(), R.color.inactiveJourney))
                .setDirection(0, 180)
                .setSpeed(4f, 10f)
                .setFadeOutEnabled(true)
                .setTimeToLive(3000L)
                .addShapes(Shape.RECT)
                .addSizes(new Size(10, 6f))
                .setPosition(-50f, mKonfettiView.getWidth() + 50f, -50f, -50f)
                .stream(50, 1000L);
    }

    /**
     * Custom onBackPressed for changing the fragment
     */
    @Override
    public void onBackPressed() {
        if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            mDrawerLayout.closeDrawer(GravityCompat.START);
        } else {
            if (mCurrentFragment.getTag() == mChatsFragment.getClass().getName()) {
                setSelectedFragmentByMenuItem(R.id.menu_item_journey_history);
            } else if (mCurrentFragment.getTag() == mJourneyHistoryFragment.getClass().getName()) {
                setSelectedFragmentByMenuItem(R.id.menu_item_map);
            } else {
                super.onBackPressed();
            }
        }

    }

    public void saveLastActiveChat(Journey journey) {
        SharedPreferences.Editor prefsEditor = mPrefs.edit();
        Gson gson = new Gson();
        String json = gson.toJson(journey);
        prefsEditor.putString(Constants.CURRENT_JOURNEY, json);
        prefsEditor.apply();
    }

    public Journey getLastActiveChat() {
        // Get Current user saved in the phone, if it doesn't exist use a new one created for this
        Gson gson = new Gson();
        String json = mPrefs.getString(Constants.CURRENT_JOURNEY, "");
        return gson.fromJson(json, Journey.class);

    }
}
