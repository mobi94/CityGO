package com.android.socialnetworks;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseRelation;
import com.parse.ParseUser;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainFragment extends Fragment implements OnMapReadyCallback {

    private LatLng currentCameraPosition;
    private boolean isLocationShowed;
    private CameraPosition currentCam;
    private SupportMapFragment map;
    private static final String ARG_POSITION = "position";
    private boolean isLongPressed = false;
    private int stackSize = 0;

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
        map = (SupportMapFragment) this.getChildFragmentManager().findFragmentById(R.id.map);
        map.getMapAsync(this);

        getFragmentManager().addOnBackStackChangedListener(new FragmentManager.OnBackStackChangedListener() {
            @Override
            public void onBackStackChanged() {
                if (isLongPressed) {
                    int current = getFragmentManager().getBackStackEntryCount();
                    if (current == 1) stackSize = 1;
                    if (current == 0 && stackSize == 1) {
                        ActionBar actionBar = ((ActionBarActivity)getActivity()).getSupportActionBar();
                        if (actionBar != null) {
                            actionBar.setHomeButtonEnabled(false);
                            actionBar.setDisplayHomeAsUpEnabled(false);
                            actionBar.setTitle(R.string.app_name);
                        }
                        MyViewPager pager = (MyViewPager) getActivity().findViewById(R.id.pager);
                        pager.setPagingEnabled(true);
                        map.getMap().getUiSettings().setAllGesturesEnabled(true);

                        if (currentCameraPosition != null && MainActivity.EVENT_FRAGMENT_RESULT.equals("done")) {
                            ArrayList<LatLng> addMarkers = new ArrayList<>(getEventLocation("location"));
                            for (int i = 0; i < addMarkers.size(); i++) {
                                if (addMarkers.get(i).equals(currentCameraPosition))
                                    map.getMap().addMarker(new MarkerOptions()
                                            .position(new LatLng(addMarkers.get(i).latitude, addMarkers.get(i).longitude))
                                            .icon(BitmapDescriptorFactory.fromResource(getMarkerIcon(getEventString("category").get(i)))));
                            }
                        }

                        isLongPressed = false;
                        stackSize = 0;
                    }
                }
            }
        });
        return rootView;
    }

    private boolean isMarkerOnArray(List<Marker> array, LatLng m) {
            for(Marker marker : array) {
                if (marker.getPosition().latitude == m.latitude && marker.getPosition().longitude == m.longitude)
                    return true;
            }
        return false;
    }

    private boolean isMarkerOnArray(Marker marker, LatLng m) {
        return marker.getPosition().latitude == m.latitude && marker.getPosition().longitude == m.longitude;
    }

    private int getMarkerIcon(String key){
        ArrayList<Integer> event_markers = new ArrayList<>();
        event_markers.add(R.drawable.marker_meet);
        event_markers.add(R.drawable.marker_movie);
        event_markers.add(R.drawable.marker_sport);
        event_markers.add(R.drawable.marker_business);
        event_markers.add(R.drawable.marker_coffee);
        event_markers.add(R.drawable.marker_love);

        ArrayList<String> values = new ArrayList<>();
        values.add("Meet");
        values.add("Movie");
        values.add("Sport");
        values.add("Business");
        values.add("Coffee");
        values.add("Love");

        return event_markers.get(values.indexOf(key));
    }

    private ArrayList<Date> getEventDate(final String stringKey){
        final ArrayList<Date> eventString = new ArrayList<>();
        final ParseUser currentUser = ParseUser.getCurrentUser();
        if (currentUser != null) {
            ParseRelation<ParseObject> relation = currentUser.getRelation("goEvent");
            ParseQuery query = relation.getQuery().whereExists(stringKey);
            try {
                List<ParseObject> queryResult = query.find();
                for(ParseObject po : queryResult) {
                    Date name = po.getDate(stringKey);
                    eventString.add(name);
                }
            }
            catch(ParseException e) {
                Log.d("getEventString", "Error: " + e.getMessage());
            }
        }
        return eventString;
    }

    private ArrayList<String> getEventString(final String stringKey){
        final ArrayList<String> eventString = new ArrayList<>();
        final ParseUser currentUser = ParseUser.getCurrentUser();
        if (currentUser != null) {
            ParseRelation<ParseObject> relation = currentUser.getRelation("goEvent");
            ParseQuery query = relation.getQuery().whereExists(stringKey);
            try {
                List<ParseObject> queryResult = query.find();
                for(ParseObject po : queryResult) {
                    String name = po.getString(stringKey);
                    eventString.add(name);
                }
            }
            catch(ParseException e) {
                Log.d("getEventString", "Error: " + e.getMessage());
            }
        }
        return eventString;
    }

    private ArrayList<LatLng> getEventLocation(final String stringKey){
        final ArrayList<LatLng> location = new ArrayList<>();
        final ParseUser currentUser = ParseUser.getCurrentUser();
        if (currentUser != null) {
            ParseRelation<ParseObject> relation = currentUser.getRelation("goEvent");
            ParseQuery query = relation.getQuery().whereExists(stringKey);
            try {
                List<ParseObject> queryResult = query.find();
                for(ParseObject po : queryResult) {
                    LatLng markerLocation = new LatLng(po.getParseGeoPoint(stringKey).getLatitude(),
                            po.getParseGeoPoint(stringKey).getLongitude());
                    location.add(markerLocation);
                }
            }
            catch(ParseException e) {
                Log.d("getEventLocation", "Error: " + e.getMessage());
            }
        }
        return location;
    }

    @Override
    public void onMapReady(final GoogleMap map) {
        map.getUiSettings().setCompassEnabled(true);
        map.getUiSettings().setTiltGesturesEnabled(true);
        map.getUiSettings().setRotateGesturesEnabled(true);
        map.getUiSettings().setScrollGesturesEnabled(true);
        map.getUiSettings().setZoomControlsEnabled(true);
        map.getUiSettings().setZoomGesturesEnabled(true);
        map.setMyLocationEnabled(true);
        map.setMapType(GoogleMap.MAP_TYPE_NORMAL);

        ArrayList<LatLng> addMarkers = new ArrayList<>(getEventLocation("location"));
        for (int i = 0; i < addMarkers.size(); i++) {
            map.addMarker(new MarkerOptions()
                    .position(new LatLng(addMarkers.get(i).latitude, addMarkers.get(i).longitude))
                    .icon(BitmapDescriptorFactory.fromResource(getMarkerIcon(getEventString("category").get(i)))));
        }

        map.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
            @Override
            public View getInfoWindow(Marker arg0) {
                return null;
            }

            @Override
            public View getInfoContents(Marker arg0) {
                View v = LayoutInflater.from(getActivity()).inflate(R.layout.map_info_window, null);
                LatLng latLng = arg0.getPosition();

                TextView mapTitle = (TextView) v.findViewById(R.id.map_title);
                TextView mapStartDate = (TextView) v.findViewById(R.id.map_start_date);
                TextView mapDuration = (TextView) v.findViewById(R.id.map_duration);
                TextView mapEmptySeats = (TextView) v.findViewById(R.id.map_empty_seats);

                ArrayList<LatLng> location = new ArrayList<>(getEventLocation("location"));
                for (int i = 0; i < location.size(); i++) {
                    if (location.get(i).equals(latLng)) {
                        SimpleDateFormat mFormat;
                        if (getResources().getConfiguration().locale != Locale.US)
                            mFormat = new SimpleDateFormat("d MMMM, yyyy 'на' HH:mm");
                        else
                            mFormat = new SimpleDateFormat("MMMM dd, yyyy 'at' h:mm a");
                        mapStartDate.setText(mFormat.format(getEventDate("startDate").get(i)));
                        mapTitle.setText(getEventString("title").get(i));
                        mapDuration.setText(getEventString("temporary").get(i));
                        mapEmptySeats.setText(getEventString("avaibleSeats").get(i));
                    }
                }
                return v;
            }
        });

        map.setOnMyLocationChangeListener(new GoogleMap.OnMyLocationChangeListener() {
            @Override
            public void onMyLocationChange(Location location) {
                if (!isLocationShowed) {
                    /*CameraUpdate center = CameraUpdateFactory.newLatLng(new LatLng(location.getLatitude(), location.getLongitude()));
                    CameraUpdate zoom = CameraUpdateFactory.zoomTo(16);
                    map.moveCamera(center);
                    map.animateCamera(zoom);*/
                    LatLng latLng;
                    CameraUpdate cameraUpdate;
                    if (currentCam == null) {
                        latLng = new LatLng(location.getLatitude(), location.getLongitude());
                        cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 16);
                        map.animateCamera(cameraUpdate);
                    } else {
                        SharedPreferences settings = getActivity().getSharedPreferences("MAP_STATE", 0);
                        latLng = new LatLng(settings.getFloat("latitude", 0), settings.getFloat("longitude", 0));
                        cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 16);
                        map.moveCamera(cameraUpdate);
                    }
                    isLocationShowed = true;
                } else currentCam = map.getCameraPosition();
            }
        });
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

    private void destroyMap(){
        if (!getActivity().isFinishing()) {
            SupportMapFragment destroyMap = (SupportMapFragment) this.getChildFragmentManager().findFragmentById(R.id.map);
            if (destroyMap != null) {
                if (currentCam != null) {
                    SharedPreferences settings = getActivity().getSharedPreferences("MAP_STATE", 0);
                    SharedPreferences.Editor editor = settings.edit();
                    editor.putFloat("latitude", (float) currentCam.target.latitude);
                    editor.putFloat("longitude", (float) currentCam.target.longitude);
                    editor.apply();
                }
                getFragmentManager().beginTransaction().remove(destroyMap).commit();
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        destroyMap();
    }
}
