package com.example.noam_final;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.firestore.DocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

public class FollowRequestAdapter extends RecyclerView.Adapter<FollowRequestAdapter.RequestViewHolder> {
    private List<FollowRequestDisplayItem> displayItems;
    private OnRequestActionListener listener;

    public interface OnRequestActionListener {
        void onCancelRequest(DocumentSnapshot requestDocument);
    }

    public FollowRequestAdapter(OnRequestActionListener listener) {
        this.displayItems = new ArrayList<>();
        this.listener = listener;
    }

    public void setRequests(List<FollowRequestDisplayItem> displayItems) {
        this.displayItems = displayItems;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RequestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_follow_request, parent, false);
        return new RequestViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RequestViewHolder holder, int position) {
        holder.bind(displayItems.get(position));
    }

    @Override
    public int getItemCount() {
        return displayItems.size();
    }

    class RequestViewHolder extends RecyclerView.ViewHolder {
        private TextView tvToUserId;
        private Button btnCancel;

        public RequestViewHolder(@NonNull View itemView) {
            super(itemView);
            tvToUserId = itemView.findViewById(R.id.tvToUserId);
            btnCancel = itemView.findViewById(R.id.btnCancel);

            btnCancel.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onCancelRequest(displayItems.get(position).getRequestDocument());
                }
            });
        }

        public void bind(FollowRequestDisplayItem item) {
            if (item != null && item.getTargetUser() != null) {
                String userName = item.getTargetUser().getName();
                String userEmail = item.getTargetUser().getEmail();
                // Display name if available, otherwise display email
                tvToUserId.setText("To: " + (userName != null && !userName.isEmpty() ? userName : userEmail));
            } else if (item != null) {
                 // Display a placeholder if user data could not be fetched
                 tvToUserId.setText("To: Unknown User");
            }
        }
    }
}