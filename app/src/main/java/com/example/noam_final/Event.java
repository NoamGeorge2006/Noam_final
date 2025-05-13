package com.example.noam_final;

import java.util.HashMap;
import java.util.Map;

public class Event {
    private String id;
    private String title;
    private String description;
    private String date;
    private String location;
    private boolean isPublic;
    private String userId;
    private long createdAt;
    private double latitude;
    private double longitude;

    // Constructor
    public Event(String id, String title, String description, String date, String location, boolean isPublic, String userId, double latitude, double longitude) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.date = date;
        this.location = location;
        this.isPublic = isPublic;
        this.userId = userId;
        this.createdAt = System.currentTimeMillis();
        this.latitude = latitude;
        this.longitude = longitude;
    }

    // Empty constructor for Firebase
    public Event() {}

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public boolean isPublic() {
        return isPublic;
    }

    public void setPublic(boolean isPublic) {
        this.isPublic = isPublic;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    // Convert to Map for Firebase
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("id", id);
        result.put("title", title);
        result.put("description", description);
        result.put("date", date);
        result.put("location", location);
        result.put("isPublic", isPublic);
        result.put("userId", userId);
        result.put("createdAt", createdAt);
        result.put("latitude", latitude);
        result.put("longitude", longitude);
        return result;
    }
}
