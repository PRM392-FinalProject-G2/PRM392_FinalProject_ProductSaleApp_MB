package com.example.prm392_finalproject_productsaleapp_group2.order;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.prm392_finalproject_productsaleapp_group2.net.ApiConfig;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;

public class VnpayWebViewActivity extends AppCompatActivity {
    private static final String[] RETURN_URL_HINTS = new String[]{
            "/vnpay-return",
            "/api/payments/vnpay/callback"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        WebView webView = new WebView(this);
        setContentView(webView);

        WebSettings ws = webView.getSettings();
        ws.setJavaScriptEnabled(true);
        ws.setDomStorageEnabled(true);
        webView.setWebChromeClient(new WebChromeClient());
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (url == null) return false;
                android.util.Log.d("VNPay", "Navigating to: " + url);
                for (String hint : RETURN_URL_HINTS) {
                    if (url.contains(hint)) {
                        handleReturnUrl(url);
                        return true;
                    }
                }
                return false;
            }
        });

        String url = getIntent().getStringExtra("paymentUrl");
        if (url != null) webView.loadUrl(url);
    }

    private void handleReturnUrl(String url) {
        Uri uri = Uri.parse(url);
        String responseCode = uri.getQueryParameter("vnp_ResponseCode");
        boolean isSuccess = "00".equals(responseCode);
        relayCallbackToBackend(uri);
        Intent intent = new Intent(this, isSuccess ? CheckoutSuccessActivity.class : CheckoutFailActivity.class);
        intent.putExtra("vnp_return_url", url);
        intent.putExtra("vnp_response_code", responseCode);
        startActivity(intent);
        finish();
    }

    private void relayCallbackToBackend(Uri returnUri) {
        try {
            String encoded = returnUri.getEncodedQuery();
            if (encoded == null || encoded.isEmpty()) {
                android.util.Log.w("VNPay", "Encoded query is empty; skip relay");
                return;
            }
            // Basic sanity: ensure this looks like VNPay params
            if (returnUri.getQueryParameter("vnp_TxnRef") == null && returnUri.getQueryParameter("vnp_ResponseCode") == null) {
                android.util.Log.w("VNPay", "Not VNPay callback; skip relay");
                return;
            }
            String finalUrl = ApiConfig.endpoint("/api/Payments/vnpay/callback") + "?" + encoded;
            android.util.Log.d("VNPay", "Relaying callback to: " + finalUrl);
            OkHttpClient client = new OkHttpClient();
            Request req = new Request.Builder().url(finalUrl).get().build();
            client.newCall(req).enqueue(new Callback() {
                @Override public void onFailure(Call call, IOException e) { android.util.Log.e("VNPay", "Relay failed", e); }
                @Override public void onResponse(Call call, Response response) throws IOException { android.util.Log.d("VNPay", "Relay response code=" + response.code()); response.close(); }
            });
        } catch (Exception e) {
            android.util.Log.e("VNPay", "Relay exception", e);
        }
    }
}

 



