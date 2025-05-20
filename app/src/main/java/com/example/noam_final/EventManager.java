package com.example.noam_final;

import android.util.Log;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

public class EventManager {
    private FirebaseFirestore db;
    private static final String TAG = "EventManager";

    public EventManager() {
        db = FirebaseFirestore.getInstance();
    }

    // Add an event to Firestore
    public void addEvent(Event event, OnEventOperationListener listener) {
        db.collection("events")
                .document(event.getId())
                .set(event.toMap())
                .addOnSuccessListener(aVoid -> listener.onSuccess())
                .addOnFailureListener(e -> listener.onFailure(e.getMessage()));
    }

    // Delete an event from Firestore
    public void deleteEvent(String eventId, String userId, OnEventOperationListener listener) {
        Log.d(TAG, "Attempting to delete event: " + eventId + " for user: " + userId);
        
        // First verify that the event belongs to the user
        db.collection("events")
                .document(eventId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Event event = documentSnapshot.toObject(Event.class);
                        if (event != null && event.getUserId().equals(userId)) {
                            // Delete the event
                            db.collection("events")
                                    .document(eventId)
                                    .delete()
                                    .addOnSuccessListener(aVoid -> {
                                        Log.d(TAG, "Event successfully deleted");
                                        listener.onSuccess();
                                    })
                                    .addOnFailureListener(e -> {
                                        Log.e(TAG, "Error deleting event", e);
                                        listener.onFailure(e.getMessage());
                                    });
                        } else {
                            Log.e(TAG, "User does not have permission to delete this event");
                            listener.onFailure("You don't have permission to delete this event");
                        }
                    } else {
                        Log.e(TAG, "Event not found");
                        listener.onFailure("Event not found");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error checking event ownership", e);
                    listener.onFailure(e.getMessage());
                });
    }

    // Get events for a user (only their own events)
    public void getUserEvents(String userId, OnEventsFetchedListener listener) {
        Log.d(TAG, "Querying events for user: " + userId);
        
        // Query only events that belong to this user
        db.collection("events")
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Event> events = new ArrayList<>();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Event event = document.toObject(Event.class);
                        Log.d(TAG, "Found event: " + event.getTitle() + ", UserID: " + event.getUserId());
                        events.add(event);
                    }
                    Log.d(TAG, "Total events fetched for user " + userId + ": " + events.size());
                    listener.onEventsFetched(events);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error fetching events for user " + userId, e);
                    listener.onFailure(e.getMessage());
                });
    }

    // Get public events (events that are marked as public)
    public void getPublicEvents(OnEventsFetchedListener listener) {
        Log.d(TAG, "Querying public events");
        
        db.collection("events")
                .whereEqualTo("isPublic", true)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Event> events = new ArrayList<>();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Event event = document.toObject(Event.class);
                        Log.d(TAG, "Found public event: " + event.getTitle());
                        events.add(event);
                    }
                    Log.d(TAG, "Total public events fetched: " + events.size());
                    listener.onEventsFetched(events);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error fetching public events", e);
                    listener.onFailure(e.getMessage());
                });
    }

    // Helper method to fetch private events
    private void fetchPrivateEvents(String userId, List<Event> events, OnEventsFetchedListener listener) {
        db.collection("events")
                .whereEqualTo("userId", userId)
                .whereEqualTo("isPublic", false)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Event event = document.toObject(Event.class);
                        events.add(event);
                    }
                    listener.onEventsFetched(events);
                })
                .addOnFailureListener(e -> listener.onFailure(e.getMessage()));
    }

    // Listener interfaces
    public interface OnEventOperationListener {
        void onSuccess();
        void onFailure(String error);
    }

    public interface OnEventsFetchedListener {
        void onEventsFetched(List<Event> events);
        void onFailure(String error);
    }
}