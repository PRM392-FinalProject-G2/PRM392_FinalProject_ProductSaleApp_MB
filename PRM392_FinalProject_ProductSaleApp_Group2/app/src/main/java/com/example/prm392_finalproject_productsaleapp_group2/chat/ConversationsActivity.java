package com.example.prm392_finalproject_productsaleapp_group2.chat;

import android.content.Intent;
import android.os.Bundle;
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
import com.example.prm392_finalproject_productsaleapp_group2.net.ChatApiClient;
import com.example.prm392_finalproject_productsaleapp_group2.utils.NavigationBarUtil;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ConversationsActivity extends AppCompatActivity implements ConversationAdapter.OnConversationClickListener {

    private RecyclerView recyclerView;
    private ConversationAdapter adapter;
    private int currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_conversations);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0);
            return WindowInsetsCompat.CONSUMED;
        });

        NavigationBarUtil.setupNavigationBar(this);
        NavigationBarUtil.setActiveNavigationButton(this, "chat");

        SessionManager sessionManager = new SessionManager(this);
        currentUserId = sessionManager.getUserId();

        recyclerView = findViewById(R.id.rv_conversations);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ConversationAdapter(this);
        recyclerView.setAdapter(adapter);

        loadConversations();
    }

    private void loadConversations() {
        ChatApiClient.getInstance().getApiService().getConversations(currentUserId)
                .enqueue(new Callback<List<ConversationItem>>() {
                    @Override
                    public void onResponse(Call<List<ConversationItem>> call, Response<List<ConversationItem>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            adapter.submitList(response.body());
                        } else {
                            Toast.makeText(ConversationsActivity.this, "Không tải được danh sách hội thoại", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<List<ConversationItem>> call, Throwable t) {
                        Toast.makeText(ConversationsActivity.this, "Lỗi mạng khi tải hội thoại", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public void onConversationClick(ConversationItem item) {
        Intent intent = new Intent(this, ChatActivity.class);
        intent.putExtra("otherUserId", item.getUserId());
        startActivity(intent);
        overridePendingTransition(0, 0);
    }
}


