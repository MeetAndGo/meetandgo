package com.meetandgo.meetandgo.fragments;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Point;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
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
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.JointType;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.RoundCap;
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
import com.meetandgo.meetandgo.FireBaseDB;
import com.meetandgo.meetandgo.R;
import com.meetandgo.meetandgo.activities.MatchingResultsActivity;
import com.meetandgo.meetandgo.activities.PreferencesActivity;
import com.meetandgo.meetandgo.data.Loc;
import com.meetandgo.meetandgo.data.Preferences;
import com.meetandgo.meetandgo.data.Search;
import com.meetandgo.meetandgo.data.User;
import com.meetandgo.meetandgo.receivers.AddressResultReceiver;
import com.meetandgo.meetandgo.services.FetchAddressIntentService;
import com.meetandgo.meetandgo.utils.GoogleMap.GMapV2Direction;
import com.meetandgo.meetandgo.utils.GoogleMap.GMapV2DirectionAsyncTask;
import com.meetandgo.meetandgo.utils.MapUtils;

import org.w3c.dom.Document;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MapsFragment extends Fragment implements OnMapReadyCallback, GoogleMap.OnMyLocationClickListener {

    private static final String TAG = "MapsFragment";
    private static final int PREFERENCES_REQUEST_CODE = 1;
    private static final String STARTING_PREFERENCES = "STARTING_PREFERENCES";

    private static final String KEY_CAMERA_POSITION = "camera_position";
    private static final String KEY_LOCATION = "location";
    private static final String KEY_MARKER_LOCATION = "marker_location";

    private GoogleMap mMap;
    private boolean mLocationPermissionGranted = false;
    private Location mLastKnownLocation;
    private Location mLastKnownMarkerLocation;
    private Marker mMarkerDestination;
    private Location mStartLocation;
    private Location mEndLocation;
    private Polyline mMapPolyline;
    private Circle mStartCircle;
    private Circle mEndCircle;

    private AddressResultReceiver mResultReceiver;

    private View mView;
    @BindView(R.id.map) MapView mMapView;
    @BindView(R.id.fab) FloatingActionButton mFab;
    @BindView(R.id.imageViewMapCenter) ImageView mImageViewMapCenter;
    @BindView(R.id.textViewStartLocation) TextView mTextViewStartLocation;
    @BindView(R.id.textViewEndLocation) TextView mTextViewEndLocation;
    private TextView mTextViewCurrentFocus;
    @BindView(R.id.startLocationLayout) LinearLayout mStartLocationLayout;
    @BindView(R.id.endLocationLayout) LinearLayout mEndLocationLayout;
    @BindView(R.id.preferencesLayout) LinearLayout mPreferencesLayout;
    @BindView(R.id.startLocationImage) ImageView mStartLocationImage;
    @BindView(R.id.endLocationImage) ImageView mEndLocationImage;
    @BindView(R.id.buttonSearch) Button mSearchButton;

    private OnCompleteListener mOnCompleteListenerMove;
    private OnCompleteListener mOnCompleteListenerAnimate;
    private boolean mUserIsDragging;

    private Preferences mPreferences;
    private SharedPreferences mSharedPreferences;

    public ArrayList<Search> resultSearches = new ArrayList<>();

    public MapsFragment() {
    }

    public static Fragment newInstance() {
        return new MapsFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        if (savedInstanceState != null) {
            mLastKnownLocation = savedInstanceState.getParcelable(KEY_LOCATION);
            mLastKnownMarkerLocation = savedInstanceState.getParcelable(KEY_MARKER_LOCATION);
        } else {
            mLastKnownMarkerLocation = MapUtils.convertLatLngToLocation(Constants.DEFAULT_LOCATION);
        }
        mResultReceiver = new AddressResultReceiver(getActivity(), new Handler());
        mSharedPreferences = getActivity().getPreferences(Context.MODE_PRIVATE);
        setUpPreferences();

    }

    /**
     * Sets up preferences for the current user
     */
    private void setUpPreferences() {
        User currenUser = FireBaseDB.getCurrentUser();
        Gson gson = new Gson();
        String json = mSharedPreferences.getString(STARTING_PREFERENCES, "");
        mPreferences = gson.fromJson(json, Preferences.class);
        if (mPreferences == null) mPreferences = new Preferences(currenUser);
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
        mStartLocationImage.setColorFilter(ContextCompat.getColor(getContext(), R.color.colorPrimary));
        mEndLocationImage.setColorFilter(ContextCompat.getColor(getContext(), R.color.colorSoftSoftGrey));

        setupBottomSheet();
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
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(Constants.DEFAULT_LOCATION, Constants.DEFAULT_ZOOM));
        mMarkerDestination = mMap.addMarker(new MarkerOptions().position(Constants.DEFAULT_LOCATION).visible(false));
        mMap.setOnMyLocationClickListener(this);
        mMap.getUiSettings().setRotateGesturesEnabled(false);
        mMap.getUiSettings().setMyLocationButtonEnabled(false);
        mMap.getUiSettings().setMapToolbarEnabled(false);

        setUpMapListeners();
        manageLocationPermission();
        setupMapStyle();
    }

    /**
     * Initializes and setups all the map listeners used.
     */
    private void setUpMapListeners() {
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                MapsFragment.this.onMapClick(latLng);
            }

        });

        // When the camera is being moved we se the marker to the actual position in order to
        // have the location of the center point.
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
        // Called when the camera starts moving from a static position
        mMap.setOnCameraMoveStartedListener(new GoogleMap.OnCameraMoveStartedListener() {
            @Override
            public void onCameraMoveStarted(int reason) {
                if (reason == GoogleMap.OnCameraMoveStartedListener.REASON_GESTURE) {
                    mUserIsDragging = true;
                }
            }
        });
        // Idle listener is used for when the camera is moved and gets back to an idle position
        mMap.setOnCameraIdleListener(new GoogleMap.OnCameraIdleListener() {
            @Override
            public void onCameraIdle() {
                if (!mUserIsDragging) return;
                mUserIsDragging = false;
                LatLng cameraLatLng = mMap.getCameraPosition().target;
                // Depending on the current view that is selected we set the location to either start or end location
                setCurrentFocusedTextViewLocation(MapUtils.convertLatLngToLocation(cameraLatLng));
                if (mStartLocation != null && mEndLocation != null) {
                    route(MapUtils.convertLocationToLatLng(mStartLocation),
                            MapUtils.convertLocationToLatLng(mEndLocation),
                            mPreferences.getMode() == Preferences.Mode.WALK ? GMapV2Direction.MODE_WALKING : GMapV2Direction.MODE_DRIVING);
                }

            }
        });
    }

    /**
     * Checks the search information from the activity and turns the button to enable or disable
     */
    private void setSearchButtonState() {
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
        if (requestCode == PREFERENCES_REQUEST_CODE) {
            mPreferences = (Preferences) data.getSerializableExtra(Constants.PREFERENCES_EXTRA);
            SharedPreferences.Editor prefsEditor = mSharedPreferences.edit();
            Gson gson = new Gson();
            String json = gson.toJson(mPreferences);
            prefsEditor.putString(STARTING_PREFERENCES, json);
            prefsEditor.apply();

        }
    }

    /**
     * Sets up the map for the fragment
     */
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
     * Adds the style form the json file to the map, and customizes the map options
     */
    private void setupMapStyle() {
        try {
            // Customise the styling of the base map using a JSON object defined
            // in a raw resource file.
            boolean success = mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(getActivity(), R.raw.map_style));
            if (!success) {
                Log.e(TAG, "Style parsing failed.");
            }
        } catch (Resources.NotFoundException e) {
            Log.e(TAG, "Can't find style. Error: ", e);
        }
    }

    /**
     * Actions to be done when the map is clicked by the user
     *
     * @param latLng Latitude and longitude of the tap position on the map
     */
    private void onMapClick(LatLng latLng) {
        putMarkerOnLocation(latLng);
        Location location = MapUtils.convertLatLngToLocation(latLng);
        MapUtils.animateCameraToLocation(mMap, location);
        // Depending on the current view that is selected we set the location to either start or end location
        setCurrentFocusedTextViewLocation(MapUtils.convertLatLngToLocation(latLng));
    }

    /**
     * Manage the local permission, if the user does grant the LOCATION permission we continue showing
     * the map. If not we can not set the current user location and we try to setup everything
     * so that the user does not run into any problems.
     *
     * @return boolean indicating if the location was granted or not
     */
    private void manageLocationPermission() {
        PermissionListener locationPermissionListener = new PermissionListener() {
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
                .withListener(locationPermissionListener).check();
    }

    /**
     * After performing the permission checks this method allows to access user location
     */
    private void setMyLocationEnabled() {
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
        }
    }


    /**
     * Setup the bottom sheet used for setting the preferences for the matching
     */
    private void setupBottomSheet() {
        // Setting up the start location layout listener, when clicked it will set the current focus
        // to it
        mStartLocationLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mTextViewCurrentFocus = mTextViewStartLocation;
                mStartLocationImage.setColorFilter(ContextCompat.getColor(getContext(), R.color.colorPrimary));
                mEndLocationImage.setColorFilter(ContextCompat.getColor(getContext(), R.color.colorSoftSoftGrey));
            }
        });
        // Setting up the end location layout listener, when clicked it will set the current focus
        // to it
        mEndLocationLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mTextViewCurrentFocus = mTextViewEndLocation;
                mEndLocationImage.setColorFilter(ContextCompat.getColor(getContext(), R.color.colorPrimary));
                mStartLocationImage.setColorFilter(ContextCompat.getColor(getContext(), R.color.colorSoftSoftGrey));

            }
        });
        // Start the preferences layout when the button is clicked
        mPreferencesLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startPreferencesActivity();
            }
        });
        // Listener called when the search button is clicked
        mSearchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onSearchButtonClick();
            }
        });

        setSearchButtonState();
    }

    /**
     * Actions performed upon clicking the search button
     */
    private void onSearchButtonClick() {
        resultSearches.clear();
        Loc sLocation = new Loc(mStartLocation.getLatitude(), mStartLocation.getLongitude());
        Loc eLocation = new Loc(mEndLocation.getLatitude(), mEndLocation.getLongitude());
        final Search currentUserSearch = new Search(mPreferences, "", sLocation, eLocation, mTextViewStartLocation.getText().toString(), mTextViewEndLocation.getText().toString());

        String searchID = FireBaseDB.addNewSearch(currentUserSearch);
        currentUserSearch.setSearchID(searchID);
        startMatchingResultsActivity(currentUserSearch);

    }

    /**
     * Starts the matching results activity after a button is click
     *
     * @param search current user search
     */
    private void startMatchingResultsActivity(Search search) {
        Intent matchingResultsIntent = new Intent(getActivity(), MatchingResultsActivity.class);
        Gson gson = new Gson();
        String json = gson.toJson(search);
        matchingResultsIntent.putExtra(Constants.CURRENT_USER_SEARCH_EXTRA, json);
        startActivity(matchingResultsIntent);
    }

    /**
     * Creates an intent, adds location data to it as an extra, and starts the intent service for
     * fetching an address.
     */
    public void getLocationName(Location location) {
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
                    mLastKnownLocation = MapUtils.convertLatLngToLocation(Constants.DEFAULT_LOCATION);
                }
                MapUtils.moveCameraToLocation(mMap, mLastKnownLocation);
                putMarkerOnLocation(new LatLng(mLastKnownLocation.getLatitude(),
                        mLastKnownLocation.getLongitude()));
                setCurrentFocusedTextViewLocation(mLastKnownLocation);
            }
        };

        mOnCompleteListenerAnimate = new OnCompleteListener() {
            @Override
            public void onComplete(@NonNull Task task) {
                mLastKnownLocation = (Location) task.getResult();
                if (!task.isSuccessful() || mLastKnownLocation == null) {
                    Log.d(TAG, "Current location is null. Using defaults.");
                    mLastKnownLocation = MapUtils.convertLatLngToLocation(Constants.DEFAULT_LOCATION);
                }
                MapUtils.animateCameraToLocation(mMap, mLastKnownLocation);
                putMarkerOnLocation(new LatLng(mLastKnownLocation.getLatitude(),
                        mLastKnownLocation.getLongitude()));
                setCurrentFocusedTextViewLocation(mLastKnownLocation);
            }
        };
    }

    /**
     * Sets the location string to the focused textview when the location string is found in the other
     * thread
     *
     * @param location
     */
    private void setCurrentFocusedTextViewLocation(Location location) {
        getLocationName(location);
        // Depending on the current view that is selected we set the location to either start or end location
        if (mTextViewCurrentFocus.getId() == mTextViewStartLocation.getId()) {
            mStartLocation = location;
        } else {
            mEndLocation = location;
        }

        //setSearchButtonState();
    }

    protected void route(final LatLng sourcePosition, final LatLng destPosition, String mode) {
        @SuppressLint("HandlerLeak") final Handler handler = new Handler() {
            public void handleMessage(Message msg) {
                try {
                    if (mMapPolyline != null) mMapPolyline.remove();
                    if (mStartCircle != null) mStartCircle.remove();
                    if (mEndCircle != null) mEndCircle.remove();
                    Document doc = (Document) msg.obj;
                    GMapV2Direction md = new GMapV2Direction();
                    ArrayList<LatLng> directionPoint = md.getDirection(doc);
                    drawPath(directionPoint, sourcePosition, destPosition);
                    md.getDurationText(doc);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        };

        new GMapV2DirectionAsyncTask(handler, sourcePosition, destPosition, mode).execute();
    }

    /**
     * Draws the path and the starting and ending point of the path
     * @param directionPoint List of points for the path
     * @param sourcePosition Starting point
     * @param destPosition End Point
     */
    private void drawPath(ArrayList<LatLng> directionPoint, LatLng sourcePosition, LatLng destPosition) {
        PolylineOptions rectLine = new PolylineOptions().width(15)
                .color(ContextCompat.getColor(getActivity(), R.color.colorPrimarySoft))
                .jointType(JointType.ROUND)
                .zIndex(-100)
                .startCap(new RoundCap())
                .endCap(new RoundCap());

        for (int i = 0; i < directionPoint.size(); i++) {
            rectLine.add(directionPoint.get(i));
        }

        mStartCircle = mMap.addCircle(new CircleOptions()
                .center(sourcePosition)
                .radius(20)
                .strokeWidth(5)
                .strokeColor(ContextCompat.getColor(getActivity(), R.color.colorPrimarySoft))
                .fillColor(ContextCompat.getColor(getActivity(), R.color.colorWhite)));


        mEndCircle = mMap.addCircle(new CircleOptions()
                .center(destPosition)
                .radius(20)
                .strokeWidth(5)
                .strokeColor(ContextCompat.getColor(getActivity(), R.color.colorPrimarySoft))
                .fillColor(ContextCompat.getColor(getActivity(), R.color.colorWhite)));


        mMapPolyline = mMap.addPolyline(rectLine);
    }

    /**
     * Adds marker to specific latitude and longitude
     *
     * @param latLng latitude and longitude to be set
     */
    private void putMarkerOnLocation(@NonNull LatLng latLng) {
        mLastKnownMarkerLocation = MapUtils.convertLatLngToLocation(latLng);
        mMarkerDestination.setPosition(latLng);
    }

    /**
     * Add marker on pixel point
     *
     * @param point pixel point
     */
    public void putMarkerOnPoint(Point point) {
        LatLng latLng = MapUtils.convertPixelsToLatLng(mMap, point);
        putMarkerOnLocation(latLng);
    }

    /**
     * Sets address inside the maps fragment for the user to see
     *
     * @param address String with the written address
     */
    public void setAddressInView(String address) {
        if (mTextViewCurrentFocus == null) return;
        setSearchButtonState();
        mTextViewCurrentFocus.setText(address);
    }

    private void startPreferencesActivity() {
        Intent preferencesIntent = new Intent(getActivity(), PreferencesActivity.class);
        startActivityForResult(preferencesIntent, PREFERENCES_REQUEST_CODE);
        getActivity().overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left);
    }
}
