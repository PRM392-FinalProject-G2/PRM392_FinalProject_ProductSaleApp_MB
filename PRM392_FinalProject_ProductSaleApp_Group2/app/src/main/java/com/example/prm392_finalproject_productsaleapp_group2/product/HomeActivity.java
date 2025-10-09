package com.example.prm392_finalproject_productsaleapp_group2.product;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.example.prm392_finalproject_productsaleapp_group2.R;
import com.example.prm392_finalproject_productsaleapp_group2.net.ApiConfig;
import com.example.prm392_finalproject_productsaleapp_group2.utils.NavigationBarUtil;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class HomeActivity extends AppCompatActivity {

    private ViewPager2 bannerViewPager;
    private Handler handler;
    private int currentPageBanner = 0;
    private Runnable bannerRunnable;
    private List<Integer> bannerImages = Arrays.asList(
            R.drawable.sample_banner,   // thay bằng ảnh của bạn
            R.drawable.sample_banner2,
            R.drawable.sample_banner3,
            R.drawable.sample_banner4);

    private int currentPageProduct = 1;
    private boolean isLoading = false;
    private boolean isLastPage = false;
    private final int PAGE_SIZE = 10;

    private ImageView btnFilter;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home);

    // Xử lý padding cho Edge-to-Edge
//        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
//            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
//            return insets;
//        });

        // Ánh xạ ViewPager
        bannerViewPager = findViewById(R.id.bannerViewPager);
        BannerAdapter adapter_banner = new BannerAdapter(bannerImages);
        bannerViewPager.setAdapter(adapter_banner);

        // Tạo handler và runnable cho auto-slide
        handler = new Handler(Looper.getMainLooper());
        bannerRunnable = () -> {
            currentPageBanner = (currentPageBanner + 1) % bannerImages.size();
            bannerViewPager.setCurrentItem(currentPageBanner, true);
            handler.postDelayed(bannerRunnable, 3000); // đổi ảnh sau 3s
        };

        // Bắt đầu auto-slide
        handler.postDelayed(bannerRunnable, 3000);

        // Nếu người dùng vuốt tay thì reset timer
        bannerViewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                currentPageBanner = position;
                handler.removeCallbacks(bannerRunnable);
                handler.postDelayed(bannerRunnable, 3000);
            }
        });
        // Category
        RecyclerView rvCategory = findViewById(R.id.rvCategory);
        List<JSONObject> categoryList = new ArrayList<>();
        CategoryAdapter adapter_category = new CategoryAdapter(categoryList);
        LinearLayoutManager layoutManager =
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        rvCategory.setLayoutManager(layoutManager);
        rvCategory.setAdapter(adapter_category);
        // Gọi hàm tải danh mục
        loadCategories(categoryList, adapter_category);

        //Sản phẩm nổi bật
        RecyclerView rvProduct = findViewById(R.id.rvFeaturedProducts);
        JSONArray[] productArray = new JSONArray[]{new JSONArray()};
        ProductAdapter adapter_product = new ProductAdapter(productArray[0]);
        rvProduct.setLayoutManager(new GridLayoutManager(this, 2)); // 2 sản phẩm / hàng
        rvProduct.setAdapter(adapter_product);

        // Gọi API đầu tiên lấy sản phẩm
        loadProducts(productArray, adapter_product, currentPageProduct);

        //  Gắn sự kiện cuộn để load thêm khi gần cuối danh sách
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
                        //  Gần cuối danh sách -> tải thêm trang mới
                        currentPageProduct++;
                        loadProducts(productArray, adapter_product, currentPageProduct);
                    }
                }
            }
        });
        btnFilter = findViewById(R.id.btnFilter);
        //Filter
        btnFilter.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, FilterActivity.class);
            startActivity(intent);
        });

        // Setup navigation bar
        NavigationBarUtil.setupNavigationBar(this);
        NavigationBarUtil.setActiveNavigationButton(this, "home");
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        NavigationBarUtil.finishActivityWithoutAnimation(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        handler.removeCallbacks(bannerRunnable);
    }

    @Override
    protected void onResume() {
        super.onResume();
        handler.postDelayed(bannerRunnable, 3000);
    }

    private void loadCategories(List<JSONObject> categoryList, CategoryAdapter adapter_category) {
        new AsyncTask<Void, Void, Boolean>() {
            @Override
            protected Boolean doInBackground(Void... voids) {
                try {
                    URL url = new URL(ApiConfig.endpoint("/api/Categories/filter"));
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
                if (success) adapter_category.notifyDataSetChanged();
                else Toast.makeText(HomeActivity.this, "Không tải được danh mục", Toast.LENGTH_SHORT).show();
            }
        }.execute();
    }

    private void loadProducts(JSONArray[] productArray, ProductAdapter adapter_product, int pageNumber) {
        if (isLoading || isLastPage) return;
        isLoading = true;

        new AsyncTask<Void, Void, Boolean>() {
            JSONArray newItems;

            @Override
            protected Boolean doInBackground(Void... voids) {
                try {
                    URL url = new URL(ApiConfig.endpoint("/api/Products/filter?pageNumber=" + pageNumber +
                            "&pageSize=" + PAGE_SIZE));
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
                        newItems = root.getJSONArray("items");
                        // Nếu không có thêm dữ liệu thì đánh dấu là trang cuối
                        if (newItems.length() == 0) isLastPage = true;
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
                    // Nối thêm vào mảng cũ
                    for (int i = 0; i < newItems.length(); i++) {
                        productArray[0].put(newItems.optJSONObject(i));
                    }
                    adapter_product.updateData(productArray[0]);
                 }
//                else if (isLastPage) {
//                    Toast.makeText(HomeActivity.this, "Đã tải hết sản phẩm", Toast.LENGTH_SHORT).show();
//                } else {
//                    Toast.makeText(HomeActivity.this, "Không tải được sản phẩm", Toast.LENGTH_SHORT).show();
//                }
            }
        }.execute();
    }


}