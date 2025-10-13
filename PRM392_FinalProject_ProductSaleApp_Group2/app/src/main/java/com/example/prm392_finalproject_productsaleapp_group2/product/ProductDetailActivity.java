package com.example.prm392_finalproject_productsaleapp_group2.product;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.StyleSpan;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.example.prm392_finalproject_productsaleapp_group2.R;
import com.example.prm392_finalproject_productsaleapp_group2.net.ApiConfig;
import com.example.prm392_finalproject_productsaleapp_group2.auth.SessionManager;
import com.example.prm392_finalproject_productsaleapp_group2.utils.NavigationBarUtil;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ProductDetailActivity extends AppCompatActivity {

    private ImageView imgProduct;
    private TextView tvName, tvPrice, tvFullDesc, tvSpecs;
    private ViewPager2 viewPager;
    private ImageView btnBack;
    private ImageView ivWishlist;
    private SessionManager sessionManager;
    private int userId;
    private int productId;
    private TabLayout tabDots;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        // Set status bar transparent to let gradient show through
        getWindow().setStatusBarColor(android.graphics.Color.TRANSPARENT);

        setContentView(R.layout.activity_product_detail);

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
                        header.getPaddingBottom());
            }

            return WindowInsetsCompat.CONSUMED;
        });
        // Setup navigation bar
        NavigationBarUtil.setupNavigationBar(this);
        NavigationBarUtil.setActiveNavigationButton(this, "home");
        // Đóng trang sản phẩm
        btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());
        viewPager = findViewById(R.id.viewPagerImages);
        tvName = findViewById(R.id.tvProductNameDetail);
        tvPrice = findViewById(R.id.tvProductPriceDetail);
        tvFullDesc = findViewById(R.id.tvProductDescriptionDetail);
        tvSpecs = findViewById(R.id.tvProductSpecsDetail);
        ivWishlist = findViewById(R.id.ivWishlist);
        tabDots = findViewById(R.id.tabDots);

        sessionManager = new SessionManager(this);
        userId = sessionManager.getUserId();
        // Tăng giảm số lượng
        TextView btnDecrease = findViewById(R.id.btnDecrease);
        TextView btnIncrease = findViewById(R.id.btnIncrease);
        EditText etQuantity = findViewById(R.id.etQuantity);

        btnIncrease.setOnClickListener(v -> {
            String value = etQuantity.getText().toString();
            int qty = 1;
            try {
                qty = Integer.parseInt(value);
            } catch (NumberFormatException e) {
                qty = 1;
            }
            qty++;
            etQuantity.setText(String.valueOf(qty));
        });

        btnDecrease.setOnClickListener(v -> {
            String value = etQuantity.getText().toString();
            int qty = 1;
            try {
                qty = Integer.parseInt(value);
            } catch (NumberFormatException e) {
                qty = 1;
            }
            if (qty > 1)
                qty--; // không cho số lượng < 1
            etQuantity.setText(String.valueOf(qty));
        });

        productId = getIntent().getIntExtra("productId", -1);
        if (productId != -1) {
            loadProductDetail(productId);
        }

        // Initial wishlist state check and toggle handler
        if (userId > 0 && productId > 0) {
            checkWishlistForProduct(productId);
        }

        ivWishlist.setOnClickListener(v -> {
            if (userId <= 0 || productId <= 0)
                return;
            Object tag = ivWishlist.getTag();
            boolean currentFav = tag instanceof Boolean && (Boolean) tag;
            if (currentFav) {
                Object widTag = ivWishlist.getTag(R.id.tag_wishlist_id);
                int wishlistId = (widTag instanceof Integer) ? (Integer) widTag : -1;
                if (wishlistId > 0)
                    removeFromWishlist(wishlistId);
            } else {
                addToWishlist(userId, productId);
            }
        });
    }

    private void loadProductDetail(int productId) {
        new AsyncTask<Integer, Void, JSONObject>() {
            @Override
            protected JSONObject doInBackground(Integer... ids) {
                try {
                    URL url = new URL(ApiConfig.endpoint("/api/Products/" + ids[0]));
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("GET");
                    conn.setRequestProperty("Accept", "application/json");

                    int code = conn.getResponseCode();
                    if (code >= 200 && code < 300) {
                        BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                        StringBuilder sb = new StringBuilder();
                        String line;
                        while ((line = br.readLine()) != null)
                            sb.append(line);
                        br.close();
                        return new JSONObject(sb.toString());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(JSONObject product) {
                if (product != null) {
                    try {
                        tvName.setText(product.getString("productName"));
                        long price = product.getLong("price");
                        tvPrice.setText(NumberFormat.getInstance(new Locale("vi", "VN")).format(price) + " ₫");
                        SpannableStringBuilder descBuilder = new SpannableStringBuilder();
                        // Thương hiệu
                        if (product.has("brand") && !product.isNull("brand")) {
                            JSONObject brandObj = product.getJSONObject("brand");
                            String brandName = brandObj.optString("brandName", "Không rõ");
                            TextView tvBrandNameDetail = findViewById(R.id.tvBrandNameDetail);
                            tvBrandNameDetail.setText("Thương hiệu: " + brandName);
                        }

                        // Đánh giá trung bình
                        if (product.has("averageRating")) {
                            double avg = product.optDouble("averageRating", 0.0);
                            TextView tvAverageRatingDetail = findViewById(R.id.tvAverageRatingDetail);
                            tvAverageRatingDetail.setText("⭐ " + avg);
                        }

                        // Độ phổ biến
                        if (product.has("popularity")) {
                            int pop = product.optInt("popularity", 0);
                            TextView tvPopularityDetail = findViewById(R.id.tvPopularityDetail);
                            tvPopularityDetail.setText("🔥 " + pop);
                        }

                        // Số lượng đánh giá
                        if (product.has("reviewCount")) {
                            int reviewCount = product.optInt("reviewCount", 0);
                            TextView tvReviewCountDetail = findViewById(R.id.tvReviewCountDetail);
                            tvReviewCountDetail.setText("(" + reviewCount + ")");
                        }
                        String labelDesc = "Mô tả: ";
                        String descText = product.getString("fullDescription");
                        descBuilder.append(labelDesc);
                        descBuilder.setSpan(new StyleSpan(Typeface.BOLD), 0, labelDesc.length(),
                                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                        descBuilder.append(descText);

                        tvFullDesc.setText(descBuilder);

                        SpannableStringBuilder specsBuilder = new SpannableStringBuilder();
                        String labelSpecs = "Thông số: ";
                        String specsText = product.getString("technicalSpecifications");
                        specsBuilder.append(labelSpecs);
                        specsBuilder.setSpan(new StyleSpan(Typeface.BOLD), 0, labelSpecs.length(),
                                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                        specsBuilder.append(specsText);

                        tvSpecs.setText(specsBuilder);
                        // BƯỚC 4: LẤY DANH SÁCH ẢNH SẢN PHẨM
                        // ---------------------------
                        JSONArray images = product.getJSONArray("productImages");
                        List<String> imageUrls = new ArrayList<>();
                        for (int i = 0; i < images.length(); i++) {
                            JSONObject imgObj = images.getJSONObject(i);
                            imageUrls.add(imgObj.getString("imageUrl"));
                        }

                        // Gắn adapter cho ViewPager2
                        ProductDetailImageAdapter adapter = new ProductDetailImageAdapter(ProductDetailActivity.this,
                                imageUrls);
                        viewPager.setAdapter(adapter);

                        // Attach dots indicator and set dot drawable as icon
                        new TabLayoutMediator(tabDots, viewPager, (tab, position) -> {
                            tab.setIcon(R.drawable.tab_indicator_dot);
                        }).attach();

                        // ---------------------------
                        // 🕒 BƯỚC 5: TỰ ĐỘNG CHUYỂN ẢNH MỖI 3 GIÂY
                        // ---------------------------
                        new Handler().postDelayed(new Runnable() {
                            int currentPage = 0;

                            @Override
                            public void run() {
                                if (viewPager.getAdapter() != null && viewPager.getAdapter().getItemCount() > 0) {
                                    currentPage = (currentPage + 1) % viewPager.getAdapter().getItemCount();
                                    viewPager.setCurrentItem(currentPage, true);
                                    viewPager.postDelayed(this, 3000); // 3 giây đổi 1 lần
                                }
                            }
                        }, 3000);

                        // Hiện review
                        JSONArray reviews = product.getJSONArray("productReviews");
                        List<JSONObject> reviewList = new ArrayList<>();
                        for (int i = 0; i < reviews.length(); i++) {
                            reviewList.add(reviews.getJSONObject(i));
                        }

                        RecyclerView rvReviews = findViewById(R.id.rvReviews);
                        rvReviews.setLayoutManager(new LinearLayoutManager(ProductDetailActivity.this));
                        rvReviews.setAdapter(new ProductReviewAdapter(ProductDetailActivity.this, reviewList));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        }.execute(productId);
    }

    private void checkWishlistForProduct(int pid) {
        new Thread(() -> {
            try {
                URL url = new URL(ApiConfig
                        .endpoint("/api/Wishlists/filter?UserId=" + userId + "&ProductId=" + pid + "&pageSize=1"));
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                if (conn.getResponseCode() == 200) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null)
                        sb.append(line);
                    JSONObject root = new JSONObject(sb.toString());
                    JSONArray items = root.optJSONArray("items");
                    if (items != null && items.length() > 0) {
                        JSONObject item = items.getJSONObject(0);
                        int wid = item.optInt("wishlistId", -1);
                        runOnUiThread(() -> {
                            ivWishlist.setImageResource(R.drawable.baseline_favorite_24);
                            ivWishlist.setTag(true);
                            if (wid > 0)
                                ivWishlist.setTag(R.id.tag_wishlist_id, wid);
                        });
                    } else {
                        runOnUiThread(() -> {
                            ivWishlist.setImageResource(R.drawable.baseline_favorite_border_24);
                            ivWishlist.setTag(false);
                            ivWishlist.setTag(R.id.tag_wishlist_id, null);
                        });
                    }
                }
            } catch (Exception ignored) {
            }
        }).start();
    }

    private void addToWishlist(int uid, int pid) {
        new Thread(() -> {
            try {
                URL url = new URL(ApiConfig.endpoint("/api/Wishlists"));
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                conn.setDoOutput(true);

                JSONObject body = new JSONObject();
                body.put("userId", uid);
                body.put("productId", pid);

                try (java.io.OutputStream os = conn.getOutputStream()) {
                    os.write(body.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8));
                }

                if (conn.getResponseCode() == 201) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null)
                        sb.append(line);
                    JSONObject resp = new JSONObject(sb.toString());
                    int wid = resp.optInt("wishlistId", -1);
                    runOnUiThread(() -> {
                        ivWishlist.setImageResource(R.drawable.baseline_favorite_24);
                        ivWishlist.setTag(true);
                        if (wid > 0)
                            ivWishlist.setTag(R.id.tag_wishlist_id, wid);
                    });
                }
            } catch (Exception ignored) {
            }
        }).start();
    }

    private void removeFromWishlist(int wishlistId) {
        new Thread(() -> {
            try {
                URL url = new URL(ApiConfig.endpoint("/api/Wishlists/" + wishlistId));
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("DELETE");
                if (conn.getResponseCode() >= 200 && conn.getResponseCode() < 300) {
                    runOnUiThread(() -> {
                        ivWishlist.setImageResource(R.drawable.baseline_favorite_border_24);
                        ivWishlist.setTag(false);
                        ivWishlist.setTag(R.id.tag_wishlist_id, null);
                    });
                }
            } catch (Exception ignored) {
            }
        }).start();
    }

    public static class LoadImageTask extends AsyncTask<String, Void, Bitmap> {
        private final ImageView imageView;

        LoadImageTask(ImageView imageView) {
            this.imageView = imageView;
        }

        @Override
        protected Bitmap doInBackground(String... urls) {
            try (InputStream in = new URL(urls[0]).openStream()) {
                return BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                return null;
            }
        }

        @Override
        protected void onPostExecute(Bitmap result) {
            if (result != null)
                imageView.setImageBitmap(result);
        }
    }
}