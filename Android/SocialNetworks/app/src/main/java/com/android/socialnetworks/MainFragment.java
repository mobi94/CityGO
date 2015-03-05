package com.android.socialnetworks;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;

public class MainFragment extends Fragment implements OnMapReadyCallback {

    private boolean isLocationShowed;
    private CameraPosition currentCam;
    private MapFragment map;
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

        isLocationShowed = false;
        map = getMapFragment();
        map.getMapAsync(this);
/*
        setHasOptionsMenu(true);
        ActionBar actionBar = getActivity().getActionBar();
        if (actionBar != null) {
            actionBar.setTitle(R.string.app_name);
            actionBar.setDisplayHomeAsUpEnabled(false);
        }
*/
        getFragmentManager().addOnBackStackChangedListener(new FragmentManager.OnBackStackChangedListener() {
            @Override
            public void onBackStackChanged() {
                if (isLongPressed) {
                    int current = getFragmentManager().getBackStackEntryCount();
                    if (current == 1) stackSize = 1;
                    if (current == 0 && stackSize == 1) {
                        MyViewPager pager = (MyViewPager) getActivity().findViewById(R.id.pager);
                        pager.setPagingEnabled(true);
                        map.getMap().getUiSettings().setAllGesturesEnabled(true);
                        isLongPressed = false;
                        stackSize = 0;
                    }
                }
            }
        });
        return rootView;
    }
    private MapFragment getMapFragment() {
        FragmentManager fm;
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            Log.d("MainFragment", "using getFragmentManager");
            fm = getFragmentManager();
        } else {
            Log.d("MainFragment", "using getChildFragmentManager");
            fm = getChildFragmentManager();
        }
        return (MapFragment) fm.findFragmentById(R.id.map);
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
                    getFragmentManager().beginTransaction()
                            .setCustomAnimations(
                                    R.animator.slide_up, R.animator.slide_down,
                                    R.animator.slide_up, R.animator.slide_down)
                            .add(R.id.container, new NewEventFragment())
                            .addToBackStack(null)
                            .commit();
                    isLongPressed = true;
                }
            }
        });
    }

    private void destroyMap(){
        if (!getActivity().isFinishing()) {
            MapFragment destroyMap = getMapFragment();
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
