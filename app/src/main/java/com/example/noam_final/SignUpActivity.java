package com.example.noam_final;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

public class SignUpActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private EditText etName, etEmail, etPassword, etConfirmPassword;
    private Button btnSignUp;
    private TextView tvLogin;
    private ProgressBar progressBar;

    // Constants for validation and messages
    private static final String ERROR_EMPTY_NAME = "Please enter your name";
    private static final String ERROR_EMPTY_EMAIL = "Please enter your email";
    private static final String ERROR_EMPTY_PASSWORD = "Please enter a password";
    private static final String ERROR_EMPTY_CONFIRM = "Please confirm your password";
    private static final String ERROR_INVALID_EMAIL = "Please enter a valid email address";
    private static final String ERROR_SHORT_PASSWORD = "Password must be at least 6 characters";
    private static final String ERROR_PASSWORD_MATCH = "Passwords do not match";
    private static final String ERROR_USER_EXISTS = "An account with this email already exists";
    private static final String ERROR_CHECK_USER = "Error checking user existence. Please try again.";
    private static final String SUCCESS_SIGNUP = "Account created successfully!";
    private static final int MIN_PASSWORD_LENGTH = 6;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        init();
        addListener();
    }

    private void init() {
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        btnSignUp = findViewById(R.id.btnSignUp);
        tvLogin = findViewById(R.id.tvLogIn);
        progressBar = findViewById(R.id.progressBar);

        progressBar.setVisibility(View.GONE);
    }

    private void addListener() {
        btnSignUp.setOnClickListener(view -> {
            String name = etName.getText().toString().trim();
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();
            String confirmPassword = etConfirmPassword.getText().toString().trim();

            if (!validateInputs(name, email, password, confirmPassword)) {
                return;
            }

            showLoading(true);
            checkIfUserExists(email, password);
        });

        tvLogin.setOnClickListener(v -> {
            startActivity(new Intent(SignUpActivity.this, LoginActivity.class));
            finish();
        });
    }

    private boolean validateInputs(String name, String email, String password, String confirmPassword) {
        // Check for empty fields
        if (name.isEmpty()) {
            showError(etName, ERROR_EMPTY_NAME);
            return false;
        }

        if (email.isEmpty()) {
            showError(etEmail, ERROR_EMPTY_EMAIL);
            return false;
        }

        if (password.isEmpty()) {
            showError(etPassword, ERROR_EMPTY_PASSWORD);
            return false;
        }

        if (confirmPassword.isEmpty()) {
            showError(etConfirmPassword, ERROR_EMPTY_CONFIRM);
            return false;
        }

        // Validate email format
        if (!isValidEmail(email)) {
            showError(etEmail, ERROR_INVALID_EMAIL);
            return false;
        }

        // Validate password length
        if (password.length() < MIN_PASSWORD_LENGTH) {
            showError(etPassword, ERROR_SHORT_PASSWORD);
            return false;
        }

        // Check if passwords match
        if (!password.equals(confirmPassword)) {
            showError(etConfirmPassword, ERROR_PASSWORD_MATCH);
            return false;
        }

        return true;
    }

    private void showError(EditText field, String message) {
        field.setError(message);
        field.requestFocus();
    }

    private void showToast(String message) {
        Toast.makeText(SignUpActivity.this, message, Toast.LENGTH_SHORT).show();
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        btnSignUp.setEnabled(!show);
    }

    private void checkIfUserExists(String email, String password) {
        db.collection("users").whereEqualTo("email", email).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        QuerySnapshot querySnapshot = task.getResult();
                        if (querySnapshot != null && !querySnapshot.isEmpty()) {
                            showLoading(false);
                            showToast(ERROR_USER_EXISTS);
                        } else {
                            addAccount(email, password);
                        }
                    } else {
                        showLoading(false);
                        showToast(ERROR_CHECK_USER);
                    }
                });
    }

    private void addAccount(String email, String password) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    showLoading(false);
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        showToast(SUCCESS_SIGNUP);
                        startActivity(new Intent(SignUpActivity.this, HomePageActivity.class));
                        finish();
                    } else {
                        String errorMessage = task.getException() != null ? 
                            task.getException().getMessage() : 
                            "Sign up failed. Please try again.";
                        showToast(errorMessage);
                    }
                });
    }

    private boolean isValidEmail(String email) {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }
}