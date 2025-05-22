package com.example.noam_final;

import com.google.firebase.firestore.DocumentSnapshot;

public class FollowRequestDisplayItem {
    private DocumentSnapshot requestDocument;
    private User targetUser;

    public FollowRequestDisplayItem(DocumentSnapshot requestDocument, User targetUser) {
        this.requestDocument = requestDocument;
        this.targetUser = targetUser;
    }

    public DocumentSnapshot getRequestDocument() {
        return requestDocument;
    }

    public User getTargetUser() {
        return targetUser;
    }
}