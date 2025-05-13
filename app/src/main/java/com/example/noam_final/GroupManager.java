package com.example.noam_final;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class GroupManager {
    private FirebaseFirestore db;

    public GroupManager() {
        db = FirebaseFirestore.getInstance();
    }

    // Create a new group
    public void createGroup(Group group, OnGroupOperationListener listener) {
        db.collection("groups")
                .document(group.getGroupId())
                .set(group.toMap())
                .addOnSuccessListener(aVoid -> listener.onSuccess())
                .addOnFailureListener(e -> listener.onFailure(e.getMessage()));
    }

    // Join a group (update user's groups list)
    public void joinGroup(String userId, String groupId, OnGroupOperationListener listener) {
        // Update the user's groups field
        DocumentReference userRef = db.collection("users").document(userId);
        userRef.update("groups", FieldValue.arrayUnion(groupId))
                .addOnSuccessListener(aVoid -> listener.onSuccess())
                .addOnFailureListener(e -> listener.onFailure(e.getMessage()));
    }

    // Leave a group (remove group from user's groups list)
    public void leaveGroup(String userId, String groupId, OnGroupOperationListener listener) {
        DocumentReference userRef = db.collection("users").document(userId);
        userRef.update("groups", FieldValue.arrayRemove(groupId))
                .addOnSuccessListener(aVoid -> listener.onSuccess())
                .addOnFailureListener(e -> listener.onFailure(e.getMessage()));
    }

    // Fetch events for a group
    public void getGroupEvents(String groupId, OnGroupEventsListener listener) {
        db.collection("groups")
                .document(groupId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Group group = documentSnapshot.toObject(Group.class);
                        if (group != null && group.getEvents() != null) {
                            List<Event> events = new ArrayList<>();
                            fetchEventsForGroup(group.getEvents(), events, 0, listener);
                        } else {
                            listener.onEventsFetched(new ArrayList<>());
                        }
                    } else {
                        listener.onEventsFetched(new ArrayList<>());
                    }
                })
                .addOnFailureListener(e -> listener.onFailure(e.getMessage()));
    }

    // Helper method to fetch events recursively
    private void fetchEventsForGroup(List<String> eventIds, List<Event> events, int index, OnGroupEventsListener listener) {
        if (index >= eventIds.size()) {
            listener.onEventsFetched(events);
            return;
        }

        db.collection("events")
                .document(eventIds.get(index))
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Event event = documentSnapshot.toObject(Event.class);
                        if (event != null) {
                            events.add(event);
                        }
                    }
                    fetchEventsForGroup(eventIds, events, index + 1, listener);
                })
                .addOnFailureListener(e -> listener.onFailure(e.getMessage()));
    }

    // Fetch all groups a user is part of
    public void getUserGroups(String userId, OnUserGroupsListener listener) {
        db.collection("users")
                .document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        User user = documentSnapshot.toObject(User.class);
                        if (user != null && user.getGroups() != null) {
                            List<Group> groups = new ArrayList<>();
                            fetchGroupsForUser(user.getGroups(), groups, 0, listener);
                        } else {
                            listener.onGroupsFetched(new ArrayList<>());
                        }
                    } else {
                        listener.onGroupsFetched(new ArrayList<>());
                    }
                })
                .addOnFailureListener(e -> listener.onFailure(e.getMessage()));
    }

    // Helper method to fetch groups recursively
    private void fetchGroupsForUser(List<String> groupIds, List<Group> groups, int index, OnUserGroupsListener listener) {
        if (index >= groupIds.size()) {
            listener.onGroupsFetched(groups);
            return;
        }

        db.collection("groups")
                .document(groupIds.get(index))
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Group group = documentSnapshot.toObject(Group.class);
                        if (group != null) {
                            groups.add(group);
                        }
                    }
                    fetchGroupsForUser(groupIds, groups, index + 1, listener);
                })
                .addOnFailureListener(e -> listener.onFailure(e.getMessage()));
    }

    // Fetch all public groups
    public void getPublicGroups(OnUserGroupsListener listener) {
        db.collection("groups")
                .whereEqualTo("isPublic", true)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Group> groups = new ArrayList<>();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Group group = document.toObject(Group.class);
                        groups.add(group);
                    }
                    listener.onGroupsFetched(groups);
                })
                .addOnFailureListener(e -> listener.onFailure(e.getMessage()));
    }

    // Listener interfaces for callbacks
    public interface OnGroupOperationListener {
        void onSuccess();
        void onFailure(String error);
    }

    public interface OnGroupEventsListener {
        void onEventsFetched(List<Event> events);
        void onFailure(String error);
    }

    public interface OnUserGroupsListener {
        void onGroupsFetched(List<Group> groups);
        void onFailure(String error);
    }
}