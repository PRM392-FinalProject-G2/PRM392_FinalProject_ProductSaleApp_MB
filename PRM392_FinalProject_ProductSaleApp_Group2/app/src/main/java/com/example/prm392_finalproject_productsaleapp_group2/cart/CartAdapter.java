package com.example.prm392_finalproject_productsaleapp_group2.cart;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.prm392_finalproject_productsaleapp_group2.R;
import com.example.prm392_finalproject_productsaleapp_group2.models.CartItem;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder> {
    
    private List<CartItem> cartItems;
    private Context context;
    private OnCartItemClickListener listener;

    public interface OnCartItemClickListener {
        void onQuantityChange(CartItem cartItem, int newQuantity);
        void onItemDetailsClick(CartItem cartItem);
        void onDeleteItemClick(CartItem cartItem);
    }

    public CartAdapter(Context context, List<CartItem> cartItems) {
        this.context = context;
        this.cartItems = cartItems;
    }

    public void setOnCartItemClickListener(OnCartItemClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_cart, parent, false);
        return new CartViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CartViewHolder holder, int position) {
        CartItem cartItem = cartItems.get(position);
        holder.bind(cartItem);
    }

    @Override
    public int getItemCount() {
        return cartItems != null ? cartItems.size() : 0;
    }

    public void updateCartItems(List<CartItem> newCartItems) {
        this.cartItems = newCartItems;
        notifyDataSetChanged();
    }

    class CartViewHolder extends RecyclerView.ViewHolder {
        private ImageView ivProductImage;
        private TextView tvProductName, tvCategory, tvPrice;
        private TextView tvQuantity, btnMinus, btnPlus, btnDetails, btnDelete;

        public CartViewHolder(@NonNull View itemView) {
            super(itemView);
            ivProductImage = itemView.findViewById(R.id.iv_product_image);
            tvProductName = itemView.findViewById(R.id.tv_product_name);
            tvCategory = itemView.findViewById(R.id.tv_category);
            tvPrice = itemView.findViewById(R.id.tv_price);
            tvQuantity = itemView.findViewById(R.id.tv_quantity);
            btnMinus = itemView.findViewById(R.id.btn_minus);
            btnPlus = itemView.findViewById(R.id.btn_plus);
            btnDetails = itemView.findViewById(R.id.btn_details);
            btnDelete = itemView.findViewById(R.id.btn_delete);
        }

        public void bind(CartItem cartItem) {
            // Hiển thị ảnh sản phẩm: ưu tiên ảnh đầu tiên trong productImages, fallback imageUrl
            if (cartItem.getProduct() != null) {
                String url = null;
                if (cartItem.getProduct().getProductImages() != null && !cartItem.getProduct().getProductImages().isEmpty()) {
                    url = cartItem.getProduct().getProductImages().get(0).getImageUrl();
                }
                if (url == null) {
                    url = cartItem.getProduct().getImageUrl();
                }
                if (url != null) {
                    Glide.with(context)
                            .load(url)
                            .placeholder(R.drawable.ic_placeholder)
                            .error(R.drawable.ic_placeholder)
                            .into(ivProductImage);
                }
            }

            // Hiển thị tên sản phẩm
            tvProductName.setText(cartItem.getProduct() != null ? 
                    cartItem.getProduct().getProductName() : "Sản phẩm không xác định");

            // Hiển thị danh mục
            String categoryName = cartItem.getProduct() != null && cartItem.getProduct().getCategory() != null ? 
                    cartItem.getProduct().getCategory().getCategoryName() : "Không xác định";
            tvCategory.setText(categoryName);

            // Hiển thị giá với định dạng tiền tệ
            NumberFormat formatter = NumberFormat.getNumberInstance(Locale.getDefault());
            String formattedPrice = formatter.format(cartItem.getPrice()) + " ₫";
            tvPrice.setText(formattedPrice);

            // Hiển thị số lượng
            tvQuantity.setText(String.valueOf(cartItem.getQuantity()));


            // Xử lý sự kiện click
            btnMinus.setOnClickListener(v -> {
                int currentQuantity = cartItem.getQuantity();
                if (currentQuantity > 1 && listener != null) {
                    listener.onQuantityChange(cartItem, currentQuantity - 1);
                }
            });

            btnPlus.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onQuantityChange(cartItem, cartItem.getQuantity() + 1);
                }
            });

            btnDetails.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onItemDetailsClick(cartItem);
                }
            });

            btnDelete.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDeleteItemClick(cartItem);
                }
            });
        }
    }
}

