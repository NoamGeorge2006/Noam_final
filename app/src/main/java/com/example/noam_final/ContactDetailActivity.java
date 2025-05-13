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

public class ContactDetailActivity extends AppCompatActivity {
    private ImageButton btnBack;
    private TextView tvContactTitle, tvContactEmail;
    private Button btnRemoveContact;
    private String contactId;
    private ContactManager contactManager;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_detail);

        initializeViews();
        setupListeners();
        initializeFirebase();

        contactId = getIntent().getStringExtra("contactId");
        if (contactId != null) {
            fetchContactDetails();
        } else {
            Toast.makeText(this, "Contact ID not found", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void initializeViews() {
        btnBack = findViewById(R.id.btnBack);
        tvContactTitle = findViewById(R.id.tvContactTitle);
        tvContactEmail = findViewById(R.id.tvContactEmail);
        btnRemoveContact = findViewById(R.id.btnRemoveContact);
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());
        btnRemoveContact.setOnClickListener(v -> removeContact());
    }

    private void initializeFirebase() {
        contactManager = new ContactManager();
        db = FirebaseFirestore.getInstance();
    }

    private void fetchContactDetails() {
        db.collection("users").document(contactId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        User contact = documentSnapshot.toObject(User.class);
                        if (contact != null) {
                            tvContactEmail.setText(contact.getEmail());
                        }
                    } else {
                        Toast.makeText(this, "Contact not found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error fetching contact: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void removeContact() {
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        contactManager.removeContact(currentUserId, contactId, new ContactManager.OnContactOperationListener() {
            @Override
            public void onSuccess() {
                Toast.makeText(ContactDetailActivity.this, "Contact removed successfully", Toast.LENGTH_SHORT).show();
                finish(); // Return to GroupActivity
            }

            @Override
            public void onFailure(String error) {
                Toast.makeText(ContactDetailActivity.this, "Error removing contact: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }
}