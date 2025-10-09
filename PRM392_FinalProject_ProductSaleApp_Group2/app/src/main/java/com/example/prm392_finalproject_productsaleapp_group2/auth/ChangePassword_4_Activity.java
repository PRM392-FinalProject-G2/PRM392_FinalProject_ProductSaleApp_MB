package com.example.prm392_finalproject_productsaleapp_group2.auth;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.prm392_finalproject_productsaleapp_group2.R;
import com.example.prm392_finalproject_productsaleapp_group2.utils.NavigationBarUtil;

public class ChangePassword_4_Activity extends AppCompatActivity {

    private AppCompatButton btnContinue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_change_password4);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            // Let content draw edge-to-edge; apply only sides to root
            v.setPadding(systemBars.left, 0, systemBars.right, 0);

            // Add top inset to header so its background extends under status bar
            android.view.View header = findViewById(R.id.header_layout);
            if (header != null) {
                header.setPadding(
                        header.getPaddingLeft(),
                        header.getPaddingTop() + systemBars.top,
                        header.getPaddingRight(),
                        header.getPaddingBottom()
                );
            }

            // Bottom inset is handled centrally in NavigationBarUtil
            return insets;
        });

        // Setup navigation bar
        NavigationBarUtil.setupNavigationBar(this);

        // Initialize views
        btnContinue = findViewById(R.id.btn_continue);

        // Continue button - navigate back to Edit Profile
        btnContinue.setOnClickListener(v -> {
            Intent intent = new Intent(ChangePassword_4_Activity.this, EditProfileActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NO_ANIMATION);
            startActivity(intent);
            overridePendingTransition(0, 0);
            finish();
        });
    }

    @Override
    public void onBackPressed() {
        // Prevent back button - force user to click Continue
        // Or navigate to EditProfile
        Intent intent = new Intent(ChangePassword_4_Activity.this, EditProfileActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NO_ANIMATION);
        startActivity(intent);
        overridePendingTransition(0, 0);
        finish();
    }
}