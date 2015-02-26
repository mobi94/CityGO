package com.android.socialnetworks;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.animation.AnimatorListenerAdapter;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.FragmentManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.app.Fragment;
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
import android.widget.TextView;
import android.widget.Toast;

import com.makeramen.RoundedTransformationBuilder;
import com.parse.GetDataCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseUser;
import com.parse.ProgressCallback;
import com.parse.SaveCallback;
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

    private ImageView avatar;
    private TextView userName;
    private TextView gender;
    private TextView age;
    private Button editProfile;
    private String birthday;
    private Uri outputFileUri;
    private MyProgressDialog progressDialog;
    EditProfileFragment editProfleFragment;
    private int stackSize = 0;

    @Override
    public Animator onCreateAnimator(int transit, boolean enter, int nextAnim) {
        Animator animator = AnimatorInflater.loadAnimator(getActivity(), nextAnim);
        animator.addListener(new AnimatorListenerAdapter(){
            @Override
            public void onAnimationStart(Animator animation){}
            @Override
            public void onAnimationEnd(Animator animation){
                if (getFragmentManager().getBackStackEntryCount() == 1) {
                    getPhoto();
                    getProfile();
                }
            }
        });
        return animator;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.profile_fragment, container, false);
        setHasOptionsMenu(true);
        ActionBar actionBar = getActivity().getActionBar();
        if (actionBar != null) {
            actionBar.setTitle("Profile");
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        progressDialog = new MyProgressDialog(getActivity());
        avatar = (ImageView)rootView.findViewById(R.id.profile_avatar);

        Button changeAvatar = (Button) rootView.findViewById(R.id.profile_change_avatar);
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
                editProfile.setEnabled(false);
                startEditFragment();
            }
        });

        userName = (TextView)rootView.findViewById(R.id.profile_username);
        gender = (TextView)rootView.findViewById(R.id.profile_gender);
        age = (TextView)rootView.findViewById(R.id.profile_age);

        getFragmentManager().addOnBackStackChangedListener(new FragmentManager.OnBackStackChangedListener() {
            @Override
            public void onBackStackChanged() {
                if (isResumed()) {
                    int current = getFragmentManager().getBackStackEntryCount();
                    if (current == 2) stackSize = 2;
                    if (current == 1 && stackSize == 2) {
                        getPhoto();
                        getProfile();
                        editProfile.setEnabled(true);
                        stackSize = 0;
                    }
                }
            }
        });

        return rootView;
    }

    private void startEditFragment(){
        editProfleFragment = new EditProfileFragment();
        Bundle bundle = new Bundle();
        bundle.putString("NICKNAME", userName.getText().toString());
        bundle.putString("BIRTHDAY", birthday);
        bundle.putString("GENDER", gender.getText().toString());
        editProfleFragment.setArguments(bundle);
        getFragmentManager().beginTransaction()
                .setCustomAnimations(
                        R.animator.slide_up, R.animator.slide_down,
                        R.animator.slide_up, R.animator.slide_down)
                .add(R.id.container, editProfleFragment)
                .addToBackStack(null)
                .commit();
    }

    private Transformation transformation(){
       return new RoundedTransformationBuilder()
                .borderColor(Color.WHITE)
                .borderWidthDp(1)
                .cornerRadiusDp(200)
                .oval(false)
                .build();
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
                        gender.setText(getString(R.string.profile_gender_na));
                        break;
                    default:
                        gender.setText(genderString);
                        break;
                }
            }
            else gender.setText(getString(R.string.profile_gender_na));

            String ageString = currentUser.getString("birthday");
            if (ageString != null) {
                age.setText(Integer.toString(getAge(ageString)));
                birthday = ageString;
            }
            else {
                age.setText(getString(R.string.profile_age_na));
                birthday = getString(R.string.profile_age_na);
            }
        }
    }

    private int getAge(String string){
        DateFormat format = new SimpleDateFormat("dd.MM.yyyy", Locale.ENGLISH);
        Date date = null;
        try {
            date = format.parse(string);
        } catch (java.text.ParseException e) {
            e.printStackTrace();
        }
        Calendar birthDay = Calendar.getInstance();
        if (date != null) {
            birthDay.setTimeInMillis(date.getTime());
        }
        long currentTime = System.currentTimeMillis();
        Calendar now = Calendar.getInstance();
        now.setTimeInMillis(currentTime);
        return now.get(Calendar.YEAR) - birthDay.get(Calendar.YEAR);
    }

    private void getPhoto(){
        progressDialog.showProgress(getString(R.string.progress_dialog_msg_loading_user_profile));
        final ParseUser currentUser = ParseUser.getCurrentUser();
        if (currentUser != null) {
            String photoUrl = currentUser.getString("avatarURL");
            if (photoUrl != null){
                if (Patterns.WEB_URL.matcher(photoUrl).matches()) {
                    Picasso.with(getActivity())
                            .load(photoUrl)
                            .transform(transformation())
                            .resize(400, 400)
                            .centerCrop()
                            .into(avatar, new com.squareup.picasso.Callback() {
                                @Override
                                public void onSuccess() {
                                     progressDialog.hideProgress();
                                }
                                @Override
                                public void onError() {
                                     progressDialog.hideProgress();
                                }
                            });
                }
                else downloadPhoto(currentUser);
            }
            else downloadPhoto(currentUser);
        }
    }

    private void downloadPhoto(ParseUser user){
        ParseFile photo = (ParseFile) user.get("photo");
        photo.getDataInBackground(new GetDataCallback() {
            public void done(byte[] data, ParseException e) {
                if (e == null) {
                    // data has the bytes for the resume
                    final File f = new File(getActivity().getCacheDir(), getRandomString(10));
                    boolean isFileCreated = false;
                    try {
                        isFileCreated = f.createNewFile();
                        FileOutputStream fos = new FileOutputStream(f);
                        fos.write(data);
                        fos.flush();
                        fos.close();
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                    if (isFileCreated) {
                        Picasso.with(getActivity())
                                .load(f)
                                .into(avatar, new com.squareup.picasso.Callback() {
                                    @Override
                                    public void onSuccess() {
                                        deletePhoto(Uri.fromFile(f));
                                        progressDialog.hideProgress();
                                    }
                                    @Override
                                    public void onError() {
                                        progressDialog.hideProgress();
                                    }
                                });
                    }
                }
            }
        });
    }

    private static final String ALLOWED_CHARACTERS ="0123456789qwertyuiopasdfghjklzxcvbnm";

    private static String getRandomString(final int sizeOfRandomString)
    {
        final Random random=new Random();
        final StringBuilder sb=new StringBuilder(sizeOfRandomString);
        for(int i=0;i<sizeOfRandomString;++i)
            sb.append(ALLOWED_CHARACTERS.charAt(random.nextInt(ALLOWED_CHARACTERS.length())));
        return sb.toString();
    }

    private void selectPhoto() {
        final CharSequence[] items = {
                getString(R.string.photo_picker_dialog_take_photo),
                getString(R.string.photo_picker_dialog_choose_from_gallery),
                getString(R.string.photo_picker_dialog_cancel) };

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(getString(R.string.photo_picker_dialog_title));
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                if (items[item].equals(items[0])) {
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    File file = new File(Environment.getExternalStorageDirectory(), getRandomString(10) + ".jpg");
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
                    .transform(transformation())
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
                        currentUser.put("photo", file);
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
        inflater.inflate(R.menu.menu_profile, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                getFragmentManager().popBackStack();
                return true;
            case R.id.action_logout:
                ParseUser.logOut();
                startActivity(new Intent(getActivity(), SignUpActivity.class));
                getActivity().finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
