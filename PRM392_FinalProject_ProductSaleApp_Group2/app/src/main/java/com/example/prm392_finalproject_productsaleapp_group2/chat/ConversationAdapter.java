package com.example.prm392_finalproject_productsaleapp_group2.chat;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.prm392_finalproject_productsaleapp_group2.R;
import com.example.prm392_finalproject_productsaleapp_group2.chat.models.ConversationItem;

public class ConversationAdapter extends ListAdapter<ConversationItem, ConversationAdapter.ViewHolder> {

    public interface OnConversationClickListener {
        void onConversationClick(ConversationItem item);
    }

    private final OnConversationClickListener clickListener;

    public ConversationAdapter(OnConversationClickListener clickListener) {
        super(DIFF_CALLBACK);
        this.clickListener = clickListener;
    }

    private static final DiffUtil.ItemCallback<ConversationItem> DIFF_CALLBACK = new DiffUtil.ItemCallback<ConversationItem>() {
        @Override
        public boolean areItemsTheSame(@NonNull ConversationItem oldItem, @NonNull ConversationItem newItem) {
            return oldItem.getUserId() == newItem.getUserId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull ConversationItem oldItem, @NonNull ConversationItem newItem) {
            return oldItem.equals(newItem);
        }
    };

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_conversation, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ConversationItem item = getItem(position);
        holder.bind(item, clickListener);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final ImageView avatar;
        private final TextView name;
        private final TextView lastMessage;
        private final TextView time;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            avatar = itemView.findViewById(R.id.img_avatar);
            name = itemView.findViewById(R.id.tv_name);
            lastMessage = itemView.findViewById(R.id.tv_last_message);
            time = itemView.findViewById(R.id.tv_time);
        }

        void bind(ConversationItem item, OnConversationClickListener listener) {
            name.setText(item.getUserName());
            lastMessage.setText(item.getLastMessage() != null ? item.getLastMessage() : "");
            time.setText(item.getLastMessageTime() != null ? item.getLastMessageTime() : "");

            if (item.getUserAvatar() != null && !item.getUserAvatar().isEmpty()) {
                Glide.with(avatar.getContext()).load(item.getUserAvatar()).placeholder(R.drawable.ic_person_circle).into(avatar);
            } else {
                avatar.setImageResource(R.drawable.ic_person_circle);
            }

            itemView.setOnClickListener(v -> {
                if (listener != null) listener.onConversationClick(item);
            });
        }
    }
}


