package com.example.prm392_finalproject_productsaleapp_group2.chat;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.prm392_finalproject_productsaleapp_group2.R;
import com.example.prm392_finalproject_productsaleapp_group2.auth.SessionManager;
import com.example.prm392_finalproject_productsaleapp_group2.chat.models.ConversationItem;
import com.example.prm392_finalproject_productsaleapp_group2.chat.models.ChatHistoryResponse;
import com.example.prm392_finalproject_productsaleapp_group2.chat.models.ChatMessage;
import com.example.prm392_finalproject_productsaleapp_group2.chat.models.ChatMessageSendRequest;
import com.example.prm392_finalproject_productsaleapp_group2.chat.models.ChatMessageSendResponse;
import com.example.prm392_finalproject_productsaleapp_group2.net.ChatApiClient;
import com.example.prm392_finalproject_productsaleapp_group2.utils.NavigationBarUtil;
import com.bumptech.glide.Glide;
import android.util.Log;
import com.example.prm392_finalproject_productsaleapp_group2.chat.signalr.SignalRManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.util.List;
import java.util.Collections;
import java.time.Instant;
import java.time.format.DateTimeParseException;

public class ChatActivity extends AppCompatActivity {

    private RecyclerView rvMessages;
    private ChatMessageAdapter adapter;
    private EditText etMessage;
    private ImageButton btnSend;
    private TextView tvTitle;
    private ImageView imgAvatar;

    private int currentUserId;
    private int otherUserId; // TODO: pass via intent when navigating to chat
    private String otherUserName;
    private String otherUserAvatar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        getWindow().setStatusBarColor(android.graphics.Color.TRANSPARENT);
        setContentView(R.layout.activity_chat);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0);
            return WindowInsetsCompat.CONSUMED;
        });

        NavigationBarUtil.setupNavigationBar(this);
        NavigationBarUtil.setActiveNavigationButton(this, "chat");

        SessionManager sessionManager = new SessionManager(this);
        currentUserId = sessionManager.getUserId();
        otherUserId = getIntent().getIntExtra("otherUserId", 0);
        Log.d("ChatActivity", "currentUserId=" + currentUserId + ", otherUserId(fromIntent)=" + otherUserId);

        rvMessages = findViewById(R.id.rv_messages);
        etMessage = findViewById(R.id.et_message);
        btnSend = findViewById(R.id.btn_send);
        tvTitle = findViewById(R.id.tv_title);
        imgAvatar = findViewById(R.id.img_avatar);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        rvMessages.setLayoutManager(layoutManager);
        adapter = new ChatMessageAdapter(currentUserId);
        rvMessages.setAdapter(adapter);

        btnSend.setOnClickListener(v -> trySend());
        etMessage.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                trySend();
                return true;
            }
            return false;
        });

        // Connect SignalR and register user
        SignalRManager.getInstance().connect(currentUserId,
                message -> runOnUiThread(() -> onReceiveRealtimeMessage(message)),
                message -> runOnUiThread(() -> onMessageSentAck(message)),
                error -> runOnUiThread(() -> Toast.makeText(this, String.valueOf(error), Toast.LENGTH_SHORT).show()));

        if (otherUserId > 0) {
            // Try populate header from conversations
            populateHeaderFromConversationsOrFallback(otherUserId);
            loadHistory(1, 50);
        } else {
            // If no target selected, load conversations and auto-pick the latest one
            loadFirstConversationAndHistory();
        }
    }

    private void loadFirstConversationAndHistory() {
        ChatApiClient.getInstance().getApiService().getConversations(currentUserId)
                .enqueue(new Callback<java.util.List<com.example.prm392_finalproject_productsaleapp_group2.chat.models.ConversationItem>>() {
                    @Override
                    public void onResponse(Call<java.util.List<com.example.prm392_finalproject_productsaleapp_group2.chat.models.ConversationItem>> call, Response<java.util.List<com.example.prm392_finalproject_productsaleapp_group2.chat.models.ConversationItem>> response) {
                        if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                            ConversationItem first = response.body().get(0);
                            otherUserId = first.getUserId();
                            setHeader(first.getUserName(), first.getUserAvatar());
                            loadHistory(1, 50);
                        } else {
                            Toast.makeText(ChatActivity.this, "Không có cuộc hội thoại nào", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<java.util.List<com.example.prm392_finalproject_productsaleapp_group2.chat.models.ConversationItem>> call, Throwable t) {
                        Toast.makeText(ChatActivity.this, "Lỗi tải danh sách hội thoại", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void populateHeaderFromConversationsOrFallback(int targetUserId) {
        ChatApiClient.getInstance().getApiService().getConversations(currentUserId)
                .enqueue(new Callback<java.util.List<ConversationItem>>() {
                    @Override
                    public void onResponse(Call<java.util.List<ConversationItem>> call, Response<java.util.List<ConversationItem>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            for (ConversationItem item : response.body()) {
                                if (item.getUserId() == targetUserId) {
                                    setHeader(item.getUserName(), item.getUserAvatar());
                                    return;
                                }
                            }
                        }
                        setHeader("Chat", null);
                    }

                    @Override
                    public void onFailure(Call<java.util.List<ConversationItem>> call, Throwable t) {
                        setHeader("Chat", null);
                    }
                });
    }

    private void setHeader(String name, String avatarUrl) {
        otherUserName = name;
        otherUserAvatar = avatarUrl;
        if (tvTitle != null) tvTitle.setText(name != null ? name : "Chat");
        if (imgAvatar != null) {
            if (!TextUtils.isEmpty(avatarUrl)) {
                Glide.with(this).load(avatarUrl).placeholder(R.drawable.ic_person_circle).into(imgAvatar);
            } else {
                imgAvatar.setImageResource(R.drawable.ic_person_circle);
            }
        }
        Log.d("ChatActivity", "Header set => otherUserId=" + otherUserId + ", otherUserName=" + otherUserName);
    }

    private void loadHistory(int page, int size) {
        ChatApiClient.getInstance().getApiService()
                .getHistory(currentUserId, otherUserId, page, size)
                .enqueue(new Callback<ChatHistoryResponse>() {
                    @Override
                    public void onResponse(Call<ChatHistoryResponse> call, Response<ChatHistoryResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            List<ChatMessage> messages = response.body().getItems();
                            if (messages != null) {
                                // Sort ascending by sentAt so newest is at the bottom
                                try {
                                    Collections.sort(messages, (a, b) -> compareSentAt(a.getSentAt(), b.getSentAt()));
                                } catch (Exception ignored) { }
                            }
                            adapter.setMessages(messages);
                            rvMessages.scrollToPosition(Math.max((messages != null ? messages.size() : 0) - 1, 0));
                        } else {
                            Toast.makeText(ChatActivity.this, "Không tải được lịch sử", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<ChatHistoryResponse> call, Throwable t) {
                        Toast.makeText(ChatActivity.this, "Lỗi mạng khi tải lịch sử", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private int compareSentAt(String a, String b) {
        if (a == null && b == null) return 0;
        if (a == null) return -1;
        if (b == null) return 1;
        // Prefer ISO-8601 parsing; fallback to lexicographical compare
        try {
            Instant ia = Instant.parse(a);
            Instant ib = Instant.parse(b);
            return ia.compareTo(ib);
        } catch (DateTimeParseException ex) {
            return a.compareTo(b);
        }
    }

    private void trySend() {
        String text = etMessage.getText().toString().trim();
        if (TextUtils.isEmpty(text)) return;
        if (currentUserId <= 0) {
            Toast.makeText(this, "Bạn chưa đăng nhập", Toast.LENGTH_SHORT).show();
            return;
        }
        if (otherUserId <= 0) {
            Toast.makeText(this, "Chưa xác định người nhận", Toast.LENGTH_SHORT).show();
            return;
        }

        Log.d("ChatActivity", "Sending message from " + currentUserId + " to " + otherUserId + ": " + text);
        btnSend.setEnabled(false);
        SignalRManager.getInstance().sendMessage(currentUserId, otherUserId, text);
    }

    private void onReceiveRealtimeMessage(Object payload) {
        // payload is generic map-like object; for simplicity reload history or append later
        loadHistory(1, 50);
    }

    private void onMessageSentAck(Object payload) {
        etMessage.setText("");
        btnSend.setEnabled(true);
        loadHistory(1, 50);
    }

    @Override
    public void onBackPressed() {
        NavigationBarUtil.finishActivityWithoutAnimation(this);
    }
}