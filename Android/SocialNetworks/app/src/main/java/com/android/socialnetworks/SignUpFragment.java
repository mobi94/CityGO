package com.android.socialnetworks;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
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
import com.parse.RequestPasswordResetCallback;
import com.parse.SignUpCallback;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.vk.sdk.VKScope;

import fr.castorflex.android.smoothprogressbar.SmoothProgressBar;

public class SignUpFragment extends Fragment implements SocialNetworkManager.OnInitializationCompleteListener, OnLoginCompleteListener, OnRequestDetailedSocialPersonCompleteListener {

    public static SocialNetworkManager mSocialNetworkManager;
    private Button signup_button;
    private SmoothProgressBar progressBar;
    private MaterialEditText username;
    private MaterialEditText password;
    private boolean isEmailValid;
    private boolean isUserNameValid;
    private boolean isPasswordValid;
    private boolean isUserNameFilled;
    private boolean isPasswordFilled;
    private int stackSize = 0;

    public SignUpFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.sign_up_fragment, container, false);
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
        mSocialNetworkManager = (SocialNetworkManager) getFragmentManager().findFragmentByTag(SignUpActivity.SOCIAL_NETWORK_TAG);

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
            getFragmentManager().beginTransaction().add(mSocialNetworkManager, SignUpActivity.SOCIAL_NETWORK_TAG).commit();
            mSocialNetworkManager.setOnInitializationCompleteListener(this);
        } else {
            //if manager exist - get and setup login only for initialized SocialNetworks
            if (!mSocialNetworkManager.getInitializedSocialNetworks().isEmpty()) {
                List<SocialNetwork> socialNetworks = mSocialNetworkManager.getInitializedSocialNetworks();
                for (SocialNetwork socialNetwork : socialNetworks) {
                    socialNetwork.setOnLoginCompleteListener(this);
                }
            }
        }
        isUserNameFilled = false;
        isPasswordFilled = false;
        signup_button = (Button) rootView.findViewById(R.id.sign_up);
        signup_button.setOnClickListener(buttonsClick);
        progressBar = (SmoothProgressBar) rootView.findViewById(R.id.signup_progress_bar);
        progressBar.setVisibility(View.GONE);
        username = (MaterialEditText)rootView.findViewById(R.id.username_edit);
        username.setBackgroundResource(R.drawable.background_normal_name);
        userNameListener();
        password = (MaterialEditText)rootView.findViewById(R.id.password_edit);
        password.setBackgroundResource(R.drawable.background_normal);
        passwordListener();
        TextView passwordRecovery = (TextView) rootView.findViewById(R.id.password_recovery);
        passwordRecovery.setOnClickListener(buttonsClick);

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

        getFragmentManager().addOnBackStackChangedListener(new android.support.v4.app.FragmentManager.OnBackStackChangedListener() {
            @Override
            public void onBackStackChanged() {
                if (isResumed()) {
                    int current = getFragmentManager().getBackStackEntryCount();
                    if (current == 1) stackSize = 1;
                    if (current == 0 && stackSize == 1) {
                        enableAllViews();
                        stackSize = 0;
                    }
                }
            }
        });

        return rootView;
    }

    private void disableAllViews(){
        RelativeLayout layout = (RelativeLayout) getActivity().findViewById(R.id.sign_up_fragment);
        for (int i = 0; i < layout.getChildCount(); i++) {
            View child = layout.getChildAt(i);
            child.setEnabled(false);
        }
    }

    private void enableAllViews(){
        RelativeLayout layout = (RelativeLayout) getActivity().findViewById(R.id.sign_up_fragment);
        for (int i = 0; i < layout.getChildCount(); i++) {
            View child = layout.getChildAt(i);
            child.setEnabled(true);
        }
    }

    private void passwordListener(){
        password.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {
            }
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                validatePassword(s.toString());
                if (isPasswordValid) {
                    password.setBackgroundResource(R.drawable.background_normal);
                    password.setFloatingLabelText("     " + getString(R.string.password_edit_text));
                }
                else {
                    password.setBackgroundResource(R.drawable.background_error);
                    password.setFloatingLabelText("     " + getString(R.string.password_edit_text_main_hint));
                }
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
                if (isUserNameValid) {
                    username.setBackgroundResource(R.drawable.background_normal_name);
                    username.setFloatingLabelText("     " + getString(R.string.user_name_edit_text));
                }
                else {
                    username.setBackgroundResource(R.drawable.background_error);
                    if (s.length()<=2) username.setFloatingLabelText("   " + getString(R.string.user_name_hint_1));
                    else username.setFloatingLabelText("     " + getString(R.string.user_name_hint_2));
                }
                updateSigninButtonState();
            }
        });
    }

    private void validateUserName(String text) {
        isUserNameValid = (text.length()==0 || text.length()>=2) && text.length()<=15;
        isUserNameFilled = text.length() != 0;
    }

    private void validatePassword(String text) {
        isPasswordValid = text.length()==0 || text.length()>=6;
        isPasswordFilled = text.length() != 0;
    }

    private void updateSigninButtonState() {
        if (isUserNameFilled && isPasswordFilled) {
            signup_button.setText(R.string.login_button);
            signup_button.setBackgroundResource(R.drawable.background_success);
            if (isPasswordValid && isUserNameValid) {
                signup_button.setEnabled(true);
            } else {
                signup_button.setEnabled(false);
            }
        }
        else {
            signup_button.setText(R.string.sign_up_main_button);
            signup_button.setBackgroundResource(R.drawable.background_signup);
            signup_button.setEnabled(true);
        }
    }

    private View.OnClickListener buttonsClick = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()){
                case R.id.sign_up:
                    if(!SignUpActivity.isNetworkOn(getActivity().getBaseContext())) {
                        Toast.makeText(getActivity().getBaseContext(), getString(R.string.toast_no_network_connection), Toast.LENGTH_SHORT).show();
                    } else {
                        if(signup_button.getText().toString().equals(getString(R.string.sign_up_main_button))) {
                            disableAllViews();
                            getFragmentManager().beginTransaction()
                                    .setCustomAnimations(
                                            R.anim.slide_in_bottom, R.anim.slide_out_bottom,
                                            R.anim.slide_in_bottom, R.anim.slide_out_bottom)
                                    .add(R.id.container, new RegistrationFragment())
                                    .addToBackStack(null)
                                    .commit();
                        }
                        else{
                            //SignUpActivity.showProgress(getString(R.string.progress_dialog_msg_loading_user_profile));
                            signup_button.setText(getString(R.string.progress_bar_loading));
                            disableAllViews();
                            progressBar.setVisibility(View.VISIBLE);
                            progressBar.progressiveStart();
                            ParseUser.logInInBackground(username.getText().toString(), password.getText().toString(), new LogInCallback() {
                                public void done(ParseUser user, ParseException e) {
                                    //SignUpActivity.hideProgress();
                                    enableAllViews();
                                    updateSigninButtonState();
                                    progressBar.progressiveStop();
                                    if (user == null) {
                                        // Signup failed. Look at the ParseException to see what happened.
                                        Toast.makeText(getActivity(), "LOGIN ERROR: " + e, Toast.LENGTH_LONG).show();
                                    } else {
                                        signup_button.setText(getString(R.string.progress_bar_success));
                                        SignUpActivity.startMainActivity();
                                    }
                                }
                            });
                        }
                    }
                    break;
                case R.id.password_recovery:
                    passwordRestoreDialog();
                    break;
            }
        }
    };

    @Override
    public void onSocialNetworkManagerInitialized() {
        //when init SocialNetworks - get and setup login only for initialized SocialNetworks
        for (SocialNetwork socialNetwork : mSocialNetworkManager.getInitializedSocialNetworks()) {
            socialNetwork.setOnLoginCompleteListener(this);
        }
    }

    private View.OnClickListener loginClick = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (!SignUpActivity.isNetworkOn(getActivity().getBaseContext())) {
                Toast.makeText(getActivity().getBaseContext(), getString(R.string.toast_no_network_connection), Toast.LENGTH_SHORT).show();
            } else {
                int networkId = 0;
                switch (view.getId()) {
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
                //SignUpActivity.showProgress(getString(R.string.progress_dialog_msg_loading_user_profile));
                disableAllViews();
                signup_button.setText(getString(R.string.progress_bar_loading));
                progressBar.setVisibility(View.VISIBLE);
                progressBar.progressiveStart();
                SocialNetwork socialNetwork = mSocialNetworkManager.getSocialNetwork(networkId);
                if (!socialNetwork.isConnected()) {
                    if (networkId != 0) {
                        socialNetwork.requestLogin();
                    } else {
                        Toast.makeText(getActivity(), "Wrong networkId", Toast.LENGTH_LONG).show();
                    }
                } else {
                    startProfile(socialNetwork.getID());
                }
            }
        }
    };

    @Override
    public void onLoginSuccess(int networkId) {
        Toast.makeText(getActivity(), getString(R.string.toast_login_success), Toast.LENGTH_SHORT).show();
        startProfile(networkId);
    }

    @Override
    public void onError(int networkId, String requestID, String errorMessage, Object data) {
        //SignUpActivity.hideProgress();
        enableAllViews();
        updateSigninButtonState();
        progressBar.progressiveStop();
        Log.d("LOGIN ERROR", errorMessage);
        Toast.makeText(getActivity(), "LOGIN ERROR: " + errorMessage, Toast.LENGTH_LONG).show();
    }

    private void startProfile(int networkId){
        SocialNetwork socialNetwork = SignUpFragment.mSocialNetworkManager.getSocialNetwork(networkId);
        socialNetwork.setOnRequestDetailedSocialPersonCompleteListener(this);
        socialNetwork.requestDetailedCurrentPerson();
    }

    @Override
    public void onRequestDetailedSocialPersonSuccess(int socialNetworkID, SocialPerson socialPerson) {
        ParseUser currentUser = ParseUser.getCurrentUser();
        if (currentUser == null) {
            // show the signup or login screen
            parseNewUser(socialNetworkID, socialPerson);
        }
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
                enableAllViews();
                updateSigninButtonState();
                progressBar.progressiveStop();
                if (user == null) {
                    // Signup failed. Look at the ParseException to see what happened.
                    Log.d("LOGIN ERROR", "logInInBackground" + e);
                    Toast.makeText(getActivity(), "LOGIN ERROR: " + e, Toast.LENGTH_LONG).show();
                }
                else {
                    signup_button.setText(getString(R.string.progress_bar_success));
                    signup_button.setBackgroundResource(R.drawable.background_success);
                    SignUpActivity.startMainActivity();
                }
                //SignUpActivity.hideProgress();
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
                    else //SignUpActivity.hideProgress();
                    {
                        enableAllViews();
                        updateSigninButtonState();
                        progressBar.progressiveStop();
                    }
                }
                else {
                    enableAllViews();
                    updateSigninButtonState();
                    progressBar.progressiveStop();
                    signup_button.setText(getString(R.string.progress_bar_success));
                    signup_button.setBackgroundResource(R.drawable.background_success);
                    SignUpActivity.startMainActivity();
                    //SignUpActivity.hideProgress();
                }
            }
        });
    }

    private void passwordRestoreDialog (){
        AlertDialog.Builder alertBw;
        final AlertDialog alertDw;
        final EditText email = new EditText(getActivity());
        email.setHint(getString(R.string.password_restore_dialog_email_hint));
        email.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS );
        email.setBackgroundResource(R.drawable.background_normal);
        email.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }
                @Override
                public void afterTextChanged(Editable s) {
                }
                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    validateEmail(s.toString());
                    if (isEmailValid) email.setBackgroundResource(R.drawable.background_normal);
                    else email.setBackgroundResource(R.drawable.background_error);
                }
            });

        RelativeLayout linearLayout=new RelativeLayout(getActivity());
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(250,100);
        RelativeLayout.LayoutParams numPicerParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,RelativeLayout.LayoutParams.WRAP_CONTENT);
        numPicerParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
        linearLayout.setLayoutParams(params);
        linearLayout.addView(email,numPicerParams);

        alertBw=new AlertDialog.Builder(getActivity());
        alertBw.setTitle(getString(R.string.password_restore_dialog_title));
        alertBw.setView(linearLayout);
        alertBw.setPositiveButton(getString(R.string.ok_button), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(final DialogInterface dialog, int which) {
                if (isEmailValid) {
                    if (!SignUpActivity.isNetworkOn(getActivity().getBaseContext())) {
                        Toast.makeText(getActivity().getBaseContext(), getString(R.string.toast_no_network_connection), Toast.LENGTH_SHORT).show();
                    } else {
                        SignUpActivity.showProgress(getString(R.string.progress_dialog_msg_sending_email));
                        ParseUser.requestPasswordResetInBackground(email.getText().toString(),
                                new RequestPasswordResetCallback() {
                                    public void done(ParseException e) {
                                        SignUpActivity.hideProgress();
                                        if (e == null) {
                                            Toast.makeText(getActivity(),
                                                    getString(R.string.password_restore_dialog_toast_success), Toast.LENGTH_LONG).show();
                                            dialog.dismiss();
                                        } else {
                                            // Something went wrong. Look at the ParseException to see what's up.
                                            Toast.makeText(getActivity(), "PASSWORD_RESET_ERROR: " + e, Toast.LENGTH_LONG).show();
                                        }
                                    }
                                });
                    }
                }
            }
        });
        alertBw.setNeutralButton(getString(R.string.cancel_button), new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialog, int which){
                dialog.dismiss();
            }
        });
        alertDw=alertBw.create();
        alertDw.show();
    }

    private void validateEmail(String text) { isEmailValid = text.length()==0 || Patterns.EMAIL_ADDRESS.matcher(text).matches();
    }
}
