package com.example.prm392_finalproject_productsaleapp_group2.product;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.prm392_finalproject_productsaleapp_group2.R;
import com.example.prm392_finalproject_productsaleapp_group2.net.ApiConfig;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class FilterActivity extends AppCompatActivity {

    private ChipGroup chipGroupCategory, chipGroupBrand, chipGroupRating;
    private RadioGroup radioSortGroup;
    private Button btnApply, btnReset;
    private EditText etMinPrice, etMaxPrice;
    private ImageView btnClose;

    private List<JSONObject> categoryList = new ArrayList<>();
    private List<JSONObject> brandList = new ArrayList<>();

    private int selectedCategoryId = -1;
    private int selectedBrandId = -1;
    private int selectedRating = -1;
    private String selectedSort = "";
    private int minPrice = -1;
    private int maxPrice = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filter_product);

        // 🔹 Ánh xạ view
        chipGroupCategory = findViewById(R.id.chipGroupCategory);
        chipGroupBrand = findViewById(R.id.chipGroupBrand);
        chipGroupRating = findViewById(R.id.chipGroupRating);
        radioSortGroup = findViewById(R.id.radioSortGroup);
        etMinPrice = findViewById(R.id.etMinPrice);
        etMaxPrice = findViewById(R.id.etMaxPrice);
        btnApply = findViewById(R.id.btnApply);
        btnReset = findViewById(R.id.btnReset);
        btnClose = findViewById(R.id.btnClose);

        // 🔹 Gọi API lấy category & brand
        loadCategories();
        loadBrands();

        // 🔹 Đóng màn filter
        btnClose.setOnClickListener(v -> finish());

        // 🔹 Rating
        chipGroupRating.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (!checkedIds.isEmpty()) {
                Chip chip = findViewById(checkedIds.get(0));
                selectedRating = Integer.parseInt(chip.getText().toString().replace("★", "").trim());
            } else selectedRating = -1;
        });

        // 🔹 Sort
        radioSortGroup.setOnCheckedChangeListener((group, checkedId) -> {
            RadioButton rb = findViewById(checkedId);
            if (rb != null) selectedSort = rb.getText().toString();
        });

        // 🔹 Nút Đặt lại
        btnReset.setOnClickListener(v -> {
            chipGroupCategory.clearCheck();
            chipGroupBrand.clearCheck();
            chipGroupRating.clearCheck();
            radioSortGroup.clearCheck();
            etMinPrice.setText("");
            etMaxPrice.setText("");
            selectedCategoryId = -1;
            selectedBrandId = -1;
            selectedRating = -1;
            selectedSort = "";
            minPrice = maxPrice = -1;
        });

        // 🔹 Nút Áp dụng
        btnApply.setOnClickListener(v -> {
            try {
                String minStr = etMinPrice.getText().toString().trim();
                String maxStr = etMaxPrice.getText().toString().trim();
                minPrice = minStr.isEmpty() ? -1 : Integer.parseInt(minStr);
                maxPrice = maxStr.isEmpty() ? -1 : Integer.parseInt(maxStr);
            } catch (Exception e) {
                Toast.makeText(this, "Giá không hợp lệ", Toast.LENGTH_SHORT).show();
                return;
            }

            String result = String.format(
                    "Category: %d | Brand: %d | Rating: %d★ | Sort: %s | Price: %d–%d",
                    selectedCategoryId, selectedBrandId, selectedRating,
                    selectedSort, minPrice, maxPrice
            );
            Toast.makeText(this, "Bộ lọc: " + result, Toast.LENGTH_LONG).show();

            // TODO: Truyền dữ liệu filter qua Intent hoặc gọi API lọc sản phẩm ở đây
        });
    }

    // ====================== GỌI API CATEGORY ======================
    private void loadCategories() {
        new AsyncTask<Void, Void, Boolean>() {
            @Override
            protected Boolean doInBackground(Void... voids) {
                try {
                    URL url = new URL(ApiConfig.endpoint("/api/Categories/filter?PageSize=100"));
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("GET");
                    conn.setRequestProperty("Accept", "application/json");

                    int code = conn.getResponseCode();
                    if (code >= 200 && code < 300) {
                        BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                        StringBuilder sb = new StringBuilder();
                        String line;
                        while ((line = br.readLine()) != null) sb.append(line);
                        br.close();

                        JSONObject root = new JSONObject(sb.toString());
                        JSONArray items = root.getJSONArray("items");

                        categoryList.clear();
                        for (int i = 0; i < items.length(); i++) {
                            categoryList.add(items.getJSONObject(i));
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
                if (success) populateCategoryChips();
                else Toast.makeText(FilterActivity.this, "Không tải được danh mục", Toast.LENGTH_SHORT).show();
            }
        }.execute();
    }

    // ====================== GỌI API BRAND ======================
    private void loadBrands() {
        new AsyncTask<Void, Void, Boolean>() {
            @Override
            protected Boolean doInBackground(Void... voids) {
                try {
                    URL url = new URL(ApiConfig.endpoint("/api/Brands/filter?PageSize=100"));
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("GET");
                    conn.setRequestProperty("Accept", "application/json");

                    int code = conn.getResponseCode();
                    if (code >= 200 && code < 300) {
                        BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                        StringBuilder sb = new StringBuilder();
                        String line;
                        while ((line = br.readLine()) != null) sb.append(line);
                        br.close();

                        JSONObject root = new JSONObject(sb.toString());
                        JSONArray items = root.getJSONArray("items");

                        brandList.clear();
                        for (int i = 0; i < items.length(); i++) {
                            brandList.add(items.getJSONObject(i));
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
                if (success) populateBrandChips();
                else Toast.makeText(FilterActivity.this, "Không tải được thương hiệu", Toast.LENGTH_SHORT).show();
            }
        }.execute();
    }

    // ====================== TẠO CHIP ======================
    private void populateCategoryChips() {
        chipGroupCategory.removeAllViews();
        for (JSONObject cat : categoryList) {
            try {
                Chip chip = new Chip(this);
                chip.setText(cat.getString("categoryName"));
                chip.setTag(cat.getInt("categoryId"));
                chip.setCheckable(true);
                chip.setChipBackgroundColor(ColorStateList.valueOf(Color.parseColor("#FF6B57")));
                chip.setTextColor(getResources().getColor(R.color.black));
                chipGroupCategory.addView(chip);
            } catch (Exception ignored) {}
        }

        chipGroupCategory.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (!checkedIds.isEmpty()) {
                Chip chip = findViewById(checkedIds.get(0));
                selectedCategoryId = (int) chip.getTag();
            } else selectedCategoryId = -1;
        });
    }

    private void populateBrandChips() {
        chipGroupBrand.removeAllViews();
        for (JSONObject brand : brandList) {
            try {
                Chip chip = new Chip(this);
                chip.setText(brand.getString("brandName"));
                chip.setTag(brand.getInt("brandId"));
                chip.setCheckable(true);
                chip.setChipBackgroundColor(ColorStateList.valueOf(Color.parseColor("#FF6B57")));
                chip.setTextColor(getResources().getColor(R.color.black));
                chipGroupBrand.addView(chip);
            } catch (Exception ignored) {}
        }

        chipGroupBrand.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (!checkedIds.isEmpty()) {
                Chip chip = findViewById(checkedIds.get(0));
                selectedBrandId = (int) chip.getTag();
            } else selectedBrandId = -1;
        });
    }
}
