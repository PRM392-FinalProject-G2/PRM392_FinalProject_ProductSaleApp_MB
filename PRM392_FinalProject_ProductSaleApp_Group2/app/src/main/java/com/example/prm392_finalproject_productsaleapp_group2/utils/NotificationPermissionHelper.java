package com.example.prm392_finalproject_productsaleapp_group2.utils;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class NotificationPermissionHelper {
    private static final String TAG = "NotificationPermission";
    private static final int PERMISSION_REQUEST_CODE = 1001;

    /**
     * Check if notification permission is granted (Android 13+)
     */
    public static boolean isPermissionGranted(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            int permission = ContextCompat.checkSelfPermission(
                    activity, 
                    Manifest.permission.POST_NOTIFICATIONS
            );
            return permission == PackageManager.PERMISSION_GRANTED;
        }
        return true; // Permission not required for older versions
    }

    /**
     * Request notification permission (Android 13+)
     */
    public static void requestPermission(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (!isPermissionGranted(activity)) {
                Log.d(TAG, "Requesting notification permission");
                ActivityCompat.requestPermissions(
                        activity,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        PERMISSION_REQUEST_CODE
                );
            } else {
                Log.d(TAG, "Notification permission already granted");
            }
        } else {
            Log.d(TAG, "Android version < 13, no permission needed");
        }
    }
}

