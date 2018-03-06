package com.meetandgo.meetandgo.fragments;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.gson.Gson;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;
import com.meetandgo.meetandgo.Constants;
import com.meetandgo.meetandgo.FirebaseDB;
import com.meetandgo.meetandgo.R;
import com.meetandgo.meetandgo.activities.PreferencesActivity;
import com.meetandgo.meetandgo.data.Loc;
import com.meetandgo.meetandgo.data.Preferences;
import com.meetandgo.meetandgo.data.Search;
import com.meetandgo.meetandgo.receivers.AddressResultReceiver;
import com.meetandgo.meetandgo.services.FetchAddressIntentService;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.meetandgo.meetandgo.Constants.PREFERENCES_EXTRA;

public class MapsFragment extends Fragment implements OnMapReadyCallback, GoogleMap.OnMyLocationClickListener {

    private static final String TAG = "MapsFragment";
    private static final int PREFERENCES_REQUEST_CODE = 1;
    private static final String STARTING_PREFERENCES = "STARTING_PREFERENCES";
    private LatLng DEFAULT_LOCATION = new LatLng(53.341563, -6.253010);
    private static final int DEFAULT_ZOOM = 13;

    private static final String KEY_CAMERA_POSITION = "camera_position";
    private static final String KEY_LOCATION = "location";
    private static final String KEY_MARKER_LOCATION = "marker_location";

    private PermissionListener mLocationPermissionListener;

    private GoogleMap mMap;
    private boolean mLocationPermissionGranted = false;
    private Location mLastKnownLocation;
    private Location mLastKnownMarkerLocation;
    private Marker mMarkerDestination;
    private Location mStartLocation;
    private Location mEndLocation;

    private AddressResultReceiver mResultReceiver;

    private View mView;
    @BindView(R.id.map) MapView mMapView;
    @BindView(R.id.fab) FloatingActionButton mFab;
    @BindView(R.id.bottomSheetContent) LinearLayout mRelativeSheetContent;
    @BindView(R.id.imageViewMapCenter) ImageView mImageViewMapCenter;
    @BindView(R.id.textViewStartLocation) TextView mTextViewStartLocation;
    @BindView(R.id.textViewEndLocation) TextView mTextViewEndLocation;
    private TextView mTextViewCurrentFocus;
    @BindView(R.id.startLocationLayout) LinearLayout mStartLocationLayout;
    @BindView(R.id.endLocationLayout) LinearLayout mEndLocationLayout;
    @BindView(R.id.preferencesLayout) LinearLayout mPreferencesLayout;
    @BindView(R.id.buttonSearch) Button mSearchButton;

    private OnCompleteListener mOnCompleteListenerMove;
    private OnCompleteListener mOnCompleteListenerAnimate;
    private boolean mUserIsDragging;

    private Preferences mPreferences;
    private SharedPreferences mSharedPreferences;


    public MapsFragment() {

    }

    public static Fragment newInstance() {
        MapsFragment fragment = new MapsFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        if (savedInstanceState != null) {
            mLastKnownLocation = savedInstanceState.getParcelable(KEY_LOCATION);
            mLastKnownMarkerLocation = savedInstanceState.getParcelable(KEY_MARKER_LOCATION);
        } else {
            mLastKnownMarkerLocation = convertLatLngToLocation(DEFAULT_LOCATION);
        }
        mResultReceiver = new AddressResultReceiver(getActivity(), new Handler());
        mSharedPreferences = getActivity().getPreferences(Context.MODE_PRIVATE);
        setUpPreferences();

    }

    private void setUpPreferences() {
        Gson gson = new Gson();
        String json = mSharedPreferences.getString(STARTING_PREFERENCES, "");
        mPreferences = gson.fromJson(json, Preferences.class);
        if (mPreferences == null) mPreferences = new Preferences();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mView = inflater.inflate(R.layout.fragment_maps, container, false);
        ButterKnife.bind(this, mView);
        setUpUI();
        return mView;
    }

    /**
     * SetUps the UI of the whole fragment
     */
    private void setUpUI() {
        mTextViewCurrentFocus = mTextViewStartLocation;
        setupBottomSheet(mView);
        setUpOnCompleteListeners();

        // SetUp the map fragment and all the methods needed for the map API to work
        setUpMap();

        // Floating Button OnClickListener
        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getDeviceLocation(mOnCompleteListenerAnimate);
            }
        });
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        // In order to use the location feature we need to ask for location permission
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(DEFAULT_LOCATION, DEFAULT_ZOOM));
        mMarkerDestination = mMap.addMarker(new MarkerOptions().position(DEFAULT_LOCATION).visible(false));
        mMap.setOnMyLocationClickListener(this);
        mMap.getUiSettings().setRotateGesturesEnabled(false);
        mMap.getUiSettings().setMyLocationButtonEnabled(false);
        mMap.getUiSettings().setMapToolbarEnabled(false);
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                MapsFragment.this.onMapClick(latLng);
            }

        });


        mMap.setOnCameraMoveListener(new GoogleMap.OnCameraMoveListener() {
            @Override
            public void onCameraMove() {
                // We put the marker on the center of the mImageViewMapCenter
                int markerX = (int) mImageViewMapCenter.getX() + mImageViewMapCenter.getWidth() / 2;
                int markerY = (int) mImageViewMapCenter.getY() + mImageViewMapCenter.getHeight() / 2;
                Point point = new Point(markerX, markerY);
                putMarkerOnPoint(point);

            }
        });
        mMap.setOnCameraMoveStartedListener(new GoogleMap.OnCameraMoveStartedListener() {
            @Override
            public void onCameraMoveStarted(int reason) {
                if (reason == GoogleMap.OnCameraMoveStartedListener.REASON_GESTURE) {
                    mUserIsDragging = true;
                }
            }
        });

        mMap.setOnCameraIdleListener(new GoogleMap.OnCameraIdleListener() {
            @Override
            public void onCameraIdle() {
                if (!mUserIsDragging) return;
                mUserIsDragging = false;
                LatLng cameraLatLng = mMap.getCameraPosition().target;
                // Depending on the current view that is selected we set the location to either start or end location
                getLocationName(convertLatLngToLocation(cameraLatLng));
                if (mTextViewCurrentFocus.getId() == mTextViewStartLocation.getId()) {
                    mStartLocation = convertLatLngToLocation(cameraLatLng);
                } else {
                    mEndLocation = convertLatLngToLocation(cameraLatLng);
                }
                setButtonState();

            }
        });

        manageLocationPermission();
    }

    /**
     * Checks the search information from the activity and turns the button to enable or disable
     */
    private void setButtonState() {
        if (mStartLocation == null || mEndLocation == null) {
            mSearchButton.setEnabled(false);
            mSearchButton.setTextColor(getActivity().getColor(R.color.colorGrey));
        } else {
            mSearchButton.setEnabled(true);
            mSearchButton.setTextColor(getActivity().getColor(R.color.colorWhite));
        }

    }

    /**
     * Handles the click of the current location of the user. This method is overriding a method by
     * the Google MAPs API
     *
     * @param location Loc of the current user
     */
    @Override
    public void onMyLocationClick(@NonNull Location location) {
        getLocationName(location);
    }

    /**
     * This is method is called when the device suffers any changes, such as screen rotation or change
     * of the screensize. We save variables in order to be accessed one those changes have finished,
     * in order to give a better user experience to the user.
     *
     * @param outState
     */
    @Override
    public void onSaveInstanceState(Bundle outState) {
        if (mMap != null) {
            outState.putParcelable(KEY_CAMERA_POSITION, mMap.getCameraPosition());
            outState.putParcelable(KEY_LOCATION, mLastKnownLocation);
            outState.putParcelable(KEY_MARKER_LOCATION, mLastKnownMarkerLocation);
            super.onSaveInstanceState(outState);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mView = null;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, requestCode + " " + resultCode);
        if (requestCode == PREFERENCES_REQUEST_CODE) {
            mPreferences = (Preferences) data.getSerializableExtra(PREFERENCES_EXTRA);
            SharedPreferences.Editor prefsEditor = mSharedPreferences.edit();
            Gson gson = new Gson();
            String json = gson.toJson(mPreferences);
            Log.d(TAG, json);
            prefsEditor.putString(STARTING_PREFERENCES, json);
            prefsEditor.apply();
            Log.d(TAG, mSharedPreferences.getString(STARTING_PREFERENCES, ""));

        }
    }

    private void setUpMap() {
        // Map Fragment containing the Google MAP is added to the content layout
        FragmentManager manager = getFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        SupportMapFragment mapFragment = new SupportMapFragment();
        transaction.add(R.id.map, mapFragment);
        transaction.commit();
        // We load the map in an async way
        mapFragment.getMapAsync(this);
    }

    /**
     * Actions to be done when the map is clicked by the user
     *
     * @param latLng
     */
    private void onMapClick(LatLng latLng) {
        putMarkerOnLocation(latLng);
        Location location = convertLatLngToLocation(latLng);
        animateCameraToLocation(location);
        getLocationName(location);
        // Depending on the current view that is selected we set the location to either start or end location
        getLocationName(convertLatLngToLocation(latLng));
        if (mTextViewCurrentFocus.getId() == mTextViewStartLocation.getId()) {
            mStartLocation = convertLatLngToLocation(latLng);
        } else {
            mEndLocation = convertLatLngToLocation(latLng);
        }
        setButtonState();
    }

    /**
     * Manage the local permission, if the user does grant the LOCATION permission we continue showing
     * the map. If not we can not set the current user location and we try to setup everything
     * so that the user does not run into any problems.
     *
     * @return boolean indicating if the location was granted or not
     */
    private boolean manageLocationPermission() {
        mLocationPermissionListener = new PermissionListener() {
            @Override
            public void onPermissionGranted(PermissionGrantedResponse response) {
                Log.d(TAG, "PermissionGranted -> " + response.getPermissionName());
                mLocationPermissionGranted = true;
                setMyLocationEnabled();
                getDeviceLocation(mOnCompleteListenerMove);
            }

            @Override
            public void onPermissionDenied(PermissionDeniedResponse response) {
                Log.d(TAG, "PermissionDenied -> " + response.getPermissionName());
                mLocationPermissionGranted = false;
            }

            @Override
            public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {
                Log.d(TAG, "OnPermissionRationaleShouldBeShown -> " + permission.getName());
                token.continuePermissionRequest();
                mLocationPermissionGranted = false;
            }
        };

        Dexter.withActivity(getActivity()).withPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                .withListener(mLocationPermissionListener).check();

        return mLocationPermissionGranted;
    }

    private void setMyLocationEnabled() {
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
        }
    }


    /**
     * Setup the bottom sheet used for setting the preferences for the matching
     *
     * @param view The fragment view needed for finding the view id of the bottom sheet
     */
    private void setupBottomSheet(@NonNull View view) {
        mStartLocationLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mTextViewCurrentFocus = mTextViewStartLocation;
            }
        });

        mEndLocationLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mTextViewCurrentFocus = mTextViewEndLocation;
            }
        });

        mPreferencesLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startPreferencesActivity();
            }
        });
        mSearchButton.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View view) {
                clickSearchButton();
            }
        });
        setButtonState();
    }

    /**
     *
     */
    private void clickSearchButton() {
        Loc sLocation = new Loc(mStartLocation.getLatitude(), mStartLocation.getLongitude());
        Loc eLocation = new Loc(mEndLocation.getLatitude(), mEndLocation.getLongitude());
        Search searchTest = new Search(mPreferences, sLocation, eLocation);
        FirebaseDB.addSearch(searchTest);
    }

    /**
     * Creates an intent, adds location data to it as an extra, and starts the intent service for
     * fetching an address.
     */
    private void getLocationName(Location location) {
        // Create an intent for passing to the intent service responsible for fetching the address.
        Intent intent = new Intent(getActivity(), FetchAddressIntentService.class);

        // Pass the result receiver as an extra to the service.
        intent.putExtra(Constants.RECEIVER, mResultReceiver);

        // Pass the location data as an extra to the service.
        intent.putExtra(Constants.LOCATION_DATA_EXTRA, location);

        // Start the service. If the service isn't already running, it is instantiated and started
        // (creating a process for it if needed); if it is running then it remains running. The
        // service kills itself automatically once all intents are processed.
        getActivity().startService(intent);
    }

    /**
     * Get the best and most recent location of the device, which may be null in rare
     * cases when a location is not available. Important: This runs on other thread, assigning
     * variables to it will give an error. Only set things on the OnComplete callback
     *
     * @return location The last device location available
     */
    private void getDeviceLocation(OnCompleteListener onCompleteListener) {
        // This OnCompleteListener waits for when the task is completed, it also assigns the
        // mLastKnownLocation variable to the location received and moves the map to the location.
        try {
            if (mLocationPermissionGranted) {
                FusedLocationProviderClient mFusedLocationProviderClient =
                        LocationServices.getFusedLocationProviderClient(getActivity());
                Task locationResult = mFusedLocationProviderClient.getLastLocation();
                locationResult.addOnCompleteListener(getActivity(), onCompleteListener);
            } else {
                manageLocationPermission();
            }
        } catch (SecurityException e) {
            Log.e("Exception: %s", e.getMessage());
        }
    }

    /**
     * SetUp the completion listeners for the move and animate camera methods. When the user
     * starts the app we want to move the camera fast to some position without any animation. If
     * they click the button "MyLocation" show the animation and move smoothly to the required position
     */
    private void setUpOnCompleteListeners() {
        mOnCompleteListenerMove = new OnCompleteListener() {
            @Override
            public void onComplete(@NonNull Task task) {
                mLastKnownLocation = (Location) task.getResult();
                if (!task.isSuccessful() || mLastKnownLocation == null) {
                    Log.d(TAG, "Current location is null. Using defaults.");
                    mLastKnownLocation = convertLatLngToLocation(DEFAULT_LOCATION);
                }
                moveCameraToLocation(mLastKnownLocation);
                putMarkerOnLocation(new LatLng(mLastKnownLocation.getLatitude(),
                        mLastKnownLocation.getLongitude()));
            }
        };

        mOnCompleteListenerAnimate = new OnCompleteListener() {
            @Override
            public void onComplete(@NonNull Task task) {
                mLastKnownLocation = (Location) task.getResult();
                if (!task.isSuccessful() || mLastKnownLocation == null) {
                    Log.d(TAG, "Current location is null. Using defaults.");
                    mLastKnownLocation = convertLatLngToLocation(DEFAULT_LOCATION);
                }
                animateCameraToLocation(mLastKnownLocation);
                putMarkerOnLocation(new LatLng(mLastKnownLocation.getLatitude(),
                        mLastKnownLocation.getLongitude()));
            }
        };
    }

    public Point convertLatLngToPixels(LatLng latLng) {
        Projection projection = mMap.getProjection();
        Point point = projection.toScreenLocation(latLng);
        return point;
    }

    public LatLng convertPixelsToLatLng(Point point) {
        Projection projection = mMap.getProjection();
        return projection.fromScreenLocation(point);
    }

    private Location convertLatLngToLocation(LatLng latLng) {
        Location newLocation = new Location("");
        newLocation.setLatitude(latLng.latitude);
        newLocation.setLongitude(latLng.longitude);
        return newLocation;
    }

    private void putMarkerOnLocation(@NonNull LatLng latLng) {
        mLastKnownMarkerLocation = convertLatLngToLocation(latLng);
        mMarkerDestination.setPosition(latLng);
    }

    private void putMarkerOnPoint(Point point) {
        LatLng latLng = convertPixelsToLatLng(point);
        putMarkerOnLocation(latLng);
    }

    private void animateCameraToLocation(Location location) {
        LatLng newLocation = new LatLng(location.getLatitude(), location.getLongitude());
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(newLocation, DEFAULT_ZOOM));
    }

    private void moveCameraToLocation(Location location) {
        LatLng newLocation = new LatLng(location.getLatitude(), location.getLongitude());
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(newLocation, DEFAULT_ZOOM));
    }

    public void setAddressInView(String address) {
        if (mTextViewCurrentFocus == null) return;
        mTextViewCurrentFocus.setText(address);
    }

    private void startPreferencesActivity() {
        Intent preferencesIntent = new Intent(getActivity(), PreferencesActivity.class);
        startActivityForResult(preferencesIntent, PREFERENCES_REQUEST_CODE);
        getActivity().overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left);
    }
}
