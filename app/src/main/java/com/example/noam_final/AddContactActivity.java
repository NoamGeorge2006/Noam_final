package com.example.noam_final;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

public class AddContactActivity extends AppCompatActivity {
    private ImageButton btnBack;
    private TextView tvContactFound;
    private EditText etContactEmail;
    private Button btnFindContact, btnAddContact;
    private ContactManager contactManager;
    private String userId;
    private FirebaseFirestore db;
    private String foundContactId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_contact);

        initializeViews();
        setupListeners();
        initializeFirebase();
    }

    private void initializeViews() {
        btnBack = findViewById(R.id.btnBack);
        etContactEmail = findViewById(R.id.etContactEmail);
        btnFindContact = findViewById(R.id.btnFindContact);
        tvContactFound = findViewById(R.id.tvContactFound);
        btnAddContact = findViewById(R.id.btnAddContact);

        btnAddContact.setEnabled(false); // Initially disabled until a contact is found
        tvContactFound.setVisibility(View.GONE); // Hidden until a contact is found
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());
        btnFindContact.setOnClickListener(v -> searchContact());
        btnAddContact.setOnClickListener(v -> addContact());
    }

    private void initializeFirebase() {
        contactManager = new ContactManager();
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        db = FirebaseFirestore.getInstance();
    }

    private void searchContact() {
        String contactEmail = etContactEmail.getText().toString().trim();

        if (contactEmail.isEmpty()) {
            etContactEmail.setError("Email is required");
            etContactEmail.requestFocus();
            return;
        }

        db.collection("users")
                .whereEqualTo("email", contactEmail)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots.isEmpty()) {
                        Toast.makeText(AddContactActivity.this, "User not found", Toast.LENGTH_SHORT).show();
                        tvContactFound.setVisibility(View.GONE);
                        btnAddContact.setEnabled(false);
                        foundContactId = null;
                        return;
                    }

                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        foundContactId = document.getId();
                        if (foundContactId.equals(userId)) {
                            Toast.makeText(AddContactActivity.this, "You cannot add yourself as a contact", Toast.LENGTH_SHORT).show();
                            tvContactFound.setVisibility(View.GONE);
                            btnAddContact.setEnabled(false);
                            foundContactId = null;
                            return;
                        }

                        tvContactFound.setText("Found: " + contactEmail);
                        tvContactFound.setVisibility(View.VISIBLE);
                        btnAddContact.setEnabled(true);
                        break; // Only take the first matching user
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(AddContactActivity.this, "Error searching for user: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    tvContactFound.setVisibility(View.GONE);
                    btnAddContact.setEnabled(false);
                    foundContactId = null;
                });
    }

    private void addContact() {
        if (foundContactId == null) {
            Toast.makeText(this, "No contact selected", Toast.LENGTH_SHORT).show();
            return;
        }

        contactManager.addContact(userId, foundContactId, new ContactManager.OnContactOperationListener() {
            @Override
            public void onSuccess() {
                Toast.makeText(AddContactActivity.this, "Contact added successfully", Toast.LENGTH_SHORT).show();
                finish();
            }

            @Override
            public void onFailure(String error) {
                Toast.makeText(AddContactActivity.this, "Error adding contact: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }
}