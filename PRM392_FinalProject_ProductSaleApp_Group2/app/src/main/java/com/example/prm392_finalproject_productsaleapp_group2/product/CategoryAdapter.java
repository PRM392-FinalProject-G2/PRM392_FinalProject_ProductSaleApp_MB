package com.example.prm392_finalproject_productsaleapp_group2.product;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.prm392_finalproject_productsaleapp_group2.R;
import com.example.prm392_finalproject_productsaleapp_group2.models.Category;
import com.example.prm392_finalproject_productsaleapp_group2.models.ProductFilterBM;

import java.util.ArrayList;
import java.util.List;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.ViewHolder> {

    private final List<Category> categories;

    public CategoryAdapter(List<Category> categories) {
        this.categories = categories;
    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_category, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Category category = categories.get(position);

        // 👉 Điều chỉnh kích thước item
        ViewGroup.LayoutParams params = holder.itemView.getLayoutParams();
        params.width = (int) (holder.itemView.getContext()
                .getResources().getDisplayMetrics().widthPixels / 4.2);
        holder.itemView.setLayoutParams(params);

        holder.tvName.setText(category.getCategoryName());

        // ✅ Load ảnh bằng Glide
        if (category.getImageUrl() != null && !category.getImageUrl().isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(category.getImageUrl())
                    .placeholder(R.drawable.ic_placeholder) // ảnh tạm khi chưa load xong
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .centerCrop()
                    .into(holder.img);
        } else {
            holder.img.setImageResource(R.drawable.ic_placeholder);
        }

        // 👉 Click mở ListProductActivity với ProductFilterBM
        holder.itemView.setOnClickListener(v -> {
            ProductFilterBM filter = new ProductFilterBM();
            filter.CategoryIds = new ArrayList<>();
            filter.CategoryIds.add(category.getCategoryId());

            // Dùng Context của Activity truyền vào adapter
            Context context = v.getContext();
            Intent intent = new Intent(context, ListProductActivity.class);
            intent.putExtra("productFilter", filter); // ProductFilterBM phải Serializable
            context.startActivity(intent);
        });

    }

    @Override
    public int getItemCount() {
        return categories.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView img;
        TextView tvName;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            img = itemView.findViewById(R.id.imgCategory);
            tvName = itemView.findViewById(R.id.tvCategoryName);
        }
    }

}
