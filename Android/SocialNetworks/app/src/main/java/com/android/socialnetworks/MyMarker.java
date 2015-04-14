package com.android.socialnetworks;

import com.google.android.gms.maps.model.LatLng;

import java.util.Date;

public class MyMarker {

    private String mObjectId;
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

    public MyMarker(String objectId, String category, LatLng location, String title,
                    String creatorName, String creatorGender, String availableSeats,
                    String duration, Date startDate) {
        this.mObjectId = objectId;
        this.mCategory = category;
        this.mLocation = location;
        this.mTitle = title;
        this.mCreatorName = creatorName;
        this.mCreatorGender = creatorGender;
        this.mAvailableSeats = availableSeats;
        this.mDuration = duration;
        this.mStartDate = startDate;
    }

    public String getObjectId() {
        return mObjectId;
    }

    public void setObjectId(String objectId) {
        this.mObjectId = objectId;
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
