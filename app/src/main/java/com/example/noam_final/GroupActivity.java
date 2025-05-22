package com.example.noam_final;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import java.util.ArrayList;
import java.util.List;

public class GroupActivity extends AppCompatActivity implements PopupMenu.OnMenuItemClickListener {
    private GroupManager groupManager;
    private ContactManager contactManager;
    private ListView lvGroups, lvContacts;
    private ImageButton btnBack, menuIcon;
    private TextView tvGroupsHeader, tvContactsHeader;
    private List<Group> connectedGroups;
    private List<Contact> connectedContacts;
    private ArrayAdapter<String> groupsAdapter;
    private ArrayAdapter<String> contactsAdapter;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group);

        initializeViews();
        setupListeners();
        initializeFirebase();
        fetchConnectedGroups();
        fetchConnectedContacts();
    }

    private void initializeViews() {
        btnBack = findViewById(R.id.btnBack);
        menuIcon = findViewById(R.id.menuIcon);
        tvGroupsHeader = findViewById(R.id.tvGroupsHeader);
        lvGroups = findViewById(R.id.lvGroups);
        tvContactsHeader = findViewById(R.id.tvContactsHeader);
        lvContacts = findViewById(R.id.lvContacts);

        connectedGroups = new ArrayList<>();
        connectedContacts = new ArrayList<>();
        groupsAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, new ArrayList<>());
        contactsAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, new ArrayList<>());
        lvGroups.setAdapter(groupsAdapter);
        lvContacts.setAdapter(contactsAdapter);
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());
        menuIcon.setOnClickListener(v -> showPopupMenu(v));

        lvGroups.setOnItemClickListener((parent, view, position, id) -> {
            Group selectedGroup = connectedGroups.get(position);
            Intent intent = new Intent(GroupActivity.this, GroupDetailActivity.class);
            intent.putExtra("groupId", selectedGroup.getGroupId());
            startActivity(intent);
        });

        lvContacts.setOnItemClickListener((parent, view, position, id) -> {
            Contact selectedContact = connectedContacts.get(position);
            Intent intent = new Intent(GroupActivity.this, ContactDetailActivity.class);
            intent.putExtra("contactId", selectedContact.getUserId());
            startActivity(intent);
        });
    }

    private void showPopupMenu(View v) {
        PopupMenu popup = new PopupMenu(this, v);
        popup.setOnMenuItemClickListener(this);
        popup.inflate(R.menu.menu_group);
        popup.show();
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        Intent intent;
        int id = item.getItemId();
        if (id == R.id.createGroup) {
            intent = new Intent(GroupActivity.this, CreateGroupActivity.class);
            startActivity(intent);
            return true;
        } else if (id == R.id.joinGroup) {
            intent = new Intent(GroupActivity.this, JoinNewGroupActivity.class);
            startActivity(intent);
            return true;
        } else if (id == R.id.addContact) {
            intent = new Intent(GroupActivity.this, AddContactActivity.class);
            startActivity(intent);
            return true;
        }
        return false;
    }

    private void initializeFirebase() {
        groupManager = new GroupManager();
        contactManager = new ContactManager();
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
    }

    private void fetchConnectedGroups() {
        groupManager.getUserGroups(userId, new GroupManager.OnUserGroupsListener() {
            @Override
            public void onGroupsFetched(List<Group> groups) {
                connectedGroups.clear();
                connectedGroups.addAll(groups);
                List<String> groupNames = new ArrayList<>();
                for (Group group : groups) {
                    groupNames.add(group.getName());
                }
                groupsAdapter.clear();
                groupsAdapter.addAll(groupNames);
                groupsAdapter.notifyDataSetChanged();
                tvGroupsHeader.setText("My Groups (" + groups.size() + ")");
            }

            @Override
            public void onFailure(String error) {
                Toast.makeText(GroupActivity.this, "Error fetching groups: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchConnectedContacts() {
        contactManager.getUserContacts(userId, new ContactManager.OnUserContactsListener() {
            @Override
            public void onContactsFetched(List<Contact> contacts) {
                connectedContacts.clear();
                connectedContacts.addAll(contacts);
                List<String> contactEmails = new ArrayList<>();
                for (Contact contact : contacts) {
                    contactEmails.add(contact.getEmail());
                }
                contactsAdapter.clear();
                contactsAdapter.addAll(contactEmails);
                contactsAdapter.notifyDataSetChanged();
                tvContactsHeader.setText("My Contacts (" + contacts.size() + ")");
            }

            @Override
            public void onFailure(String error) {
                Toast.makeText(GroupActivity.this, "Error fetching contacts: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    protected void onResume() {
        super.onResume();
        fetchConnectedGroups();
        fetchConnectedContacts();
    }
}