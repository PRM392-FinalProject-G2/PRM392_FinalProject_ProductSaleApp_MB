package com.example.prm392_finalproject_productsaleapp_group2.utils;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.view.animation.ScaleAnimation;

import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.prm392_finalproject_productsaleapp_group2.R;
import com.example.prm392_finalproject_productsaleapp_group2.auth.ProfileActivity;
import com.example.prm392_finalproject_productsaleapp_group2.cart.CartActivity;
import com.example.prm392_finalproject_productsaleapp_group2.chat.ChatActivity;
import com.example.prm392_finalproject_productsaleapp_group2.map.MapActivity;
import com.example.prm392_finalproject_productsaleapp_group2.product.HomeActivity;

public class NavigationBarUtil {

    public static void setupNavigationBar(Activity activity) {
        // Profile button
        View profileBtn = activity.findViewById(R.id.btn_profile);
        if (profileBtn != null) {
            profileBtn.setOnClickListener(v -> {
                if (!(activity instanceof ProfileActivity)) {
                    Intent intent = new Intent(activity, ProfileActivity.class);
                    // Remove activity transition animation
                    intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                    activity.startActivity(intent);
                    activity.overridePendingTransition(0, 0);
                }
            });
        }

        // Chat button
        View chatBtn = activity.findViewById(R.id.btn_chat);
        if (chatBtn != null) {
            chatBtn.setOnClickListener(v -> {
                if (!(activity instanceof ChatActivity)) {
                    Intent intent = new Intent(activity, ChatActivity.class);
                    // Remove activity transition animation
                    intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                    activity.startActivity(intent);
                    activity.overridePendingTransition(0, 0);
                }
            });
        }

        // Home button
        View homeBtn = activity.findViewById(R.id.btn_home);
        if (homeBtn != null) {
            homeBtn.setOnClickListener(v -> {
                if (!(activity instanceof HomeActivity)) {
                    Intent intent = new Intent(activity, HomeActivity.class);
                    // Remove activity transition animation
                    intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                    activity.startActivity(intent);
                    activity.overridePendingTransition(0, 0);
                }
            });
        }

        // Map button
        View mapBtn = activity.findViewById(R.id.btn_map);
        if (mapBtn != null) {
            mapBtn.setOnClickListener(v -> {
                if (!(activity instanceof MapActivity)) {
                    Intent intent = new Intent(activity, MapActivity.class);
                    // Remove activity transition animation
                    intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                    activity.startActivity(intent);
                    activity.overridePendingTransition(0, 0);
                }
            });
        }

        // Cart button
        View cartBtn = activity.findViewById(R.id.btn_cart);
        if (cartBtn != null) {
            cartBtn.setOnClickListener(v -> {
                if (!(activity instanceof CartActivity)) {
                    Intent intent = new Intent(activity, CartActivity.class);
                    // Remove activity transition animation
                    intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                    activity.startActivity(intent);
                    activity.overridePendingTransition(0, 0);
                }
            });
        }

        // Ensure bottom navigation sits at the very bottom (edge-to-edge) but pads content
        View bottomNav = activity.findViewById(R.id.bottom_navigation);
        if (bottomNav != null) {
            ViewCompat.setOnApplyWindowInsetsListener(bottomNav, (v, insets) -> {
                Insets sys = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                Object tag = v.getTag(R.id.bottom_navigation);
                int baseBottomPadding;
                if (tag instanceof Integer) {
                    baseBottomPadding = (Integer) tag;
                } else {
                    baseBottomPadding = v.getPaddingBottom();
                    v.setTag(R.id.bottom_navigation, baseBottomPadding);
                }
                v.setPadding(v.getPaddingLeft(), v.getPaddingTop(), v.getPaddingRight(), baseBottomPadding + sys.bottom);
                return insets;
            });
        }
    }

    /**
     * Highlight the current active navigation button
     */
    public static void setActiveNavigationButton(Activity activity, String activeButton) {
        // Reset all buttons first
        resetNavigationButtons(activity);

        // Set active button based on parameter
        switch (activeButton) {
            case "profile":
                highlightButton(activity.findViewById(R.id.btn_profile));
                break;
            case "chat":
                highlightButton(activity.findViewById(R.id.btn_chat));
                break;
            case "home":
                highlightButton(activity.findViewById(R.id.btn_home));
                break;
            case "map":
                highlightButton(activity.findViewById(R.id.btn_map));
                break;
            case "cart":
                highlightButton(activity.findViewById(R.id.btn_cart));
                break;
        }
    }

    private static void resetNavigationButtons(Activity activity) {
        // Reset all button styles to default
        View profileBtn = activity.findViewById(R.id.btn_profile);
        View chatBtn = activity.findViewById(R.id.btn_chat);
        View homeBtn = activity.findViewById(R.id.btn_home);
        View mapBtn = activity.findViewById(R.id.btn_map);
        View cartBtn = activity.findViewById(R.id.btn_cart);

        if (profileBtn != null) {
            profileBtn.setBackgroundResource(R.drawable.nav_button_default_background);
            profileBtn.setAlpha(1.0f);
        }
        if (chatBtn != null) {
            chatBtn.setBackgroundResource(R.drawable.nav_button_default_background);
            chatBtn.setAlpha(1.0f);
        }
        if (homeBtn != null) {
            homeBtn.setBackgroundResource(R.drawable.nav_button_default_background);
            homeBtn.setAlpha(1.0f);
        }
        if (mapBtn != null) {
            mapBtn.setBackgroundResource(R.drawable.nav_button_default_background);
            mapBtn.setAlpha(1.0f);
        }
        if (cartBtn != null) {
            cartBtn.setBackgroundResource(R.drawable.nav_button_default_background);
            cartBtn.setAlpha(1.0f);
        }
    }

    private static void highlightButton(View button) {
        if (button != null) {
            // Set active background
            button.setBackgroundResource(R.drawable.nav_button_active_background);
            
            // Add subtle animation
            ScaleAnimation scaleAnimation = new ScaleAnimation(
                    1.0f, 1.05f, 1.0f, 1.05f,
                    ScaleAnimation.RELATIVE_TO_SELF, 0.5f,
                    ScaleAnimation.RELATIVE_TO_SELF, 0.5f
            );
            scaleAnimation.setDuration(200);
            scaleAnimation.setFillAfter(false);
            button.startAnimation(scaleAnimation);
        }
    }

    /**
     * Override back press to remove animation
     */
    public static void finishActivityWithoutAnimation(Activity activity) {
        activity.finish();
        activity.overridePendingTransition(0, 0);
    }
}
