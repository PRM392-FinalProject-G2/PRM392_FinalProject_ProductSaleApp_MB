package com.example.prm392_finalproject_productsaleapp_group2.product;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.prm392_finalproject_productsaleapp_group2.R;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.InputStream;
import java.net.URL;
import java.text.NumberFormat;
import java.util.Locale;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ViewHolder> {
    private JSONArray products;

    public ProductAdapter(JSONArray products) {
        this.products = products;
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

            new LoadImageTask(holder.img).execute(product.getString("imageUrl"));


            holder.itemView.setOnClickListener(v -> {
                try {
                    int productId = products.getJSONObject(position).getInt("productId");
                    android.content.Intent intent = new android.content.Intent(
                            v.getContext(),
                            ProductDetailActivity.class
                    );
                    intent.putExtra("productId", productId); // chỉ truyền ID
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
        return products.length();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView img;
        TextView tvName, tvPrice, tvBrief;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            img = itemView.findViewById(R.id.imgProduct);
            tvName = itemView.findViewById(R.id.tvProductName);
            tvPrice = itemView.findViewById(R.id.tvProductPrice);
            tvBrief = itemView.findViewById(R.id.tvProductBriefDescription);
        }
    }

    public void updateData(JSONArray newProducts) {
        this.products = newProducts;
        notifyDataSetChanged();
    }

    private static class LoadImageTask extends AsyncTask<String, Void, Bitmap> {
        private final ImageView imageView;
        LoadImageTask(ImageView imageView) { this.imageView = imageView; }

        @Override
        protected Bitmap doInBackground(String... urls) {
            try (InputStream in = new URL(urls[0]).openStream()) {
                return BitmapFactory.decodeStream(in);
            } catch (Exception e) { return null; }
        }

        @Override
        protected void onPostExecute(Bitmap result) {
            if (result != null) imageView.setImageBitmap(result);
            else imageView.setImageResource(R.drawable.ic_launcher_foreground);
        }
    }
}
