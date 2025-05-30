package com.example.noam_final;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

public class AppAlarmManager {
    private static final String TAG = "AlarmManager";
    private static final int ALARM_REQUEST_CODE = 123;
    private static final long DEFAULT_INTERVAL = 1000 * 60 * 30; // 30 minutes in milliseconds

    private final Context context;
    private final android.app.AlarmManager alarmManager;

    public AppAlarmManager(Context context) {
        this.context = context;
        this.alarmManager = (android.app.AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
    }

    public void schedulePeriodicAlarm() {
        Intent intent = new Intent(context, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                ALARM_REQUEST_CODE,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // Cancel any existing alarm
        cancelAlarm();

        long startTime = System.currentTimeMillis() + 5000; // Start after 5 seconds

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                        android.app.AlarmManager.RTC_WAKEUP,
                        startTime,
                        pendingIntent
                );
            } else {
                alarmManager.setExact(
                        android.app.AlarmManager.RTC_WAKEUP,
                        startTime,
                        pendingIntent
                );
            }
            Log.d(TAG, "Periodic alarm scheduled successfully");
        } catch (SecurityException e) {
            Log.e(TAG, "Failed to schedule alarm: " + e.getMessage());
        }
    }

    public void scheduleRepeatingAlarm() {
        Intent intent = new Intent(context, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                ALARM_REQUEST_CODE,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // Cancel any existing alarm
        cancelAlarm();

        long startTime = System.currentTimeMillis() + 5000; // Start after 5 seconds

        try {
            alarmManager.setInexactRepeating(
                    android.app.AlarmManager.RTC_WAKEUP,
                    startTime,
                    DEFAULT_INTERVAL,
                    pendingIntent
            );
            Log.d(TAG, "Repeating alarm scheduled successfully");
        } catch (SecurityException e) {
            Log.e(TAG, "Failed to schedule repeating alarm: " + e.getMessage());
        }
    }

    public void cancelAlarm() {
        Intent intent = new Intent(context, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                ALARM_REQUEST_CODE,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        try {
            alarmManager.cancel(pendingIntent);
            Log.d(TAG, "Alarm cancelled successfully");
        } catch (SecurityException e) {
            Log.e(TAG, "Failed to cancel alarm: " + e.getMessage());
        }
    }

    public boolean isAlarmScheduled() {
        Intent intent = new Intent(context, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                ALARM_REQUEST_CODE,
                intent,
                PendingIntent.FLAG_NO_CREATE | PendingIntent.FLAG_IMMUTABLE
        );
        return pendingIntent != null;
    }
}
