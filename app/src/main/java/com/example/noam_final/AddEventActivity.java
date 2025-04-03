package com.example.noam_final;

import android.Manifest;
import android.app.DatePickerDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.Calendar;
import java.util.TimeZone;
import java.util.UUID;

public class AddEventActivity extends AppCompatActivity {
    private static final int CALENDAR_PERMISSION_CODE = 100;

    private ImageButton btnBack;
    private EditText etTitle, etDescription, etDate, etLocation;
    private Button btnAddEventPrivate, btnAddEventPublic;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private Calendar selectedDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_event);

        initializeViews();
        setupListeners();
        initializeFirebase();
        selectedDate = Calendar.getInstance();
    }

    private void initializeViews() {
        btnBack = findViewById(R.id.btnBack);
        etTitle = findViewById(R.id.etTitle);
        etDescription = findViewById(R.id.etDescription);
        etDate = findViewById(R.id.etDate);
        etLocation = findViewById(R.id.etLocation);
        btnAddEventPrivate = findViewById(R.id.btnAddEventPrivate);
        btnAddEventPublic = findViewById(R.id.btnAddEventPublic);
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());
        etDate.setOnClickListener(v -> showDatePicker());
        btnAddEventPrivate.setOnClickListener(v -> checkCalendarPermissionAndAddEvent(false));
        btnAddEventPublic.setOnClickListener(v -> checkCalendarPermissionAndAddEvent(true));
    }

    private void initializeFirebase() {
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    selectedDate.set(selectedYear, selectedMonth, selectedDay);
                    String date = selectedDay + "/" + (selectedMonth + 1) + "/" + selectedYear;
                    etDate.setText(date);
                },
                year,
                month,
                day
        );
        datePickerDialog.show();
    }

    private void checkCalendarPermissionAndAddEvent(boolean isPublic) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_CALENDAR)
                != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CALENDAR)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{
                        Manifest.permission.WRITE_CALENDAR,
                        Manifest.permission.READ_CALENDAR
                    },
                    CALENDAR_PERMISSION_CODE);
        } else {
            addEvent(isPublic);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CALENDAR_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                addEvent(false); // Default to private if permission just granted
            } else {
                Toast.makeText(this, "Calendar permission is required to add events", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void addEvent(boolean isPublic) {
        if (mAuth.getCurrentUser() == null) {
            Toast.makeText(this, "Please log in to add events", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        String title = etTitle.getText().toString().trim();
        String description = etDescription.getText().toString().trim();
        String date = etDate.getText().toString().trim();
        String location = etLocation.getText().toString().trim();

        if (!validateInputs(title, description, date, location)) {
            return;
        }

        // Create Event object
        String eventId = UUID.randomUUID().toString();
        String userId = mAuth.getCurrentUser().getUid();
        Event event = new Event(eventId, title, description, date, location, isPublic, userId);

        // Add to Firebase
        addEventToFirebase(event);

        // Add to device calendar
        addEventToDeviceCalendar(event);
    }

    private void addEventToFirebase(Event event) {
        db.collection("events")
                .document(event.getId())
                .set(event.toMap())
                .addOnSuccessListener(aVoid -> {
                    String visibility = event.isPublic() ? "public" : "private";
                    Toast.makeText(this, "Event added successfully as " + visibility, Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to add event: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void addEventToDeviceCalendar(Event event) {
        ContentResolver cr = getContentResolver();
        ContentValues values = new ContentValues();

        // Get the default calendar ID
        String[] projection = new String[]{CalendarContract.Calendars._ID};
        String selection = CalendarContract.Calendars.IS_PRIMARY + " = 1";
        android.database.Cursor cursor = cr.query(CalendarContract.Calendars.CONTENT_URI, projection, selection, null, null);
        
        long calendarId = 1; // Default fallback
        if (cursor != null && cursor.moveToFirst()) {
            calendarId = cursor.getLong(0);
            cursor.close();
        }

        // Set event details
        values.put(CalendarContract.Events.CALENDAR_ID, calendarId);
        values.put(CalendarContract.Events.TITLE, event.getTitle());
        values.put(CalendarContract.Events.DESCRIPTION, event.getDescription());
        values.put(CalendarContract.Events.EVENT_LOCATION, event.getLocation());

        // Set time
        long startMillis = selectedDate.getTimeInMillis();
        values.put(CalendarContract.Events.DTSTART, startMillis);
        values.put(CalendarContract.Events.DTEND, startMillis + 3600000); // 1 hour duration
        values.put(CalendarContract.Events.EVENT_TIMEZONE, TimeZone.getDefault().getID());

        // Event status
        values.put(CalendarContract.Events.HAS_ALARM, 1);

        try {
            Uri uri = cr.insert(CalendarContract.Events.CONTENT_URI, values);
            if (uri != null) {
                long eventID = Long.parseLong(uri.getLastPathSegment());
                Toast.makeText(this, "Event added to calendar", Toast.LENGTH_SHORT).show();
            }
        } catch (SecurityException e) {
            Toast.makeText(this, "Error adding event to calendar: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private boolean validateInputs(String title, String description, String date, String location) {
        if (title.isEmpty()) {
            etTitle.setError("Title is required");
            etTitle.requestFocus();
            return false;
        }

        if (description.isEmpty()) {
            etDescription.setError("Description is required");
            etDescription.requestFocus();
            return false;
        }

        if (date.isEmpty()) {
            etDate.setError("Date is required");
            etDate.requestFocus();
            return false;
        }

        if (location.isEmpty()) {
            etLocation.setError("Location is required");
            etLocation.requestFocus();
            return false;
        }

        return true;
    }
}