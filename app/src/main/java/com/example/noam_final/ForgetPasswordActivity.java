package com.example.noam_final;

import android.content.Intent;
import android.os.Bundle;
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

public class ForgetPasswordActivity extends AppCompatActivity {
    private ImageButton btnBack;
    private EditText etEmailForPassword;
    private Button btnResetPassword;
    private ProgressBar progressBar;
    private FirebaseAuth mAuth;


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        btnBack = findViewById(R.id.btnBack);
        etEmailForPassword = findViewById(R.id.etEmailForPassword);
        btnResetPassword = findViewById(R.id.btnResetPassword);
        progressBar = findViewById(R.id.progressBar);

        mAuth = FirebaseAuth.getInstance();

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        btnResetPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetPassword();
            }
        });

    }

    private void resetPassword() {
        String email = etEmailForPassword.getText().toString().trim();

        if (email.isEmpty()) {
            etEmailForPassword.setError("Enter an email");
            etEmailForPassword.requestFocus();
            return;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmailForPassword.setError("Please enter a valid email");
            etEmailForPassword.requestFocus();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);


        mAuth.sendPasswordResetEmail(email).addOnCompleteListener(task -> {
            progressBar.setVisibility(View.GONE);
            if (task.isSuccessful()) {
                Toast.makeText(ForgetPasswordActivity.this, "Please check your email to reset your password", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(ForgetPasswordActivity.this, "Error! Please try again", Toast.LENGTH_SHORT).show();
            }
        });
    }

}
