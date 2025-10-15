package com.example.prm392_finalproject_productsaleapp_group2.order;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.prm392_finalproject_productsaleapp_group2.R;
import com.example.prm392_finalproject_productsaleapp_group2.models.CartItem;
import com.example.prm392_finalproject_productsaleapp_group2.models.Product;
import com.example.prm392_finalproject_productsaleapp_group2.services.ImageService;

import java.util.ArrayList;
import java.util.List;

public class OrderItemAdapter extends RecyclerView.Adapter<OrderItemAdapter.OrderItemViewHolder> {
    private List<CartItem> cartItems;
    private OnProductClickListener onProductClickListener;

    public OrderItemAdapter() {
        this.cartItems = new ArrayList<>();
    }

    public void setOnProductClickListener(OnProductClickListener listener) {
        this.onProductClickListener = listener;
    }

    public interface OnProductClickListener {
        void onProductClick(Product product);
    }

    public void setCartItems(List<CartItem> cartItems) {
        this.cartItems = cartItems;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public OrderItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_order_product, parent, false);
        return new OrderItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderItemViewHolder holder, int position) {
        CartItem cartItem = cartItems.get(position);
        holder.bind(cartItem);
    }

    @Override
    public int getItemCount() {
        return cartItems.size();
    }

    class OrderItemViewHolder extends RecyclerView.ViewHolder {
        private ImageView ivProductImage;
        private TextView tvProductName;
        private TextView tvCategory;
        private TextView tvPrice;
        private TextView tvQuantity;
        private TextView btnDetails;

        public OrderItemViewHolder(@NonNull View itemView) {
            super(itemView);
            ivProductImage = itemView.findViewById(R.id.iv_product_image);
            tvProductName = itemView.findViewById(R.id.tv_product_name);
            tvCategory = itemView.findViewById(R.id.tv_category);
            tvPrice = itemView.findViewById(R.id.tv_price);
            tvQuantity = itemView.findViewById(R.id.tv_quantity);
            btnDetails = itemView.findViewById(R.id.btn_details);
        }

        public void bind(CartItem cartItem) {
            try {
                android.util.Log.d("OrderItemAdapter", "Binding cart item: " + cartItem);
                Product product = cartItem.getProduct();
                android.util.Log.d("OrderItemAdapter", "Product: " + product);
                
                if (product != null) {
                    // Set product name - safe way
                    String productName = product.getProductName();
                    if (productName == null || productName.trim().isEmpty()) {
                        productName = "Unknown Product";
                    }
                    tvProductName.setText(productName);
                    
                    // Set category - safe way
                    String categoryName = "N/A";
                    if (product.getCategory() != null && product.getCategory().getCategoryName() != null) {
                        categoryName = product.getCategory().getCategoryName();
                    }
                    tvCategory.setText(categoryName);
                    
                    // Set product price - safe way
                    int price = product.getPrice();
                    tvPrice.setText(price + " ₫");
                    
                    // Set quantity - safe way
                    int quantity = cartItem.getQuantity();
                    tvQuantity.setText("x" + quantity);

                    // Load product image - safe way
                    try {
                        String imageUrl = getPrimaryImageUrl(product);
                        android.util.Log.d("OrderItemAdapter", "Image URL: " + imageUrl);
                        if (imageUrl != null && !imageUrl.trim().isEmpty()) {
                            ImageService.loadImage(imageUrl, ivProductImage, R.drawable.ic_placeholder);
                        } else {
                            android.util.Log.d("OrderItemAdapter", "No image URL, using placeholder");
                            ivProductImage.setImageResource(R.drawable.ic_placeholder);
                        }
                    } catch (Exception e) {
                        android.util.Log.e("OrderItemAdapter", "Error loading image: " + e.getMessage());
                        ivProductImage.setImageResource(R.drawable.ic_placeholder);
                    }

                    // Set click listener for details button
                    btnDetails.setOnClickListener(v -> {
                        if (onProductClickListener != null) {
                            onProductClickListener.onProductClick(product);
                        }
                    });
                } else {
                    // Fallback if no product found
                    tvProductName.setText("Unknown Product");
                    tvCategory.setText("N/A");
                    tvPrice.setText("0 ₫");
                    tvQuantity.setText("x0");
                    ivProductImage.setImageResource(R.drawable.ic_placeholder);
                    
                    // Disable details button if no product
                    btnDetails.setEnabled(false);
                    btnDetails.setText("N/A");
                }
            } catch (Exception e) {
                // Complete fallback with error logging
                android.util.Log.e("OrderItemAdapter", "Error binding cart item: " + e.getMessage(), e);
                tvProductName.setText("Error: " + e.getMessage());
                tvCategory.setText("N/A");
                tvPrice.setText("0 ₫");
                tvQuantity.setText("x0");
                ivProductImage.setImageResource(R.drawable.ic_placeholder);
            }
        }
    }

    private String getPrimaryImageUrl(Product product) {
        try {
            if (product != null && product.getProductImages() != null && !product.getProductImages().isEmpty()) {
                for (com.example.prm392_finalproject_productsaleapp_group2.models.ProductImage img : product.getProductImages()) {
                    if (img != null && img.isPrimary()) {
                        return img.getImageUrl();
                    }
                }
                // If no primary image found, return first image
                com.example.prm392_finalproject_productsaleapp_group2.models.ProductImage firstImg = product.getProductImages().get(0);
                return firstImg != null ? firstImg.getImageUrl() : null;
            }
        } catch (Exception e) {
            // Return null if any error occurs
        }
        return null;
    }
}
