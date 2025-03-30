package com.example.noam_final;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.fragment.app.Fragment;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class CalendarFragment extends Fragment {
    private EventManager eventManager;
    private Calendar currentMonth;
    private CalendarAdapter calendarAdapter;
    private TextView monthTitleTextView;
    private GridView calendarGridView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        eventManager = new EventManager();
        currentMonth = Calendar.getInstance();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_calendar, container, false);

        // Initialize views
        monthTitleTextView = rootView.findViewById(R.id.tvMonthTitle);
        calendarGridView = rootView.findViewById(R.id.gridCalendar);
        ImageButton previousMonthButton = rootView.findViewById(R.id.btnPreviousMonth);
        ImageButton nextMonthButton = rootView.findViewById(R.id.btnNextMonth);

        // Set up calendar adapter
        calendarAdapter = new CalendarAdapter(getContext(), currentMonth);
        calendarGridView.setAdapter(calendarAdapter);

        // Update month title
        updateMonthTitle();

        // Set up navigation buttons
        previousMonthButton.setOnClickListener(v -> {
            currentMonth.add(Calendar.MONTH, -1);
            calendarAdapter.updateMonth(currentMonth);
            updateMonthTitle();
        });

        nextMonthButton.setOnClickListener(v -> {
            currentMonth.add(Calendar.MONTH, 1);
            calendarAdapter.updateMonth(currentMonth);
            updateMonthTitle();
        });

        // Set up day click listener
        calendarGridView.setOnItemClickListener((parent, view, position, id) -> {
            Date selectedDate = (Date) calendarAdapter.getItem(position);
            if (selectedDate != null) {
                Calendar selectedCalendar = Calendar.getInstance();
                selectedCalendar.setTime(selectedDate);
                // TODO: Handle day selection (e.g., show events for that day)
            }
        });

        return rootView;
    }

    private void updateMonthTitle() {
        SimpleDateFormat monthFormat = new SimpleDateFormat("MMMM yyyy", Locale.getDefault());
        monthTitleTextView.setText(monthFormat.format(currentMonth.getTime()));
    }
}
