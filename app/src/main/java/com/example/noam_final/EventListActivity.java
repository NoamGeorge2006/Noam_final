package com.example.noam_final;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class EventListActivity extends AppCompatActivity {
    private ImageButton btnBack;
    private TextView tvDateTitle;
    private ListView lvEvents;
    private EventManager eventManager;
    private ArrayAdapter<String> eventsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_list);

        initializeViews();
        setupListeners();
        initializeFirebase();
        loadEvents();
    }

    private void initializeViews() {
        btnBack = findViewById(R.id.btnBack);
        tvDateTitle = findViewById(R.id.tvDateTitle);
        lvEvents = findViewById(R.id.lvEvents);

        eventsAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, new ArrayList<>());
        lvEvents.setAdapter(eventsAdapter);
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());
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
        tvDateTitle.setText(dateFormat.format(selectedDate));

        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        eventManager.getUserEvents(userId, new EventManager.OnEventsFetchedListener() {
            @Override
            public void onEventsFetched(List<Event> events) {
                List<String> eventTitles = new ArrayList<>();
                String selectedDateStr = dateFormat.format(selectedDate);
                for (Event event : events) {
                    if (event.getDate().equals(selectedDateStr)) {
                        eventTitles.add(event.getTitle());
                    }
                }
                eventsAdapter.clear();
                eventsAdapter.addAll(eventTitles);
                eventsAdapter.notifyDataSetChanged();
                if (eventTitles.isEmpty()) {
                    Toast.makeText(EventListActivity.this, "No events for this date", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(String error) {
                Toast.makeText(EventListActivity.this, "Error fetching events: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }
}