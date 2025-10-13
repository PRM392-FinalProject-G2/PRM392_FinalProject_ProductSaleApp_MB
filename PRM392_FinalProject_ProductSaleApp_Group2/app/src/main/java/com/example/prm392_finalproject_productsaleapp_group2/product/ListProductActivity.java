package com.example.prm392_finalproject_productsaleapp_group2.product;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.prm392_finalproject_productsaleapp_group2.R;
import com.example.prm392_finalproject_productsaleapp_group2.net.ApiConfig;
import com.example.prm392_finalproject_productsaleapp_group2.utils.NavigationBarUtil;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class ListProductActivity extends AppCompatActivity {

    private int categoryId;
    private String categoryName;
    private RecyclerView rvProducts;
    private ProductAdapter productAdapter;
    private JSONArray productArray = new JSONArray();

    private int currentPageProduct = 1;
    private boolean isLoading = false;
    private boolean isLastPage = false;
    private final int PAGE_SIZE = 10;
    private ImageView btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        // Set status bar transparent to let gradient show through
        getWindow().setStatusBarColor(android.graphics.Color.TRANSPARENT);

        setContentView(R.layout.activity_list_product);

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

        // Đóng trang sản phẩm
        btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());
        // Gọi API danh sách sản phẩm
        rvProducts = findViewById(R.id.rvProductList);
        GridLayoutManager layoutManager = new GridLayoutManager(this, 2);
        rvProducts.setLayoutManager(layoutManager);

        productAdapter = new ProductAdapter(productArray);
        rvProducts.setAdapter(productAdapter);
        // Initialize session for adapter and preload wishlist
        productAdapter.initSession(this);

        // Nhận dữ liệu từ Intent
        categoryId = getIntent().getIntExtra("categoryId", -1);
        categoryName = getIntent().getStringExtra("categoryName");

        if (categoryId == -1) {
            Toast.makeText(this, "Không có danh mục hợp lệ", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Load trang đầu tiên
        loadProductsByCategory(categoryId, currentPageProduct);

        // Thêm sự kiện cuộn xuống để tải thêm
        rvProducts.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                int visibleItemCount = layoutManager.getChildCount();
                int totalItemCount = layoutManager.getItemCount();
                int firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition();

                if (!isLoading && !isLastPage) {
                    // Khi cuộn gần cuối danh sách -> tải thêm trang mới
                    if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount - 2
                            && firstVisibleItemPosition >= 0) {
                        currentPageProduct++;
                        loadProductsByCategory(categoryId, currentPageProduct);
                    }
                }
            }
        });
        // Setup navigation bar
        NavigationBarUtil.setupNavigationBar(this);
        NavigationBarUtil.setActiveNavigationButton(this, "home");
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (productAdapter != null) {
            productAdapter.loadWishlistForUser();
        }
    }

    private void loadProductsByCategory(int categoryId, int pageNumber) {
        if (isLoading || isLastPage)
            return;
        isLoading = true;

        new AsyncTask<Void, Void, Boolean>() {
            JSONArray newItems;

            @Override
            protected Boolean doInBackground(Void... voids) {
                try {
                    String urlStr = ApiConfig.endpoint(
                            "/api/Products/filter?CategoryId=" + categoryId +
                                    "&pageNumber=" + pageNumber +
                                    "&pageSize=" + PAGE_SIZE);
                    Log.d("LoadPage", "Đang tải trang: " + pageNumber + " -> " + urlStr);

                    URL url = new URL(urlStr);
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

                        JSONObject root = new JSONObject(sb.toString());
                        newItems = root.optJSONArray("items");

                        if (newItems == null || newItems.length() == 0) {
                            isLastPage = true;
                        }
                        return true;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return false;
            }

            @Override
            protected void onPostExecute(Boolean success) {
                isLoading = false;

                if (success && newItems != null && newItems.length() > 0) {
                    // Gộp thêm vào mảng hiện có
                    for (int i = 0; i < newItems.length(); i++) {
                        productArray.put(newItems.optJSONObject(i));
                    }
                    productAdapter.updateData(productArray);
                    // preload wishlist mapping for current user
                    productAdapter.loadWishlistForUser();

                }
                // else if (isLastPage) {
                // Toast.makeText(ListProductActivity.this, "Đã tải hết sản phẩm",
                // Toast.LENGTH_SHORT).show();
                //
                // } else {
                // Toast.makeText(ListProductActivity.this, "Không tải được sản phẩm",
                // Toast.LENGTH_SHORT).show();
                // }
            }
        }.execute();
    }
}
