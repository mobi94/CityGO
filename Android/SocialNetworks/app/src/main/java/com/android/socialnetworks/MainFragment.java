package com.android.socialnetworks;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.InputFilter;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.github.gorbin.asne.core.SocialNetwork;
import com.github.gorbin.asne.core.SocialNetworkManager;
import com.github.gorbin.asne.core.listener.OnLoginCompleteListener;
import com.github.gorbin.asne.core.listener.OnRequestDetailedSocialPersonCompleteListener;
import com.github.gorbin.asne.core.persons.SocialPerson;
import com.github.gorbin.asne.facebook.FacebookPerson;
import com.github.gorbin.asne.facebook.FacebookSocialNetwork;
import com.github.gorbin.asne.twitter.TwitterSocialNetwork;
import com.github.gorbin.asne.vk.VKPerson;
import com.github.gorbin.asne.vk.VkSocialNetwork;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SignUpCallback;
import com.vk.sdk.VKScope;

public class MainFragment extends Fragment implements SocialNetworkManager.OnInitializationCompleteListener, OnLoginCompleteListener, OnRequestDetailedSocialPersonCompleteListener {

    public static SocialNetworkManager mSocialNetworkManager;
    private Button signup_button;
    private EditText username;
    private EditText password;
    private boolean isUserNameValid;
    private boolean isPasswordValid;
    private boolean isUserNameFilled;
    private boolean isPasswordFilled;

    public MainFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.main_fragment, container, false);
        // init buttons and set Listener
        ImageButton vk = (ImageButton) rootView.findViewById(R.id.vk);
        vk.setOnClickListener(loginClick);
        ImageButton facebook = (ImageButton) rootView.findViewById(R.id.facebook);
        facebook.setOnClickListener(loginClick);
        ImageButton twitter = (ImageButton) rootView.findViewById(R.id.twitter);
        twitter.setOnClickListener(loginClick);

        //Get Keys for initiate SocialNetworks
        String VK_KEY = getActivity().getString(R.string.vk_app_id);
        String TWITTER_CONSUMER_KEY = getActivity().getString(R.string.twitter_consumer_key);
        String TWITTER_CONSUMER_SECRET = getActivity().getString(R.string.twitter_consumer_secret);
        String TWITTER_CALLBACK_URL = "oauth://ASNE";

        //Chose permissions
        String[] vkScope = new String[]{
                VKScope.FRIENDS,
                VKScope.WALL,
                VKScope.PHOTOS,
                VKScope.NOHTTPS,
                VKScope.STATUS,
        };
        ArrayList<String> fbScope = new ArrayList<>();
        fbScope.addAll(Arrays.asList("public_profile, email, user_friends"));

        //Use manager to manage SocialNetworks
        mSocialNetworkManager = (SocialNetworkManager) getFragmentManager().findFragmentByTag(MainActivity.SOCIAL_NETWORK_TAG);

        //Check if manager exist
        if (mSocialNetworkManager == null) {
            mSocialNetworkManager = new SocialNetworkManager();

            VkSocialNetwork vkNetwork = new VkSocialNetwork(this, VK_KEY, vkScope);
            mSocialNetworkManager.addSocialNetwork(vkNetwork);
            FacebookSocialNetwork fbNetwork = new FacebookSocialNetwork(this, fbScope);
            mSocialNetworkManager.addSocialNetwork(fbNetwork);
            TwitterSocialNetwork twNetwork = new TwitterSocialNetwork(this, TWITTER_CONSUMER_KEY, TWITTER_CONSUMER_SECRET, TWITTER_CALLBACK_URL);
            mSocialNetworkManager.addSocialNetwork(twNetwork);

            //Initiate every network from mSocialNetworkManager
            getFragmentManager().beginTransaction().add(mSocialNetworkManager, MainActivity.SOCIAL_NETWORK_TAG).commit();
            mSocialNetworkManager.setOnInitializationCompleteListener(this);
        } else {
            //if manager exist - get and setup login only for initialized SocialNetworks
            if (!mSocialNetworkManager.getInitializedSocialNetworks().isEmpty()) {
                List<SocialNetwork> socialNetworks = mSocialNetworkManager.getInitializedSocialNetworks();
                for (SocialNetwork socialNetwork : socialNetworks) {
                    socialNetwork.setOnLoginCompleteListener(this);
                    initSocialNetwork(socialNetwork);
                }
            }
        }
        isUserNameFilled = false;
        isPasswordFilled = false;
        signup_button = (Button) rootView.findViewById(R.id.sign_up);
        signup_button.setOnClickListener(buttonsClick);
        username = (EditText)rootView.findViewById(R.id.username_edit);
        username.setBackgroundResource(R.drawable.background_normal);
        userNameListener();
        password = (EditText)rootView.findViewById(R.id.password_edit);
        password.setBackgroundResource(R.drawable.background_normal);
        passwordListener();

        InputFilter filter = new InputFilter() {
            @Override
            public CharSequence filter(CharSequence source, int start, int end,
                                       Spanned dest, int dstart, int dend) {

                if (source instanceof SpannableStringBuilder) {
                    SpannableStringBuilder sourceAsSpannableBuilder = (SpannableStringBuilder)source;
                    for (int i = end - 1; i >= start; i--) {
                        char currentChar = source.charAt(i);
                        if (!Character.isLetterOrDigit(currentChar)) {
                            sourceAsSpannableBuilder.delete(i, i+1);
                        }
                    }
                    return source;
                } else {
                    StringBuilder filteredStringBuilder = new StringBuilder();
                    for (int i = start; i < end; i++) {
                        char currentChar = source.charAt(i);
                        if (Character.isLetterOrDigit(currentChar)) {
                            filteredStringBuilder.append(currentChar);
                        }
                    }
                    return filteredStringBuilder.toString();
                }
            }
        };
        username.setFilters(new InputFilter[] { filter });
        password.setFilters(new InputFilter[] { filter });
        return rootView;
    }

    private void passwordListener(){
        password.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {
            }
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                validatePassword(s.toString());
                if (isPasswordValid) password.setBackgroundResource(R.drawable.background_normal);
                else password.setBackgroundResource(R.drawable.background_error);
                updateSigninButtonState();
            }
        });
    }

    private void userNameListener() {
        username.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
            @Override
            public void afterTextChanged(Editable s) {
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                validateUserName(s.toString());
                if (isUserNameValid) username.setBackgroundResource(R.drawable.background_normal);
                else username.setBackgroundResource(R.drawable.background_error);
                updateSigninButtonState();
            }
        });
    }

    private void validateUserName(String text) {
        isUserNameValid = text.length()==0 || text.length()>=2;
        isUserNameFilled = text.length() != 0;
    }

    private void validatePassword(String text) {
        isPasswordValid = text.length()==0 || text.length()>=6;
        isPasswordFilled = text.length() != 0;
    }

    private void updateSigninButtonState() {
        if (isUserNameFilled && isPasswordFilled) {
            signup_button.setText("Sign In");
            if (isPasswordValid && isUserNameValid) {
                signup_button.setEnabled(true);
            } else {
                signup_button.setEnabled(false);
            }
        }
        else {
            signup_button.setText("Sign Up");
            signup_button.setEnabled(true);
        }
    }

    private View.OnClickListener buttonsClick = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()){
                case R.id.sign_up:
                    if(!isNetworkOn(getActivity().getBaseContext())) {
                        Toast.makeText(getActivity().getBaseContext(), "No network connection", Toast.LENGTH_SHORT).show();
                    } else {
                        // do login\
                        if(signup_button.getText().toString().equals("Sign Up")) {
                            getFragmentManager().beginTransaction()
                                    .setCustomAnimations(
                                            R.anim.slide_in_bottom, R.anim.slide_out_top,
                                            R.anim.slide_in_top, R.anim.slide_out_bottom)
                                    .replace(R.id.container, new RegistrationFragment())
                                    .addToBackStack(null)
                                    .commit();
                        }
                        else{
                            MainActivity.showProgress("Loading social person");
                            ParseUser.logInInBackground(username.getText().toString(), password.getText().toString(), new LogInCallback() {
                                public void done(ParseUser user, ParseException e) {
                                    if (user == null) {
                                        // Signup failed. Look at the ParseException to see what happened.
                                        Toast.makeText(getActivity(), "LOGIN ERROR: " + e, Toast.LENGTH_LONG).show();
                                    }
                                    else{
                                        Intent intent = new Intent(getActivity(), LoggedInActivity.class);
                                        getActivity().startActivity(intent);
                                        getActivity().finish();
                                        MainActivity.hideProgress();
                                    }
                                }
                            });
                        }
                    }
                    break;
            }
        }
    };

    public boolean isNetworkOn(Context context) { ConnectivityManager connMgr =
            (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
    }

    private void initSocialNetwork(SocialNetwork socialNetwork) {
        if (socialNetwork.isConnected()) {
            switch (socialNetwork.getID()) {
                case VkSocialNetwork.ID:
                    //vk.setText("Show VK profile");
                    break;
                case FacebookSocialNetwork.ID:
                    //facebook.setText("Show Facebook profile");
                    break;
                case TwitterSocialNetwork.ID:
                    //twitter.setText("Show Twitter profile");
                    break;
            }
        }
    }

    @Override
    public void onSocialNetworkManagerInitialized() {
        //when init SocialNetworks - get and setup login only for initialized SocialNetworks
        for (SocialNetwork socialNetwork : mSocialNetworkManager.getInitializedSocialNetworks()) {
            socialNetwork.setOnLoginCompleteListener(this);
            initSocialNetwork(socialNetwork);
        }
    }

    private View.OnClickListener loginClick = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            int networkId = 0;
            switch (view.getId()){
                case R.id.vk:
                    networkId = VkSocialNetwork.ID;
                    break;
                case R.id.facebook:
                    networkId = FacebookSocialNetwork.ID;
                    break;
                case R.id.twitter:
                    networkId = TwitterSocialNetwork.ID;
                    break;
            }
            SocialNetwork socialNetwork = mSocialNetworkManager.getSocialNetwork(networkId);
            if(!socialNetwork.isConnected()) {
                if(networkId != 0) {
                    socialNetwork.requestLogin();
                    MainActivity.showProgress("Loading social person");
                } else {
                    Toast.makeText(getActivity(), "Wrong networkId", Toast.LENGTH_LONG).show();
                }
            } else {
                startProfile(socialNetwork.getID());
            }
        }
    };

    @Override
    public void onLoginSuccess(int networkId) {
        MainActivity.hideProgress();
        startProfile(networkId);
        //Toast.makeText(getActivity(), "Login Success", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onError(int networkId, String requestID, String errorMessage, Object data) {
        MainActivity.hideProgress();
        Toast.makeText(getActivity(), "ERROR: " + errorMessage, Toast.LENGTH_LONG).show();
    }

    private void startProfile(int networkId){
        MainActivity.showProgress("Loading social person");
        SocialNetwork socialNetwork = MainFragment.mSocialNetworkManager.getSocialNetwork(networkId);
        socialNetwork.setOnRequestDetailedSocialPersonCompleteListener(this);
        socialNetwork.requestDetailedCurrentPerson();
    }

    @Override
    public void onRequestDetailedSocialPersonSuccess(int socialNetworkID, SocialPerson socialPerson) {
        ParseUser currentUser = ParseUser.getCurrentUser();
        if (currentUser == null) {
            // show the signup or login screen
            parseNewUser(socialNetworkID, socialPerson);
            Intent intent = new Intent(getActivity(), LoggedInActivity.class);
            getActivity().startActivity(intent);
            getActivity().finish();
        } /*else {
            // do stuff with the user
        }*/
    }

    public void parseLoginUser(final int socialNetworkID, final SocialPerson socialPerson){
        String name = "";
        switch(socialNetworkID){
            case 1:
                name = "tw";
                break;
            case 4:
                name = "fb";
                break;
            case 5:
                name = "vk";
                break;
        }
        ParseUser.logInInBackground(name + socialPerson.id, socialPerson.id + "aFdeCbc550c9", new LogInCallback() {
            public void done(ParseUser user, ParseException e) {
                if (user != null) {
                    // Hooray! The user is logged in.
                    user.put("nickname", socialPerson.name.replace(" ", ""));
                    switch (socialNetworkID) {
                        case 1:
                            user.put("avatarURL", socialPerson.avatarURL);
                            break;
                        case 4:
                            FacebookPerson facebookPerson = (FacebookPerson) socialPerson;
                            user.put("gender", facebookPerson.gender);
                            user.put("birthday", facebookPerson.birthday.replace("/", "."));
                            user.put("avatarURL", socialPerson.avatarURL);
                            break;
                        case 5:
                            VKPerson vkPerson = (VKPerson) socialPerson;
                            switch (vkPerson.sex) {
                                case 0:
                                    user.put("gender", "not presented");
                                    break;
                                case 1:
                                    user.put("gender", "female");
                                    break;
                                case 2:
                                    user.put("gender", "male");
                                    break;
                            }
                            user.put("birthday", vkPerson.birthday);
                            user.put("avatarURL", vkPerson.photoMaxOrig);
                            break;
                    }
                    user.saveInBackground(); // This succeeds, since the user was authenticated on the device
                } else {
                    // Signup failed. Look at the ParseException to see what happened.
                    Log.d("LOGIN ERROR", "logInInBackground" + e);
                    Toast.makeText(getActivity(), "LOGIN ERROR: " + e, Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    public void parseNewUser(final int socialNetworkID, final SocialPerson socialPerson){
        ParseUser user = new ParseUser();
        user.put("nickname", socialPerson.name.replace(" ", ""));
        user.setPassword(socialPerson.id + "aFdeCbc550c9");
        switch(socialNetworkID){
            case 1:
                user.setUsername("tw" + socialPerson.id);
                user.put("avatarURL", socialPerson.avatarURL);
                break;
            case 4:
                FacebookPerson facebookPerson = (FacebookPerson) socialPerson;
                user.setUsername("fb" + socialPerson.id);
                user.put("gender", facebookPerson.gender);
                user.put("birthday", facebookPerson.birthday.replace("/", "."));
                user.put("avatarURL", socialPerson.avatarURL);
                break;
            case 5:
                VKPerson vkPerson = (VKPerson) socialPerson;
                user.setUsername("vk" + socialPerson.id);
                switch(vkPerson.sex){
                    case 0:
                        user.put("gender", "not presented");
                        break;
                    case 1:
                        user.put("gender", "female");
                        break;
                    case 2:
                        user.put("gender", "male");
                        break;
                }
                user.put("birthday", vkPerson.birthday);
                user.put("avatarURL", vkPerson.photoMaxOrig);
                break;
        }
        user.signUpInBackground(new SignUpCallback() {
            public void done(ParseException e) {
                if (e != null) {
                    // Sign up didn't succeed. Look at the ParseException
                    // to figure out what went wrong
                    Log.d("SIGNUP ERROR", "signUpInBackground" + e);
                    // Toast.makeText(getActivity(), "SIGNUP ERROR: " + e, Toast.LENGTH_LONG).show();
                    if (e.getCode() == 202) {
                        parseLoginUser(socialNetworkID, socialPerson);
                    }
                }
            }
        });
    }
}
