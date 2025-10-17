package com.example.prm392_finalproject_productsaleapp_group2.chat;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.prm392_finalproject_productsaleapp_group2.R;
import com.example.prm392_finalproject_productsaleapp_group2.chat.models.ChatMessage;

import java.util.ArrayList;
import java.util.List;

public class ChatMessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int TYPE_SENT = 1;
    private static final int TYPE_RECEIVED = 2;

    private final int currentUserId;
    private final List<ChatMessage> messages = new ArrayList<>();

    public ChatMessageAdapter(int currentUserId) {
        this.currentUserId = currentUserId;
    }

    public void setMessages(List<ChatMessage> list) {
        messages.clear();
        if (list != null) messages.addAll(list);
        notifyDataSetChanged();
    }

    public void addMessage(ChatMessage msg) {
        messages.add(msg);
        notifyItemInserted(messages.size() - 1);
    }

    @Override
    public int getItemViewType(int position) {
        ChatMessage m = messages.get(position);
        return m.getSenderId() == currentUserId ? TYPE_SENT : TYPE_RECEIVED;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == TYPE_SENT) {
            View v = inflater.inflate(R.layout.item_chat_message_sent, parent, false);
            return new SentVH(v);
        } else {
            View v = inflater.inflate(R.layout.item_chat_message_received, parent, false);
            return new ReceivedVH(v);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ChatMessage m = messages.get(position);
        if (holder instanceof SentVH) {
            ((SentVH) holder).message.setText(m.getMessage());
            ((SentVH) holder).time.setText(m.getSentAt());
        } else if (holder instanceof ReceivedVH) {
            ((ReceivedVH) holder).message.setText(m.getMessage());
            ((ReceivedVH) holder).time.setText(m.getSentAt());
        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    static class SentVH extends RecyclerView.ViewHolder {
        TextView message; TextView time;
        SentVH(@NonNull View itemView) {
            super(itemView);
            message = itemView.findViewById(R.id.tv_message);
            time = itemView.findViewById(R.id.tv_time);
        }
    }

    static class ReceivedVH extends RecyclerView.ViewHolder {
        TextView message; TextView time;
        ReceivedVH(@NonNull View itemView) {
            super(itemView);
            message = itemView.findViewById(R.id.tv_message);
            time = itemView.findViewById(R.id.tv_time);
        }
    }
}


