package com.example.noam_final;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import java.util.ArrayList;
import java.util.List;

public class JoinNewGroupActivity extends AppCompatActivity {
    private ImageButton btnBack;
    private EditText etGroupSearch;
    private Button btnFindGroup, btnJoinGroup;
    private ListView lvPublicGroups;
    private GroupManager groupManager;
    private String userId;
    private List<Group> publicGroups;
    private List<Group> filteredGroups;
    private ArrayAdapter<String> groupsAdapter;
    private Group selectedGroup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join_new_group);

        initializeViews();
        setupListeners();
        initializeFirebase();
        fetchPublicGroups();
    }

    private void initializeViews() {
        btnBack = findViewById(R.id.btnBack);
        etGroupSearch = findViewById(R.id.etGroupSearch);
        btnFindGroup = findViewById(R.id.btnFindGroup);
        lvPublicGroups = findViewById(R.id.lvPublicGroups);
        btnJoinGroup = findViewById(R.id.btnJoinGroup);

        publicGroups = new ArrayList<>();
        filteredGroups = new ArrayList<>();
        groupsAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, new ArrayList<>());
        lvPublicGroups.setAdapter(groupsAdapter);
        btnJoinGroup.setEnabled(false); // Initially disabled until a group is selected
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());
        btnFindGroup.setOnClickListener(v -> searchGroups());
        lvPublicGroups.setOnItemClickListener((parent, view, position, id) -> {
            selectedGroup = filteredGroups.get(position);
            btnJoinGroup.setEnabled(true);
        });
        btnJoinGroup.setOnClickListener(v -> joinGroup());
    }

    private void initializeFirebase() {
        groupManager = new GroupManager();
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
    }

    private void fetchPublicGroups() {
        groupManager.getPublicGroups(new GroupManager.OnUserGroupsListener() {
            @Override
            public void onGroupsFetched(List<Group> groups) {
                publicGroups.clear();
                publicGroups.addAll(groups);
                filteredGroups.clear();
                filteredGroups.addAll(groups);
                updateGroupsList();
            }

            @Override
            public void onFailure(String error) {
                Toast.makeText(JoinNewGroupActivity.this, "Error fetching groups: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void searchGroups() {
        String query = etGroupSearch.getText().toString().trim().toLowerCase();
        filteredGroups.clear();
        if (query.isEmpty()) {
            filteredGroups.addAll(publicGroups);
        } else {
            for (Group group : publicGroups) {
                if (group.getName().toLowerCase().contains(query)) {
                    filteredGroups.add(group);
                }
            }
        }
        updateGroupsList();
        if (filteredGroups.isEmpty()) {
            Toast.makeText(this, "No groups found", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateGroupsList() {
        List<String> groupNames = new ArrayList<>();
        for (Group group : filteredGroups) {
            groupNames.add(group.getName());
        }
        groupsAdapter.clear();
        groupsAdapter.addAll(groupNames);
        groupsAdapter.notifyDataSetChanged();
        selectedGroup = null;
        btnJoinGroup.setEnabled(false); // Disable until a new group is selected
    }

    private void joinGroup() {
        if (selectedGroup == null) {
            Toast.makeText(this, "Please select a group", Toast.LENGTH_SHORT).show();
            return;
        }
        groupManager.joinGroup(userId, selectedGroup.getGroupId(), new GroupManager.OnGroupOperationListener() {
            @Override
            public void onSuccess() {
                Toast.makeText(JoinNewGroupActivity.this, "Joined group successfully", Toast.LENGTH_SHORT).show();
                finish();
            }

            @Override
            public void onFailure(String error) {
                Toast.makeText(JoinNewGroupActivity.this, "Error joining group: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }
}