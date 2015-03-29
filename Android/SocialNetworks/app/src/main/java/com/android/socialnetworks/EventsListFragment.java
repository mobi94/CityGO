package com.android.socialnetworks;

import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.twotoasters.jazzylistview.JazzyHelper;
import com.twotoasters.jazzylistview.JazzyListView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.TreeSet;

public class EventsListFragment extends Fragment {

    private ArrayList<ListItem> listItems;
    private LatLng location;
    private float[] distance;
    private ListAdapter listAdapter;
    private int stackSize = 1;
    private boolean isFragmentShown = false;
    private ArrayList<Integer> listItemHeaderIndexes;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.events_list_fragment, container, false);
        setHasOptionsMenu(true);
        final ActionBar actionBar = ((ActionBarActivity) getActivity()).getSupportActionBar();
        if (actionBar != null) {
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(R.string.app_name);
            actionBar.setBackgroundDrawable(new ColorDrawable(0xff009A90));
        }

        Bundle bundle = getArguments();
        location = new LatLng(bundle.getDouble("LOCATION_LAT", 0), bundle.getDouble("LOCATION_LONG", 0));
        distance = bundle.getFloatArray("CURRENT_AREA");

        getFragmentManager().addOnBackStackChangedListener(new FragmentManager.OnBackStackChangedListener() {
            @Override
            public void onBackStackChanged() {
                if (isVisible()) {
                    int current = getFragmentManager().getBackStackEntryCount();
                    if (current == 2) stackSize = 2;
                    if (current == 1 && stackSize == 2) {
                        if (actionBar != null) {
                            actionBar.setHomeButtonEnabled(true);
                            actionBar.setDisplayHomeAsUpEnabled(true);
                            actionBar.setTitle(R.string.app_name);
                            actionBar.setBackgroundDrawable(new ColorDrawable(0xff009A90));
                        }
                        updateEventList();
                        enableAllViews();
                        stackSize = 1;
                    }
                }
            }
        });

        return rootView;
    }

    private void disableAllViews(){
        LinearLayout layout = (LinearLayout) getActivity().findViewById(R.id.events_list_fragment);
        for (int i = 0; i < layout.getChildCount(); i++) {
            View child = layout.getChildAt(i);
            child.setEnabled(false);
        }
    }

    private void enableAllViews(){
        LinearLayout layout = (LinearLayout) getActivity().findViewById(R.id.events_list_fragment);
        for (int i = 0; i < layout.getChildCount(); i++) {
            View child = layout.getChildAt(i);
            child.setEnabled(true);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.clear();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                getActivity().getSupportFragmentManager().popBackStack();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void updateEventList(){
        if (MainActivity.getEventListToUpdateFlag(MainActivity.UpdatedFrom.LIST_FRAGMENT)){
            setListViewItems(true);
        }
        else if(MainActivity.getEventListToDeleteFlag(MainActivity.UpdatedFrom.LIST_FRAGMENT)){
            deleteEventFromList();
        }
    }

    private void generateEventList(){
        JazzyListView listView = (JazzyListView) getActivity().findViewById(R.id.events_fragment_list);
        listView.setTransitionEffect(JazzyHelper.TILT);

        listItems = new ArrayList<>();
        setListViewItems(false);

        listAdapter = new ListAdapter(this, getActivity(), R.layout.profile_event_list, listItems);

        listView.setAdapter(listAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (!listItemHeaderIndexes.contains(position)) {
                    disableAllViews();
                    if (listItems.get(position).getCreatorName().equals(ParseUser.getCurrentUser().getUsername())) {
                        NewEventFragment newEventFragment = new NewEventFragment();
                        Bundle bundle = new Bundle();
                        bundle.putString("USED_FOR", "edit");
                        bundle.putString("MARKER_ID", listItems.get(position).getObjectId());
                        newEventFragment.setArguments(bundle);
                        getFragmentManager().beginTransaction()
                                .setCustomAnimations(
                                        R.anim.slide_in_bottom, R.anim.slide_out_bottom,
                                        R.anim.slide_in_bottom, R.anim.slide_out_bottom)
                                .add(R.id.container, newEventFragment)
                                .addToBackStack(null)
                                .commit();
                    } else {
                        DetailedEventFragment detailedEventFragment = new DetailedEventFragment();
                        Bundle bundle = new Bundle();
                        bundle.putString("MARKER_ID", listItems.get(position).getObjectId());
                        detailedEventFragment.setArguments(bundle);
                        getFragmentManager().beginTransaction()
                                .setCustomAnimations(
                                        R.anim.slide_in_bottom, R.anim.slide_out_bottom,
                                        R.anim.slide_in_bottom, R.anim.slide_out_bottom)
                                .add(R.id.container, detailedEventFragment)
                                .addToBackStack(null)
                                .commit();
                    }
                }
            }
        });
    }

    private void setListViewItems(final boolean change) {
        final LinearLayout eventListEmpty = (LinearLayout) getActivity().findViewById(R.id.events_fragment_empty);
        final LinearLayout eventListLoadingProgress = (LinearLayout) getActivity().findViewById(R.id.events_fragment_loading_progress);
        LinearLayout eventListNoNetwork = (LinearLayout) getActivity().findViewById(R.id.events_fragment_no_network);
        if(!SignUpActivity.isNetworkOn(getActivity())) {
            if (eventListEmpty.getVisibility() == View.VISIBLE)
                eventListEmpty.setVisibility(View.GONE);
            if (eventListLoadingProgress.getVisibility() == View.VISIBLE)
                eventListLoadingProgress.setVisibility(View.GONE);
            eventListNoNetwork.setVisibility(View.VISIBLE);
        } else {
            final int COLLAPSED_HEIGHT = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 56.5f, getResources().getDisplayMetrics());
            final int EXPANDED_HEIGHT = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 106.5f, getResources().getDisplayMetrics());

            if (eventListEmpty.getVisibility() == View.VISIBLE)
                eventListEmpty.setVisibility(View.GONE);
            if (eventListNoNetwork.getVisibility() == View.VISIBLE)
                eventListNoNetwork.setVisibility(View.GONE);
            eventListLoadingProgress.setVisibility(View.VISIBLE);

            ParseQuery<ParseObject> query = ParseQuery.getQuery("GoEvents");
            query.whereExists("startDate").whereExists("category").whereExists("title")
                    .whereExists("avaibleSeats").whereExists("creatorName").whereWithinKilometers("location", new ParseGeoPoint(
                            location.latitude, location.longitude), distance[0] / 1000).setLimit(25).addDescendingOrder("startDate");
            query.findInBackground(new FindCallback<ParseObject>() {
                @Override
                public void done(List<ParseObject> parseObjects, ParseException e) {
                    if (e == null) {
                        if (parseObjects.size() == 0) {
                            if (eventListLoadingProgress.getVisibility() == View.VISIBLE)
                                eventListLoadingProgress.setVisibility(View.GONE);
                            eventListEmpty.setVisibility(View.VISIBLE);
                        } else {
                            ListItem listItem;
                            deleteSectionHeader();
                            for (ParseObject po : parseObjects) {
                                if (change) {
                                    String objectId = po.getObjectId();
                                    if (MainActivity.markersIdsToUpdateForEventsListFragment != null) {
                                        for (int i = 0; i < MainActivity.markersIdsToUpdateForEventsListFragment.size(); i++) {
                                            if (MainActivity.markersIdsToUpdateForEventsListFragment.get(i).equals(objectId)) {
                                                listItem = new ListItem(
                                                        po.getString("category"),
                                                        po.getString("title"),
                                                        po.getString("avaibleSeats") + " available seats",
                                                        po.getDate("startDate"),
                                                        objectId,
                                                        po.getString("creatorName"));
                                                // setUp IS REQUIRED
                                                listItem.setUp(COLLAPSED_HEIGHT, EXPANDED_HEIGHT, false);

                                                for (int j = 0; j < listItems.size(); j++) {
                                                    if (listItems.get(j).getObjectId().equals(objectId)) {
                                                        listItems.set(j, listItem);
                                                        break;
                                                    }
                                                }
                                                break;
                                            }
                                        }
                                    }
                                } else {
                                    listItem = new ListItem(
                                            po.getString("category"),
                                            po.getString("title"),
                                            po.getString("avaibleSeats") + " available seats",
                                            po.getDate("startDate"),
                                            po.getObjectId(),
                                            po.getString("creatorName"));
                                    // setUp IS REQUIRED
                                    listItem.setUp(COLLAPSED_HEIGHT, EXPANDED_HEIGHT, false);
                                    listItems.add(listItem);
                                }
                            }
                            Collections.sort(listItems, new Comparator<ListItem>() {
                                @Override
                                public int compare(ListItem r1, ListItem r2) {
                                    return r1.getDate().compareTo(r2.getDate());
                                }
                            });
                            sortUpcomingIntoTop();
                            setSectionHeader();
                            listAdapter.notifyDataSetChanged();
                            eventListLoadingProgress.setVisibility(View.GONE);
                            MainActivity.setOffEventListToUpdateFlag(MainActivity.UpdatedFrom.LIST_FRAGMENT);
                            MainActivity.markersIdsToUpdateForEventsListFragment = new ArrayList<>();
                        }
                    } else
                        Toast.makeText(getActivity(), "UPDATE_EVENT_LIST_ERROR: " + e, Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    private void sortUpcomingIntoTop(){
        Date now = new Date();
        if(listItems.get(0).getDate().getTime() < now.getTime()) {
            for (int i = listItems.size() - 1; i >= 0; i--) {
                if (listItems.get(i).getDate().getTime() >= now.getTime()) {
                    ListItem listItem = listItems.get(i);
                    listItems.remove(i);
                    listItems.add(0, listItem);
                    i++;
                } else break;
            }
        }
    }

    private void deleteSectionHeader(){
        if(listItemHeaderIndexes != null) {
            for (int i = 0; i < listItemHeaderIndexes.size(); i++) {
                listItems.remove(listItemHeaderIndexes.get(i) -i);
            }
        }
    }

    private void setSectionHeader(){
        listItemHeaderIndexes = new ArrayList<>();
        Date now = new Date();
        boolean isHeaderInList = false;
        listAdapter.clearSectionHeaderItem();
        for(int i=0; i<listItems.size()-1; i++) {
            if (listItems.get(i).getDate().getTime() >= now.getTime() && listItems.get(i+1).getDate().getTime() < now.getTime()
                    || i==0) {
                if (i==0) {
                    listAdapter.addSectionHeaderItem(i);
                    listItemHeaderIndexes.add(i);
                }
                else{
                    listAdapter.addSectionHeaderItem(i+2);
                    listItemHeaderIndexes.add(i+2);
                }
                isHeaderInList = true;
            }
        }
        if (!isHeaderInList) {
            listAdapter.addSectionHeaderItem(-1);
        }
        else{
            for(int i=0; i<listItemHeaderIndexes.size(); i++) {
                listItems.add(listItemHeaderIndexes.get(i), new ListItem());
            }
        }
    }

    private void deleteEventFromList () {
        deleteSectionHeader();
        if (MainActivity.markersIdsToDeleteForEventsListFragment != null) {
            for (int i = 0; i < MainActivity.markersIdsToDeleteForEventsListFragment.size(); i++) {
                for (int j = 0; j < listItems.size(); j++) {
                    if (listItems.get(j).getObjectId().equals(MainActivity.markersIdsToDeleteForEventsListFragment.get(i))) {
                        listItems.remove(j);
                        break;
                    }
                }
            }
            sortUpcomingIntoTop();
            setSectionHeader();
            MainActivity.markersIdsToDeleteForEventsListFragment = new ArrayList<>();
            listAdapter.notifyDataSetChanged();
        }
        MainActivity.setOffEventListToDeleteFlag(MainActivity.UpdatedFrom.LIST_FRAGMENT);
    }

    @Override
    public Animation onCreateAnimation(int transit, boolean enter, int nextAnim) {
        Animation anim = AnimationUtils.loadAnimation(getActivity(), nextAnim);
        anim.setAnimationListener(new Animation.AnimationListener() {
            public void onAnimationStart(Animation animation) {}
            public void onAnimationRepeat(Animation animation) {}
            public void onAnimationEnd(Animation animation) {
                if (!isFragmentShown) {
                    generateEventList();
                    isFragmentShown = true;
                }
            }
        });
        return anim;
    }
}
