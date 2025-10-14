package com.example.prm392_finalproject_productsaleapp_group2.product;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.example.prm392_finalproject_productsaleapp_group2.R;
import com.example.prm392_finalproject_productsaleapp_group2.auth.SessionManager;
import com.example.prm392_finalproject_productsaleapp_group2.models.Category;
import com.example.prm392_finalproject_productsaleapp_group2.models.FilterResponse;
import com.example.prm392_finalproject_productsaleapp_group2.models.Product;
import com.example.prm392_finalproject_productsaleapp_group2.models.ProductFilterBM;
import com.example.prm392_finalproject_productsaleapp_group2.net.CategoryApiClient;
import com.example.prm392_finalproject_productsaleapp_group2.net.ProductApiClient;
import com.example.prm392_finalproject_productsaleapp_group2.services.CategoryApiService;
import com.example.prm392_finalproject_productsaleapp_group2.services.ProductApiService;
import com.example.prm392_finalproject_productsaleapp_group2.utils.NavigationBarUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeActivity extends AppCompatActivity {

    //region --- UI Components ---
    private ViewPager2 bannerViewPager;
    private ImageView btnFilter;
    private RecyclerView rvCategory;
    private RecyclerView rvProduct;
    private EditText etSearch; // thêm dòng này trên cùng với các biến UI

    //endregion

    //region --- Banner ---
    private Handler handler;
    private Runnable bannerRunnable;
    private int currentPageBanner = 0;
    private final List<Integer> bannerImages = Arrays.asList(
            R.drawable.sample_banner,
            R.drawable.sample_banner2,
            R.drawable.sample_banner3,
            R.drawable.sample_banner4
    );
    //endregion

    //region --- Product Pagination ---
    private int currentPageProduct = 1;
    private final int PAGE_SIZE = 10;
    private boolean isLoading = false;
    private boolean isLastPage = false;
    //endregion

    //region --- Adapters & Services ---
    private ProductAdapter adapterProduct;
    private CategoryAdapter adapterCategory;
    private ProductApiService productService;
    private CategoryApiService categoryService;
    private SessionManager sessionManager;
    //endregion

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        // Transparent status bar
        getWindow().setStatusBarColor(android.graphics.Color.TRANSPARENT);
        setContentView(R.layout.activity_home);

        setupWindowInsets();
        initViews();
        initServices();
        setupBanner();
        setupCategories();
        setupProducts();
        setupFilterButton();
        setupNavigationBar();
    }

    //region --- Initialization ---
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
        bannerViewPager = findViewById(R.id.bannerViewPager);
        btnFilter = findViewById(R.id.btnFilter);
        rvCategory = findViewById(R.id.rvCategory);
        rvProduct = findViewById(R.id.rvFeaturedProducts);
        etSearch = findViewById(R.id.etSearch); // ánh xạ EditText
        // Xử lý khi người dùng nhấn phím "Search" trên bàn phím
        etSearch.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                String keyword = etSearch.getText().toString().trim();
                if (!keyword.isEmpty()) {
                    openListProductBySearch(keyword);
                } else {
                    Toast.makeText(this, "Vui lòng nhập từ khóa tìm kiếm", Toast.LENGTH_SHORT).show();
                }
                return true;
            }
            return false;
        });
    }

    private void initServices() {
        sessionManager = new SessionManager(this);
        productService = ProductApiClient.getInstance().getApiService();
        categoryService = CategoryApiClient.getInstance().getApiService();
    }
    //endregion

    //region --- Banner setup ---
    private void setupBanner() {
        BannerAdapter adapterBanner = new BannerAdapter(bannerImages);
        bannerViewPager.setAdapter(adapterBanner);

        handler = new Handler(Looper.getMainLooper());
        bannerRunnable = () -> {
            currentPageBanner = (currentPageBanner + 1) % bannerImages.size();
            bannerViewPager.setCurrentItem(currentPageBanner, true);
            handler.postDelayed(bannerRunnable, 3000);
        };

        handler.postDelayed(bannerRunnable, 3000);

        bannerViewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                currentPageBanner = position;
                handler.removeCallbacks(bannerRunnable);
                handler.postDelayed(bannerRunnable, 3000);
            }
        });
    }
    //endregion

    //region --- Category setup ---
    private void setupCategories() {
        List<Category> categoryList = new ArrayList<>();
        adapterCategory = new CategoryAdapter(categoryList);
        rvCategory.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        rvCategory.setAdapter(adapterCategory);

        loadCategories(categoryList);
    }

    private void loadCategories(List<Category> categoryList) {
        String token = sessionManager.getAuthToken();
        if (token == null || token.isEmpty()) {
            Toast.makeText(this, "Không tìm thấy access token", Toast.LENGTH_SHORT).show();
            return;
        }
        String accessToken = "Bearer " + token;

        categoryService.getCategories(accessToken).enqueue(new Callback<FilterResponse<Category>>() {
            @Override
            public void onResponse(Call<FilterResponse<Category>> call, Response<FilterResponse<Category>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // ✅ Lấy danh sách categories từ response
                    List<Category> categories = response.body().getItems();

                    categoryList.clear();
                    categoryList.addAll(categories);
                    adapterCategory.notifyDataSetChanged();
                } else {
                    Toast.makeText(HomeActivity.this, "Không tải được danh mục", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<FilterResponse<Category>> call, Throwable t) {
                t.printStackTrace();
                Toast.makeText(HomeActivity.this, "Lỗi kết nối server", Toast.LENGTH_SHORT).show();
            }
        });
    }

    //endregion

    //region --- Product setup ---
    private void setupProducts() {
        List<Product> productList = new ArrayList<>();
        adapterProduct = new ProductAdapter(productList);
        adapterProduct.initSession(this);

        rvProduct.setLayoutManager(new GridLayoutManager(this, 2));
        rvProduct.setAdapter(adapterProduct);

        loadProducts(productList, currentPageProduct);
        setupProductScrollListener(productList);
    }

    private void setupProductScrollListener(List<Product> productList) {
        rvProduct.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                GridLayoutManager layoutManager = (GridLayoutManager) recyclerView.getLayoutManager();
                if (layoutManager == null) return;

                int visibleItemCount = layoutManager.getChildCount();
                int totalItemCount = layoutManager.getItemCount();
                int firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition();

                if (!isLoading && !isLastPage) {
                    if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount - 2
                            && firstVisibleItemPosition >= 0) {
                        currentPageProduct++;
                        loadProducts(productList, currentPageProduct);
                    }
                }
            }
        });
    }
    private void loadProducts(List<Product> productList, int pageNumber) {
        if (isLoading || isLastPage) return;
        isLoading = true;

        String token = sessionManager.getAuthToken();
        if (token == null || token.isEmpty()) {
            Toast.makeText(this, "Không tìm thấy access token", Toast.LENGTH_SHORT).show();
            isLoading = false;
            return;
        }
        String accessToken = "Bearer " + token;

        // Gọi API
        Call<FilterResponse<Product>> call = productService.getProducts(accessToken, pageNumber, PAGE_SIZE);

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
                    adapterProduct.updateData(productList);
                } else {
                    Toast.makeText(HomeActivity.this, "Không tải được sản phẩm (" + response.code() + ")", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<FilterResponse<Product>> call, Throwable t) {
                isLoading = false;
                t.printStackTrace();
                Toast.makeText(HomeActivity.this, "Lỗi kết nối server", Toast.LENGTH_SHORT).show();
            }
        });
    }

    //endregion
    //region --- Search ---

    private void openListProductBySearch(String keyword) {
        Intent intent = new Intent(this, ListProductActivity.class);

        // Tạo bộ lọc cơ bản (chỉ có từ khóa)
        ProductFilterBM filter = new ProductFilterBM();
        filter.Search = keyword; // Thêm thuộc tính Search trong model ProductFilterBM nếu chưa có

        intent.putExtra("productFilter", filter);
        startActivity(intent);
    }

    //endregion

    //region --- Filter & Navigation ---
    private void setupFilterButton() {
        btnFilter.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, FilterActivity.class);

            // Nếu user đã nhập keyword trong ô tìm kiếm
            String keyword = etSearch.getText().toString().trim();
            if (!keyword.isEmpty()) {
                ProductFilterBM currentFilter = new ProductFilterBM();
                currentFilter.Search = keyword;
                intent.putExtra("productFilter", currentFilter);
            }

            startActivity(intent);
        });
    }

    private void setupNavigationBar() {
        NavigationBarUtil.setupNavigationBar(this);
        NavigationBarUtil.setActiveNavigationButton(this, "home");
    }
    //endregion

    //region --- Lifecycle ---
    @Override
    protected void onPause() {
        super.onPause();
        handler.removeCallbacks(bannerRunnable);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (adapterProduct != null) {
            adapterProduct.loadWishlistForUser();
        }
        handler.postDelayed(bannerRunnable, 3000);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        NavigationBarUtil.finishActivityWithoutAnimation(this);
    }
    //endregion
}
