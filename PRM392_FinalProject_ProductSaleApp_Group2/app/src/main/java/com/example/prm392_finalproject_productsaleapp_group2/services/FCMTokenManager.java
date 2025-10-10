package com.example.prm392_finalproject_productsaleapp_group2.services;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.NonNull;

import com.example.prm392_finalproject_productsaleapp_group2.auth.SessionManager;
import com.example.prm392_finalproject_productsaleapp_group2.models.FCMTokenRequest;
import com.example.prm392_finalproject_productsaleapp_group2.models.FCMTokenResponse;
import com.example.prm392_finalproject_productsaleapp_group2.net.FCMApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.messaging.FirebaseMessaging;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Manager class for handling FCM token registration and lifecycle
 */
public class FCMTokenManager {

    private static final String TAG = "FCMTokenManager";
    private static final String PREFS_NAME = "FCMPrefs";
    private static final String KEY_FCM_TOKEN = "fcm_token";
    private static final String KEY_TOKEN_SENT = "fcm_token_sent";

    private static FCMTokenManager instance;
    private final Context context;
    private final SharedPreferences prefs;

    private FCMTokenManager(Context context) {
        this.context = context.getApplicationContext();
        this.prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public static synchronized FCMTokenManager getInstance(Context context) {
        if (instance == null) {
            instance = new FCMTokenManager(context);
        }
        return instance;
    }

    /**
     * Initialize FCM token - get token and register with backend
     */
    public void initializeFCM() {
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(new OnCompleteListener<String>() {
                    @Override
                    public void onComplete(@NonNull Task<String> task) {
                        if (!task.isSuccessful()) {
                            Log.w(TAG, "Fetching FCM registration token failed", task.getException());
                            return;
                        }

                        // Get new FCM registration token
                        String token = task.getResult();
                        Log.d(TAG, "FCM Token: " + token);

                        // Save token locally
                        saveTokenLocally(token);

                        // Send to backend if user is logged in
                        sendTokenToServer(token);
                    }
                });
    }

    /**
     * Send FCM token to backend server
     */
    public void sendTokenToServer(String fcmToken) {
        if (fcmToken == null || fcmToken.isEmpty()) {
            Log.w(TAG, "Cannot send null or empty token to server");
            return;
        }

        // Get user ID from session
        SessionManager sessionManager = new SessionManager(context);
        int userId = sessionManager.getUserId();

        Log.d(TAG, "sendTokenToServer - userId: " + userId + ", fcmToken: " + fcmToken.substring(0, Math.min(20, fcmToken.length())) + "...");

        if (userId <= 0) {
            Log.w(TAG, "User not logged in (userId=" + userId + "). Token will be sent when user logs in.");
            return;
        }

        // Check if token already sent
        String lastSentToken = prefs.getString(KEY_FCM_TOKEN, "");
        boolean tokenSent = prefs.getBoolean(KEY_TOKEN_SENT, false);

        Log.d(TAG, "Token already sent: " + tokenSent + ", Same token: " + fcmToken.equals(lastSentToken));

        if (tokenSent && fcmToken.equals(lastSentToken)) {
            Log.d(TAG, "Token already sent to server. Skipping.");
            return;
        }

        // Send token to backend
        Log.d(TAG, "Sending FCM token to backend for userId: " + userId);
        FCMTokenRequest request = new FCMTokenRequest(userId, fcmToken);
        FCMApiClient.getApiService().registerToken(request).enqueue(new Callback<FCMTokenResponse>() {
            @Override
            public void onResponse(Call<FCMTokenResponse> call, Response<FCMTokenResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Log.d(TAG, "✅ FCM token registered successfully with backend. TokenId: " + response.body().getTokenId());
                    // Mark as sent
                    prefs.edit()
                            .putBoolean(KEY_TOKEN_SENT, true)
                            .putString(KEY_FCM_TOKEN, fcmToken)
                            .apply();
                } else {
                    Log.e(TAG, "❌ Failed to register FCM token. Response code: " + response.code());
                    try {
                        String errorBody = response.errorBody() != null ? response.errorBody().string() : "No error body";
                        Log.e(TAG, "Error response: " + errorBody);
                    } catch (Exception e) {
                        Log.e(TAG, "Could not read error body", e);
                    }
                }
            }

            @Override
            public void onFailure(Call<FCMTokenResponse> call, Throwable t) {
                Log.e(TAG, "❌ Error sending FCM token to backend: " + t.getMessage(), t);
            }
        });
    }

    /**
     * Deactivate FCM token on logout
     */
    public void deactivateToken() {
        String fcmToken = getToken();
        if (fcmToken == null || fcmToken.isEmpty()) {
            Log.w(TAG, "No FCM token to deactivate");
            return;
        }

        SessionManager sessionManager = new SessionManager(context);
        int userId = sessionManager.getUserId();

        if (userId <= 0) {
            Log.w(TAG, "User not logged in. Cannot deactivate token.");
            clearTokenData();
            return;
        }

        // Send deactivation request to backend
        FCMTokenRequest request = new FCMTokenRequest(userId, fcmToken);
        FCMApiClient.getApiService().deactivateToken(request).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Log.d(TAG, "FCM token deactivated successfully");
                    clearTokenData();
                } else {
                    Log.e(TAG, "Failed to deactivate FCM token. Response code: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Log.e(TAG, "Error deactivating FCM token", t);
            }
        });
    }

    /**
     * Save token locally to SharedPreferences
     */
    private void saveTokenLocally(String token) {
        prefs.edit().putString(KEY_FCM_TOKEN, token).apply();
        Log.d(TAG, "FCM token saved locally");
    }

    /**
     * Get locally stored token
     */
    public String getToken() {
        return prefs.getString(KEY_FCM_TOKEN, null);
    }

    /**
     * Clear token data
     */
    private void clearTokenData() {
        prefs.edit()
                .remove(KEY_FCM_TOKEN)
                .putBoolean(KEY_TOKEN_SENT, false)
                .apply();
        Log.d(TAG, "FCM token data cleared");
    }

    /**
     * Force refresh token (useful after login)
     */
    public void refreshToken() {
        Log.d(TAG, "refreshToken() called - Resetting token sent flag and re-sending to server");
        prefs.edit().putBoolean(KEY_TOKEN_SENT, false).apply();
        
        // Try to send existing token first
        String existingToken = getToken();
        if (existingToken != null && !existingToken.isEmpty()) {
            Log.d(TAG, "Sending existing token to server");
            sendTokenToServer(existingToken);
        } else {
            Log.d(TAG, "No existing token, fetching new one");
            initializeFCM();
        }
    }
}

