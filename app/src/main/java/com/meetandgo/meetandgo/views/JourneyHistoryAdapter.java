package com.meetandgo.meetandgo.views;

import android.annotation.SuppressLint;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.meetandgo.meetandgo.R;
import com.meetandgo.meetandgo.data.Journey;
import com.meetandgo.meetandgo.data.Preferences;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Objects;

import static com.facebook.FacebookSdk.getApplicationContext;

public class JourneyHistoryAdapter extends RecyclerView.Adapter<JourneyHistoryAdapter.ViewHolder> {
    private ArrayList<Journey> mJourneys = new ArrayList<>();
    private OnItemClickListener mListener;

    /**
     * Provide a suitable constructor (depends on the kind of dataset)
     *
     * @param journeysList Arraylist of journeys
     * @param listener     listener for when an item is called
     */
    public JourneyHistoryAdapter(ArrayList<Journey> journeysList, OnItemClickListener listener) {
        mJourneys = journeysList;
        mListener = listener;
    }

    /**
     * Create new views (invoked by the layout manager)
     *
     * @param parent
     * @param viewType
     * @return
     */
    @Override
    public JourneyHistoryAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                               int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.journey_history_item, parent, false);
        return new ViewHolder(v);
    }

    /**
     * Replace the contents of a view (invoked by the layout manager)
     */
    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        Journey j = mJourneys.get(position);
        holder.fromTextView.setText("Start: " + j.getStartLocationString());
        holder.startTimeTextView.setText(DateFormat.format("dd-MM-yyyy (HH:mm:ss)", j.getStartTime()));
        holder.numberOfUsersTextView.setText(String.valueOf(j.getUsers().size()));

        if (j.getMode() == Preferences.Mode.TAXI) {
            holder.journeyImageView.setImageResource(R.drawable.ic_local_taxi_black_48dp);
        } else if (j.getMode() == Preferences.Mode.WALK) {
            holder.journeyImageView.setImageResource(R.drawable.ic_directions_walk_black_48dp);
        }
        if (j.isActive()) {
            holder.linearLayoutBackground.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.activeJourney));
        } else {
            holder.linearLayoutBackground.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.inactiveJourney));
        }
        // Bind the holder for having the click functionality
        holder.bind(mJourneys.get(position), mListener);

    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mJourneys.size();
    }

    public class CustomComparator implements Comparator<Journey> {
        @Override
        public int compare(Journey j1, Journey j2) {
            return Long.compare(j1.getStartTime(), j2.getStartTime());
        }
    }

    /**
     * Add journey to journey history list by notifying the fragment that a new journey has been
     * added
     *
     * @param o The journey to be added
     */
    public void add(Journey o) {
        boolean exists = false;
        for (int i = 0; i < mJourneys.size(); i++) {
            Journey j = mJourneys.get(i);
            if (Objects.equals(j.getJourneyID(), o.getJourneyID())) {
                j.update(o);
                notifyItemChanged(i);
                exists = true;
            }
        }
        if (!exists) {
            mJourneys.add(o);
            notifyItemInserted(mJourneys.size() - 1);
        }
        orderJourneyList();
    }

    /**
     * Orders journey list based on the custom comparator. In this case we order by date
     */
    private void orderJourneyList() {
        Collections.sort(mJourneys, new CustomComparator());
        Collections.reverse(mJourneys);
    }

    public ArrayList<Journey> getJourneys() {
        return mJourneys;
    }

    public void deleteJourney(Journey journey) {
        for (int i = 0; i < mJourneys.size(); i++) {
            if (mJourneys.get(i).getJourneyID().equals(journey.getJourneyID())) {
                mJourneys.remove(journey);
                notifyItemRemoved(i);
                return;
            }
        }
    }



    /**
     * Provide a reference to the views for each data item
     * Complex data items may need more than one view per item, and
     * you provide access to all the views for a data item in a view holder */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        private TextView fromTextView;
        private TextView startTimeTextView;
        private ImageView journeyImageView;
        private LinearLayout linearLayoutBackground;
        private TextView numberOfUsersTextView;

        private ViewHolder(View itemView) {
            super(itemView);
            fromTextView = itemView.findViewById(R.id.fromTextView);
            startTimeTextView = itemView.findViewById(R.id.startTimeTextView);
            journeyImageView = itemView.findViewById(R.id.journeyImageView);
            linearLayoutBackground = itemView.findViewById(R.id.linearLayoutBackground);
            numberOfUsersTextView = itemView.findViewById(R.id.numberOfPeopleTextView);
        }

        private void bind(final Journey journey, final OnItemClickListener listener) {
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onItemClick(journey);
                }
            });
            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    listener.onItemLongClick(journey);
                    return true;
                }
            });
        }
    }

}