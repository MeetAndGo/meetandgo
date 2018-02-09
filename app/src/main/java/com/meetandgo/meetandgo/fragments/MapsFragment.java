package com.meetandgo.meetandgo.fragments;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;
import com.meetandgo.meetandgo.Constants;
import com.meetandgo.meetandgo.R;
import com.meetandgo.meetandgo.receivers.AddressResultReceiver;
import com.meetandgo.meetandgo.services.FetchAddressIntentService;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MapsFragment extends Fragment implements OnMapReadyCallback, GoogleMap.OnMyLocationClickListener {

    private static final String TAG = "MapsFragment";
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
    private BottomSheetBehavior bottomSheetBehavior;

    private AddressResultReceiver mResultReceiver;

    private View mView;
    @BindView(R.id.fab) FloatingActionButton mFab;
    @BindView(R.id.bottomSheetContent) RelativeLayout mRelativeSheetContent;
    @BindView(R.id.imageViewMapCenter) ImageView mImageViewMapCenter;
    private OnCompleteListener mOnCompleteListenerMove;
    private OnCompleteListener mOnCompleteListenerAnimate;
    private int mSlideOffset;
    private boolean mUserIsDragging;

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
        mResultReceiver = new AddressResultReceiver(getActivity(),  new Handler());

    }

    private void setUpMap() {
        FragmentManager manager = getFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        SupportMapFragment mapFragment = new SupportMapFragment();
        transaction.add(R.id.map, mapFragment);
        transaction.commit();
        mapFragment.getMapAsync(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mView = inflater.inflate(R.layout.fragment_maps, container, false);
        ButterKnife.bind(this, mView);

        setupBottomSheet(mView);
        setUpOnCompleteListeners();
        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getDeviceLocation(mOnCompleteListenerAnimate);
            }
        });

        setUpMap();
        return mView;
    }

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
                putMarkerOnLocation(latLng);
                Location location = convertLatLngToLocation(latLng);
                animateCameraToLocation(location);
                getLocationName(location);
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
                if(!mUserIsDragging) return;
                mUserIsDragging = false;
                LatLng cameraLatLng = mMap.getCameraPosition().target;
                getLocationName(convertLatLngToLocation(cameraLatLng));
            }
        });

        manageLocationPermission();
    }

    /**
     * Setup the bottom sheet used for setting the preferences for the matching
     *
     * @param view The fragment view needed for finding the view id of the bottom sheet
     */
    private void setupBottomSheet(@NonNull View view) {
        bottomSheetBehavior = BottomSheetBehavior.from(view.findViewById(R.id.bottomSheetLayout));
        bottomSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(View bottomSheet, int newState) {
                switch (newState) {
                    case BottomSheetBehavior.STATE_COLLAPSED:
                        Log.d(TAG, "Bottom Sheet Behaviour: STATE_COLLAPSED");
                        break;
                    case BottomSheetBehavior.STATE_DRAGGING:
                        Log.d(TAG, "Bottom Sheet Behaviour: STATE_DRAGGING");
                        break;
                    case BottomSheetBehavior.STATE_EXPANDED:
                        Log.d(TAG, "Bottom Sheet Behaviour: STATE_EXPANDED");
                        break;
                    case BottomSheetBehavior.STATE_HIDDEN:
                        Log.d(TAG, "Bottom Sheet Behaviour: STATE_HIDDEN");
                        break;
                    case BottomSheetBehavior.STATE_SETTLING:
                        Log.d(TAG, "Bottom Sheet Behaviour: STATE_SETTLING");
                        break;
                }
            }

            @Override
            public void onSlide(View bottomSheet, float slideOffset) {
                centerMapCenterImageView(bottomSheet, slideOffset);
            }
        });
        mRelativeSheetContent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_COLLAPSED) {
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                }
            }
        });
    }

    private void centerMapCenterImageView(View bottomSheet, float slideOffset) {
        int slideChangeHeight = bottomSheet.getHeight() - bottomSheetBehavior.getPeekHeight();
        mSlideOffset = (int) (slideChangeHeight * (slideOffset/2));
        slideChangeHeight *= 1 - (slideOffset / 2);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(mImageViewMapCenter.getWidth(), mImageViewMapCenter.getHeight());
        params.leftMargin = (int) mImageViewMapCenter.getX();
        params.topMargin = slideChangeHeight;
        mImageViewMapCenter.setLayoutParams(params);
    }

    public Point convertLatLngToPixels(LatLng latLng) {
        Projection projection = mMap.getProjection();
        Point p1 = projection.toScreenLocation(latLng);
        return p1;
    }

    public LatLng convertPixelsToLatLng(Point point) {
        Projection projection = mMap.getProjection();
        return projection.fromScreenLocation(point);
    }

    private void putMarkerOnLocation(@NonNull LatLng latLng) {
        mLastKnownMarkerLocation = convertLatLngToLocation(latLng);
        mMarkerDestination.setPosition(latLng);
    }

    private void putMarkerOnPoint(Point point) {
        LatLng latLng = convertPixelsToLatLng(point);
        putMarkerOnLocation(latLng);
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

    // TODO: Follow the tutorial to make it a service on another thread
//    private String getLocationName(@NonNull Location location) {
//        Geocoder geocoder = new Geocoder(getActivity(), Locale.getDefault());
//        List<Address> addresses;
//        try {
//            addresses = geocoder.getFromLocation(location.getLatitude(),
//                    location.getLongitude(),
//                    1);
//
//            // Get default Address in case map is not working for some reason
//            Locale userLocale = getResources().getConfiguration().locale;
//            Address address = new Address(userLocale);
//            if (addresses.size() > 0) address = addresses.get(0);
//
//            // Format The address returned
//            ArrayList<String> addressFragments = new ArrayList<>();
//            // Fetch the address lines using getAddressLine,
//            // join them, and send them to the thread.
//            for (int i = 0; i <= address.getMaxAddressLineIndex(); i++) {
//                addressFragments.add(address.getAddressLine(i));
//            }
//            return TextUtils.join(System.getProperty("line.separator"), addressFragments);
//        } catch (Exception e) {
//            return location.toString();
//        }
//    }

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

    private Location convertLatLngToLocation(LatLng latLng) {
        Location newLocation = new Location("");
        newLocation.setLatitude(latLng.latitude);
        newLocation.setLongitude(latLng.longitude);
        return newLocation;
    }

    private void animateCameraToLocation(Location location) {
        LatLng newLocation = new LatLng(location.getLatitude(), location.getLongitude());
        newLocation = getLatLngWithSlideOffset(newLocation);
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(newLocation, DEFAULT_ZOOM));
    }

    private void moveCameraToLocation(Location location) {
        LatLng newLocation = new LatLng(location.getLatitude(), location.getLongitude());
        newLocation = getLatLngWithSlideOffset(newLocation);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(newLocation, DEFAULT_ZOOM));
    }

    private LatLng getLatLngWithSlideOffset(LatLng newLocation) {
        Point point = convertLatLngToPixels(newLocation);
        point.y += mSlideOffset;
        newLocation = convertPixelsToLatLng(point);
        return newLocation;
    }


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

    @Override
    public void onMyLocationClick(@NonNull Location location) {
        getLocationName(location);
    }

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



}
