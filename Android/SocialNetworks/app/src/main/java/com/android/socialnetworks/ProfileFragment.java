package com.android.socialnetworks;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.makeramen.RoundedTransformationBuilder;
import com.parse.GetDataCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseUser;
import com.parse.ProgressCallback;
import com.parse.SaveCallback;
import com.pnikosis.materialishprogress.ProgressWheel;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Random;

public class ProfileFragment extends Fragment {

    public ProgressWheel wheel;
    private ImageView avatar;
    private TextView userName;
    private TextView gender;
    private TextView age;
    private Button changeAvatar;
    private Button editProfile;
    private String birthday;
    private Uri outputFileUri;
    private MyProgressDialog progressDialog;
    private int stackSize = 0;
    private boolean isFirstTime;
    private boolean isProfileEdited = false;

    private static final String ARG_POSITION = "position";

    public static ProfileFragment newInstance(int position) {
        ProfileFragment f = new ProfileFragment();
        Bundle b = new Bundle();
        b.putInt(ARG_POSITION, position);
        f.setArguments(b);
        return f;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.profile_fragment, container, false);
        setHasOptionsMenu(true);
        isFirstTime = true;
        wheel = (ProgressWheel) rootView.findViewById(R.id.progress_wheel);
        progressDialog = new MyProgressDialog(getActivity());
        avatar = (ImageView)rootView.findViewById(R.id.profile_avatar);

        changeAvatar = (Button) rootView.findViewById(R.id.profile_change_avatar);
        changeAvatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectPhoto();
            }
        });
        editProfile = (Button) rootView.findViewById(R.id.profile_edit_my_info);
        editProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isProfileEdited = true;
                startEditFragment();
            }
        });

        userName = (TextView)rootView.findViewById(R.id.profile_username);
        gender = (TextView)rootView.findViewById(R.id.profile_gender);
        age = (TextView)rootView.findViewById(R.id.profile_age);

        getFragmentManager().addOnBackStackChangedListener(new FragmentManager.OnBackStackChangedListener() {
            @Override
            public void onBackStackChanged() {
                if (isProfileEdited) {
                    int current = getFragmentManager().getBackStackEntryCount();
                    if (current == 1) stackSize = 1;
                    if (current == 0 && stackSize == 1) {
                        if (!isDataMatch()) {
                            getProfile();
                        }
                        enableButtons();
                        MyViewPager pager = (MyViewPager) getActivity().findViewById(R.id.pager);
                        pager.setPagingEnabled(true);
                        isProfileEdited = false;
                        stackSize = 0;
                    }
                }
            }
        });
        return rootView;
    }

    private void disableButtons(){
        changeAvatar.setEnabled(false);
        editProfile.setEnabled(false);
    }

    private void enableButtons(){
        changeAvatar.setEnabled(true);
        editProfile.setEnabled(true);
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {
            if (isFirstTime) {
                getPhoto();
                getProfile();
                isFirstTime = false;
            }
        }
    }

    private boolean isDataMatch(){
        boolean isNnicknameCorrect = false;
        boolean isGenderCorrect = false;
        boolean isBirthdayCorrect = false;
        ParseUser currentUser = ParseUser.getCurrentUser();
        if (currentUser != null) {
            isNnicknameCorrect = userName.getText().toString().equals(currentUser.getString("nickname"));
            isGenderCorrect = birthday.equals(currentUser.getString("birthday"));
            isBirthdayCorrect = gender.getText().toString().equals(currentUser.getString("gender"));
        }
        return isNnicknameCorrect && isGenderCorrect && isBirthdayCorrect;
    }

    private void startEditFragment(){
        disableButtons();
        MyViewPager pager = (MyViewPager) getActivity().findViewById(R.id.pager);
        pager.setPagingEnabled(false);
        EditProfileFragment editProfleFragment = new EditProfileFragment();
        Bundle bundle = new Bundle();
        bundle.putString("NICKNAME", userName.getText().toString());
        bundle.putString("BIRTHDAY", birthday);
        bundle.putString("GENDER", gender.getText().toString());
        editProfleFragment.setArguments(bundle);
        getFragmentManager().beginTransaction()
                .setCustomAnimations(
                        R.anim.slide_in_bottom, R.anim.slide_out_bottom,
                        R.anim.slide_in_bottom, R.anim.slide_out_bottom)
                .add(R.id.container, editProfleFragment)
                .addToBackStack(null)
                .commit();
    }


    private void getProfile(){
        ParseUser currentUser = ParseUser.getCurrentUser();
        if (currentUser != null) {
            String userNameString = currentUser.getString("nickname");
            if (userNameString != null) userName.setText(userNameString);
            else userName.setText(getString(R.string.profile_nickname_na));

            String genderString = currentUser.getString("gender");
            if (genderString != null) {
                switch (genderString) {
                    case "male":
                        gender.setText(R.string.gender_dialog_man_value);
                        break;
                    case "female":
                        gender.setText(R.string.gender_dialog_female_value);
                        break;
                    case "not presented":
                        gender.setText(getString(R.string.profile_na));
                        break;
                    default:
                        gender.setText(genderString);
                        break;
                }
            }
            else gender.setText(getString(R.string.profile_na));

            String ageString = currentUser.getString("birthday");
            if (ageString != null) {
                age.setText(Integer.toString(MainActivity.getAge(ageString)));
                birthday = ageString;
            }
            else {
                age.setText(getString(R.string.profile_na));
                birthday = getString(R.string.profile_na);
            }
        }
    }

    private void getPhoto(){
        wheel.spin();
        final ParseUser currentUser = ParseUser.getCurrentUser();
        if (currentUser != null) {
            String photoUrl = currentUser.getString("avatarURL");
            if (photoUrl != null){
                if (Patterns.WEB_URL.matcher(photoUrl).matches()) {
                    Picasso.with(getActivity())
                            .load(photoUrl)
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
                }
                else downloadPhoto(currentUser);
            }
            else downloadPhoto(currentUser);
        }
    }

    private void downloadPhoto(ParseUser user){
        ParseFile photo = (ParseFile) user.get("profilePic");
        String photoUrl = photo.getUrl();
        if (photoUrl.endsWith(".png"))
            Picasso.with(getActivity())
                    .load(photoUrl)
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
        else
            Picasso.with(getActivity())
                    .load(photoUrl)
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
    }

    private void selectPhoto() {
        final CharSequence[] items = {
                getString(R.string.photo_picker_dialog_take_photo),
                getString(R.string.photo_picker_dialog_choose_from_gallery),
                getString(R.string.cancel_button) };

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(getString(R.string.photo_picker_dialog_title));
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                if (items[item].equals(items[0])) {
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    File file = new File(Environment.getExternalStorageDirectory(), MainActivity.getRandomString(10) + ".jpg");
                    outputFileUri = Uri.fromFile(file);
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);
                    startActivityForResult(intent, 0);
                } else if (items[item].equals(items[1])) {
                    Intent pickPhoto = new Intent(Intent.ACTION_PICK,
                            android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(pickPhoto , 1);//one can be replaced with any action code
                } else if (items[item].equals(items[2])) {
                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }

    private void deletePhoto(Uri fileUri){
        if (fileUri != null) {
            File fdelete = new File(fileUri.getPath());
            if (fdelete.exists()) {
                if (fdelete.delete()) {
                    if(fdelete.exists())
                        try {
                            if (fdelete.getCanonicalFile().delete()) {
                                if (fdelete.exists()) {
                                    getActivity().deleteFile(fdelete.getName());
                                }
                            }
                        }
                        catch (IOException e) {
                            e.printStackTrace();
                        }
                }
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            Uri imageUri = null;
            if (requestCode == 0) {
                if (data != null) {
                    if (data.hasExtra("data"))
                        imageUri = data.getParcelableExtra("data");
                }
                else imageUri = outputFileUri;
            }
            else imageUri = data.getData();
            progressDialog.showProgress(getString(R.string.progress_dialog_msg_loading_photo));
            Picasso.with(getActivity())
                    .load(imageUri)
                    .transform(MainActivity.transformation())
                    .resize(400, 400)
                    .centerCrop()
                    .into(avatar, new com.squareup.picasso.Callback() {
                        @Override
                        public void onSuccess() {
                            deletePhoto(outputFileUri);
                            uploadPhoto();
                            progressDialog.hideProgress();
                        }
                        @Override
                        public void onError() {
                            progressDialog.hideProgress();
                        }
                    });
        }
    }

    private void uploadPhoto(){
        final ParseUser currentUser = ParseUser.getCurrentUser();
        if (currentUser != null) {
            final ParseFile file = convertImageToParse();
            file.saveInBackground(new SaveCallback() {
                public void done(ParseException e) {
                    // Handle success or failure here ...
                    if (e == null) {
                        currentUser.put("profilePic", file);
                        currentUser.saveInBackground(new SaveCallback() {
                            public void done(ParseException e) {
                                if (e != null) {
                                    // Sign up didn't succeed. Look at the ParseException
                                    // to figure out what went wrong
                                    Log.d("USER_UPDATE_ERROR", "saveInBackground" + e);
                                    Toast.makeText(getActivity(), "USER_UPDATE_ERROR: " + e, Toast.LENGTH_LONG).show();
                                }
                            }
                        });
                    } else {
                        Toast.makeText(getActivity(), "PHOTO_UPLOADING_ERROR: " + e, Toast.LENGTH_LONG).show();
                        Log.d("PHOTO_UPLOADING_ERROR", "signUpInBackground" + e);
                    }
                }
            }, new ProgressCallback() {
                public void done(Integer percentDone) {
                    // Update your progress spinner here. percentDone will be between 0 and 100.
                    if (percentDone == 100) {
                        String photoUrl = currentUser.getString("avatarURL");
                        if (photoUrl != null) {
                            if (Patterns.WEB_URL.matcher(photoUrl).matches()) {
                                currentUser.put("avatarURL", "");
                                currentUser.saveInBackground(new SaveCallback() {
                                    public void done(ParseException e) {
                                        if (e != null) {
                                            // Sign up didn't succeed. Look at the ParseException
                                            // to figure out what went wrong
                                            Log.d("USER_DATA_UPLOAD_ERROR", "saveInBackground" + e);
                                        }
                                    }
                                });
                            }
                        }
                    }
                }
            });
        }
    }

    private ParseFile convertImageToParse(){
        BitmapDrawable bitmapDrawable = ((BitmapDrawable) avatar.getDrawable());
        Bitmap bitmap = bitmapDrawable .getBitmap();
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        byte[] imageInByte = stream.toByteArray();
        try {
            stream.flush();
            stream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new ParseFile("photo.png", imageInByte);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.clear();
        inflater.inflate(R.menu.menu_profile, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_logout:
                ParseUser.logOut();
                startActivity(new Intent(getActivity(), SignUpActivity.class));
                getActivity().finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
