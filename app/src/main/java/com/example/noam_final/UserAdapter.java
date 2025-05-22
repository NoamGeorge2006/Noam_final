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
import android.util.Log;
import android.widget.ImageButton;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {
    private static final String TAG = "UserAdapter";
    private List<User> users;
    private OnUserClickListener listener;
    private OnRemoveFollowerClickListener removeListener;

    public interface OnUserClickListener {
        void onUserClick(User user);
    }

    public interface OnRemoveFollowerClickListener {
        void onRemoveFollowerClick(User user);
    }

    public UserAdapter(OnUserClickListener listener) {
        this.users = new ArrayList<>();
        this.listener = listener;
        Log.d(TAG, "UserAdapter initialized with " + users.size() + " events");
    }

    public UserAdapter(List<User> users, OnUserClickListener listener, OnRemoveFollowerClickListener removeListener) {
        this.users = users;
        this.listener = listener;
        this.removeListener = removeListener;
    }

    public void setUsers(List<User> users) {
        this.users = users;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        holder.bind(users.get(position));
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    class UserViewHolder extends RecyclerView.ViewHolder {
        private TextView tvEmail, tvStatus;
        private ImageButton btnRemoveFollower;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            tvEmail = itemView.findViewById(R.id.tvEmail);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            btnRemoveFollower = itemView.findViewById(R.id.btnRemoveFollower);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    listener.onUserClick(users.get(position));
                }
            });

            if (btnRemoveFollower != null && removeListener != null) {
                btnRemoveFollower.setOnClickListener(v -> {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        removeListener.onRemoveFollowerClick(users.get(position));
                    }
                });
            }
        }

        public void bind(User user) {
            if (user != null) {
                tvEmail.setText(user.getEmail() != null ? user.getEmail() : "No Title");
                tvStatus.setText(user.isPrivate() ? "Private" : "Public");
                if (btnRemoveFollower != null) {
                    btnRemoveFollower.setVisibility(removeListener != null ? View.VISIBLE : View.GONE);
                }
            } else {
                Log.e(TAG, "Binding null event");
                tvEmail.setText("Error");
                tvStatus.setText("");
            }
        }
    }
}