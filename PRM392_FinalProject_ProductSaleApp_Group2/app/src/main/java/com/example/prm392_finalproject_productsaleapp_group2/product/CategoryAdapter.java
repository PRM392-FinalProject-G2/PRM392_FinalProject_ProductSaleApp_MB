package com.example.prm392_finalproject_productsaleapp_group2.product;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.prm392_finalproject_productsaleapp_group2.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.ViewHolder> {

    private final List<JSONObject> categories;

    public CategoryAdapter(List<JSONObject> categories) {
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
        JSONObject category = categories.get(position);
        // 👉 1️⃣ Đặt chiều rộng mỗi item = 1/4 màn hình
        ViewGroup.LayoutParams params = holder.itemView.getLayoutParams();
        params.width = (int) (holder.itemView.getContext()
                .getResources().getDisplayMetrics().widthPixels / 4.2); // Chia 4.2 để có khoảng cách
        holder.itemView.setLayoutParams(params);

        try {
            holder.tvName.setText(category.getString("categoryName"));
            String imageUrl = category.getString("imageUrl");

            // Tải ảnh đơn giản không cần thư viện
            new LoadImageTask(holder.img).execute(imageUrl);
            // 👉 Thêm sự kiện click
            holder.itemView.setOnClickListener(v -> {
                try {
                    int categoryId = category.getInt("categoryId");
                    String categoryName = category.getString("categoryName");

                    android.content.Intent intent = new android.content.Intent(
                            v.getContext(),
                            ListProductActivity.class
                    );
                    intent.putExtra("categoryId", categoryId);
                    intent.putExtra("categoryName", categoryName);
                    v.getContext().startActivity(intent);

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

    private static class LoadImageTask extends android.os.AsyncTask<String, Void, android.graphics.Bitmap> {
        private final ImageView imageView;

        LoadImageTask(ImageView imageView) {
            this.imageView = imageView;
        }

        @Override
        protected android.graphics.Bitmap doInBackground(String... urls) {
            String urlDisplay = urls[0];
            try {
                java.io.InputStream in = new java.net.URL(urlDisplay).openStream();
                return android.graphics.BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(android.graphics.Bitmap result) {
            if (result != null) {
                imageView.setImageBitmap(result);
            } else {
                imageView.setImageResource(R.drawable.ic_launcher_foreground); // ảnh fallback
            }
        }
    }

}


