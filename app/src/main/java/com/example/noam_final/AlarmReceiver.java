package com.example.noam_final;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import java.util.List;
import android.Manifest;

public class AlarmReceiver extends BroadcastReceiver {
    private static final int REQUEST_POST_NOTIFICATIONS = 102;

    @Override
    public void onReceive(Context context, Intent intent) {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() == null) {
            return;
        }
        String currentUserId = auth.getCurrentUser().getUid();

        checkFollowRequests(context, currentUserId);
        checkNewEvents(context, currentUserId);
    }

    private void checkFollowRequests(Context context, String currentUserId) {
        FirebaseFirestore.getInstance()
                .collection("follow_requests")
                .whereEqualTo("toUserId", currentUserId)
                .whereEqualTo("status", "pending")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        String fromUserId = doc.getString("fromUserId");
                        fetchUserEmailAndNotify(context, fromUserId, doc.getId(), "Follow Request");
                    }
                })
                .addOnFailureListener(e -> {
                    android.util.Log.e("AlarmReceiver", "Error fetching follow requests: " + e.getMessage());
                });
    }

    private void checkNewEvents(Context context, String currentUserId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users")
                .document(currentUserId)
                .get()
                .addOnSuccessListener(userDoc -> {
                    if (userDoc.exists()) {
                        Long lastCheckedMillis = userDoc.getLong("lastChecked");
                        long lastChecked = (lastCheckedMillis != null) ? lastCheckedMillis : 0;

                        @SuppressWarnings("unchecked")
                        List<String> following = (List<String>) userDoc.get("following");
                        if (following != null && !following.isEmpty()) {
                            db.collection("events")
                                    .whereIn("userId", following)
                                    .whereEqualTo("isPublic", true)
                                    .whereGreaterThan("timestamp", lastChecked)
                                    .get()
                                    .addOnSuccessListener(eventSnapshot -> {
                                        for (QueryDocumentSnapshot eventDoc : eventSnapshot) {
                                            String eventId = eventDoc.getId();
                                            String title = eventDoc.getString("title");
                                            showNotification(context, "New Event: " + title,
                                                    "A user you follow added a public event!", eventId);
                                        }
                                        // Update lastChecked to current time
                                        db.collection("users")
                                                .document(currentUserId)
                                                .update("lastChecked", System.currentTimeMillis())
                                                .addOnFailureListener(e -> {
                                                    android.util.Log.e("AlarmReceiver", "Error updating lastChecked: " + e.getMessage());
                                                });
                                    })
                                    .addOnFailureListener(e -> {
                                        android.util.Log.e("AlarmReceiver", "Error fetching events: " + e.getMessage());
                                    });
                        }
                    }
                });
    }

    private void fetchUserEmailAndNotify(Context context, String fromUserId, String requestId, String notificationType) {
        FirebaseFirestore.getInstance()
                .collection("users")
                .document(fromUserId)
                .get()
                .addOnSuccessListener(userDoc -> {
                    String fromEmail = userDoc.getString("email");
                    if (fromEmail != null) {
                        String title = notificationType.equals("Follow Request") ?
                                "Follow request from " + fromEmail : "New Event Alert";
                        String message = notificationType.equals("Follow Request") ?
                                "Click to confirm or reject" : "A user you follow added a public event!";
                        showNotification(context, title, message, null);
                    }
                })
                .addOnFailureListener(e -> {
                    android.util.Log.e("AlarmReceiver", "Error fetching user email: " + e.getMessage());
                    showNotification(context, "Follow request", "A user has requested to follow you", null);
                });
    }

    private void showNotification(Context context, String title, String message, String eventId) {
        NotificationHelper.createNotificationChannel(context);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "follow_channel")
                .setSmallIcon(R.drawable.notification_icon)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        // Add intent to open appropriate activity
        Intent intent;
        if (eventId != null) {
            // For event notifications, open EventDetailActivity
            intent = new Intent(context, EventDetailActivity.class);
            intent.putExtra("eventId", eventId);
        } else {
            // For follow request notifications, open FollowNotificationsActivity
            intent = new Intent(context, FollowNotificationsActivity.class);
        }
        
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 
            (int) System.currentTimeMillis(), 
            intent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
            builder.setContentIntent(pendingIntent);

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
                        != PackageManager.PERMISSION_GRANTED) {
                    android.util.Log.w("AlarmReceiver", "POST_NOTIFICATIONS permission not granted");
                    return;
                }
            }
            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
            int notificationId = (int) System.currentTimeMillis() + (int) (Math.random() * 1000);
            notificationManager.notify(notificationId, builder.build());
        } catch (SecurityException e) {
            android.util.Log.e("AlarmReceiver", "SecurityException posting notification: " + e.getMessage());
        }
    }
}