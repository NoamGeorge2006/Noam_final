package com.example.noam_final;

import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Source;

public class ProfileActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private TextView tvName, tvEmail, tvPrivacyStatus;
    private Switch switchPrivacy;
    private ImageButton btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        tvName = findViewById(R.id.tvName);
        tvEmail = findViewById(R.id.tvEmail);
        tvPrivacyStatus = findViewById(R.id.tvPrivacyStatus);
        switchPrivacy = findViewById(R.id.switchPrivacy);
        btnBack = findViewById(R.id.btnBack);

        btnBack.setOnClickListener(v -> finish());

        loadUserProfile();
    }

    private void loadUserProfile() {
        String userId = mAuth.getCurrentUser().getUid();
        // Temporarily use Source.SERVER to bypass cache
        db.collection("users").document(userId)
                .get(Source.SERVER)
                .addOnSuccessListener(document -> {
                    Log.d("ProfileActivity", "Raw Firestore document: " + document.getData());

                    // Directly read isPrivate from the document to avoid mapping issues
                    Boolean isPrivateRaw = document.getBoolean("isPrivate");
                    boolean isPrivate = isPrivateRaw != null ? isPrivateRaw : false;
                    Log.d("ProfileActivity", "Raw isPrivate from Firestore: " + isPrivate);

                    User user = document.toObject(User.class);
                    Log.d("ProfileActivity", "User object after toObject: isPrivate = " + (user != null ? user.isPrivate() : "user is null"));

                    if (user != null) {
                        tvName.setText(user.getName() != null ? user.getName() : user.getEmail().split("@")[0]);
                        tvEmail.setText(user.getEmail());
                        tvPrivacyStatus.setText(isPrivate ? "Private" : "Public");
                        switchPrivacy.setChecked(isPrivate);

                        switchPrivacy.setOnCheckedChangeListener((buttonView, isChecked) -> {
                            updatePrivacyStatus(userId, isChecked);
                        });
                    } else {
                        Log.e("ProfileActivity", "User document is null");
                        Toast.makeText(this, "Failed to load profile", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("ProfileActivity", "Error loading profile: " + e.getMessage());
                    Toast.makeText(this, "Error loading profile", Toast.LENGTH_SHORT).show();
                    finish();
                });
    }

    private void updatePrivacyStatus(String userId, boolean isPrivate) {
        Log.d("ProfileActivity", "Updating privacy for user " + userId + " to " + isPrivate);
        db.collection("users").document(userId)
                .update("isPrivate", isPrivate)
                .addOnSuccessListener(aVoid -> {
                    Log.d("ProfileActivity", "Privacy updated to " + isPrivate);
                    tvPrivacyStatus.setText(isPrivate ? "Private" : "Public");
                    Toast.makeText(this, "Privacy updated", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Log.e("ProfileActivity", "Error updating privacy: " + e.getMessage());
                    Toast.makeText(this, "Failed to update privacy", Toast.LENGTH_SHORT).show();
                    switchPrivacy.setChecked(!isPrivate); // Revert switch on failure
                });
    }
}