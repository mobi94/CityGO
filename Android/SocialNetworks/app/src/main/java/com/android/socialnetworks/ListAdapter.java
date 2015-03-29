package com.android.socialnetworks;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.TreeSet;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.LayoutParams;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class ListAdapter extends ArrayAdapter<ListItem> {
    private ArrayList<ListItem> listItems;
    private Context context;
    private Fragment fragment;
    private static final int TYPE_ITEM = 0;
    private static final int TYPE_SEPARATOR = 1;
    private TreeSet<Integer> sectionHeader = new TreeSet<>();

    public ListAdapter(Fragment fragment, Context context, int textViewResourceId, ArrayList<ListItem> listItems) {
        super(context, textViewResourceId, listItems);
        this.listItems = listItems;
        this.context = context;
        this.fragment = fragment;
    }

    @Override
    public int getItemViewType(int position) {
        return sectionHeader.contains(position) ? TYPE_SEPARATOR : TYPE_ITEM;
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    public void addSectionHeaderItem(int newIndex) {
        sectionHeader.add(newIndex);
        notifyDataSetChanged();
    }

    public TreeSet<Integer> getSectionHeader() {
        return sectionHeader;
    }

    public void clearSectionHeaderItem() {
        sectionHeader.clear();
        notifyDataSetChanged();
    }

    @Override
    @SuppressWarnings("deprecation")
    public View getView(int position, View convertView, ViewGroup parent) {
        ListViewHolder holder;
        final ListItem listItem = listItems.get(position);
        if (convertView == null) {
            LayoutInflater vi = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            if (getItemViewType(position) == TYPE_ITEM) {
                convertView = vi.inflate(R.layout.profile_event_list, null);

                RelativeLayout textViewWrap = (RelativeLayout) convertView.findViewById(R.id.profile_list_item);
                ImageView image = (ImageView) convertView.findViewById(R.id.profile_list_category);
                TextView title = (TextView) convertView.findViewById(R.id.profile_list_title);
                TextView seat = (TextView) convertView.findViewById(R.id.profile_list_available_seats);
                TextView date = (TextView) convertView.findViewById(R.id.profile_list_date);
                TextView time = (TextView) convertView.findViewById(R.id.profile_list_time);
                TextView buttonEditEvent = (TextView) convertView.findViewById(R.id.expandable_edit_event);
                TextView buttonShowFollowers = (TextView) convertView.findViewById(R.id.expandable_show_followers);

                holder = new ListViewHolder(image, title, seat, date, time, buttonEditEvent, buttonShowFollowers);

                // setViewWrap IS REQUIRED
                holder.setViewWrap(textViewWrap);
            }
            else {
                    convertView = vi.inflate(R.layout.list_separator, null);
                    LinearLayout separatorViewWrap = (LinearLayout) convertView.findViewById(R.id.list_separator);
                    TextView separatorTitle = (TextView) convertView.findViewById(R.id.separator_title);
                    holder = new ListViewHolder(separatorTitle);

                    // setViewWrap IS REQUIRED
                    holder.setViewWrap(separatorViewWrap);
            }

        } else {
            holder = (ListViewHolder) convertView.getTag();
        }

        if (getItemViewType(position) == TYPE_ITEM) {
            // THIS IS REQUIRED
            holder.getViewWrap().setLayoutParams(new AbsListView.LayoutParams(LayoutParams.MATCH_PARENT, listItem.getCurrentHeight()));
            holder.getImage().setImageResource(getEventIcon(Integer.parseInt(listItem.getCategory())));
            holder.getTitle().setText(listItem.getTitle());
            holder.getSeat().setText(listItem.getAvailableSeat());


            SimpleDateFormat formatDate;
            SimpleDateFormat formatTime;
            if (context.getResources().getConfiguration().locale != Locale.US) {
                formatDate = new SimpleDateFormat("d MMMM, yyyy");
                formatTime = new SimpleDateFormat("'на' HH:mm");
            } else {
                formatDate = new SimpleDateFormat("MMMM dd, yyyy");
                formatTime = new SimpleDateFormat("'at' h:mm a");
            }
            holder.getDate().setText(formatDate.format(listItem.getDate()));
            holder.getTime().setText(formatTime.format(listItem.getDate()));

            holder.getButtonShowFollowers().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(context, "ShowFollowers", Toast.LENGTH_SHORT).show();
                }
            });
            holder.getButtonEditEvent().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    NewEventFragment newEventFragment = new NewEventFragment();
                    Bundle bundle = new Bundle();
                    bundle.putString("USED_FOR", "edit");
                    bundle.putString("MARKER_ID", listItem.getObjectId());
                    newEventFragment.setArguments(bundle);

                    fragment.getFragmentManager().beginTransaction()
                            .setCustomAnimations(
                                    R.anim.slide_in_bottom, R.anim.slide_out_bottom,
                                    R.anim.slide_in_bottom, R.anim.slide_out_bottom)
                            .add(R.id.container, newEventFragment)
                            .addToBackStack(null)
                            .commit();
                }
            });
        }
        else {
            if (position <= listItems.size()-1) {
                Date now = new Date();
                if (listItems.get(position + 1).getDate().getTime() < now.getTime())
                    holder.getSeparatorTitle().setText("Completed Events");
                else
                    holder.getSeparatorTitle().setText("Upcoming Events");
            }
        }
        convertView.setTag(holder);

        // setHolder IS REQUIRED
        listItem.setHolder(holder);

        return convertView;
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
}
