package com.meetandgo.meetandgo.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseListAdapter;
import com.meetandgo.meetandgo.Constants;
import com.meetandgo.meetandgo.FirebaseDB;
import com.meetandgo.meetandgo.R;
import com.meetandgo.meetandgo.activities.MainActivity;
import com.meetandgo.meetandgo.data.ChatMessage;
import com.meetandgo.meetandgo.data.Journey;
import com.meetandgo.meetandgo.data.JourneyHistory;
import com.meetandgo.meetandgo.utils.SerializationUtils;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ChatsFragment extends Fragment {

    @BindView(R.id.msg_button)
    FloatingActionButton mSendMsgButton;
    @BindView(R.id.input)
    EditText mTextInput;
    @BindView(R.id.list_of_messages)
    ListView mListMessages;
    @BindView(R.id.button_lets_go)
    Button mLetsGoButton;
    @BindView(R.id.button_finish)
    Button mFinishButton;

    private static final String TAG = "ChatsFragment";
    private View mView;
    private FirebaseListAdapter<ChatMessage> adapter;

    //TODO: Current journey should be given by the previous activity when matching
    private Journey mCurrentJourney;

    public ChatsFragment() {
    }

    public static Fragment newInstance() {
        return new ChatsFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mView = inflater.inflate(R.layout.fragment_chats, container, false);
        ButterKnife.bind(this, mView);

        setUpEditText();
        setUpBeginJourney();
        setUpFinishJourney();
        if (mCurrentJourney != null) {
            updateClickListener();
            displayChatMessages();
            ((MainActivity) getActivity()).setChatMenuItemVisibility(true);
        } else {
            ((MainActivity) getActivity()).setChatMenuItemVisibility(false);
        }
        return mView;
    }

    /**
     * Updates the click listener responsible for sending a message.
     */
    private void updateClickListener() {
        mSendMsgButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String messageText = mTextInput.getText().toString();

                if (messageText.equals("")) return;

                ChatMessage message1 = new ChatMessage(messageText, FirebaseDB.getCurrentUser().getFullName(), FirebaseDB.getCurrentUserUid());
                mListMessages.setTranscriptMode(ListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
                mListMessages.setStackFromBottom(true);
                // Read the input field and push a new instance
                // of ChatMessage to the Firebase database
                FirebaseDB.addMessageToJourney(mCurrentJourney.getjId(), message1);

                // Clear the input
                mTextInput.setText("");

                mSendMsgButton.setBackgroundTintList(ContextCompat.getColorStateList(getActivity(), R.color.colorGrey));
                mSendMsgButton.setEnabled(false);

            }
        });
    }

    /**
     * Method for retrieving serialized data from other fragments
     * @param requestCode code on request
     * @param resultCode code on result
     * @param data data retrieved
     */
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, requestCode + " " + resultCode);
        if (requestCode == Constants.JOURNEY_REQUEST_CODE) {
            mCurrentJourney = (Journey) data.getSerializableExtra(Constants.JOURNEY_EXTRA);

        }
    }

    /**
     * Allows the editing of the current message you are typing
     */
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

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
    }

    //TODO: Get searchID from current Journey

    /**
     * Performs operations when a journey is started
     */
    private void setUpBeginJourney() {
        mLetsGoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hideKeyboard();
                mLetsGoButton.setVisibility(View.GONE);
                //test:
                FirebaseDB.deleteSearch("-L7UbdMfY68vr_CFczoS");
                mFinishButton.setVisibility(View.VISIBLE);

            }
        });
    }

    /**
     * Hides the users keyboard
     */
    private void hideKeyboard() {
        View view = getActivity().getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    /**
     * Does operations when a journey is finished
     */
    private void setUpFinishJourney() {
        mFinishButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mCurrentJourney.deactivateJourney();

                MainActivity activity = (MainActivity) getActivity();
                activity.setSelectedFragmentByMenuItem(R.id.menu_item_journey_history);
                JourneyHistory.journeyIDs.push(mCurrentJourney);



                SerializationUtils sUtils = new SerializationUtils();
                sUtils.serializeJourneyHistory(getContext());
            }
        });
    }

    /**
     * Displays all chat messages for this journey
     */
    private void displayChatMessages() {
        adapter = new FirebaseListAdapter<ChatMessage>(getActivity(), ChatMessage.class,
                R.layout.message, FirebaseDB.getJourneyMessagesReference(mCurrentJourney.getjId())) {
            @Override
            protected void populateView(View v, ChatMessage model, int position) {
                // Get references to the views of message.xml
                TextView messageText = v.findViewById(R.id.message_text);
                TextView messageUser = v.findViewById(R.id.message_user);
                TextView messageTime = v.findViewById(R.id.message_time);
                RelativeLayout colorLayout = v.findViewById(R.id.color_layout);
                RelativeLayout leftMarginLayout = v.findViewById(R.id.left_margin);
                RelativeLayout rightMarginLayout = v.findViewById(R.id.right_margin);

                // Set their fromTextView
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
        scrollListViewToBottom();
    }

    /**
     * Sets which journey the user will be chatting with
     *
     * @param journey journey to be set
     */
    public void setJourney(Journey journey) {
        this.mCurrentJourney = journey;
        if (mCurrentJourney != null && mSendMsgButton != null) {
            updateClickListener();
            displayChatMessages();
            ((MainActivity) getActivity()).setChatMenuItemVisibility(true);

        }
    }

    /**
     *  Scrolls the chats to the bottom
     */
    private void scrollListViewToBottom() {
        mListMessages.post(new Runnable() {
            @Override
            public void run() {
                // Select the last row so it will scroll into view...
                mListMessages.setSelection(adapter.getCount() - 1);
            }
        });
    }


}
