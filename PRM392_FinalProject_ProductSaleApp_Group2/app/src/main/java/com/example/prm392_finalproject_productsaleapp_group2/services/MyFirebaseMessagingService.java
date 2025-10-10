package com.example.prm392_finalproject_productsaleapp_group2.services;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.example.prm392_finalproject_productsaleapp_group2.R;
import com.example.prm392_finalproject_productsaleapp_group2.cart.CartActivity;
import com.example.prm392_finalproject_productsaleapp_group2.utils.NotificationHelper;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "FCMService";
    private static final String CHANNEL_ID = "default_channel";
    private static final String CHANNEL_NAME = "TechZone Notifications";

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        
        Log.d(TAG, "From: " + remoteMessage.getFrom());

        // Check if message contains notification payload
        if (remoteMessage.getNotification() != null) {
            String title = remoteMessage.getNotification().getTitle();
            String body = remoteMessage.getNotification().getBody();
            Log.d(TAG, "Notification Title: " + title);
            Log.d(TAG, "Notification Body: " + body);
        }

        // Check if message contains data payload
        if (remoteMessage.getData().size() > 0) {
            Map<String, String> data = remoteMessage.getData();
            Log.d(TAG, "Message data payload: " + data);

            String type = data.get("type");
            String badgeStr = data.get("badge");
            
            // Update badge count
            if (badgeStr != null) {
                try {
                    int badgeCount = Integer.parseInt(badgeStr);
                    NotificationHelper.updateBadge(this, badgeCount);
                    Log.d(TAG, "Updated badge count to: " + badgeCount);
                } catch (NumberFormatException e) {
                    Log.e(TAG, "Invalid badge count: " + badgeStr, e);
                }
            }

            // Handle different notification types
            if ("cart_update".equals(type)) {
                handleCartUpdateNotification(remoteMessage);
            } else if ("order_update".equals(type)) {
                handleOrderUpdateNotification(remoteMessage);
            } else {
                handleDefaultNotification(remoteMessage);
            }
        }
    }

    @Override
    public void onNewToken(String token) {
        super.onNewToken(token);
        Log.d(TAG, "Refreshed token: " + token);
        
        // Send token to backend
        FCMTokenManager.getInstance(this).sendTokenToServer(token);
    }

    private void handleCartUpdateNotification(RemoteMessage remoteMessage) {
        String title = remoteMessage.getNotification() != null ? 
                remoteMessage.getNotification().getTitle() : "Giỏ hàng đã cập nhật";
        String body = remoteMessage.getNotification() != null ? 
                remoteMessage.getNotification().getBody() : "";

        // Get badge count from data payload
        int badgeCount = NotificationHelper.getBadgeCount(this);
        
        // Create intent to open CartActivity
        Intent intent = new Intent(this, CartActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        
        showNotification(title, body, intent, 1001, badgeCount);
    }

    private void handleOrderUpdateNotification(RemoteMessage remoteMessage) {
        String title = remoteMessage.getNotification() != null ? 
                remoteMessage.getNotification().getTitle() : "Cập nhật đơn hàng";
        String body = remoteMessage.getNotification() != null ? 
                remoteMessage.getNotification().getBody() : "";

        // You can create OrderActivity intent here
        Intent intent = new Intent(this, CartActivity.class); // Change to OrderActivity when available
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        
        showNotification(title, body, intent, 1002, 0);
    }

    private void handleDefaultNotification(RemoteMessage remoteMessage) {
        String title = remoteMessage.getNotification() != null ? 
                remoteMessage.getNotification().getTitle() : "TechZone";
        String body = remoteMessage.getNotification() != null ? 
                remoteMessage.getNotification().getBody() : "";

        Intent intent = new Intent(this, CartActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        
        showNotification(title, body, intent, 1000, 0);
    }

    private void showNotification(String title, String body, Intent intent, int notificationId, int badgeCount) {
        Log.d(TAG, "Showing notification with badge count: " + badgeCount);
        
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this, 
                notificationId, 
                intent,
                PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification_icon) // TZ monochrome icon
                .setContentTitle(title)
                .setContentText(body)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setBadgeIconType(NotificationCompat.BADGE_ICON_SMALL); // Use small icon for badge
        
        // Show badge count as small number in notification
        if (badgeCount > 0) {
            notificationBuilder.setNumber(badgeCount);
            Log.d(TAG, "Set notification number to: " + badgeCount);
        }
        
        // Simple notification style
        notificationBuilder.setStyle(new NotificationCompat.BigTextStyle().bigText(body));

        NotificationManager notificationManager = 
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (notificationManager != null) {
            notificationManager.notify(notificationId, notificationBuilder.build());
            Log.d(TAG, "Notification displayed successfully");
        }
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Notifications from TechZone");
            channel.enableLights(true);
            channel.enableVibration(true);
            channel.setShowBadge(true); // Enable badge for this channel
            
            Log.d(TAG, "Creating notification channel with badge enabled");

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
                Log.d(TAG, "Notification channel created successfully");
            }
        }
    }
}

