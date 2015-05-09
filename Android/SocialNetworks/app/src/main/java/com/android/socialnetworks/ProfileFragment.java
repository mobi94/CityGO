package com.android.socialnetworks;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.util.Patterns;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.astuetz.PagerSlidingTabStrip;
import com.gc.materialdesign.widgets.SnackBar;
import com.leocardz.aelv.library.Aelv;
import com.parse.FindCallback;
import com.parse.FunctionCallback;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseRelation;
import com.parse.ParseUser;
import com.parse.ProgressCallback;
import com.parse.SaveCallback;
import com.pnikosis.materialishprogress.ProgressWheel;
import com.quickblox.chat.QBChatService;
import com.quickblox.core.QBEntityCallbackImpl;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.users.QBUsers;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;
import com.twotoasters.jazzylistview.JazzyHelper;
import com.twotoasters.jazzylistview.JazzyListView;

import org.jivesoftware.smack.SmackException;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class ProfileFragment extends Fragment {

    private ArrayList<ListItem> listItems;

    private ProgressWheel wheel;
    private ListAdapter listAdapter;
    private ImageView avatar;
    private ImageView blur;
    private TextView userNickName;
    private TextView gender;
    private TextView age;
    private Button changeAvatar;
    private Button editProfile;
    private String birthday;
    private Uri outputFileUri;
    private MyProgressDialog progressDialog;
    private int stackSize = 0;
    private boolean isFirstTime;
    private boolean isEditProfileButtonPressed = false;
    private ArrayList<Integer> listItemHeaderIndexes;
    private boolean isVisible = false;

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
        blur = (ImageView)rootView.findViewById(R.id.blur);

        changeAvatar = (Button) rootView.findViewById(R.id.profile_change_avatar);
        changeAvatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!SignUpActivity.isNetworkOn(getActivity())) {
                    Toast.makeText(getActivity(), getString(R.string.toast_no_network_connection), Toast.LENGTH_SHORT).show();
                } else selectPhoto();
            }
        });
        editProfile = (Button) rootView.findViewById(R.id.profile_edit_my_info);
        editProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isEditProfileButtonPressed = true;
                startEditFragment();
            }
        });

        userNickName = (TextView)rootView.findViewById(R.id.profile_username);
        gender = (TextView)rootView.findViewById(R.id.profile_gender);
        age = (TextView)rootView.findViewById(R.id.profile_age);

        getFragmentManager().addOnBackStackChangedListener(new FragmentManager.OnBackStackChangedListener() {
            @Override
            public void onBackStackChanged() {
                if ((isEditProfileButtonPressed
                        || MainActivity.getEventListToUpdateFlag(MainActivity.UpdatedFrom.PROFILE_FRAGMENT)
                        || !MainActivity.getEventListToUpdateFlag(MainActivity.UpdatedFrom.PROFILE_FRAGMENT)
                        || MainActivity.getEventListToDeleteFlag(MainActivity.UpdatedFrom.PROFILE_FRAGMENT)
                        || !MainActivity.getEventListToDeleteFlag(MainActivity.UpdatedFrom.PROFILE_FRAGMENT)) && isVisible) {
                    int current = getFragmentManager().getBackStackEntryCount();
                    if (current == 1) stackSize = 1;
                    if (current == 0 && stackSize == 1) {
                        ActionBar actionBar = ((ActionBarActivity) getActivity()).getSupportActionBar();
                        if (actionBar != null) {
                            actionBar.setHomeButtonEnabled(false);
                            actionBar.setDisplayHomeAsUpEnabled(false);
                            actionBar.setTitle(R.string.app_name);
                        }
                        if (MainActivity.EDIT_PROFILE_FRAGMENT_RESULT.equals("done")) {
                            if (!isDataMatch()) {
                                getProfile();
                                updateUserEventsDialog();
                            }
                            MainActivity.EDIT_PROFILE_FRAGMENT_RESULT = "";
                        }
                        updateEventList();
                        enableButtons();
                        MainActivity.enableViewPager((MyViewPager) getActivity().findViewById(R.id.pager),
                                (PagerSlidingTabStrip) getActivity().findViewById(R.id.tabs));
                        isEditProfileButtonPressed = false;
                        stackSize = 0;
                    }
                }
            }
        });

        return rootView;
    }

    private void setListViewItems(final boolean change) {
        final LinearLayout eventListEmpty = (LinearLayout) getActivity().findViewById(R.id.event_list_empty);
        final LinearLayout eventListLoadingProgress = (LinearLayout) getActivity().findViewById(R.id.event_list_loading_progress);
        LinearLayout eventListNoNetwork = (LinearLayout) getActivity().findViewById(R.id.event_list_no_network);
        if(!SignUpActivity.isNetworkOn(getActivity())) {
            if (eventListEmpty.getVisibility() == View.VISIBLE)
                eventListEmpty.setVisibility(View.GONE);
            if (eventListLoadingProgress.getVisibility() == View.VISIBLE)
                eventListLoadingProgress.setVisibility(View.GONE);
            eventListNoNetwork.setVisibility(View.VISIBLE);
        } else {
            final int COLLAPSED_HEIGHT = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 56.5f, getResources().getDisplayMetrics());
            final int EXPANDED_HEIGHT = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 106.5f, getResources().getDisplayMetrics());

            if (eventListEmpty.getVisibility() == View.VISIBLE)
                eventListEmpty.setVisibility(View.GONE);
            if (eventListNoNetwork.getVisibility() == View.VISIBLE)
                eventListNoNetwork.setVisibility(View.GONE);
            eventListLoadingProgress.setVisibility(View.VISIBLE);
            ParseUser currentUser = ParseUser.getCurrentUser();
            if (currentUser != null) {
                ParseRelation<ParseObject> relation = currentUser.getRelation("goEvent");
                relation.getQuery().whereExists("creatorName").addDescendingOrder("startDate").findInBackground(new FindCallback<ParseObject>() {
                    @Override
                    public void done(List<ParseObject> parseObjects, ParseException e) {
                        if (e == null) {
                            if (parseObjects.size() == 0) {
                                if (eventListLoadingProgress.getVisibility() == View.VISIBLE)
                                    eventListLoadingProgress.setVisibility(View.GONE);
                                eventListEmpty.setVisibility(View.VISIBLE);
                            } else {
                                ListItem listItem;
                                deleteSectionHeader();
                                for (ParseObject po : parseObjects) {
                                    if (change) {
                                        String objectId = po.getObjectId();
                                        if (MainActivity.markersNewIdsToUpdate != null) {
                                            for (int i = 0; i < MainActivity.markersNewIdsToUpdate.size(); i++) {
                                                if (MainActivity.markersNewIdsToUpdate.get(i).equals(objectId)) {
                                                    listItem = new ListItem(
                                                            po.getString("category"),
                                                            po.getString("title"),
                                                            po.getString("avaibleSeats") + " available seats",
                                                            po.getDate("startDate"),
                                                            objectId,
                                                            po.getString("creatorName"));
                                                    // setUp IS REQUIRED
                                                    listItem.setUp(COLLAPSED_HEIGHT, EXPANDED_HEIGHT, false);
                                                    listItems.add(listItem);
                                                    break;
                                                }
                                            }
                                        }
                                        if (MainActivity.markersIdsToUpdate != null) {
                                            for (int i = 0; i < MainActivity.markersIdsToUpdate.size(); i++) {
                                                if (MainActivity.markersIdsToUpdate.get(i).equals(objectId)) {
                                                    listItem = new ListItem(
                                                            po.getString("category"),
                                                            po.getString("title"),
                                                            po.getString("avaibleSeats") + " available seats",
                                                            po.getDate("startDate"),
                                                            objectId,
                                                            po.getString("creatorName"));
                                                    // setUp IS REQUIRED
                                                    listItem.setUp(COLLAPSED_HEIGHT, EXPANDED_HEIGHT, false);

                                                    for (int j = 0; j < listItems.size(); j++) {
                                                        if (listItems.get(j).getObjectId().equals(objectId)) {
                                                            listItems.set(j, listItem);
                                                            break;
                                                        }
                                                    }
                                                    break;
                                                }
                                            }
                                        }
                                    } else {
                                        listItem = new ListItem(
                                                po.getString("category"),
                                                po.getString("title"),
                                                po.getString("avaibleSeats") + " available seats",
                                                po.getDate("startDate"),
                                                po.getObjectId(),
                                                po.getString("creatorName"));
                                        // setUp IS REQUIRED
                                        listItem.setUp(COLLAPSED_HEIGHT, EXPANDED_HEIGHT, false);
                                        listItems.add(listItem);
                                    }
                                }
                                Collections.sort(listItems, new Comparator<ListItem>() {
                                    @Override
                                    public int compare(ListItem r1, ListItem r2) {
                                        return r1.getDate().compareTo(r2.getDate());
                                    }
                                });
                                sortUpcomingIntoTop();
                                setSectionHeader();
                                listAdapter.notifyDataSetChanged();
                                eventListLoadingProgress.setVisibility(View.GONE);
                                MainActivity.setOffEventListToUpdateFlag(MainActivity.UpdatedFrom.PROFILE_FRAGMENT);
                                MainActivity.markersNewIdsToUpdate = new ArrayList<>();
                                MainActivity.markersIdsToUpdate = new ArrayList<>();
                            }
                        } else
                            Toast.makeText(getActivity(), "UPDATE_EVENT_LIST_ERROR: " + e, Toast.LENGTH_LONG).show();
                    }
                });
            }
        }
    }

    private void sortUpcomingIntoTop(){
        Date now = new Date();
        if(listItems.get(0).getDate().getTime() < now.getTime()) {
            for (int i = listItems.size() - 1; i >= 0; i--) {
                if (listItems.get(i).getDate().getTime() >= now.getTime()) {
                    ListItem listItem = listItems.get(i);
                    listItems.remove(i);
                    listItems.add(0, listItem);
                    i++;
                } else break;
            }
        }
    }

    private void deleteSectionHeader(){
        if(listItemHeaderIndexes != null) {
            for (int i = 0; i < listItemHeaderIndexes.size(); i++) {
                listItems.remove(listItemHeaderIndexes.get(i) -i);
            }
        }
    }

    private void setSectionHeader(){
        listItemHeaderIndexes = new ArrayList<>();
        Date now = new Date();
        listAdapter.clearSectionHeaderItem();

        listAdapter.addSectionHeaderItem(0);
        listItemHeaderIndexes.add(0);
        for(int i=0; i<listItems.size()-1; i++) {
            if (listItems.get(i).getDate().getTime() >= now.getTime() && listItems.get(i+1).getDate().getTime() < now.getTime()) {
                listAdapter.addSectionHeaderItem(i+2);
                listItemHeaderIndexes.add(i+2);
            }
        }
        for(int i=0; i<listItemHeaderIndexes.size(); i++)
            listItems.add(listItemHeaderIndexes.get(i), new ListItem());
    }

    private void deleteEventFromList (){
        deleteSectionHeader();
        if (MainActivity.markersIdsToDelete != null) {
            for (int i = 0; i < MainActivity.markersIdsToDelete.size(); i++) {
                for (int j = 0; j < listItems.size(); j++) {
                    if (listItems.get(j).getObjectId().equals(MainActivity.markersIdsToDelete.get(i))) {
                        listItems.remove(j);
                        break;
                    }
                }
            }
            sortUpcomingIntoTop();
            setSectionHeader();
            MainActivity.markersIdsToDelete = new ArrayList<>();
            listAdapter.notifyDataSetChanged();
        }
        MainActivity.setOffEventListToDeleteFlag(MainActivity.UpdatedFrom.PROFILE_FRAGMENT);
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
        isVisible = isVisibleToUser;
        if (isVisibleToUser) {
            if (isFirstTime) {
                getPhoto();
                getProfile();
                generateEventList();
                isFirstTime = false;
            }
            else updateEventList();
        }
    }

    private void updateEventList(){
        if (MainActivity.getEventListToUpdateFlag(MainActivity.UpdatedFrom.PROFILE_FRAGMENT)){
            setListViewItems(true);
        }
        else if(MainActivity.getEventListToDeleteFlag(MainActivity.UpdatedFrom.PROFILE_FRAGMENT)){
            deleteEventFromList();
        }
    }

    private void generateEventList(){
        JazzyListView listView = (JazzyListView) getActivity().findViewById(R.id.profile_list);
        listView.setTransitionEffect(JazzyHelper.TILT);

        listItems = new ArrayList<>();
        setListViewItems(false);

        listAdapter = new ListAdapter(this, getActivity(), R.layout.events_list_item, listItems);

        listView.setAdapter(listAdapter);

        final Aelv aelv = new Aelv(true, 200, listItems, listView, listAdapter);

        listView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                aelv.toggle(view, position);
            }
        });
    }

    private boolean isDataMatch(){
        boolean isNicknameCorrect = false;
        boolean isGenderCorrect = false;
        boolean isBirthdayCorrect = false;
        ParseUser currentUser = ParseUser.getCurrentUser();
        if (currentUser != null) {
            isNicknameCorrect = userNickName.getText().toString().equals(currentUser.getString("nickname"));
            isBirthdayCorrect = birthday.equals(currentUser.getString("birthday"));
            String genderEng;
            if (gender.getText().toString().equals(getString(R.string.gender_dialog_male_value)))
                genderEng = "male";
            else
                genderEng = "female";
            isGenderCorrect = genderEng.equals(currentUser.getString("gender"));
        }
        return isNicknameCorrect && isGenderCorrect && isBirthdayCorrect;
    }

    private void startEditFragment(){
        disableButtons();
        EditProfileFragment editProfleFragment = new EditProfileFragment();
        Bundle bundle = new Bundle();
        bundle.putString("NICKNAME", userNickName.getText().toString());
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
            if (userNameString != null) userNickName.setText(userNameString);
            else userNickName.setText(getString(R.string.profile_nickname_na));

            String genderString = currentUser.getString("gender");
            if (genderString != null) {
                switch (genderString) {
                    case "male":
                        gender.setText(R.string.gender_dialog_male_value);
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

    private void setBlurBackground(String photoUrl){
        Point size = new Point();
        getActivity().getWindowManager().getDefaultDisplay().getSize(size);
        Picasso.with(getActivity())
                .load(photoUrl)
                .transform(new BlurTransformation(getActivity(), size.x))
                .resize(size.x, (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 100, getResources().getDisplayMetrics()))
                .centerCrop()
                .into(blur);
    }

    private void setBlurBackground(Uri photoUri){
        Point size = new Point();
        getActivity().getWindowManager().getDefaultDisplay().getSize(size);
        Picasso.with(getActivity())
                .load(photoUri)
                .transform(new BlurTransformation(getActivity(), size.x))
                .resize(400,400)
                .centerCrop()
                .into(targetForLocal);
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
                    setBlurBackground(photoUrl);
                }
                else downloadPhoto(currentUser);
            }
            else downloadPhoto(currentUser);
        }
    }

    private void downloadPhoto(ParseUser user){
        ParseFile photo = (ParseFile) user.get("profilePic");
        String photoUrl = photo.getUrl();
        Picasso.with(getActivity())
                .load(photo.getUrl())
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
        setBlurBackground(photoUrl);
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

    public Uri getImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.PNG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);
    }

    private Target targetForLocal = new Target() {
        @Override
        public void onBitmapLoaded(final Bitmap bitmap, Picasso.LoadedFrom from) {
            Point size = new Point();
            getActivity().getWindowManager().getDefaultDisplay().getSize(size);
            final Uri uri = getImageUri(getActivity(), bitmap);
            Picasso.with(getActivity())
                    .load(uri)
                    .resize(size.x, (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 100, getResources().getDisplayMetrics()))
                    .centerCrop()
                    .into(blur, new Callback() {
                        @Override
                        public void onSuccess() {
                            getActivity().getContentResolver().delete(uri, null, null);
                        }
                        @Override
                        public void onError() {
                            getActivity().getContentResolver().delete(uri, null, null);
                        }
                    });
        }
        @Override
        public void onBitmapFailed(Drawable errorDrawable) {}
        @Override
        public void onPrepareLoad(Drawable placeHolderDrawable) {}
    };

    private Target targetForInner = new Target() {
        @Override
        public void onBitmapLoaded(final Bitmap bitmap, Picasso.LoadedFrom from) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    uploadPhoto(bitmap);
                }
            }).start();
        }
        @Override
        public void onBitmapFailed(Drawable errorDrawable) {
            progressDialog.hideProgress();}
        @Override
        public void onPrepareLoad(Drawable placeHolderDrawable) {
            progressDialog.showProgress(getString(R.string.progress_dialog_msg_loading_photo));}
    };

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            Uri imageUri = null;
            if (requestCode == 0) {
                if (data != null) {
                    if (data.hasExtra("data"))
                        imageUri = data.getParcelableExtra("data");
                } else imageUri = outputFileUri;
            } else imageUri = data.getData();
            if (imageUri != null) {
                setBlurBackground(imageUri);
                Picasso.with(getActivity())
                        .load(imageUri)
                        .resize(400, 400)
                        .centerCrop()
                        .into(targetForInner);
                Picasso.with(getActivity())
                        .load(imageUri)
                        .transform(MainActivity.transformation())
                        .resize(400, 400)
                        .centerCrop()
                        .into(avatar, new com.squareup.picasso.Callback() {
                            @Override
                            public void onSuccess() {
                                deletePhoto(outputFileUri);
                            }
                            @Override
                            public void onError() {}
                        });
            }
        }
    }

    private void uploadPhoto(Bitmap bitmap){
        final ParseUser currentUser = ParseUser.getCurrentUser();
        if (currentUser != null) {
            final ParseFile file = convertImageToParse(bitmap);
            file.saveInBackground(new SaveCallback() {
                public void done(ParseException e) {
                    // Handle success or failure here ...
                    if (e == null) {
                        currentUser.put("profilePic", file);
                        currentUser.saveInBackground(new SaveCallback() {
                            public void done(ParseException e) {
                                if (e != null) {
                                    Toast.makeText(getActivity(), "PHOTO_UPDATE_ERROR: " + e, Toast.LENGTH_LONG).show();
                                    progressDialog.hideProgress();
                                }
                            }
                        });
                    } else {
                        Toast.makeText(getActivity(), "PHOTO_UPDATE_ERROR: " + e, Toast.LENGTH_LONG).show();
                        progressDialog.hideProgress();
                    }
                }
            }, new ProgressCallback() {
                public void done(Integer percentDone) {
                    if (percentDone == 100) {
                        String photoUrl = currentUser.getString("avatarURL");
                        if (photoUrl != null) {
                            if (Patterns.WEB_URL.matcher(photoUrl).matches()) {
                                currentUser.put("avatarURL", "");
                                currentUser.saveInBackground(new SaveCallback() {
                                    public void done(ParseException e) {
                                        progressDialog.hideProgress();
                                        if (e == null) {
                                            updateUserEventsDialog();
                                        }
                                    }
                                });
                            }
                            else {
                                progressDialog.hideProgress();
                                updateUserEventsDialog();
                            }
                        }
                        else {
                            progressDialog.hideProgress();
                            updateUserEventsDialog();
                        }
                    }
                }
            });
        }
    }

    private ParseFile convertImageToParse(Bitmap bitmap){
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
                logoutFromQuickBloxChat();
                startActivity(new Intent(getActivity(), SignUpActivity.class));
                getActivity().finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void logoutFromQuickBloxChat(){
        //QBChatService chatService = MainActivity.chatService;
        final QBChatService chatService;
        if (!QBChatService.isInitialized()) {
            QBChatService.init(getActivity());
        }
        chatService = QBChatService.getInstance();
        if(chatService != null) {
            if (chatService.isLoggedIn()) {
                chatService.logout(new QBEntityCallbackImpl() {
                    @Override
                    public void onSuccess() {
                        Log.d("Chat_logout_success", "Chat_logout_success");
                        chatService.destroy();
                    }
                    @Override
                    public void onError(final List error) {
                        Log.d("Chat_logout_error", error.get(0).toString());
                    }
                });
            }
        }
    }

    private void updateUserEventsDialog() {
        String message = "Do you want to update your events with new data?";
        SnackBar snackbar = new SnackBar(getActivity(), message, "Yes", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!SignUpActivity.isNetworkOn(getActivity())) {
                    Toast.makeText(getActivity(), getString(R.string.toast_no_network_connection), Toast.LENGTH_SHORT).show();
                }
                else {
                    progressDialog.showProgress("Events data updating...");
                    ParseUser currentUser = ParseUser.getCurrentUser();
                    String creatorAvatarUrl = currentUser.getString("avatarURL");
                    if (creatorAvatarUrl == null || creatorAvatarUrl.equals("")) {
                        ParseFile photo = (ParseFile) currentUser.get("profilePic");
                        creatorAvatarUrl = photo.getUrl();
                    }
                    HashMap<String, Object> params = new HashMap<>();
                    params.put("creatorName", currentUser.getUsername());
                    params.put("newCreatorNickName", userNickName.getText());
                    if (gender.getText().toString().equals(getString(R.string.gender_dialog_male_value)))
                        params.put("newCreatorGender", "male");
                    else
                        params.put("newCreatorGender", "female");
                    params.put("newCreatorAge", age.getText());
                    params.put("newCreatorAvatarUrl", creatorAvatarUrl);
                    ParseCloud.callFunctionInBackground("updateUserEvents", params, new FunctionCallback<Object>() {
                        @Override
                        public void done(Object o, ParseException e) {
                            progressDialog.hideProgress();
                            if (e != null) Toast.makeText(getActivity(), "UPDATE_EVENT_DATA_ERROR" + e, Toast.LENGTH_SHORT).show();
                            else {
                                MainActivity.EVENT_FRAGMENT_RESULT = "done";
                                MainActivity.MAP_MARKERS_UPDATE = true;
                            }
                        }
                    });
                }
            }
        });
        snackbar.setDismissTimer(7*1000);
        snackbar.setBackgroundSnackBar(getResources().getColor(R.color.niagara));
        snackbar.setColorButton(getResources().getColor(R.color.red));
        snackbar.setMessageTextSize(16);
        snackbar.show();
        /*String message = "Do you want to update your events with new data?";
        AlertDialog.Builder alertBw = new AlertDialog.Builder(getActivity());
        alertBw.setTitle("Attention!");
        alertBw.setMessage(message);
        alertBw.setPositiveButton(getString(R.string.ok_button), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(final DialogInterface dialog, int which) {
                if (!SignUpActivity.isNetworkOn(getActivity())) {
                    Toast.makeText(getActivity(), getString(R.string.toast_no_network_connection), Toast.LENGTH_SHORT).show();
                }
                else {
                    progressDialog.showProgress("Events data updating...");
                    ParseUser currentUser = ParseUser.getCurrentUser();
                    String creatorAvatarUrl = currentUser.getString("avatarURL");
                    if (creatorAvatarUrl == null || creatorAvatarUrl.equals("")) {
                        ParseFile photo = (ParseFile) currentUser.get("profilePic");
                        creatorAvatarUrl = photo.getUrl();
                    }
                    HashMap<String, Object> params = new HashMap<>();
                    params.put("creatorName", currentUser.getUsername());
                    params.put("newCreatorNickName", userNickName.getText());
                    if (gender.getText().toString().equals(getString(R.string.gender_dialog_male_value)))
                        params.put("newCreatorGender", "male");
                    else
                        params.put("newCreatorGender", "female");
                    params.put("newCreatorAge", age.getText());
                    params.put("newCreatorAvatarUrl", creatorAvatarUrl);
                    ParseCloud.callFunctionInBackground("updateUserEvents", params, new FunctionCallback<Object>() {
                        @Override
                        public void done(Object o, ParseException e) {
                            progressDialog.hideProgress();
                            if (e != null) {
                                Toast.makeText(getActivity(), "UPDATE_EVENT_DATA_ERROR" + e, Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }
        });
        alertBw.setNeutralButton(getString(R.string.cancel_button), new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialog, int which){
                dialog.dismiss();
            }
        });
        AlertDialog alertDw = alertBw.create();
        alertDw.show();*/
    }
}
