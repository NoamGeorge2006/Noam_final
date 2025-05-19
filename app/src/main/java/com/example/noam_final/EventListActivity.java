package com.example.noam_final;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class EventListActivity extends AppCompatActivity {
    private ImageButton btnBack;
    private TextView tvEventsHeader;
    private RecyclerView recyclerView;
    private EventAdapter eventAdapter;
    private EventManager eventManager;
    private List<Event> eventList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_list);

        initializeViews();
        setupRecyclerView();
        initializeFirebase();
        loadEvents();
    }

    private void initializeViews() {
        btnBack = findViewById(R.id.btnBack);
        tvEventsHeader = findViewById(R.id.tvEventsHeader);
        recyclerView = findViewById(R.id.recyclerViewEvents);

        btnBack.setOnClickListener(v -> finish());
    }

    private void setupRecyclerView() {
        eventList = new ArrayList<>();
        eventAdapter = new EventAdapter(eventList, event -> {
            // Handle event click - navigate to event details
            Intent intent = new Intent(EventListActivity.this, EventDetailActivity.class);
            intent.putExtra("eventTitle", event.getTitle());
            intent.putExtra("eventDate", event.getDate());
            intent.putExtra("eventDescription", event.getDescription());
            intent.putExtra("eventLocation", event.getLocation());
            intent.putExtra("eventIsPublic", event.isPublic());
            startActivity(intent);
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(eventAdapter);
    }

    private void initializeFirebase() {
        eventManager = new EventManager();
    }

    private void loadEvents() {
        long dateMillis = getIntent().getLongExtra("selectedDate", -1);
        if (dateMillis == -1) {
            Toast.makeText(this, "Invalid date", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        Date selectedDate = new Date(dateMillis);
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        String selectedDateStr = dateFormat.format(selectedDate);
        tvEventsHeader.setText("Events for " + selectedDateStr);
        Log.d("EventListActivity", "Loading events for date: " + selectedDateStr);

        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        eventManager.getUserEvents(userId, new EventManager.OnEventsFetchedListener() {
            @Override
            public void onEventsFetched(List<Event> events) {
                Log.d("EventListActivity", "Fetched " + events.size() + " events");
                eventList.clear();
                SimpleDateFormat inputFormat = new SimpleDateFormat("d/M/yyyy", Locale.getDefault());
                SimpleDateFormat outputFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                for (Event event : events) {
                    try {
                        String eventDateStr = event.getDate();
                        Date eventDate = inputFormat.parse(eventDateStr);
                        String normalizedEventDate = outputFormat.format(eventDate);
                        Log.d("EventListActivity", "Checking event: " + event.getTitle() + ", Date: " + eventDateStr + ", Normalized: " + normalizedEventDate + ", isPublic: " + event.isPublic());
                        if (normalizedEventDate.equals(selectedDateStr)) {
                            Log.d("EventListActivity", "Adding event: " + event.getTitle());
                            eventList.add(event);
                        }
                    } catch (Exception e) {
                        Log.e("EventListActivity", "Error parsing event date: " + event.getDate() + ", Error: " + e.getMessage());
                    }
                }
                eventAdapter.notifyDataSetChanged();
                if (eventList.isEmpty()) {
                    Log.d("EventListActivity", "No events for " + selectedDateStr);
                    Toast.makeText(EventListActivity.this, "No events for this date", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(String error) {
                Log.e("EventListActivity", "Error fetching events: " + error);
                Toast.makeText(EventListActivity.this, "Error fetching events: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadEvents(); // Refresh the list when returning to this activity
    }
}