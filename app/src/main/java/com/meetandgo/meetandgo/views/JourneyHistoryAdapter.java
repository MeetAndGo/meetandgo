package com.meetandgo.meetandgo.views;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.meetandgo.meetandgo.R;
import com.meetandgo.meetandgo.data.Journey;

import java.util.ArrayList;

public class JourneyHistoryAdapter extends RecyclerView.Adapter<JourneyHistoryAdapter.ViewHolder> {
    private ArrayList<Journey> mJourneys;

    public void add(Journey o) {
        mJourneys.add(o);
        notifyItemInserted(mJourneys.size()-1);

    }

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView text;

        public ViewHolder(View itemView) {
            super(itemView);
            text = itemView.findViewById(R.id.fromTextView);
        }
    }


    // Provide a suitable constructor (depends on the kind of dataset)
    public JourneyHistoryAdapter(ArrayList<Journey> searchesList) {
        mJourneys = searchesList;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public JourneyHistoryAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                               int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.journey_history_item, parent, false);
        return new ViewHolder(v);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        holder.text.setText(String.valueOf((mJourneys.get(position).getmJid())));

    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mJourneys.size();
    }
}