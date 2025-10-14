package com.example.prm392_finalproject_productsaleapp_group2.product;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.prm392_finalproject_productsaleapp_group2.R;
import com.example.prm392_finalproject_productsaleapp_group2.auth.SessionManager;
import com.example.prm392_finalproject_productsaleapp_group2.models.FilterResponse;
import com.example.prm392_finalproject_productsaleapp_group2.models.Product;
import com.example.prm392_finalproject_productsaleapp_group2.models.Wishlist;
import com.example.prm392_finalproject_productsaleapp_group2.models.WishlistMobile;
import com.example.prm392_finalproject_productsaleapp_group2.net.WishlistApiClient;
import com.example.prm392_finalproject_productsaleapp_group2.services.ImageService;
import com.example.prm392_finalproject_productsaleapp_group2.services.WishlistApiService;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ViewHolder> {

    private List<Product> products;
    private int userId;
    private SessionManager sessionManager;
    private Context appContext;
    private WishlistApiService wishlistService;

    private final Set<Integer> favoriteProductIds = new HashSet<>();
    private final Map<Integer, Integer> productIdToWishlistId = new HashMap<>();

    public ProductAdapter(List<Product> products) {
        this.products = products != null ? products : new ArrayList<>();
    }

    public void initSession(Context context) {
        this.appContext = context.getApplicationContext();
        this.sessionManager = new SessionManager(context);
        this.userId = sessionManager.getUserId();
        this.wishlistService = WishlistApiClient.getInstance().getApiService();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_product, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Product product = products.get(position);
        bindProductData(holder, product);
    }

    @Override
    public int getItemCount() {
        return products.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivWishlist, img;
        TextView tvName, tvPrice, tvBrief, tvAverageRating, tvPopularity;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            img = itemView.findViewById(R.id.imgProduct);
            ivWishlist = itemView.findViewById(R.id.ivWishlist);
            tvName = itemView.findViewById(R.id.tvProductName);
            tvPrice = itemView.findViewById(R.id.tvProductPrice);
            tvBrief = itemView.findViewById(R.id.tvProductBriefDescription);
            tvAverageRating = itemView.findViewById(R.id.tvAverageRating);
            tvPopularity = itemView.findViewById(R.id.tvPopularity);
        }
    }

    // -----------------------------
    // 🧩 BIND PRODUCT DATA
    // -----------------------------
    private void bindProductData(@NonNull ViewHolder holder, @NonNull Product product) {
        holder.tvName.setText(product.getProductName());
        holder.tvPrice.setText(NumberFormat.getInstance(new Locale("vi", "VN"))
                .format(product.getPrice()) + " ₫");
        holder.tvBrief.setText(product.getBriefDescription() != null
                ? product.getBriefDescription() : "Không có mô tả");
        holder.tvAverageRating.setText("⭐ " + product.getAverageRating());
        holder.tvPopularity.setText("🔥 " + product.getPopularity());

        String imageUrl = getPrimaryImageUrl(product);
        if (imageUrl != null) {
            ImageService.loadImage(imageUrl, holder.img);
        } else {
            holder.img.setImageResource(R.drawable.ic_launcher_foreground);
        }

        holder.itemView.setOnClickListener(v -> {
            android.content.Intent intent = new android.content.Intent(v.getContext(), ProductDetailActivity.class);
            intent.putExtra("productId", product.getProductId());
            v.getContext().startActivity(intent);
        });

        setupWishlistIcon(holder, product);
    }

    // -----------------------------
    // 💖 WISHLIST LOGIC
    // -----------------------------
    private void setupWishlistIcon(ViewHolder holder, Product product) {
        boolean isFavorite = favoriteProductIds.contains(product.getProductId());
        int productId = product.getProductId();

        holder.ivWishlist.setImageResource(isFavorite
                ? R.drawable.baseline_favorite_24
                : R.drawable.baseline_favorite_border_24);

        if (isFavorite && productIdToWishlistId.containsKey(productId)) {
            holder.ivWishlist.setTag(R.id.tag_wishlist_id, productIdToWishlistId.get(productId));
        }

        holder.ivWishlist.setTag(isFavorite);

        if (!isFavorite) checkWishlistForProduct(productId, holder.ivWishlist);

        holder.ivWishlist.setOnClickListener(v -> {
            if (sessionManager == null) initSession(holder.itemView.getContext());
            if (userId <= 0) return;

            boolean currentFav = holder.ivWishlist.getTag() instanceof Boolean
                    && (Boolean) holder.ivWishlist.getTag();

            if (currentFav) {
                removeFromWishlist(userId, productId, holder.ivWishlist);
            } else {
                addToWishlist(userId, productId, holder.ivWishlist);
            }
        });
    }

    private void addToWishlist(int userId, int productId, ImageView ivWishlist) {
        String token = sessionManager.getAuthToken();
        if (token == null || token.isEmpty()) return;

        Wishlist wishlist = new Wishlist(userId, productId);
        Call<Wishlist> call = wishlistService.addToWishlist("Bearer " + token, wishlist);

        call.enqueue(new Callback<Wishlist>() {
            @Override
            public void onResponse(@NonNull Call<Wishlist> call, @NonNull Response<Wishlist> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Wishlist result = response.body();
                    ivWishlist.setImageResource(R.drawable.baseline_favorite_24);
                    ivWishlist.setTag(true);
                    ivWishlist.setTag(R.id.tag_wishlist_id, result.getWishlistId());
                    favoriteProductIds.add(productId);
                    productIdToWishlistId.put(productId, result.getWishlistId());
                } else {
                    showToast("Không thể thêm vào yêu thích (" + response.code() + ")");
                }
            }

            @Override
            public void onFailure(@NonNull Call<Wishlist> call, @NonNull Throwable t) {
                showToast("Lỗi kết nối khi thêm vào yêu thích");
            }
        });
    }

    private void removeFromWishlist(int userId, int productId, ImageView ivWishlist) {
        Object tag = ivWishlist.getTag(R.id.tag_wishlist_id);
        if (tag == null) return;
        int wishlistId = (int) tag;

        String token = sessionManager.getAuthToken();
        if (token == null || token.isEmpty()) return;

        Call<com.example.prm392_finalproject_productsaleapp_group2.models.ApiResponse> call =
                wishlistService.removeFromWishlist("Bearer " + token, wishlistId);

        call.enqueue(new Callback<com.example.prm392_finalproject_productsaleapp_group2.models.ApiResponse>() {
            @Override
            public void onResponse(@NonNull Call<com.example.prm392_finalproject_productsaleapp_group2.models.ApiResponse> call,
                                   @NonNull Response<com.example.prm392_finalproject_productsaleapp_group2.models.ApiResponse> response) {
                if (response.isSuccessful()) {
                    ivWishlist.setImageResource(R.drawable.baseline_favorite_border_24);
                    ivWishlist.setTag(false);
                    ivWishlist.setTag(R.id.tag_wishlist_id, null);
                    favoriteProductIds.remove(productId);
                    productIdToWishlistId.remove(productId);
                } else {
                    showToast("Không thể xóa khỏi yêu thích (" + response.code() + ")");
                }
            }

            @Override
            public void onFailure(@NonNull Call<com.example.prm392_finalproject_productsaleapp_group2.models.ApiResponse> call,
                                  @NonNull Throwable t) {
                showToast("Lỗi kết nối khi xóa khỏi yêu thích");
            }
        });
    }

    public void loadWishlistForUser() {
        String token = sessionManager.getAuthToken();
        if (token == null || token.isEmpty() || userId <= 0) return;

        Call<FilterResponse<WishlistMobile>> call =
                wishlistService.getWishlistByUser("Bearer " + token, userId, 1, 1000);

        call.enqueue(new Callback<FilterResponse<WishlistMobile>>() {
            @Override
            public void onResponse(@NonNull Call<FilterResponse<WishlistMobile>> call,
                                   @NonNull Response<FilterResponse<WishlistMobile>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<WishlistMobile> wishlists = response.body().getItems();
                    favoriteProductIds.clear();
                    productIdToWishlistId.clear();

                    for (WishlistMobile w : wishlists) {
                        favoriteProductIds.add(w.getProductId());
                        if (w.getWishlistId() > 0)
                            productIdToWishlistId.put(w.getProductId(), w.getWishlistId());
                    }

                    notifyDataSetChanged();
                }
            }

            @Override
            public void onFailure(@NonNull Call<FilterResponse<WishlistMobile>> call, @NonNull Throwable t) {
                showToast("Không thể tải danh sách yêu thích");
            }
        });
    }

    private void checkWishlistForProduct(int productId, ImageView ivWishlist) {
        String token = sessionManager.getAuthToken();
        if (token == null || token.isEmpty() || userId <= 0) return;

        Call<FilterResponse<WishlistMobile>> call =
                wishlistService.getWishlistByUser("Bearer " + token, userId, 1, 1000);

        call.enqueue(new Callback<FilterResponse<WishlistMobile>>() {
            @Override
            public void onResponse(@NonNull Call<FilterResponse<WishlistMobile>> call,
                                   @NonNull Response<FilterResponse<WishlistMobile>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    for (WishlistMobile w : response.body().getItems()) {
                        if (w.getProductId() == productId) {
                            int wid = w.getWishlistId();
                            favoriteProductIds.add(productId);
                            productIdToWishlistId.put(productId, wid);

                            ivWishlist.setImageResource(R.drawable.baseline_favorite_24);
                            ivWishlist.setTag(true);
                            ivWishlist.setTag(R.id.tag_wishlist_id, wid);
                            break;
                        }
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<FilterResponse<WishlistMobile>> call, @NonNull Throwable t) {
                // Không cần báo lỗi ở đây
            }
        });
    }

    private String getPrimaryImageUrl(Product product) {
        if (product.getProductImages() != null && !product.getProductImages().isEmpty()) {
            for (com.example.prm392_finalproject_productsaleapp_group2.models.ProductImage img : product.getProductImages()) {
                if (img.isPrimary()) return img.getImageUrl();
            }
            return product.getProductImages().get(0).getImageUrl();
        }
        return null;
    }

    public void updateData(List<Product> newProducts) {
        this.products = newProducts != null ? newProducts : new ArrayList<>();
        notifyDataSetChanged();
    }

    private void showToast(String msg) {
        if (appContext != null)
            Toast.makeText(appContext, msg, Toast.LENGTH_SHORT).show();
    }
}
