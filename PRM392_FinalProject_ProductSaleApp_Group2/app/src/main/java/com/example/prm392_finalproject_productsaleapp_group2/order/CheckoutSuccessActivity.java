package com.example.prm392_finalproject_productsaleapp_group2.order;

import android.os.Bundle;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.prm392_finalproject_productsaleapp_group2.R;

public class CheckoutSuccessActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        // Set status bar transparent to let gradient show through
        getWindow().setStatusBarColor(android.graphics.Color.TRANSPARENT);

        setContentView(R.layout.activity_checkout_success);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            // Don't add padding to let gradient extend to status bar
            return WindowInsetsCompat.CONSUMED;
        });

        Button track = findViewById(R.id.btn_track_order);
        Button cont = findViewById(R.id.btn_continue_shopping);
        if (track != null) {
            track.setOnClickListener(v -> {
                // Always go to order list with "Pending" (Chờ giao hàng) tab pre-selected
                android.content.Intent intent = new android.content.Intent(
                        CheckoutSuccessActivity.this,
                        com.example.prm392_finalproject_productsaleapp_group2.order.ListOrderActivity.class
                );
                intent.putExtra("selectedTab", "Pending");
                startActivity(intent);
                finish();
            });
        }
        if (cont != null) {
            cont.setOnClickListener(v -> {
                android.content.Intent intent = new android.content.Intent(
                        CheckoutSuccessActivity.this,
                        com.example.prm392_finalproject_productsaleapp_group2.product.HomeActivity.class
                );
                intent.addFlags(android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP | android.content.Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            });
        }
    }
}