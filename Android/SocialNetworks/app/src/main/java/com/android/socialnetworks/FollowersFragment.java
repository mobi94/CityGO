package com.android.socialnetworks;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.astuetz.PagerSlidingTabStrip;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseRelation;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.pnikosis.materialishprogress.ProgressWheel;
import com.quickblox.chat.QBChatService;
import com.quickblox.chat.QBGroupChatManager;
import com.quickblox.chat.model.QBDialog;
import com.quickblox.core.QBEntityCallbackImpl;
import com.quickblox.core.request.QBRequestUpdateBuilder;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class FollowersFragment extends Fragment {

    private ArrayList<FollowersItem> followersItems;
    private RecyclerView recyclerView;
    private FollowersAdapter followersAdapter;
    private boolean isFragmentShown = false;
    private MyProgressDialog progressDialog;
    private int acceptedFollowersCount = 0;
    private int onPendingFollowersCount = 0;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.followers_list_fragment, container, false);
        setHasOptionsMenu(true);
        final ActionBar actionBar = ((ActionBarActivity) getActivity()).getSupportActionBar();
        if (actionBar != null) {
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
            String eventTitle = getArguments().getString("EVENT_TITLE", "");
            if (!eventTitle.equals("")) actionBar.setTitle("Followers of the event \"" + eventTitle + "\"");
            else actionBar.setTitle("Followers of the event");
        }
        progressDialog = new MyProgressDialog(getActivity());
        recyclerView = (RecyclerView) rootView.findViewById(R.id.followers_list);
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

            ParseQuery<ParseObject> query = ParseQuery.getQuery("GoEvents");
            query.getInBackground(getArguments().getString("MARKER_ID", ""), new GetCallback<ParseObject>() {
                @Override
                public void done(ParseObject parseObject, ParseException e) {
                    if (e == null) {
                        JSONArray usersRequest = parseObject.getJSONArray("usersRequest");
                        if (usersRequest.length() != 0) {
                            followersItems.add(new FollowersItem(true));
                            followersItems.get(followersItems.size() - 1).setIsAccepted(false);
                            onPendingFollowersCount = usersRequest.length();
                            for (int i = 0; i < usersRequest.length(); i++) {
                                try {
                                    String userName = usersRequest.getJSONObject(i).optString("username");
                                    String nickName = usersRequest.getJSONObject(i).optString("nickname");
                                    String avatarUrl = usersRequest.getJSONObject(i).optString("userAvatar");
                                    String userChatID = usersRequest.getJSONObject(i).optString("userChatID");
                                    followersItems.add(new FollowersItem(avatarUrl, nickName, userName, userChatID));
                                    followersItems.get(followersItems.size() - 1).setIsAccepted(false);
                                } catch (JSONException e1) {
                                    e1.printStackTrace();
                                }
                            }
                        }
                        JSONArray usersAccept = parseObject.getJSONArray("usersAccept");
                        if (usersAccept.length() != 0) {
                            followersItems.add(new FollowersItem(true));
                            followersItems.get(followersItems.size() - 1).setIsAccepted(true);
                            acceptedFollowersCount = usersAccept.length();
                            for (int i = 0; i < usersAccept.length(); i++) {
                                try {
                                    String userName = usersAccept.getJSONObject(i).optString("username");
                                    String nickName = usersAccept.getJSONObject(i).optString("nickname");
                                    String avatarUrl = usersAccept.getJSONObject(i).optString("userAvatar");
                                    String userChatID = usersAccept.getJSONObject(i).optString("userChatID");
                                    followersItems.add(new FollowersItem(avatarUrl, nickName, userName, userChatID));
                                    followersItems.get(followersItems.size() - 1).setIsAccepted(true);
                                } catch (JSONException e1) {
                                    e1.printStackTrace();
                                }
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
        followersItems = new ArrayList<>();
        setItems();
        followersAdapter = new FollowersAdapter(getActivity());
        recyclerView.setAdapter(followersAdapter);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager layoutManager
                = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.addItemDecoration(new DividerItemDecoration(ContextCompat.getDrawable(getActivity(), R.drawable.divider)));
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
        private String userChatID;
        private boolean isHeader = false;
        private boolean isAccepted = false;

        public FollowersItem(String avatarUrl, String nickName, String userName, String userChatID) {
            super();
            this.avatarUrl = avatarUrl;
            this.nickName = nickName;
            this.userName = userName;
            this.userChatID = userChatID;
        }

        public FollowersItem(boolean isHeader) {
            this.isHeader = isHeader;
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

        public String getUserChatID() {
            return userChatID;
        }

        public void setUserChatID(String userChatID) {
            this.userChatID = userChatID;
        }

        public boolean isHeader() {
            return isHeader;
        }

        public void setIsHeader(boolean isHeader) {
            this.isHeader = isHeader;
        }

        public boolean isAccepted() {
            return isAccepted;
        }

        public void setIsAccepted(boolean isAccepted) {
            this.isAccepted = isAccepted;
        }
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        ImageView avatar;
        TextView nickName;
        ImageButton accept;
        ImageButton decline;
        TextView separator;
        ProgressWheel progressWheel;

        public ViewHolder(View view, boolean isSeparator) {
            super(view);
            if (!isSeparator) {
                avatar = (ImageView) view.findViewById(R.id.followers_list_avatar);
                nickName = (TextView) view.findViewById(R.id.followers_list_name);
                accept = (ImageButton) view.findViewById(R.id.followers_list_accept);
                decline = (ImageButton) view.findViewById(R.id.followers_list_decline);
                progressWheel = (ProgressWheel) view.findViewById(R.id.progress_wheel_followers_list);
            }
            else separator = (TextView) view.findViewById(R.id.separator_title);
        }
    }

    public class FollowersAdapter extends RecyclerView.Adapter<ViewHolder> {
        private Activity context;

        public FollowersAdapter(Activity context) {
            this.context = context;
        }

        @Override
        public int getItemViewType(int position) {
            int vewType;
            if (followersItems.get(position).isHeader()) vewType = 0;
            else if (!followersItems.get(position).isAccepted()) vewType = 1;
            else vewType = 2;
            return vewType;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent , int viewType) {
            LayoutInflater mInflater = LayoutInflater.from(parent.getContext());
            View view;
            boolean isSeparator = false;
            switch (viewType) {
                case 0:
                    view = mInflater.inflate(R.layout.list_separator, parent, false);
                    isSeparator = true;
                    break;
                case 1:
                    view = mInflater.inflate(R.layout.followers_list_item, parent, false);
                    break;
                default:
                    view = mInflater.inflate(R.layout.followers_list_item, parent, false);
            }
            return new ViewHolder(view, isSeparator);
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, final int position) {
            if (holder.getItemViewType() == 0){
                if (!followersItems.get(position).isAccepted()) holder.separator.setText("On pending");
                else holder.separator.setText("Accepted");
            }
            else {
                Picasso.with(context)
                        .load(followersItems.get(position).getAvatarUrl())
                        .transform(MainActivity.transformation())
                        .resize(400, 400)
                        .centerCrop()
                        .into(holder.avatar, new com.squareup.picasso.Callback() {
                            @Override
                            public void onSuccess() {
                                holder.progressWheel.stopSpinning();
                            }
                            @Override
                            public void onError() {
                                holder.progressWheel.stopSpinning();
                            }
                        });
                holder.nickName.setText(followersItems.get(position).getNickName());
                if (holder.getItemViewType() == 1) {
                    holder.accept.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            actionWithFollower(followersItems.get(position), true);
                        }
                    });
                    holder.decline.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            actionWithFollower(followersItems.get(position), false);
                        }
                    });
                }
                else{
                    holder.accept.setVisibility(View.GONE);
                    holder.decline.setVisibility(View.GONE);
                }
            }
        }

        @Override
        public int getItemCount() {
            return followersItems.size();
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
                                newAcceptedFollower.put("userChatID", followersItem.getUserChatID());
                            } catch (JSONException e1) {
                                e1.printStackTrace();
                            }
                            parseObject.add("usersAccept", newAcceptedFollower);
                            updateChatDialog(parseObject, followersItem);
                        }
                        else {
                            int availableSeats = Integer.parseInt(parseObject.getString("avaibleSeats"));
                            availableSeats++;
                            parseObject.put("avaibleSeats", Integer.toString(availableSeats));
                        }

                        JSONArray requestedFollowers = parseObject.getJSONArray("usersRequest");
                        JSONArray updatedRequestedFollowers = new JSONArray();
                        for(int i=0; i<requestedFollowers.length(); i++) {
                            try {
                                String userName = requestedFollowers.getJSONObject(i).optString("username");
                                if (userName.equals(followersItem.getUserName())) {
                                    if (onPendingFollowersCount == 1) followersItems.remove(0);
                                    followersItems.remove(followersItem);
                                    onPendingFollowersCount--;
                                }
                                else updatedRequestedFollowers.put(requestedFollowers.getJSONObject(i));
                            } catch (JSONException e1) {
                                e1.printStackTrace();
                            }
                        }

                        if (accept) {
                            int acceptedFollowersHeaderIndex = onPendingFollowersCount + 1;
                            if (acceptedFollowersCount == 0) {
                                followersItems.add(acceptedFollowersHeaderIndex, new FollowersItem(true));
                                followersItems.get(acceptedFollowersHeaderIndex).setIsAccepted(true);
                                followersItems.add(acceptedFollowersHeaderIndex + 1, followersItem);
                                followersItems.get(acceptedFollowersHeaderIndex + 1).setIsAccepted(true);
                            } else {
                                followersItems.add(acceptedFollowersHeaderIndex, followersItem);
                                followersItems.get(acceptedFollowersHeaderIndex).setIsAccepted(true);
                            }
                            acceptedFollowersCount++;
                        }
                        followersAdapter.notifyDataSetChanged();
                        followersAdapter.notifyItemRangeChanged(0, followersItems.size());
                        if (followersItems.size() == 0) {
                            LinearLayout eventListEmpty = (LinearLayout) getActivity().findViewById(R.id.followers_fragment_empty);
                            LinearLayout eventListLoadingProgress = (LinearLayout) getActivity().findViewById(R.id.followers_fragment_loading_progress);
                            LinearLayout eventListNoNetwork = (LinearLayout) getActivity().findViewById(R.id.followers_fragment_no_network);
                            eventListNoNetwork.setVisibility(View.GONE);
                            eventListLoadingProgress.setVisibility(View.GONE);
                            eventListEmpty.setVisibility(View.VISIBLE);
                        }

                        parseObject.put("usersRequest", updatedRequestedFollowers);

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

    private void updateChatDialog(ParseObject parseObject, FollowersItem followersItem){
        String userChatId = followersItem.getUserChatID();
        QBRequestUpdateBuilder requestBuilder = new QBRequestUpdateBuilder();
        requestBuilder.push("occupants_ids", Integer.parseInt(userChatId));

        String dialogId = parseObject.getJSONObject("chatDialog").optString("dialogID");
        QBDialog dialog = new QBDialog(dialogId);
        QBChatService chatService;
        if (!QBChatService.isInitialized()) {
            QBChatService.init(getActivity());
        }
        chatService = QBChatService.getInstance();
        QBGroupChatManager groupChatManager = chatService.getGroupChatManager();
        groupChatManager.updateDialog(dialog, requestBuilder, new QBEntityCallbackImpl<QBDialog>() {
            @Override
            public void onSuccess(QBDialog dialog, Bundle args) {
            }

            @Override
            public void onError(List<String> errors) {
                Log.d("ACCEPTING_ERROR", errors.get(0));
                Toast.makeText(getActivity(), "ACCEPTING_ERROR: " + errors, Toast.LENGTH_LONG).show();
            }
        });
    }
}
