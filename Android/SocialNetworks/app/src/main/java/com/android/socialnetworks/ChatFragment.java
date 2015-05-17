package com.android.socialnetworks;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.ParseFile;
import com.parse.ParseUser;
import com.quickblox.chat.QBChatService;
import com.quickblox.chat.model.QBDialog;
import com.quickblox.chat.model.QBDialogType;
import com.quickblox.core.QBEntityCallbackImpl;
import com.quickblox.core.request.QBRequestGetBuilder;
import com.quickblox.users.model.QBUser;

import java.util.ArrayList;
import java.util.List;

public class ChatFragment extends Fragment {

    private static final String ARG_POSITION = "position";
    private boolean isFirstTime;
    private boolean isVisible;
    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ArrayList<ChatDialog> chatDialogs = new ArrayList<>();
    private ListAdapterHolder adapter;
    static public boolean needToUpdateDialogs = false;


    public static ChatFragment newInstance(int position) {
        ChatFragment f = new ChatFragment();
        Bundle b = new Bundle();
        b.putInt(ARG_POSITION, position);
        f.setArguments(b);
        return f;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.chat_fragment, container, false);
        isFirstTime = true;
        recyclerView = (RecyclerView) rootView.findViewById(R.id.chat_fragment_list);
        swipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.chat_swipeRefreshLayout);
        swipeRefreshLayout.setEnabled(false);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                updateDialogsList();
            }
        });
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (isVisible && needToUpdateDialogs && !isFirstTime) {
            updateDialogsList();
            needToUpdateDialogs = false;
        }
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        isVisible = isVisibleToUser;
        if (isVisibleToUser) {
            if (isFirstTime) {
                getDialogs();
                isFirstTime = false;
            }
            else if(needToUpdateDialogs) {
                updateDialogsList();
                needToUpdateDialogs = false;
            }
        }
    }

    private void disableAllViews(){
        LinearLayout layout = (LinearLayout) getActivity().findViewById(R.id.chat_fragment);
        for (int i = 0; i < layout.getChildCount(); i++) {
            View child = layout.getChildAt(i);
            child.setEnabled(false);
        }
    }

    private void enableAllViews(){
        LinearLayout layout = (LinearLayout) getActivity().findViewById(R.id.chat_fragment);
        for (int i = 0; i < layout.getChildCount(); i++) {
            View child = layout.getChildAt(i);
            child.setEnabled(true);
        }
    }

    private void getDialogs(){
        adapter = new ListAdapterHolder(getActivity());
        recyclerView.setAdapter(adapter);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager layoutManager
                = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
    }

    private void updateDialogsList(){
        chatDialogs = new ArrayList<>();
        adapter.getChatDialogs();
    }

    public class ListAdapterHolder extends RecyclerView.Adapter<ListAdapterHolder.ViewHolder> {

        private final Context context;

        public ListAdapterHolder(Context context) {
            this.context = context;
            getChatDialogs();
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent , int viewType) {
            LayoutInflater mInflater = LayoutInflater.from(parent.getContext());
            View view = mInflater.inflate(R.layout.chat_list_item, parent, false);
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(SignUpActivity.isNetworkOn(context)) {
                        int itemPosition = recyclerView.getChildAdapterPosition(v);
                        Intent intent = new Intent(context, DetailedDialogActivity.class);
                        intent.putExtra("RoomJid", chatDialogs.get(itemPosition).getRoomJid());
                        intent.putExtra("DialogId", chatDialogs.get(itemPosition).getDialogId());
                        intent.putExtra("UserNickName", MainActivity.qbUser.getFullName());
                        intent.putExtra("UnreadCount", chatDialogs.get(itemPosition).getUnread());

                        ParseUser parseUser = ParseUser.getCurrentUser();
                        String creatorAvatarUrl = parseUser.getString("avatarURL");
                        if (creatorAvatarUrl == null || creatorAvatarUrl.equals("")) {
                            ParseFile photo = (ParseFile) parseUser.get("profilePic");
                            creatorAvatarUrl = photo.getUrl();
                        }
                        intent.putExtra("UserAvatarUrl", creatorAvatarUrl);
                        startActivity(intent);
                        /*getFragmentManager().beginTransaction()
                            .setCustomAnimations(
                                    R.anim.slide_in_right, R.anim.slide_out_right,
                                    R.anim.slide_in_right, R.anim.slide_out_right)
                            .add(R.id.container, new DetailedDialogActivity())
                            .addToBackStack(null)
                            .commit();*/
                    }
                    else Toast.makeText(context, "Please, check your internet connection", Toast.LENGTH_SHORT).show();
                }
            });
            return new ViewHolder(view);
        }

        public int getDialogIcon(int categoryIcon){
            switch(categoryIcon) {
                case 0: return R.drawable.event_love;
                case 1: return R.drawable.event_movie;
                case 2: return R.drawable.event_sport;
                case 3: return R.drawable.event_business;
                case 4: return R.drawable.event_coffee;
                case 5: return R.drawable.event_meet;
                default: return R.drawable.event_meet;
            }
        }

        @Override
        public void onBindViewHolder(ViewHolder holder , int position) {
            holder.dialog_type.setImageResource(getDialogIcon(Integer.parseInt(chatDialogs.get(position).dialogType)));
            holder.title.setText(chatDialogs.get(position).getTitle());
            if (chatDialogs.get(position).getLastMessage() != null) {
                holder.lastMessage.setText("Last message: " + chatDialogs.get(position).getLastMessage());
                holder.pastTime.setText(getPastTime(chatDialogs.get(position).getPastTime()) + " ago");
            }else {
                holder.lastMessage.setText("No messages");
                holder.pastTime.setText("");
            }
            if (Integer.parseInt(chatDialogs.get(position).getUnread()) != 0) {
                holder.unread.setVisibility(View.VISIBLE);
                holder.unread.setText(chatDialogs.get(position).getUnread());
            }
            else holder.unread.setVisibility(View.GONE);
        }

        public String getPastTime(long secondsPast){
            String pastTime;
            long millisPast = System.currentTimeMillis() - secondsPast * 1000;
            long diffSeconds = millisPast / 1000 % 60;
            long diffMinutes = millisPast / (60 * 1000) % 60;
            long diffHours = millisPast / (60 * 60 * 1000) % 24;
            long diffDays = millisPast / (24 * 60 * 60 * 1000);
            if (diffDays == 0 && diffHours != 0 && diffMinutes != 0) pastTime = Long.toString(diffHours) +
                    "h " + Long.toString(diffMinutes) + "m " + Long.toString(diffSeconds) + "s";
            else if (diffDays == 0 && diffHours == 0 && diffMinutes != 0) pastTime = Long.toString(diffMinutes) + "m "
                    + Long.toString(diffSeconds) + "s";
            else if (diffDays == 0 && diffHours == 0) pastTime = Long.toString(diffSeconds) + "s";
            else pastTime = Long.toString(diffDays) + "d " + Long.toString(diffHours) +
                        "h " + Long.toString(diffMinutes) + "m " + Long.toString(diffSeconds) + "s";
            return pastTime;
        }

        @Override
        public int getItemCount() {
            return chatDialogs.size();
        }

        private void getChatDialogs(){
            final LinearLayout dialogListEmpty = (LinearLayout) getActivity().findViewById(R.id.chat_fragment_empty);
            final LinearLayout dialogListLoadingProgress = (LinearLayout) getActivity().findViewById(R.id.chat_fragment_loading_progress);
            final LinearLayout dialogListNoNetwork = (LinearLayout) getActivity().findViewById(R.id.chat_fragment_no_network);
            if(!SignUpActivity.isNetworkOn(context)) {
                if (chatDialogs.size() == 0) {
                    dialogListEmpty.setVisibility(View.GONE);
                    dialogListLoadingProgress.setVisibility(View.GONE);
                    dialogListNoNetwork.setVisibility(View.VISIBLE);
                }
                else Toast.makeText(context, "Please, check your internet connection", Toast.LENGTH_SHORT).show();
                swipeRefreshLayout.setEnabled(true);
            } else {
                QBUser user = MainActivity.qbUser;
                if (user == null) {
                    Toast.makeText(getActivity(), "You're not logged into the chat.\nPress \"login to chat\" button", Toast.LENGTH_LONG).show();
                    swipeRefreshLayout.setEnabled(true);
                    swipeRefreshLayout.setRefreshing(false);
                }
                else {
                    dialogListEmpty.setVisibility(View.GONE);
                    dialogListNoNetwork.setVisibility(View.GONE);
                    dialogListLoadingProgress.setVisibility(View.VISIBLE);

                    QBRequestGetBuilder requestBuilder = new QBRequestGetBuilder();

                    int userId = user.getId();
                    requestBuilder.in("occupants_ids", userId);

                    QBChatService.getChatDialogs(QBDialogType.GROUP, requestBuilder, new QBEntityCallbackImpl<ArrayList<QBDialog>>() {
                        @Override
                        public void onSuccess(ArrayList<QBDialog> dialogs, Bundle args) {
                            swipeRefreshLayout.setEnabled(true);
                            swipeRefreshLayout.setRefreshing(false);
                            if (dialogs.size() == 0) {
                                dialogListNoNetwork.setVisibility(View.GONE);
                                dialogListLoadingProgress.setVisibility(View.GONE);
                                dialogListEmpty.setVisibility(View.VISIBLE);
                            } else {
                                for(QBDialog qbDialog: dialogs) {
                                    chatDialogs.add(new ChatDialog(qbDialog.getPhoto(), qbDialog.getName(),
                                            qbDialog.getLastMessage(), qbDialog.getLastMessageDateSent(),
                                            Integer.toString(qbDialog.getUnreadMessageCount()), qbDialog.getRoomJid(),
                                            qbDialog.getDialogId()));
                                }
                                adapter.notifyDataSetChanged();
                                dialogListLoadingProgress.setVisibility(View.GONE);
                            }
                        }

                        @Override
                        public void onError(List<String> errors) {
                            swipeRefreshLayout.setEnabled(true);
                            swipeRefreshLayout.setRefreshing(false);
                            dialogListLoadingProgress.setVisibility(View.GONE);
                            Toast.makeText(context, "UPDATE_DIALOG_LIST_ERROR: " + errors, Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }
        }

        public class ViewHolder extends RecyclerView.ViewHolder{
            ImageView dialog_type;
            TextView title;
            TextView lastMessage;
            TextView pastTime;
            TextView unread;
            public ViewHolder(View view) {
                super(view);
                dialog_type = (ImageView) view.findViewById(R.id.chat_dialog_type);
                title = (TextView) view.findViewById(R.id.chat_dialog_title);
                lastMessage = (TextView) view.findViewById(R.id.chat_dialog_last_messaage);
                pastTime = (TextView) view.findViewById(R.id.chat_dialog_past_time);
                unread = (TextView) view.findViewById(R.id.chat_dialog_unread);
            }
        }
    }

    public class ChatDialog {
        private String dialogType;
        private String title;
        private String lastMessage;
        private long pastTime;
        private String unread;
        private String roomJid;
        private String dialogId;

        public ChatDialog(String dialogType, String title, String lastMessage, long pastTime,
                          String unread, String roomJid, String dialogId) {
            super();
            this.dialogType = dialogType;
            this.title = title;
            this.lastMessage = lastMessage;
            this.pastTime = pastTime;
            this.unread = unread;
            this.roomJid = roomJid;
            this.dialogId = dialogId;
        }

        public String getDialogType() {
            return dialogType;
        }

        public String getTitle() {
            return title;
        }

        public String getLastMessage() {
            return lastMessage;
        }

        public long getPastTime() {
            return pastTime;
        }

        public String getUnread() {
            return unread;
        }

        public String getRoomJid() {
            return roomJid;
        }

        public String getDialogId() {
            return dialogId;
        }

        public void setDialogType(String dialogType) {
            this.dialogType = dialogType;
        }

        public void setPastTime(long pastTime) {
            this.pastTime = pastTime;
        }

        public void setLastMessage(String lastMessage) {
            this.lastMessage = lastMessage;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public void setUnread(String unread) {
            this.unread = unread;
        }

        public void setRoomJid(String roomJid) {
            this.roomJid = roomJid;
        }

        public void setDialogId(String dialogId) {
            this.dialogId = dialogId;
        }
    }
}