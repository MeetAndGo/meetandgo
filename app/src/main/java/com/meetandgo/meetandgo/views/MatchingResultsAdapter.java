package com.meetandgo.meetandgo.views;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.meetandgo.meetandgo.R;
import com.meetandgo.meetandgo.data.Search;

import java.util.ArrayList;

public class MatchingResultsAdapter extends RecyclerView.Adapter<MatchingResultsAdapter.ViewHolder> {

    private static final String TAG = "MatchingResultsAdapter";

    public interface OnItemClickListener {
        void onItemClick(Search search);
    }
    private ArrayList<Search> mSearches;
    private OnItemClickListener listener;

    public void add(Search o) {
        mSearches.add(o);
        notifyItemInserted(mSearches.size()-1);

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
        holder.text.setText(String.valueOf((mSearches.get(position).getUserId())));
        holder.bind(mSearches.get(position), listener);

    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mSearches.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        private TextView text;
        private ImageView image;

        public ViewHolder(View itemView) {
            super(itemView);
            text = (TextView) itemView.findViewById(R.id.fromTextView);
            image = (ImageView) itemView.findViewById(R.id.walkImageView);
        }

        public void bind(final Search search, final OnItemClickListener listener) {
            text.setText(search.getUserId());
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) {
                    listener.onItemClick(search);
                    //Log.d(TAG, search.toString());
                }
            });
        }
    }
}