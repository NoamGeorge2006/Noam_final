package com.example.noam_final;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.firestore.PropertyName;

import java.util.HashMap;
import java.util.Map;

public class Event implements Parcelable {
    private String id;
    private String title;
    private String description;
    private String date;
    private String location;
    private boolean isPublic;
    private String userId;
    private double latitude;
    private double longitude;

    public Event() {}

    public Event(String id, String title, String description, String date, String location, boolean isPublic, String userId, double latitude, double longitude) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.date = date;
        this.location = location;
        this.isPublic = isPublic;
        this.userId = userId;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    // Getters
    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getDate() { return date; }
    public String getLocation() { return location; }
    @PropertyName("isPublic")
    public boolean isPublic() { return isPublic; }
    public String getUserId() { return userId; }
    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }

    // Setters
    public void setId(String id) { this.id = id; }
    public void setTitle(String title) { this.title = title; }
    public void setDescription(String description) { this.description = description; }
    public void setDate(String date) { this.date = date; }
    public void setLocation(String location) { this.location = location; }
    @PropertyName("isPublic")
    public void setPublic(boolean isPublic) { this.isPublic = isPublic; }
    public void setUserId(String userId) { this.userId = userId; }
    public void setLatitude(double latitude) { this.latitude = latitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("id", id);
        map.put("title", title);
        map.put("description", description);
        map.put("date", date);
        map.put("location", location);
        map.put("isPublic", isPublic);
        map.put("userId", userId);
        map.put("latitude", latitude);
        map.put("longitude", longitude);
        return map;
    }

    // Parcelable implementation
    protected Event(Parcel in) {
        id = in.readString();
        title = in.readString();
        description = in.readString();
        date = in.readString();
        location = in.readString();
        userId = in.readString();
        isPublic = in.readByte() != 0;
        latitude = in.readDouble();
        longitude = in.readDouble();
    }

    public static final Creator<Event> CREATOR = new Creator<Event>() {
        @Override
        public Event createFromParcel(Parcel in) { return new Event(in); }
        @Override
        public Event[] newArray(int size) { return new Event[size]; }
    };

    @Override
    public int describeContents() { return 0; }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(title);
        dest.writeString(description);
        dest.writeString(date);
        dest.writeString(location);
        dest.writeString(userId);
        dest.writeByte((byte) (isPublic ? 1 : 0));
        dest.writeDouble(latitude);
        dest.writeDouble(longitude);
    }
}