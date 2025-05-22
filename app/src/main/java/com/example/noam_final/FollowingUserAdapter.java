package com.example.noam_final;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class FollowingUserAdapter extends RecyclerView.Adapter<FollowingUserAdapter.FollowingUserViewHolder> {
    private List<User> followingUsers;
    private OnUnfollowClickListener listener;

    public interface OnUnfollowClickListener {
        void onUnfollowClick(User user);
    }

    public FollowingUserAdapter(OnUnfollowClickListener listener) {
        this.followingUsers = new ArrayList<>();
        this.listener = listener;
    }

    public void setFollowingUsers(List<User> users) {
        this.followingUsers = users;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public FollowingUserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_following_user, parent, false);
        return new FollowingUserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FollowingUserViewHolder holder, int position) {
        holder.bind(followingUsers.get(position));
    }

    @Override
    public int getItemCount() {
        return followingUsers.size();
    }

    class FollowingUserViewHolder extends RecyclerView.ViewHolder {
        private TextView tvEmail, tvStatus;
        private Button btnUnfollow;

        public FollowingUserViewHolder(@NonNull View itemView) {
            super(itemView);
            tvEmail = itemView.findViewById(R.id.tvEmail);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            btnUnfollow = itemView.findViewById(R.id.btnUnfollow);

            btnUnfollow.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onUnfollowClick(followingUsers.get(position));
                }
            });
        }

        public void bind(User user) {
            if (user != null) {
                tvEmail.setText(user.getEmail() != null ? user.getEmail() : "No Email");
                tvStatus.setText(user.isPrivate() ? "Private" : "Public");
            }
        }
    }
}