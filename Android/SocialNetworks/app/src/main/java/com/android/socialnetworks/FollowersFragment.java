package com.android.socialnetworks;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.astuetz.PagerSlidingTabStrip;
import com.leocardz.aelv.library.AelvListItem;
import com.leocardz.aelv.library.AelvListViewHolder;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseRelation;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.squareup.picasso.Picasso;
import com.twotoasters.jazzylistview.JazzyHelper;
import com.twotoasters.jazzylistview.JazzyListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class FollowersFragment extends Fragment {

    private ArrayList<FollowersItem> followersItems;
    private FollowersAdapter followersAdapter;
    private boolean isFragmentShown = false;
    private MyProgressDialog progressDialog;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.followers_list_fragment, container, false);
        setHasOptionsMenu(true);
        final ActionBar actionBar = ((ActionBarActivity) getActivity()).getSupportActionBar();
        if (actionBar != null) {
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle("Followers on pending");
        }
        progressDialog = new MyProgressDialog(getActivity());
        return rootView;
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

    private void setItems(){
        final LinearLayout eventListEmpty = (LinearLayout) getActivity().findViewById(R.id.followers_fragment_empty);
        final LinearLayout eventListLoadingProgress = (LinearLayout) getActivity().findViewById(R.id.followers_fragment_loading_progress);
        LinearLayout eventListNoNetwork = (LinearLayout) getActivity().findViewById(R.id.followers_fragment_no_network);
        if(!SignUpActivity.isNetworkOn(getActivity())) {
            if (eventListEmpty.getVisibility() == View.VISIBLE)
                eventListEmpty.setVisibility(View.GONE);
            if (eventListLoadingProgress.getVisibility() == View.VISIBLE)
                eventListLoadingProgress.setVisibility(View.GONE);
            eventListNoNetwork.setVisibility(View.VISIBLE);
        } else {
            if (eventListEmpty.getVisibility() == View.VISIBLE)
                eventListEmpty.setVisibility(View.GONE);
            if (eventListNoNetwork.getVisibility() == View.VISIBLE)
                eventListNoNetwork.setVisibility(View.GONE);
            eventListLoadingProgress.setVisibility(View.VISIBLE);

            followersItems = new ArrayList<>();
            ParseQuery<ParseObject> query = ParseQuery.getQuery("GoEvents");
            query.getInBackground(getArguments().getString("MARKER_ID", ""), new GetCallback<ParseObject>() {
                @Override
                public void done(ParseObject parseObject, ParseException e) {
                    if (e == null) {
                        JSONArray usersRequest = parseObject.getJSONArray("usersRequest");
                        for (int i = 0; i < usersRequest.length(); i++) {
                            try {
                                String userName = usersRequest.getJSONObject(i).optString("username");
                                String nickName = usersRequest.getJSONObject(i).optString("nickname");
                                String avatarUrl = usersRequest.getJSONObject(i).optString("userAvatar");
                                followersItems.add(new FollowersItem(avatarUrl, nickName, userName));
                            } catch (JSONException e1) {
                                e1.printStackTrace();
                            }
                        }
                        if(followersItems.size() == 0) {
                            if (eventListLoadingProgress.getVisibility() == View.VISIBLE)
                                eventListLoadingProgress.setVisibility(View.GONE);
                            eventListEmpty.setVisibility(View.VISIBLE);
                        }
                        else {
                            followersAdapter.notifyDataSetChanged();
                            eventListLoadingProgress.setVisibility(View.GONE);
                        }
                    }
                    else Toast.makeText(getActivity(), "UPDATE_FOLLOWERS_LIST_ERROR: " + e, Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    private void generateFollowersList(){
        JazzyListView listView = (JazzyListView) getActivity().findViewById(R.id.followers_list);
        listView.setTransitionEffect(JazzyHelper.TILT);

        setItems();
        followersAdapter = new FollowersAdapter(getActivity(), R.layout.followers_list_item, followersItems);
        listView.setAdapter(followersAdapter);
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
                    generateFollowersList();
                    isFragmentShown = true;
                }
            }
        });
        return anim;
    }

    public class FollowersItem {
        private String avatarUrl;
        private String nickName;
        private String userName;

        public FollowersItem(String avatarUrl, String nickName, String userName) {
            super();
            this.avatarUrl = avatarUrl;
            this.nickName = nickName;
            this.userName = userName;
        }

        public String getAvatarUrl() {
            return avatarUrl;
        }

        public void setAvatarUrl(String avatarUrl) {
            this.avatarUrl = avatarUrl;
        }

        public String getNickName() {
            return nickName;
        }

        public void setNickName(String nickName) {
            this.nickName = nickName;
        }

        public String getUserName() {
            return userName;
        }

        public void setUserName(String userName) {
            this.userName = userName;
        }
    }

    public class ViewHolder {
        private ImageView avatar;
        private TextView nickName;
        private ImageButton accept;
        private ImageButton decline;

        public ViewHolder(ImageView avatar, TextView nickName, ImageButton accept, ImageButton decline) {
            super();
            this.avatar = avatar;
            this.nickName = nickName;
            this.accept = accept;
            this.decline = decline;
        }
    }

    public class FollowersAdapter extends ArrayAdapter<FollowersItem> {
        private Activity context;

        public FollowersAdapter(Activity context, int id, ArrayList<FollowersItem> followersItems) {
            super(context, id, followersItems);
            this.context = context;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final ViewHolder holder;
            final FollowersItem followersItem = followersItems.get(position);
            if (convertView == null) {
                LayoutInflater inflater = context.getLayoutInflater();
                convertView = inflater.inflate(R.layout.followers_list_item, null);

                ImageView avatar = (ImageView) convertView.findViewById(R.id.followers_list_avatar);
                TextView nickName = (TextView) convertView.findViewById(R.id.followers_list_name);
                ImageButton accept = (ImageButton) convertView.findViewById(R.id.followers_list_accept);
                ImageButton decline = (ImageButton) convertView.findViewById(R.id.followers_list_decline);
                holder = new ViewHolder(avatar, nickName, accept, decline);
            }
            else holder = (ViewHolder) convertView.getTag();

            Picasso.with(context)
                    .load(followersItem.getAvatarUrl())
                    .transform(MainActivity.transformation())
                    .resize(400, 400)
                    .centerCrop()
                    .into(holder.avatar);
            holder.nickName.setText(followersItem.getNickName());
            holder.accept.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    actionWithFollower(followersItem, true);
                }
            });
            holder.decline.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    actionWithFollower(followersItem, false);
                }
            });
            convertView.setTag(holder);
            return convertView;
        }
    }

    private void actionWithFollower(final FollowersItem followersItem, final boolean accept) {
        progressDialog.showProgress("Loading...");
        final ParseUser currentUser = ParseUser.getCurrentUser();
        if (currentUser != null) {
            ParseRelation<ParseObject> relation = currentUser.getRelation("goEvent");
            relation.getQuery().getInBackground(getArguments().getString("MARKER_ID", ""), new GetCallback<ParseObject>() {
                @Override
                public void done(final ParseObject parseObject, ParseException e) {
                    if (e == null) {
                        if (accept) {
                            JSONObject newAcceptedFollower = new JSONObject();
                            try {
                                newAcceptedFollower.put("nickname", followersItem.getNickName());
                                newAcceptedFollower.put("userAvatar", followersItem.getAvatarUrl());
                                newAcceptedFollower.put("username", followersItem.getUserName());
                            } catch (JSONException e1) {
                                e1.printStackTrace();
                            }
                            parseObject.add("usersAccept", newAcceptedFollower);
                        }

                        int availableSeats = Integer.parseInt(parseObject.getString("avaibleSeats"));
                        availableSeats++;
                        parseObject.put("avaibleSeats", Integer.toString(availableSeats));

                        JSONArray requestedFollowers = parseObject.getJSONArray("usersRequest");
                        JSONArray updatedRequestedFollowers = new JSONArray();
                        for(int i=0; i<requestedFollowers.length(); i++) {
                            try {
                                String userName = requestedFollowers.getJSONObject(i).optString("username");
                                if (userName.equals(followersItem.getUserName())) {
                                    followersItems.remove(followersItem);
                                }
                                else updatedRequestedFollowers.put(requestedFollowers.getJSONObject(i));
                            } catch (JSONException e1) {
                                e1.printStackTrace();
                            }
                        }
                        parseObject.put("usersRequest", updatedRequestedFollowers);
                        followersAdapter.notifyDataSetChanged();
                        parseObject.saveInBackground(new SaveCallback() {
                            @Override
                            public void done(ParseException e) {
                                if (e != null) {
                                    String err;
                                    if (accept) err = "ACCEPTING_ERROR: ";
                                    else err = "DECLINING_ERROR: ";
                                    Toast.makeText(getActivity(), err + e, Toast.LENGTH_LONG).show();
                                }
                                else {
                                    MainActivity.markersIdsToUpdate.add(parseObject.getObjectId());
                                    MainActivity.setOnEventListToUpdateFlag();
                                }
                                progressDialog.hideProgress();
                            }
                        });
                    }
                    else {
                        String err;
                        if (accept) err = "ACCEPTING_ERROR: ";
                        else err = "DECLINING_ERROR: ";
                        Toast.makeText(getActivity(), err + e, Toast.LENGTH_LONG).show();
                        progressDialog.hideProgress();
                    }
                }
            });
        }
    }
}
