package com.example.noam_final;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.jar.Attributes;

public class ContactActivity extends AppCompatActivity implements View.OnClickListener {
    private Button btnContactSend;
    private EditText etName, etMessage;
    private String Name;
    private String Message;
    private ImageView ivBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact);
        init();
    }


    private void init() {
        btnContactSend = findViewById(R.id.btnContactSend);
        etName = findViewById(R.id.etName);
        etMessage = findViewById(R.id.etMessage);
        btnContactSend.setOnClickListener(this);
        ivBack = findViewById(R.id.ivBack);
        ivBack.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        Name = etName.getText().toString();
        Message = etMessage.getText().toString();

        if (v == ivBack) {
            finish();
        }
        if (v == btnContactSend) {
            if (TextUtils.isEmpty(Name) && TextUtils.isEmpty(Message)) {
                Toast.makeText(this, "Please enter name and message", Toast.LENGTH_LONG).show();
            } else if (TextUtils.isEmpty(Name)) {
                Toast.makeText(this, "Enter your name", Toast.LENGTH_LONG).show();
            } else if (Name.length() < 2) { // Add this condition to check if the name has less than 2 characters
                Toast.makeText(this, "Name should have at least 2 characters", Toast.LENGTH_LONG).show();
            } else if (TextUtils.isEmpty(Message)) {
                Toast.makeText(this, "Enter your message", Toast.LENGTH_LONG).show();
            } else {
                sendMail(Name, Message);
            }
            if (v == ivBack) {
                finish();
            }
        }
    }
    private void sendMail( String name, String message) {
        Intent email = new Intent(Intent.ACTION_SEND);
        email.setData(Uri.parse("malito:"));
        email.setType("text/plain");
        email.putExtra(Intent.EXTRA_EMAIL, new String[]{"calendunity@gmail.com"});
        email.putExtra(Intent.EXTRA_SUBJECT, name);
        email.putExtra(Intent.EXTRA_TEXT, message);
        email.setType("message/rfc822");
        try {
            startActivity(Intent.createChooser(email, "send mail..."));
            finish();
        } catch (Exception e) {
        }
    }
}


