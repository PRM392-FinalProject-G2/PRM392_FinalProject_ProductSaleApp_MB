package com.example.prm392_finalproject_productsaleapp_group2.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import me.leolin.shortcutbadger.ShortcutBadger;

/**
 * Helper class for managing notifications and app icon badge count
 */
public class NotificationHelper {

    private static final String TAG = "NotificationHelper";
    private static final String PREFS_NAME = "NotificationPrefs";
    private static final String KEY_BADGE_COUNT = "badge_count";

    /**
     * Update the app icon badge with the given count
     * @param context Application context
     * @param count Badge count (0 to remove badge)
     */
    public static void updateBadge(Context context, int count) {
        try {
            // Ensure count is non-negative
            int badgeCount = Math.max(0, count);
            
            // Update badge using ShortcutBadger
            boolean success = ShortcutBadger.applyCount(context, badgeCount);
            
            if (success) {
                Log.d(TAG, "Badge updated successfully: " + badgeCount);
                // Save badge count to preferences
                saveBadgeCount(context, badgeCount);
            } else {
                Log.w(TAG, "Badge update failed. Device may not support badges.");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error updating badge", e);
        }
    }

    /**
     * Increment the current badge count by 1
     * @param context Application context
     */
    public static void incrementBadge(Context context) {
        int currentCount = getBadgeCount(context);
        updateBadge(context, currentCount + 1);
    }

    /**
     * Decrement the current badge count by 1
     * @param context Application context
     */
    public static void decrementBadge(Context context) {
        int currentCount = getBadgeCount(context);
        updateBadge(context, Math.max(0, currentCount - 1));
    }

    /**
     * Clear/remove the app icon badge
     * @param context Application context
     */
    public static void clearBadge(Context context) {
        updateBadge(context, 0);
    }

    /**
     * Get the current badge count from preferences
     * @param context Application context
     * @return Current badge count
     */
    public static int getBadgeCount(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getInt(KEY_BADGE_COUNT, 0);
    }

    /**
     * Save badge count to preferences
     * @param context Application context
     * @param count Badge count to save
     */
    private static void saveBadgeCount(Context context, int count) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putInt(KEY_BADGE_COUNT, count).apply();
    }

    /**
     * Check if the device supports badge notifications
     * @param context Application context
     * @return true if supported, false otherwise
     */
    public static boolean isBadgeSupported(Context context) {
        return ShortcutBadger.isBadgeCounterSupported(context);
    }
}

