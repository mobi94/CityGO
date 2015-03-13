package com.android.socialnetworks;

import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.Date;

public class DetailedEventFragment extends Fragment {

    private ImageView avatar;
    private TextView creatorName;
    private TextView creatorAge;
    private TextView title;
    private TextView description;
    private TextView availableSeats;
    private TextView startDate;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.detailed_event_fragment, container, false);

        setHasOptionsMenu(true);

        ActionBar actionBar = ((ActionBarActivity)getActivity()).getSupportActionBar();
        if (actionBar != null) {
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle("Event details");
        }

        avatar = (ImageView)rootView.findViewById(R.id.detailed_avatar);
        title = (TextView) rootView.findViewById(R.id.detailed_title);
        creatorName = (TextView) rootView.findViewById(R.id.detailed_creator_name);
        creatorAge = (TextView) rootView.findViewById(R.id.detailed_creator_age);
        description = (TextView) rootView.findViewById(R.id.detailed_description);
        availableSeats = (TextView) rootView.findViewById(R.id.detailed_available_seats);
        startDate = (TextView) rootView.findViewById(R.id.detailed_start_date);

        if (MainActivity.newMarker != null) {
            if (actionBar != null) {
                actionBar.setBackgroundDrawable(new ColorDrawable(getEventColor(MainActivity.newMarker.getCategory())));
            }
            RelativeLayout currentLayout = (RelativeLayout) rootView.findViewById(R.id.detailed_fragment);
            currentLayout.setBackgroundResource(getMarkerIcon(MainActivity.newMarker.getCategory()));

            if (MainActivity.newMarker.getCreatorAvatarUrl() != null && !MainActivity.newMarker.getCreatorAvatarUrl().equals("")) {
                if (MainActivity.newMarker.getCreatorAvatarUrl().endsWith(".png"))
                    Picasso.with(getActivity())
                        .load(MainActivity.newMarker.getCreatorAvatarUrl())
                        .into(avatar);
                else
                    Picasso.with(getActivity())
                            .load(MainActivity.newMarker.getCreatorAvatarUrl())
                            .transform(MainActivity.transformation())
                            .resize(400, 400)
                            .centerCrop()
                            .into(avatar);
            }
            title.setText(MainActivity.newMarker.getTitle());
            creatorName.setText(MainActivity.newMarker.getCreatorNickName());
            creatorAge.setText(", " + MainActivity.newMarker.getCreatorAge());
            description.setText(MainActivity.newMarker.getDescription());
            availableSeats.setText("Available seats: " + MainActivity.newMarker.getAvailableSeats());
            Date now = new Date();
            String diffString;
            if (MainActivity.newMarker.getStartDate().getTime() > now.getTime()) {
                long remainingTime = MainActivity.newMarker.getStartDate().getTime() - now.getTime();
                long diffSeconds = remainingTime / 1000 % 60;
                long diffMinutes = remainingTime / (60 * 1000) % 60;
                long diffHours = remainingTime / (60 * 60 * 1000) % 24;
                long diffDays = remainingTime / (24 * 60 * 60 * 1000);

                if (diffDays == 0 && diffHours != 0 && diffMinutes != 0) diffString = Long.toString(diffHours) +
                        " h " + Long.toString(diffMinutes) + " m " + Long.toString(diffSeconds) + " s";
                else if (diffDays == 0 && diffHours == 0 && diffMinutes != 0) diffString = Long.toString(diffMinutes) + " m "
                        + Long.toString(diffSeconds) + " s";
                else if (diffDays == 0 && diffHours == 0) diffString = Long.toString(diffSeconds) + " s";
                else diffString = Long.toString(diffDays) + " d " + Long.toString(diffHours) +
                        " h " + Long.toString(diffMinutes) + " m " + Long.toString(diffSeconds) + " s";

                MyCountDownTimer countDownTimer = new MyCountDownTimer(remainingTime, 1000);
                countDownTimer.start();
            } else diffString = "Event completed";
            startDate.setText(diffString);
        }

        return rootView;
    }

    private int getMarkerIcon(String categoryIcon){
        switch(categoryIcon){
            case "0": return R.drawable.detailed_love;
            case "1": return R.drawable.detailed_movie;
            case "2": return R.drawable.detailed_sport;
            case "3": return R.drawable.detailed_business;
            case "4": return R.drawable.detailed_coffee;
            case "5": return R.drawable.detailed_coffee;
            default: return R.drawable.detailed_coffee;
        }
    }

    private int getEventColor(String categoryIcon){
        switch(categoryIcon){
            case "0": return 0xff9c0307;
            case "1": return 0xff000000;
            case "2": return 0xff718d48;
            case "3": return 0xffe3a92b;
            case "4": return 0xff663a2e;
            case "5": return 0xff663a2e;
            default: return 0xff663a2e;
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

    public class MyCountDownTimer extends CountDownTimer {
        public MyCountDownTimer(long startTime, long interval) {
            super(startTime, interval);
        }

        @Override
        public void onFinish() {
            startDate.setText("Event completed");
        }

        @Override
        public void onTick(long millisUntilFinished) {
            long diffSeconds = millisUntilFinished / 1000 % 60;
            long diffMinutes = millisUntilFinished / (60 * 1000) % 60;
            long diffHours = millisUntilFinished / (60 * 60 * 1000) % 24;
            long diffDays = millisUntilFinished / (24 * 60 * 60 * 1000);
            if (diffDays == 0 && diffHours != 0 && diffMinutes != 0) startDate.setText(Long.toString(diffHours) +
                    " h " + Long.toString(diffMinutes) + " m " + Long.toString(diffSeconds) + " s");
            else if (diffDays == 0 && diffHours == 0 && diffMinutes != 0) startDate.setText(Long.toString(diffMinutes) + " m "
                    + Long.toString(diffSeconds) + " s");
            else if (diffDays == 0 && diffHours == 0) startDate.setText(Long.toString(diffSeconds) + " s");
            else startDate.setText(Long.toString(diffDays) + " d " + Long.toString(diffHours) +
                    " h " + Long.toString(diffMinutes) + " m " + Long.toString(diffSeconds) + " s");
        }
    }
}
