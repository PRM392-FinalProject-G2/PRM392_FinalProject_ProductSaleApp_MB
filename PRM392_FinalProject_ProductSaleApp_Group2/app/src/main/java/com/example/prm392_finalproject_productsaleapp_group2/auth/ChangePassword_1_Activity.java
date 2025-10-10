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
import com.example.prm392_finalproject_productsaleapp_group2.models.OtpRequest;
import com.example.prm392_finalproject_productsaleapp_group2.net.AuthApiClient;
import com.example.prm392_finalproject_productsaleapp_group2.services.AuthApiService;
import com.example.prm392_finalproject_productsaleapp_group2.utils.NavigationBarUtil;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChangePassword_1_Activity extends AppCompatActivity {

    private static final String TAG = "ChangePassword_1";
    
    private EditText edtEmail;
    private AppCompatButton btnContinue;
    private ImageView btnBack;
    private AuthApiService authApiService;
    private SessionManager sessionManager;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        
        // Set status bar transparent to let gradient show through
        getWindow().setStatusBarColor(android.graphics.Color.TRANSPARENT);
        
        setContentView(R.layout.activity_change_password1);
        
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            
            // Apply only sides to root, let header extend under status bar
            v.setPadding(systemBars.left, 0, systemBars.right, 0);

            // Add top inset to header so its background extends under status bar
            android.view.View header = findViewById(R.id.header_layout);
            if (header != null) {
                header.setPadding(
                        header.getPaddingLeft(),
                        systemBars.top,
                        header.getPaddingRight(),
                        header.getPaddingBottom()
                );
            }

            return WindowInsetsCompat.CONSUMED;
        });

        // Setup navigation bar
        NavigationBarUtil.setupNavigationBar(this);

        // Initialize API service and session manager
        authApiService = AuthApiClient.getInstance().getApiService();
        sessionManager = new SessionManager(this);

        // Initialize progress dialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Đang gửi OTP...");
        progressDialog.setCancelable(false);

        // Initialize views
        edtEmail = findViewById(R.id.edt_email);
        btnContinue = findViewById(R.id.btn_continue);
        btnBack = findViewById(R.id.btn_back);

        // Back button
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> {
                Intent intent = new Intent(ChangePassword_1_Activity.this, EditProfileActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                overridePendingTransition(0, 0);
                finish();
            });
        }

        // Continue button
        btnContinue.setOnClickListener(v -> {
            String email = edtEmail.getText().toString().trim();
            
            if (email.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập email", Toast.LENGTH_SHORT).show();
                return;
            }

            // Get userId from session
            int userId = sessionManager.getUserId();
            if (userId == -1) {
                Toast.makeText(this, "Vui lòng đăng nhập", Toast.LENGTH_SHORT).show();
                return;
            }

            // Call API request OTP
            requestOtp(email, userId);
        });
    }

    private void requestOtp(String email, int userId) {
        progressDialog.show();

        OtpRequest otpRequest = new OtpRequest(email, userId);

        authApiService.requestOtp(otpRequest).enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                progressDialog.dismiss();

                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse apiResponse = response.body();
                    
                    if (apiResponse.isSuccess()) {
                        // Success - show message and navigate to step 2
                        Toast.makeText(ChangePassword_1_Activity.this, 
                                apiResponse.getMessage(), 
                                Toast.LENGTH_LONG).show();
                        
                        // Navigate to ChangePassword_2_Activity
                        Intent intent = new Intent(ChangePassword_1_Activity.this, ChangePassword_2_Activity.class);
                        intent.putExtra("email", email);
                        intent.putExtra("userId", userId);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                        startActivity(intent);
                        overridePendingTransition(0, 0);
                        finish();
                    } else {
                        // API returned success=false
                        Toast.makeText(ChangePassword_1_Activity.this, 
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
                                Toast.makeText(ChangePassword_1_Activity.this, 
                                        errorResponse.getMessage(), 
                                        Toast.LENGTH_LONG).show();
                            } else {
                                Toast.makeText(ChangePassword_1_Activity.this, 
                                        "Lỗi: " + response.code(), 
                                        Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(ChangePassword_1_Activity.this, 
                                    "Lỗi: " + response.code(), 
                                    Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing error response", e);
                        Toast.makeText(ChangePassword_1_Activity.this, 
                                "Email không khớp hoặc không thể gửi OTP", 
                                Toast.LENGTH_LONG).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                progressDialog.dismiss();
                Log.e(TAG, "API call failed", t);
                Toast.makeText(ChangePassword_1_Activity.this, 
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