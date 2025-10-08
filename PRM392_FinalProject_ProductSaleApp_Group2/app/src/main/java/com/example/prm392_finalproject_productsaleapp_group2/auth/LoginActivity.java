package com.example.prm392_finalproject_productsaleapp_group2.auth;

import android.os.Bundle;
import android.content.Intent;
import android.os.AsyncTask;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.prm392_finalproject_productsaleapp_group2.R;
import com.example.prm392_finalproject_productsaleapp_group2.product.HomeActivity;
import com.example.prm392_finalproject_productsaleapp_group2.net.ApiConfig;
import com.google.android.material.textfield.TextInputEditText;

public class LoginActivity extends AppCompatActivity {

    private Button btnLogin;
    private TextView btnRegister;
    private TextInputEditText etIdentifier;
    private TextInputEditText etPassword;
    private CheckBox cbRemember;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        // Không thêm padding insets ở top để banner tràn sát mép trên

        btnLogin = findViewById(R.id.btnLogin);
        btnRegister = findViewById(R.id.btnRegister);
        etIdentifier = findViewById(R.id.etIdentifier);
        etPassword = findViewById(R.id.etPassword);
        cbRemember = findViewById(R.id.cbRemember);

        btnRegister.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });

        btnLogin.setOnClickListener(v -> {
            String id = etIdentifier.getText() != null ? etIdentifier.getText().toString().trim() : "";
            String pw = etPassword.getText() != null ? etPassword.getText().toString() : "";
            if (id.isEmpty() || pw.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập đầy đủ", Toast.LENGTH_SHORT).show();
                return;
            }
            new LoginTask(id, pw, cbRemember.isChecked()).execute();
        });
    }

    private class LoginTask extends AsyncTask<Void, Void, Boolean> {
        private final String identifier;
        private final String password;
        private final boolean remember;
        private String accessToken;
        private int userId;

        LoginTask(String identifier, String password, boolean remember) {
            this.identifier = identifier;
            this.password = password;
            this.remember = remember;
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            java.net.HttpURLConnection conn = null;
            try {
                java.net.URL url = new java.net.URL(ApiConfig.endpoint("/api/Authentication/login"));
                conn = (java.net.HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                conn.setDoOutput(true);

                String body = "{\"identifier\":\"" + identifier + "\",\"password\":\"" + password + "\"}";
                try (java.io.OutputStream os = conn.getOutputStream()) {
                    byte[] input = body.getBytes(java.nio.charset.StandardCharsets.UTF_8);
                    os.write(input, 0, input.length);
                }

                int code = conn.getResponseCode();
                if (code >= 200 && code < 300) {
                    try (java.io.InputStream is = conn.getInputStream();
                         java.io.BufferedReader br = new java.io.BufferedReader(new java.io.InputStreamReader(is))) {
                        StringBuilder sb = new StringBuilder();
                        String line;
                        while ((line = br.readLine()) != null) sb.append(line);
                        String json = sb.toString();
                        
                        // Parse accessToken
                        int idx = json.indexOf("\"accessToken\":");
                        if (idx != -1) {
                            int q1 = json.indexOf('"', idx + 14);
                            int q2 = json.indexOf('"', q1 + 1);
                            if (q1 != -1 && q2 != -1) accessToken = json.substring(q1 + 1, q2);
                        }
                        
                        // Parse userId
                        int userIdIdx = json.indexOf("\"userId\":");
                        if (userIdIdx != -1) {
                            int startIdx = userIdIdx + 9;
                            int endIdx = json.indexOf(',', startIdx);
                            if (endIdx == -1) endIdx = json.indexOf('}', startIdx);
                            if (endIdx != -1) {
                                try {
                                    userId = Integer.parseInt(json.substring(startIdx, endIdx).trim());
                                } catch (NumberFormatException e) {
                                    userId = -1;
                                }
                            }
                        }
                        
                        return accessToken != null && !accessToken.isEmpty();
                    }
                }
            } catch (Exception e) {
                return false;
            } finally {
                if (conn != null) conn.disconnect();
            }
            return false;
        }

        @Override
        protected void onPostExecute(Boolean success) {
            if (success) {
                SessionManager sessionManager = new SessionManager(LoginActivity.this);
                if (remember) {
                    // Save both token and userId for persistent login
                    sessionManager.saveUserData(accessToken, userId);
                } else {
                    // Save both token and userId for current session only
                    sessionManager.saveUserData(accessToken, userId);
                }
                Toast.makeText(LoginActivity.this, "Đăng nhập thành công", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(LoginActivity.this, HomeActivity.class));
                finish();
            } else {
                Toast.makeText(LoginActivity.this, "Đăng nhập thất bại", Toast.LENGTH_SHORT).show();
            }
        }
    }
}