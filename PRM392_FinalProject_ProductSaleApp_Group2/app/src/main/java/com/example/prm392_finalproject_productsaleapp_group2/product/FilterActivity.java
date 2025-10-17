package com.example.prm392_finalproject_productsaleapp_group2.product;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.prm392_finalproject_productsaleapp_group2.R;
import com.example.prm392_finalproject_productsaleapp_group2.auth.SessionManager;
import com.example.prm392_finalproject_productsaleapp_group2.models.Brand;
import com.example.prm392_finalproject_productsaleapp_group2.models.Category;
import com.example.prm392_finalproject_productsaleapp_group2.models.FilterResponse;
import com.example.prm392_finalproject_productsaleapp_group2.models.ProductFilterBM;
import com.example.prm392_finalproject_productsaleapp_group2.net.ApiConfig;
import com.example.prm392_finalproject_productsaleapp_group2.net.BrandApiClient;
import com.example.prm392_finalproject_productsaleapp_group2.net.CategoryApiClient;
import com.example.prm392_finalproject_productsaleapp_group2.net.ProductApiClient;
import com.example.prm392_finalproject_productsaleapp_group2.services.BrandApiService;
import com.example.prm392_finalproject_productsaleapp_group2.services.CategoryApiService;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FilterActivity extends AppCompatActivity {

    private ChipGroup chipGroupCategory, chipGroupBrand, chipGroupRating;
    private RadioGroup radioSortGroup;
    private Button btnApply, btnReset;
    private EditText etMinPrice, etMaxPrice;
    private ImageView btnClose;

    private List<Category> categoryList = new ArrayList<>();
    private List<Brand> brandList = new ArrayList<>();

    // Multi-select lists
    private List<Integer> selectedCategoryIds = new ArrayList<>();
    private List<Integer> selectedBrandIds = new ArrayList<>();
    private List<Integer> selectedRatings = new ArrayList<>();

    // Single value sort
    private String selectedSort = "";
    private ProductFilterBM incomingFilter;

    private int minPrice = -1;
    private int maxPrice = -1;

    private CategoryApiService categoryService;
    private BrandApiService brandService;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        getWindow().setStatusBarColor(Color.TRANSPARENT);
        setContentView(R.layout.activity_filter_product);

        setupWindowInsets();
        initViews();
        initServices();

        loadCategories();
        loadBrands();

        setupListeners();

    }
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
        chipGroupCategory = findViewById(R.id.chipGroupCategory);
        chipGroupBrand = findViewById(R.id.chipGroupBrand);
        chipGroupRating = findViewById(R.id.chipGroupRating);
        radioSortGroup = findViewById(R.id.radioSortGroup);
        etMinPrice = findViewById(R.id.etMinPrice);
        etMaxPrice = findViewById(R.id.etMaxPrice);
        btnApply = findViewById(R.id.btnApply);
        btnReset = findViewById(R.id.btnReset);
        btnClose = findViewById(R.id.btnClose);
        incomingFilter = (ProductFilterBM) getIntent().getSerializableExtra("productFilter");

    }

    private void initServices() {
        sessionManager = new SessionManager(this);
        categoryService = CategoryApiClient.getInstance().getApiService();
        brandService = BrandApiClient.getInstance().getApiService();
    }

    private void setupListeners() {
        btnClose.setOnClickListener(v -> finish());
        chipGroupRating.setSingleSelection(true);

        chipGroupRating.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (!checkedIds.isEmpty()) {
                Chip chip = findViewById(checkedIds.get(0));
                selectedRatings.clear();
                selectedRatings.add(Integer.parseInt(chip.getText().toString().replace("★","").trim()));
            } else {
                selectedRatings.clear();
            }
        });


        // SortBy single-select
        radioSortGroup.setOnCheckedChangeListener((group, checkedId) -> {
            RadioButton rb = findViewById(checkedId);
            if (rb != null && rb.getTag() != null) {
                selectedSort = rb.getTag().toString(); // "price", "popularity", "category"
            }
        });


        btnReset.setOnClickListener(v -> {
            chipGroupCategory.clearCheck();
            chipGroupBrand.clearCheck();
            chipGroupRating.clearCheck();
            radioSortGroup.clearCheck();
            etMinPrice.setText("");
            etMaxPrice.setText("");
            selectedCategoryIds.clear();
            selectedBrandIds.clear();
            selectedRatings.clear();
            selectedSort = "";
            minPrice = maxPrice = -1;
        });

        btnApply.setOnClickListener(v -> applyFilter());
    }

    private void loadCategories() {
        String token = sessionManager.getAuthToken();
        if (token == null || token.isEmpty()) return;

        Call<FilterResponse<Category>> call = categoryService.getCategories("Bearer " + token);
        call.enqueue(new Callback<FilterResponse<Category>>() {
            @Override
            public void onResponse(Call<FilterResponse<Category>> call, Response<FilterResponse<Category>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    categoryList = response.body().getItems();
                    populateCategoryChips();
                }
            }
            @Override
            public void onFailure(Call<FilterResponse<Category>> call, Throwable t) { }
        });
    }

    private void loadBrands() {
        String token = sessionManager.getAuthToken();
        if (token == null || token.isEmpty()) return;

        Call<FilterResponse<Brand>> call = brandService.getBrands(token,1,50);
        call.enqueue(new Callback<FilterResponse<Brand>>() {
            @Override
            public void onResponse(Call<FilterResponse<Brand>> call, Response<FilterResponse<Brand>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    brandList = response.body().getItems();
                    populateBrandChips();
                }
            }
            @Override
            public void onFailure(Call<FilterResponse<Brand>> call, Throwable t) { }
        });
    }

    private void populateCategoryChips() {
        chipGroupCategory.removeAllViews();
        for (Category cat : categoryList) {
            Chip chip = new Chip(this);
            chip.setText(cat.getCategoryName());
            chip.setTag(cat.getCategoryId());
            chip.setCheckable(true);
            chip.setChipBackgroundColor(ColorStateList.valueOf(Color.parseColor("#FF6B57")));
            chip.setTextColor(getResources().getColor(R.color.black));
            chipGroupCategory.addView(chip);
        }
        chipGroupCategory.setOnCheckedStateChangeListener((group, checkedIds) -> {
            selectedCategoryIds.clear();
            for (int id : checkedIds) {
                Chip chip = findViewById(id);
                selectedCategoryIds.add((Integer) chip.getTag());
            }
        });
    }


    private void populateBrandChips() {
        chipGroupBrand.removeAllViews();
        for (Brand brand : brandList) {
            Chip chip = new Chip(this);
            chip.setText(brand.getBrandName());
            chip.setTag(brand.getBrandId());
            chip.setChipBackgroundColor(ColorStateList.valueOf(Color.parseColor("#FF6B57")));
            chip.setTextColor(getResources().getColor(R.color.black));
            chip.setCheckable(true);
            chipGroupBrand.addView(chip);
        }
        chipGroupBrand.setOnCheckedStateChangeListener((group, checkedIds) -> {
            selectedBrandIds.clear();
            for (int id : checkedIds) {
                Chip chip = findViewById(id);
                selectedBrandIds.add((Integer) chip.getTag());
            }
        });
    }

    private void applyFilter() {
        try {
            String minStr = etMinPrice.getText().toString().trim();
            String maxStr = etMaxPrice.getText().toString().trim();
            minPrice = minStr.isEmpty() ? -1 : Integer.parseInt(minStr);
            maxPrice = maxStr.isEmpty() ? -1 : Integer.parseInt(maxStr);
        } catch (Exception e) {
            Toast.makeText(this, "Giá không hợp lệ", Toast.LENGTH_SHORT).show();
            return;
        }

        ProductFilterBM filter = new ProductFilterBM();
        filter.Search = (incomingFilter != null) ? incomingFilter.Search : null;
        filter.CategoryIds = selectedCategoryIds.isEmpty() ? null : new ArrayList<>(selectedCategoryIds);
        filter.BrandIds = selectedBrandIds.isEmpty() ? null : new ArrayList<>(selectedBrandIds);
        filter.AverageRating = selectedRatings.isEmpty() ? null : (double) selectedRatings.get(0); // lấy rating đầu tiên nếu muốn
        filter.MinPrice = minPrice != -1 ? (double) minPrice : null;
        filter.MaxPrice = maxPrice != -1 ? (double) maxPrice : null;
        filter.SortBy = selectedSort.isEmpty() ? null : selectedSort;

        Intent intent = new Intent(FilterActivity.this, ListProductActivity.class);
        intent.putExtra("productFilter", filter);
        startActivity(intent);
    }
}

