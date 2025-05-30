package com.example.noam_final;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentTransaction;

import com.google.firebase.auth.FirebaseAuth;
import android.Manifest;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class HomePageActivity extends AppCompatActivity implements View.OnClickListener, PopupMenu.OnMenuItemClickListener {
    private ImageView menu_icon;
    private ImageView profile_icon;
    private ImageView plus_icon;
    private FirebaseAuth mAuth;
    private static final int REQUEST_SCHEDULE_EXACT_ALARM = 101;
    private static final int REQUEST_POST_NOTIFICATIONS = 102;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_homepage);
        init();
        loadCalendarFragment();
        mAuth = FirebaseAuth.getInstance();
        requestNotificationPermission();
        startFollowRequestCheck();
    }

    private void init() {
        menu_icon = findViewById(R.id.menu_icon);
        menu_icon.setOnClickListener(this);
        plus_icon = findViewById(R.id.plus_icon);
        plus_icon.setOnClickListener(this);
        profile_icon = findViewById(R.id.profile_icon);
        profile_icon.setOnClickListener(this);

    }

    private void loadCalendarFragment() {
        CalendarFragment calendarFragment = new CalendarFragment();
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.calendar_container, calendarFragment);
        transaction.commit();
    }

    public void onClick(View v) {
        if (v.getId() == R.id.menu_icon) {
            showPopup(v);
        }

        if (v.getId() == R.id.plus_icon) {
            Intent intent = new Intent(this, AddEventActivity.class);
            startActivity(intent);
        }
        if (v.getId() == R.id.profile_icon) {
            Intent intent = new Intent(this, ProfileActivity.class);
            startActivity(intent);
        }
    }

    private void showPopup(View v) {
        PopupMenu popup = new PopupMenu(this, v);
        popup.setOnMenuItemClickListener(this);
        popup.inflate(R.menu.menu_item);
        popup.show();
    }

    public boolean onMenuItemClick(MenuItem item) {
        int id = item.getItemId();
        Intent t;
        if (id == R.id.credit) {
            t = new Intent(this, CreditActivity.class);
            startActivity(t);
        }
        if (id == R.id.contact) {
            t = new Intent(this, ContactActivity.class);
            startActivity(t);
        }
        if (id == R.id.guide) {
            t = new Intent(this, GuideActivity.class);
            startActivity(t);
        }
        if (id == R.id.follow_requests) {
            t = new Intent(this, FollowRequestsActivity.class);
            startActivity(t);
        } else if (id == R.id.follow_notifications) {
            t = new Intent(this, FollowNotificationsActivity.class);
            startActivity(t);
        }
        if (id == R.id.logout) {
            // Sign out from Firebase
            mAuth.signOut();
            Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show();

            // Clear saved remember me preference
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.remove("rememberedEmail");
            editor.putBoolean("rememberMe", false);
            editor.apply();

            // Clear all activities from the stack and start MainActivity
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        }
        return true;
    }

    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        REQUEST_POST_NOTIFICATIONS);
            }
        }
    }

    private void startFollowRequestCheck() {
        AppAlarmManager alarmManager = new AppAlarmManager(this);
        alarmManager.scheduleRepeatingAlarm();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_SCHEDULE_EXACT_ALARM) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startFollowRequestCheck();
            } else {
                Toast.makeText(this, "Alarm scheduling requires permission", Toast.LENGTH_LONG).show();
            }
        } else if (requestCode == REQUEST_POST_NOTIFICATIONS) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, notifications can proceed
            } else {
                Toast.makeText(this, "Notifications require permission", Toast.LENGTH_LONG).show();
            }
        }
    }
}
