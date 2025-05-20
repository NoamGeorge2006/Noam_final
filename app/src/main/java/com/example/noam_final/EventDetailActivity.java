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

        // Set up delete button
        btnDelete.setOnClickListener(v -> showDeleteConfirmationDialog());

        // Load event details
        loadEventDetails();
    }

    private void showDeleteConfirmationDialog() {
        new AlertDialog.Builder(this)
            .setTitle("Delete Event")
            .setMessage("Are you sure you want to delete this event?")
            .setPositiveButton("Delete", (dialog, which) -> deleteEvent())
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

    private void loadEventDetails() {
        // Retrieve event details from Intent
        eventId = getIntent().getStringExtra("eventId");
        String title = getIntent().getStringExtra("eventTitle");
        String date = getIntent().getStringExtra("eventDate");
        String description = getIntent().getStringExtra("eventDescription");
        String location = getIntent().getStringExtra("eventLocation");
        boolean isPublic = getIntent().getBooleanExtra("eventIsPublic", false);

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

        // Log for debugging
        Log.d("EventDetailActivity", "Loaded event: " + title);
    }
}