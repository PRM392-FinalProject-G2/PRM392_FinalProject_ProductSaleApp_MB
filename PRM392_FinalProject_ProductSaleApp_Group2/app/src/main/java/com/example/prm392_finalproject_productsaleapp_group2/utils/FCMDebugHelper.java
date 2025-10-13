package com.example.prm392_finalproject_productsaleapp_group2.utils;

import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import android.util.Log;

import com.example.prm392_finalproject_productsaleapp_group2.auth.SessionManager;
import com.example.prm392_finalproject_productsaleapp_group2.services.FCMTokenManager;
import com.google.firebase.messaging.FirebaseMessaging;

/**
 * Helper class to debug FCM and notification issues
 */
public class FCMDebugHelper {
    private static final String TAG = "FCMDebugHelper";

    public static void printDebugInfo(Context context) {
        Log.d(TAG, "=== FCM Debug Info ===");
        
        // 1. Check Android version
        Log.d(TAG, "Android Version: " + Build.VERSION.SDK_INT);
        
        // 2. Check notification permission
        NotificationManager notificationManager = 
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            boolean enabled = notificationManager.areNotificationsEnabled();
            Log.d(TAG, "Notifications Enabled: " + enabled);
        }
        
        // 3. Check user logged in
        SessionManager sessionManager = new SessionManager(context);
        int userId = sessionManager.getUserId();
        boolean isLoggedIn = sessionManager.isLoggedIn();
        Log.d(TAG, "User Logged In: " + isLoggedIn);
        Log.d(TAG, "User ID: " + userId);
        
        // 4. Check FCM token
        String savedToken = FCMTokenManager.getInstance(context).getToken();
        Log.d(TAG, "Saved FCM Token: " + (savedToken != null ? savedToken.substring(0, Math.min(30, savedToken.length())) + "..." : "NULL"));
        
        // 5. Fetch current FCM token
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        String currentToken = task.getResult();
                        Log.d(TAG, "Current FCM Token: " + currentToken.substring(0, Math.min(30, currentToken.length())) + "...");
                        Log.d(TAG, "Token Match: " + currentToken.equals(savedToken));
                    } else {
                        Log.e(TAG, "Failed to get FCM token", task.getException());
                    }
                });
        
        // 6. Check badge support
        boolean badgeSupported = NotificationHelper.isBadgeSupported(context);
        Log.d(TAG, "Badge Supported: " + badgeSupported);
        
        Log.d(TAG, "======================");
    }
}

