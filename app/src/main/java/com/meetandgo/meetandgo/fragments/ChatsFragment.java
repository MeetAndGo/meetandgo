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
import com.meetandgo.meetandgo.FireBaseDB;
import com.meetandgo.meetandgo.R;
import com.meetandgo.meetandgo.activities.MainActivity;
import com.meetandgo.meetandgo.data.ChatMessage;
import com.meetandgo.meetandgo.data.Journey;
import com.meetandgo.meetandgo.data.JourneyHistory;

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
    private FirebaseListAdapter<ChatMessage> adapter;
    private Journey mCurrentJourney;

    // Empty constructor needed for instantiate the new chats fragment.
    public ChatsFragment() {
    }

    public static Fragment newInstance() {
        return new ChatsFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_chats, container, false);
        ButterKnife.bind(this, view);

        setUpEditText();
        MainActivity activity = ((MainActivity) getActivity());
        if (activity == null) return view;

        if (mCurrentJourney != null) {
            setUpJourney(mCurrentJourney);
        } else {
            activity.setChatMenuItemVisibility(false);
        }
        return view;
    }

    private void updateButtonsStatuses() {
        Log.e(TAG, Boolean.toString(mCurrentJourney.isActive()));
        Log.e(TAG, Boolean.toString(mCurrentJourney.isActive()));
        if (mCurrentJourney.isActive() &&
                (FireBaseDB.getCurrentUserID().equals(mCurrentJourney.getUsers().get(0)))) {
            mLetsGoButton.setVisibility(View.VISIBLE);
            mFinishButton.setVisibility(View.GONE);
            setUpBeginJourney();
            setUpFinishJourney();
        } else {
            mLetsGoButton.setVisibility(View.GONE);
            mFinishButton.setVisibility(View.GONE);
        }
    }

    /**
     * Updates the click listener responsible for sending a chat_message_item. This is called every time we
     * change the journey of the chat fragment.
     */
    private void updateClickListener() {
        mSendMsgButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String messageText = mTextInput.getText().toString();
                if (messageText.equals("")) return;

                ChatMessage message1 = new ChatMessage(messageText, FireBaseDB.getCurrentUser().getFullName(), FireBaseDB.getCurrentUserID());
                mListMessages.setTranscriptMode(ListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
                mListMessages.setStackFromBottom(true);
                // Read the input field and push a new instance
                // of ChatMessage to the FireBase database
                FireBaseDB.addMessageToJourney(mCurrentJourney.getJourneyID(), message1);

                // Clear the input
                mTextInput.setText("");

                mSendMsgButton.setBackgroundTintList(ContextCompat.getColorStateList(getActivity(), R.color.colorGrey));
                mSendMsgButton.setEnabled(false);

            }
        });
    }

    /**
     * Method for retrieving serialized data from other fragments
     *
     * @param requestCode code on request
     * @param resultCode  code on result
     * @param data        data retrieved
     */
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, requestCode + " " + resultCode);
        if (requestCode == Constants.JOURNEY_REQUEST_CODE) {
            mCurrentJourney = (Journey) data.getSerializableExtra(Constants.JOURNEY_EXTRA);

        }
    }

    /**
     * Allows the editing of the current chat_message_item you are typing
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

    /**
     * Hides the users keyboard when we are out of the scope
     */
    private void hideKeyboard() {
        View view = getActivity().getCurrentFocus();
        if (view == null) return;
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm == null) return;
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);

    }

    /**
     * When the journey finishes it will update the journey and
     */
    private void setUpFinishJourney() {
        mFinishButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mCurrentJourney.deactivateJourney();
                MainActivity activity = (MainActivity) getActivity();
                activity.setSelectedFragmentByMenuItem(R.id.menu_item_journey_history);
                JourneyHistory.journeyIDs.push(mCurrentJourney);
                FireBaseDB.updateJourney(mCurrentJourney);
                ((MainActivity)getActivity()).runKonfettiAnimation();
                //SerializationUtils sUtils = new SerializationUtils();
                //sUtils.serializeJourneyHistory(getContext());

            }
        });
    }

    /**
     * Performs operations when a journey is started
     */
    private void setUpBeginJourney() {
        mLetsGoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hideKeyboard();
                mLetsGoButton.setVisibility(View.GONE);
                FireBaseDB.deleteSearch(mCurrentJourney.getSearchID());
                mFinishButton.setVisibility(View.VISIBLE);
                ((MainActivity)getActivity()).runKonfettiAnimation();

            }
        });
    }

    /**
     * Displays all chat messages for this journey, and populates the view using the firebase
     * list adapter given by google. This adapter is used for the messages.
     */
    private void displayChatMessages() {
        adapter = new FirebaseListAdapter<ChatMessage>(getActivity(), ChatMessage.class,
                R.layout.chat_message_item, FireBaseDB.getJourneyMessagesReference(mCurrentJourney.getJourneyID())) {
            @Override
            protected void populateView(View v, ChatMessage model, int position) {
                // Get references to the views of chat_message_item.xmle_item.xml
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

                // Handle the case when the chat_message_item if from the current user
                if (model.getMessageUser().equals(FireBaseDB.getCurrentUser().getFullName())) {
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
        if (mCurrentJourney == null) return;
        if (mSendMsgButton == null) return;
        setUpJourney(journey);


    }

    private void setUpJourney(Journey journey) {
        updateButtonsStatuses();
        updateClickListener();
        displayChatMessages();
        ((MainActivity) getActivity()).setChatMenuItemVisibility(true);
    }

    /**
     * Scrolls the listview to the bottom, to always have the last chat_message_item as the focused element
     */
    private void scrollListViewToBottom() {
        mListMessages.post(new Runnable() {
            @Override
            public void run() {
                // Select the last row so it will scroll into view.
                mListMessages.setSelection(adapter.getCount() - 1);
            }
        });
    }


}
