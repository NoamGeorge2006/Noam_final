package com.example.noam_final;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FieldValue;

import java.util.ArrayList;
import java.util.List;

public class ContactManager {
    private FirebaseFirestore db;

    public ContactManager() {
        db = FirebaseFirestore.getInstance();
    }

    // Add a contact (link two users)
    public void addContact(String userId, String contactId, OnContactOperationListener listener) {
        DocumentReference userRef = db.collection("users").document(userId);
        userRef.update("contacts", FieldValue.arrayUnion(contactId))
                .addOnSuccessListener(aVoid -> listener.onSuccess())
                .addOnFailureListener(e -> listener.onFailure(e.getMessage()));
    }

    // Remove a contact
    public void removeContact(String userId, String contactId, OnContactOperationListener listener) {
        DocumentReference userRef = db.collection("users").document(userId);
        userRef.update("contacts", FieldValue.arrayRemove(contactId))
                .addOnSuccessListener(aVoid -> listener.onSuccess())
                .addOnFailureListener(e -> listener.onFailure(e.getMessage()));
    }

    // Fetch all contacts for a user
    public void getUserContacts(String userId, OnUserContactsListener listener) {
        db.collection("users")
                .document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        User user = documentSnapshot.toObject(User.class);
                        if (user != null && user.getContacts() != null) {
                            List<Contact> contacts = new ArrayList<>();
                            fetchContactsForUser(user.getContacts(), contacts, 0, listener);
                        } else {
                            listener.onContactsFetched(new ArrayList<>());
                        }
                    } else {
                        listener.onContactsFetched(new ArrayList<>());
                    }
                })
                .addOnFailureListener(e -> listener.onFailure(e.getMessage()));
    }

    // Helper method to fetch contacts recursively
    private void fetchContactsForUser(List<String> contactIds, List<Contact> contacts, int index, OnUserContactsListener listener) {
        if (index >= contactIds.size()) {
            listener.onContactsFetched(contacts);
            return;
        }

        db.collection("users")
                .document(contactIds.get(index))
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        User contactUser = documentSnapshot.toObject(User.class);
                        if (contactUser != null) {
                            Contact contact = new Contact(contactUser.getUid(), contactUser.getEmail());
                            contacts.add(contact);
                        }
                    }
                    fetchContactsForUser(contactIds, contacts, index + 1, listener);
                })
                .addOnFailureListener(e -> listener.onFailure(e.getMessage()));
    }

    // Listener interfaces
    public interface OnContactOperationListener {
        void onSuccess();
        void onFailure(String error);
    }

    public interface OnUserContactsListener {
        void onContactsFetched(List<Contact> contacts);
        void onFailure(String error);
    }
}