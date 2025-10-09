package com.example.prm392_finalproject_productsaleapp_group2.auth;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
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
import com.example.prm392_finalproject_productsaleapp_group2.models.VerifyOtpRequest;
import com.example.prm392_finalproject_productsaleapp_group2.models.VerifyOtpResponse;
import com.example.prm392_finalproject_productsaleapp_group2.net.AuthApiClient;
import com.example.prm392_finalproject_productsaleapp_group2.services.AuthApiService;
import com.example.prm392_finalproject_productsaleapp_group2.utils.NavigationBarUtil;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChangePassword_2_Activity extends AppCompatActivity {

    private static final String TAG = "ChangePassword_2";

    private EditText edtOtp1, edtOtp2, edtOtp3, edtOtp4;
    private AppCompatButton btnVerify;
    private ImageView btnBack;
    private String email;
    private int userId;
    private AuthApiService authApiService;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_change_password2);
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
        progressDialog.setMessage("Đang xác minh OTP...");
        progressDialog.setCancelable(false);

        // Get email and userId from Intent
        email = getIntent().getStringExtra("email");
        userId = getIntent().getIntExtra("userId", -1);

        // Initialize views
        edtOtp1 = findViewById(R.id.edt_otp1);
        edtOtp2 = findViewById(R.id.edt_otp2);
        edtOtp3 = findViewById(R.id.edt_otp3);
        edtOtp4 = findViewById(R.id.edt_otp4);
        btnVerify = findViewById(R.id.btn_verify);
        btnBack = findViewById(R.id.btn_back);

        // Setup OTP input behavior
        setupOtpInputs();

        // Back button
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> {
                finish();
                overridePendingTransition(0, 0);
            });
        }

        // Verify button
        btnVerify.setOnClickListener(v -> {
            String otp = edtOtp1.getText().toString() +
                         edtOtp2.getText().toString() +
                         edtOtp3.getText().toString() +
                         edtOtp4.getText().toString();

            if (otp.length() != 4) {
                Toast.makeText(this, "Vui lòng nhập đầy đủ mã OTP", Toast.LENGTH_SHORT).show();
                return;
            }

            // Call API to verify OTP
            verifyOtp(email, otp);
        });

        // Auto-focus first OTP field
        edtOtp1.requestFocus();
    }

    private void verifyOtp(String email, String otp) {
        progressDialog.show();

        VerifyOtpRequest request = new VerifyOtpRequest(email, otp);

        authApiService.verifyOtp(request).enqueue(new Callback<VerifyOtpResponse>() {
            @Override
            public void onResponse(Call<VerifyOtpResponse> call, Response<VerifyOtpResponse> response) {
                progressDialog.dismiss();

                if (response.isSuccessful() && response.body() != null) {
                    VerifyOtpResponse otpResponse = response.body();

                    if (otpResponse.isSuccess()) {
                        // Success - navigate to change password page
                        Toast.makeText(ChangePassword_2_Activity.this,
                                otpResponse.getMessage(),
                                Toast.LENGTH_SHORT).show();

                        Intent intent = new Intent(ChangePassword_2_Activity.this, ChangePassword_3_Activity.class);
                        intent.putExtra("resetToken", otpResponse.getResetToken());
                        intent.putExtra("email", email);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                        startActivity(intent);
                        overridePendingTransition(0, 0);
                        finish();
                    } else {
                        // API returned success=false
                        Toast.makeText(ChangePassword_2_Activity.this,
                                otpResponse.getMessage(),
                                Toast.LENGTH_LONG).show();
                        // Clear OTP fields
                        clearOtpFields();
                    }
                } else {
                    // Handle error response
                    try {
                        if (response.errorBody() != null) {
                            String errorBody = response.errorBody().string();
                            Log.e(TAG, "Error response: " + errorBody);

                            // Try to parse error as VerifyOtpResponse
                            com.google.gson.Gson gson = new com.google.gson.Gson();
                            VerifyOtpResponse errorResponse = gson.fromJson(errorBody, VerifyOtpResponse.class);

                            if (errorResponse != null && errorResponse.getMessage() != null) {
                                Toast.makeText(ChangePassword_2_Activity.this,
                                        errorResponse.getMessage(),
                                        Toast.LENGTH_LONG).show();
                            } else {
                                Toast.makeText(ChangePassword_2_Activity.this,
                                        "Lỗi: " + response.code(),
                                        Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(ChangePassword_2_Activity.this,
                                    "Lỗi: " + response.code(),
                                    Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing error response", e);
                        Toast.makeText(ChangePassword_2_Activity.this,
                                "Mã OTP không hợp lệ hoặc đã hết hạn",
                                Toast.LENGTH_LONG).show();
                    }
                    // Clear OTP fields
                    clearOtpFields();
                }
            }

            @Override
            public void onFailure(Call<VerifyOtpResponse> call, Throwable t) {
                progressDialog.dismiss();
                Log.e(TAG, "API call failed", t);
                Toast.makeText(ChangePassword_2_Activity.this,
                        "Lỗi kết nối: " + t.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    private void clearOtpFields() {
        edtOtp1.setText("");
        edtOtp2.setText("");
        edtOtp3.setText("");
        edtOtp4.setText("");
        edtOtp1.requestFocus();
    }

    private void setupOtpInputs() {
        // Setup auto-focus to next field when typing
        edtOtp1.addTextChangedListener(new OtpTextWatcher(edtOtp1, edtOtp2));
        edtOtp2.addTextChangedListener(new OtpTextWatcher(edtOtp2, edtOtp3));
        edtOtp3.addTextChangedListener(new OtpTextWatcher(edtOtp3, edtOtp4));
        edtOtp4.addTextChangedListener(new OtpTextWatcher(edtOtp4, null));

        // Setup backspace to go to previous field
        edtOtp2.setOnKeyListener((v, keyCode, event) -> {
            if (keyCode == KeyEvent.KEYCODE_DEL && event.getAction() == KeyEvent.ACTION_DOWN) {
                if (edtOtp2.getText().toString().isEmpty()) {
                    edtOtp1.requestFocus();
                    return true;
                }
            }
            return false;
        });

        edtOtp3.setOnKeyListener((v, keyCode, event) -> {
            if (keyCode == KeyEvent.KEYCODE_DEL && event.getAction() == KeyEvent.ACTION_DOWN) {
                if (edtOtp3.getText().toString().isEmpty()) {
                    edtOtp2.requestFocus();
                    return true;
                }
            }
            return false;
        });

        edtOtp4.setOnKeyListener((v, keyCode, event) -> {
            if (keyCode == KeyEvent.KEYCODE_DEL && event.getAction() == KeyEvent.ACTION_DOWN) {
                if (edtOtp4.getText().toString().isEmpty()) {
                    edtOtp3.requestFocus();
                    return true;
                }
            }
            return false;
        });
    }

    @Override
    public void onBackPressed() {
        NavigationBarUtil.finishActivityWithoutAnimation(this);
    }

    // TextWatcher for OTP input
    private class OtpTextWatcher implements TextWatcher {
        private final EditText currentView;
        private final EditText nextView;

        public OtpTextWatcher(EditText currentView, EditText nextView) {
            this.currentView = currentView;
            this.nextView = nextView;
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        @Override
        public void afterTextChanged(Editable s) {
            if (s.length() == 1 && nextView != null) {
                nextView.requestFocus();
            }
        }
    }
}