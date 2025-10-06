package com.example.prm392_finalproject_productsaleapp_group2.auth;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

/**
 * Simple session manager backed by SharedPreferences.
 * Stores an auth token or a boolean flag to determine login state.
 */
public class SessionManager {

    private static final String PREFS_NAME = "app_prefs";
    private static final String KEY_AUTH_TOKEN = "auth_token";
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";

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

    public String getAuthToken() {
        return sharedPreferences.getString(KEY_AUTH_TOKEN, null);
    }

    public boolean isLoggedIn() {
        String token = getAuthToken();
        return (!TextUtils.isEmpty(token)) || sharedPreferences.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    public void clear() {
        sharedPreferences.edit().clear().apply();
    }
}


