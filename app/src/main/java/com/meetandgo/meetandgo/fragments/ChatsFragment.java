package com.meetandgo.meetandgo.fragments;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.meetandgo.meetandgo.R;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ChatsFragment extends Fragment {

    @BindView(R.id.msg_button) FloatingActionButton mSendMsgButton;
    @BindView(R.id.input) EditText mTextInput;

    private static final String TAG = "ChatsFragment";
    private View mView;

    public ChatsFragment() {}

    public static Fragment newInstance() {
        ChatsFragment fragment = new ChatsFragment();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mView = inflater.inflate(R.layout.fragment_chats, container, false);
        ButterKnife.bind(this, mView);

        mSendMsgButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText input = mTextInput;

                //FirebaseDB.addMessageToJourney(, input)
            }
        });

        return mView;
    }

    public void onStart() {
        super.onStart();
    }


}
