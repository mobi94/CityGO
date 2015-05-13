package com.android.socialnetworks;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.pnikosis.materialishprogress.ProgressWheel;
import com.quickblox.chat.QBChatService;
import com.quickblox.chat.QBGroupChat;
import com.quickblox.chat.QBGroupChatManager;
import com.quickblox.chat.exception.QBChatException;
import com.quickblox.chat.listeners.QBMessageListener;
import com.quickblox.chat.model.QBChatMessage;
import com.quickblox.chat.model.QBDialog;
import com.quickblox.core.QBEntityCallbackImpl;
import com.quickblox.core.request.QBRequestGetBuilder;
import com.squareup.picasso.Picasso;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smackx.muc.DiscussionHistory;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class DetailedDialogActivity extends ActionBarActivity {

    private RecyclerView recyclerView;
    private ListAdapterHolder adapter;
    private EditText chatText;
    private String roomJid;
    private String dialogId;
    private String userNickName;
    private int userId;
    private String userAvatarUrl;
    private QBGroupChat currentChatRoom;
    private ArrayList<DialogMessage> dialogMessages = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.detailed_dialog_activity);
        Toolbar toolbar = (Toolbar) findViewById(R.id.dialog_tool_bar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
            actionBar.setTitle("Chat dialog");
        }
        /*Parse.initialize(this, "OZYscvQi1cKHCP0vo6hPbbGAPvWs6M6vuMvrMRHi", "CTt2KMlNavNfboR5Tt0f8bA0I0h38ZgCFPtE6I5s");
        QBSettings.getInstance().fastConfigInit("21742", "OHjwDjYZG58QChy", "7-QuGgDaATdY8fR");*/

        userNickName = MainActivity.qbUser.getFullName();
        userId = MainActivity.qbUser.getId();
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            roomJid = extras.getString("RoomJid");
            dialogId = extras.getString("DialogId");
            //userNickName = extras.getString("UserNickName");
            userAvatarUrl = extras.getString("UserAvatarUrl");
        }
        joinDialog();

        recyclerView = (RecyclerView) findViewById(R.id.dialog_messages_list);
        adapter = new ListAdapterHolder(this);
        recyclerView.setAdapter(adapter);
        LinearLayoutManager layoutManager
                = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        ImageButton buttonSend = (ImageButton) findViewById(R.id.buttonSend);
        chatText = (EditText) findViewById(R.id.chatText);
        buttonSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                sendChatMessage();
            }
        });
    }

    private void sendChatMessage() {
        if(SignUpActivity.isNetworkOn(this)) {
            String text = chatText.getText().toString().trim();
            if (!text.equals("")) {
                QBChatMessage chatMessage = new QBChatMessage();
                chatMessage.setBody(text);
                chatMessage.setProperty("nickname", userNickName);
                chatMessage.setProperty("avatarUrl", userAvatarUrl);
                chatMessage.setProperty("save_to_history", "1");
                try {
                    currentChatRoom.sendMessage(chatMessage);
                    addHeaderIfNeeded(dialogMessages.get(dialogMessages.size() - 1).getTime(),
                            System.currentTimeMillis() / 1000);
                    dialogMessages.add(new DialogMessage(userAvatarUrl, userNickName, text,
                            System.currentTimeMillis() / 1000, userId));
                    adapter.notifyDataSetChanged();
                    chatText.setText("");
                    hideKeyboard();
                    recyclerView.scrollToPosition(dialogMessages.size() - 1);
                    ChatFragment.isAnyMessageSent = true;
                } catch (XMPPException | SmackException.NotConnectedException | IllegalStateException e) {
                    Toast.makeText(this, "Send message error:" + e, Toast.LENGTH_LONG).show();
                }
            }
        }
        else Toast.makeText(this, "Please, check your internet connection", Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void hideKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager inputManager = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
            inputManager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    private void addHeaderIfNeeded(long currentSeconds, long nextSeconds){
        int lastIndex = dialogMessages.size() - 1;
        Calendar c1 = Calendar.getInstance();
        c1.setTimeInMillis(currentSeconds * 1000);
        Calendar c2 = Calendar.getInstance();
        c2.setTimeInMillis(nextSeconds * 1000);
        if (c1.get(Calendar.DAY_OF_MONTH) != c2.get(Calendar.DAY_OF_MONTH)) {
            dialogMessages.add(new DialogMessage(nextSeconds));
            dialogMessages.get(lastIndex + 1).setIsHeader(true);
        }
    }

    private void joinDialog(){
        final QBMessageListener<QBGroupChat> groupChatQBMessageListener = new QBMessageListener<QBGroupChat>() {
            @Override
            public void processMessage(final QBGroupChat groupChat, final QBChatMessage chatMessage) {
                if (chatMessage.getSenderId() != userId) {
                    addHeaderIfNeeded(dialogMessages.get(dialogMessages.size() - 1).getTime(), chatMessage.getDateSent());
                    dialogMessages.add(new DialogMessage((String) chatMessage.getProperty("avatarUrl"),
                            (String) chatMessage.getProperty("nickname"),
                            chatMessage.getBody(), chatMessage.getDateSent(), chatMessage.getSenderId()));
                    recyclerView.scrollToPosition(dialogMessages.size() - 1);
                    LinearLayout dialogListEmpty = (LinearLayout) findViewById(R.id.dialog_empty);
                    dialogListEmpty.setVisibility(View.GONE);
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void processError(final QBGroupChat groupChat, QBChatException error, QBChatMessage originMessage){
                Toast.makeText(DetailedDialogActivity.this, "Receive message error: " + error, Toast.LENGTH_LONG).show();
            }

            @Override
            public void processMessageDelivered(QBGroupChat groupChat, String messageID){
                // never be called, works only for 1-1 chat
            }

            @Override
            public void processMessageRead(QBGroupChat groupChat, String messageID){
                // never be called, works only for 1-1 chat
            }
        };

        DiscussionHistory history = new DiscussionHistory();
        history.setMaxStanzas(0);

        QBChatService chatService;
        if (!QBChatService.isInitialized()) {
            QBChatService.init(this);
        }
        chatService = QBChatService.getInstance();
        QBGroupChatManager groupChatManager = chatService.getGroupChatManager();

        currentChatRoom = groupChatManager.createGroupChat(roomJid);
        currentChatRoom.join(history, new QBEntityCallbackImpl() {
            @Override
            public void onSuccess() {
                // add listeners
                currentChatRoom.addMessageListener(groupChatQBMessageListener);
            }

            @Override
            public void onError(final List errors) {
                Toast.makeText(DetailedDialogActivity.this, "Send message error: " + errors.get(0), Toast.LENGTH_LONG).show();
            }
        });
    }

    public class ListAdapterHolder extends RecyclerView.Adapter<ListAdapterHolder.ViewHolder> {

        private final Context context;

        public ListAdapterHolder(Context context) {
            this.context = context;
            loadHistory();
        }

        @Override
        public int getItemViewType(int position) {
            int vewType;
            if (!dialogMessages.get(position).isHeader()) {
                if (dialogMessages.get(position).getUserId() == userId) vewType = 0;
                else vewType = 1;
            }
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
                    view = mInflater.inflate(R.layout.detailed_dialog_outgoing_message, parent, false);
                    break;
                case 1:
                    view = mInflater.inflate(R.layout.detailed_dialog_incoming_message, parent, false);
                    break;
                default:
                    view = mInflater.inflate(R.layout.detailed_dialog_separator, parent, false);
                    isSeparator = true;
            }
            return new ViewHolder(view, isSeparator);
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder , int position) {
            if (holder.getItemViewType() == 2){
                holder.separator.setText(getDate(dialogMessages.get(position).getTime()));
            }
            else {
                holder.wheel.spin();
                String avatarUrl = dialogMessages.get(position).getAvatar();
                if (avatarUrl != null && !avatarUrl.equals("")) {
                    Picasso.with(context)
                            .load(avatarUrl)
                            .transform(MainActivity.transformation())
                            .resize(400, 400)
                            .centerCrop()
                            .into(holder.avatar, new com.squareup.picasso.Callback() {
                                @Override
                                public void onSuccess() {
                                    holder.wheel.stopSpinning();
                                }

                                @Override
                                public void onError() {
                                    holder.wheel.stopSpinning();
                                }
                            });
                }else holder.wheel.stopSpinning();
                if (holder.getItemViewType() == 1)
                    holder.nickName.setText(dialogMessages.get(position).getNickName());
                holder.message.setText(dialogMessages.get(position).getMessage());
                holder.time.setText(getTime(dialogMessages.get(position).getTime()));
            }
        }

        @Override
        public int getItemCount() {
            return dialogMessages.size();
        }

        public String getTime(long seconds){
            long millis = seconds * 1000;
            return new SimpleDateFormat("HH:mm", getResources().getConfiguration().locale).format(new Date(millis));
        }

        public String getDate(long seconds){
            long millis = seconds * 1000;
            return new SimpleDateFormat("d MMMM, yyyy", getResources().getConfiguration().locale).format(new Date(millis));
        }

        private void loadHistory(){
            final LinearLayout dialogListLoadingProgress = (LinearLayout) findViewById(R.id.dialog_loading_progress);
            final LinearLayout dialogListEmpty = (LinearLayout) findViewById(R.id.dialog_empty);
            dialogListLoadingProgress.setVisibility(View.VISIBLE);

            QBDialog qbDialog = new QBDialog(dialogId);

            QBRequestGetBuilder requestBuilder = new QBRequestGetBuilder();
            requestBuilder.setPagesLimit(100);

            QBChatService.getDialogMessages(qbDialog, requestBuilder, new QBEntityCallbackImpl<ArrayList<QBChatMessage>>() {
                @Override
                public void onSuccess(ArrayList<QBChatMessage> messages, Bundle args) {
                    for(QBChatMessage msg: messages){
                        dialogMessages.add(new DialogMessage((String) msg.getProperty("avatarUrl"),
                                (String) msg.getProperty("nickname"),
                                msg.getBody(), msg.getDateSent(), msg.getSenderId()));
                    }
                    if (dialogMessages.size() != 0) {
                        ArrayList<Integer> listItemHeaderIndexes = new ArrayList<>();
                        int headerCounter = 0;
                        listItemHeaderIndexes.add(0);
                        headerCounter++;
                        for(int i=0; i<dialogMessages.size()-1; i++) {
                            Calendar c1 = Calendar.getInstance();
                            c1.setTimeInMillis(dialogMessages.get(i).getTime() * 1000);
                            Calendar c2 = Calendar.getInstance();
                            c2.setTimeInMillis(dialogMessages.get(i + 1).getTime() * 1000);
                            if (c1.get(Calendar.DAY_OF_MONTH) != c2.get(Calendar.DAY_OF_MONTH) ||
                                    (c1.get(Calendar.DAY_OF_MONTH) == c2.get(Calendar.DAY_OF_MONTH) &&
                                     c1.get(Calendar.MONTH) != c2.get(Calendar.MONTH)) ||
                                        (c1.get(Calendar.DAY_OF_MONTH) == c2.get(Calendar.DAY_OF_MONTH) &&
                                         c1.get(Calendar.MONTH) == c2.get(Calendar.MONTH) &&
                                         c1.get(Calendar.YEAR) != c2.get(Calendar.YEAR))) {
                                listItemHeaderIndexes.add(i + 1 + headerCounter);
                                headerCounter++;
                            }
                        }
                        for(int i=0; i<listItemHeaderIndexes.size(); i++) {
                            int position = listItemHeaderIndexes.get(i);
                            dialogMessages.add(position, new DialogMessage(dialogMessages.get(position).getTime()));
                            dialogMessages.get(position).setIsHeader(true);
                        }

                        recyclerView.scrollToPosition(dialogMessages.size() - 1);
                        adapter.notifyDataSetChanged();
                        dialogListLoadingProgress.setVisibility(View.GONE);
                    }
                    else dialogListEmpty.setVisibility(View.VISIBLE);
                }

                @Override
                public void onError(List<String> errors) {
                    dialogListLoadingProgress.setVisibility(View.GONE);
                    Toast.makeText(DetailedDialogActivity.this, "Get history error: " + errors.get(0), Toast.LENGTH_LONG).show();
                }
            });
        }

        public class ViewHolder extends RecyclerView.ViewHolder{
            ImageView avatar;
            TextView nickName;
            TextView message;
            TextView time;
            TextView separator;
            ProgressWheel wheel;

            public ViewHolder(View view, boolean isSeparator) {
                super(view);
                if (!isSeparator) {
                    avatar = (ImageView) view.findViewById(R.id.dialog_opponent_avatar);
                    nickName = (TextView) view.findViewById(R.id.dialog_nickname);
                    message = (TextView) view.findViewById(R.id.dialog_message);
                    time = (TextView) view.findViewById(R.id.dialog_time);
                    wheel = (ProgressWheel) view.findViewById(R.id.dialog_avatar_progress);
                }
                else separator = (TextView) view.findViewById(R.id.dialog_separator_title);
            }
        }
    }

    public class DialogMessage {
        private String avatar;
        private String nickName;
        private String message;
        private long time;
        private int userId;
        private boolean isHeader = false;

        public DialogMessage(String avatar, String nickName, String message, long time, int userId) {
            super();
            this.avatar = avatar;
            this.nickName = nickName;
            this.message = message;
            this.time = time;
            this.userId = userId;
        }

        public DialogMessage(long time) {
            super();
            this.time = time;
        }

        public String getNickName() {
            return nickName;
        }

        public String getAvatar() {
            return avatar;
        }

        public String getMessage() {
            return message;
        }

        public long getTime() {
            return time;
        }

        public int getUserId() {
            return userId;
        }

        public boolean isHeader() {
            return isHeader;
        }

        public void setAvatar(String avatar) {
            this.avatar = avatar;
        }

        public void setTime(long time) {
            this.time = time;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public void setNickName(String nickName) {
            this.nickName = nickName;
        }

        public void setUserId(int userId) {
            this.userId = userId;
        }

        public void setIsHeader(boolean isHeader) {
            this.isHeader = isHeader;
        }
    }
}
