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
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseListAdapter;
import com.meetandgo.meetandgo.FirebaseDB;
import com.meetandgo.meetandgo.R;
import com.meetandgo.meetandgo.activities.MainActivity;
import com.meetandgo.meetandgo.data.ChatMessage;
import com.meetandgo.meetandgo.data.Journey;
import com.meetandgo.meetandgo.data.Loc;
import com.meetandgo.meetandgo.data.User;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ChatsFragment extends Fragment {

    @BindView(R.id.msg_button) FloatingActionButton mSendMsgButton;
    @BindView(R.id.input) EditText mTextInput;
    @BindView(R.id.list_of_messages) ListView mListMessages;
    @BindView(R.id.button_lets_go) Button mLetsGoButton;
    @BindView(R.id.button_finish) Button mFinishButton;

    private static final String TAG = "ChatsFragment";
    private View mView;
    private FirebaseListAdapter<ChatMessage> adapter;

    //TODO: Current journey should be given by the previous activity when matching
    private Journey curr_journey;
    private String journey_key;
    private User curr_user;
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
        //curr_journey.setmJid(FirebaseDB.addNewJourney(curr_journey));
        curr_journey.setmJid("Viva Espana");
        journey_key = FirebaseDB.addNewJourney(curr_journey);
        curr_user=FirebaseDB.getCurrentUser();
        curr_user.journeyIDs.add(journey_key);
        FirebaseDB.addUser(curr_user);

        mSendMsgButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String messageText = mTextInput.getText().toString();

                if (messageText.equals("")) return;

                ChatMessage message1 = new ChatMessage(messageText, FirebaseDB.getCurrentUser().getFullName(), FirebaseDB.getCurrentUserUid());
                mListMessages.setTranscriptMode(ListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);

                // Read the input field and push a new instance
                // of ChatMessage to the Firebase database
                FirebaseDB.addMessageToJourney(journey_key, message1);

                // Clear the input
                mTextInput.setText("");

                mSendMsgButton.setBackgroundTintList(ContextCompat.getColorStateList(getActivity(), R.color.colorGrey));
                mSendMsgButton.setEnabled(false);

            }
        });

        setUpEditText();
        setUpBeginJourney();
        setUpFinishJourney();

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
                // i is start, i1 is before
                if (count == 0 && i1 == 1 && i == 0) {
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
    //TODO: Get searchID from current Journey
    private void setUpBeginJourney(){
        mLetsGoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mLetsGoButton.setVisibility(View.GONE);
                //test:
                FirebaseDB.deleteSearch("-L7UbdMfY68vr_CFczoS");
                mFinishButton.setVisibility(View.VISIBLE);
            }
        });
    }

    private void setUpFinishJourney(){
        mFinishButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                curr_journey.deactivateJourney();

                MainActivity activity = (MainActivity) getActivity();
                activity.setSelectedFragmentByMenuItem(R.id.menu_item_4);
            }
        });
    }


    public void onStart() {
        super.onStart();
    }

    private void displayChatMessages() {
        adapter = new FirebaseListAdapter<ChatMessage>(getActivity(), ChatMessage.class,
                R.layout.message, FirebaseDB.getJourneyMessagesReference(journey_key)) {
            @Override
            protected void populateView(View v, ChatMessage model, int position) {
                // Get references to the views of message.xml
                TextView messageText = v.findViewById(R.id.message_text);
                TextView messageUser = v.findViewById(R.id.message_user);
                TextView messageTime = v.findViewById(R.id.message_time);
                RelativeLayout colorLayout = v.findViewById(R.id.color_layout);
                RelativeLayout leftMarginLayout = v.findViewById(R.id.left_margin);
                RelativeLayout rightMarginLayout = v.findViewById(R.id.right_margin);

                // Set their text
                messageText.setText(model.getMessageText());
                messageUser.setText(model.getMessageUser());

                // Format the date before showing it
                messageTime.setText(DateFormat.format("dd-MM-yyyy (HH:mm:ss)", model.getTimestampCreatedLong()));

                // Handle the case when the message if from the current user
                if (model.getMessageUser().equals(FirebaseDB.getCurrentUser().getFullName())) {
                    colorLayout.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.myMessageColor));
                    rightMarginLayout.setVisibility(View.GONE);
                    leftMarginLayout.setVisibility(View.VISIBLE);

                } else {
                    colorLayout.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.othersMessageColor));
                    leftMarginLayout.setVisibility(View.GONE);
                    rightMarginLayout.setVisibility(View.VISIBLE);
                }

            }
        };

        mListMessages.setAdapter(adapter);
    }


}
