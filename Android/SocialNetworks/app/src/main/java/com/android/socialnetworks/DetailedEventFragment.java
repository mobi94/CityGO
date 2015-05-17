package com.android.socialnetworks;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.astuetz.PagerSlidingTabStrip;
import com.google.android.gms.maps.model.LatLng;
import com.parse.FunctionCallback;
import com.parse.GetCallback;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.pnikosis.materialishprogress.ProgressWheel;
import com.quickblox.auth.QBAuth;
import com.quickblox.auth.model.QBSession;
import com.quickblox.core.QBEntityCallbackImpl;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.users.model.QBUser;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class DetailedEventFragment extends Fragment {

    private boolean isFragmentShown;
    private ImageView avatar;
    private TextView creatorName;
    private TextView creatorAge;
    private TextView title;
    private TextView description;
    private TextView availableSeats;
    private TextView startDate;
    private Button goToChatButton;
    private Button goToDirectButton;
    private ActionBar actionBar;
    private MyProgressDialog progressDialog;
    private String objectId;
    private int seats;
    private JSONArray usersRequest;
    private JSONArray usersAccept;
    private JSONObject userDialogData;
    private LatLng eventLocation;
    private RecyclerView recyclerView;

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

        recyclerView = (RecyclerView) rootView.findViewById(R.id.recycler_view);

        goToChatButton = (Button) rootView.findViewById(R.id.detailed_chat_button);
        goToChatButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addToFollowers();
            }
        });
        goToDirectButton = (Button) rootView.findViewById(R.id.detailed_direct_button);
        goToDirectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToDirect();
            }
        });

        return rootView;
    }

    private void goToChat(){
        QBUser user = MainActivity.qbUser;
        if (user == null) Toast.makeText(getActivity(), "You're not logged into the chat.\nPress \"login to chat\" button", Toast.LENGTH_LONG).show();
        else {
            Intent intent = new Intent(getActivity(), DetailedDialogActivity.class);
            try {
                intent.putExtra("RoomJid", (String)userDialogData.get("roomJID"));
                intent.putExtra("DialogId",(String)userDialogData.get("dialogID"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
            intent.putExtra("UserNickName", user.getFullName());

            ParseUser parseUser = ParseUser.getCurrentUser();
            String creatorAvatarUrl = parseUser.getString("avatarURL");
            if (creatorAvatarUrl == null || creatorAvatarUrl.equals("")) {
                ParseFile photo = (ParseFile) parseUser.get("profilePic");
                creatorAvatarUrl = photo.getUrl();
            }
            intent.putExtra("UserAvatarUrl", creatorAvatarUrl);
            startActivity(intent);
        }
    }

    private void goToDirect(){
        if (!SignUpActivity.isNetworkOn(getActivity())) {
            Toast.makeText(getActivity(), getString(R.string.toast_no_network_connection), Toast.LENGTH_SHORT).show();
        } else {
            String latitude = Double.toString(eventLocation.latitude);
            String longtitude = Double.toString(eventLocation.longitude);
            Uri gmmIntentUri = Uri.parse("google.navigation:q=" + latitude + "," + longtitude + "&mode=w");
            Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
            mapIntent.setPackage("com.google.android.apps.maps");
            startActivity(mapIntent);
        }
    }

    private void addToFollowers(){
        if (!SignUpActivity.isNetworkOn(getActivity())) {
            Toast.makeText(getActivity(), getString(R.string.toast_no_network_connection), Toast.LENGTH_SHORT).show();
        } else {
            final ParseUser currentUser = ParseUser.getCurrentUser();
            if (isInJSONArray(usersAccept, currentUser)) {
                goToChat();
            } else {
                if (isInJSONArray(usersRequest, currentUser)) {
                    goToChatButton.setText("On pending...");
                    goToChatButton.setEnabled(false);
                } else {
                    if (seats <= 0){
                        goToChatButton.setEnabled(false);
                    }
                    else {
                        QBUser user = MainActivity.qbUser;
                        if (user == null) Toast.makeText(getActivity(), "You're not logged into the chat.\nPress \"login to chat\" button", Toast.LENGTH_LONG).show();
                        else {
                            progressDialog.showProgress("Loading...");
                            String userChatID = user.getId().toString();
                            sendFollowRequest(currentUser, userChatID);
                        }
                    }
                }
            }
        }
    }

    private void sendFollowRequest(ParseUser currentUser, String userChatID){
        String creatorAvatarUrl = currentUser.getString("avatarURL");
        if (creatorAvatarUrl == null || creatorAvatarUrl.equals("")) {
            ParseFile photo = (ParseFile) currentUser.get("profilePic");
            creatorAvatarUrl = photo.getUrl();
        }
        JSONObject userRequestInfo = new JSONObject();
        try {
            userRequestInfo.put("nickname", currentUser.getString("nickname"));
            userRequestInfo.put("userAvatar", creatorAvatarUrl);
            userRequestInfo.put("username", currentUser.getString("username"));
            userRequestInfo.put("userChatID", userChatID);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        HashMap<String, Object> params = new HashMap<>();
        params.put("objectID", objectId);
        params.put("userRequestInfo", userRequestInfo);
        ParseCloud.callFunctionInBackground("sendFollowRequest", params, new FunctionCallback<Object>() {
            @Override
            public void done(Object o, ParseException e) {
                if (e != null) {
                    Toast.makeText(getActivity(), "FOLLOW_REQUEST_ERROR" + e, Toast.LENGTH_SHORT).show();
                    progressDialog.hideProgress();
                } else {
                    Toast.makeText(getActivity(), "Request was sent. Wait for confirmation", Toast.LENGTH_SHORT).show();
                    ParseQuery<ParseObject> query = ParseQuery.getQuery("GoEvents");
                    query.getInBackground(objectId, new GetCallback<ParseObject>() {
                        @Override
                        public void done(ParseObject parseObject, ParseException e) {
                            if (e == null) {
                                seats = Integer.parseInt(parseObject.getString("avaibleSeats"));
                                if (seats <= 0)
                                    availableSeats.setText("No available seats");
                                else
                                    availableSeats.setText("Available seats: " + seats);
                                goToChatButton.setText("On pending...");
                                goToChatButton.setEnabled(false);
                            } else
                                Toast.makeText(getActivity(), "FOLLOW_REQUEST_ERROR" + e, Toast.LENGTH_SHORT).show();
                            progressDialog.hideProgress();
                        }
                    });
                }
            }
        });
    }

    private boolean isInJSONArray(JSONArray jsonArray, ParseUser currentUser){
        boolean isInJSON = false;
        String userName;
        for(int i=0; i<jsonArray.length(); i++) {
            try {
                userName = jsonArray.getJSONObject(i).optString("username");
                if (currentUser.getUsername().equals(userName))
                    isInJSON = true;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return isInJSON;
    }

    private int getMarkerIcon(String categoryIcon){
        switch(categoryIcon){
            case "0": return R.drawable.detailed_love;
            case "1": return R.drawable.detailed_movie;
            case "2": return R.drawable.detailed_sport;
            case "3": return R.drawable.detailed_business;
            case "4": return R.drawable.detailed_coffee;
            case "5": return R.drawable.detailed_meet;
            default: return R.drawable.detailed_meet;
        }
    }

    private int getEventColor(String categoryIcon){
        switch(categoryIcon){
            case "0": return 0xff9c0307;
            case "1": return 0xff000000;
            case "2": return 0xff718d48;
            case "3": return 0xffe3a92b;
            case "4": return 0xff663a2e;
            case "5": return 0xff496fb2;
            default: return 0xff009A90;
        }
    }

    private int getEventDrawable(String categoryIcon){
        switch(categoryIcon){
            case "0": return R.drawable.background_button_love;
            case "1": return R.drawable.background_button_movie;
            case "2": return R.drawable.background_button_sport;
            case "3": return R.drawable.background_button_business;
            case "4": return R.drawable.background_button_coffee;
            case "5": return R.drawable.background_button_meet;
            default: return R.drawable.background_niagara;
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
                    MainActivity.disableViewPager((MyViewPager) getActivity().findViewById(R.id.pager),
                            (PagerSlidingTabStrip) getActivity().findViewById(R.id.tabs));
                    getEventData();
                }
            }
        });
        return anim;
    }

    private void getEventData(){
        progressDialog.showProgress("Loading...");
        ParseQuery<ParseObject> query = ParseQuery.getQuery("GoEvents");
        query.getInBackground(getArguments().getString("MARKER_ID", ""), new GetCallback<ParseObject>() {
            @Override
            public void done(ParseObject parseObject, ParseException e) {
                if (e == null) {
                    final ProgressWheel wheel = (ProgressWheel) getActivity().findViewById(R.id.progress_wheel_detailed);
                    wheel.spin();

                    String category = parseObject.getString("category");
                    if (actionBar != null) {
                        actionBar.setBackgroundDrawable(new ColorDrawable(getEventColor(category)));
                    }
                    RelativeLayout currentLayout = (RelativeLayout) getActivity().findViewById(R.id.detailed_fragment);
                    currentLayout.setBackgroundResource(getMarkerIcon(parseObject.getString("category")));

                    goToChatButton.setBackgroundResource(getEventDrawable(category));
                    goToDirectButton.setBackgroundResource(getEventDrawable(category));

                    objectId = parseObject.getObjectId();
                    userDialogData = parseObject.getJSONObject("chatDialog");
                    usersRequest = parseObject.getJSONArray("usersRequest");
                    usersAccept = parseObject.getJSONArray("usersAccept");
                    if (isInJSONArray(usersAccept, ParseUser.getCurrentUser())) goToChatButton.setText("Go to chat");
                    if (isInJSONArray(usersRequest, ParseUser.getCurrentUser())) goToChatButton.setText("On pending...");
                    //if (usersAccept.length() > 0){
                        ListAdapterHolder adapter = new ListAdapterHolder(getActivity());
                        recyclerView.setAdapter(adapter);
                        recyclerView.setHasFixedSize(true);
                        LinearLayoutManager layoutManager
                                = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);
                        recyclerView.setLayoutManager(layoutManager);
                        recyclerView.setItemAnimator(new DefaultItemAnimator());
                    //}

                    eventLocation = new LatLng(parseObject.getParseGeoPoint("location").getLatitude(),
                            parseObject.getParseGeoPoint("location").getLongitude());

                    String creatorAvatarUrl = parseObject.getString("creatorAvatarUrl");
                    if (creatorAvatarUrl != null && !creatorAvatarUrl.equals("")) {
                        Picasso.with(getActivity())
                                .load(creatorAvatarUrl)
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

                    seats = Integer.parseInt(parseObject.getString("avaibleSeats"));
                    if (seats <= 0) availableSeats.setText("No available seats");
                    else availableSeats.setText("Available seats: " + seats);

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

    public class ListAdapterHolder extends RecyclerView.Adapter<ListAdapterHolder.ViewHolder> {

        private final Context context;
        private ArrayList<String> followersAvatars = new ArrayList<>();

        public ListAdapterHolder(Context context) {
            this.context = context;
            getFollowersAvatars();
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent , int viewType) {
            final LayoutInflater mInflater = LayoutInflater.from(parent.getContext());
            final View sView = mInflater.inflate(R.layout.detailed_followers_item, parent, false);
            return new ViewHolder(sView);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder , int position) {
            String avatarUrl = followersAvatars.get(position);
            if (avatarUrl != null && !avatarUrl.equals(""))
                Picasso.with(context)
                        .load(avatarUrl)
                        .transform(MainActivity.transformation())
                        .resize(400, 400)
                        .centerCrop()
                        .into(holder.avatar);
        }

        @Override
        public int getItemCount() {
            return followersAvatars.size();
        }

        private void getFollowersAvatars(){
            for(int i=0; i<usersAccept.length(); i++) {
                try {
                    followersAvatars.add(usersAccept.getJSONObject(i).optString("userAvatar"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }

        public class ViewHolder extends RecyclerView.ViewHolder{
            ImageView avatar;
            public ViewHolder(View view) {
                super(view);
                avatar = (ImageView) view.findViewById(R.id.detailed_followers_avatar);
            }
        }
    }
}
