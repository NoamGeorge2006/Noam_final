package com.example.noam_final;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class EventAdapter extends RecyclerView.Adapter<EventAdapter.EventViewHolder> {
    private static final String TAG = "EventAdapter";
    private List<Event> events;
    private OnEventClickListener listener;

    public interface OnEventClickListener {
        void onEventClick(Event event);
    }

    public EventAdapter(List<Event> events, OnEventClickListener listener) {
        this.events = events;
        this.listener = listener;
        Log.d(TAG, "EventAdapter initialized with " + events.size() + " events");
    }

    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_event, parent, false);
        Log.d(TAG, "Creating new ViewHolder");
        return new EventViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        Event event = events.get(position);
        Log.d(TAG, "Binding event at position " + position + ": " + (event != null ? event.getTitle() : "null"));
        holder.bind(event);
    }

    @Override
    public int getItemCount() {
        return events.size();
    }

    class EventViewHolder extends RecyclerView.ViewHolder {
        private TextView tvEventTitle;
        private TextView tvEventDescription;
        private TextView tvEventLocation;
        private TextView tvEventStatus;

        public EventViewHolder(@NonNull View itemView) {
            super(itemView);
            tvEventTitle = itemView.findViewById(R.id.tvEventTitle);
            tvEventDescription = itemView.findViewById(R.id.tvEventDescription);
            tvEventLocation = itemView.findViewById(R.id.tvEventLocation);
            tvEventStatus = itemView.findViewById(R.id.tvEventStatus);

            // Set click listener on the entire item view
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    Log.d(TAG, "Item clicked at position: " + position);
                    if (position != RecyclerView.NO_POSITION && listener != null) {
                        Event event = events.get(position);
                        Log.d(TAG, "Calling listener for event: " + (event != null ? event.getTitle() : "null"));
                        listener.onEventClick(event);
                    } else {
                        Log.e(TAG, "Invalid position or null listener: position=" + position + ", listener=" + (listener != null));
                    }
                }
            });
        }

        public void bind(Event event) {
            if (event != null) {
                tvEventTitle.setText(event.getTitle() != null ? event.getTitle() : "No Title");
                tvEventDescription.setText(event.getDescription() != null ? event.getDescription() : "No Description");
                tvEventLocation.setText(event.getLocation() != null ? event.getLocation() : "No Location");
                tvEventStatus.setText(event.isPublic() ? "Public" : "Private");
                Log.d(TAG, "Bound event: " + event.getTitle());
            } else {
                Log.e(TAG, "Binding null event");
                tvEventTitle.setText("Error");
                tvEventDescription.setText("");
                tvEventLocation.setText("");
                tvEventStatus.setText("");
            }
        }
    }
}