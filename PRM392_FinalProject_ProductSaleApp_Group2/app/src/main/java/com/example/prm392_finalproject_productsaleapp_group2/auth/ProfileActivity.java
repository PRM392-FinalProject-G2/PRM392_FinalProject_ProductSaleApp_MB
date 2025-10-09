package com.example.prm392_finalproject_productsaleapp_group2.auth;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.example.prm392_finalproject_productsaleapp_group2.R;
import com.example.prm392_finalproject_productsaleapp_group2.models.FilterResponse;
import com.example.prm392_finalproject_productsaleapp_group2.models.UserResponse;
import com.example.prm392_finalproject_productsaleapp_group2.net.ProfileApiClient;
import com.example.prm392_finalproject_productsaleapp_group2.services.ProfileApiService;
import com.example.prm392_finalproject_productsaleapp_group2.utils.NavigationBarUtil;
import com.example.prm392_finalproject_productsaleapp_group2.auth.SessionManager;
import com.example.prm392_finalproject_productsaleapp_group2.order.ListOrderActivity;
import com.example.prm392_finalproject_productsaleapp_group2.wishlist.WishlistActivity;
import com.example.prm392_finalproject_productsaleapp_group2.voucher.VoucherActivity;
import com.example.prm392_finalproject_productsaleapp_group2.payment.PaymentActivity;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileActivity extends AppCompatActivity {

    private static final String TAG = "ProfileActivity";
    private ImageView ivProfileAvatar;
    private TextView tvUsername, tvOrdersCount, tvWishlistCount, tvVouchersCount;
    private Button btnEditProfile;
    private LinearLayout btnLogout;
    private LinearLayout menuOrders;
    private LinearLayout menuWishlist;
    private LinearLayout menuVouchers;
    private LinearLayout menuPayment;
    private ProfileApiService apiService;
    private SessionManager sessionManager;
    private int userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_profile);
        
        // Handle window insets for edge-to-edge display
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());

            // Header receives top inset so gradient can go under status bar
            View headerLayout = findViewById(R.id.header_layout);
            if (headerLayout != null) {
                headerLayout.setPadding(
                        headerLayout.getPaddingLeft(),
                        headerLayout.getPaddingTop() + systemBars.top,
                        headerLayout.getPaddingRight(),
                        headerLayout.getPaddingBottom()
                );
            }

            // Do not add bottom padding here; NavigationBarUtil gives it to bottom_navigation
            v.setPadding(0, 0, 0, 0);
            return insets;
        });

        // Initialize views
        initViews();
        
        // Initialize API service and session manager
        apiService = ProfileApiClient.getInstance().getApiService();
        sessionManager = new SessionManager(this);
        
        // Get user ID from session
        userId = sessionManager.getUserId();
        
        Log.d("ProfileActivity", "onCreate() - userId: " + userId);
        Log.d("ProfileActivity", "onCreate() - sessionManager.isLoggedIn(): " + sessionManager.isLoggedIn());
        
        // Check if user is logged in
        if (!sessionManager.isLoggedIn() || userId == -1) {
            Log.d("ProfileActivity", "Login check failed - isLoggedIn: " + sessionManager.isLoggedIn() + ", userId: " + userId);
            Toast.makeText(this, "Vui lòng đăng nhập", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        
        Log.d("ProfileActivity", "Login check passed, continuing with ProfileActivity setup");
        
        // Setup navigation bar
        NavigationBarUtil.setupNavigationBar(this);
        NavigationBarUtil.setActiveNavigationButton(this, "profile");
        
        // Load profile data
        loadProfileData();
        
        // Setup click listeners
        setupClickListeners();
    }

    private void initViews() {
        ivProfileAvatar = findViewById(R.id.iv_profile_avatar);
        tvUsername = findViewById(R.id.tv_username);
        tvOrdersCount = findViewById(R.id.tv_orders_count);
        tvWishlistCount = findViewById(R.id.tv_wishlist_count);
        tvVouchersCount = findViewById(R.id.tv_vouchers_count);
        btnEditProfile = findViewById(R.id.btn_edit_profile);
        btnLogout = findViewById(R.id.btn_logout);
        menuOrders = findViewById(R.id.menu_orders);
        menuWishlist = findViewById(R.id.menu_wishlist);
        menuVouchers = findViewById(R.id.menu_vouchers);
        menuPayment = findViewById(R.id.menu_payment);
    }

    private void setupClickListeners() {
        btnEditProfile.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, EditProfileActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            startActivity(intent);
            overridePendingTransition(0, 0);
        });
        
        btnLogout.setOnClickListener(v -> {
            showLogoutDialog();
        });

        if (menuOrders != null) {
            menuOrders.setOnClickListener(v -> {
                Intent intent = new Intent(ProfileActivity.this, ListOrderActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                startActivity(intent);
                overridePendingTransition(0, 0);
            });
        }

        if (menuWishlist != null) {
            menuWishlist.setOnClickListener(v -> {
                Intent intent = new Intent(ProfileActivity.this, WishlistActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                startActivity(intent);
                overridePendingTransition(0, 0);
            });
        }

        if (menuVouchers != null) {
            menuVouchers.setOnClickListener(v -> {
                Intent intent = new Intent(ProfileActivity.this, VoucherActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                startActivity(intent);
                overridePendingTransition(0, 0);
            });
        }

        if (menuPayment != null) {
            menuPayment.setOnClickListener(v -> {
                Intent intent = new Intent(ProfileActivity.this, PaymentActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                startActivity(intent);
                overridePendingTransition(0, 0);
            });
        }
    }
    
    private void showLogoutDialog() {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Đăng xuất")
                .setMessage("Bạn có chắc chắn muốn đăng xuất?")
                .setPositiveButton("Đăng xuất", (dialog, which) -> {
                    performLogout();
                })
                .setNegativeButton("Hủy", (dialog, which) -> {
                    dialog.dismiss();
                })
                .show();
    }
    
    private void performLogout() {
        // Clear session data
        sessionManager.clear();
        
        // Show logout message
        Toast.makeText(this, "Đã đăng xuất thành công", Toast.LENGTH_SHORT).show();
        
        // Navigate to LoginActivity and clear back stack
        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void loadProfileData() {
        // Load user info
        loadUserInfo();
        
        // Load statistics
        loadOrdersCount();
        loadWishlistCount();
        loadVouchersCount();
    }

    private void loadUserInfo() {
        // Get access token from session manager
        String token = sessionManager.getAuthToken();
        if (token == null || token.isEmpty()) {
            Toast.makeText(this, "Không tìm thấy access token", Toast.LENGTH_SHORT).show();
            return;
        }
        String accessToken = "Bearer " + token;
        
        Call<UserResponse> call = apiService.getUserInfo(accessToken, userId);
        call.enqueue(new Callback<UserResponse>() {
            @Override
            public void onResponse(Call<UserResponse> call, Response<UserResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    UserResponse user = response.body();
                    tvUsername.setText(user.getUsername());
                    
                    // Load avatar image
                    String avatarUrl = user.getAvatarUrl();
                    Log.d(TAG, "User avatar URL: " + avatarUrl);
                    Log.d(TAG, "Username: " + user.getUsername());
                    Log.d(TAG, "Role: " + user.getRole());
                    
                    if (avatarUrl != null && !avatarUrl.isEmpty()) {
                        // Load avatar from URL
                        Glide.with(ProfileActivity.this)
                                .load(avatarUrl)
                                .placeholder(R.drawable.profile) // Show default avatar while loading
                                .error(R.drawable.profile) // Show default avatar if load fails
                                .centerCrop() // Make it square with rounded corners
                                .into(ivProfileAvatar);
                        Log.d(TAG, "Loading avatar from URL: " + avatarUrl);
                    } else {
                        // No avatar URL, use default
                        ivProfileAvatar.setImageResource(R.drawable.profile);
                        Log.d(TAG, "No avatar URL, using default avatar");
                    }
                } else {
                    Log.e(TAG, "Failed to load user info: " + response.code());
                    Toast.makeText(ProfileActivity.this, "Không thể tải thông tin người dùng", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<UserResponse> call, Throwable t) {
                Log.e(TAG, "Error loading user info", t);
                Toast.makeText(ProfileActivity.this, "Lỗi kết nối", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadOrdersCount() {
        String token = sessionManager.getAuthToken();
        if (token == null || token.isEmpty()) return;
        String accessToken = "Bearer " + token;
        
        Call<FilterResponse<Object>> call = apiService.getOrdersCount(accessToken, userId);
        call.enqueue(new Callback<FilterResponse<Object>>() {
            @Override
            public void onResponse(Call<FilterResponse<Object>> call, Response<FilterResponse<Object>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    int count = response.body().getTotalItems();
                    tvOrdersCount.setText(String.valueOf(count));
                } else {
                    Log.e(TAG, "Failed to load orders count: " + response.code());
                    tvOrdersCount.setText("0");
                }
            }

            @Override
            public void onFailure(Call<FilterResponse<Object>> call, Throwable t) {
                Log.e(TAG, "Error loading orders count", t);
                tvOrdersCount.setText("0");
            }
        });
    }

    private void loadWishlistCount() {
        String token = sessionManager.getAuthToken();
        if (token == null || token.isEmpty()) return;
        String accessToken = "Bearer " + token;
        
        Call<FilterResponse<Object>> call = apiService.getWishlistCount(accessToken, userId);
        call.enqueue(new Callback<FilterResponse<Object>>() {
            @Override
            public void onResponse(Call<FilterResponse<Object>> call, Response<FilterResponse<Object>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    int count = response.body().getTotalItems();
                    tvWishlistCount.setText(String.valueOf(count));
                } else {
                    Log.e(TAG, "Failed to load wishlist count: " + response.code());
                    tvWishlistCount.setText("0");
                }
            }

            @Override
            public void onFailure(Call<FilterResponse<Object>> call, Throwable t) {
                Log.e(TAG, "Error loading wishlist count", t);
                tvWishlistCount.setText("0");
            }
        });
    }

    private void loadVouchersCount() {
        String token = sessionManager.getAuthToken();
        if (token == null || token.isEmpty()) return;
        String accessToken = "Bearer " + token;
        
        Call<FilterResponse<Object>> call = apiService.getVouchersCount(accessToken, userId);
        call.enqueue(new Callback<FilterResponse<Object>>() {
            @Override
            public void onResponse(Call<FilterResponse<Object>> call, Response<FilterResponse<Object>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    int count = response.body().getTotalItems();
                    tvVouchersCount.setText(String.valueOf(count));
                } else {
                    Log.e(TAG, "Failed to load vouchers count: " + response.code());
                    tvVouchersCount.setText("0");
                }
            }

            @Override
            public void onFailure(Call<FilterResponse<Object>> call, Throwable t) {
                Log.e(TAG, "Error loading vouchers count", t);
                tvVouchersCount.setText("0");
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        NavigationBarUtil.finishActivityWithoutAnimation(this);
    }
}