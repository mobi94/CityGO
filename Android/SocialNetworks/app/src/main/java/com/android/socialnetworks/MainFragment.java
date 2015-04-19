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
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.appyvet.rangebar.RangeBar;
import com.astuetz.PagerSlidingTabStrip;
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
import com.kyleduo.switchbutton.SwitchButton;
import com.parse.FunctionCallback;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import at.markushi.ui.CircleButton;

public class MainFragment extends Fragment implements OnMapReadyCallback, LocationProvider.LocationCallback {

    static public ArrayList<MyMarker> markersArray;
    static public HashMap<Marker, MyMarker> markersHashMap;

    private LatLng currentCameraPosition;
    private boolean isLocationShowed;

    final int RQS_GooglePlayServices = 1;
    //private CameraPosition currentCam;
    private MyProgressDialog progressDialog;
    private SupportMapFragment map;
    private static final String ARG_POSITION = "position";
    private boolean isLongPressed = false;
    private boolean isInfoWindowPressedToEdit = false;
    private boolean isInfoWindowPressedToShow = false;
    private boolean isEventsListPressed = false;
    private boolean isVisible = false;
    private int stackSize = 0;

    static public int[] filterAge = {1,35};
    static public boolean[] filterCategories = {true,true,true,true,true,true};
    static public boolean[] filterSex = {true,true};
    static public boolean isFilterDialogUsed = false;

    private LocationProvider mLocationProvider;
    private CircleButton currentLocationButton;

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

        progressDialog = new MyProgressDialog(getActivity());

        currentLocationButton = (CircleButton) rootView.findViewById(R.id.buttonFloat);

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
                if ((isEventsListPressed || isLongPressed || isInfoWindowPressedToEdit || MainActivity.MAP_MARKERS_UPDATE
                        || !MainActivity.MAP_MARKERS_UPDATE || isInfoWindowPressedToShow) && isVisible) {
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
                        else if(MainActivity.EVENT_FRAGMENT_RESULT.equals("done") && MainActivity.MAP_MARKERS_UPDATE)
                            updateMarkers();
                        MainActivity.enableViewPager((MyViewPager) getActivity().findViewById(R.id.pager),
                                (PagerSlidingTabStrip) getActivity().findViewById(R.id.tabs));
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
        query.whereWithinKilometers("location", new ParseGeoPoint(map.getMap().getCameraPosition().target.latitude,
                        map.getMap().getCameraPosition().target.longitude), distance[0] / 1000).setLimit(25);
        try {
            List<ParseObject> queryResult = query.find();
            for(ParseObject po : queryResult) {
                LatLng markerLocation = new LatLng(po.getParseGeoPoint("location").getLatitude(),
                        po.getParseGeoPoint("location").getLongitude());
                eventMarkers.add(new MyMarker(po.getObjectId(), po.getString("category"), markerLocation,
                        po.getString("title"), po.getString("creatorName"), po.getString("creatorGender"),
                        po.getString("avaibleSeats"), po.getString("temporary"), po.getDate("startDate")));
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

    private void updateMarkersWithoutFilters() {
        if(!SignUpActivity.isNetworkOn(getActivity())) {
            Toast.makeText(getActivity(), "Please, check your internet connection", Toast.LENGTH_SHORT).show();
        } else {
            progressDialog.showProgress("Events data updating...");
            MainActivity.MAP_MARKERS_UPDATE = false;
            map.getMap().clear();
            if (markersArray != null) markersArray.clear();
            if (markersHashMap != null) markersHashMap.clear();
            markersArray = new ArrayList<>(getEventMarkers());
            plotMarkers(markersArray);
            progressDialog.hideProgress();
        }
    }

    private void updateMarkers() {
        if(!SignUpActivity.isNetworkOn(getActivity())) {
            Toast.makeText(getActivity(), "Please, check your internet connection", Toast.LENGTH_SHORT).show();
        } else {
            progressDialog.showProgress("Loading...");
            float [] distance = new float[1];
            Location.distanceBetween(
                    map.getMap().getProjection().getVisibleRegion().farLeft.latitude,
                    map.getMap().getProjection().getVisibleRegion().farLeft.longitude,
                    map.getMap().getProjection().getVisibleRegion().farRight.latitude,
                    map.getMap().getProjection().getVisibleRegion().farRight.longitude,
                    distance);
            HashMap<String, Object> params = new HashMap<>();
            ArrayList<String> categories = new ArrayList<>();
            String[] sex = new String[2];
            int minAge = 1, maxAge = 70;
            if (isFilterDialogUsed) {
                for (int i = 0; i < filterCategories.length; i++) {
                    if (filterCategories[i])
                        categories.add(Integer.toString(i));
                }
                if (filterSex[0]) sex[0] = "male";
                if (filterSex[1]) sex[1] = "female";
                minAge = filterAge[0];
                maxAge = filterAge[1];
            }
            else{
                for (int i = 0; i < 6; i++)
                    categories.add(Integer.toString(i));
                sex[0] = "male";
                sex[1] = "female";
            }
            params.put("category", categories);
            params.put("gender", Arrays.asList(sex));
            params.put("minAge", Integer.toString(minAge));
            params.put("maxAge", Integer.toString(maxAge));
            params.put("location", new ParseGeoPoint(map.getMap().getCameraPosition().target.latitude,
                    map.getMap().getCameraPosition().target.longitude));
            params.put("radius", distance[0] / 1000);
            ParseCloud.callFunctionInBackground("getFilteredEvents", params, new FunctionCallback<List<ParseObject>>() {
                @Override
                public void done(List<ParseObject> parseObjects, ParseException e) {
                    if (e != null) {
                        Toast.makeText(getActivity(), "UPDATE_EVENT_DATA_ERROR" + e, Toast.LENGTH_SHORT).show();
                    } else {
                        MainActivity.MAP_MARKERS_UPDATE = false;
                        map.getMap().clear();
                        if (markersArray != null) markersArray.clear();
                        else markersArray = new ArrayList<>();
                        if (markersHashMap != null) markersHashMap.clear();
                        else markersHashMap = new HashMap<>();
                        for (ParseObject po : parseObjects) {
                            LatLng markerLocation = new LatLng(po.getParseGeoPoint("location").getLatitude(),
                                    po.getParseGeoPoint("location").getLongitude());
                            markersArray.add(new MyMarker(po.getObjectId(), po.getString("category"), markerLocation,
                                    po.getString("title"), po.getString("creatorName"), po.getString("creatorGender"),
                                    po.getString("avaibleSeats"), po.getString("temporary"), po.getDate("startDate")));
                        }
                        plotMarkers(markersArray);
                    }
                    progressDialog.hideProgress();
                }
            });
        }
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        isVisible = isVisibleToUser;
        if (isVisibleToUser) {
            if(MainActivity.EVENT_FRAGMENT_RESULT.equals("done") && MainActivity.MAP_MARKERS_UPDATE)
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
        map.getUiSettings().setZoomControlsEnabled(false);
        map.getUiSettings().setZoomGesturesEnabled(true);
        map.getUiSettings().setMapToolbarEnabled(false);
        map.getUiSettings().setMyLocationButtonEnabled(false);
        map.setMyLocationEnabled(true);
        map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        currentLocationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Location location = map.getMyLocation();
                if(location != null) {
                    LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                    CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 16);
                    map.animateCamera(cameraUpdate);
                }
                else Toast.makeText(getActivity(), "Your current location is undefined", Toast.LENGTH_SHORT).show();
            }
        });
        map.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
            @Override
            public View getInfoWindow(Marker currentMarker) {
                View v = LayoutInflater.from(getActivity()).inflate(R.layout.map_info_window, null);

                MyMarker myMarker = markersHashMap.get(currentMarker);
                TextView mapTitle = (TextView) v.findViewById(R.id.map_title);
                TextView mapCreatorGender = (TextView) v.findViewById(R.id.map_creator_gender);

                LinearLayout currentLayout = (LinearLayout) v.findViewById(R.id.map_info);

                if (myMarker.getCreatorName().equals(ParseUser.getCurrentUser().getUsername()))
                    currentLayout.setBackgroundResource(R.drawable.background_info_window_creator);
                else currentLayout.setBackgroundResource(R.drawable.background_info_window_user);
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
                            updateMarkersWithoutFilters();
                        }
                        @Override
                        public void onCancel() {
                            updateMarkersWithoutFilters();
                        }
                    });/*
                    if (currentCam == null) {
                        latLng = new LatLng(location.getLatitude(), location.getLongitude());
                        cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 16);
                        map.animateCamera(cameraUpdate, new GoogleMap.CancelableCallback() {
                            @Override
                            public void onFinish() {
                                updateMarkersWithoutFilters();
                            }
                            @Override
                            public void onCancel() {
                                updateMarkersWithoutFilters();
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
        animateToCurrentLocation(location);
    }

    private void animateToCurrentLocation(Location location){
        if (markersArray == null) {
            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 16);
            map.getMap().animateCamera(cameraUpdate, new GoogleMap.CancelableCallback() {
                @Override
                public void onFinish() {
                    updateMarkers();
                    markersArray = new ArrayList<>();
                }

                @Override
                public void onCancel() {
                }
            });
        }
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
        markersArray = null;
        markersHashMap = null;
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
                if(map.getMap().getMyLocation() != null) {
                    isEventsListPressed = true;
                    map.getMap().getUiSettings().setAllGesturesEnabled(false);

                    EventsListFragment eventsListFragment = new EventsListFragment();
                    Bundle bundle = new Bundle();
                    bundle.putDouble("LEFT_VISIBLE_REGION_LAT", map.getMap().getProjection().getVisibleRegion().farLeft.latitude);
                    bundle.putDouble("LEFT_VISIBLE_REGION_LONG", map.getMap().getProjection().getVisibleRegion().farLeft.longitude);
                    bundle.putDouble("RIGHT_VISIBLE_REGION_LAT", map.getMap().getProjection().getVisibleRegion().farRight.latitude);
                    bundle.putDouble("RIGHT_VISIBLE_REGION_LONG", map.getMap().getProjection().getVisibleRegion().farRight.longitude);
                    bundle.putDouble("LOCATION_LAT", map.getMap().getMyLocation().getLatitude());
                    bundle.putDouble("LOCATION_LONG", map.getMap().getMyLocation().getLongitude());
                    eventsListFragment.setArguments(bundle);

                    getFragmentManager().beginTransaction()
                            .setCustomAnimations(
                                    R.anim.slide_in_bottom, R.anim.slide_out_bottom,
                                    R.anim.slide_in_bottom, R.anim.slide_out_bottom)
                            .add(R.id.container, eventsListFragment)
                            .addToBackStack(null)
                            .commit();
                }
                else Toast.makeText(getActivity(), "Your current location is undefined", Toast.LENGTH_SHORT).show();
                return true;
            case R.id.ic_action_filter:
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

        SwitchButton switcherLove = (SwitchButton) v.findViewById(R.id.switch_love);
        SwitchButton switcherMovie = (SwitchButton) v.findViewById(R.id.switch_movie);
        SwitchButton switcherSport = (SwitchButton) v.findViewById(R.id.switch_sport);
        SwitchButton switcherBusiness = (SwitchButton) v.findViewById(R.id.switch_business);
        SwitchButton switcherCoffee = (SwitchButton) v.findViewById(R.id.switch_coffee);
        SwitchButton switcherMeet = (SwitchButton) v.findViewById(R.id.switch_meet);
        SwitchButton switcherMale = (SwitchButton) v.findViewById(R.id.switch_male);
        SwitchButton switcherFemale = (SwitchButton) v.findViewById(R.id.switch_female);
        final TextView rangeMin = (TextView) v.findViewById(R.id.filter_age_min);
        final TextView rangeMax = (TextView) v.findViewById(R.id.filter_age_max);
        final RangeBar rangebar = (RangeBar) v.findViewById(R.id.filter_rangebar);

        alertBw.setPositiveButton("Apply", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(final DialogInterface dialog, int which) {
                isFilterDialogUsed = true;
                updateMarkers();
            }
        });
        alertBw.setNeutralButton("Default", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                boolean[] categories = filterCategories.clone();
                boolean[] sex = filterSex.clone();
                int[] age = filterAge.clone();
                for (int i=0; i<filterCategories.length; i++)
                    filterCategories[i] = true;
                for (int i=0; i<filterSex.length; i++)
                    filterSex[i] = true;
                filterAge[0]=1;
                filterAge[1]=35;

                if (!Arrays.equals(categories, filterCategories)
                        || !Arrays.equals(sex, filterSex)
                        || !Arrays.equals(age, filterAge)) {
                    isFilterDialogUsed = false;
                    updateMarkers();
                }
            }
        });

        switcherLove.setChecked(filterCategories[MainActivity.EventCategories.LOVE.ordinal()]);
        switcherMovie.setChecked(filterCategories[MainActivity.EventCategories.MOVIE.ordinal()]);
        switcherSport.setChecked(filterCategories[MainActivity.EventCategories.SPORT.ordinal()]);
        switcherBusiness.setChecked(filterCategories[MainActivity.EventCategories.BUSINESS.ordinal()]);
        switcherCoffee.setChecked(filterCategories[MainActivity.EventCategories.COFFEE.ordinal()]);
        switcherMeet.setChecked(filterCategories[MainActivity.EventCategories.MEET.ordinal()]);

        switcherLove.setOnCheckedChangeListener(switcherCategoryListener);
        switcherMovie.setOnCheckedChangeListener(switcherCategoryListener);
        switcherSport.setOnCheckedChangeListener(switcherCategoryListener);
        switcherBusiness.setOnCheckedChangeListener(switcherCategoryListener);
        switcherCoffee.setOnCheckedChangeListener(switcherCategoryListener);
        switcherMeet.setOnCheckedChangeListener(switcherCategoryListener);

        switcherMale.setOnCheckedChangeListener(switcherSexListener);
        switcherFemale.setOnCheckedChangeListener(switcherSexListener);
        switcherMale.setChecked(filterSex[0]);
        switcherFemale.setChecked(filterSex[1]);

        rangeMin.setText(Integer.toString(filterAge[0]));
        rangeMax.setText(Integer.toString(filterAge[1]));
        rangebar.setRangePinsByValue(filterAge[0], filterAge[1]);
        rangebar.setOnRangeBarChangeListener(new RangeBar.OnRangeBarChangeListener() {
            @Override
            public void onRangeChangeListener(RangeBar rangeBar, int leftPinIndex,
                                              int rightPinIndex, String leftPinValue, String rightPinValue) {
                int left = Integer.parseInt(leftPinValue);
                int right = Integer.parseInt(rightPinValue);
                if (Integer.parseInt(leftPinValue) < 1 || Integer.parseInt(rightPinValue) > 70) {
                    if (left < 1) left = 1;
                    if (right > 70) right = 70;
                    rangebar.setRangePinsByValue(left, right);
                    rangeMin.setText(Integer.toString(left));
                    rangeMax.setText(Integer.toString(right));
                    filterAge[0]=left;
                    filterAge[1]=right;
                }
                else {
                    rangeMin.setText(leftPinValue);
                    rangeMax.setText(rightPinValue);
                    filterAge[0] = Integer.parseInt(leftPinValue);
                    filterAge[1] = Integer.parseInt(rightPinValue);
                }
            }
        });

        AlertDialog alertDialog = alertBw.create();
        alertDialog.show();
    }

    static public CompoundButton.OnCheckedChangeListener switcherCategoryListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            switch (buttonView.getId()) {
                case R.id.switch_love:
                    filterCategories[MainActivity.EventCategories.LOVE.ordinal()] = isChecked;
                    break;
                case R.id.switch_movie:
                    filterCategories[MainActivity.EventCategories.MOVIE.ordinal()] = isChecked;
                    break;
                case R.id.switch_sport:
                    filterCategories[MainActivity.EventCategories.SPORT.ordinal()] = isChecked;
                    break;
                case R.id.switch_business:
                    filterCategories[MainActivity.EventCategories.BUSINESS.ordinal()] = isChecked;
                    break;
                case R.id.switch_coffee:
                    filterCategories[MainActivity.EventCategories.COFFEE.ordinal()] = isChecked;
                    break;
                case R.id.switch_meet:
                    filterCategories[MainActivity.EventCategories.MEET.ordinal()] = isChecked;
                    break;
            }
        }
    };

    static public CompoundButton.OnCheckedChangeListener switcherSexListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            switch (buttonView.getId()) {
                case R.id.switch_male:
                    filterSex[0] = isChecked;
                    break;
                case R.id.switch_female:
                    filterSex[1] = isChecked;
                    break;
            }
        }
    };
}
