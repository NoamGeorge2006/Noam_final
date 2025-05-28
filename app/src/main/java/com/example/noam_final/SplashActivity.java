package com.example.noam_final;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import com.google.firebase.auth.FirebaseAuth;

public class SplashActivity extends AppCompatActivity {
    private SharedPreferences sharedPreferences;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        mAuth = FirebaseAuth.getInstance();

        new Handler().postDelayed(() -> {
            boolean rememberMe = sharedPreferences.getBoolean("rememberMe", false);
            Intent intent;

            if (rememberMe && mAuth.getCurrentUser() != null) {
                // User wants to be remembered and is already logged in
                intent = new Intent(SplashActivity.this, HomePageActivity.class);
            } else {
                // User does not want to be remembered or is not logged in
                // If user is logged in but doesn't want to be remembered, sign them out
                if (mAuth.getCurrentUser() != null && !rememberMe) {
                    mAuth.signOut();
                }
                intent = new Intent(SplashActivity.this, MainActivity.class);
            }

            startActivity(intent);
            finish();
        }, 1000); // Reduced delay to 1 second
    }
}
