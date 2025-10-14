package com.example.prm392_finalproject_productsaleapp_group2.wishlist;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;
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
import com.example.prm392_finalproject_productsaleapp_group2.models.ApiResponse;
import com.example.prm392_finalproject_productsaleapp_group2.models.WishlistMobile;
import com.example.prm392_finalproject_productsaleapp_group2.net.WishlistApiClient;
import com.example.prm392_finalproject_productsaleapp_group2.services.WishlistApiService;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class WishlistMobileAdapter extends RecyclerView.Adapter<WishlistMobileAdapter.WishlistViewHolder> {
    
    private List<WishlistMobile> wishlistItems;
    private final Context context;
    private final OnWishlistItemListener listener;
    private final SessionManager sessionManager;
    private final WishlistApiService apiService;
    private final ExecutorService executor;
    private final Handler mainHandler;

    public interface OnWishlistItemListener {
        void onItemRemoved(int position);
        void onItemClicked(int productId);
    }

    public WishlistMobileAdapter(Context context, List<WishlistMobile> wishlistItems, OnWishlistItemListener listener) {
        this.context = context;
        this.wishlistItems = wishlistItems;
        this.listener = listener;
        this.sessionManager = new SessionManager(context);
        this.apiService = WishlistApiClient.getInstance().getApiService();
        this.executor = Executors.newFixedThreadPool(4);
        this.mainHandler = new Handler(Looper.getMainLooper());
    }

    @NonNull
    @Override
    public WishlistViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_wishlist, parent, false);
        return new WishlistViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull WishlistViewHolder holder, @SuppressLint("RecyclerView") int position) {
        WishlistMobile wishlistItem = wishlistItems.get(position);
        
        // Set product name
        holder.tvProductName.setText(wishlistItem.getProductName());
        
        // Set product price with formatting
        DecimalFormat formatter = new DecimalFormat("#,###");
        String formattedPrice = formatter.format(wishlistItem.getPrice()) + " đ";
        holder.tvProductPrice.setText(formattedPrice);
        
        // Set product description
        holder.tvProductDescription.setText(wishlistItem.getBriefDescription());
        
        // Load product image
        if (wishlistItem.getPrimaryImageUrl() != null && !wishlistItem.getPrimaryImageUrl().isEmpty()) {
            loadImageAsync(holder.ivProductImage, wishlistItem.getPrimaryImageUrl());
        } else {
            holder.ivProductImage.setImageResource(R.drawable.ic_placeholder);
        }
        
        // Handle product click
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onItemClicked(wishlistItem.getProductId());
                }
            }
        });
        
        // Handle remove from wishlist
        holder.ivRemove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                removeFromWishlist(wishlistItem.getWishlistId(), position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return wishlistItems != null ? wishlistItems.size() : 0;
    }

    private void removeFromWishlist(int wishlistId, int position) {
        String token = sessionManager.getAuthToken();
        if (token == null || token.isEmpty()) {
            Toast.makeText(context, "Vui lòng đăng nhập", Toast.LENGTH_SHORT).show();
            return;
        }

        String authToken = "Bearer " + token;
        
        Call<ApiResponse> call = apiService.removeFromWishlist(authToken, wishlistId);
        call.enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse> call, @NonNull Response<ApiResponse> response) {
                if (response.isSuccessful()) {
                    // Remove item from list and notify adapter
                    wishlistItems.remove(position);
                    notifyItemRemoved(position);
                    notifyItemRangeChanged(position, wishlistItems.size());
                    
                    if (listener != null) {
                        listener.onItemRemoved(position);
                    }
                    
                    Toast.makeText(context, "Đã xóa khỏi danh sách yêu thích", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(context, "Không thể xóa sản phẩm", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse> call, @NonNull Throwable t) {
                Toast.makeText(context, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @SuppressLint("NotifyDataSetChanged")
    public void updateData(List<WishlistMobile> newWishlistItems) {
        this.wishlistItems = newWishlistItems;
        notifyDataSetChanged();
    }

    static class WishlistViewHolder extends RecyclerView.ViewHolder {
        ImageView ivProductImage, ivRemove;
        TextView tvProductName, tvProductPrice, tvProductDescription;

        public WishlistViewHolder(@NonNull View itemView) {
            super(itemView);
            ivProductImage = itemView.findViewById(R.id.iv_product_image);
            ivRemove = itemView.findViewById(R.id.iv_remove);
            tvProductName = itemView.findViewById(R.id.tv_product_name);
            tvProductPrice = itemView.findViewById(R.id.tv_product_price);
            tvProductDescription = itemView.findViewById(R.id.tv_product_description);
        }
    }

    private void loadImageAsync(ImageView imageView, String imageUrl) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                Bitmap bitmap = null;
                try {
                    URL url = new URL(imageUrl);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setDoInput(true);
                    connection.connect();
                    InputStream inputStream = connection.getInputStream();
                    bitmap = BitmapFactory.decodeStream(inputStream);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                
                final Bitmap finalBitmap = bitmap;
                mainHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (finalBitmap != null) {
                            imageView.setImageBitmap(finalBitmap);
                        } else {
                            imageView.setImageResource(R.drawable.ic_placeholder);
                        }
                    }
                });
            }
        });
    }
}