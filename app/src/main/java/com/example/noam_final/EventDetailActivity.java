package com.example.noam_final;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;

public class EventDetailActivity extends AppCompatActivity {
    private ImageButton btnBack;
    private Button btnDelete;
    private TextView tvEventTitle, tvEventDate, tvEventDescription, tvEventLocation, tvEventStatus;
    private EventManager eventManager;
    private String eventId;
    private String eventUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_detail);

        // Initialize views
        btnBack = findViewById(R.id.btnBack);
        btnDelete = findViewById(R.id.btnDelete);
        tvEventTitle = findViewById(R.id.tvEventTitle);
        tvEventDate = findViewById(R.id.tvEventDate);
        tvEventDescription = findViewById(R.id.tvEventDescription);
        tvEventLocation = findViewById(R.id.tvEventLocation);
        tvEventStatus = findViewById(R.id.tvEventStatus);

        // Initialize EventManager
        eventManager = new EventManager();

        // Set up back button
        btnBack.setOnClickListener(v -> finish());

        // Load event details
        loadEventDetails();
    }

    private void showDeleteConfirmationDialog() {
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        boolean isCreator = currentUserId.equals(eventUserId);
        
        String title = isCreator ? "Delete Event" : "Remove from Calendar";
        String message = isCreator ? 
            "Are you sure you want to delete this event? This will remove it from everyone's calendar." :
            "Are you sure you want to remove this event from your calendar? This will only hide it from your view.";
        
        new AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton(isCreator ? "Delete" : "Remove", (dialog, which) -> {
                if (isCreator) {
                    deleteEvent();
                } else {
                    removeFromCalendar();
                }
            })
            .setNegativeButton("Cancel", null)
            .show();
    }

    private void deleteEvent() {
        if (eventId != null) {
            String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            eventManager.deleteEvent(eventId, userId, new EventManager.OnEventOperationListener() {
                @Override
                public void onSuccess() {
                    Log.d("EventDetailActivity", "Event deleted successfully");
                    Toast.makeText(EventDetailActivity.this, "Event deleted successfully", Toast.LENGTH_SHORT).show();
                    finish();
                }

                @Override
                public void onFailure(String error) {
                    Log.e("EventDetailActivity", "Error deleting event: " + error);
                    Toast.makeText(EventDetailActivity.this, "Error deleting event: " + error, Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(this, "Invalid event ID", Toast.LENGTH_SHORT).show();
        }
    }

    private void removeFromCalendar() {
        if (eventId != null) {
            String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            eventManager.removeEventFromCalendar(eventId, userId, new EventManager.OnEventOperationListener() {
                @Override
                public void onSuccess() {
                    Log.d("EventDetailActivity", "Event removed from calendar successfully");
                    Toast.makeText(EventDetailActivity.this, "Event removed from your calendar", Toast.LENGTH_SHORT).show();
                    finish();
                }

                @Override
                public void onFailure(String error) {
                    Log.e("EventDetailActivity", "Error removing event from calendar: " + error);
                    Toast.makeText(EventDetailActivity.this, "Error removing event: " + error, Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(this, "Invalid event ID", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadEventDetails() {
        // Retrieve event details from Intent
        eventId = getIntent().getStringExtra("eventId");
        String title = getIntent().getStringExtra("eventTitle");
        String date = getIntent().getStringExtra("eventDate");
        String description = getIntent().getStringExtra("eventDescription");
        String location = getIntent().getStringExtra("eventLocation");
        boolean isPublic = getIntent().getBooleanExtra("eventIsPublic", false);
        eventUserId = getIntent().getStringExtra("eventUserId");

        if (title == null || date == null) {
            Log.e("EventDetailActivity", "Invalid event data: title or date is null");
            Toast.makeText(this, "Invalid event data", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Set text views
        tvEventTitle.setText(title);
        tvEventDate.setText("Date: " + date);
        tvEventDescription.setText("Description: " + (description != null ? description : "N/A"));
        tvEventLocation.setText("Location: " + (location != null ? location : "N/A"));
        tvEventStatus.setText("Status: " + (isPublic ? "Public" : "Private"));

        // Set up delete button based on whether user is the creator
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        boolean isCreator = currentUserId.equals(eventUserId);
        btnDelete.setText(isCreator ? "Delete Event" : "Remove from Calendar");
        btnDelete.setOnClickListener(v -> showDeleteConfirmationDialog());

        // Log for debugging
        Log.d("EventDetailActivity", "Loaded event: " + title + ", isCreator: " + isCreator);
    }
}