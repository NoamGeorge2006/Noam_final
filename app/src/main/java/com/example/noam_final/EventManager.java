package com.example.noam_final;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;


public class EventManager {
    private DatabaseReference database;

    public EventManager() {
        // Initializing Firebase Database
        database = FirebaseDatabase.getInstance().getReference("events");
    }

    public void addEvent(Event event) {
        String eventId = database.push().getKey();
        event.setId(eventId);
        if (eventId != null) {
            database.child(eventId).setValue(event);
        }
    }

    // Method to get all events from Firebase (can be modified for filtering)
    public void getAllEvents(ValueEventListener listener) {
        database.addListenerForSingleValueEvent(listener);
    }

//    דוגמה לשימוש במתודה getAllEvents (לדוגמה בHomePageActivity):
//    eventManager.getAllEvents(new ValueEventListener() {
//        @Override
//        public void onDataChange(@NonNull DataSnapshot snapshot) {
//            List<Event> eventsList = new ArrayList<>();
//            for (DataSnapshot eventSnapshot : snapshot.getChildren()) {
//                Event event = eventSnapshot.getValue(Event.class);
//                if (event != null) {
//                    eventsList.add(event);
//                }
//            }
//            // כאן אפשר לעדכן UI (למשל, להכניס לרשימה ב-RecyclerView)
//        }
//
//        @Override
//        public void onCancelled(@NonNull DatabaseError error) {
//            Log.e("Firebase", "Error loading events: " + error.getMessage());
//        }
//    });


}
