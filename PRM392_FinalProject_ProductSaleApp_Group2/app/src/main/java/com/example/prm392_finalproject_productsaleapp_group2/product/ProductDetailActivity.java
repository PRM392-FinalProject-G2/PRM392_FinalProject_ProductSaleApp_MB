package com.example.prm392_finalproject_productsaleapp_group2.product;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.StyleSpan;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.prm392_finalproject_productsaleapp_group2.R;
import com.example.prm392_finalproject_productsaleapp_group2.net.ApiConfig;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.NumberFormat;
import java.util.Locale;

public class ProductDetailActivity extends AppCompatActivity {

    private ImageView imgProduct;
    private TextView tvName, tvPrice, tvFullDesc, tvSpecs;
    private ImageView btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        
        // Set status bar transparent to let gradient show through
        getWindow().setStatusBarColor(android.graphics.Color.TRANSPARENT);
        
        setContentView(R.layout.activity_product_detail);
        
        // Handle window insets for edge-to-edge display
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0);
            return WindowInsetsCompat.CONSUMED;
        });
        
        //Đóng trang sản phẩm
        btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());
        imgProduct = findViewById(R.id.imgProductDetail);
        tvName = findViewById(R.id.tvProductNameDetail);
        tvPrice = findViewById(R.id.tvProductPriceDetail);
        tvFullDesc = findViewById(R.id.tvProductDescriptionDetail);
        tvSpecs = findViewById(R.id.tvProductSpecsDetail);
        // Tăng giảm số lượng
        TextView btnDecrease = findViewById(R.id.btnDecrease);
        TextView btnIncrease = findViewById(R.id.btnIncrease);
        EditText etQuantity = findViewById(R.id.etQuantity);

        btnIncrease.setOnClickListener(v -> {
            String value = etQuantity.getText().toString();
            int qty = 1;
            try {
                qty = Integer.parseInt(value);
            } catch (NumberFormatException e) {
                qty = 1;
            }
            qty++;
            etQuantity.setText(String.valueOf(qty));
        });

        btnDecrease.setOnClickListener(v -> {
            String value = etQuantity.getText().toString();
            int qty = 1;
            try {
                qty = Integer.parseInt(value);
            } catch (NumberFormatException e) {
                qty = 1;
            }
            if (qty > 1) qty--;  // không cho số lượng < 1
            etQuantity.setText(String.valueOf(qty));
        });

        int productId = getIntent().getIntExtra("productId", -1);
        if (productId != -1) {
            loadProductDetail(productId);
        }
    }

    private void loadProductDetail(int productId) {
        new AsyncTask<Integer, Void, JSONObject>() {
            @Override
            protected JSONObject doInBackground(Integer... ids) {
                try {
                    URL url = new URL(ApiConfig.endpoint("/api/Products/" + ids[0]));
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
                        return new JSONObject(sb.toString());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(JSONObject product) {
                if (product != null) {
                    try {
                        tvName.setText(product.getString("productName"));
                        long price = product.getLong("price");
                        tvPrice.setText(NumberFormat.getInstance(new Locale("vi", "VN")).format(price) + " ₫");
                        SpannableStringBuilder descBuilder = new SpannableStringBuilder();

                        String labelDesc = "Mô tả: ";
                        String descText = product.getString("fullDescription");
                        descBuilder.append(labelDesc);
                        descBuilder.setSpan(new StyleSpan(Typeface.BOLD), 0, labelDesc.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                        descBuilder.append(descText);

                        tvFullDesc.setText(descBuilder);

                        SpannableStringBuilder specsBuilder = new SpannableStringBuilder();
                        String labelSpecs = "Thông số: ";
                        String specsText = product.getString("technicalSpecifications");
                        specsBuilder.append(labelSpecs);
                        specsBuilder.setSpan(new StyleSpan(Typeface.BOLD), 0, labelSpecs.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                        specsBuilder.append(specsText);

                        tvSpecs.setText(specsBuilder);

                        new LoadImageTask(imgProduct).execute(product.getString("imageUrl"));

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        }.execute(productId);
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
            } catch (Exception e) { return null; }
        }

        @Override
        protected void onPostExecute(Bitmap result) {
            if (result != null) imageView.setImageBitmap(result);
        }
    }
}