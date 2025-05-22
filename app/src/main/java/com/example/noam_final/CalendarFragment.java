package com.example.noam_final;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.fragment.app.Fragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class CalendarFragment extends Fragment {
    private EventManager eventManager;
    private Calendar currentMonth;
    private CalendarAdapter calendarAdapter;
    private TextView monthTitleTextView;
    private GridView calendarGridView;
//    private ProgressBar progressBar;
    private HashMap<Date, List<Event>> eventsByDate;
    private FirebaseFirestore db;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        eventManager = new EventManager();
        currentMonth = Calendar.getInstance();
        currentMonth.set(2025, Calendar.MAY, 1); // Set to May 1, 2025
        eventsByDate = new HashMap<>();
        db = FirebaseFirestore.getInstance();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_calendar, container, false);

        // Initialize views
        monthTitleTextView = rootView.findViewById(R.id.tvMonthTitle);
        calendarGridView = rootView.findViewById(R.id.gridCalendar);
//        progressBar = rootView.findViewById(R.id.progressBar);
        ImageButton previousMonthButton = rootView.findViewById(R.id.btnPreviousMonth);
        ImageButton nextMonthButton = rootView.findViewById(R.id.btnNextMonth);

        // Set up calendar adapter
        calendarAdapter = new CalendarAdapter(getContext(), currentMonth, eventsByDate);
        calendarGridView.setAdapter(calendarAdapter);

        // Fetch events
        fetchEvents();

        // Update month title
        updateMonthTitle();

        // Set up navigation buttons
        previousMonthButton.setOnClickListener(v -> {
            currentMonth.add(Calendar.MONTH, -1);
            calendarAdapter.updateMonth(currentMonth);
            updateMonthTitle();
            fetchEvents();
        });

        nextMonthButton.setOnClickListener(v -> {
            currentMonth.add(Calendar.MONTH, 1);
            calendarAdapter.updateMonth(currentMonth);
            updateMonthTitle();
            fetchEvents();
        });

        // Set up day click listener
        calendarGridView.setOnItemClickListener((parent, view, position, id) -> {
            Date selectedDate = (Date) calendarAdapter.getItem(position);
            if (selectedDate != null) {
                Intent intent = new Intent(getActivity(), EventListActivity.class);
                intent.putExtra("selectedDate", selectedDate.getTime());
                startActivity(intent);
            }
        });

        return rootView;
    }

    private void fetchEvents() {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        Log.d("CalendarFragment", "Fetching events for user: " + userId);

        // First get the user's following list
        db.collection("users").document(userId).get()
            .addOnSuccessListener(documentSnapshot -> {
                User currentUser = documentSnapshot.toObject(User.class);
                if (currentUser != null) {
                    List<String> followingList = currentUser.getFollowing();
                    Log.d("CalendarFragment", "User is following " + followingList.size() + " users");

                    // Now fetch all events (own + followed users' public events)
                    eventManager.getAllUserEvents(userId, followingList, new EventManager.OnEventsFetchedListener() {
                        @Override
                        public void onEventsFetched(List<Event> events) {
                            Log.d("CalendarFragment", "Fetched " + events.size() + " total events");
                            eventsByDate.clear();
                            SimpleDateFormat inputFormat = new SimpleDateFormat("d/M/yyyy", Locale.getDefault());
                            SimpleDateFormat outputFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                            
                            for (Event event : events) {
                                try {
                                    String eventDateStr = event.getDate();
                                    Date eventDate = inputFormat.parse(eventDateStr);
                                    String normalizedEventDate = outputFormat.format(eventDate);
                                    Log.d("CalendarFragment", "Event: " + event.getTitle() + 
                                        ", Date: " + eventDateStr + 
                                        ", Normalized: " + normalizedEventDate + 
                                        ", UserID: " + event.getUserId() + 
                                        ", isPublic: " + event.isPublic());
                                    
                                    if (eventDate != null) {
                                        Calendar eventCal = Calendar.getInstance();
                                        eventCal.setTime(eventDate);
                                        Calendar monthCal = (Calendar) currentMonth.clone();
                                        
                                        if (eventCal.get(Calendar.YEAR) == monthCal.get(Calendar.YEAR) &&
                                                eventCal.get(Calendar.MONTH) == monthCal.get(Calendar.MONTH)) {
                                            if (!eventsByDate.containsKey(eventDate)) {
                                                eventsByDate.put(eventDate, new ArrayList<>());
                                            }
                                            eventsByDate.get(eventDate).add(event);
                                            Log.d("CalendarFragment", "Added event to date: " + normalizedEventDate);
                                        } else {
                                            Log.d("CalendarFragment", "Event date " + normalizedEventDate + " not in current month");
                                        }
                                    }
                                } catch (Exception e) {
                                    Log.e("CalendarFragment", "Error parsing event date: " + event.getDate() + ", Error: " + e.getMessage());
                                }
                            }
                            
                            Log.d("CalendarFragment", "Events by date size: " + eventsByDate.size());
                            calendarAdapter.updateEvents(eventsByDate);
                            calendarAdapter.notifyDataSetChanged();
                        }

                        @Override
                        public void onFailure(String error) {
                            Log.e("CalendarFragment", "Error fetching events: " + error);
                            Toast.makeText(getContext(), "Error loading events: " + error, Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            })
            .addOnFailureListener(e -> {
                Log.e("CalendarFragment", "Error fetching user data: " + e.getMessage());
                Toast.makeText(getContext(), "Error loading user data", Toast.LENGTH_SHORT).show();
            });
    }

    private void updateMonthTitle() {
        SimpleDateFormat monthFormat = new SimpleDateFormat("MMMM yyyy", Locale.getDefault());
        monthTitleTextView.setText(monthFormat.format(currentMonth.getTime()));
    }
}