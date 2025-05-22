package com.example.noam_final;

import java.util.ArrayList;
import java.util.List;

public class User {
    private String uid;
    private String email;
    private String name;
    private boolean isPrivate;
    private List<String> followers;
    private List<String> following;
    private List<String> hiddenEvents;

    public User() {
        this.isPrivate = false;
        this.followers = new ArrayList<>();
        this.following = new ArrayList<>();
        this.hiddenEvents = new ArrayList<>();
    }

    public User(String uid, String email, String name, boolean isPrivate, List<String> followers, List<String> following) {
        this.uid = uid;
        this.email = email;
        this.name = name;
        this.isPrivate = isPrivate;
        this.followers = followers != null ? followers : new ArrayList<>();
        this.following = following != null ? following : new ArrayList<>();
        this.hiddenEvents = new ArrayList<>();
    }

    // Getters
    public String getUid() {
        return uid;
    }

    public String getEmail() {
        return email;
    }

    public boolean isPrivate() {
        return isPrivate;
    }

    public List<String> getFollowers() {
        return followers;
    }

    public List<String> getFollowing() {
        return following;
    }

    public List<String> getHiddenEvents() {
        return hiddenEvents;
    }

    public String getName() {
        return name;
    }

    // Setters
    public void setEmail(String email) {
        this.email = email;
    }

    public void setPrivate(boolean isPrivate) {
        this.isPrivate = isPrivate;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setFollowers(List<String> followers) {
        this.followers = followers;
    }

    public void setFollowing(List<String> following) {
        this.following = following;
    }

    public void setHiddenEvents(List<String> hiddenEvents) {
        this.hiddenEvents = hiddenEvents;
    }

    public void addFollower(String followerId) {
        if (!followers.contains(followerId)) {
            followers.add(followerId);
        }
    }

    public void removeFollower(String followerId) {
        followers.remove(followerId);
    }

    public void addFollowing(String followingId) {
        if (!following.contains(followingId)) {
            following.add(followingId);
        }
    }

    public void removeFollowing(String followingId) {
        following.remove(followingId);
    }

    public void addHiddenEvent(String eventId) {
        if (!hiddenEvents.contains(eventId)) {
            hiddenEvents.add(eventId);
        }
    }

    public void removeHiddenEvent(String eventId) {
        hiddenEvents.remove(eventId);
    }
}