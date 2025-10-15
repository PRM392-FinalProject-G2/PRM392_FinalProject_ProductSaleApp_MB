package com.example.prm392_finalproject_productsaleapp_group2.order;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
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
import com.example.prm392_finalproject_productsaleapp_group2.models.CartItem;
import com.example.prm392_finalproject_productsaleapp_group2.models.Order;
import com.example.prm392_finalproject_productsaleapp_group2.models.Product;
import com.example.prm392_finalproject_productsaleapp_group2.net.OrderApiClient;
import com.example.prm392_finalproject_productsaleapp_group2.services.OrderApiService;
import com.example.prm392_finalproject_productsaleapp_group2.utils.NavigationBarUtil;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OrderDetailActivity extends AppCompatActivity implements OrderItemAdapter.OnProductClickListener {
    
    // Views
    private ImageView btnBack;
    private TextView tvOrderId, tvOrderDate, tvStatus, tvTotalAmount, tvPaymentMethod, tvBillingAddress;
    private LinearLayout layoutEmpty;
    private RecyclerView rvOrderItems;
    
    // Data
    private Order order;
    private int orderId;
    private List<CartItem> orderItems;
    private OrderItemAdapter orderItemAdapter;
    
    // API Service
    private OrderApiService orderApiService;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        try {
            EdgeToEdge.enable(this);
            
            // Set status bar transparent to let gradient show through
            getWindow().setStatusBarColor(android.graphics.Color.TRANSPARENT);
            
            setContentView(R.layout.activity_order_detail);
            
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
            
            // Get orderId from intent
            orderId = getIntent().getIntExtra("orderId", -1);
            if (orderId == -1) {
                Toast.makeText(this, "Invalid order ID", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }
            
            initViews();
            initData();
            setupListeners();
            setupNavigationBar();
            loadOrderDetail();
        } catch (Exception e) {
            Toast.makeText(this, "Error initializing activity: " + e.getMessage(), Toast.LENGTH_LONG).show();
            finish();
        }
    }
    
    private void initViews() {
        btnBack = findViewById(R.id.btn_back_header);
        tvOrderId = findViewById(R.id.tv_order_id);
        tvOrderDate = findViewById(R.id.tv_order_date);
        tvStatus = findViewById(R.id.tv_status);
        tvTotalAmount = findViewById(R.id.tv_total_amount);
        tvPaymentMethod = findViewById(R.id.tv_payment_method);
        tvBillingAddress = findViewById(R.id.tv_billing_address);
        layoutEmpty = findViewById(R.id.layout_empty);
        rvOrderItems = findViewById(R.id.rv_order_items);
    }
    
    private void initData() {
        orderItems = new ArrayList<>();
        orderItemAdapter = new OrderItemAdapter();
        orderApiService = OrderApiClient.getInstance().getApiService();
        sessionManager = new SessionManager(this);
        
        // Setup RecyclerView
        rvOrderItems.setLayoutManager(new LinearLayoutManager(this));
        rvOrderItems.setAdapter(orderItemAdapter);
        
        // Set click listener
        orderItemAdapter.setOnProductClickListener(this);
    }
    
    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());
    }
    
    private void setupNavigationBar() {
        // Setup bottom navigation bar
        NavigationBarUtil.setupNavigationBar(this);
        // Highlight profile button since this is related to profile/orders
        NavigationBarUtil.setActiveNavigationButton(this, "profile");
    }
    
    private void loadOrderDetail() {
        try {
            String token = "Bearer " + sessionManager.getAuthToken();
            if (token == null || token.isEmpty() || token.equals("Bearer null")) {
                Toast.makeText(this, "Please login first", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }
            
            Call<Order> call = orderApiService.getOrderById(token, orderId);
            
            call.enqueue(new Callback<Order>() {
                @Override
                public void onResponse(Call<Order> call, Response<Order> response) {
                    try {
                        if (response.isSuccessful() && response.body() != null) {
                            order = response.body();
                            
                            // Debug: Log the raw response
                            android.util.Log.d("OrderDetail", "API Response successful");
                            android.util.Log.d("OrderDetail", "Order object: " + order.toString());
                            
                            updateOrderInfo();
                            loadOrderItems();
                        } else {
                            android.util.Log.e("OrderDetail", "API Response failed: " + response.code() + " - " + response.message());
                            Toast.makeText(OrderDetailActivity.this, "Failed to load order details", Toast.LENGTH_SHORT).show();
                            updateEmptyState();
                        }
                    } catch (Exception e) {
                        android.util.Log.e("OrderDetail", "Error processing order: " + e.getMessage(), e);
                        Toast.makeText(OrderDetailActivity.this, "Error processing order: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        updateEmptyState();
                    }
                }
                
                @Override
                public void onFailure(Call<Order> call, Throwable t) {
                    Toast.makeText(OrderDetailActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    updateEmptyState();
                }
            });
        } catch (Exception e) {
            Toast.makeText(this, "Error loading order: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            finish();
        }
    }
    
    private void updateOrderInfo() {
        try {
            if (order != null) {
                // Debug logging
                android.util.Log.d("OrderDetail", "Order ID: " + order.getOrderId());
                android.util.Log.d("OrderDetail", "Billing Address: " + order.getBillingAddress());
                android.util.Log.d("OrderDetail", "Payment Method: " + order.getPaymentMethod());
                android.util.Log.d("OrderDetail", "Order Date: " + order.getOrderDate());
                
                tvOrderId.setText("#" + order.getOrderId());
                tvOrderDate.setText(order.getFormattedOrderDate() != null ? order.getFormattedOrderDate() : "N/A");
                tvTotalAmount.setText(order.getFormattedTotalAmount() != null ? order.getFormattedTotalAmount() : "0 ₫");
                tvPaymentMethod.setText(order.getPaymentMethod() != null ? order.getPaymentMethod() : "N/A");
                
                // Check if billing address is null or empty
                String billingAddress = order.getBillingAddress();
                if (billingAddress != null && !billingAddress.trim().isEmpty()) {
                    tvBillingAddress.setText(billingAddress);
                } else {
                    tvBillingAddress.setText("Chưa có địa chỉ");
                }
                
                // Set status
                tvStatus.setText(order.getStatusDisplayText() != null ? order.getStatusDisplayText() : "Unknown");
                String resourceName = order.getStatusBackgroundResource();
                if (resourceName != null) {
                    int resourceId = getResources().getIdentifier(resourceName, "drawable", getPackageName());
                    if (resourceId != 0) {
                        tvStatus.setBackgroundResource(resourceId);
                    }
                }
            }
        } catch (Exception e) {
            Toast.makeText(this, "Error updating order info: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
    
    private void loadOrderItems() {
        try {
            android.util.Log.d("OrderDetailActivity", "Loading order items for order: " + order);
            if (order != null && order.getCart() != null && order.getCart().getCartItems() != null) {
                android.util.Log.d("OrderDetailActivity", "Cart items: " + order.getCart().getCartItems());
                orderItems.clear();
                orderItems.addAll(order.getCart().getCartItems());
                orderItemAdapter.setCartItems(orderItems);
                updateEmptyState();
            } else {
                android.util.Log.d("OrderDetailActivity", "No cart items found");
                orderItems.clear();
                orderItemAdapter.setCartItems(orderItems);
                updateEmptyState();
            }
        } catch (Exception e) {
            android.util.Log.e("OrderDetailActivity", "Error loading order items: " + e.getMessage(), e);
            Toast.makeText(this, "Error loading order items: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            orderItems.clear();
            orderItemAdapter.setCartItems(orderItems);
            updateEmptyState();
        }
    }
    
    private void updateEmptyState() {
        if (orderItems.isEmpty()) {
            layoutEmpty.setVisibility(View.VISIBLE);
            rvOrderItems.setVisibility(View.GONE);
        } else {
            layoutEmpty.setVisibility(View.GONE);
            rvOrderItems.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onProductClick(Product product) {
        // Navigate to ProductDetailActivity
        Intent intent = new Intent(this, com.example.prm392_finalproject_productsaleapp_group2.product.ProductDetailActivity.class);
        intent.putExtra("productId", product.getProductId());
        startActivity(intent);
    }
}