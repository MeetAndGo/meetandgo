package com.meetandgo.meetandgo.fragments;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseListAdapter;
import com.meetandgo.meetandgo.FirebaseDB;
import com.meetandgo.meetandgo.R;
import com.meetandgo.meetandgo.data.ChatMessage;
import com.meetandgo.meetandgo.data.Journey;
import com.meetandgo.meetandgo.data.Loc;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ChatsFragment extends Fragment {

    @BindView(R.id.msg_button) FloatingActionButton mSendMsgButton;
    @BindView(R.id.input) EditText mTextInput;
    @BindView(R.id.list_of_messages) ListView mListMessages;

    private static final String TAG = "ChatsFragment";
    private View mView;
    private FirebaseListAdapter<ChatMessage> adapter;

    //TODO: Current journey should be given by the previous activity when matching
    private Journey curr_journey;

    public ChatsFragment() {
    }

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

        final Loc curr_loc = new Loc(12, 13);
        long start = new Date().getTime();
        List<String> users = new ArrayList<String>();
        users.add("Paddy");

        curr_journey = new Journey(curr_loc, start, users);
        curr_journey.setmJid(FirebaseDB.addNewJourney(curr_journey));

        mSendMsgButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String messageText = mTextInput.getText().toString();
                if (messageText.equals("")) return;

                ChatMessage message1 = new ChatMessage(messageText, FirebaseDB.getCurrentUser().fullName, FirebaseDB.getCurrentUserUid());
                mListMessages.setTranscriptMode(ListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);

                // Read the input field and push a new instance
                // of ChatMessage to the Firebase database
                FirebaseDB.addMessageToJourney(curr_journey.getmJid(), message1);

                // Clear the input
                mTextInput.setText("");
            }
        });


        setUpEditText();

        //display message
        displayChatMessages();

        return mView;
    }

    private void setUpEditText() {
        mSendMsgButton.setBackgroundTintList(ContextCompat.getColorStateList(getActivity(), R.color.colorGrey));
        mSendMsgButton.setEnabled(false);
        mTextInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int count) {
                if (count == 0) {
                    mSendMsgButton.setBackgroundTintList(ContextCompat.getColorStateList(getActivity(), R.color.colorGrey));
                    mSendMsgButton.setEnabled(false);
                } else {
                    mSendMsgButton.setBackgroundTintList(ContextCompat.getColorStateList(getActivity(), R.color.colorPrimary));
                    mSendMsgButton.setEnabled(true);
                }

            }

            @Override public void afterTextChanged(Editable editable) {

            }
        });
    }

    public void onStart() {
        super.onStart();
    }

    private void displayChatMessages() {
        adapter = new FirebaseListAdapter<ChatMessage>(getActivity(), ChatMessage.class,
                R.layout.message, FirebaseDB.getJourneyMessagesReference(curr_journey.getmJid())) {
            @Override
            protected void populateView(View v, ChatMessage model, int position) {
                // Get references to the views of message.xml
                TextView messageText = v.findViewById(R.id.message_text);
                TextView messageUser = v.findViewById(R.id.message_user);
                TextView messageTime = v.findViewById(R.id.message_time);

                // Set their text
                messageText.setText(model.getMessageText());
                messageUser.setText(model.getMessageUser());

                // Format the date before showing it
                messageTime.setText(DateFormat.format("dd-MM-yyyy (HH:mm:ss)",
                        model.getMessageTime()));


            }
        };

        mListMessages.setAdapter(adapter);
    }


}
