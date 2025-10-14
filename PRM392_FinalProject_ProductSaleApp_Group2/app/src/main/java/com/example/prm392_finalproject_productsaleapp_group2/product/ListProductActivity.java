package com.example.prm392_finalproject_productsaleapp_group2.product;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
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
import com.example.prm392_finalproject_productsaleapp_group2.auth.SessionManager;
import com.example.prm392_finalproject_productsaleapp_group2.models.FilterResponse;
import com.example.prm392_finalproject_productsaleapp_group2.models.Product;
import com.example.prm392_finalproject_productsaleapp_group2.models.ProductFilterBM;
import com.example.prm392_finalproject_productsaleapp_group2.net.ProductApiClient;
import com.example.prm392_finalproject_productsaleapp_group2.services.ProductApiService;
import com.example.prm392_finalproject_productsaleapp_group2.utils.NavigationBarUtil;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;

public class ListProductActivity extends AppCompatActivity {

    // ==============================
    // 🔹 Fields
    // ==============================

    private RecyclerView rvProducts;
    private ProductAdapter productAdapter;
    private List<Product> productList = new ArrayList<>();

    private ProductApiService productService;
    private SessionManager sessionManager;

    private int currentPageProduct = 1;
    private boolean isLoading = false;
    private boolean isLastPage = false;
    private static final int PAGE_SIZE = 10;
    private ProductFilterBM productFilter;

    private ImageView btnBack;

    // ==============================
    // 🔹 Lifecycle Methods
    // ==============================
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        getWindow().setStatusBarColor(android.graphics.Color.TRANSPARENT);
        setContentView(R.layout.activity_list_product);
        setupWindowInsets();
        initViews();
        initRecyclerView();
        initSessionAndService();
        handleIntentData();
        setupScrollListener();
        setupNavigationBar();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (productAdapter != null) {
            productAdapter.loadWishlistForUser();
        }
    }

    // ==============================
    // 🔹 Initialization Methods
    // ==============================
    private void setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, 0, systemBars.right, 0);

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
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        rvProducts = findViewById(R.id.rvProductList);
    }

    private void initRecyclerView() {
        GridLayoutManager layoutManager = new GridLayoutManager(this, 2);
        rvProducts.setLayoutManager(layoutManager);

        productAdapter = new ProductAdapter(productList);
        rvProducts.setAdapter(productAdapter);
        productAdapter.initSession(this);
    }

    private void initSessionAndService() {
        sessionManager = new SessionManager(this);
        productService = ProductApiClient.getInstance().getApiService();
    }

    private void handleIntentData() {

        productFilter = (ProductFilterBM) getIntent().getSerializableExtra("productFilter");
        if (productFilter == null) {
            Log.e("ListProductActivity", "Filter is null!");
            Toast.makeText(this, "Không có bộ lọc hợp lệ", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        currentPageProduct = 1;
        isLastPage = false;
        productList.clear();
        loadProducts(productFilter, currentPageProduct);

    }

    private void setupScrollListener() {
        GridLayoutManager layoutManager = (GridLayoutManager) rvProducts.getLayoutManager();

        rvProducts.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                if (layoutManager == null) return;

                int visibleItemCount = layoutManager.getChildCount();
                int totalItemCount = layoutManager.getItemCount();
                int firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition();

                if (!isLoading && !isLastPage) {
                    boolean isNearEnd = (visibleItemCount + firstVisibleItemPosition) >= totalItemCount - 2
                            && firstVisibleItemPosition >= 0;
                    if (isNearEnd) {
                        currentPageProduct++;
                        loadProducts(productFilter, currentPageProduct);
                    }
                }
            }
        });
    }

    private void setupNavigationBar() {
        NavigationBarUtil.setupNavigationBar(this);
        NavigationBarUtil.setActiveNavigationButton(this, "home");
    }

    // ==============================
    // 🔹 Data Loading
    // ==============================
    private void loadProducts(ProductFilterBM filter, int pageNumber) {
        if (isLoading || isLastPage) return;
        isLoading = true;

        String token = sessionManager.getAuthToken();
        if (token == null || token.isEmpty()) {
            Toast.makeText(this, "Không tìm thấy access token", Toast.LENGTH_SHORT).show();
            isLoading = false;
            return;
        }

        Call<FilterResponse<Product>> call = productService.getProducts(
                "Bearer " + token,
                filter.CategoryIds,
                filter.Search,
                filter.BrandIds,
                filter.MinPrice,
                filter.MaxPrice,
                filter.AverageRating,
                filter.SortBy,
                pageNumber,
                PAGE_SIZE
        );

        call.enqueue(new retrofit2.Callback<FilterResponse<Product>>() {
            @Override
            public void onResponse(Call<FilterResponse<Product>> call, retrofit2.Response<FilterResponse<Product>> response) {
                isLoading = false;

                if (response.isSuccessful() && response.body() != null) {
                    List<Product> newItems = response.body().getItems();

                    if (newItems == null || newItems.isEmpty()) {
                        isLastPage = true;
                        return;
                    }

                    productList.addAll(newItems);
                    productAdapter.updateData(productList);
                    productAdapter.loadWishlistForUser();
                } else {
                    Toast.makeText(ListProductActivity.this,
                            "Không tải được sản phẩm (" + response.code() + ")", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<FilterResponse<Product>> call, Throwable t) {
                isLoading = false;
                t.printStackTrace();
                Toast.makeText(ListProductActivity.this, "Lỗi kết nối server", Toast.LENGTH_SHORT).show();
            }
        });
    }

}
