package com.example.noam_final;

import java.util.ArrayList;
import java.util.List;

public class User {
    private String uid;
    private String email;
    private List<String> groups;
    private List<String> contacts;

    public User() {
        this.groups = new ArrayList<>();
        this.contacts = new ArrayList<>();
    }
    public User(String uid, String email) {
        this.uid = uid;
        this.email = email;
        this.groups = new ArrayList<>();
        this.contacts = new ArrayList<>();
    }

    public String getUid() {
        return uid;
    }

    public String getEmail() {
        return email;
    }

    public List<String> getGroups() {
        return groups;
    }

    public void setGroups(List<String> groups) {
        this.groups = groups;
    }

    // Add a group to the user's list
    public void addGroup(String groupId) {
        if (!groups.contains(groupId)) {
            groups.add(groupId);
        }
    }

    // Remove a group from the user's list
    public void removeGroup(String groupId) {
        groups.remove(groupId);
    }

    public List<String> getContacts() {
        return contacts;
    }

    public void setContacts(List<String> contacts) {
        this.contacts = contacts;
    }

    public void addContact(String contactId) {
        if (!contacts.contains(contactId)) {
            contacts.add(contactId);
        }
    }

    public void removeContact(String contactId) {
        contacts.remove(contactId);
    }
}
