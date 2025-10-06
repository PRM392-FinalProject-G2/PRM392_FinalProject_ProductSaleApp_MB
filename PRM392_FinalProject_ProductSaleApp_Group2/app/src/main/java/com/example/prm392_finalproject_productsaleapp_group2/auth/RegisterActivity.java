package com.example.prm392_finalproject_productsaleapp_group2.auth;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.prm392_finalproject_productsaleapp_group2.R;
import com.example.prm392_finalproject_productsaleapp_group2.net.ApiConfig;
import com.google.android.material.textfield.TextInputEditText;

public class RegisterActivity extends AppCompatActivity {

    private TextInputEditText etEmail;
    private TextInputEditText etUsername;
    private TextInputEditText etPhone;
    private TextInputEditText etPassword;
    private Button btnRegister;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);

        TextView btnGoLogin = findViewById(R.id.btnGoLogin);
        btnGoLogin.setOnClickListener(v -> {
            startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
            finish();
        });

        etEmail = findViewById(R.id.etEmail);
        etUsername = findViewById(R.id.etUsername);
        etPhone = findViewById(R.id.etPhone);
        etPassword = findViewById(R.id.etPassword);
        btnRegister = findViewById(R.id.btnRegister);

        btnRegister.setOnClickListener(v -> {
            String email = etEmail.getText() != null ? etEmail.getText().toString().trim() : "";
            String username = etUsername.getText() != null ? etUsername.getText().toString().trim() : "";
            String phone = etPhone.getText() != null ? etPhone.getText().toString().trim() : "";
            String password = etPassword.getText() != null ? etPassword.getText().toString() : "";
            if (email.isEmpty() || username.isEmpty() || phone.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập đầy đủ", Toast.LENGTH_SHORT).show();
                return;
            }
            new RegisterTask(email, username, phone, password).execute();
        });
    }

    private class RegisterTask extends AsyncTask<Void, Void, Boolean> {
        private final String email;
        private final String username;
        private final String phoneNumber;
        private final String password;
        private int httpCode = -1;
        private String errorBody = null;

        RegisterTask(String email, String username, String phoneNumber, String password) {
            this.email = email;
            this.username = username;
            this.phoneNumber = phoneNumber;
            this.password = password;
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            java.net.HttpURLConnection conn = null;
            try {
                java.net.URL url = new java.net.URL(ApiConfig.endpoint("/api/Authentication/register"));
                conn = (java.net.HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                conn.setRequestProperty("Accept", "application/json");
                conn.setConnectTimeout(10000);
                conn.setReadTimeout(10000);
                conn.setDoOutput(true);

                String body = "{\"email\":\"" + email +
                        "\",\"username\":\"" + username +
                        "\",\"phoneNumber\":\"" + phoneNumber +
                        "\",\"password\":\"" + password + "\"}";
                try (java.io.OutputStream os = conn.getOutputStream()) {
                    byte[] input = body.getBytes(java.nio.charset.StandardCharsets.UTF_8);
                    os.write(input, 0, input.length);
                }

                httpCode = conn.getResponseCode();
                if (httpCode >= 200 && httpCode < 300) {
                    // Drain and ignore success response
                    try (java.io.InputStream is = conn.getInputStream()) { while (is.read() != -1) { /* drain */ } }
                    return true;
                } else {
                    try (java.io.InputStream es = conn.getErrorStream()) {
                        if (es != null) {
                            errorBody = new String(es.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
                        }
                    }
                    return false;
                }
            } catch (Exception e) {
                errorBody = e.getMessage();
                return false;
            } finally {
                if (conn != null) conn.disconnect();
            }
        }

        @Override
        protected void onPostExecute(Boolean success) {
            if (success) {
                Toast.makeText(RegisterActivity.this, "Đăng ký thành công", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                finish();
            } else {
                String msg;
                if (httpCode == 409) {
                    msg = "Đăng ký thất bại: Tài khoản/Email/SĐT đã tồn tại";
                } else if (httpCode > 0) {
                    msg = "Đăng ký thất bại (" + httpCode + ")" + (errorBody != null ? ": " + errorBody : "");
                } else {
                    msg = "Đăng ký thất bại: " + (errorBody != null ? errorBody : "Lỗi kết nối");
                }
                Toast.makeText(RegisterActivity.this, msg, Toast.LENGTH_LONG).show();
            }
        }
    }
}