package com.example.noam_final;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class GroupDetailActivity extends AppCompatActivity {
    private ImageButton btnBack;
    private TextView tvGroupTitle, tvGroupName, tvGroupVisibility;
    private Button btnLeaveGroup;
    private String groupId;
    private GroupManager groupManager;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_detail);

        initializeViews();
        setupListeners();
        initializeFirebase();

        groupId = getIntent().getStringExtra("groupId");
        if (groupId != null) {
            fetchGroupDetails();
        } else {
            Toast.makeText(this, "Group ID not found", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void initializeViews() {
        btnBack = findViewById(R.id.btnBack);
        tvGroupTitle = findViewById(R.id.tvGroupTitle);
        tvGroupName = findViewById(R.id.tvGroupName);
        tvGroupVisibility = findViewById(R.id.tvGroupVisibility);
        btnLeaveGroup = findViewById(R.id.btnLeaveGroup);
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());
        btnLeaveGroup.setOnClickListener(v -> leaveGroup());
    }

    private void initializeFirebase() {
        groupManager = new GroupManager();
        db = FirebaseFirestore.getInstance();
    }

    private void fetchGroupDetails() {
        db.collection("groups").document(groupId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Group group = documentSnapshot.toObject(Group.class);
                        if (group != null) {
                            tvGroupName.setText(group.getName());
                            tvGroupVisibility.setText(group.isPublic() ? "Public Group" : "Private Group");
                        }
                    } else {
                        Toast.makeText(this, "Group not found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error fetching group: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void leaveGroup() {
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        groupManager.leaveGroup(currentUserId, groupId, new GroupManager.OnGroupOperationListener() {
            @Override
            public void onSuccess() {
                Toast.makeText(GroupDetailActivity.this, "Left group successfully", Toast.LENGTH_SHORT).show();
                finish(); // Return to GroupActivity
            }

            @Override
            public void onFailure(String error) {
                Toast.makeText(GroupDetailActivity.this, "Error leaving group: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }
}