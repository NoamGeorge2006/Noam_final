package com.example.noam_final;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
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
import java.util.UUID;

public class FollowRequestsActivity extends AppCompatActivity {
    private EditText etSearchUser;
    private Button btnSearch;
    private ImageButton btnBack;
    private RecyclerView rvUsers, rvPendingRequests;
    private UserAdapter userAdapter;
    private FollowRequestAdapter requestAdapter;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_follow_requests);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        etSearchUser = findViewById(R.id.etSearchUser);
        btnSearch = findViewById(R.id.btnSearch);
        btnBack = findViewById(R.id.btnBack);
        rvUsers = findViewById(R.id.rvUsers);
        rvPendingRequests = findViewById(R.id.rvPendingRequests);

        setupRecyclerViews();

        btnSearch.setOnClickListener(v -> searchUsers());
        btnBack.setOnClickListener(v -> finish());
    }

    private void setupRecyclerViews() {
        userAdapter = new UserAdapter(user -> sendFollowRequest(user));
        rvUsers.setLayoutManager(new LinearLayoutManager(this));
        rvUsers.setAdapter(userAdapter);

        requestAdapter = new FollowRequestAdapter(request -> cancelFollowRequest(request));
        rvPendingRequests.setLayoutManager(new LinearLayoutManager(this));
        rvPendingRequests.setAdapter(requestAdapter);

        loadPendingRequests();
    }

    private void searchUsers() {
        String searchQuery = etSearchUser.getText().toString().trim();
        if (searchQuery.isEmpty()) {
            Toast.makeText(this, "Please enter an email to search", Toast.LENGTH_SHORT).show();
            return;
        }

        String currentUserId = mAuth.getCurrentUser().getUid();
        db.collection("users")
                .whereEqualTo("email", searchQuery)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<User> users = new ArrayList<>();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        User user = document.toObject(User.class);
                        if (user != null) {
                            // Explicitly get the 'isPrivate' field from the document
                            Boolean isPrivateBoolean = document.getBoolean("isPrivate");
                            boolean isPrivate = isPrivateBoolean != null ? isPrivateBoolean : false;
                            user.setPrivate(isPrivate);

                            if (!user.getUid().equals(currentUserId)) {
                                users.add(user);
                            }
                        }
                    }
                    userAdapter.setUsers(users);
                    if (users.isEmpty()) {
                        Toast.makeText(this, "No users found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("FollowRequestsActivity", "Error searching users: " + e.getMessage());
                    Toast.makeText(this, "Error searching users", Toast.LENGTH_SHORT).show();
                });
    }

    private void sendFollowRequest(User user) {
        String currentUserId = mAuth.getCurrentUser().getUid();
        if (user.getFollowers().contains(currentUserId)) {
            Toast.makeText(this, "You are already following this user", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!user.isPrivate()) { // User is public
            user.addFollower(currentUserId);
            db.collection("users").document(user.getUid()).update("followers", user.getFollowers())
                    .addOnSuccessListener(aVoid -> {
                        db.collection("users").document(currentUserId)
                                .get()
                                .addOnSuccessListener(document -> {
                                    User currentUser = document.toObject(User.class);
                                    if (currentUser != null) {
                                        currentUser.addFollowing(user.getUid());
                                        db.collection("users").document(currentUserId)
                                                .update("following", currentUser.getFollowing())
                                                .addOnSuccessListener(aVoid1 -> {
                                                    Toast.makeText(this, "Now following " + user.getEmail(), Toast.LENGTH_SHORT).show();
                                                    userAdapter.notifyDataSetChanged();
                                                });
                                    }
                                });
                    })
                    .addOnFailureListener(e -> {
                        Log.e("FollowRequestsActivity", "Error following user: " + e.getMessage());
                        Toast.makeText(this, "Error following user", Toast.LENGTH_SHORT).show();
                    });
        } else { // User is private
            // Check if a pending request already exists
            db.collection("follow_requests")
                    .whereEqualTo("fromUserId", currentUserId)
                    .whereEqualTo("toUserId", user.getUid())
                    .whereEqualTo("status", "pending")
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        if (queryDocumentSnapshots.isEmpty()) {
                            // No pending request, send a new one
                            String requestId = UUID.randomUUID().toString();
                            Map<String, Object> request = new HashMap<>();
                            request.put("fromUserId", currentUserId);
                            request.put("toUserId", user.getUid());
                            request.put("status", "pending");
                            request.put("timestamp", System.currentTimeMillis());

                            db.collection("follow_requests").document(requestId).set(request)
                                    .addOnSuccessListener(aVoid -> {
                                        Toast.makeText(this, "Follow request sent to " + user.getEmail(), Toast.LENGTH_SHORT).show();
                                        loadPendingRequests();
                                    })
                                    .addOnFailureListener(e -> {
                                        Log.e("FollowRequestsActivity", "Error sending follow request: " + e.getMessage());
                                        Toast.makeText(this, "Error sending request", Toast.LENGTH_SHORT).show();
                                    });
                        } else {
                            // Pending request already exists
                            Toast.makeText(this, "Pending follow request already sent to " + user.getEmail(), Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e("FollowRequestsActivity", "Error checking for existing follow request: " + e.getMessage());
                        Toast.makeText(this, "Error checking request status", Toast.LENGTH_SHORT).show();
                    });
        }
    }

    private void loadPendingRequests() {
        String currentUserId = mAuth.getCurrentUser().getUid();
        db.collection("follow_requests")
                .whereEqualTo("fromUserId", currentUserId)
                .whereEqualTo("status", "pending")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<DocumentSnapshot> pendingRequests = queryDocumentSnapshots.getDocuments();
                    List<FollowRequestDisplayItem> displayItems = new ArrayList<>();

                    if (pendingRequests.isEmpty()) {
                        requestAdapter.setRequests(new ArrayList<>()); // Clear the adapter if no requests
                        return;
                    }

                    // Fetch user details for each pending request
                    for (DocumentSnapshot requestDoc : pendingRequests) {
                        String toUserId = requestDoc.getString("toUserId");
                        if (toUserId != null) {
                            db.collection("users").document(toUserId).get()
                                    .addOnSuccessListener(userDoc -> {
                                        User targetUser = userDoc.toObject(User.class);
                                        if (targetUser != null) {
                                            displayItems.add(new FollowRequestDisplayItem(requestDoc, targetUser));
                                            // Check if all user details are fetched
                                            if (displayItems.size() == pendingRequests.size()) {
                                                requestAdapter.setRequests(displayItems);
                                            }
                                        } else {
                                            Log.e("FollowRequestsActivity", "Target user not found for request: " + requestDoc.getId());
                                            // Still add the item with limited info if user not found
                                            displayItems.add(new FollowRequestDisplayItem(requestDoc, null));
                                            if (displayItems.size() == pendingRequests.size()) {
                                                requestAdapter.setRequests(displayItems);
                                            }
                                        }
                                    })
                                    .addOnFailureListener(e -> {
                                        Log.e("FollowRequestsActivity", "Error fetching target user for request: " + requestDoc.getId() + ": " + e.getMessage());
                                        // Still add the item with limited info on failure
                                        displayItems.add(new FollowRequestDisplayItem(requestDoc, null));
                                        if (displayItems.size() == pendingRequests.size()) {
                                            requestAdapter.setRequests(displayItems);
                                        }
                                    });
                        } else {
                             Log.e("FollowRequestsActivity", "toUserId is null for request: " + requestDoc.getId());
                             // Still add the item with limited info if toUserId is null
                             displayItems.add(new FollowRequestDisplayItem(requestDoc, null));
                             if (displayItems.size() == pendingRequests.size()) {
                                 requestAdapter.setRequests(displayItems);
                             }
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("FollowRequestsActivity", "Error loading pending requests: " + e.getMessage());
                    Toast.makeText(this, "Error loading pending requests", Toast.LENGTH_SHORT).show();
                });
    }

    private void cancelFollowRequest(DocumentSnapshot request) {
        request.getReference().delete()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Follow request canceled", Toast.LENGTH_SHORT).show();
                    loadPendingRequests();
                })
                .addOnFailureListener(e -> {
                    Log.e("FollowRequestsActivity", "Error canceling request: " + e.getMessage());
                    Toast.makeText(this, "Error canceling request", Toast.LENGTH_SHORT).show();
                });
    }
}