package com.example.noam_final;

import android.content.Intent;
import android.os.Bundle;
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

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        eventManager = new EventManager();
        currentMonth = Calendar.getInstance();
        eventsByDate = new HashMap<>();
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
//        progressBar.setVisibility(View.VISIBLE);
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        eventManager.getUserEvents(userId, new EventManager.OnEventsFetchedListener() {
            @Override
            public void onEventsFetched(List<Event> events) {
//                progressBar.setVisibility(View.GONE);
                eventsByDate.clear();
                SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                for (Event event : events) {
                    try {
                        Date eventDate = dateFormat.parse(event.getDate());
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
                            }
                        }
                    } catch (Exception e) {
                        Toast.makeText(getContext(), "Error parsing event date: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
                calendarAdapter.updateEvents(eventsByDate);
            }

            @Override
            public void onFailure(String error) {
//                progressBar.setVisibility(View.GONE);
                Toast.makeText(getContext(), "Error fetching events: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateMonthTitle() {
        SimpleDateFormat monthFormat = new SimpleDateFormat("MMMM yyyy", Locale.getDefault());
        monthTitleTextView.setText(monthFormat.format(currentMonth.getTime()));
    }
}