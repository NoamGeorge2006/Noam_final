package com.example.noam_final;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class JoinNewGroupActivity extends AppCompatActivity {
    private ImageButton btnBack;
    private EditText etGroupCode;
    private Button btnJoinGroup;
    private GroupManager groupManager;
    private String userId;
    private FirebaseFirestore db;
    private ProgressBar progressBar;
    private TextInputLayout tilGroupCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join_new_group);

        initializeViews();
        setupListeners();
        initializeFirebase();
    }

    private void initializeViews() {
        btnBack = findViewById(R.id.btnBack);
        tilGroupCode = findViewById(R.id.tilGroupCode);
        etGroupCode = findViewById(R.id.etGroupCode);
        btnJoinGroup = findViewById(R.id.btnJoinGroup);
        btnJoinGroup.setText("Join Group");
        progressBar = findViewById(R.id.progressBar);
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());
        btnJoinGroup.setOnClickListener(v -> joinGroup());
    }

    private void initializeFirebase() {
        groupManager = new GroupManager();
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        db = FirebaseFirestore.getInstance();
    }

    private void joinGroup() {
        String code = etGroupCode.getText().toString().trim();
        if (code.isEmpty()) {
            tilGroupCode.setError("Please enter a group code");
            return;
        }
        tilGroupCode.setError(null);
        progressBar.setVisibility(View.VISIBLE);
        db.collection("groups")
                .whereEqualTo("code", code)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    progressBar.setVisibility(View.GONE);
                    if (!queryDocumentSnapshots.isEmpty()) {
                        Group group = queryDocumentSnapshots.getDocuments().get(0).toObject(Group.class);
                        if (group != null) {
                            groupManager.joinGroup(userId, group.getGroupId(), new GroupManager.OnGroupOperationListener() {
                                @Override
                                public void onSuccess() {
                                    Toast.makeText(JoinNewGroupActivity.this, "Joined group: " + group.getName(), Toast.LENGTH_SHORT).show();
                                    finish();
                                }
                                @Override
                                public void onFailure(String error) {
                                    Toast.makeText(JoinNewGroupActivity.this, "Error joining group: " + error, Toast.LENGTH_SHORT).show();
                                }
                            });
                        } else {
                            tilGroupCode.setError("Invalid group code");
                        }
                    } else {
                        tilGroupCode.setError("Group not found");
                    }
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    tilGroupCode.setError("Error finding group");
                });
    }
}