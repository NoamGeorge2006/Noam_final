package com.example.noam_final;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import java.util.UUID;

public class CreateGroupActivity extends AppCompatActivity {
    private ImageButton btnBack;
    private EditText etGroupName;
    private Button btnCreateGroup;
    private GroupManager groupManager;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_group);

        initializeViews();
        setupListeners();
        initializeFirebase();
    }

    private void initializeViews() {
        btnBack = findViewById(R.id.btnBack);
        etGroupName = findViewById(R.id.etGroupName);
        btnCreateGroup = findViewById(R.id.btnCreateGroup);
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());
        btnCreateGroup.setOnClickListener(v -> createGroup());
    }

    private void initializeFirebase() {
        groupManager = new GroupManager();
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
    }

    private void createGroup() {
        String groupName = etGroupName.getText().toString().trim();
        if (groupName.isEmpty()) {
            Toast.makeText(this, "Please enter a group name", Toast.LENGTH_SHORT).show();
            return;
        }

        String groupId = UUID.randomUUID().toString();
        String groupCode = UUID.randomUUID().toString().substring(0, 8); // Simple 8-char code
        Group group = new Group(groupId, groupName, false, groupCode); // Private group

        groupManager.createGroup(group, new GroupManager.OnGroupOperationListener() {
            @Override
            public void onSuccess() {
                // Join the creator to the group
                groupManager.joinGroup(userId, groupId, new GroupManager.OnGroupOperationListener() {
                    @Override
                    public void onSuccess() {
                        // Copy code to clipboard
                        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                        ClipData clip = ClipData.newPlainText("Group Code", groupCode);
                        clipboard.setPrimaryClip(clip);
                        Toast.makeText(CreateGroupActivity.this, "Group created! Code: " + groupCode + " (copied to clipboard)", Toast.LENGTH_LONG).show();
                        finish();
                    }
                    @Override
                    public void onFailure(String error) {
                        Toast.makeText(CreateGroupActivity.this, "Error joining group: " + error, Toast.LENGTH_SHORT).show();
                    }
                });
            }
            @Override
            public void onFailure(String error) {
                Toast.makeText(CreateGroupActivity.this, "Error creating group: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }
}