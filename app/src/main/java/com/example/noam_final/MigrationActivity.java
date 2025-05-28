package com.example.noam_final;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

public class MigrationActivity extends AppCompatActivity {
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_migration);

        db = FirebaseFirestore.getInstance();
        migrateUsers();
    }

    private void migrateUsers() {
        db.collection("users")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        String userId = document.getId();
                        if (document.get("lastChecked") == null) {
                            db.collection("users").document(userId)
                                    .update("lastChecked", 0L)
                                    .addOnSuccessListener(aVoid -> Log.d("Migration", "Set lastChecked for user: " + userId))
                                    .addOnFailureListener(e -> Log.e("Migration", "Error updating user " + userId + ": " + e.getMessage()));
                        }
                    }
                    Toast.makeText(this, "User migration complete", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Log.e("Migration", "Error fetching users: " + e.getMessage());
                    Toast.makeText(this, "Migration failed", Toast.LENGTH_SHORT).show();
                    finish();
                });
    }
}