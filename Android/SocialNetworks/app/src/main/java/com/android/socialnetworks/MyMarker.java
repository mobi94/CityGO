package com.android.socialnetworks;

import com.google.android.gms.maps.model.LatLng;

import java.util.Date;

public class MyMarker {
    private String mTitle;
    private String mCreatorAge;
    private String mCreatorGender;
    private String mCreatorName;
    private String mCreatorNickName;

    private String mCreatorAvatarUrl;
    private String mCategory;
    private String mDuration;
    private String mAvailableSeats;
    private String mDescription;
    private Date mStartDate;
    private LatLng mLocation;

    public MyMarker(String title, String creatorAge, String creatorGender, String creatorName, String creatorNickName, String creatorAvatarUrl,
                    String mCategory, String mDuration, String availableSeats, String description, Date startDate, LatLng location) {
        this.mTitle = title;
        this.mCreatorAge = creatorAge;
        this.mCreatorGender = creatorGender;
        this.mCreatorName = creatorName;
        this.mCreatorNickName = creatorNickName;
        this.mCreatorAvatarUrl = creatorAvatarUrl;
        this.mCategory = mCategory;
        this.mDuration = mDuration;
        this.mAvailableSeats = availableSeats;
        this.mDescription = description;
        this.mStartDate = startDate;
        this.mLocation = location;
    }

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String mTitle) {
        this.mTitle = mTitle;
    }

    public String getCreatorAge() {
        return mCreatorAge;
    }

    public void setCreatorAge(String creatorAge) {
        this.mCreatorAge = creatorAge;
    }

    public String getCreatorGender() {
        return mCreatorGender;
    }

    public void setCreatorGender(String creatorGender) {
        this.mCreatorGender = creatorGender;
    }

    public String getCreatorName() {
        return mCreatorName;
    }

    public void setCreatorName(String creatorName) {
        this.mCreatorName = creatorName;
    }

    public String getCreatorNickName() {
        return mCreatorNickName;
    }

    public void setCreatorNickName(String creatorNickName) {
        this.mCreatorNickName = creatorNickName;
    }

    public String getCreatorAvatarUrl() {
        return mCreatorAvatarUrl;
    }

    public void setCreatorAvatarUrl(String creatorAvatarUrl) {
        this.mCreatorAvatarUrl = creatorAvatarUrl;
    }

    public String getCategory() {
        return mCategory;
    }

    public void setCategory(String category) {
        this.mCategory = category;
    }

    public String getDuration() {
        return mDuration;
    }

    public void setDuration(String duration) {
        this.mDuration = duration;
    }

    public String getAvailableSeats() {
        return mAvailableSeats;
    }

    public void setAvailableSeats(String availableSeats) {
        this.mAvailableSeats = availableSeats;
    }

    public String getDescription() {
        return mDescription;
    }

    public void setDescription(String description) {
        this.mDescription = description;
    }

    public Date getStartDate() {
        return mStartDate;
    }

    public void setStartDate(Date startDate) {
        this.mStartDate = startDate;
    }

    public LatLng getLocation() {
        return mLocation;
    }

    public void setLocation(LatLng location) {
        this.mLocation = location;
    }
}
