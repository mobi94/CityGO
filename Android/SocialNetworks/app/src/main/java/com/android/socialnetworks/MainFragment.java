package com.android.socialnetworks;

import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
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
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class MainFragment extends Fragment implements OnMapReadyCallback {

    private ArrayList<MyMarker> markersArray = new ArrayList<>();
    private ArrayList<String> markersIdArray;
    private HashMap<Marker, MyMarker> markersHashMap;

    private MyProgressDialog progressDialog;
    private MyMarker currentMarker;
    private Marker currentMarkerKey;
    private LatLng currentCameraPosition;
    private boolean isLocationShowed;
    private CameraPosition currentCam;
    private SupportMapFragment map;
    private static final String ARG_POSITION = "position";
    private boolean isLongPressed = false;
    private boolean isInfoWindowPressedToEdit = false;
    private boolean isInfoWindowPressedToShow = false;
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
        progressDialog = new MyProgressDialog(getActivity());
        setHasOptionsMenu(true);

        isLocationShowed = false;
        markersHashMap = new HashMap<Marker, MyMarker>();
        map = (SupportMapFragment) this.getChildFragmentManager().findFragmentById(R.id.map);
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
                if (isLongPressed || isInfoWindowPressedToEdit || isInfoWindowPressedToShow) {
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
                        else if (isInfoWindowPressedToEdit) changeCurrentMarker();
                        MyViewPager pager = (MyViewPager) getActivity().findViewById(R.id.pager);
                        pager.setPagingEnabled(true);
                        map.getMap().getUiSettings().setAllGesturesEnabled(true);
                        isLongPressed = false;
                        isInfoWindowPressedToEdit = false;
                        isInfoWindowPressedToShow = false;
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

    private ArrayList<Date> getEventDate(final String stringKey){
        final ArrayList<Date> eventString = new ArrayList<>();
        final ParseUser currentUser = ParseUser.getCurrentUser();
        if (currentUser != null) {
            /*ParseRelation<ParseObject> relation = currentUser.getRelation("goEvent");
            ParseQuery query = relation.getQuery().whereExists(stringKey);*/
            float [] distance = new float[1];
            Location.distanceBetween(
                    map.getMap().getProjection().getVisibleRegion().farLeft.latitude,
                    map.getMap().getProjection().getVisibleRegion().farLeft.longitude,
                    map.getMap().getProjection().getVisibleRegion().farRight.latitude,
                    map.getMap().getProjection().getVisibleRegion().farRight.longitude,
                    distance);
            ParseQuery<ParseObject> query = ParseQuery.getQuery("GoEvents");
            query.whereExists(stringKey).whereWithinKilometers("location", new ParseGeoPoint(
                            map.getMap().getCameraPosition().target.latitude, map.getMap().getCameraPosition().target.longitude),
                    distance[0] / 1000);
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
            float [] distance = new float[1];
            Location.distanceBetween(
                    map.getMap().getProjection().getVisibleRegion().farLeft.latitude,
                    map.getMap().getProjection().getVisibleRegion().farLeft.longitude,
                    map.getMap().getProjection().getVisibleRegion().farRight.latitude,
                    map.getMap().getProjection().getVisibleRegion().farRight.longitude,
                    distance);
            ParseQuery<ParseObject> query = ParseQuery.getQuery("GoEvents");
            query.whereExists(stringKey).whereWithinKilometers("location", new ParseGeoPoint(
                            map.getMap().getCameraPosition().target.latitude, map.getMap().getCameraPosition().target.longitude),
                    distance[0] / 1000);
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

    private ArrayList<String> getEventObjectId(){
        final ArrayList<String> eventString = new ArrayList<>();
        final ParseUser currentUser = ParseUser.getCurrentUser();
        if (currentUser != null) {
            float [] distance = new float[1];
            Location.distanceBetween(
                    map.getMap().getProjection().getVisibleRegion().farLeft.latitude,
                    map.getMap().getProjection().getVisibleRegion().farLeft.longitude,
                    map.getMap().getProjection().getVisibleRegion().farRight.latitude,
                    map.getMap().getProjection().getVisibleRegion().farRight.longitude,
                    distance);
            ParseQuery<ParseObject> query = ParseQuery.getQuery("GoEvents");
            query.whereWithinKilometers("location", new ParseGeoPoint(
                            map.getMap().getCameraPosition().target.latitude, map.getMap().getCameraPosition().target.longitude),
                    distance[0] / 1000);
            try {
                List<ParseObject> queryResult = query.find();
                for(ParseObject po : queryResult) {
                    String name = po.getObjectId();
                    eventString.add(name);
                }
            }
            catch(ParseException e) {
                Log.d("getEventString", "Error: " + e.getMessage());
            }
        }
        return eventString;
    }

    private ArrayList<LatLng> getEventLocation(final String stringKey) {
        final ArrayList<LatLng> location = new ArrayList<>();
        final ParseUser currentUser = ParseUser.getCurrentUser();
        if (currentUser != null) {
            float [] distance = new float[1];
            Location.distanceBetween(
                    map.getMap().getProjection().getVisibleRegion().farLeft.latitude,
                    map.getMap().getProjection().getVisibleRegion().farLeft.longitude,
                    map.getMap().getProjection().getVisibleRegion().farRight.latitude,
                    map.getMap().getProjection().getVisibleRegion().farRight.longitude,
                    distance);
            ParseQuery<ParseObject> query = ParseQuery.getQuery("GoEvents");
            query.whereExists(stringKey).whereWithinKilometers("location", new ParseGeoPoint(
                            map.getMap().getCameraPosition().target.latitude, map.getMap().getCameraPosition().target.longitude),
                    distance[0] / 1000);
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

    private void addNewMarker() {
        if (MainActivity.EVENT_FRAGMENT_RESULT.equals("done"))
            if (!isMarkerOnArray(markersArray, currentCameraPosition)) {
                if (MainActivity.newMarker != null) {
                    Marker currentMarker = map.getMap().addMarker(new MarkerOptions()
                            .position(new LatLng(MainActivity.newMarker.getLocation().latitude,
                                    MainActivity.newMarker.getLocation().longitude))
                            .icon(BitmapDescriptorFactory.fromResource(getMarkerIcon(MainActivity.newMarker.getCategory()))));
                    markersArray.add(MainActivity.newMarker);
                    ArrayList<String> markerIds = new ArrayList<>(getEventObjectId());
                    markersIdArray.add(markerIds.get(markerIds.size()-1));
                    markersHashMap.put(currentMarker, MainActivity.newMarker);
                }
            }
    }

    private void changeCurrentMarker() {
        if (MainActivity.EVENT_FRAGMENT_RESULT.equals("done"))
            if (MainActivity.newMarker != null) {
                markersArray.set(markersArray.indexOf(currentMarker), MainActivity.newMarker);
                markersHashMap.put(currentMarkerKey, MainActivity.newMarker);
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
            }
        }
    }

    private void updateMarkers() {
        if(!SignUpActivity.isNetworkOn(getActivity())) {
            Toast.makeText(getActivity(), "Please, check your internet connection", Toast.LENGTH_SHORT).show();
        } else {
            map.getMap().clear();
            markersArray.clear();
            markersHashMap.clear();
            progressDialog.showProgress("Loading markers...");
            ArrayList<String> markerTitles = new ArrayList<>(getEventString("title"));
            ArrayList<String> markerCategories = new ArrayList<>(getEventString("category"));
            ArrayList<String> markerAuthorAvatarUrl = new ArrayList<>(getEventString("creatorAvatarUrl"));
            ArrayList<String> markerAuthorNickName = new ArrayList<>(getEventString("creatorNickName"));
            ArrayList<String> markerAuthorName = new ArrayList<>(getEventString("creatorName"));
            ArrayList<String> markerAuthorGender = new ArrayList<>(getEventString("creatorGender"));
            ArrayList<String> markerAuthorAge = new ArrayList<>(getEventString("creatorAge"));
            ArrayList<String> markerDuration = new ArrayList<>(getEventString("temporary"));
            ArrayList<String> markerAvailableSeats = new ArrayList<>(getEventString("avaibleSeats"));
            ArrayList<String> markerDescription = new ArrayList<>(getEventString("description"));
            ArrayList<Date> markerStartDates = new ArrayList<>(getEventDate("startDate"));
            ArrayList<LatLng> markerLocations = new ArrayList<>(getEventLocation("location"));
            markersIdArray = new ArrayList<>(getEventObjectId());
            for (int i = 0; i < markerLocations.size(); i++) {
                markersArray.add(new MyMarker(markerTitles.get(i), markerAuthorAge.get(i), markerAuthorGender.get(i), markerAuthorName.get(i),
                        markerAuthorNickName.get(i), markerAuthorAvatarUrl.get(i), markerCategories.get(i), markerDuration.get(i),
                        markerAvailableSeats.get(i), markerDescription.get(i), markerStartDates.get(i), markerLocations.get(i)));
            }
            plotMarkers(markersArray);
            progressDialog.hideProgress();
        }
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
        map.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
            @Override
            public View getInfoWindow(Marker currentMarker) {
                View v = LayoutInflater.from(getActivity()).inflate(R.layout.map_info_window, null);
                MyMarker myMarker = markersHashMap.get(currentMarker);

                LinearLayout currentLayout = (LinearLayout) v.findViewById(R.id.map_info);
                if (myMarker.getCreatorName().equals(ParseUser.getCurrentUser().getUsername()))
                    currentLayout.setBackgroundResource(R.drawable.background_normal_name);
                else
                    currentLayout.setBackgroundResource(R.drawable.background_signup);

                TextView mapTitle = (TextView) v.findViewById(R.id.map_title);
                TextView mapCreatorGender = (TextView) v.findViewById(R.id.map_creator_gender);
                TextView mapStartDate = (TextView) v.findViewById(R.id.map_start_date);

                SimpleDateFormat format;
                if (getResources().getConfiguration().locale != Locale.US)
                    format = new SimpleDateFormat("d MMMM, yyyy 'на' HH:mm");
                else
                    format = new SimpleDateFormat("MMMM dd, yyyy 'at' h:mm a");
                mapStartDate.setText(format.format(myMarker.getStartDate()));
                mapTitle.setText(myMarker.getTitle());
                mapCreatorGender.setText("Gender: " + myMarker.getCreatorGender());

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
                MyMarker myMarker = markersHashMap.get(marker);
                String currentUser = ParseUser.getCurrentUser().getUsername();
                if (markersHashMap.get(marker).getCreatorName().equals(currentUser)) {
                    if (!isInfoWindowPressedToEdit) {
                        map.getUiSettings().setAllGesturesEnabled(false);
                        MyViewPager pager = (MyViewPager) getActivity().findViewById(R.id.pager);
                        pager.setPagingEnabled(false);
                        currentMarker = myMarker;
                        currentMarkerKey = marker;

                        MainActivity.newMarker = myMarker;
                        NewEventFragment newEventFragment = new NewEventFragment();
                        Bundle bundle = new Bundle();
                        bundle.putString("CURRENT_USER", currentUser);
                        bundle.putString("MARKER_ID", markersIdArray.get(markersArray.indexOf(currentMarker)));
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
                    MainActivity.newMarker = myMarker;
                    getFragmentManager().beginTransaction()
                            .setCustomAnimations(
                                    R.anim.slide_in_bottom, R.anim.slide_out_bottom,
                                    R.anim.slide_in_bottom, R.anim.slide_out_bottom)
                            .add(R.id.container, new DetailedEventFragment())
                            .addToBackStack(null)
                            .commit();
                    marker.hideInfoWindow();
                    isInfoWindowPressedToShow = true;
                }
            }
        });
        map.setOnMyLocationChangeListener(new GoogleMap.OnMyLocationChangeListener() {
            @Override
            public void onMyLocationChange(Location location) {
                if (!isLocationShowed) {
                    LatLng latLng;
                    CameraUpdate cameraUpdate;
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
