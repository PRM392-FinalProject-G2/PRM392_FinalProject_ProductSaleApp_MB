package com.example.prm392_finalproject_productsaleapp_group2.auth;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.prm392_finalproject_productsaleapp_group2.R;
import com.example.prm392_finalproject_productsaleapp_group2.models.UserResponse;
import com.example.prm392_finalproject_productsaleapp_group2.net.ProfileApiClient;
import com.example.prm392_finalproject_productsaleapp_group2.services.ProfileApiService;
import com.example.prm392_finalproject_productsaleapp_group2.utils.NavigationBarUtil;
import com.bumptech.glide.Glide;

public class EditProfileActivity extends AppCompatActivity {

    private ImageView imgAvatar;
    private EditText edtName;
    private EditText edtEmail;
    private EditText edtPhone;
    private EditText edtAddress;
    private LinearLayout rowChangePassword;
    private Button btnSave;
    private ProfileApiService apiService;
    private SessionManager sessionManager;
    private int userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_edit_profile);
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
        NavigationBarUtil.setActiveNavigationButton(this, "profile");

        // Init
        imgAvatar = findViewById(R.id.img_avatar);
        edtName = findViewById(R.id.edt_name);
        edtEmail = findViewById(R.id.edt_email);
        edtPhone = findViewById(R.id.edt_phone);
        edtAddress = findViewById(R.id.edt_address);
        rowChangePassword = findViewById(R.id.row_change_password);
        btnSave = findViewById(R.id.btn_save);
        ImageView btnBack = findViewById(R.id.btn_back);

        apiService = ProfileApiClient.getInstance().getApiService();
        sessionManager = new SessionManager(this);
        userId = sessionManager.getUserId();

        // Load data from API using userId from session
        loadUserData();

        // Navigate to change password screen
        rowChangePassword.setOnClickListener(v -> {
            android.content.Intent intent = new android.content.Intent(EditProfileActivity.this, ChangePassword_1_Activity.class);
            intent.addFlags(android.content.Intent.FLAG_ACTIVITY_NO_ANIMATION);
            startActivity(intent);
            overridePendingTransition(0, 0);
        });

        // Back to Profile
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> {
                android.content.Intent intent = new android.content.Intent(EditProfileActivity.this, ProfileActivity.class);
                intent.addFlags(android.content.Intent.FLAG_ACTIVITY_NO_ANIMATION | android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                overridePendingTransition(0, 0);
                finish();
            });
        }
    }

    @Override
    public void onBackPressed() {
        NavigationBarUtil.finishActivityWithoutAnimation(this);
    }

    private void loadUserData() {
        if (userId == -1) {
            Toast.makeText(this, "Vui lòng đăng nhập", Toast.LENGTH_SHORT).show();
            return;
        }

        String token = sessionManager.getAuthToken();
        String accessToken = token == null || token.isEmpty() ? null : "Bearer " + token;

        apiService.getUserInfo(accessToken, userId).enqueue(new retrofit2.Callback<UserResponse>() {
            @Override
            public void onResponse(retrofit2.Call<UserResponse> call, retrofit2.Response<UserResponse> response) {
                if (!response.isSuccessful() || response.body() == null) {
                    Toast.makeText(EditProfileActivity.this, "Không thể tải thông tin", Toast.LENGTH_SHORT).show();
                    return;
                }
                UserResponse user = response.body();
                edtName.setText(user.getUsername());
                edtEmail.setText(user.getEmail());
                edtPhone.setText(user.getPhone());
                edtAddress.setText(user.getAddress());

                String avatarUrl = user.getAvatarUrl();
                if (avatarUrl != null && !avatarUrl.isEmpty()) {
                    Glide.with(EditProfileActivity.this)
                            .load(avatarUrl)
                            .placeholder(R.drawable.profile)
                            .error(R.drawable.profile)
                            .centerCrop()
                            .into(imgAvatar);
                } else {
                    imgAvatar.setImageResource(R.drawable.profile);
                }
            }

            @Override
            public void onFailure(retrofit2.Call<UserResponse> call, Throwable t) {
                Toast.makeText(EditProfileActivity.this, "Lỗi kết nối", Toast.LENGTH_SHORT).show();
            }
        });
    }
}