package com.example.noam_final;

import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class EventDetailActivity extends AppCompatActivity {
    private ImageButton btnBack;
    private TextView tvEventTitle, tvEventDate, tvEventDescription, tvEventLocation, tvEventStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_detail);

        // Initialize views
        btnBack = findViewById(R.id.btnBack);
        tvEventTitle = findViewById(R.id.tvEventTitle);
        tvEventDate = findViewById(R.id.tvEventDate);
        tvEventDescription = findViewById(R.id.tvEventDescription);
        tvEventLocation = findViewById(R.id.tvEventLocation);
        tvEventStatus = findViewById(R.id.tvEventStatus);

        // Set up back button
        btnBack.setOnClickListener(v -> finish());

        // Load event details
        loadEventDetails();
    }

    private void loadEventDetails() {
        // Assuming event details are passed via Intent
        String title = getIntent().getStringExtra("eventTitle");
        String date = getIntent().getStringExtra("eventDate");
        String description = getIntent().getStringExtra("eventDescription");
        String location = getIntent().getStringExtra("eventLocation");
        boolean isPublic = getIntent().getBooleanExtra("eventIsPublic", false);

        if (title == null || date == null) {
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
    }
}