package com.example.prm392_finalproject_productsaleapp_group2.auth;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.prm392_finalproject_productsaleapp_group2.R;
import com.example.prm392_finalproject_productsaleapp_group2.models.UserResponse;
import com.example.prm392_finalproject_productsaleapp_group2.net.ProfileApiClient;
import com.example.prm392_finalproject_productsaleapp_group2.services.ProfileApiService;
import com.example.prm392_finalproject_productsaleapp_group2.utils.NavigationBarUtil;
import com.bumptech.glide.Glide;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EditProfileActivity extends AppCompatActivity {

    private static final int PERMISSION_REQUEST_CODE = 100;
    private static final String TAG = "EditProfileActivity";

    private ShapeableImageView imgAvatar;
    private ImageView iconCamera;
    private EditText edtName;
    private EditText edtEmail;
    private EditText edtPhone;
    private EditText edtAddress;
    private LinearLayout rowChangePassword;
    private Button btnSave;
    private ProfileApiService apiService;
    private SessionManager sessionManager;
    private int userId;
    private Uri selectedImageUri;
    private ProgressDialog progressDialog;

    // Activity result launcher for image selection
    private final ActivityResultLauncher<Intent> imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    selectedImageUri = result.getData().getData();
                    if (selectedImageUri != null) {
                        // Display selected image
                        Glide.with(this)
                                .load(selectedImageUri)
                                .centerCrop()
                                .into(imgAvatar);
                    }
                }
            }
    );

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
        iconCamera = findViewById(R.id.icon_camera);
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

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Đang tải...");
        progressDialog.setCancelable(false);

        // Load data from API using userId from session
        loadUserData();

        // Click avatar or camera icon to choose image
        imgAvatar.setOnClickListener(v -> selectImage());
        iconCamera.setOnClickListener(v -> selectImage());

        // Navigate to change password screen
        rowChangePassword.setOnClickListener(v -> {
            Intent intent = new Intent(EditProfileActivity.this, ChangePassword_1_Activity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            startActivity(intent);
            overridePendingTransition(0, 0);
        });

        // Save button
        btnSave.setOnClickListener(v -> updateProfile());

        // Back to Profile
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> {
                Intent intent = new Intent(EditProfileActivity.this, ProfileActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                overridePendingTransition(0, 0);
                finish();
            });
        }
    }

    private void selectImage() {
        // Check permission for Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_MEDIA_IMAGES},
                        PERMISSION_REQUEST_CODE);
                return;
            }
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        PERMISSION_REQUEST_CODE);
                return;
            }
        }

        // Open image picker
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        imagePickerLauncher.launch(intent);
    }

    private void updateProfile() {
        String email = edtEmail.getText().toString().trim();
        String phone = edtPhone.getText().toString().trim();
        String address = edtAddress.getText().toString().trim();

        progressDialog.setMessage("Đang cập nhật...");
        progressDialog.show();

        String token = sessionManager.getAuthToken();
        String accessToken = token == null || token.isEmpty() ? null : "Bearer " + token;

        // Prepare request parts
        RequestBody emailBody = RequestBody.create(MediaType.parse("text/plain"), email);
        RequestBody phoneBody = RequestBody.create(MediaType.parse("text/plain"), phone);
        RequestBody addressBody = RequestBody.create(MediaType.parse("text/plain"), address);

        // Prepare avatar file part
        MultipartBody.Part avatarPart = null;
        if (selectedImageUri != null) {
            try {
                File file = getFileFromUri(selectedImageUri);
                if (file != null) {
                    RequestBody requestFile = RequestBody.create(
                            MediaType.parse(getContentResolver().getType(selectedImageUri)),
                            file
                    );
                    avatarPart = MultipartBody.Part.createFormData("AvatarFile", file.getName(), requestFile);
                }
            } catch (IOException e) {
                Log.e(TAG, "Error creating file from URI", e);
            }
        }

        // Call API
        Call<UserResponse> call = apiService.updateUserProfile(accessToken, userId, emailBody, phoneBody, addressBody, avatarPart);
        call.enqueue(new Callback<UserResponse>() {
            @Override
            public void onResponse(Call<UserResponse> call, Response<UserResponse> response) {
                progressDialog.dismiss();

                if (response.isSuccessful() && response.body() != null) {
                    Toast.makeText(EditProfileActivity.this, "Cập nhật thành công!", Toast.LENGTH_SHORT).show();
                    // Reload user data
                    loadUserData();
                    selectedImageUri = null; // Reset selected image
                } else {
                    // Handle error response
                    handleErrorResponse(response);
                }
            }

            @Override
            public void onFailure(Call<UserResponse> call, Throwable t) {
                progressDialog.dismiss();
                Log.e(TAG, "Update failed", t);
                Toast.makeText(EditProfileActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void handleErrorResponse(Response<?> response) {
        try {
            if (response.errorBody() != null) {
                String errorBody = response.errorBody().string();
                Log.e(TAG, "Error body: " + errorBody);

                Gson gson = new Gson();
                JsonObject errorJson = gson.fromJson(errorBody, JsonObject.class);

                // Check for validation errors (400 with errors object)
                if (errorJson.has("errors")) {
                    JsonObject errors = errorJson.getAsJsonObject("errors");
                    StringBuilder errorMessage = new StringBuilder();

                    if (errors.has("Email")) {
                        errorMessage.append(errors.getAsJsonArray("Email").get(0).getAsString()).append("\n");
                    }
                    if (errors.has("PhoneNumber")) {
                        errorMessage.append(errors.getAsJsonArray("PhoneNumber").get(0).getAsString()).append("\n");
                    }

                    Toast.makeText(this, errorMessage.toString().trim(), Toast.LENGTH_LONG).show();
                }
                // Check for simple message error
                else if (errorJson.has("message")) {
                    String message = errorJson.get("message").getAsString();
                    Toast.makeText(this, message, Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(this, "Lỗi: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Lỗi: " + response.code(), Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error parsing error response", e);
            Toast.makeText(this, "Đã xảy ra lỗi", Toast.LENGTH_SHORT).show();
        }
    }

    private File getFileFromUri(Uri uri) throws IOException {
        String fileName = getFileName(uri);
        File tempFile = new File(getCacheDir(), fileName);
        
        try (InputStream inputStream = getContentResolver().openInputStream(uri);
             FileOutputStream outputStream = new FileOutputStream(tempFile)) {
            
            if (inputStream == null) return null;
            
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
            outputStream.flush();
        }
        
        return tempFile;
    }

    private String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            try (Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    if (index != -1) {
                        result = cursor.getString(index);
                    }
                }
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
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