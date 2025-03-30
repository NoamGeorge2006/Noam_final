package com.example.noam_final;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Month {
    private String monthName;
    private int year;
    private int daysInMonth;
    private HashMap<Integer, List<Event>> eventsByDay; // מיפוי בין מספר יום לבין אירועים

    public Month(String monthName, int year, int daysInMonth) {
        this.monthName = monthName;
        this.year = year;
        this.daysInMonth = daysInMonth;
        this.eventsByDay = new HashMap<>();

        // אתחול של כל הימים במערך
        for (int i = 1; i <= daysInMonth; i++) {
            eventsByDay.put(i, new ArrayList<>());
        }
    }

    public String getMonthName() {
        return monthName;
    }

    public int getYear() {
        return year;
    }

    public int getDaysInMonth() {
        return daysInMonth;
    }

    public List<Event> getEventsForDay(int day) {
        return eventsByDay.getOrDefault(day, new ArrayList<>());
    }

    public void addEvent(int day, Event event) {
        eventsByDay.get(day).add(event);
    }
}
