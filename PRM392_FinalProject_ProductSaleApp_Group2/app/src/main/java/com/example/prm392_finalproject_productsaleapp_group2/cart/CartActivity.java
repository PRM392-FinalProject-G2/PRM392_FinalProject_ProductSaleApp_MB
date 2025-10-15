package com.example.prm392_finalproject_productsaleapp_group2.cart;

import android.os.Bundle;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
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
import com.example.prm392_finalproject_productsaleapp_group2.models.Cart;
import com.example.prm392_finalproject_productsaleapp_group2.models.CartItem;
import com.example.prm392_finalproject_productsaleapp_group2.models.CartResponse;
import com.example.prm392_finalproject_productsaleapp_group2.net.CartApiClient;
import com.example.prm392_finalproject_productsaleapp_group2.services.CartApiService;
import com.example.prm392_finalproject_productsaleapp_group2.product.ProductDetailActivity;
import com.example.prm392_finalproject_productsaleapp_group2.order.CheckoutPaymentActivity;
import com.example.prm392_finalproject_productsaleapp_group2.utils.NavigationBarUtil;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CartActivity extends AppCompatActivity implements CartAdapter.OnCartItemClickListener {

    private RecyclerView rvCartItems;
    private CartAdapter cartAdapter;
    private List<CartItem> cartItems = new ArrayList<>();
    private ProgressBar progressBar;
    private LinearLayout emptyState;
    private TextView tvTotalQuantity, tvTotalPrice;
    private Button btnBuyNow;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        
        // Set status bar transparent to let gradient show through
        getWindow().setStatusBarColor(android.graphics.Color.TRANSPARENT);
        
        setContentView(R.layout.activity_cart);
        
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            // Don't add padding to let gradient extend to status bar
            return WindowInsetsCompat.CONSUMED;
        });

        // Khởi tạo các view
        initViews();
        
        // Khởi tạo session manager
        sessionManager = new SessionManager(this);
        
        // Thiết lập navigation bar
        NavigationBarUtil.setupNavigationBar(this);
        NavigationBarUtil.setActiveNavigationButton(this, "cart");
        
        // Thiết lập RecyclerView
        setupRecyclerView();
        
        // Tải dữ liệu giỏ hàng
        loadCartData();
    }

    private void initViews() {
        rvCartItems = findViewById(R.id.rv_cart_items);
        progressBar = findViewById(R.id.progress_bar);
        emptyState = findViewById(R.id.empty_state);
        tvTotalQuantity = findViewById(R.id.tv_total_quantity);
        tvTotalPrice = findViewById(R.id.tv_total_price);
        btnBuyNow = findViewById(R.id.btn_buy_now);
        
        btnBuyNow.setOnClickListener(v -> {
            if (cartItems.isEmpty()) {
                Toast.makeText(this, "Giỏ hàng trống", Toast.LENGTH_SHORT).show();
                return;
            }
            // Pass first cart's id and totals
            int totalQuantity = 0;
            int totalPrice = 0;
            for (CartItem item : cartItems) {
                totalQuantity += item.getQuantity();
                totalPrice += item.getPrice() * item.getQuantity();
            }

            Intent intent = new Intent(this, CheckoutPaymentActivity.class);
            intent.putExtra("totalQuantity", totalQuantity);
            intent.putExtra("totalPrice", totalPrice);
            // cartId is from first item's cartId (all items are same cart)
            if (!cartItems.isEmpty()) {
                intent.putExtra("cartId", cartItems.get(0).getCartId());
            }
            startActivity(intent);
        });
    }

    private void setupRecyclerView() {
        cartAdapter = new CartAdapter(this, cartItems);
        cartAdapter.setOnCartItemClickListener(this);
        rvCartItems.setLayoutManager(new LinearLayoutManager(this));
        rvCartItems.setAdapter(cartAdapter);
    }

    private void loadCartData() {
        int userId = sessionManager.getUserId();
        if (userId == -1) {
            Toast.makeText(this, "Vui lòng đăng nhập", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        showLoading(true);
        
        CartApiService apiService = CartApiClient.getInstance().getApiService();
        String authToken = "Bearer " + sessionManager.getAuthToken();
        
        Call<CartResponse> call = apiService.getCartItems(authToken, userId, "Active");
        call.enqueue(new Callback<CartResponse>() {
            @Override
            public void onResponse(Call<CartResponse> call, Response<CartResponse> response) {
                showLoading(false);
                
                if (response.isSuccessful() && response.body() != null) {
                    CartResponse cartResponse = response.body();
                    List<Cart> carts = cartResponse.getItems();
                    
                    if (carts != null && !carts.isEmpty()) {
                        // Lấy cart items từ cart đầu tiên (giả sử một user có một cart active)
                        Cart cart = carts.get(0);
                        List<CartItem> items = cart.getCartItems();
                        
                        if (items != null && !items.isEmpty()) {
                            cartItems.clear();
                            cartItems.addAll(items);
                            cartAdapter.updateCartItems(cartItems);
                            updateOrderSummary();
                            showEmptyState(false);
                        } else {
                            showEmptyState(true);
                        }
                    } else {
                        showEmptyState(true);
                    }
                } else {
                    Log.e("CartActivity", "Không thể tải giỏ hàng: " + response.code());
                    Toast.makeText(CartActivity.this, "Không thể tải giỏ hàng", Toast.LENGTH_SHORT).show();
                    showEmptyState(true);
                }
            }

            @Override
            public void onFailure(Call<CartResponse> call, Throwable t) {
                showLoading(false);
                Log.e("CartActivity", "Lỗi khi tải giỏ hàng", t);
                Toast.makeText(CartActivity.this, "Lỗi kết nối", Toast.LENGTH_SHORT).show();
                showEmptyState(true);
            }
        });
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        rvCartItems.setVisibility(show ? View.GONE : View.VISIBLE);
    }

    private void showEmptyState(boolean show) {
        emptyState.setVisibility(show ? View.VISIBLE : View.GONE);
        rvCartItems.setVisibility(show ? View.GONE : View.VISIBLE);
        if (show) {
            cartItems.clear();
            cartAdapter.updateCartItems(cartItems);
            updateOrderSummary();
        }
    }

    private void updateOrderSummary() {
        int totalQuantity = 0;
        int totalPrice = 0;
        
        for (CartItem item : cartItems) {
            totalQuantity += item.getQuantity();
            totalPrice += item.getPrice() * item.getQuantity();
        }
        
        tvTotalQuantity.setText(totalQuantity + " sản phẩm");
        
        NumberFormat formatter = NumberFormat.getNumberInstance(Locale.getDefault());
        String formattedPrice = formatter.format(totalPrice) + " ₫";
        tvTotalPrice.setText(formattedPrice);
    }

    @Override
    public void onQuantityChange(CartItem cartItem, int newQuantity) {
        // Tạo object CartItem để gửi lên server
        CartItem updateItem = new CartItem();
        updateItem.setCartId(cartItem.getCartId());
        updateItem.setProductId(cartItem.getProductId());
        updateItem.setQuantity(newQuantity);
        updateItem.setPrice(cartItem.getPrice());
        
        // Gọi API cập nhật
        updateCartItemQuantity(cartItem.getCartItemId(), updateItem, newQuantity);
    }
    
    private void updateCartItemQuantity(int cartItemId, CartItem updateItem, int newQuantity) {
        CartApiService apiService = CartApiClient.getInstance().getApiService();
        String authToken = "Bearer " + sessionManager.getAuthToken();
        
        Call<CartItem> call = apiService.updateCartItem(authToken, cartItemId, updateItem);
        call.enqueue(new Callback<CartItem>() {
            @Override
            public void onResponse(Call<CartItem> call, Response<CartItem> response) {
                if (response.isSuccessful()) {
                    // Cập nhật số lượng trong danh sách local
                    for (CartItem item : cartItems) {
                        if (item.getCartItemId() == cartItemId) {
                            item.setQuantity(newQuantity);
                            break;
                        }
                    }
                    
                    // Cập nhật UI
                    cartAdapter.updateCartItems(cartItems);
                    updateOrderSummary();
                    Toast.makeText(CartActivity.this, "Số lượng đã cập nhật", Toast.LENGTH_SHORT).show();
                } else {
                    Log.e("CartActivity", "Không thể cập nhật số lượng: " + response.code());
                    Toast.makeText(CartActivity.this, "Không thể cập nhật số lượng", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<CartItem> call, Throwable t) {
                Log.e("CartActivity", "Lỗi khi cập nhật số lượng", t);
                Toast.makeText(CartActivity.this, "Lỗi kết nối khi cập nhật", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onItemDetailsClick(CartItem cartItem) {
        int productId = cartItem.getProductId();
        if (productId > 0) {
            Intent intent = new Intent(this, ProductDetailActivity.class);
            intent.putExtra("productId", productId);
            startActivity(intent);
        } else if (cartItem.getProduct() != null) {
            // Fallback if productId missing but product object exists
            int pid = cartItem.getProduct().getProductId();
            if (pid > 0) {
                Intent intent = new Intent(this, ProductDetailActivity.class);
                intent.putExtra("productId", pid);
                startActivity(intent);
                return;
            }
            Toast.makeText(this, "Không tìm thấy mã sản phẩm", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Không tìm thấy mã sản phẩm", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDeleteItemClick(CartItem cartItem) {
        // Hiển thị dialog xác nhận xóa
        new android.app.AlertDialog.Builder(this)
                .setTitle("Xóa sản phẩm")
                .setMessage("Bạn có chắc chắn muốn xóa sản phẩm này khỏi giỏ hàng?")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    deleteCartItem(cartItem.getCartItemId());
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void deleteCartItem(int cartItemId) {
        CartApiService apiService = CartApiClient.getInstance().getApiService();
        String authToken = "Bearer " + sessionManager.getAuthToken();
        
        Call<Void> call = apiService.deleteCartItem(authToken, cartItemId);
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    // Xóa item khỏi danh sách local
                    cartItems.removeIf(item -> item.getCartItemId() == cartItemId);
                    
                    // Cập nhật UI
                    cartAdapter.updateCartItems(cartItems);
                    updateOrderSummary();
                    
                    // Hiển thị thông báo
                    Toast.makeText(CartActivity.this, "Đã xóa sản phẩm khỏi giỏ hàng", Toast.LENGTH_SHORT).show();
                    
                    // Nếu giỏ hàng trống, hiển thị empty state
                    if (cartItems.isEmpty()) {
                        showEmptyState(true);
                    }
                } else {
                    Log.e("CartActivity", "Không thể xóa sản phẩm: " + response.code());
                    Toast.makeText(CartActivity.this, "Không thể xóa sản phẩm", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Log.e("CartActivity", "Lỗi khi xóa sản phẩm", t);
                Toast.makeText(CartActivity.this, "Lỗi kết nối khi xóa", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onBackPressed() {
        NavigationBarUtil.finishActivityWithoutAnimation(this);
    }
}