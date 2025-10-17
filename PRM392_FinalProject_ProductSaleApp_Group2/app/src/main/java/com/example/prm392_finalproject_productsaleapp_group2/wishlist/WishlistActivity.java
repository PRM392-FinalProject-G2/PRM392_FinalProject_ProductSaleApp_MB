package com.example.prm392_finalproject_productsaleapp_group2.wishlist;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.prm392_finalproject_productsaleapp_group2.product.HomeActivity;
import com.example.prm392_finalproject_productsaleapp_group2.R;
import com.example.prm392_finalproject_productsaleapp_group2.auth.SessionManager;
import com.example.prm392_finalproject_productsaleapp_group2.utils.NavigationBarUtil;
import com.example.prm392_finalproject_productsaleapp_group2.models.ApiResponse;
import com.example.prm392_finalproject_productsaleapp_group2.models.FilterResponse;
import com.example.prm392_finalproject_productsaleapp_group2.models.Product;
import com.example.prm392_finalproject_productsaleapp_group2.models.Wishlist;
import com.example.prm392_finalproject_productsaleapp_group2.models.WishlistMobile;
import com.example.prm392_finalproject_productsaleapp_group2.net.WishlistApiClient;
import com.example.prm392_finalproject_productsaleapp_group2.product.ProductDetailActivity;
import com.example.prm392_finalproject_productsaleapp_group2.services.WishlistApiService;

import java.util.ArrayList;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class WishlistActivity extends AppCompatActivity implements WishlistMobileAdapter.OnWishlistItemListener {

    private RecyclerView rvWishlist;
    private ProgressBar progressBar;
    private LinearLayout layoutEmpty;
    private AppCompatButton btnGoShopping, btnClearAll;
    private EditText edtSearch;
    private List<WishlistMobile> allWishlistItems; // For search functionality
    private ImageView ivBack;
    
    private WishlistMobileAdapter adapter;
    private List<WishlistMobile> wishlistItems;
    private SessionManager sessionManager;
    private WishlistApiService apiService;
    private int userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        
        // Set status bar transparent to let gradient show through
        getWindow().setStatusBarColor(android.graphics.Color.TRANSPARENT);
        
        setContentView(R.layout.activity_wishlist);
        
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            // Apply only side paddings to the root, let header extend under status bar
            v.setPadding(systemBars.left, 0, systemBars.right, 0);

            // Add top inset to the header so its gradient extends under the status bar
            View header = findViewById(R.id.header_layout);
            if (header != null) {
                header.setPadding(
                        header.getPaddingLeft(),
                        header.getPaddingTop() + systemBars.top,
                        header.getPaddingRight(),
                        header.getPaddingBottom()
                );
            }
            return WindowInsetsCompat.CONSUMED;
        });

        initViews();
        initData();
        setupNavigationBar();
        setupClickListeners();
        loadWishlistData();
    }

    private void initViews() {
        rvWishlist = findViewById(R.id.rv_wishlist);
        progressBar = findViewById(R.id.progress_bar);
        layoutEmpty = findViewById(R.id.layout_empty);
        btnGoShopping = findViewById(R.id.btn_go_shopping);
        btnClearAll = findViewById(R.id.btn_clear_all);
        ivBack = findViewById(R.id.iv_back);
        edtSearch = findViewById(R.id.edt_search);
        
        // Setup RecyclerView
        rvWishlist.setLayoutManager(new LinearLayoutManager(this));
        wishlistItems = new ArrayList<>();
        adapter = new WishlistMobileAdapter(this, wishlistItems, this);
        rvWishlist.setAdapter(adapter);
    }

    private void initData() {
        sessionManager = new SessionManager(this);
        apiService = WishlistApiClient.getInstance().getApiService();
        userId = sessionManager.getUserId();
        
        // Check if user is logged in
        if (!sessionManager.isLoggedIn() || userId == -1) {
            Toast.makeText(this, "Vui lòng đăng nhập", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
    }

    private void setupNavigationBar() {
        // Setup navigation bar
        NavigationBarUtil.setupNavigationBar(this);
        // Set wishlist as active if there's a wishlist button, otherwise don't highlight any
        // NavigationBarUtil.setActiveNavigationButton(this, "wishlist");
    }

    private void setupClickListeners() {
        ivBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        
        btnGoShopping.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(WishlistActivity.this, HomeActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            }
        });
        
        btnClearAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showClearAllConfirmDialog();
            }
        });
        
        // Setup search functionality
        edtSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterWishlist(s.toString().trim());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void loadWishlistData() {
        showLoading(true);
        
        String token = sessionManager.getAuthToken();
        if (token == null || token.isEmpty()) {
            Toast.makeText(this, "Vui lòng đăng nhập", Toast.LENGTH_SHORT).show();
            showLoading(false);
            return;
        }

        String authToken = "Bearer " + token;
        
        Call<FilterResponse<WishlistMobile>> call = apiService.getWishlistByUser(authToken, userId, 1, 100);
        call.enqueue(new Callback<FilterResponse<WishlistMobile>>() {
            @Override
            public void onResponse(@NonNull Call<FilterResponse<WishlistMobile>> call, @NonNull Response<FilterResponse<WishlistMobile>> response) {
                showLoading(false);
                
                if (response.isSuccessful() && response.body() != null) {
                    FilterResponse<WishlistMobile> filterResponse = response.body();
                    List<WishlistMobile> items = filterResponse.getItems();
                    
                    if (items != null && !items.isEmpty()) {
                        // Store all items for search functionality
                        allWishlistItems = new ArrayList<>(items);
                        
                        wishlistItems.clear();
                        wishlistItems.addAll(items);
                        adapter.updateData(wishlistItems);
                        showEmptyState(false);
                    } else {
                        allWishlistItems = new ArrayList<>();
                        showEmptyState(true);
                    }
                } else {
                    Toast.makeText(WishlistActivity.this, "Không thể tải danh sách yêu thích", Toast.LENGTH_SHORT).show();
                    showEmptyState(true);
                }
            }

            @Override
            public void onFailure(@NonNull Call<FilterResponse<WishlistMobile>> call, @NonNull Throwable t) {
                showLoading(false);
                Toast.makeText(WishlistActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                showEmptyState(true);
            }
        });
    }

    private void showLoading(boolean isLoading) {
        progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        rvWishlist.setVisibility(isLoading ? View.GONE : View.VISIBLE);
    }

    private void showEmptyState(boolean isEmpty) {
        if (isEmpty) {
            layoutEmpty.setVisibility(View.VISIBLE);
            rvWishlist.setVisibility(View.GONE);
            btnClearAll.setVisibility(View.GONE);
        } else {
            layoutEmpty.setVisibility(View.GONE);
            rvWishlist.setVisibility(View.VISIBLE);
            btnClearAll.setVisibility(wishlistItems.size() > 0 ? View.VISIBLE : View.GONE);
        }
    }



    @Override
    public void onItemRemoved(int position) {
        // Update UI state after item removal
        if (wishlistItems.isEmpty()) {
            showEmptyState(true);
        } else {
            btnClearAll.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onItemClicked(int productId) {
        // Navigate to product detail
        Intent intent = new Intent(this, ProductDetailActivity.class);
        intent.putExtra("productId", productId);
        startActivity(intent);
    }

    private void showClearAllConfirmDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Xóa tất cả")
                .setMessage("Bạn có chắc chắn muốn xóa tất cả sản phẩm khỏi danh sách yêu thích?")
                .setPositiveButton("Xóa", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        clearAllWishlist();
                    }
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void clearAllWishlist() {
        showLoading(true);
        
        String token = sessionManager.getAuthToken();
        if (token == null || token.isEmpty()) {
            showLoading(false);
            return;
        }

        String authToken = "Bearer " + token;
        List<WishlistMobile> itemsToRemove = new ArrayList<>(allWishlistItems != null ? allWishlistItems : wishlistItems);
        
        // Remove items one by one (since there might not be a clear all API)
        removeWishlistItems(authToken, itemsToRemove, 0);
    }

    private void removeWishlistItems(String authToken, List<WishlistMobile> items, int index) {
        if (index >= items.size()) {
            // All items removed
            showLoading(false);
            wishlistItems.clear();
            if (allWishlistItems != null) {
                allWishlistItems.clear();
            }
            adapter.updateData(wishlistItems);
            showEmptyState(true);
            Toast.makeText(this, "Đã xóa tất cả sản phẩm", Toast.LENGTH_SHORT).show();
            return;
        }

        WishlistMobile item = items.get(index);
        Call<ApiResponse> call = apiService.removeFromWishlist(authToken, item.getWishlistId());
        call.enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse> call, @NonNull Response<ApiResponse> response) {
                // Continue with next item regardless of individual success/failure
                removeWishlistItems(authToken, items, index + 1);
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse> call, @NonNull Throwable t) {
                // Continue with next item even if this one failed
                removeWishlistItems(authToken, items, index + 1);
            }
        });
    }

    private void filterWishlist(String searchText) {
        if (allWishlistItems == null) {
            return;
        }

        List<WishlistMobile> filteredList = new ArrayList<>();
        
        if (searchText.isEmpty()) {
            filteredList.addAll(allWishlistItems);
        } else {
            for (WishlistMobile item : allWishlistItems) {
                // Search by product name (case insensitive)
                if (item.getProductName() != null && 
                    item.getProductName().toLowerCase().contains(searchText.toLowerCase())) {
                    filteredList.add(item);
                }
            }
        }

        wishlistItems.clear();
        wishlistItems.addAll(filteredList);
        adapter.updateData(wishlistItems);
        
        // Update UI state
        showEmptyState(filteredList.isEmpty());
    }

    @Override
    public void finish() {
        NavigationBarUtil.finishActivityWithoutAnimation(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh wishlist when returning to this activity
        loadWishlistData();
    }
}