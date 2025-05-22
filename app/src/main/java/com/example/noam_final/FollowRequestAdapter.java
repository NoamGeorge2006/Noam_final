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
    private List<DocumentSnapshot> requests;
    private OnRequestActionListener listener;

    public interface OnRequestActionListener {
        void onCancelRequest(DocumentSnapshot request);
    }

    public FollowRequestAdapter(OnRequestActionListener listener) {
        this.requests = new ArrayList<>();
        this.listener = listener;
    }

    public void setRequests(List<DocumentSnapshot> requests) {
        this.requests = requests;
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
        holder.bind(requests.get(position));
    }

    @Override
    public int getItemCount() {
        return requests.size();
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
                if (position != RecyclerView.NO_POSITION) {
                    listener.onCancelRequest(requests.get(position));
                }
            });
        }

        public void bind(DocumentSnapshot request) {
            String toUserId = request.getString("toUserId");
            tvToUserId.setText("To: " + toUserId);
        }
    }
}