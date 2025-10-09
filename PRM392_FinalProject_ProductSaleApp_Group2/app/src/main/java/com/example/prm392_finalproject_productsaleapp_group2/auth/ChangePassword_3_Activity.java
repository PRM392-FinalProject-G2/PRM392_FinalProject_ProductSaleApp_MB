package com.example.prm392_finalproject_productsaleapp_group2.auth;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.prm392_finalproject_productsaleapp_group2.R;
import com.example.prm392_finalproject_productsaleapp_group2.models.ApiResponse;
import com.example.prm392_finalproject_productsaleapp_group2.models.ChangePasswordRequest;
import com.example.prm392_finalproject_productsaleapp_group2.net.AuthApiClient;
import com.example.prm392_finalproject_productsaleapp_group2.services.AuthApiService;
import com.example.prm392_finalproject_productsaleapp_group2.utils.NavigationBarUtil;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChangePassword_3_Activity extends AppCompatActivity {

    private static final String TAG = "ChangePassword_3";

    private EditText edtNewPassword, edtConfirmPassword;
    private AppCompatButton btnContinue;
    private ImageView btnBack;
    private String email;
    private String resetToken;
    private AuthApiService authApiService;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_change_password3);
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

        // Initialize API service
        authApiService = AuthApiClient.getInstance().getApiService();

        // Initialize progress dialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Đang đổi mật khẩu...");
        progressDialog.setCancelable(false);

        // Get email and resetToken from Intent
        email = getIntent().getStringExtra("email");
        resetToken = getIntent().getStringExtra("resetToken");

        // Initialize views
        edtNewPassword = findViewById(R.id.edt_new_password);
        edtConfirmPassword = findViewById(R.id.edt_confirm_password);
        btnContinue = findViewById(R.id.btn_continue);
        btnBack = findViewById(R.id.btn_back);

        // Back button
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> {
                finish();
                overridePendingTransition(0, 0);
            });
        }

        // Continue button
        btnContinue.setOnClickListener(v -> {
            String newPassword = edtNewPassword.getText().toString().trim();
            String confirmPassword = edtConfirmPassword.getText().toString().trim();

            // Validation
            if (newPassword.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập mật khẩu mới", Toast.LENGTH_SHORT).show();
                return;
            }

            if (confirmPassword.isEmpty()) {
                Toast.makeText(this, "Vui lòng xác nhận mật khẩu", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!newPassword.equals(confirmPassword)) {
                Toast.makeText(this, "Mật khẩu xác nhận không khớp", Toast.LENGTH_SHORT).show();
                return;
            }

            if (newPassword.length() < 6) {
                Toast.makeText(this, "Mật khẩu phải có ít nhất 6 ký tự", Toast.LENGTH_SHORT).show();
                return;
            }

            // Call API to change password
            changePassword(email, resetToken, newPassword, confirmPassword);
        });
    }

    private void changePassword(String email, String resetToken, String newPassword, String confirmPassword) {
        progressDialog.show();

        ChangePasswordRequest request = new ChangePasswordRequest(email, resetToken, newPassword, confirmPassword);

        authApiService.changePassword(request).enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                progressDialog.dismiss();

                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse apiResponse = response.body();

                    if (apiResponse.isSuccess()) {
                        // Success - navigate to success page
                        Toast.makeText(ChangePassword_3_Activity.this,
                                apiResponse.getMessage(),
                                Toast.LENGTH_SHORT).show();

                        Intent intent = new Intent(ChangePassword_3_Activity.this, ChangePassword_4_Activity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                        overridePendingTransition(0, 0);
                        finish();
                    } else {
                        // API returned success=false
                        Toast.makeText(ChangePassword_3_Activity.this,
                                apiResponse.getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                } else {
                    // Handle error response
                    try {
                        if (response.errorBody() != null) {
                            String errorBody = response.errorBody().string();
                            Log.e(TAG, "Error response: " + errorBody);

                            // Try to parse error as ApiResponse
                            com.google.gson.Gson gson = new com.google.gson.Gson();
                            ApiResponse errorResponse = gson.fromJson(errorBody, ApiResponse.class);

                            if (errorResponse != null && errorResponse.getMessage() != null) {
                                Toast.makeText(ChangePassword_3_Activity.this,
                                        errorResponse.getMessage(),
                                        Toast.LENGTH_LONG).show();
                            } else {
                                Toast.makeText(ChangePassword_3_Activity.this,
                                        "Lỗi: " + response.code(),
                                        Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(ChangePassword_3_Activity.this,
                                    "Lỗi: " + response.code(),
                                    Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing error response", e);
                        Toast.makeText(ChangePassword_3_Activity.this,
                                "Không thể đổi mật khẩu. Vui lòng thử lại",
                                Toast.LENGTH_LONG).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                progressDialog.dismiss();
                Log.e(TAG, "API call failed", t);
                Toast.makeText(ChangePassword_3_Activity.this,
                        "Lỗi kết nối: " + t.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void onBackPressed() {
        NavigationBarUtil.finishActivityWithoutAnimation(this);
    }
}