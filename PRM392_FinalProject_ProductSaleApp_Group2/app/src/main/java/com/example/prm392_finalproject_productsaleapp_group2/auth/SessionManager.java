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

    private final SharedPreferences sharedPreferences;

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
        boolean loggedIn = !TextUtils.isEmpty(token);
        Log.d("SessionManager", "saveUserData() - token: " + token + ", userId: " + userId + ", loggedIn: " + loggedIn);
        sharedPreferences.edit()
                .putString(KEY_AUTH_TOKEN, token)
                .putInt(KEY_USER_ID, userId)
                .putBoolean(KEY_IS_LOGGED_IN, loggedIn)
                .apply();
    }

    public String getAuthToken() {
        return sharedPreferences.getString(KEY_AUTH_TOKEN, null);
    }

    public int getUserId() {
        return sharedPreferences.getInt(KEY_USER_ID, -1);
    }

    public boolean isLoggedIn() {
        String token = getAuthToken();
        boolean hasToken = !TextUtils.isEmpty(token);
        boolean isLoggedInFlag = sharedPreferences.getBoolean(KEY_IS_LOGGED_IN, false);
        boolean result = hasToken || isLoggedInFlag;
        
        Log.d("SessionManager", "isLoggedIn() - Token: " + token + ", hasToken: " + hasToken + 
              ", isLoggedInFlag: " + isLoggedInFlag + ", result: " + result);
        
        return result;
    }

    public void clear() {
        sharedPreferences.edit().clear().apply();
    }
}


