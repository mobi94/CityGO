package com.android.socialnetworks;

import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.pnikosis.materialishprogress.ProgressWheel;
import com.squareup.picasso.Picasso;

import java.util.Date;

public class DetailedEventFragment extends Fragment {

    private boolean isFragmentShown;
    private ImageView avatar;
    private TextView creatorName;
    private TextView creatorAge;
    private TextView title;
    private TextView description;
    private TextView availableSeats;
    private TextView startDate;
    private ActionBar actionBar;
    private MyProgressDialog progressDialog;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.detailed_event_fragment, container, false);

        setHasOptionsMenu(true);

        actionBar = ((ActionBarActivity)getActivity()).getSupportActionBar();
        if (actionBar != null) {
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle("Event details");
        }

        progressDialog = new MyProgressDialog(getActivity());
        isFragmentShown = false;

        avatar = (ImageView)rootView.findViewById(R.id.detailed_avatar);
        title = (TextView) rootView.findViewById(R.id.detailed_title);
        creatorName = (TextView) rootView.findViewById(R.id.detailed_creator_name);
        creatorAge = (TextView) rootView.findViewById(R.id.detailed_creator_age);
        description = (TextView) rootView.findViewById(R.id.detailed_description);
        description.setMovementMethod(ScrollingMovementMethod.getInstance());
        availableSeats = (TextView) rootView.findViewById(R.id.detailed_available_seats);
        startDate = (TextView) rootView.findViewById(R.id.detailed_start_date);

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
                    "h " + Long.toString(diffMinutes) + "m " + Long.toString(diffSeconds) + "s");
            else if (diffDays == 0 && diffHours == 0 && diffMinutes != 0) startDate.setText(Long.toString(diffMinutes) + "m "
                    + Long.toString(diffSeconds) + "s");
            else if (diffDays == 0 && diffHours == 0) startDate.setText(Long.toString(diffSeconds) + "s");
            else startDate.setText(Long.toString(diffDays) + "d " + Long.toString(diffHours) +
                    "h " + Long.toString(diffMinutes) + "m " + Long.toString(diffSeconds) + "s");
        }
    }

    @Override
    public Animation onCreateAnimation(int transit, boolean enter, int nextAnim) {
        Animation anim = AnimationUtils.loadAnimation(getActivity(), nextAnim);
        anim.setAnimationListener(new Animation.AnimationListener() {
            public void onAnimationStart(Animation animation) {}
            public void onAnimationRepeat(Animation animation) {}
            public void onAnimationEnd(Animation animation) {
                if (!isFragmentShown) {
                    progressDialog.showProgress("Loading...");
                    ParseQuery<ParseObject> query = ParseQuery.getQuery("GoEvents");
                    query.getInBackground(getArguments().getString("MARKER_ID", ""), new GetCallback<ParseObject>() {
                        @Override
                        public void done(ParseObject parseObject, ParseException e) {
                            if (e == null) {
                                final ProgressWheel wheel = (ProgressWheel) getActivity().findViewById(R.id.progress_wheel_detailed);
                                wheel.spin();

                                if (actionBar != null) {
                                    actionBar.setBackgroundDrawable(new ColorDrawable(getEventColor(parseObject.getString("category"))));
                                }
                                RelativeLayout currentLayout = (RelativeLayout) getActivity().findViewById(R.id.detailed_fragment);
                                currentLayout.setBackgroundResource(getMarkerIcon(parseObject.getString("category")));


                                if (parseObject.getString("creatorAvatarUrl") != null && !parseObject.getString("creatorAvatarUrl").equals("")) {
                                    Picasso.with(getActivity())
                                            .load(parseObject.getString("creatorAvatarUrl"))
                                            .transform(MainActivity.transformation())
                                            .resize(400, 400)
                                            .centerCrop()
                                            .into(avatar, new com.squareup.picasso.Callback() {
                                                @Override
                                                public void onSuccess() {
                                                    wheel.stopSpinning();
                                                }
                                                @Override
                                                public void onError() {
                                                    wheel.stopSpinning();
                                                }
                                            });
                                } else wheel.stopSpinning();
                                title.setText(parseObject.getString("title"));
                                creatorName.setText(parseObject.getString("creatorNickName"));
                                creatorAge.setText(", " + parseObject.getString("creatorAge"));
                                description.setText(parseObject.getString("description"));
                                availableSeats.setText("Available seats: " + parseObject.getString("avaibleSeats"));
                                Date now = new Date();
                                String diffString;
                                if (parseObject.getDate("startDate").getTime() > now.getTime()) {
                                    long remainingTime = parseObject.getDate("startDate").getTime() - now.getTime();
                                    long diffSeconds = remainingTime / 1000 % 60;
                                    long diffMinutes = remainingTime / (60 * 1000) % 60;
                                    long diffHours = remainingTime / (60 * 60 * 1000) % 24;
                                    long diffDays = remainingTime / (24 * 60 * 60 * 1000);

                                    if (diffDays == 0 && diffHours != 0 && diffMinutes != 0)
                                        diffString = Long.toString(diffHours) +
                                                "h " + Long.toString(diffMinutes) + "m " + Long.toString(diffSeconds) + "s";
                                    else if (diffDays == 0 && diffHours == 0 && diffMinutes != 0)
                                        diffString = Long.toString(diffMinutes) + "m "
                                                + Long.toString(diffSeconds) + "s";
                                    else if (diffDays == 0 && diffHours == 0)
                                        diffString = Long.toString(diffSeconds) + "s";
                                    else
                                        diffString = Long.toString(diffDays) + "d " + Long.toString(diffHours) +
                                                "h " + Long.toString(diffMinutes) + "m " + Long.toString(diffSeconds) + "s";

                                    MyCountDownTimer countDownTimer = new MyCountDownTimer(remainingTime, 1000);
                                    countDownTimer.start();
                                } else diffString = "Event completed";
                                startDate.setText(diffString);
                                progressDialog.hideProgress();
                                isFragmentShown = true;
                            }
                            else progressDialog.hideProgress();
                        }
                    });
                }
            }
        });
        return anim;
    }
}
