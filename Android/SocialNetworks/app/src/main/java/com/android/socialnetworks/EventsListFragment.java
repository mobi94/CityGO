package com.android.socialnetworks;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
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
import android.widget.TextView;
import android.widget.Toast;

import com.appyvet.rangebar.RangeBar;
import com.astuetz.PagerSlidingTabStrip;
import com.google.android.gms.maps.model.LatLng;
import com.kyleduo.switchbutton.SwitchButton;
import com.parse.FunctionCallback;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.twotoasters.jazzylistview.JazzyHelper;
import com.twotoasters.jazzylistview.JazzyListView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class EventsListFragment extends Fragment {

    private ArrayList<ListItem> listItems;
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
        inflater.inflate(R.menu.menu_events_list, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                getActivity().getSupportFragmentManager().popBackStack();
                return true;
            case R.id.ic_action_filter_for_events_list:
                eventsFilterDialog();
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
        listAdapter = new ListAdapter(this, getActivity(), R.layout.events_list_item, listItems);
        setListViewItems(false);

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

            if (change) {
                ParseQuery<ParseObject> query = ParseQuery.getQuery("GoEvents");
                for (ListItem li : listItems)
                    query.whereEqualTo("objectId", li.getObjectId());
                try {
                    List<ParseObject> queryResult = query.find();
                    if (queryResult.size() != 0) {
                        ListItem listItem;
                        deleteSectionHeader();
                        for (ParseObject po : queryResult) {
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
                        }
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                    Toast.makeText(getActivity(), "UPDATE_EVENT_LIST_ERROR: " + e, Toast.LENGTH_LONG).show();
                }
            }
            else {
                listItems.clear();
                for(MyMarker mm: MainFragment.markersArray) {
                    ListItem listItem = new ListItem(
                            mm.getCategory(),
                            mm.getTitle(),
                            mm.getAvailableSeats() + " available seats",
                            mm.getStartDate(),
                            mm.getObjectId(),
                            mm.getCreatorName());
                    // setUp IS REQUIRED
                    listItem.setUp(COLLAPSED_HEIGHT, EXPANDED_HEIGHT, false);
                    listItems.add(listItem);
                }
            }
            if(listItems.size() == 0) {
                if (eventListLoadingProgress.getVisibility() == View.VISIBLE)
                    eventListLoadingProgress.setVisibility(View.GONE);
                eventListEmpty.setVisibility(View.VISIBLE);
            }
            else {
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
        listAdapter.clearSectionHeaderItem();

        listAdapter.addSectionHeaderItem(0);
        listItemHeaderIndexes.add(0);
        for(int i=0; i<listItems.size()-1; i++) {
            if (listItems.get(i).getDate().getTime() >= now.getTime() && listItems.get(i+1).getDate().getTime() < now.getTime()) {
                listAdapter.addSectionHeaderItem(i+2);
                listItemHeaderIndexes.add(i+2);
            }
        }
        for(int i=0; i<listItemHeaderIndexes.size(); i++)
            listItems.add(listItemHeaderIndexes.get(i), new ListItem());
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
                    MainActivity.disableViewPager((MyViewPager) getActivity().findViewById(R.id.pager),
                            (PagerSlidingTabStrip) getActivity().findViewById(R.id.tabs));
                    generateEventList();
                    isFragmentShown = true;
                }
            }
        });
        return anim;
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

        alertBw.setPositiveButton(getString(R.string.ok_button), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(final DialogInterface dialog, int which) {
                MainFragment.isFilterDialogUsed = true;
                updateMarkers();
            }
        });
        alertBw.setNeutralButton("Reset filters", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                boolean[] categories = MainFragment.filterCategories.clone();
                boolean[] sex = MainFragment.filterSex.clone();
                int[] age = MainFragment.filterAge.clone();
                for (int i=0; i<MainFragment.filterCategories.length; i++)
                    MainFragment.filterCategories[i] = true;
                for (int i=0; i<MainFragment.filterSex.length; i++)
                    MainFragment.filterSex[i] = true;
                MainFragment.filterAge[0]=1;
                MainFragment.filterAge[1]=35;

                if (!Arrays.equals(categories, MainFragment.filterCategories)
                        || !Arrays.equals(sex, MainFragment.filterSex)
                        || !Arrays.equals(age, MainFragment.filterAge)) {
                    MainFragment.isFilterDialogUsed = false;
                    updateMarkers();
                }
            }
        });

        switcherLove.setChecked(MainFragment.filterCategories[MainActivity.EventCategories.LOVE.ordinal()]);
        switcherMovie.setChecked(MainFragment.filterCategories[MainActivity.EventCategories.MOVIE.ordinal()]);
        switcherSport.setChecked(MainFragment.filterCategories[MainActivity.EventCategories.SPORT.ordinal()]);
        switcherBusiness.setChecked(MainFragment.filterCategories[MainActivity.EventCategories.BUSINESS.ordinal()]);
        switcherCoffee.setChecked(MainFragment.filterCategories[MainActivity.EventCategories.COFFEE.ordinal()]);
        switcherMeet.setChecked(MainFragment.filterCategories[MainActivity.EventCategories.MEET.ordinal()]);

        switcherLove.setOnCheckedChangeListener(MainFragment.switcherCategoryListener);
        switcherMovie.setOnCheckedChangeListener(MainFragment.switcherCategoryListener);
        switcherSport.setOnCheckedChangeListener(MainFragment.switcherCategoryListener);
        switcherBusiness.setOnCheckedChangeListener(MainFragment.switcherCategoryListener);
        switcherCoffee.setOnCheckedChangeListener(MainFragment.switcherCategoryListener);
        switcherMeet.setOnCheckedChangeListener(MainFragment.switcherCategoryListener);

        switcherMale.setOnCheckedChangeListener(MainFragment.switcherSexListener);
        switcherFemale.setOnCheckedChangeListener(MainFragment.switcherSexListener);
        switcherMale.setChecked(MainFragment.filterSex[0]);
        switcherFemale.setChecked(MainFragment.filterSex[1]);

        rangeMin.setText(Integer.toString(MainFragment.filterAge[0]));
        rangeMax.setText(Integer.toString(MainFragment.filterAge[1]));
        rangebar.setRangePinsByValue(MainFragment.filterAge[0], MainFragment.filterAge[1]);
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
                    MainFragment.filterAge[0]=left;
                    MainFragment.filterAge[1]=right;
                }
                else {
                    rangeMin.setText(leftPinValue);
                    rangeMax.setText(rightPinValue);
                    MainFragment.filterAge[0] = Integer.parseInt(leftPinValue);
                    MainFragment.filterAge[1] = Integer.parseInt(rightPinValue);
                }
            }
        });

        AlertDialog alertDialog = alertBw.create();
        alertDialog.show();
    }

    private void updateMarkers() {
        if(!SignUpActivity.isNetworkOn(getActivity())) {
            Toast.makeText(getActivity(), "Please, check your internet connection", Toast.LENGTH_SHORT).show();
        } else {
            MainActivity.EVENT_FRAGMENT_RESULT = "done";
            MainActivity.MAP_MARKERS_UPDATE = true;
            final LinearLayout eventListEmpty = (LinearLayout) getActivity().findViewById(R.id.events_fragment_empty);
            final LinearLayout eventListLoadingProgress = (LinearLayout) getActivity().findViewById(R.id.events_fragment_loading_progress);
            LinearLayout eventListNoNetwork = (LinearLayout) getActivity().findViewById(R.id.events_fragment_no_network);
            if (eventListEmpty.getVisibility() == View.VISIBLE)
                eventListEmpty.setVisibility(View.GONE);
            if (eventListNoNetwork.getVisibility() == View.VISIBLE)
                eventListNoNetwork.setVisibility(View.GONE);
            eventListLoadingProgress.setVisibility(View.VISIBLE);
            float [] distance = new float[1];
            Bundle bundle = getArguments();
            Location.distanceBetween(
                    bundle.getDouble("LEFT_VISIBLE_REGION_LAT", 0),
                    bundle.getDouble("LEFT_VISIBLE_REGION_LONG", 0),
                    bundle.getDouble("RIGHT_VISIBLE_REGION_LAT", 0),
                    bundle.getDouble("RIGHT_VISIBLE_REGION_LONG", 0),
                    distance);
            HashMap<String, Object> params = new HashMap<>();
            ArrayList<String> categories = new ArrayList<>();
            String[] sex = new String[2];
            int minAge = 1, maxAge = 70;
            if (MainFragment.isFilterDialogUsed) {
                for (int i = 0; i < MainFragment.filterCategories.length; i++) {
                    if (MainFragment.filterCategories[i])
                        categories.add(Integer.toString(i));
                }
                if (MainFragment.filterSex[0]) sex[0] = "male";
                if (MainFragment.filterSex[1]) sex[1] = "female";
                minAge = MainFragment.filterAge[0];
                maxAge = MainFragment.filterAge[1];
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
            params.put("location", new ParseGeoPoint(bundle.getDouble("LOCATION_LAT", 0),
                    bundle.getDouble("LOCATION_LONG", 0)));
            params.put("radius", distance[0] / 1000);
            ParseCloud.callFunctionInBackground("getFilteredEvents", params, new FunctionCallback<List<ParseObject>>() {
                @Override
                public void done(List<ParseObject> parseObjects, ParseException e) {
                    if (e != null) {
                        Toast.makeText(getActivity(), "UPDATE_EVENT_DATA_ERROR" + e, Toast.LENGTH_SHORT).show();
                    } else {
                        if (MainFragment.markersArray != null) MainFragment.markersArray.clear();
                        else MainFragment.markersArray = new ArrayList<>();
                        if (MainFragment.markersHashMap != null) MainFragment.markersHashMap.clear();
                        else MainFragment.markersHashMap = new HashMap<>();
                        for (ParseObject po : parseObjects) {
                            LatLng markerLocation = new LatLng(po.getParseGeoPoint("location").getLatitude(),
                                    po.getParseGeoPoint("location").getLongitude());
                            MainFragment.markersArray.add(new MyMarker(po.getObjectId(), po.getString("category"), markerLocation,
                                    po.getString("title"), po.getString("creatorName"), po.getString("creatorGender"),
                                    po.getString("avaibleSeats"), po.getString("temporary"), po.getDate("startDate")));
                        }
                        setListViewItems(false);
                    }
                }
            });
        }
    }
}
