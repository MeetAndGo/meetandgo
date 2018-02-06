package com.meetandgo.meetandgo;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.firebase.ui.auth.AuthUI;
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
import com.meetandgo.meetandgo.fragments.Chats;
import com.meetandgo.meetandgo.fragments.Commute;
import com.meetandgo.meetandgo.fragments.JourneyHistory;
import com.meetandgo.meetandgo.fragments.MapsFragment;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private DatabaseReference myRef;
    private String uid;
    private boolean exists = false;

    private Button btn_signout;

    private String[] mPlanetTitles;
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;
    private Toolbar mToolbar;
    private View mheaderLayout;
    private TextView mTextViewUserName;
    private TextView mTextViewUserEmail;
    private NavigationView navView;

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

        mPlanetTitles = getResources().getStringArray(R.array.planets_array);
        mDrawerLayout = findViewById(R.id.drawer_layout);

        navView = (NavigationView) findViewById(R.id.navigation);

        navView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {
                Log.d("MenuItemClicked", "MenuItem: " + menuItem.getTitle());

                Fragment fragment = null;
                Class fragmentClass;

                switch(menuItem.getItemId()) {
                    case R.id.menu_item_1:
                        fragmentClass = MapsFragment.class;
                        break;
                    case R.id.menu_item_2:
                        fragmentClass = Chats.class;
                        break;
                    case R.id.menu_item_3:
                        fragmentClass = Commute.class;
                        break;
                    case R.id.menu_item_4:
                        fragmentClass = JourneyHistory.class;
                        break;
                    case R.id.navigation_sign_out:
                        sign_out();
                    default:
                        fragmentClass = MapsFragment.class;
                }

                try {
                    fragment = (Fragment) fragmentClass.newInstance();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                FragmentManager fragmentManager = getSupportFragmentManager();
                fragmentManager.beginTransaction().replace(R.id.content_frame, fragment).commit();

                // Highlight the selected item has been done by NavigationView
                int size = navView.getMenu().size();
                for (int i = 0; i < size; i++) {
                    navView.getMenu().getItem(i).setChecked(false);
                }

                menuItem.setChecked(true);
                // Set action bar title
                setTitle(menuItem.getTitle());
                // Close the navigation drawer
                mDrawerLayout.closeDrawers();

                return true;
            }
        });

        mheaderLayout =   navView.getHeaderView(0);
        mTextViewUserName = mheaderLayout.findViewById(R.id.user_name);
        mTextViewUserEmail = mheaderLayout.findViewById(R.id.user_email);

        mDrawerToggle = new ActionBarDrawerToggle(
                this,                  /* host Activity */
                mDrawerLayout,         /* DrawerLayout object */
                R.string.drawer_open,  /* "open drawer" description */
                R.string.drawer_close  /* "close drawer" description */
        ) {

            /** Called when a drawer has settled in a completely closed state. */
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                getSupportActionBar().setTitle("Helou people");
            }

            /** Called when a drawer has settled in a completely open state. */
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                getSupportActionBar().setTitle("Open Navigation");
            }
        };

        // Set the drawer toggle as the DrawerListener
        mDrawerLayout.addDrawerListener(mDrawerToggle);

        mDrawerToggle.syncState();


        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);


        // TODO: Set database stuff in other method
        user = FirebaseAuth.getInstance().getCurrentUser();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        mTextViewUserName.setText(user.getDisplayName());
        mTextViewUserEmail.setText(user.getEmail());
        uid = user.getUid();
        Log.e("Database","uid:"+uid);
        // Write a message to the database
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        //Check if user already exists
        myRef = database.getReference("users");
        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {

                if (snapshot.hasChild(uid)) {
                    Log.e("Snapshot", "True");
                    exists = true;
                    addUser();
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        // ...
        //startActivity();
        //Intent myIntent = new Intent(MainActivity.this, MapsActivity.class);
        //startActivity(myIntent);
    }

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
            Log.e("Debug", "get_debug_keyhash() error: Package Manager name not found");
        } catch (NoSuchAlgorithmException e) {
            Log.e("Debug", "get_debug_keyhash() error: No such algorithm");
        }
    }

    protected void addUser()    {
        if(!exists) {
            User newuser = new User(user.getDisplayName(), user.getEmail(), 0.0, null);
            myRef.child(uid).push().setValue(newuser);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(mDrawerToggle.onOptionsItemSelected(item)){
            if (mDrawerLayout.isDrawerOpen(Gravity.START)) mDrawerLayout.closeDrawer(Gravity.START);
            else mDrawerLayout.openDrawer(Gravity.START);
        }
        return super.onOptionsItemSelected(item);
    }
}
