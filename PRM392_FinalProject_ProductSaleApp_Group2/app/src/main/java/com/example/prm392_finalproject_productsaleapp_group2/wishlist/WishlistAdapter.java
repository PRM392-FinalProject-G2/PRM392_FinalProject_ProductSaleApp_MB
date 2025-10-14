package com.example.prm392_finalproject_productsaleapp_group2.wishlist;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
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
import com.example.prm392_finalproject_productsaleapp_group2.models.Product;
import com.example.prm392_finalproject_productsaleapp_group2.models.ProductImage;
import com.example.prm392_finalproject_productsaleapp_group2.models.Wishlist;
import com.example.prm392_finalproject_productsaleapp_group2.net.WishlistApiClient;
import com.example.prm392_finalproject_productsaleapp_group2.product.ProductDetailActivity;
import com.example.prm392_finalproject_productsaleapp_group2.services.WishlistApiService;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class WishlistAdapter extends RecyclerView.Adapter<WishlistAdapter.WishlistViewHolder> {
    
    private List<Wishlist> wishlistItems;
    private Context context;
    private OnWishlistItemListener listener;
    private SessionManager sessionManager;
    private WishlistApiService apiService;

    public interface OnWishlistItemListener {
        void onItemRemoved(int position);
        void onItemClicked(Product product);
    }

    public WishlistAdapter(Context context, List<Wishlist> wishlistItems, OnWishlistItemListener listener) {
        this.context = context;
        this.wishlistItems = wishlistItems;
        this.listener = listener;
        this.sessionManager = new SessionManager(context);
        this.apiService = WishlistApiClient.getInstance().getApiService();
    }

    @NonNull
    @Override
    public WishlistViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_wishlist, parent, false);
        return new WishlistViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull WishlistViewHolder holder, int position) {
        Wishlist wishlistItem = wishlistItems.get(position);
        Product product = wishlistItem.getProduct();
        
        if (product != null) {
            // Set product name
            holder.tvProductName.setText(product.getProductName());
            
            // Set product price with formatting
            DecimalFormat formatter = new DecimalFormat("#,###");
            String formattedPrice = formatter.format(product.getPrice()) + " đ";
            holder.tvProductPrice.setText(formattedPrice);
            
            // Set product description
            holder.tvProductDescription.setText(product.getBriefDescription());
            
            // Load product image - check multiple sources
            loadProductImage(holder.ivProductImage, product);
            
            // Handle product click
            holder.itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onItemClicked(product);
                }
            });
            
            // Handle remove from wishlist
            holder.ivRemove.setOnClickListener(v -> {
                removeFromWishlist(wishlistItem.getWishlistId(), position);
            });
        }
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
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
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
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                Toast.makeText(context, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void updateData(List<Wishlist> newWishlistItems) {
        this.wishlistItems = newWishlistItems;
        notifyDataSetChanged();
    }

    private void loadProductImage(ImageView imageView, Product product) {
        String imageUrl = null;
        
        // 1. Try to get primary image from productImages list
        if (product.getProductImages() != null && !product.getProductImages().isEmpty()) {
            // Find primary image
            for (ProductImage productImage : product.getProductImages()) {
                if (productImage.isPrimary()) {
                    imageUrl = productImage.getImageUrl();
                    break;
                }
            }
            
            // If no primary image found, use first image
            if (imageUrl == null || imageUrl.isEmpty()) {
                imageUrl = product.getProductImages().get(0).getImageUrl();
            }
        }
        
        // 2. Fallback to legacy imageUrl field
        if ((imageUrl == null || imageUrl.isEmpty()) && 
            product.getImageUrl() != null && !product.getImageUrl().isEmpty()) {
            imageUrl = product.getImageUrl();
        }
        
        // 3. Load image or show placeholder
        if (imageUrl != null && !imageUrl.isEmpty()) {
            new LoadImageTask(imageView).execute(imageUrl);
        } else {
            imageView.setImageResource(R.drawable.ic_placeholder);
        }
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

    private static class LoadImageTask extends AsyncTask<String, Void, Bitmap> {
        private final ImageView imageView;

        LoadImageTask(ImageView imageView) {
            this.imageView = imageView;
        }

        @Override
        protected Bitmap doInBackground(String... urls) {
            String imageUrl = urls[0];
            try {
                URL url = new URL(imageUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setDoInput(true);
                connection.connect();
                InputStream inputStream = connection.getInputStream();
                return BitmapFactory.decodeStream(inputStream);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            if (bitmap != null && imageView != null) {
                imageView.setImageBitmap(bitmap);
            } else if (imageView != null) {
                imageView.setImageResource(R.drawable.ic_placeholder);
            }
        }
    }
}