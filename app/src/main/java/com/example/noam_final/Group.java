package com.example.noam_final;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Group {
    private String groupId;
    private String name;
    private String description;
    private boolean isPublic;
    private List<String> events;
    private long createdAt;
    private String code; // New field for group code

    // Constructor
    public Group(String groupId, String name, boolean isPublic, String code) {
        this.groupId = groupId;
        this.name = name;
        this.events = new ArrayList<>();
        this.isPublic = isPublic;
        this.createdAt = System.currentTimeMillis();
        this.code = code;
    }

    // Empty constructor for Firebase
    public Group() {
        this.events = new ArrayList<>();
    }

    // Getters and Setters
    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getEvents() {
        return events;
    }

    public void setEvents(List<String> events) {
        this.events = events;
    }

    public boolean isPublic() {
        return isPublic;
    }

    public void setPublic(boolean isPublic) {
        this.isPublic = isPublic;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    // Add event to group
    public void addEvent(String eventId) {
        if (!events.contains(eventId)) {
            events.add(eventId);
        }
    }

    // Remove event from group
    public void removeEvent(String eventId) {
        events.remove(eventId);
    }

    // Convert to Map for Firebase
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("groupId", groupId);
        result.put("name", name);
        result.put("events", events);
        result.put("isPublic", isPublic);
        result.put("createdAt", createdAt);
        result.put("code", code);
        return result;
    }
}