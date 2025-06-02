package com.example.noam_final;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import android.preference.PreferenceManager;

public class LoginActivity extends AppCompatActivity {
    private EditText etEmail, etPassword;
    private Button btnLogin;
    private TextView tvSignUp, tvForgotPassword;
    private ProgressBar progressBar;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private CheckBox cbRememberMe;
    private SharedPreferences sharedPreferences;

    // Constants for validation and messages
    private static final String ERROR_EMPTY_FIELDS = "Please fill in all fields";
    private static final String ERROR_INVALID_EMAIL = "Please enter a valid email address";
    private static final String ERROR_SHORT_PASSWORD = "Password must be at least 6 characters";
    private static final String SUCCESS_LOGIN = "Login successful!";
    private static final int MIN_PASSWORD_LENGTH = 6;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Check if user is already logged in
        mAuth = FirebaseAuth.getInstance();
        if (mAuth.getCurrentUser() != null) {
            // User is signed in, go directly to HomePageActivity
            startActivity(new Intent(LoginActivity.this, HomePageActivity.class));
            finish();
            return; // Skip the rest of onCreate
        }

        setContentView(R.layout.activity_login);

        init();
        addListener();
    }

    private void init() {
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvSignUp = findViewById(R.id.tvSignUp);
//        tvForgotPassword = findViewById(R.id.tvForgotPassword);
        progressBar = findViewById(R.id.progressBar);
        cbRememberMe = findViewById(R.id.cbRememberMe);

        db = FirebaseFirestore.getInstance();
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        progressBar.setVisibility(View.GONE);
        loadRememberedCredentials();
    }

    private void addListener() {
        btnLogin.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (!validateInputs(email, password)) {
                return;
            }

            showLoading(true);
            signInWithFirebase(email, password);
        });

        tvSignUp.setOnClickListener(v -> startActivity(new Intent(LoginActivity.this, SignUpActivity.class)));
//        tvForgotPassword.setOnClickListener(v -> startActivity(new Intent(LoginActivity.this, ForgetPasswordActivity.class)));
    }

    private boolean validateInputs(String email, String password) {
        // Check for empty fields
        if (email.isEmpty() || password.isEmpty()) {
            showError(ERROR_EMPTY_FIELDS);
            if (email.isEmpty()) etEmail.requestFocus();
            else etPassword.requestFocus();
            return false;
        }

        // Validate email format
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError(ERROR_INVALID_EMAIL);
            etEmail.requestFocus();
            return false;
        }

        // Validate password length
        if (password.length() < MIN_PASSWORD_LENGTH) {
            etPassword.setError(ERROR_SHORT_PASSWORD);
            etPassword.requestFocus();
            return false;
        }

        return true;
    }

    private void showError(String message) {
        Toast.makeText(LoginActivity.this, message, Toast.LENGTH_SHORT).show();
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        btnLogin.setEnabled(!show);
    }

    private void signInWithFirebase(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    showLoading(false);
                    if (task.isSuccessful()) {
                        if (cbRememberMe.isChecked()) {
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putString("rememberedEmail", email);
                            editor.putBoolean("rememberMe", true);
                            editor.apply();
                        } else {
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.remove("rememberedEmail");
                            editor.putBoolean("rememberMe", false);
                            editor.apply();
                        }

                        Toast.makeText(LoginActivity.this, SUCCESS_LOGIN, Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(LoginActivity.this, HomePageActivity.class));
                        finish();
                    } else {
                        String errorMessage = "Login failed. Please check your email and password.";
                        showError(errorMessage);
                    }
                });
    }

    private void loadRememberedCredentials() {
        boolean rememberMe = sharedPreferences.getBoolean("rememberMe", false);
        if (rememberMe) {
            String rememberedEmail = sharedPreferences.getString("rememberedEmail", "");
            etEmail.setText(rememberedEmail);
            cbRememberMe.setChecked(true);
        }
    }
}
