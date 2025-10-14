package com.example.prm392_finalproject_productsaleapp_group2.product;

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

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class ProductReviewAdapter extends RecyclerView.Adapter<ProductReviewAdapter.ReviewViewHolder> {

    private final Context context;
    private final List<JSONObject> reviewList;

    public ProductReviewAdapter(Context context, List<JSONObject> reviewList) {
        this.context = context;
        this.reviewList = reviewList;
    }

    @NonNull
    @Override
    public ReviewViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_review, parent, false);
        return new ReviewViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReviewViewHolder holder, int position) {
        JSONObject review = reviewList.get(position);
        try {
            holder.tvUsername.setText(review.getString("username"));
            holder.tvComment.setText(review.getString("comment"));

            int rating = review.getInt("rating");
            holder.tvRating.setText(new String(new char[rating]).replace("\0", "★"));

            String createdAt = review.getString("createdAt").substring(0, 10);
            holder.tvCreatedAt.setText(createdAt);

            Glide.with(context)
                    .load(review.getString("userAvatarUrl"))
                    .circleCrop()
                    .placeholder(R.drawable.ic_placeholder)
                    .into(holder.imgUserAvatar);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        return reviewList.size();
    }

    public static class ReviewViewHolder extends RecyclerView.ViewHolder {
        ImageView imgUserAvatar;
        TextView tvUsername, tvRating, tvComment, tvCreatedAt;

        public ReviewViewHolder(@NonNull View itemView) {
            super(itemView);
            imgUserAvatar = itemView.findViewById(R.id.imgUserAvatar);
            tvUsername = itemView.findViewById(R.id.tvUsername);
            tvRating = itemView.findViewById(R.id.tvRating);
            tvComment = itemView.findViewById(R.id.tvComment);
            tvCreatedAt = itemView.findViewById(R.id.tvCreatedAt);
        }
    }
}

