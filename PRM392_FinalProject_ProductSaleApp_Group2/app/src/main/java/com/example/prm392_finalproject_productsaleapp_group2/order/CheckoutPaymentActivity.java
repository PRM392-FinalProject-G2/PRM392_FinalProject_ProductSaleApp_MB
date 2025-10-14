package com.example.prm392_finalproject_productsaleapp_group2.order;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.Button;
import android.widget.EditText;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.prm392_finalproject_productsaleapp_group2.R;
import com.bumptech.glide.Glide;
import com.example.prm392_finalproject_productsaleapp_group2.auth.SessionManager;
import com.example.prm392_finalproject_productsaleapp_group2.models.Cart;
import com.example.prm392_finalproject_productsaleapp_group2.models.CartItem;
import com.example.prm392_finalproject_productsaleapp_group2.models.FilterResponse;
import com.example.prm392_finalproject_productsaleapp_group2.models.UserVoucher;
import com.example.prm392_finalproject_productsaleapp_group2.net.CartApiClient;
import com.example.prm392_finalproject_productsaleapp_group2.net.VoucherApiClient;
import com.example.prm392_finalproject_productsaleapp_group2.services.CartApiService;
import com.example.prm392_finalproject_productsaleapp_group2.services.VoucherApiService;

public class CheckoutPaymentActivity extends AppCompatActivity {

    private RecyclerView rvCartItems;
    private TextView tvSelectedVoucher;
    private Button btnChooseVoucher;
    private EditText etBillingAddress, etPhone;
    private SessionManager sessionManager;
    private int userId;
    private int cartId;
    private java.util.List<CartItem> cartItems = new java.util.ArrayList<>();
    private CheckoutCartAdapter cartAdapter;
    private Integer selectedVoucherId;
    private String lastPaymentUrl; // cache to switch button behavior
    private boolean hasConfirmed = false; // chặn chọn voucher sau khi xác nhận

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        
        // Set status bar transparent to let gradient show through
        getWindow().setStatusBarColor(android.graphics.Color.TRANSPARENT);
        
        setContentView(R.layout.activity_checkout_payment);
        
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0);
            return WindowInsetsCompat.CONSUMED;
        });

        sessionManager = new SessionManager(this);
        userId = sessionManager.getUserId();
        cartId = getIntent().getIntExtra("cartId", -1);

        rvCartItems = findViewById(R.id.rv_cart_items);
        tvSelectedVoucher = findViewById(R.id.tv_selected_voucher);
        btnChooseVoucher = findViewById(R.id.btn_choose_voucher);
        etBillingAddress = findViewById(R.id.et_billing_address);
        etPhone = findViewById(R.id.et_phone);

        cartAdapter = new CheckoutCartAdapter(cartItems);
        rvCartItems.setLayoutManager(new LinearLayoutManager(this));
        rvCartItems.setAdapter(cartAdapter);

        btnChooseVoucher.setOnClickListener(v -> {
            if (hasConfirmed) {
                Toast.makeText(this, "Đã xác nhận thanh toán, không thể chọn voucher", Toast.LENGTH_SHORT).show();
                return;
            }
            openVoucherDialog();
        });
        findViewById(R.id.btn_pay).setOnClickListener(v -> onPayButtonClick());

        loadCart();
        preloadUserContact();
    }

    private void onPayButtonClick() {
        Button btn = findViewById(R.id.btn_pay);
        if (lastPaymentUrl != null && !lastPaymentUrl.isEmpty()) {
            // Second click: go to VNPay
            android.content.Intent intent = new android.content.Intent(CheckoutPaymentActivity.this, VnpayWebViewActivity.class);
            intent.putExtra("paymentUrl", lastPaymentUrl);
            startActivity(intent);
        } else {
            // First click: create order then switch label
            // Chặn voucher về sau
            hasConfirmed = true;
            if (btnChooseVoucher != null) btnChooseVoucher.setEnabled(false);
            // Hiệu ứng loading giả 2s rồi mới gọi create-order (giữ nguyên logic cũ sau đó)
            new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(this::createOrderAndShowVnpay, 2000);
        }
    }

    private void loadCart() {
        String token = "Bearer " + sessionManager.getAuthToken();
        CartApiService api = CartApiClient.getInstance().getApiService();
        api.getCartItems(token, userId, "Active").enqueue(new retrofit2.Callback<com.example.prm392_finalproject_productsaleapp_group2.models.CartResponse>() {
            @Override
            public void onResponse(retrofit2.Call<com.example.prm392_finalproject_productsaleapp_group2.models.CartResponse> call, retrofit2.Response<com.example.prm392_finalproject_productsaleapp_group2.models.CartResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getItems() != null && !response.body().getItems().isEmpty()) {
                    Cart cart = response.body().getItems().get(0);
                    cartItems.clear();
                    if (cart.getCartItems() != null) cartItems.addAll(cart.getCartItems());
                    cartAdapter.notifyDataSetChanged();
                    updateTotal();
                    if (cartId <= 0) cartId = cart.getCartId();
                } else {
                    Toast.makeText(CheckoutPaymentActivity.this, "Không tải được giỏ hàng", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(retrofit2.Call<com.example.prm392_finalproject_productsaleapp_group2.models.CartResponse> call, Throwable t) {
                Toast.makeText(CheckoutPaymentActivity.this, "Lỗi kết nối giỏ hàng", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateTotal() {
        int total = 0;
        for (CartItem item : cartItems) {
            total += item.getPrice() * item.getQuantity();
        }
        java.text.NumberFormat formatter = java.text.NumberFormat.getNumberInstance(java.util.Locale.getDefault());
        TextView tvOriginal = findViewById(R.id.tv_original_amount);
        TextView tvFinal = findViewById(R.id.tv_final_amount);
        if (tvOriginal != null) tvOriginal.setText(formatter.format(total) + " ₫");
        if (tvFinal != null) tvFinal.setText(formatter.format(total) + " ₫");
    }

    private void preloadUserContact() {
        // Use profile API to load address and phone for current user
        com.example.prm392_finalproject_productsaleapp_group2.net.ProfileApiClient.getInstance()
                .getApiService()
                .getUserInfo("Bearer " + sessionManager.getAuthToken(), userId)
                .enqueue(new retrofit2.Callback<com.example.prm392_finalproject_productsaleapp_group2.models.UserResponse>() {
                    @Override
                    public void onResponse(retrofit2.Call<com.example.prm392_finalproject_productsaleapp_group2.models.UserResponse> call, retrofit2.Response<com.example.prm392_finalproject_productsaleapp_group2.models.UserResponse> response) {
                        if (response.isSuccessful() && response.body()!=null) {
                            com.example.prm392_finalproject_productsaleapp_group2.models.UserResponse u = response.body();
                            if (u.getAddress()!=null && etBillingAddress.getText().toString().trim().isEmpty()) {
                                etBillingAddress.setText(u.getAddress());
                            }
                            if (u.getPhone()!=null && etPhone.getText().toString().trim().isEmpty()) {
                                etPhone.setText(u.getPhone());
                            }
                        }
                    }

                    @Override
                    public void onFailure(retrofit2.Call<com.example.prm392_finalproject_productsaleapp_group2.models.UserResponse> call, Throwable t) { }
                });
    }

    private void openVoucherDialog() {
        String token = "Bearer " + sessionManager.getAuthToken();
        VoucherApiService api = VoucherApiClient.getInstance().getApiService();
        api.getUserVouchers(token, userId, false).enqueue(new retrofit2.Callback<FilterResponse<UserVoucher>>() {
            @Override
            public void onResponse(retrofit2.Call<FilterResponse<UserVoucher>> call, retrofit2.Response<FilterResponse<UserVoucher>> response) {
                java.util.List<UserVoucher> vouchers = (response.isSuccessful() && response.body()!=null) ? response.body().getItems() : java.util.Collections.emptyList();
                showVoucherDialog(vouchers);
            }

            @Override
            public void onFailure(retrofit2.Call<FilterResponse<UserVoucher>> call, Throwable t) {
                Toast.makeText(CheckoutPaymentActivity.this, "Lỗi tải voucher", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showVoucherDialog(java.util.List<UserVoucher> vouchers) {
        if (vouchers == null || vouchers.isEmpty()) {
            Toast.makeText(this, "Không có voucher khả dụng", Toast.LENGTH_SHORT).show();
            return;
        }
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle("Chọn voucher");
        String[] items = new String[vouchers.size()];
        for (int i = 0; i < vouchers.size(); i++) {
            UserVoucher uv = vouchers.get(i);
            String code = (uv.getVoucher()!=null && uv.getVoucher().getCode()!=null) ? uv.getVoucher().getCode() : ("ID:"+uv.getVoucherId());
            items[i] = code;
        }
        builder.setItems(items, (dialog, which) -> {
            UserVoucher selected = vouchers.get(which);
            selectedVoucherId = selected.getVoucherId();
            tvSelectedVoucher.setText("Voucher: " + items[which]);
        });
        builder.setNeutralButton("Bỏ chọn", (dialog, which) -> {
            // Clear voucher selection
            selectedVoucherId = null;
            tvSelectedVoucher.setText("Chưa chọn voucher");
        });
        builder.setNegativeButton("Đóng", null);
        builder.show();
    }

    private void createOrderAndShowVnpay() {
        String address = etBillingAddress.getText().toString().trim();
        if (address.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập địa chỉ", Toast.LENGTH_SHORT).show();
            return;
        }
        String token = "Bearer " + sessionManager.getAuthToken();
        com.example.prm392_finalproject_productsaleapp_group2.services.PaymentApiService.CreateOrderRequest req = new com.example.prm392_finalproject_productsaleapp_group2.services.PaymentApiService.CreateOrderRequest();
        req.cartId = cartId;
        req.userId = userId;
        req.paymentMethod = "Vnpay";
        req.billingAddress = address;
        // Match BE expected field values
        req.orderStatus = "string";
        req.voucherId = selectedVoucherId;

        com.example.prm392_finalproject_productsaleapp_group2.services.PaymentApiService api = com.example.prm392_finalproject_productsaleapp_group2.net.PaymentApiClient.getInstance().getApiService();
        api.createVnpayOrder(token, req).enqueue(new retrofit2.Callback<com.example.prm392_finalproject_productsaleapp_group2.models.PaymentCreateResponse>() {
            @Override
            public void onResponse(retrofit2.Call<com.example.prm392_finalproject_productsaleapp_group2.models.PaymentCreateResponse> call, retrofit2.Response<com.example.prm392_finalproject_productsaleapp_group2.models.PaymentCreateResponse> response) {
                if (response.isSuccessful() && response.body()!=null) {
                    com.example.prm392_finalproject_productsaleapp_group2.models.PaymentCreateResponse data = response.body();
                    // Update amounts UI
                    TextView tvOriginal = findViewById(R.id.tv_original_amount);
                    TextView tvDiscount = findViewById(R.id.tv_discount_amount);
                    TextView tvFinal = findViewById(R.id.tv_final_amount);
                    java.text.NumberFormat f = java.text.NumberFormat.getNumberInstance(java.util.Locale.getDefault());
                    tvOriginal.setText(f.format(data.getOriginalAmount()) + " ₫");
                    tvDiscount.setText("-" + f.format(data.getVoucherDiscount()) + " ₫");
                    tvFinal.setText(f.format(data.getFinalAmount()) + " ₫");

                    // Cache URL and switch button label to VNPay (do not auto-open)
                    lastPaymentUrl = data.getPaymentUrl();
                    Button btn = findViewById(R.id.btn_pay);
                    if (btn != null) btn.setText("Thanh toán bằng VNPay");
                } else {
                    String detail = "";
                    try {
                        if (response.errorBody()!=null) {
                            detail = response.errorBody().string();
                        }
                    } catch (Exception ignored) {}
                    Toast.makeText(CheckoutPaymentActivity.this, "Tạo đơn hàng thất bại (" + response.code() + ")", Toast.LENGTH_SHORT).show();
                    android.util.Log.e("Checkout", "Create order failed: code=" + response.code() + ", body=" + detail);
                }
            }

            @Override
            public void onFailure(retrofit2.Call<com.example.prm392_finalproject_productsaleapp_group2.models.PaymentCreateResponse> call, Throwable t) {
                Toast.makeText(CheckoutPaymentActivity.this, "Lỗi kết nối thanh toán", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Adapter reusing item_cart layout for display-only
    private static class CheckoutCartAdapter extends RecyclerView.Adapter<CheckoutCartAdapter.VH> {
        private final java.util.List<CartItem> items;
        CheckoutCartAdapter(java.util.List<CartItem> items) { this.items = items; }

        static class VH extends RecyclerView.ViewHolder {
            ImageView ivProductImage;
            TextView tvProductName, tvCategory, tvPrice, tvQuantity;
            TextView btnMinus, btnPlus, btnDetails, btnDelete;
            VH(android.view.View itemView) {
                super(itemView);
                ivProductImage = itemView.findViewById(R.id.iv_product_image);
                tvProductName = itemView.findViewById(R.id.tv_product_name);
                tvCategory = itemView.findViewById(R.id.tv_category);
                tvPrice = itemView.findViewById(R.id.tv_price);
                tvQuantity = itemView.findViewById(R.id.tv_quantity);
                btnMinus = itemView.findViewById(R.id.btn_minus);
                btnPlus = itemView.findViewById(R.id.btn_plus);
                btnDetails = itemView.findViewById(R.id.btn_details);
                btnDelete = itemView.findViewById(R.id.btn_delete);
            }
        }

        @Override
        public VH onCreateViewHolder(android.view.ViewGroup parent, int viewType) {
            android.view.View view = android.view.LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_cart, parent, false);
            return new VH(view);
        }

        @Override
        public void onBindViewHolder(VH h, int pos) {
            CartItem ci = items.get(pos);
            // Image: ưu tiên ảnh đầu tiên trong productImages, fallback imageUrl
            if (ci.getProduct() != null) {
                String url = null;
                if (ci.getProduct().getProductImages() != null && !ci.getProduct().getProductImages().isEmpty()) {
                    url = ci.getProduct().getProductImages().get(0).getImageUrl();
                }
                if (url == null) {
                    url = ci.getProduct().getImageUrl();
                }
                if (url != null) {
                    Glide.with(h.itemView.getContext())
                            .load(url)
                            .placeholder(R.drawable.ic_placeholder)
                            .error(R.drawable.ic_placeholder)
                            .into(h.ivProductImage);
                } else {
                    h.ivProductImage.setImageResource(R.drawable.ic_placeholder);
                }
            } else {
                h.ivProductImage.setImageResource(R.drawable.ic_placeholder);
            }
            // Texts
            String name = (ci.getProduct()!=null && ci.getProduct().getProductName()!=null) ? ci.getProduct().getProductName() : ("SP #"+ci.getProductId());
            h.tvProductName.setText(name);
            String categoryName = (ci.getProduct()!=null && ci.getProduct().getCategory()!=null) ? ci.getProduct().getCategory().getCategoryName() : "Không xác định";
            h.tvCategory.setText(categoryName);
            java.text.NumberFormat f = java.text.NumberFormat.getNumberInstance(java.util.Locale.getDefault());
            h.tvPrice.setText(f.format(ci.getPrice()) + " ₫");
            h.tvQuantity.setText(String.valueOf(ci.getQuantity()));
            // Hide interactive buttons in checkout
            h.btnMinus.setVisibility(android.view.View.GONE);
            h.btnPlus.setVisibility(android.view.View.GONE);
            h.btnDetails.setVisibility(android.view.View.GONE);
            h.btnDelete.setVisibility(android.view.View.GONE);
        }

        @Override
        public int getItemCount() { return items.size(); }
    }
}