package com.example.prm392_finalproject_productsaleapp_group2.product;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.prm392_finalproject_productsaleapp_group2.R;
import com.example.prm392_finalproject_productsaleapp_group2.auth.LoginActivity;
import com.example.prm392_finalproject_productsaleapp_group2.auth.SessionManager;
import com.example.prm392_finalproject_productsaleapp_group2.utils.NavigationBarUtil;

public class HomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            // Let content draw edge-to-edge; no bottom padding here
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0);
            return insets;
        });

        // Check login status
        SessionManager sessionManager = new SessionManager(this);
        boolean isLoggedIn = sessionManager.isLoggedIn();
        
        Log.d("HomeActivity", "onCreate() - isLoggedIn: " + isLoggedIn);
        
        if (!isLoggedIn) {
            Log.d("HomeActivity", "User not logged in, redirecting to LoginActivity");
            Toast.makeText(this, "Vui lòng đăng nhập", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            return;
        }
        
        Log.d("HomeActivity", "User is logged in, continuing with HomeActivity setup");

        // Setup navigation bar click listeners
        NavigationBarUtil.setupNavigationBar(this);
        
        // Highlight current active button
        NavigationBarUtil.setActiveNavigationButton(this, "home");
    }

    @Override
    public void onBackPressed() {
        // Disable back button for main navigation activity
        // Or you can exit the app
        super.onBackPressed();
    }
}