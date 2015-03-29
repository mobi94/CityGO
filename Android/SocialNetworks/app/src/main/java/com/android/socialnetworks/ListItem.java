package com.android.socialnetworks;

import com.leocardz.aelv.library.AelvListItem;

import java.util.Date;

public class ListItem extends AelvListItem {

    private String category;
    private String title;
    private String availableSeat;
    private Date date;
    private String objectId;
    private String creatorName;

    public ListItem(String category, String title, String availableSeat, Date date, String objectId) {
        super();
        this.category = category;
        this.title = title;
        this.availableSeat = availableSeat;
        this.date = date;
        this.objectId = objectId;
    }

    public ListItem() {
        super();
    }

    public ListItem(String category, String title, String availableSeat, Date date, String objectId, String creatorName) {
        super();
        this.category = category;
        this.title = title;
        this.availableSeat = availableSeat;
        this.date = date;
        this.objectId = objectId;
        this.creatorName = creatorName;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAvailableSeat() {
        return availableSeat;
    }

    public void setAvailableSeat(String availableSeat) {
        this.availableSeat = availableSeat;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getObjectId() {
        return objectId;
    }

    public void setObjectId(String objectId) {
        this.objectId = objectId;
    }

    public String getCreatorName() {
        return creatorName;
    }

    public void setCreatorName(String creatorName) {
        this.creatorName = creatorName;
    }
}
