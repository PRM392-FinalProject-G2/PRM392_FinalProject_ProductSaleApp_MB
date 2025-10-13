package com.example.prm392_finalproject_productsaleapp_group2.product;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.prm392_finalproject_productsaleapp_group2.R;
import com.example.prm392_finalproject_productsaleapp_group2.auth.SessionManager;
import com.example.prm392_finalproject_productsaleapp_group2.net.ApiConfig;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.HashSet;
import java.util.HashMap;
import java.util.Set;
import java.util.Map;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ViewHolder> {
    private JSONArray products;
    private int userId;
    private SessionManager sessionManager;
    private Context appContext;
    private final Set<Integer> favoriteProductIds = new HashSet<>();
    private final Map<Integer, Integer> productIdToWishlistId = new HashMap<>();


    public ProductAdapter(JSONArray products) {
        this.products = products;
    }

    public void initSession(Context context) {
        this.appContext = context.getApplicationContext();
        this.sessionManager = new SessionManager(context);
        this.userId = sessionManager.getUserId();
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
        try {
            JSONObject product = products.getJSONObject(position);
            holder.tvName.setText(product.getString("productName"));

            long price = product.getLong("price");
            holder.tvPrice.setText(NumberFormat.getInstance(new Locale("vi", "VN")).format(price) + " ₫");
            holder.tvBrief.setText(product.optString("briefDescription", "Không có mô tả"));
            holder.tvAverageRating.setText("⭐ " + product.optDouble("averageRating", 0));
            holder.tvPopularity.setText("🔥 " + product.optInt("popularity", 0));

            JSONArray images = product.optJSONArray("productImages");
            if (images != null && images.length() > 0) {
                String primaryImageUrl = null;
                for (int i = 0; i < images.length(); i++) {
                    JSONObject imgObj = images.getJSONObject(i);
                    if (imgObj.optBoolean("isPrimary", false)) {
                        primaryImageUrl = imgObj.optString("imageUrl", null);
                        break;
                    }
                }

                // Nếu không có hình nào isPrimary = true thì lấy hình đầu tiên làm fallback
                if (primaryImageUrl == null) {
                    primaryImageUrl = images.getJSONObject(0).optString("imageUrl", null);
                }

                if (primaryImageUrl != null) {
                    new LoadImageTask(holder.img).execute(primaryImageUrl);
                } else {
                    holder.img.setImageResource(R.drawable.ic_launcher_foreground);
                }
            } else {
                holder.img.setImageResource(R.drawable.ic_launcher_foreground);
            }

            holder.itemView.setOnClickListener(v -> {
                try {
                    int productId = products.getJSONObject(position).getInt("productId");
                    android.content.Intent intent = new android.content.Intent(
                            v.getContext(),
                            ProductDetailActivity.class);
                    intent.putExtra("productId", productId); // chỉ truyền ID
                    v.getContext().startActivity(intent);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            });

            // Set initial wishlist UI state per product data
            boolean isFavorite = product.optBoolean("isFavorite", false);
            int pidForBind = product.optInt("productId", -1);
            if (!isFavorite && pidForBind > 0 && favoriteProductIds.contains(pidForBind)) {
                isFavorite = true;
                if (productIdToWishlistId.containsKey(pidForBind)) {
                    product.put("wishlistId", productIdToWishlistId.get(pidForBind));
                }
            }
            if (isFavorite) {
                holder.ivWishlist.setImageResource(R.drawable.baseline_favorite_24);
                if (product.has("wishlistId")) {
                    holder.ivWishlist.setTag(R.id.tag_wishlist_id, product.optInt("wishlistId", -1));
                }
            } else {
                holder.ivWishlist.setImageResource(R.drawable.baseline_favorite_border_24);
                holder.ivWishlist.setTag(R.id.tag_wishlist_id, null);
            }
            holder.ivWishlist.setTag(isFavorite);

            // If unknown yet, perform a quick on-demand check for this item
            if (!isFavorite && pidForBind > 0) {
                checkWishlistForProduct(pidForBind, holder.ivWishlist);
            }

            holder.ivWishlist.setOnClickListener(v -> {
                try {
                    if (sessionManager == null) {
                        initSession(holder.itemView.getContext());
                    }
                    // Ensure we have user ID
                    if (userId <= 0)
                        return;
                    int productId = product.getInt("productId");

                    Object tag = holder.ivWishlist.getTag();
                    boolean currentFav = tag instanceof Boolean && (Boolean) tag;
                    if (currentFav) {
                        removeFromWishlist(userId, productId, holder.ivWishlist);
                    } else {
                        addToWishlist(userId, productId, holder.ivWishlist);
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            });

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        return products.length();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivWishlist;
        ImageView img;
        TextView tvName, tvPrice, tvBrief;
        TextView tvAverageRating, tvPopularity;


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

    public void updateData(JSONArray newProducts) {
        this.products = newProducts;
        notifyDataSetChanged();
    }

    private static class LoadImageTask extends AsyncTask<String, Void, Bitmap> {
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
            else
                imageView.setImageResource(R.drawable.ic_launcher_foreground);
        }
    }

    private void addToWishlist(int userId, int productId, ImageView ivWishlist) {
        new Thread(() -> {
            try {
                URL url = new URL(ApiConfig.endpoint("/api/Wishlists"));
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                conn.setDoOutput(true);

                JSONObject body = new JSONObject();
                body.put("userId", userId);
                body.put("productId", productId);

                try (OutputStream os = conn.getOutputStream()) {
                    os.write(body.toString().getBytes(StandardCharsets.UTF_8));
                }

                if (conn.getResponseCode() == 201) {
                    InputStream in = conn.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null)
                        sb.append(line);
                    JSONObject response = new JSONObject(sb.toString());

                    int wishlistId = response.getInt("wishlistId");

                    ivWishlist.post(() -> {
                        ivWishlist.setImageResource(R.drawable.baseline_favorite_24); // ❤️
                        ivWishlist.setTag(true);
                        ivWishlist.setTag(R.id.tag_wishlist_id, wishlistId); // 🔹 lưu id lại
                        favoriteProductIds.add(productId);
                        productIdToWishlistId.put(productId, wishlistId);
                    });
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void removeFromWishlist(int userId, int productId, ImageView ivWishlist) {
        Object tag = ivWishlist.getTag(R.id.tag_wishlist_id);
        if (tag == null)
            return;
        int wishlistId = (int) tag;

        new Thread(() -> {
            try {
                URL url = new URL(ApiConfig.endpoint("/api/Wishlists/" + wishlistId));
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("DELETE");

                if (conn.getResponseCode() >= 200 && conn.getResponseCode() < 300) {
                    ivWishlist.post(() -> {
                        ivWishlist.setImageResource(R.drawable.baseline_favorite_border_24); // 🤍
                        ivWishlist.setTag(false);
                        ivWishlist.setTag(R.id.tag_wishlist_id, null);
                        favoriteProductIds.remove(productId);
                        productIdToWishlistId.remove(productId);
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    public void loadWishlistForUser() {
        new Thread(() -> {
            try {
                if (userId <= 0)
                    return;
                URL url = new URL(ApiConfig.endpoint("/api/Wishlists/filter?UserId=" + userId + "&pageSize=1000"));
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");

                if (conn.getResponseCode() == 200) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null)
                        sb.append(line);
                    JSONObject root = new JSONObject(sb.toString());
                    JSONArray wishlistArray = root.optJSONArray("items");
                    favoriteProductIds.clear();
                    productIdToWishlistId.clear();

                    // Cập nhật từng sản phẩm trong danh sách
                    if (wishlistArray != null) {
                        for (int j = 0; j < wishlistArray.length(); j++) {
                            JSONObject wishlistItem = wishlistArray.getJSONObject(j);
                            int pid = wishlistItem.optInt("productId", -1);
                            int wid = wishlistItem.optInt("wishlistId", -1);
                            if (pid > 0) {
                                favoriteProductIds.add(pid);
                                if (wid > 0)
                                    productIdToWishlistId.put(pid, wid);
                            }
                        }
                    }

                    for (int i = 0; i < products.length(); i++) {
                        JSONObject product = products.getJSONObject(i);
                        int pid = product.optInt("productId", -1);
                        if (pid > 0 && favoriteProductIds.contains(pid)) {
                            product.put("isFavorite", true);
                            if (productIdToWishlistId.containsKey(pid)) {
                                product.put("wishlistId", productIdToWishlistId.get(pid));
                            }
                        }
                    }

                    // Cập nhật giao diện
                    new Handler(Looper.getMainLooper()).post(this::notifyDataSetChanged);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void checkWishlistForProduct(int productId, ImageView ivWishlist) {
        if (userId <= 0)
            return;
        if (favoriteProductIds.contains(productId))
            return;
        new Thread(() -> {
            try {
                URL url = new URL(ApiConfig.endpoint(
                        "/api/Wishlists/filter?UserId=" + userId + "&ProductId=" + productId + "&pageSize=1"));
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
                        favoriteProductIds.add(productId);
                        if (wid > 0)
                            productIdToWishlistId.put(productId, wid);
                        ivWishlist.post(() -> {
                            ivWishlist.setImageResource(R.drawable.baseline_favorite_24);
                            ivWishlist.setTag(true);
                            if (wid > 0)
                                ivWishlist.setTag(R.id.tag_wishlist_id, wid);
                        });
                    }
                }
            } catch (Exception ignored) {
            }
        }).start();
    }

}
