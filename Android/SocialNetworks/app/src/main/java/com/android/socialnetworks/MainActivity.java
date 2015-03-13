package com.android.socialnetworks;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.TypedValue;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;

import com.astuetz.PagerSlidingTabStrip;
import com.google.android.gms.maps.model.LatLng;
import com.makeramen.RoundedTransformationBuilder;
import com.parse.Parse;
import com.parse.ParseUser;
import com.squareup.picasso.Transformation;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Random;

public class MainActivity extends ActionBarActivity {

    public static String EVENT_FRAGMENT_RESULT = "";
    public static MyMarker newMarker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);
        Toolbar toolbar = (Toolbar) findViewById(R.id.tool_bar);
        setSupportActionBar(toolbar);
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
                initialiseViewPager();
            }
        }
    }

    private void initialiseViewPager() {
        final int pageMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4, getResources()
                .getDisplayMetrics());
        final PagerSlidingTabStrip tabs = (PagerSlidingTabStrip) findViewById(R.id.tabs);
        MyViewPager pager = (MyViewPager) findViewById(R.id.pager);
        pager.setAdapter(new MyPagerAdapter(getSupportFragmentManager()));
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

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        EVENT_FRAGMENT_RESULT = "canceled";
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
            case R.id.action_search:
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

    public static int getAge(String string){
        DateFormat format = new SimpleDateFormat("dd.MM.yyyy", Locale.ENGLISH);
        Date date = null;
        try {
            date = format.parse(string);
        } catch (java.text.ParseException e) {
            e.printStackTrace();
        }
        Calendar birthDay = Calendar.getInstance();
        if (date != null) {
            birthDay.setTimeInMillis(date.getTime());
        }
        long currentTime = System.currentTimeMillis();
        Calendar now = Calendar.getInstance();
        now.setTimeInMillis(currentTime);
        return now.get(Calendar.YEAR) - birthDay.get(Calendar.YEAR);
    }

    private static final String ALLOWED_CHARACTERS ="0123456789qwertyuiopasdfghjklzxcvbnm";
    public static String getRandomString(final int sizeOfRandomString)
    {
        final Random random=new Random();
        final StringBuilder sb=new StringBuilder(sizeOfRandomString);
        for(int i=0;i<sizeOfRandomString;++i)
            sb.append(ALLOWED_CHARACTERS.charAt(random.nextInt(ALLOWED_CHARACTERS.length())));
        return sb.toString();
    }

    public static Transformation transformation(){
        return new RoundedTransformationBuilder()
                .borderColor(Color.WHITE)
                .borderWidthDp(1)
                .cornerRadiusDp(200)
                .oval(false)
                .build();
    }
}
