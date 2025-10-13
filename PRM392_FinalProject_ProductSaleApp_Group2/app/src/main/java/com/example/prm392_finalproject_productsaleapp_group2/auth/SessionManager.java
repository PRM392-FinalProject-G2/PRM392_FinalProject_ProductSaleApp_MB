package com.example.prm392_finalproject_productsaleapp_group2.auth;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.util.Log;

/**
 * Simple session manager backed by SharedPreferences.
 * Stores an auth token or a boolean flag to determine login state.
 */
public class SessionManager {

    private static final String PREFS_NAME = "app_prefs";
    private static final String KEY_AUTH_TOKEN = "auth_token";
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_REMEMBER = "remember_me";

    private final SharedPreferences sharedPreferences;
    
    // Temporary session storage (will be cleared when app is killed)
    private static String tempToken = null;
    private static int tempUserId = -1;

    public SessionManager(Context context) {
        this.sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public void saveAuthToken(String token) {
        boolean loggedIn = !TextUtils.isEmpty(token);
        sharedPreferences.edit()
                .putString(KEY_AUTH_TOKEN, token)
                .putBoolean(KEY_IS_LOGGED_IN, loggedIn)
                .apply();
    }

    public void saveUserData(String token, int userId) {
        saveUserData(token, userId, true); // Default to remember=true for backward compatibility
    }

    public void saveUserData(String token, int userId, boolean remember) {
        boolean loggedIn = !TextUtils.isEmpty(token);
        Log.d("SessionManager", "saveUserData() - token: " + token + ", userId: " + userId + 
                ", remember: " + remember + ", loggedIn: " + loggedIn);
        
        if (remember) {
            // Save to SharedPreferences (persistent)
            sharedPreferences.edit()
                    .putString(KEY_AUTH_TOKEN, token)
                    .putInt(KEY_USER_ID, userId)
                    .putBoolean(KEY_IS_LOGGED_IN, loggedIn)
                    .putBoolean(KEY_REMEMBER, true)
                    .apply();
            // Clear temp storage
            tempToken = null;
            tempUserId = -1;
        } else {
            // Save to temp storage only (will be lost when app is killed)
            tempToken = token;
            tempUserId = userId;
            // Clear SharedPreferences
            sharedPreferences.edit()
                    .remove(KEY_AUTH_TOKEN)
                    .remove(KEY_USER_ID)
                    .putBoolean(KEY_IS_LOGGED_IN, false)
                    .putBoolean(KEY_REMEMBER, false)
                    .apply();
        }
    }

    public String getAuthToken() {
        // Check temp storage first (for non-remember sessions)
        if (tempToken != null) {
            return tempToken;
        }
        // Then check SharedPreferences (for remember sessions)
        return sharedPreferences.getString(KEY_AUTH_TOKEN, null);
    }

    public int getUserId() {
        // Check temp storage first (for non-remember sessions)
        if (tempUserId != -1) {
            return tempUserId;
        }
        // Then check SharedPreferences (for remember sessions)
        return sharedPreferences.getInt(KEY_USER_ID, -1);
    }

    public boolean isLoggedIn() {
        // Check temp storage first (for current session without remember)
        if (tempToken != null && !tempToken.isEmpty()) {
            Log.d("SessionManager", "isLoggedIn() - Using temp session: true");
            return true;
        }
        
        // Check SharedPreferences only if remember=true
        boolean remember = sharedPreferences.getBoolean(KEY_REMEMBER, false);
        if (remember) {
            String token = sharedPreferences.getString(KEY_AUTH_TOKEN, null);
            boolean hasToken = !TextUtils.isEmpty(token);
            Log.d("SessionManager", "isLoggedIn() - Remember=true, hasToken: " + hasToken);
            return hasToken;
        }
        
        Log.d("SessionManager", "isLoggedIn() - No temp session and remember=false: false");
        return false;
    }

    public void clear() {
        sharedPreferences.edit().clear().apply();
        // Also clear temp storage
        tempToken = null;
        tempUserId = -1;
    }
}


