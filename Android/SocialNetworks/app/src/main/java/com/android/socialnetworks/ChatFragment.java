package com.android.socialnetworks;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class ChatFragment extends Fragment {

    private static final String ARG_POSITION = "position";

    public static ChatFragment newInstance(int position) {
        ChatFragment f = new ChatFragment();
        Bundle b = new Bundle();
        b.putInt(ARG_POSITION, position);
        f.setArguments(b);
        return f;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.chat_fragment, container, false);

        return rootView;
    }
}