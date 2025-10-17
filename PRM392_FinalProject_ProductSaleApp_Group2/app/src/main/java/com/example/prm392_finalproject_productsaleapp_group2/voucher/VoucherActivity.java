package com.example.prm392_finalproject_productsaleapp_group2.voucher;

import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.prm392_finalproject_productsaleapp_group2.R;
import com.example.prm392_finalproject_productsaleapp_group2.auth.SessionManager;
import com.example.prm392_finalproject_productsaleapp_group2.models.UserVoucher;
import com.example.prm392_finalproject_productsaleapp_group2.net.ApiConfig;
import com.example.prm392_finalproject_productsaleapp_group2.utils.NavigationBarUtil;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class VoucherActivity extends AppCompatActivity implements VoucherAdapter.OnVoucherClickListener {

    private static final String TAG = "VoucherActivity";

    private RecyclerView rvVouchers;
    private LinearLayout layoutEmpty;
    private EditText edtSearch;
    private ImageView btnBack;
    
    private VoucherAdapter adapter;
    private List<UserVoucher> voucherList = new ArrayList<>();
    private List<UserVoucher> voucherListFull = new ArrayList<>(); // Full list for search
    private SessionManager sessionManager;
    private int userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        androidx.activity.EdgeToEdge.enable(this);
        
        // Set status bar transparent to let gradient show through
        getWindow().setStatusBarColor(android.graphics.Color.TRANSPARENT);
        
        setContentView(R.layout.activity_voucher);
        
        // Handle window insets for edge-to-edge
        androidx.core.view.ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            androidx.core.graphics.Insets systemBars = insets.getInsets(androidx.core.view.WindowInsetsCompat.Type.systemBars());
            
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

            return androidx.core.view.WindowInsetsCompat.CONSUMED;
        });

        // Initialize SessionManager
        sessionManager = new SessionManager(this);
        userId = sessionManager.getUserId();

        // Initialize views
        rvVouchers = findViewById(R.id.rv_vouchers);
        layoutEmpty = findViewById(R.id.layout_empty);
        edtSearch = findViewById(R.id.edt_search);
        btnBack = findViewById(R.id.btn_back);

        // Setup navigation bar
        NavigationBarUtil.setupNavigationBar(this);

        // Setup RecyclerView
        adapter = new VoucherAdapter(voucherList, this);
        rvVouchers.setLayoutManager(new LinearLayoutManager(this));
        rvVouchers.setAdapter(adapter);

        // Back button
        btnBack.setOnClickListener(v -> finish());

        // Setup search functionality
        setupSearch();

        // Load vouchers
        if (userId != -1) {
            loadVouchers(userId);
        } else {
            Toast.makeText(this, "Vui lòng đăng nhập", Toast.LENGTH_SHORT).show();
            showEmptyState();
        }
    }

    private void setupSearch() {
        edtSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterVouchers(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private void filterVouchers(String query) {
        List<UserVoucher> filteredList = new ArrayList<>();
        
        if (query == null || query.trim().isEmpty()) {
            // Show all vouchers if search is empty
            filteredList.addAll(voucherListFull);
        } else {
            String lowerCaseQuery = query.toLowerCase().trim();
            for (UserVoucher userVoucher : voucherListFull) {
                if (userVoucher.getVoucher() != null) {
                    String code = userVoucher.getVoucher().getCode().toLowerCase();
                    String description = userVoucher.getVoucher().getDescription().toLowerCase();
                    
                    // Search by code or description
                    if (code.contains(lowerCaseQuery) || description.contains(lowerCaseQuery)) {
                        filteredList.add(userVoucher);
                    }
                }
            }
        }
        
        // Update adapter with filtered list
        voucherList.clear();
        voucherList.addAll(filteredList);
        adapter.updateData(voucherList);
        
        // Show empty state if no results
        if (filteredList.isEmpty()) {
            showEmptyState();
        } else {
            showVoucherList();
        }
    }

    private void loadVouchers(int userId) {
        new LoadVouchersTask().execute(userId);
    }

    private void showEmptyState() {
        layoutEmpty.setVisibility(View.VISIBLE);
        rvVouchers.setVisibility(View.GONE);
    }

    private void showVoucherList() {
        layoutEmpty.setVisibility(View.GONE);
        rvVouchers.setVisibility(View.VISIBLE);
    }

    @Override
    public void onApplyClick(UserVoucher voucher) {
        // Navigate to CartActivity
        android.content.Intent intent = new android.content.Intent(VoucherActivity.this, com.example.prm392_finalproject_productsaleapp_group2.cart.CartActivity.class);
        intent.addFlags(android.content.Intent.FLAG_ACTIVITY_NO_ANIMATION);
        startActivity(intent);
        overridePendingTransition(0, 0);
        finish();
    }

    @Override
    public void onBackPressed() {
        NavigationBarUtil.finishActivityWithoutAnimation(this);
    }

    // AsyncTask to load vouchers
    private class LoadVouchersTask extends AsyncTask<Integer, Void, List<UserVoucher>> {

        @Override
        protected List<UserVoucher> doInBackground(Integer... params) {
            int userId = params[0];
            List<UserVoucher> vouchers = new ArrayList<>();
            
            try {
                String urlString = ApiConfig.endpoint("/api/UserVouchers/user/" + userId + "/active-valid");
                Log.d(TAG, "Loading vouchers from: " + urlString);
                
                URL url = new URL(urlString);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("Accept", "application/json");

                int code = conn.getResponseCode();
                Log.d(TAG, "Response code: " + code);
                
                if (code >= 200 && code < 300) {
                    BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = br.readLine()) != null) {
                        sb.append(line);
                    }
                    br.close();

                    String responseStr = sb.toString();
                    Log.d(TAG, "Response: " + responseStr);

                    // API returns a bare array of UserVoucher
                    JSONArray items = new JSONArray(responseStr);
                    Log.d(TAG, "Total items: " + items.length());
                    if (items.length() > 0) {
                        Gson gson = new Gson();
                        for (int i = 0; i < items.length(); i++) {
                            JSONObject item = items.getJSONObject(i);
                            UserVoucher voucher = gson.fromJson(item.toString(), UserVoucher.class);
                            vouchers.add(voucher);
                        }
                    }
                }
                
                conn.disconnect();
            } catch (Exception e) {
                Log.e(TAG, "Error loading vouchers", e);
            }
            
            return vouchers;
        }

        @Override
        protected void onPostExecute(List<UserVoucher> vouchers) {
            if (vouchers != null && !vouchers.isEmpty()) {
                voucherList.clear();
                voucherList.addAll(vouchers);
                // Save to full list for search
                voucherListFull.clear();
                voucherListFull.addAll(vouchers);
                adapter.updateData(voucherList);
                showVoucherList();
                Log.d(TAG, "Loaded " + vouchers.size() + " vouchers");
            } else {
                voucherListFull.clear();
                showEmptyState();
                Log.d(TAG, "No vouchers found");
            }
        }
    }
}
