package com.example.noam_final;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FollowNotificationsActivity extends AppCompatActivity {
    private RecyclerView rvNotifications, rvFollowers;
    private NotificationAdapter notificationAdapter;
    private UserAdapter followerAdapter;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private ImageButton btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_follow_notifications);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        rvNotifications = findViewById(R.id.rvNotifications);
        rvFollowers = findViewById(R.id.rvFollowers); // New RecyclerView for followers
        btnBack = findViewById(R.id.btnBack);

        setupRecyclerViews();

        btnBack.setOnClickListener(v -> finish());
    }

    private void setupRecyclerViews() {
        // Setup for notifications (existing logic)
        notificationAdapter = new NotificationAdapter(new NotificationAdapter.OnNotificationActionListener() {
            @Override
            public void onAccept(DocumentSnapshot request) {
                handleRequest(request, "accepted");
            }

            @Override
            public void onReject(DocumentSnapshot request) {
                handleRequest(request, "rejected");
            }
        });
        rvNotifications.setLayoutManager(new LinearLayoutManager(this));
        rvNotifications.setAdapter(notificationAdapter);
        loadNotifications();

        // Setup for followers
        followerAdapter = new UserAdapter(user -> {
            // Optional: Handle click on a follower (e.g., view their profile)
            Toast.makeText(this, "Viewing " + user.getEmail(), Toast.LENGTH_SHORT).show();
        });
        rvFollowers.setLayoutManager(new LinearLayoutManager(this));
        rvFollowers.setAdapter(followerAdapter);
        loadFollowers();
    }

    private void loadNotifications() {
        String currentUserId = mAuth.getCurrentUser().getUid();
        db.collection("follow_requests")
                .whereEqualTo("toUserId", currentUserId)
                .whereEqualTo("status", "pending")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<DocumentSnapshot> notifications = queryDocumentSnapshots.getDocuments();
                    notificationAdapter.setNotifications(notifications);
                })
                .addOnFailureListener(e -> {
                    Log.e("FollowNotifications", "Error loading notifications: " + e.getMessage());
                    Toast.makeText(this, "Error loading notifications", Toast.LENGTH_SHORT).show();
                });
    }

    private void loadFollowers() {
        String currentUserId = mAuth.getCurrentUser().getUid();
        db.collection("users").document(currentUserId)
                .get()
                .addOnSuccessListener(document -> {
                    User currentUser = document.toObject(User.class);
                    if (currentUser != null && currentUser.getFollowers() != null) {
                        List<String> followerIds = currentUser.getFollowers();
                        if (!followerIds.isEmpty()) {
                            db.collection("users")
                                    .whereIn("uid", followerIds)
                                    .get()
                                    .addOnSuccessListener(queryDocumentSnapshots -> {
                                        List<User> followers = new ArrayList<>();
                                        for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                                            User user = doc.toObject(User.class);
                                            followers.add(user);
                                        }
                                        followerAdapter.setUsers(followers);
                                    })
                                    .addOnFailureListener(e -> {
                                        Log.e("FollowNotifications", "Error loading followers: " + e.getMessage());
                                        Toast.makeText(this, "Error loading followers", Toast.LENGTH_SHORT).show();
                                    });
                        } else {
                            followerAdapter.setUsers(new ArrayList<>());
                        }
                    } else {
                        followerAdapter.setUsers(new ArrayList<>());
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("FollowNotifications", "Error fetching current user: " + e.getMessage());
                    Toast.makeText(this, "Error fetching followers", Toast.LENGTH_SHORT).show();
                });
    }

    private void handleRequest(DocumentSnapshot request, String status) {
        String fromUserId = request.getString("fromUserId");
        String toUserId = request.getString("toUserId");

        Map<String, Object> updates = new HashMap<>();
        updates.put("status", status);
        request.getReference().update(updates)
                .addOnSuccessListener(aVoid -> {
                    if (status.equals("accepted")) {
                        db.collection("users").document(toUserId)
                                .get()
                                .addOnSuccessListener(document -> {
                                    User toUser = document.toObject(User.class);
                                    if (toUser != null) {
                                        toUser.addFollower(fromUserId);
                                        db.collection("users").document(toUserId)
                                                .update("followers", toUser.getFollowers())
                                                .addOnSuccessListener(aVoid1 -> {
                                                    db.collection("users").document(fromUserId)
                                                            .get()
                                                            .addOnSuccessListener(doc -> {
                                                                User fromUser = doc.toObject(User.class);
                                                                if (fromUser != null) {
                                                                    fromUser.addFollowing(toUserId);
                                                                    db.collection("users").document(fromUserId)
                                                                            .update("following", fromUser.getFollowing())
                                                                            .addOnSuccessListener(aVoid2 -> {
                                                                                Toast.makeText(this, "Request accepted", Toast.LENGTH_SHORT).show();
                                                                                loadNotifications();
                                                                                loadFollowers(); // Refresh followers list
                                                                                // Send broadcast to notify CalendarFragment
                                                                                Intent intent = new Intent("com.example.noam_final.FOLLOW_UPDATE");
                                                                                LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
                                                                            });
                                                                }
                                                            });
                                                });
                                    }
                                });
                    } else {
                        Toast.makeText(this, "Request rejected", Toast.LENGTH_SHORT).show();
                        loadNotifications();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("FollowNotifications", "Error updating request: " + e.getMessage());
                    Toast.makeText(this, "Error handling request", Toast.LENGTH_SHORT).show();
                });
    }
}