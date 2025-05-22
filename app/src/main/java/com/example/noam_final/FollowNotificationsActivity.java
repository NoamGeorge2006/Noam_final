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
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FollowNotificationsActivity extends AppCompatActivity implements UserAdapter.OnRemoveFollowerClickListener {
    private RecyclerView rvNotifications, rvFollowers, rvFollowing;
    private NotificationAdapter notificationAdapter;
    private UserAdapter followerAdapter;
    private FollowingUserAdapter followingUserAdapter;
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
        rvFollowers = findViewById(R.id.rvFollowers);
        rvFollowing = findViewById(R.id.rvFollowing);
        btnBack = findViewById(R.id.btnBack);

        setupRecyclerViews();

        btnBack.setOnClickListener(v -> finish());
    }

    private void setupRecyclerViews() {
        // Setup for notifications (existing logic)
        notificationAdapter = new NotificationAdapter(new NotificationAdapter.OnNotificationActionListener() {
            @Override
            public void onAccept(DocumentSnapshot request) {
                String fromUserId = request.getString("fromUserId");
                String toUserId = request.getString("toUserId");
                db.collection("users").document(toUserId)
                        .update("followers", FieldValue.arrayUnion(fromUserId))
                        .addOnSuccessListener(aVoid -> {
                            db.collection("users").document(fromUserId)
                                    .update("following", FieldValue.arrayUnion(toUserId))
                                    .addOnSuccessListener(aVoid1 -> {
                                        request.getReference().delete();
                                        loadNotifications();
                                        loadFollowers();
                                    });
                        });
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
        followerAdapter = new UserAdapter(new ArrayList<>(), user -> {
            // Optional: Handle click on a follower (e.g., view their profile)
            Toast.makeText(this, "Viewing " + user.getEmail(), Toast.LENGTH_SHORT).show();
        }, this);
        rvFollowers.setLayoutManager(new LinearLayoutManager(this));
        rvFollowers.setAdapter(followerAdapter);
        loadFollowers();

        // Setup for following
        followingUserAdapter = new FollowingUserAdapter(user -> unfollowUser(user));
        rvFollowing.setLayoutManager(new LinearLayoutManager(this));
        rvFollowing.setAdapter(followingUserAdapter);
        loadFollowing();
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
                                            if (user != null) {
                                                // Explicitly get the 'isPrivate' field from the document
                                                Boolean isPrivateBoolean = doc.getBoolean("isPrivate");
                                                boolean isPrivate = isPrivateBoolean != null ? isPrivateBoolean : false;
                                                user.setPrivate(isPrivate);
                                                followers.add(user);
                                            }
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

    private void loadFollowing() {
        String currentUserId = mAuth.getCurrentUser().getUid();
        db.collection("users").document(currentUserId)
                .get()
                .addOnSuccessListener(document -> {
                    User currentUser = document.toObject(User.class);
                    if (currentUser != null && currentUser.getFollowing() != null) {
                        List<String> followingIds = currentUser.getFollowing();
                        if (!followingIds.isEmpty()) {
                            db.collection("users")
                                    .whereIn("uid", followingIds)
                                    .get()
                                    .addOnSuccessListener(queryDocumentSnapshots -> {
                                        List<User> followingUsers = new ArrayList<>();
                                        for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                                            User user = doc.toObject(User.class);
                                            if (user != null) {
                                                // Explicitly get the 'isPrivate' field from the document
                                                Boolean isPrivateBoolean = doc.getBoolean("isPrivate");
                                                boolean isPrivate = isPrivateBoolean != null ? isPrivateBoolean : false;
                                                user.setPrivate(isPrivate);
                                                followingUsers.add(user);
                                            }
                                        }
                                        followingUserAdapter.setFollowingUsers(followingUsers);
                                    })
                                    .addOnFailureListener(e -> {
                                        Log.e("FollowNotifications", "Error loading following users: " + e.getMessage());
                                        Toast.makeText(this, "Error loading following", Toast.LENGTH_SHORT).show();
                                    });
                        } else {
                            followingUserAdapter.setFollowingUsers(new ArrayList<>());
                        }
                    } else {
                        followingUserAdapter.setFollowingUsers(new ArrayList<>());
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("FollowNotifications", "Error fetching current user for following: " + e.getMessage());
                    Toast.makeText(this, "Error fetching following", Toast.LENGTH_SHORT).show();
                });
    }

    private void unfollowUser(User userToUnfollow) {
        String currentUserId = mAuth.getCurrentUser().getUid();
        String userToUnfollowId = userToUnfollow.getUid();

        // Remove from current user's following list
        db.collection("users").document(currentUserId)
                .update("following", FieldValue.arrayRemove(userToUnfollowId))
                .addOnSuccessListener(aVoid -> {
                    // Remove current user from the other user's followers list
                    db.collection("users").document(userToUnfollowId)
                            .update("followers", FieldValue.arrayRemove(currentUserId))
                            .addOnSuccessListener(aVoid1 -> {
                                Toast.makeText(this, "Unfollowed " + userToUnfollow.getEmail(), Toast.LENGTH_SHORT).show();
                                loadFollowing(); // Refresh the following list
                                loadFollowers(); // Refresh followers list in case the unfollowed user was following back
                                // Consider broadcasting a follow update if necessary for other parts of the app
                            })
                            .addOnFailureListener(e -> {
                                Log.e("FollowNotifications", "Error removing user from followers list: " + e.getMessage());
                                Toast.makeText(this, "Error unfollowing", Toast.LENGTH_SHORT).show();
                            });
                })
                .addOnFailureListener(e -> {
                    Log.e("FollowNotifications", "Error removing user from following list: " + e.getMessage());
                    Toast.makeText(this, "Error unfollowing", Toast.LENGTH_SHORT).show();
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

    @Override
    public void onRemoveFollowerClick(User userToRemove) {
        String currentUserId = mAuth.getCurrentUser().getUid();
        String userToRemoveId = userToRemove.getUid();

        // Remove the user from the current user's followers list
        db.collection("users").document(currentUserId)
                .update("followers", FieldValue.arrayRemove(userToRemoveId))
                .addOnSuccessListener(aVoid -> {
                    // (Optional) Also remove the current user from the other user's following list
                    // This depends on whether unfollowing is a two-way removal or just from your side.
                    // For a complete removal, uncomment the following block:
                    /*
                    db.collection("users").document(userToRemoveId)
                            .update("following", FieldValue.arrayRemove(currentUserId))
                            .addOnSuccessListener(aVoid1 -> {
                                Log.d("FollowNotifications", "Removed user from following list of " + userToRemove.getEmail());
                            })
                            .addOnFailureListener(e -> {
                                Log.e("FollowNotifications", "Error removing user from following list: " + e.getMessage());
                            });
                    */

                    Toast.makeText(this, "Removed " + userToRemove.getEmail() + " as a follower", Toast.LENGTH_SHORT).show();
                    loadFollowers(); // Refresh the followers list
                })
                .addOnFailureListener(e -> {
                    Log.e("FollowNotifications", "Error removing follower: " + e.getMessage());
                    Toast.makeText(this, "Error removing follower", Toast.LENGTH_SHORT).show();
                });
    }
}