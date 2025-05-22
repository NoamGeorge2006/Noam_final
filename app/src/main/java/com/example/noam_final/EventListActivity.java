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
import com.google.firebase.firestore.FirebaseFirestore;
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
    private FirebaseFirestore db;

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
        Log.d("EventListActivity", "Setting up RecyclerView");
        
        eventAdapter = new EventAdapter(eventList, new EventAdapter.OnEventClickListener() {
            @Override
            public void onEventClick(Event event) {
                Log.d("EventListActivity", "onEventClick called with event: " + (event != null ? event.getTitle() : "null"));
                if (event != null) {
                    try {
                        Intent intent = new Intent(EventListActivity.this, EventDetailActivity.class);
                        intent.putExtra("eventId", event.getId());
                        intent.putExtra("eventTitle", event.getTitle());
                        intent.putExtra("eventDate", event.getDate());
                        intent.putExtra("eventDescription", event.getDescription());
                        intent.putExtra("eventLocation", event.getLocation());
                        intent.putExtra("eventIsPublic", event.isPublic());
                        intent.putExtra("eventLatitude", event.getLatitude());
                        intent.putExtra("eventLongitude", event.getLongitude());
                        intent.putExtra("eventUserId", event.getUserId());
                        
                        Log.d("EventListActivity", "Starting EventDetailActivity with event: " + event.getTitle());
                        startActivity(intent);
                        Log.d("EventListActivity", "Successfully started EventDetailActivity");
                    } catch (Exception e) {
                        Log.e("EventListActivity", "Error starting EventDetailActivity", e);
                        Toast.makeText(EventListActivity.this, "Error opening event details: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Log.e("EventListActivity", "Clicked event is null");
                    Toast.makeText(EventListActivity.this, "Invalid event data", Toast.LENGTH_SHORT).show();
                }
            }
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(eventAdapter);
        recyclerView.setNestedScrollingEnabled(false);
        Log.d("EventListActivity", "RecyclerView setup completed");
    }

    private void initializeFirebase() {
        eventManager = new EventManager();
        db = FirebaseFirestore.getInstance();
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
        
        // First get the user's following list
        db.collection("users").document(userId).get()
            .addOnSuccessListener(documentSnapshot -> {
                User currentUser = documentSnapshot.toObject(User.class);
                if (currentUser != null) {
                    List<String> followingList = currentUser.getFollowing();
                    Log.d("EventListActivity", "User is following " + followingList.size() + " users");

                    // Now fetch all events (own + followed users' public events)
                    eventManager.getAllUserEvents(userId, followingList, new EventManager.OnEventsFetchedListener() {
                        @Override
                        public void onEventsFetched(List<Event> events) {
                            Log.d("EventListActivity", "Fetched " + events.size() + " total events");
                            eventList.clear();
                            SimpleDateFormat inputFormat = new SimpleDateFormat("d/M/yyyy", Locale.getDefault());
                            SimpleDateFormat outputFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                            
                            for (Event event : events) {
                                try {
                                    String eventDateStr = event.getDate();
                                    Date eventDate = inputFormat.parse(eventDateStr);
                                    String normalizedEventDate = outputFormat.format(eventDate);
                                    Log.d("EventListActivity", "Checking event: " + event.getTitle() + 
                                        ", Date: " + eventDateStr + 
                                        ", Normalized: " + normalizedEventDate + 
                                        ", UserID: " + event.getUserId() + 
                                        ", isPublic: " + event.isPublic());
                                    
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
            })
            .addOnFailureListener(e -> {
                Log.e("EventListActivity", "Error fetching user data: " + e.getMessage());
                Toast.makeText(this, "Error loading user data", Toast.LENGTH_SHORT).show();
            });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadEvents(); // Refresh the list when returning to this activity
    }
}