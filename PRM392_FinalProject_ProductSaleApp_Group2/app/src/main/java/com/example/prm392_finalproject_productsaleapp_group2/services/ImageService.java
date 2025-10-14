package com.example.prm392_finalproject_productsaleapp_group2.services;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.widget.ImageView;

import com.example.prm392_finalproject_productsaleapp_group2.R;

import java.io.InputStream;
import java.net.URL;

public class ImageService {

    public static void loadImage(String imageUrl, ImageView imageView) {
        new LoadImageTask(imageView).execute(imageUrl);
    }

    public static void loadImage(String imageUrl, ImageView imageView, int defaultResourceId) {
        new LoadImageTask(imageView, defaultResourceId).execute(imageUrl);
    }

    private static class LoadImageTask extends AsyncTask<String, Void, Bitmap> {
        private final ImageView imageView;
        private final int defaultResourceId;

        LoadImageTask(ImageView imageView) {
            this.imageView = imageView;
            this.defaultResourceId = R.drawable.ic_launcher_foreground;
        }

        LoadImageTask(ImageView imageView, int defaultResourceId) {
            this.imageView = imageView;
            this.defaultResourceId = defaultResourceId;
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
            if (result != null) {
                imageView.setImageBitmap(result);
            } else {
                imageView.setImageResource(defaultResourceId);
            }
        }
    }
}
