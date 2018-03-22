package com.meetandgo.meetandgo.views;

import android.support.v7.widget.RecyclerView;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.meetandgo.meetandgo.R;
import com.meetandgo.meetandgo.data.Preferences;
import com.meetandgo.meetandgo.data.Search;

import java.util.ArrayList;

public class MatchingResultsAdapter extends RecyclerView.Adapter<MatchingResultsAdapter.ViewHolder> {

    private static final String TAG = "MatchingResultsAdapter";
    private ArrayList<Search> mSearches;
    private OnItemClickListener listener;

    public ArrayList<Search> getSearches() {
        return mSearches;
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public MatchingResultsAdapter(ArrayList<Search> searchesList, OnItemClickListener listener) {
        mSearches = searchesList;
        this.listener = listener;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public MatchingResultsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                                int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.matching_results_item, parent, false);
        return new ViewHolder(v);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        Search search = mSearches.get(position);
        holder.fromTextView.setText(search.getStartLocationString());
        holder.toTextView.setText(search.getEndLocationString());
        holder.numberOfUsersTextView.setText(String.valueOf(search.getAdditionalUsers().size() + 1));
        holder.startTimeTextView.setText(DateFormat.format("dd-MM-yyyy (HH:mm:ss)", (Long) search.getTimeCreated().get("timestamp")));

        if (search.getUserPreferences().getMode() == Preferences.Mode.TAXI) {
            holder.journeyImageView.setImageResource(R.drawable.ic_local_taxi_black_48dp);
        } else if (search.getUserPreferences().getMode() == Preferences.Mode.WALK) {
            holder.journeyImageView.setImageResource(R.drawable.ic_directions_walk_black_48dp);
        }
        holder.bind(mSearches.get(position), listener);

    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mSearches.size();
    }

    /**
     * Set the searches to a passed in variable
     *
     * @param searches The new updated searches to be replaced with.
     */
    public void setListOfSearches(ArrayList<Search> searches) {
        this.mSearches = searches;
        notifyDataSetChanged();
    }

    /**
     * Add search and notify fragment.
     *
     * @param o Search to be added.
     */
    public void add(Search o) {
        mSearches.add(o);
        notifyItemInserted(mSearches.size() - 1);

    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        public TextView fromTextView;
        public TextView toTextView;
        public TextView startTimeTextView;
        public ImageView journeyImageView;
        public TextView numberOfUsersTextView;

        public ViewHolder(View itemView) {
            super(itemView);
            fromTextView = itemView.findViewById(R.id.fromTextView);
            toTextView = itemView.findViewById(R.id.toTextView);
            startTimeTextView = itemView.findViewById(R.id.startTimeTextView);
            journeyImageView = itemView.findViewById(R.id.journeyImageView);
            numberOfUsersTextView = itemView.findViewById(R.id.numberOfPeopleTextView);
        }

        public void bind(final Search search, final OnItemClickListener listener) {
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) {
                    listener.onItemClick(search);
                }
            });
        }
    }
}