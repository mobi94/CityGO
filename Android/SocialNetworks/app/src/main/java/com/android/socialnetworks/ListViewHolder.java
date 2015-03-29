package com.android.socialnetworks;

import android.widget.ImageView;
import android.widget.TextView;

import com.leocardz.aelv.library.AelvListViewHolder;

public class ListViewHolder extends AelvListViewHolder {

    private TextView title;
    private TextView seat;
    private TextView date;
    private TextView time;
    private ImageView image;
    private TextView buttonEditEvent;
    private TextView buttonShowFollowers;
    private TextView separatorTitle;

    public ListViewHolder(ImageView image, TextView title, TextView seat, TextView date, TextView time,
                          TextView buttonEditEvent, TextView buttonShowFollowers) {
        super();
        this.image = image;
        this.title = title;
        this.seat = seat;
        this.date = date;
        this.time = time;
        this.buttonEditEvent = buttonEditEvent;
        this.buttonShowFollowers = buttonShowFollowers;
    }

    public ListViewHolder(TextView separatorTitle) {
        super();
        this.separatorTitle = separatorTitle;
    }

    public TextView getTitle() {
        return title;
    }

    public void setTitle(TextView title) {
        this.title = title;
    }

    public TextView getSeat() {
        return seat;
    }

    public void setSeat(TextView seat) {
        this.seat = seat;
    }

    public TextView getDate() {
        return date;
    }

    public void setDate(TextView date) {
        this.date = date;
    }

    public TextView getTime() {
        return time;
    }

    public void setTime(TextView time) {
        this.time = time;
    }

    public ImageView getImage() {
        return image;
    }

    public void setImage(ImageView image) {
        this.image = image;
    }


    public TextView getButtonEditEvent() {
        return buttonEditEvent;
    }

    public void setButtonEditEvent(TextView buttonEditEvent) {
        this.buttonEditEvent = buttonEditEvent;
    }

    public TextView getButtonShowFollowers() {
        return buttonShowFollowers;
    }

    public void setButtonShowFollowers(TextView buttonShowFollowers) {
        this.buttonShowFollowers = buttonShowFollowers;
    }

    public TextView getSeparatorTitle() {
        return separatorTitle;
    }

}
