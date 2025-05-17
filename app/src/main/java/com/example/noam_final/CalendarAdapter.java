package com.example.noam_final;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class CalendarAdapter extends BaseAdapter {
    private Context context;
    private ArrayList<Date> dates;
    private Calendar currentMonth;
    private Calendar today;
    private HashMap<Date, List<Event>> eventsByDate;

    // Colors for different states
    private static final int WEEKDAY_COLOR = Color.parseColor("#FFE4E1"); // Misty rose
    private static final int WEEKEND_COLOR = Color.parseColor("#FFB6C1"); // Light pink
    private static final int TEXT_COLOR = Color.BLACK;
    private static final int EMPTY_CELL_COLOR = Color.parseColor("#EEEEEE"); // Light gray

    public CalendarAdapter(Context context, Calendar currentMonth, HashMap<Date, List<Event>> eventsByDate) {
        this.context = context;
        this.currentMonth = currentMonth;
        this.today = Calendar.getInstance();
        this.dates = new ArrayList<>();
        this.eventsByDate = eventsByDate != null ? eventsByDate : new HashMap<>();
        generateDates();
    }

    private void generateDates() {
        dates.clear();
        Calendar monthStart = (Calendar) currentMonth.clone();
        monthStart.set(Calendar.DAY_OF_MONTH, 1);
        int firstDayOfWeek = monthStart.get(Calendar.DAY_OF_WEEK);

        // Add empty cells for days before the first day of the month
        for (int i = 0; i < firstDayOfWeek - 1; i++) {
            dates.add(null);
        }

        // Add all days of the month
        int daysInMonth = currentMonth.getActualMaximum(Calendar.DAY_OF_MONTH);
        for (int i = 1; i <= daysInMonth; i++) {
            Calendar date = (Calendar) currentMonth.clone();
            date.set(Calendar.DAY_OF_MONTH, i);
            dates.add(date.getTime());
        }
    }

    @Override
    public int getCount() {
        return dates.size();
    }

    @Override
    public Object getItem(int position) {
        return dates.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.calendar_cell, parent, false);
        }

        TextView textView = convertView.findViewById(R.id.calendar_cell);
        Date date = dates.get(position);

        if (date != null) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            textView.setText(String.valueOf(calendar.get(Calendar.DAY_OF_MONTH)));

            // Check if it's today
            if (calendar.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
                    calendar.get(Calendar.MONTH) == today.get(Calendar.MONTH) &&
                    calendar.get(Calendar.DAY_OF_MONTH) == today.get(Calendar.DAY_OF_MONTH)) {
                textView.setBackgroundResource(R.drawable.today_cell_background);
            } else {
                // Check if the day has events
                if (eventsByDate.containsKey(date) && !eventsByDate.get(date).isEmpty()) {
                    textView.setBackgroundResource(R.drawable.event_cell_background);
                } else {
                    // Check if it's a weekend (Friday or Saturday)
                    int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
                    if (dayOfWeek == Calendar.FRIDAY || dayOfWeek == Calendar.SATURDAY) {
                        textView.setBackgroundColor(WEEKEND_COLOR);
                    } else {
                        textView.setBackgroundColor(WEEKDAY_COLOR);
                    }
                }
            }
            textView.setTextColor(TEXT_COLOR);
        } else {
            textView.setText("");
            textView.setBackgroundResource(R.drawable.calendar_cell_background);
        }

        return convertView;
    }

    public void updateMonth(Calendar newMonth) {
        this.currentMonth = newMonth;
        generateDates();
        notifyDataSetChanged();
    }

    public void updateEvents(HashMap<Date, List<Event>> newEvents) {
        this.eventsByDate = newEvents != null ? newEvents : new HashMap<>();
        notifyDataSetChanged();
    }
}