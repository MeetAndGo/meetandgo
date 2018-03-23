package com.meetandgo.meetandgo.views;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.meetandgo.meetandgo.R;
import com.meetandgo.meetandgo.data.User;

import java.util.ArrayList;

public class RatingItemAdapter extends RecyclerView.Adapter<RatingItemAdapter.ViewHolder> {
    private ArrayList<User> mUsers;

    public RatingItemAdapter(ArrayList<User> users) {
        mUsers = users;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.rating_dialog_item, parent, false);
        return new RatingItemAdapter.ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        User search = mUsers.get(position);
        holder.userNameTextView.setText(search.getFullName());

    }

    @Override
    public int getItemCount() {
        return mUsers.size();
    }

    public void addUser(User user) {
        mUsers.add(user);
        notifyItemInserted(mUsers.size());
    }

    public void clean() {
        mUsers.clear();
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView userNameTextView;

        public ViewHolder(View itemView) {
            super(itemView);
            userNameTextView = itemView.findViewById(R.id.user_name);
        }
    }
}
