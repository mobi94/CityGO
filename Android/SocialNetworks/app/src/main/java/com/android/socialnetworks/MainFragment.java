package com.android.socialnetworks;

import android.app.ActionBar;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.desarrollodroide.libraryfragmenttransactionextended.FragmentTransactionExtended;
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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.map_fragment, container, false);

        isLocationShowed = false;
        MapFragment map = getMapFragment();
        map.getMapAsync(this);

        setHasOptionsMenu(true);
        ActionBar actionBar = getActivity().getActionBar();
        if (actionBar != null) {
            actionBar.setTitle(R.string.app_name);
            actionBar.setDisplayHomeAsUpEnabled(false);
        }
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
                    if (currentCam == null) {
                        latLng = new LatLng(location.getLatitude(), location.getLongitude());
                    }
                    else {
                        SharedPreferences settings = getActivity().getSharedPreferences("MAP_STATE", 0);
                        latLng = new LatLng(settings.getFloat("latitude", 0), settings.getFloat("longitude", 0));
                    }
                    CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 16);
                    map.animateCamera(cameraUpdate);
                    isLocationShowed = true;
                }
                else currentCam = map.getCameraPosition();
            }
        });
        map.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {
                Toast.makeText(getActivity(), "LongClick", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void destroyMap(){
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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        destroyMap();
    }

    @Override
    public void onStop() {
        super.onStop();
        destroyMap();
    }

    @Override
    public void onPause() {
        super.onPause();
        destroyMap();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_main, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_chat:
                return true;
            case R.id.action_profile:
                FragmentTransactionExtended fragmentTransactionExtended = new FragmentTransactionExtended(getActivity(),
                        getFragmentManager().beginTransaction(), MainFragment.this, new ProfileFragment(), R.id.container);
                fragmentTransactionExtended.addTransition(FragmentTransactionExtended.ZOOM_SLIDE_HORIZONTAL);
                fragmentTransactionExtended.commit();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
