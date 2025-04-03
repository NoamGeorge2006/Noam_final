package com.example.noam_final;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

/**
 * Activity for handling password reset functionality.
 * Allows users to request a password reset email by providing their registered email address.
 */
public class ForgetPasswordActivity extends AppCompatActivity {
    // UI Components
    private ImageButton btnBack;
    private EditText etEmailForPassword;
    private Button btnResetPassword;
    private ProgressBar progressBar;

    // Firebase Authentication instance
    private FirebaseAuth mAuth;

    // Constants for validation and messages
    private static final String ERROR_EMPTY_EMAIL = "Enter an email";
    private static final String ERROR_INVALID_EMAIL = "Please enter a valid email";
    private static final String SUCCESS_MESSAGE = "Please check your email to reset your password";
    private static final String ERROR_MESSAGE = "Error! Please try again";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        initializeViews();
        setupClickListeners();
    }

    /**
     * Initializes all view components and Firebase Authentication.
     */
    private void initializeViews() {
        btnBack = findViewById(R.id.btnBack);
        etEmailForPassword = findViewById(R.id.etEmailForPassword);
        btnResetPassword = findViewById(R.id.btnResetPassword);
        progressBar = findViewById(R.id.progressBar);
        
        mAuth = FirebaseAuth.getInstance();
    }

    /**
     * Sets up click listeners for buttons.
     */
    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> finish());
        btnResetPassword.setOnClickListener(v -> resetPassword());
    }

    /**
     * Handles the password reset process.
     * Validates email input and sends reset email if validation passes.
     */
    private void resetPassword() {
        String email = etEmailForPassword.getText().toString().trim();

        if (!validateEmail(email)) {
            return;
        }

        showLoading(true);
        sendResetEmail(email);
    }

    /**
     * Validates the email address format.
     * @param email The email address to validate
     * @return true if email is valid, false otherwise
     */
    private boolean validateEmail(String email) {
        if (email.isEmpty()) {
            showError(ERROR_EMPTY_EMAIL);
            return false;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            showError(ERROR_INVALID_EMAIL);
            return false;
        }

        return true;
    }

    /**
     * Shows error message and sets focus to email input.
     * @param errorMessage The error message to display
     */
    private void showError(String errorMessage) {
        etEmailForPassword.setError(errorMessage);
        etEmailForPassword.requestFocus();
    }

    /**
     * Controls the visibility of the loading indicator.
     * @param show true to show loading, false to hide
     */
    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        btnResetPassword.setEnabled(!show);
    }

    /**
     * Sends password reset email to the specified address.
     * @param email The email address to send reset instructions to
     */
    private void sendResetEmail(String email) {
        mAuth.sendPasswordResetEmail(email)
            .addOnCompleteListener(task -> {
                showLoading(false);
                String message = task.isSuccessful() ? SUCCESS_MESSAGE : ERROR_MESSAGE;
                Toast.makeText(ForgetPasswordActivity.this, message, Toast.LENGTH_SHORT).show();
                
                if (task.isSuccessful()) {
                    // Return to login screen after successful reset request
                    finish();
                }
            });
    }
}
