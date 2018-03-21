package com.meetandgo.meetandgo.views;

import android.annotation.SuppressLint;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.meetandgo.meetandgo.R;
import com.meetandgo.meetandgo.data.Journey;
import com.meetandgo.meetandgo.data.Preferences;

import java.util.ArrayList;
import java.util.Objects;

public class JourneyHistoryAdapter extends RecyclerView.Adapter<JourneyHistoryAdapter.ViewHolder> {
    private ArrayList<Journey> mJourneys = new ArrayList<>();
    private OnItemClickListener mListener;

    public void add(Journey o) {
        boolean exists = false;
        for (int i = 0; i < mJourneys.size(); i++) {
            Journey j = mJourneys.get(i);
            if (Objects.equals(j.getjId(), o.getjId())) {
                j.update(o);
                notifyItemChanged(i);
                exists = true;
            }
        }
        if (!exists) {
            mJourneys.add(o);
            notifyItemInserted(mJourneys.size() - 1);
        }

    }

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView fromTextView;
        public TextView startTimeTextView;
        public ImageView journeyImageView;
        public TextView numberOfUsersTextView;

        public ViewHolder(View itemView) {
            super(itemView);
            fromTextView = itemView.findViewById(R.id.fromTextView);
            startTimeTextView = itemView.findViewById(R.id.startTimeTextView);
            journeyImageView = itemView.findViewById(R.id.journeyImageView);
            numberOfUsersTextView = itemView.findViewById(R.id.numberOfPeopleTextView);
        }

        public void bind(final Journey journey, final OnItemClickListener listener) {
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) {
                    listener.onItemClick(journey);
                }
            });
        }
    }


    // Provide a suitable constructor (depends on the kind of dataset)
    public JourneyHistoryAdapter(ArrayList<Journey> journeysList, OnItemClickListener listener) {
        mJourneys = journeysList;
        mListener = listener;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public JourneyHistoryAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                               int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.journey_history_item, parent, false);
        return new ViewHolder(v);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        Journey j = mJourneys.get(position);
        holder.fromTextView.setText("Start: " + j.getStartLocationString());
        holder.startTimeTextView.setText(DateFormat.format("dd-MM-yyyy (HH:mm:ss)", j.getStartTime()));
        holder.numberOfUsersTextView.setText(String.valueOf(j.getUsers().size()));

        if (j.getMode() == Preferences.Mode.TAXI){
            holder.journeyImageView.setImageResource(R.drawable.ic_local_taxi_black_48dp);
        }else if (j.getMode() == Preferences.Mode.WALK){
            holder.journeyImageView.setImageResource(R.drawable.ic_directions_walk_black_48dp);

        }
        // Bind the holder for having the click functionality
        holder.bind(mJourneys.get(position), mListener);

    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mJourneys.size();
    }
}