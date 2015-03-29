package com.android.socialnetworks;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Point;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.BounceInterpolator;
import android.view.animation.Interpolator;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.gc.materialdesign.views.Switch;
import com.appyvet.rangebar.RangeBar;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MainFragment extends Fragment implements OnMapReadyCallback, LocationProvider.LocationCallback {

    private ArrayList<MyMarker> markersArray;
    private HashMap<Marker, MyMarker> markersHashMap;

    private LatLng currentCameraPosition;
    private boolean isLocationShowed;

    final int RQS_GooglePlayServices = 1;
    //private CameraPosition currentCam;
    private SupportMapFragment map;
    private static final String ARG_POSITION = "position";
    private boolean isLongPressed = false;
    private boolean isInfoWindowPressedToEdit = false;
    private boolean isInfoWindowPressedToShow = false;
    private boolean isEventsListPressed = false;
    private boolean isVisible = false;
    private int stackSize = 0;

    private LocationProvider mLocationProvider;

    public static MainFragment newInstance(int position) {
        MainFragment f = new MainFragment();
        Bundle b = new Bundle();
        b.putInt(ARG_POSITION, position);
        f.setArguments(b);
        return f;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.map_fragment, container, false);
        setHasOptionsMenu(true);

        isLocationShowed = false;
        markersHashMap = new HashMap<>();
        map = (SupportMapFragment) this.getChildFragmentManager().findFragmentById(R.id.map);

        mLocationProvider = new LocationProvider(getActivity(), this);
        if (checkGooglePlayServices())
            map.getMapAsync(this);

        Button loadMarkers = (Button) rootView.findViewById(R.id.load_markers_button);
        loadMarkers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateMarkers();
            }
        });

        getFragmentManager().addOnBackStackChangedListener(new FragmentManager.OnBackStackChangedListener() {
            @Override
            public void onBackStackChanged() {
                if ((isEventsListPressed || isLongPressed || isInfoWindowPressedToEdit || MainActivity.EVENT_CATEGORY_EDITED
                        || !MainActivity.EVENT_CATEGORY_EDITED || isInfoWindowPressedToShow) && isVisible) {
                    int current = getFragmentManager().getBackStackEntryCount();
                    if (current == 1) stackSize = 1;
                    if (current == 0 && stackSize == 1) {
                        ActionBar actionBar = ((ActionBarActivity) getActivity()).getSupportActionBar();
                        if (actionBar != null) {
                            actionBar.setHomeButtonEnabled(false);
                            actionBar.setDisplayHomeAsUpEnabled(false);
                            actionBar.setTitle(R.string.app_name);
                            actionBar.setBackgroundDrawable(new ColorDrawable(0xff009A90));
                        }
                        if (isLongPressed) addNewMarker();
                        else if(MainActivity.EVENT_FRAGMENT_RESULT.equals("done") && MainActivity.EVENT_CATEGORY_EDITED)
                                updateMarkers();
                        MyViewPager pager = (MyViewPager) getActivity().findViewById(R.id.pager);
                        pager.setPagingEnabled(true);
                        map.getMap().getUiSettings().setAllGesturesEnabled(true);
                        isLongPressed = false;
                        isInfoWindowPressedToEdit = false;
                        isInfoWindowPressedToShow = false;
                        isEventsListPressed = false;
                        stackSize = 0;
                    }
                }
            }
        });
        return rootView;
    }

    private boolean isMarkerOnArray(List<MyMarker> array, LatLng m) {
            for(MyMarker marker : array) {
                if (marker.getLocation().latitude == m.latitude && marker.getLocation().longitude == m.longitude)
                    return true;
            }
        return false;
    }

    private int getMarkerIcon(String categoryIcon){
        switch(categoryIcon){
            case "0": return R.drawable.marker_love;
            case "1": return R.drawable.marker_movie;
            case "2": return R.drawable.marker_sport;
            case "3": return R.drawable.marker_business;
            case "4": return R.drawable.marker_coffee;
            case "5": return R.drawable.marker_meet;
            default: return R.drawable.marker_meet;
        }
    }

    private ArrayList<MyMarker> getEventMarkers(){
        final ArrayList<MyMarker> eventMarkers = new ArrayList<>();
        float [] distance = new float[1];
        Location.distanceBetween(
                map.getMap().getProjection().getVisibleRegion().farLeft.latitude,
                map.getMap().getProjection().getVisibleRegion().farLeft.longitude,
                map.getMap().getProjection().getVisibleRegion().farRight.latitude,
                map.getMap().getProjection().getVisibleRegion().farRight.longitude,
                distance);
        ParseQuery<ParseObject> query = ParseQuery.getQuery("GoEvents");
        query.whereExists("category").whereExists("location").whereWithinKilometers("location", new ParseGeoPoint(
                        map.getMap().getCameraPosition().target.latitude, map.getMap().getCameraPosition().target.longitude),
                distance[0] / 1000).setLimit(25);
        try {
            List<ParseObject> queryResult = query.find();
            for(ParseObject po : queryResult) {
                LatLng markerLocation = new LatLng(po.getParseGeoPoint("location").getLatitude(),
                        po.getParseGeoPoint("location").getLongitude());
                eventMarkers.add(new MyMarker(po.getObjectId(), po.getString("category"), markerLocation));
            }
        }
        catch(ParseException e) {
            Log.d("getEventString", "Error: " + e.getMessage());
        }
        return eventMarkers;
    }

    private void addNewMarker() {
        if (MainActivity.EVENT_FRAGMENT_RESULT.equals("done"))
            if (!isMarkerOnArray(markersArray, currentCameraPosition)) {
                if (MainActivity.newMarker != null) {
                    Marker currentMarker = map.getMap().addMarker(new MarkerOptions()
                            .position(new LatLng(MainActivity.newMarker.getLocation().latitude,
                                    MainActivity.newMarker.getLocation().longitude))
                            .icon(BitmapDescriptorFactory.fromResource(getMarkerIcon(MainActivity.newMarker.getCategory()))));
                    markersArray.add(MainActivity.newMarker);
                    markersHashMap.put(currentMarker, MainActivity.newMarker);
                }
            }
    }

    private void plotMarkers(ArrayList<MyMarker> markers) {
        if(markers.size() > 0) {
            for (MyMarker myMarker : markers) {
                MarkerOptions markerOption = new MarkerOptions().position(new LatLng(myMarker.getLocation().latitude,
                        myMarker.getLocation().longitude));
                markerOption.icon(BitmapDescriptorFactory.fromResource(getMarkerIcon(myMarker.getCategory())));
                Marker currentMarker =  map.getMap().addMarker(markerOption);
                markersHashMap.put(currentMarker, myMarker);
                animateMarker(currentMarker);
            }
        }
    }

    private void updateMarkers() {
        if(!SignUpActivity.isNetworkOn(getActivity())) {
            Toast.makeText(getActivity(), "Please, check your internet connection", Toast.LENGTH_SHORT).show();
        } else {
            MainActivity.EVENT_CATEGORY_EDITED = false;
            map.getMap().clear();
            if (markersArray != null) markersArray.clear();
            if (markersHashMap != null) markersHashMap.clear();
            markersArray = new ArrayList<>(getEventMarkers());
            plotMarkers(markersArray);
        }
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        isVisible = isVisibleToUser;
        if (isVisibleToUser) {
            if(MainActivity.EVENT_FRAGMENT_RESULT.equals("done") && MainActivity.EVENT_CATEGORY_EDITED)
                updateMarkers();
        }
    }

    private void animateMarker(final Marker marker){

        //Make the marker bounce
        final Handler handler = new Handler();

        final long startTime = SystemClock.uptimeMillis();
        final long duration = 2000;

        Projection proj = map.getMap().getProjection();
        final LatLng markerLatLng = marker.getPosition();
        Point startPoint = proj.toScreenLocation(markerLatLng);
        startPoint.offset(0, -100);
        final LatLng startLatLng = proj.fromScreenLocation(startPoint);

        final Interpolator interpolator = new BounceInterpolator();

        handler.post(new Runnable() {
            @Override
            public void run() {
                long elapsed = SystemClock.uptimeMillis() - startTime;
                float t = interpolator.getInterpolation((float) elapsed / duration);
                double lng = t * markerLatLng.longitude + (1 - t) * startLatLng.longitude;
                double lat = t * markerLatLng.latitude + (1 - t) * startLatLng.latitude;
                marker.setPosition(new LatLng(lat, lng));

                if (t < 1.0) {
                    // Post again 16ms later.
                    handler.postDelayed(this, 16);
                }
            }
        });
    }

    @Override
    public void onMapReady(final GoogleMap map) {
        checkLocationServices();
        map.getUiSettings().setCompassEnabled(true);
        map.getUiSettings().setTiltGesturesEnabled(true);
        map.getUiSettings().setRotateGesturesEnabled(true);
        map.getUiSettings().setScrollGesturesEnabled(true);
        map.getUiSettings().setZoomControlsEnabled(true);
        map.getUiSettings().setZoomGesturesEnabled(true);
        map.getUiSettings().setMapToolbarEnabled(false);
        map.getUiSettings().setMyLocationButtonEnabled(true);
        map.setMyLocationEnabled(true);
        map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        map.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
            @Override
            public View getInfoWindow(Marker currentMarker) {
                View v = LayoutInflater.from(getActivity()).inflate(R.layout.map_info_window, null);

                MyMarker myMarker = markersHashMap.get(currentMarker);
                TextView mapTitle = (TextView) v.findViewById(R.id.map_title);
                TextView mapCreatorGender = (TextView) v.findViewById(R.id.map_creator_gender);

                LinearLayout currentLayout = (LinearLayout) v.findViewById(R.id.map_info);

                ParseQuery<ParseObject> query = ParseQuery.getQuery("GoEvents");
                try {
                    ParseObject parseObject = query.get(myMarker.getObjectId());
                    myMarker.setTitle(parseObject.getString("title"));
                    myMarker.setCreatorName(parseObject.getString("creatorName"));
                    myMarker.setCreatorGender(parseObject.getString("creatorGender"));
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                if (myMarker.getCreatorName().equals(ParseUser.getCurrentUser().getUsername()))
                    currentLayout.setBackgroundResource(R.drawable.background_normal_name);
                else currentLayout.setBackgroundResource(R.drawable.background_signup);
                mapTitle.setText(myMarker.getTitle());
                mapCreatorGender.setText(myMarker.getCreatorGender());

                return v;
            }

            @Override
            public View getInfoContents(Marker currentMarker) {
                return null;
            }
        });
        map.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                if (!SignUpActivity.isNetworkOn(getActivity())) {
                    Toast.makeText(getActivity(), "Please, check your internet connection", Toast.LENGTH_SHORT).show();
                } else {
                    MyMarker myMarker = markersHashMap.get(marker);
                    String currentUser = ParseUser.getCurrentUser().getUsername();
                    if (markersHashMap.get(marker).getCreatorName().equals(currentUser)) {
                        if (!isInfoWindowPressedToEdit) {
                            map.getUiSettings().setAllGesturesEnabled(false);
                            MyViewPager pager = (MyViewPager) getActivity().findViewById(R.id.pager);
                            pager.setPagingEnabled(false);

                            NewEventFragment newEventFragment = new NewEventFragment();
                            Bundle bundle = new Bundle();
                            bundle.putString("USED_FOR", "edit");
                            bundle.putString("MARKER_ID", myMarker.getObjectId());
                            newEventFragment.setArguments(bundle);

                            getFragmentManager().beginTransaction()
                                    .setCustomAnimations(
                                            R.anim.slide_in_bottom, R.anim.slide_out_bottom,
                                            R.anim.slide_in_bottom, R.anim.slide_out_bottom)
                                    .add(R.id.container, newEventFragment)
                                    .addToBackStack(null)
                                    .commit();
                            marker.hideInfoWindow();
                            isInfoWindowPressedToEdit = true;
                        }
                    }
                    else if (!isInfoWindowPressedToShow) {
                        map.getUiSettings().setAllGesturesEnabled(false);
                        MyViewPager pager = (MyViewPager) getActivity().findViewById(R.id.pager);
                        pager.setPagingEnabled(false);

                        DetailedEventFragment detailedEventFragment = new DetailedEventFragment();
                        Bundle bundle = new Bundle();
                        bundle.putString("MARKER_ID", myMarker.getObjectId());
                        detailedEventFragment.setArguments(bundle);

                        getFragmentManager().beginTransaction()
                                .setCustomAnimations(
                                        R.anim.slide_in_bottom, R.anim.slide_out_bottom,
                                        R.anim.slide_in_bottom, R.anim.slide_out_bottom)
                                .add(R.id.container, detailedEventFragment)
                                .addToBackStack(null)
                                .commit();
                        marker.hideInfoWindow();
                        isInfoWindowPressedToShow = true;
                    }
                }
            }
        });
        /*map.getMyLocation();
        map.setOnMyLocationChangeListener(new GoogleMap.OnMyLocationChangeListener() {
            @Override
            public void onMyLocationChange(Location location) {
                if (!isLocationShowed) {
                    LatLng latLng;
                    CameraUpdate cameraUpdate;

                    latLng = new LatLng(location.getLatitude(), location.getLongitude());
                    cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 16);
                    map.animateCamera(cameraUpdate, new GoogleMap.CancelableCallback() {
                        @Override
                        public void onFinish() {
                            updateMarkers();
                        }
                        @Override
                        public void onCancel() {
                            updateMarkers();
                        }
                    });/*
                    if (currentCam == null) {
                        latLng = new LatLng(location.getLatitude(), location.getLongitude());
                        cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 16);
                        map.animateCamera(cameraUpdate, new GoogleMap.CancelableCallback() {
                            @Override
                            public void onFinish() {
                                updateMarkers();
                            }
                            @Override
                            public void onCancel() {
                                updateMarkers();
                            }
                        });
                    } else {
                        SharedPreferences settings = getActivity().getSharedPreferences("MAP_STATE", 0);
                        latLng = new LatLng(settings.getFloat("latitude", 0), settings.getFloat("longitude", 0));
                        cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 16);
                        map.moveCamera(cameraUpdate);

                    }
                    isLocationShowed = true;
                } //else currentCam = map.getCameraPosition();
            }
        });*/
        map.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {
                if (!isLongPressed) {
                    map.getUiSettings().setAllGesturesEnabled(false);
                    MyViewPager pager = (MyViewPager) getActivity().findViewById(R.id.pager);
                    pager.setPagingEnabled(false);

                    currentCameraPosition = latLng;
                    NewEventFragment newEventFragment = new NewEventFragment();
                    Bundle bundle = new Bundle();
                    bundle.putDouble("LOCATION_LAT", latLng.latitude);
                    bundle.putDouble("LOCATION_LONG", latLng.longitude);
                    newEventFragment.setArguments(bundle);

                    getFragmentManager().beginTransaction()
                            .setCustomAnimations(
                                    R.anim.slide_in_bottom, R.anim.slide_out_bottom,
                                    R.anim.slide_in_bottom, R.anim.slide_out_bottom)
                            .add(R.id.container, newEventFragment)
                            .addToBackStack(null)
                            .commit();
                    isLongPressed = true;
                }
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        mLocationProvider.connect();
    }

    @Override
    public void onPause() {
        super.onPause();
        mLocationProvider.disconnect();
    }

    @Override
    public void handleNewLocation(Location location) {
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 16);
        map.getMap().animateCamera(cameraUpdate, new GoogleMap.CancelableCallback() {
            @Override
            public void onFinish() {
                updateMarkers();
            }
            @Override
            public void onCancel() {
                updateMarkers();
            }
        });
    }

    private boolean checkGooglePlayServices(){
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(getActivity());
        if (resultCode != ConnectionResult.SUCCESS)
            GooglePlayServicesUtil.getErrorDialog(resultCode, getActivity(), RQS_GooglePlayServices);
        return resultCode == ConnectionResult.SUCCESS;
    }

    private void checkLocationServices() {
        boolean gps_enabled, network_enabled;
        LocationManager lm = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        network_enabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        if(!gps_enabled && !network_enabled){
            AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
            dialog.setMessage("GPS and network location services are disabled!");
            dialog.setPositiveButton("Enable", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                    Intent myIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    getActivity().startActivity(myIntent);
                }
            });
            dialog.setNeutralButton(getString(R.string.cancel_button), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            dialog.show();
            }
        }

    private void destroyMap(){
        if (!getActivity().isFinishing()) {
            SupportMapFragment destroyMap = (SupportMapFragment) this.getChildFragmentManager().findFragmentById(R.id.map);
            if (destroyMap != null) {
                /*if (currentCam != null) {
                    SharedPreferences settings = getActivity().getSharedPreferences("MAP_STATE", 0);
                    SharedPreferences.Editor editor = settings.edit();
                    editor.putFloat("latitude", (float) currentCam.target.latitude);
                    editor.putFloat("longitude", (float) currentCam.target.longitude);
                    editor.apply();
                }*/
                getFragmentManager().beginTransaction().remove(destroyMap).commit();
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        destroyMap();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.clear();
        inflater.inflate(R.menu.menu_main, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_list:
                isEventsListPressed = true;
                map.getMap().getUiSettings().setAllGesturesEnabled(false);
                MyViewPager pager = (MyViewPager) getActivity().findViewById(R.id.pager);
                pager.setPagingEnabled(false);

                float [] distance = new float[1];
                Location.distanceBetween(
                        map.getMap().getProjection().getVisibleRegion().farLeft.latitude,
                        map.getMap().getProjection().getVisibleRegion().farLeft.longitude,
                        map.getMap().getProjection().getVisibleRegion().farRight.latitude,
                        map.getMap().getProjection().getVisibleRegion().farRight.longitude,
                        distance);

                EventsListFragment eventsListFragment = new EventsListFragment();
                Bundle bundle = new Bundle();
                bundle.putFloatArray("CURRENT_AREA", distance);
                bundle.putDouble("LOCATION_LAT", map.getMap().getCameraPosition().target.latitude);
                bundle.putDouble("LOCATION_LONG", map.getMap().getCameraPosition().target.longitude);
                eventsListFragment.setArguments(bundle);

                getFragmentManager().beginTransaction()
                        .setCustomAnimations(
                                R.anim.slide_in_bottom, R.anim.slide_out_bottom,
                                R.anim.slide_in_bottom, R.anim.slide_out_bottom)
                        .add(R.id.container, eventsListFragment)
                        .addToBackStack(null)
                        .commit();
                return true;
            case R.id.action_search:
                eventsFilterDialog();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void eventsFilterDialog() {
        AlertDialog.Builder alertBw = new AlertDialog.Builder(getActivity());
        alertBw.setTitle("Events search settings");
        View v = LayoutInflater.from(getActivity()).inflate(R.layout.events_filter_dialog, null);
        alertBw.setView(v);
        alertBw.setPositiveButton(getString(R.string.ok_button), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(final DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        Switch SwitcherLove = (Switch) v.findViewById(R.id.switch_love);
        final TextView rangeMin = (TextView) v.findViewById(R.id.filter_age_min);
        final TextView rangeMax = (TextView) v.findViewById(R.id.filter_age_max);
        RangeBar rangebar = (RangeBar) v.findViewById(R.id.filter_rangebar);
        rangebar.setOnRangeBarChangeListener(new RangeBar.OnRangeBarChangeListener() {
            @Override
            public void onRangeChangeListener(RangeBar rangeBar, int leftPinIndex,
                                              int rightPinIndex,
                                              String leftPinValue, String rightPinValue) {
                rangeMin.setText(leftPinValue);
                rangeMax.setText(rightPinValue);
            }
        });

        AlertDialog alertDialog = alertBw.create();
        alertDialog.show();
    }
}
