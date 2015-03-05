package com.android.socialnetworks;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.TypedValue;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;

import com.astuetz.PagerSlidingTabStrip;
import com.parse.Parse;
import com.parse.ParseUser;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);
        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(false); // remove the left caret
            actionBar.setDisplayShowHomeEnabled(false); // remove the icon
        }
        Parse.initialize(this, "OZYscvQi1cKHCP0vo6hPbbGAPvWs6M6vuMvrMRHi", "CTt2KMlNavNfboR5Tt0f8bA0I0h38ZgCFPtE6I5s");

        if (isFirstTime()) {
            startActivity(new Intent(this, TutorialActivity.class));
            finish();
        }
        else{
            ParseUser currentUser = ParseUser.getCurrentUser();
            if (currentUser == null) {
                startActivity(new Intent(this, SignUpActivity.class));
                finish();
            }
            else{
                /*getFragmentManager().beginTransaction()
                        .add(R.id.container, new MainFragment())
                        .commit();*/
                intialiseViewPager();
            }
        }
    }

    private void intialiseViewPager() {
        final int pageMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4, getResources()
                .getDisplayMetrics());
        PagerSlidingTabStrip tabs = (PagerSlidingTabStrip) findViewById(R.id.tabs);
        MyViewPager pager = (MyViewPager) findViewById(R.id.pager);
        pager.setAdapter(new MyPagerAdapter(getFragmentManager()));
        pager.setPageMargin(pageMargin);
        pager.setCurrentItem(1);
        pager.setOffscreenPageLimit(2);
        tabs.setViewPager(pager);
        /*
        ViewPager pager = (ViewPager) findViewById(R.id.pager);
        pager.setAdapter(new MyPagerAdapter(getFragmentManager()));
        pager.setPageMargin(pageMargin);
        pager.setCurrentItem(1);
        pager.setOffscreenPageLimit(2);
        tabs.setViewPager(pager);*/
    }

    public static boolean hasNavBar() {
        boolean hasBackKey = KeyCharacterMap.deviceHasKey(KeyEvent.KEYCODE_BACK);
        boolean hasHomeKey = KeyCharacterMap.deviceHasKey(KeyEvent.KEYCODE_HOME);
        return !(hasBackKey && hasHomeKey);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_refresh:
                return true;
            case R.id.action_help:
                return true;
            case R.id.action_check_updates:
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private boolean isFirstTime()
    {
        SharedPreferences preferences = getPreferences(MODE_PRIVATE);
        boolean runBefore = preferences.getBoolean("RunBefore", false);
        if (!runBefore) {
            // first time
            SharedPreferences.Editor editor = preferences.edit();
            editor.putBoolean("RunBefore", true);
            editor.apply();
        }
        return !runBefore;
    }

    public class MyPagerAdapter extends FragmentPagerAdapter {

        private final String[] TITLES = {
                getString(R.string.tabs_chat),
                getString(R.string.tabs_map),
                getString(R.string.tabs_profile)
        };

        public MyPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return TITLES[position];
        }

        @Override
        public int getCount() {
            return TITLES.length;
        }

        @Override
        public Fragment getItem(int position) {
            switch(position) {
                case 0: return ChatFragment.newInstance(position);
                case 1: return MainFragment.newInstance(position);
                case 2: return ProfileFragment.newInstance(position);
                default: return null;
            }
        }
    }
}
