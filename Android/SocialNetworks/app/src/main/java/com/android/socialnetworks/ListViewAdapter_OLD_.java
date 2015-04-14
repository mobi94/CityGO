package com.android.socialnetworks;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class ListViewAdapter_OLD_ extends ArrayAdapter<String> {
    private final Activity context;
    private final String[] categories;
    private final String[] titles;
    private final String[] availableSeats;
    private final String[] dates;
    private final String[] times;

    static class ViewHolder {
        public TextView title;
        public TextView seat;
        public TextView date;
        public TextView time;
        public ImageView image;
    }

    public ListViewAdapter_OLD_(Activity context, String[] categories, String[] titles, String[] availableSeats, String[] dates, String[] times) {
        super(context, R.layout.events_list_item, titles);
        this.context = context;
        this.categories = categories;
        this.titles = titles;
        this.availableSeats = availableSeats;
        this.dates = dates;
        this.times = times;
    }

    private int getEventIcon(int categoryIcon){
        switch(categoryIcon){
            case 0: return R.drawable.event_love;
            case 1: return R.drawable.event_movie;
            case 2: return R.drawable.event_sport;
            case 3: return R.drawable.event_business;
            case 4: return R.drawable.event_coffee;
            case 5: return R.drawable.event_meet;
            default: return R.drawable.event_meet;
        }
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View rowView = convertView;
        // reuse views
        if (rowView == null) {
            LayoutInflater inflater = context.getLayoutInflater();
            rowView = inflater.inflate(R.layout.events_list_item, null);
            // configure view holder
            ViewHolder viewHolder = new ViewHolder();
            viewHolder.image = (ImageView) rowView.findViewById(R.id.profile_list_category);
            viewHolder.title = (TextView) rowView.findViewById(R.id.profile_list_title);
            viewHolder.seat = (TextView) rowView.findViewById(R.id.profile_list_available_seats);
            viewHolder.date = (TextView) rowView.findViewById(R.id.profile_list_date);
            viewHolder.time = (TextView) rowView.findViewById(R.id.profile_list_time);
            rowView.setTag(viewHolder);
        }

        // fill data
        ViewHolder holder = (ViewHolder) rowView.getTag();
        holder.image.setImageResource(getEventIcon(Integer.parseInt(categories[position])));
        holder.title.setText(titles[position]);
        holder.seat.setText(availableSeats[position]);
        holder.date.setText(dates[position]);
        holder.time.setText(times[position]);

        return rowView;
    }
}
