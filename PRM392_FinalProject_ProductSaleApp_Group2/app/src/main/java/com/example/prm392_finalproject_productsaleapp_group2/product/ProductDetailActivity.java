package com.example.prm392_finalproject_productsaleapp_group2.product;

import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.StyleSpan;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.example.prm392_finalproject_productsaleapp_group2.R;
import com.example.prm392_finalproject_productsaleapp_group2.auth.SessionManager;
import com.example.prm392_finalproject_productsaleapp_group2.models.ApiResponse;
import com.example.prm392_finalproject_productsaleapp_group2.models.Cart;
import com.example.prm392_finalproject_productsaleapp_group2.models.CartItem;
import com.example.prm392_finalproject_productsaleapp_group2.models.CartResponse;
import com.example.prm392_finalproject_productsaleapp_group2.models.FilterResponse;
import com.example.prm392_finalproject_productsaleapp_group2.models.Product;
import com.example.prm392_finalproject_productsaleapp_group2.models.ProductImage;
import com.example.prm392_finalproject_productsaleapp_group2.models.ProductReview;
import com.example.prm392_finalproject_productsaleapp_group2.models.Wishlist;
import com.example.prm392_finalproject_productsaleapp_group2.models.WishlistMobile;
import com.example.prm392_finalproject_productsaleapp_group2.net.CartApiClient;
import com.example.prm392_finalproject_productsaleapp_group2.net.ProductApiClient;
import com.example.prm392_finalproject_productsaleapp_group2.net.WishlistApiClient;
import com.example.prm392_finalproject_productsaleapp_group2.services.CartApiService;
import com.example.prm392_finalproject_productsaleapp_group2.services.ProductApiService;
import com.example.prm392_finalproject_productsaleapp_group2.services.WishlistApiService;
import com.example.prm392_finalproject_productsaleapp_group2.utils.NavigationBarUtil;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProductDetailActivity extends AppCompatActivity {

    //region 🧱 Fields
    private ImageView ivWishlist, btnBack;
    private TextView tvName, tvPrice, tvFullDesc, tvSpecs;
    private EditText etQuantity;
    private  Button btnAddToCart;
    private ViewPager2 viewPager;
    private TabLayout tabDots;
    private Product currentProduct;
    private SessionManager sessionManager;
    private ProductApiService productService;
    private WishlistApiService wishlistService;
    private CartApiService cartService;
    private RecyclerView rvReviews;
    private ProductReviewAdapter reviewAdapter;
    private List<ProductReview> reviewList = new ArrayList<>();
    private int userId;
    private int productId;
    //endregion


    //region ⚙️ Lifecycle
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_product_detail);
        getWindow().setStatusBarColor(android.graphics.Color.TRANSPARENT);

        setupWindowInsets();
        initServices();
        initViews();
        setupListeners();

        productId = getIntent().getIntExtra("productId", -1);
        if (productId > 0) loadProductDetail(productId);
        if (userId > 0 && productId > 0) checkWishlistStatus(productId);
    }
    //endregion


    //region 🧭 Setup
    private void setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, 0, systemBars.right, 0);

            android.view.View header = findViewById(R.id.header_layout);
            if (header != null) {
                header.setPadding(header.getPaddingLeft(), systemBars.top,
                        header.getPaddingRight(), header.getPaddingBottom());
            }
            return WindowInsetsCompat.CONSUMED;
        });
    }

    private void initServices() {
        sessionManager = new SessionManager(this);
        userId = sessionManager.getUserId();
        productService = ProductApiClient.getInstance().getApiService();
        wishlistService = WishlistApiClient.getInstance().getApiService();
        cartService = CartApiClient.getInstance().getApiService();

        NavigationBarUtil.setupNavigationBar(this);
        NavigationBarUtil.setActiveNavigationButton(this, "home");
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        ivWishlist = findViewById(R.id.ivWishlist);
        viewPager = findViewById(R.id.viewPagerImages);
        tabDots = findViewById(R.id.tabDots);

        tvName = findViewById(R.id.tvProductNameDetail);
        tvPrice = findViewById(R.id.tvProductPriceDetail);
        tvFullDesc = findViewById(R.id.tvProductDescriptionDetail);
        tvSpecs = findViewById(R.id.tvProductSpecsDetail);
        rvReviews = findViewById(R.id.rvReviews);
        btnAddToCart = findViewById(R.id.btnAddToCart);
        etQuantity = findViewById(R.id.etQuantity);

        // setup adapter
        reviewAdapter = new ProductReviewAdapter(this, reviewList);
        rvReviews.setLayoutManager(new LinearLayoutManager(this));
        rvReviews.setAdapter(reviewAdapter);

    }
    //endregion


    //region 🔘 Event listeners
    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());

        TextView btnDecrease = findViewById(R.id.btnDecrease);
        TextView btnIncrease = findViewById(R.id.btnIncrease);

        btnIncrease.setOnClickListener(v -> {
            int qty = parseQuantity(etQuantity.getText().toString());
            etQuantity.setText(String.valueOf(qty + 1));
        });

        btnDecrease.setOnClickListener(v -> {
            int qty = parseQuantity(etQuantity.getText().toString());
            if (qty > 1) etQuantity.setText(String.valueOf(qty - 1));
        });

        btnAddToCart.setOnClickListener(v -> {
            if (userId <= 0 || productId <= 0) {
                showToast("Vui lòng đăng nhập để mua hàng");
                return;
            }

            int quantity = parseQuantity(etQuantity.getText().toString());
            addToCart(productId, quantity);
        });

        ivWishlist.setOnClickListener(v -> handleWishlistClick());
    }

    private int parseQuantity(String text) {
        try {
            return Integer.parseInt(text);
        } catch (NumberFormatException e) {
            return 1;
        }
    }
    //endregion


    //region 📦 Load Product Detail
    private void loadProductDetail(int id) {
        String token = sessionManager.getAuthToken();

        productService.getProductById("Bearer " + token, id).enqueue(new Callback<Product>() {
            @Override
            public void onResponse(Call<Product> call, Response<Product> response) {
                if (!response.isSuccessful() || response.body() == null) {
                    showToast("Không thể tải chi tiết sản phẩm");
                    return;
                }
                bindProductDetails(response.body());
            }

            @Override
            public void onFailure(Call<Product> call, Throwable t) {
                showToast("Lỗi mạng khi tải sản phẩm");
            }
        });
    }

    private void bindProductDetails(Product product) {
        tvName.setText(product.getProductName());
        currentProduct = product;
        // 💰 Format giá VNĐ
        String priceStr = NumberFormat.getInstance(new Locale("vi", "VN"))
                .format(product.getPrice()) + " ₫";
        tvPrice.setText(priceStr);

        // 🏷️ Thương hiệu
        if (product.getBrand() != null) {
            TextView tvBrand = findViewById(R.id.tvBrandNameDetail);
            tvBrand.setText("Thương hiệu: " + product.getBrand().getBrandName());
        }

        // ⭐ Đánh giá
        ((TextView) findViewById(R.id.tvAverageRatingDetail))
                .setText("⭐ " + product.getAverageRating());
        ((TextView) findViewById(R.id.tvPopularityDetail))
                .setText("🔥 " + product.getPopularity());
        ((TextView) findViewById(R.id.tvReviewCountDetail))
                .setText("(" + product.getReviewCount() + ")");

        // 🧾 Mô tả & thông số
        setBoldLabelText(tvFullDesc, "Mô tả: ", product.getFullDescription());
        setBoldLabelText(tvSpecs, "Thông số: ", product.getTechnicalSpecifications());

        // 🖼️ Ảnh
        List<String> imageUrls = new ArrayList<>();
        if (product.getProductImages() != null) {
            for (ProductImage img : product.getProductImages()) {
                imageUrls.add(img.getImageUrl());
            }
        }

        ProductDetailImageAdapter adapter = new ProductDetailImageAdapter(this, imageUrls);
        viewPager.setAdapter(adapter);
        new TabLayoutMediator(tabDots, viewPager, (tab, pos) -> tab.setIcon(R.drawable.tab_indicator_dot)).attach();

        // Load reviews
        reviewList.clear();
        if (product.getReviewList() != null) {
            reviewList.addAll(product.getReviewList());
        }
        reviewAdapter.notifyDataSetChanged();

        autoSlideImages();
    }

    private void autoSlideImages() {
        new Handler().postDelayed(new Runnable() {
            int currentPage = 0;

            @Override
            public void run() {
                if (viewPager.getAdapter() != null && viewPager.getAdapter().getItemCount() > 0) {
                    currentPage = (currentPage + 1) % viewPager.getAdapter().getItemCount();
                    viewPager.setCurrentItem(currentPage, true);
                    viewPager.postDelayed(this, 3000);
                }
            }
        }, 3000);
    }

    private void setBoldLabelText(TextView tv, String label, String content) {
        SpannableStringBuilder sb = new SpannableStringBuilder();
        sb.append(label);
        sb.setSpan(new StyleSpan(Typeface.BOLD), 0, label.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        sb.append(content != null ? content : "");
        tv.setText(sb);
    }
    //endregion


    //region 💖 Wishlist
    private void handleWishlistClick() {
        if (userId <= 0 || productId <= 0) return;

        boolean isFav = ivWishlist.getTag() instanceof Boolean && (Boolean) ivWishlist.getTag();
        if (isFav) {
            int wid = ivWishlist.getTag(R.id.tag_wishlist_id) instanceof Integer
                    ? (Integer) ivWishlist.getTag(R.id.tag_wishlist_id) : -1;
            if (wid > 0) removeFromWishlist(wid);
        } else {
            addToWishlist(userId, productId);
        }
    }

    private void checkWishlistStatus(int productId) {
        String token = sessionManager.getAuthToken();

        wishlistService.getWishlistByUser("Bearer " + token, userId, 1, 1000).enqueue(new Callback<FilterResponse<WishlistMobile>>() {
            @Override
            public void onResponse(Call<FilterResponse<WishlistMobile>> call,
                                   Response<FilterResponse<WishlistMobile>> response) {
                if (!response.isSuccessful() || response.body() == null) return;

                boolean found = false;
                int wishlistId = -1;
                for (WishlistMobile w : response.body().getItems()) {
                    if (w.getProductId() == productId) {
                        found = true;
                        wishlistId = w.getWishlistId();
                        break;
                    }
                }

                final boolean isFav = found;
                final int wid = wishlistId;
                runOnUiThread(() -> {
                    ivWishlist.setImageResource(isFav ? R.drawable.baseline_favorite_24 : R.drawable.baseline_favorite_border_24);
                    ivWishlist.setTag(isFav);
                    if (isFav && wid > 0)
                        ivWishlist.setTag(R.id.tag_wishlist_id, wid);
                });
            }

            @Override
            public void onFailure(Call<FilterResponse<WishlistMobile>> call, Throwable t) {
                showToast("Không thể kiểm tra sản phẩm yêu thích");
            }
        });
    }

    private void addToWishlist(int uid, int pid) {
        String token = sessionManager.getAuthToken();
        Wishlist wishlist = new Wishlist(uid, pid);

        wishlistService.addToWishlist("Bearer " + token, wishlist).enqueue(new Callback<Wishlist>() {
            @Override
            public void onResponse(Call<Wishlist> call, Response<Wishlist> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Wishlist result = response.body();
                    ivWishlist.setImageResource(R.drawable.baseline_favorite_24);
                    ivWishlist.setTag(true);
                    ivWishlist.setTag(R.id.tag_wishlist_id, result.getWishlistId());
                } else showToast("Thêm vào sản phẩm yêu thích thất bại");
            }

            @Override
            public void onFailure(Call<Wishlist> call, Throwable t) {
                showToast("Lỗi mạng khi thêm sản phẩm yêu thích");
            }
        });
    }

    private void removeFromWishlist(int wishlistId) {
        String token = sessionManager.getAuthToken();

        wishlistService.removeFromWishlist("Bearer " + token, wishlistId)
                .enqueue(new Callback<ApiResponse>() {
                    @Override
                    public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                        if (response.isSuccessful()) {
                            // Trường hợp 204 No Content
                            if (response.code() == 204 ||
                                    (response.body() != null && response.body().isSuccess())) {
                                showToast("Đã xóa khỏi sản phẩm yêu thích");

                                // Đổi icon ngay lập tức
                                ivWishlist.setImageResource(R.drawable.baseline_favorite_border_24);
                                ivWishlist.setTag(false);
                                ivWishlist.setTag(R.id.tag_wishlist_id, null);
                            } else {
                                showToast("Xóa thất bại");
                            }
                        } else {
                            showToast("Lỗi: " + response.code());
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse> call, Throwable t) {
                        showToast("Lỗi kết nối: " + t.getMessage());
                    }
                });
    }

    //endregion


    //region 🔔 Utility
    private void showToast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
    //endregion
    // Add to Cart
    private void addToCart(int productId, int quantity) {
        String token = sessionManager.getAuthToken();
        int userId = sessionManager.getUserId();

        cartService.getCartItems("Bearer " + token, userId, "Active")
                .enqueue(new Callback<CartResponse>() {
                    @Override
                    public void onResponse(Call<CartResponse> call, Response<CartResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            List<Cart> carts = response.body().getItems();

                            if (carts != null && !carts.isEmpty()) {
                                Cart cart = carts.get(0);
                                int cartId = cart.getCartId();

                                // ✅ Tìm xem sản phẩm đã có trong giỏ chưa
                                CartItem existingItem = null;
                                if (cart.getCartItems() != null) {
                                    for (CartItem item : cart.getCartItems()) {
                                        if (item.getProductId() == productId) {
                                            existingItem = item;
                                            break;
                                        }
                                    }
                                }

                                if (existingItem != null) {
                                    // 🧮 Cập nhật lại quantity và price
                                    int newQuantity = existingItem.getQuantity() + quantity;
                                    int newPrice = (currentProduct != null ? currentProduct.getPrice() : 0) * newQuantity;
                                    existingItem.setQuantity(newQuantity);
                                    existingItem.setPrice(newPrice);

                                    updateCartItem(token, existingItem);
                                } else {
                                    // ➕ Thêm mới
                                    addCartItem(token, cartId, productId, quantity);
                                }

                            } else {
                                showToast("⚠ Không tìm thấy giỏ hàng đang hoạt động!");
                            }
                        } else {
                            showToast("⚠ Không thể lấy giỏ hàng hiện tại (" + response.code() + ")");
                        }
                    }

                    @Override
                    public void onFailure(Call<CartResponse> call, Throwable t) {
                        showToast("⚠ Lỗi khi lấy giỏ hàng: " + t.getMessage());
                    }
                });
    }

    private void updateCartItem(String token, CartItem existingItem) {
        cartService.updateCartItem("Bearer " + token, existingItem.getCartItemId(), existingItem)
                .enqueue(new Callback<CartItem>() {
                    @Override
                    public void onResponse(Call<CartItem> call, Response<CartItem> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            showToast("Đã cập nhật số lượng sản phẩm trong giỏ hàng!");
                        } else {
                            showToast("Cập nhật giỏ hàng thất bại (Mã lỗi " + response.code() + ")");
                        }
                    }

                    @Override
                    public void onFailure(Call<CartItem> call, Throwable t) {
                        showToast("⚠ Lỗi kết nối khi cập nhật giỏ hàng: " + t.getMessage());
                    }
                });
    }

    private void addCartItem(String token, int cartId, int productId, int quantity) {
        int price = currentProduct != null ? currentProduct.getPrice() : 0;

        CartItem cartItem = new CartItem();
        cartItem.setCartId(cartId);
        cartItem.setProductId(productId);
        cartItem.setQuantity(quantity);
        cartItem.setPrice(price * quantity);

        cartService.addCartItem("Bearer " + token, cartItem)
                .enqueue(new Callback<CartItem>() {
                    @Override
                    public void onResponse(Call<CartItem> call, Response<CartItem> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            CartItem addedItem = response.body();
                            int qty = addedItem.getQuantity();
                            double total = addedItem.getPrice();

                            showToast("Đã thêm " + qty + " sản phẩm " +
                                    " vào giỏ hàng!");
                        } else {
                            showToast("Thêm vào giỏ hàng thất bại (Mã lỗi " + response.code() + ")");
                        }
                    }

                    @Override
                    public void onFailure(Call<CartItem> call, Throwable t) {
                        showToast("⚠ Lỗi kết nối khi thêm sản phẩm: " + t.getMessage());
                    }
                });
    }

}
