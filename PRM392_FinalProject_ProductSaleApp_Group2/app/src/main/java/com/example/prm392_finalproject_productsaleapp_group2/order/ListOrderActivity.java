package com.example.prm392_finalproject_productsaleapp_group2.order;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.prm392_finalproject_productsaleapp_group2.R;
import com.example.prm392_finalproject_productsaleapp_group2.auth.SessionManager;
import com.example.prm392_finalproject_productsaleapp_group2.models.FilterResponse;
import com.example.prm392_finalproject_productsaleapp_group2.models.Order;
import com.example.prm392_finalproject_productsaleapp_group2.net.OrderApiClient;
import com.example.prm392_finalproject_productsaleapp_group2.services.OrderApiService;
import com.example.prm392_finalproject_productsaleapp_group2.utils.NavigationBarUtil;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ListOrderActivity extends AppCompatActivity {
    
    // Views
    private ImageView btnBack, btnSearch;
    private TextView tabPending, tabDelivering, tabSuccess;
    private EditText edtSearch;
    private LinearLayout layoutEmpty;
    private RecyclerView rvOrders;
    
    // Adapter and Data
    private OrderAdapter orderAdapter;
    private List<Order> orders;
    private String currentStatus = "Pending";
    private int currentPage = 1;
    private final int pageSize = 10;
    
    // API Service
    private OrderApiService orderApiService;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        
        // Set status bar transparent to let gradient show through
        getWindow().setStatusBarColor(android.graphics.Color.TRANSPARENT);
        
        setContentView(R.layout.activity_list_order);
        
        // Handle window insets for edge-to-edge display
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
        
        initViews();
        initData();
        setupListeners();
        setupNavigationBar();
        loadOrders();
    }
    
    private void initViews() {
        btnBack = findViewById(R.id.btn_back_header);
        btnSearch = findViewById(R.id.btn_search_header);
        tabPending = findViewById(R.id.tab_pending);
        tabDelivering = findViewById(R.id.tab_delivering);
        tabSuccess = findViewById(R.id.tab_success);
        edtSearch = findViewById(R.id.edt_search);
        layoutEmpty = findViewById(R.id.layout_empty);
        rvOrders = findViewById(R.id.rv_orders);
    }
    
    private void initData() {
        orders = new ArrayList<>();
        orderAdapter = new OrderAdapter();
        orderApiService = OrderApiClient.getInstance().getApiService();
        sessionManager = new SessionManager(this);
        
        // Setup RecyclerView
        rvOrders.setLayoutManager(new LinearLayoutManager(this));
        rvOrders.setAdapter(orderAdapter);
        
        // Set default tab
        updateTabSelection(tabPending);
    }
    
    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());
        
        btnSearch.setOnClickListener(v -> {
            if (edtSearch.getVisibility() == View.GONE) {
                edtSearch.setVisibility(View.VISIBLE);
                edtSearch.requestFocus();
            } else {
                edtSearch.setVisibility(View.GONE);
                edtSearch.setText("");
                loadOrders();
            }
        });
        
        tabPending.setOnClickListener(v -> {
            currentStatus = "Pending";
            updateTabSelection(tabPending);
            loadOrders();
        });
        
        tabDelivering.setOnClickListener(v -> {
            currentStatus = "Delivering";
            updateTabSelection(tabDelivering);
            loadOrders();
        });
        
        tabSuccess.setOnClickListener(v -> {
            currentStatus = "Success";
            updateTabSelection(tabSuccess);
            loadOrders();
        });
        
        edtSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            
            @Override
            public void afterTextChanged(Editable s) {
                String searchText = s.toString().trim();
                if (searchText.isEmpty()) {
                    loadOrders();
                } else {
                    searchOrders(searchText);
                }
            }
        });
        
        orderAdapter.setOnOrderClickListener(order -> {
            // Navigate to OrderDetailActivity
            Intent intent = new Intent(this, OrderDetailActivity.class);
            intent.putExtra("orderId", order.getOrderId());
            startActivity(intent);
        });
    }
    
    private void setupNavigationBar() {
        // Setup bottom navigation bar
        NavigationBarUtil.setupNavigationBar(this);
        // Highlight profile button since this is related to profile/orders
        NavigationBarUtil.setActiveNavigationButton(this, "profile");
    }
    
    private void updateTabSelection(TextView selectedTab) {
        // Reset all tabs
        tabPending.setBackgroundResource(R.drawable.tab_inactive_background);
        tabPending.setTextColor(getResources().getColor(android.R.color.darker_gray));
        tabDelivering.setBackgroundResource(R.drawable.tab_inactive_background);
        tabDelivering.setTextColor(getResources().getColor(android.R.color.darker_gray));
        tabSuccess.setBackgroundResource(R.drawable.tab_inactive_background);
        tabSuccess.setTextColor(getResources().getColor(android.R.color.darker_gray));
        
        // Set selected tab
        selectedTab.setBackgroundResource(R.drawable.tab_active_background);
        selectedTab.setTextColor(getResources().getColor(android.R.color.white));
    }
    
    private void loadOrders() {
        Integer userId = sessionManager.getUserId();
        if (userId == null) {
            Toast.makeText(this, "Please login first", Toast.LENGTH_SHORT).show();
            return;
        }
        
        String token = "Bearer " + sessionManager.getAuthToken();
        Call<FilterResponse<Order>> call = orderApiService.filterOrders(
                token, // authorization token
                null, // orderId
                userId, // userId
                null, // cartId
                currentStatus, // orderStatus
                currentPage, // pageNumber
                pageSize // pageSize
        );
        
        call.enqueue(new Callback<FilterResponse<Order>>() {
            @Override
            public void onResponse(Call<FilterResponse<Order>> call, Response<FilterResponse<Order>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Order> newOrders = response.body().getItems();
                    if (newOrders != null) {
                        orders.clear();
                        orders.addAll(newOrders);
                        orderAdapter.setOrders(orders);
                        updateEmptyState();
                    }
                } else {
                    Toast.makeText(ListOrderActivity.this, "Failed to load orders", Toast.LENGTH_SHORT).show();
                    updateEmptyState();
                }
            }
            
            @Override
            public void onFailure(Call<FilterResponse<Order>> call, Throwable t) {
                Toast.makeText(ListOrderActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                updateEmptyState();
            }
        });
    }
    
    private void searchOrders(String searchText) {
        Integer userId = sessionManager.getUserId();
        if (userId == null) {
            Toast.makeText(this, "Please login first", Toast.LENGTH_SHORT).show();
            return;
        }
        
        try {
            Integer orderId = Integer.parseInt(searchText);
            String token = "Bearer " + sessionManager.getAuthToken();
            Call<FilterResponse<Order>> call = orderApiService.filterOrders(
                    token, // authorization token
                    orderId, // orderId
                    userId, // userId
                    null, // cartId
                    currentStatus, // orderStatus
                    1, // pageNumber
                    pageSize // pageSize
            );
            
            call.enqueue(new Callback<FilterResponse<Order>>() {
                @Override
                public void onResponse(Call<FilterResponse<Order>> call, Response<FilterResponse<Order>> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        List<Order> searchResults = response.body().getItems();
                        if (searchResults != null) {
                            orders.clear();
                            orders.addAll(searchResults);
                            orderAdapter.setOrders(orders);
                            updateEmptyState();
                        }
                    } else {
                        orders.clear();
                        orderAdapter.setOrders(orders);
                        updateEmptyState();
                    }
                }
                
                @Override
                public void onFailure(Call<FilterResponse<Order>> call, Throwable t) {
                    Toast.makeText(ListOrderActivity.this, "Search failed: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    orders.clear();
                    orderAdapter.setOrders(orders);
                    updateEmptyState();
                }
            });
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Please enter a valid Order ID", Toast.LENGTH_SHORT).show();
        }
    }
    
    private void updateEmptyState() {
        if (orders.isEmpty()) {
            layoutEmpty.setVisibility(View.VISIBLE);
            rvOrders.setVisibility(View.GONE);
        } else {
            layoutEmpty.setVisibility(View.GONE);
            rvOrders.setVisibility(View.VISIBLE);
        }
    }
}