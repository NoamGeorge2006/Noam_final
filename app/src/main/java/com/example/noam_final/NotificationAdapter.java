package com.example.noam_final;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder> {
    private List<DocumentSnapshot> notifications;
    private OnNotificationActionListener listener;

    public interface OnNotificationActionListener {
        void onAccept(DocumentSnapshot request);
        void onReject(DocumentSnapshot request);
    }

    public NotificationAdapter(OnNotificationActionListener listener) {
        this.notifications = new ArrayList<>();
        this.listener = listener;
    }

    public void setNotifications(List<DocumentSnapshot> notifications) {
        this.notifications = notifications;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_notification, parent, false);
        return new NotificationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationViewHolder holder, int position) {
        holder.bind(notifications.get(position));
    }

    @Override
    public int getItemCount() {
        return notifications.size();
    }

    class NotificationViewHolder extends RecyclerView.ViewHolder {
        private TextView tvFromUserId;
        private Button btnAccept, btnReject;

        public NotificationViewHolder(@NonNull View itemView) {
            super(itemView);
            tvFromUserId = itemView.findViewById(R.id.tvFromUserId);
            btnAccept = itemView.findViewById(R.id.btnAccept);
            btnReject = itemView.findViewById(R.id.btnReject);

            btnAccept.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    listener.onAccept(notifications.get(position));
                }
            });

            btnReject.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    listener.onReject(notifications.get(position));
                }
            });
        }

        public void bind(DocumentSnapshot notification) {
            String fromUserId = notification.getString("fromUserId");
            // Fetch the user's email for better display
            FirebaseFirestore.getInstance().collection("users").document(fromUserId)
                    .get()
                    .addOnSuccessListener(document -> {
                        User user = document.toObject(User.class);
                        if (user != null && user.getEmail() != null) {
                            tvFromUserId.setText("From: " + user.getEmail());
                        } else {
                            tvFromUserId.setText("From: " + fromUserId);
                        }
                    })
                    .addOnFailureListener(e -> {
                        tvFromUserId.setText("From: " + fromUserId);
                    });
        }
    }
}