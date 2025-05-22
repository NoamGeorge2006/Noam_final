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

    // Get public events from followed users
    public void getFollowedUsersPublicEvents(String userId, List<String> followedUserIds, OnEventsFetchedListener listener) {
        Log.d(TAG, "Querying public events from followed users for user: " + userId);

        if (followedUserIds == null || followedUserIds.isEmpty()) {
            Log.d(TAG, "No followed users found");
            listener.onEventsFetched(new ArrayList<>());
            return;
        }

        // Query public events from followed users
        db.collection("events")
                .whereIn("userId", followedUserIds)
                .whereEqualTo("isPublic", true)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Event> events = new ArrayList<>();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Event event = document.toObject(Event.class);
                        Log.d(TAG, "Found public event from followed user: " + event.getTitle() + 
                            ", UserID: " + event.getUserId() + 
                            ", isPublic: " + event.isPublic());
                        events.add(event);
                    }
                    Log.d(TAG, "Total public events fetched from followed users: " + events.size());
                    listener.onEventsFetched(events);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error fetching public events from followed users", e);
                    listener.onFailure(e.getMessage());
                });
    }

    // Get all events for a user (own events + public events from followed users)
    public void getAllUserEvents(String userId, List<String> followedUserIds, OnEventsFetchedListener listener) {
        Log.d(TAG, "Querying all events for user: " + userId);

        // First get user's own events
        getUserEvents(userId, new OnEventsFetchedListener() {
            @Override
            public void onEventsFetched(List<Event> ownEvents) {
                // Then get public events from followed users
                getFollowedUsersPublicEvents(userId, followedUserIds, new OnEventsFetchedListener() {
                    @Override
                    public void onEventsFetched(List<Event> followedEvents) {
                        // Combine both lists
                        List<Event> allEvents = new ArrayList<>(ownEvents);
                        allEvents.addAll(followedEvents);
                        Log.d(TAG, "Total events (own + followed): " + allEvents.size());
                        listener.onEventsFetched(allEvents);
                    }

                    @Override
                    public void onFailure(String error) {
                        Log.e(TAG, "Error fetching followed users' events: " + error);
                        // Still return own events even if followed users' events failed
                        listener.onEventsFetched(ownEvents);
                    }
                });
            }

            @Override
            public void onFailure(String error) {
                Log.e(TAG, "Error fetching own events: " + error);
                listener.onFailure(error);
            }
        });
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