package com.example.noam_final;

import java.util.ArrayList;
import java.util.List;

public class User {
    private String uid;
    private String email;
    private boolean isPrivate;
    private List<String> groups;
    private List<String> contacts;
    private List<String> followers;
    private List<String> following;

    public User() {
        this.isPrivate = false;
        this.groups = new ArrayList<>();
        this.contacts = new ArrayList<>();
        this.followers = new ArrayList<>();
        this.following = new ArrayList<>();
    }

    public User(String uid, String email, boolean isPrivate) {
        this.uid = uid;
        this.email = email;
        this.isPrivate = isPrivate; // Updated to isPrivate
        this.groups = new ArrayList<>();
        this.contacts = new ArrayList<>();
        this.followers = new ArrayList<>();
        this.following = new ArrayList<>();
    }

    // Getters
    public String getUid() {
        return uid;
    }

    public String getEmail() {
        return email;
    }

    public boolean isPrivate() { // Updated to isPrivate
        return isPrivate;
    }

    public List<String> getGroups() {
        return groups;
    }

    public List<String> getContacts() {
        return contacts;
    }

    public List<String> getFollowers() {
        return followers;
    }

    public List<String> getFollowing() {
        return following;
    }

    // Setters
    public void setEmail(String email) {
        this.email = email;
    }

    public void setPrivate(boolean isPrivate) {
        this.isPrivate = isPrivate;
    }

    public void setGroups(List<String> groups) {
        this.groups = groups;
    }

    public void setContacts(List<String> contacts) {
        this.contacts = contacts;
    }

    public void setFollowers(List<String> followers) {
        this.followers = followers;
    }

    public void setFollowing(List<String> following) {
        this.following = following;
    }

    // Add/remove methods
    public void addGroup(String groupId) {
        if (!groups.contains(groupId)) {
            groups.add(groupId);
        }
    }

    public void removeGroup(String groupId) {
        groups.remove(groupId);
    }

    public void addContact(String contactId) {
        if (!contacts.contains(contactId)) {
            contacts.add(contactId);
        }
    }

    public void removeContact(String contactId) {
        contacts.remove(contactId);
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
}