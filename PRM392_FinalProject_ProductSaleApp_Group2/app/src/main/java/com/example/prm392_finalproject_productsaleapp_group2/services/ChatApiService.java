package com.example.prm392_finalproject_productsaleapp_group2.services;

import com.example.prm392_finalproject_productsaleapp_group2.chat.models.ChatHistoryResponse;
import com.example.prm392_finalproject_productsaleapp_group2.chat.models.ChatMessageSendRequest;
import com.example.prm392_finalproject_productsaleapp_group2.chat.models.ChatMessageSendResponse;
import com.example.prm392_finalproject_productsaleapp_group2.chat.models.ConversationItem;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ChatApiService {

    // GET /api/ChatRealtime/conversations/{userId}
    @GET("/api/ChatRealtime/conversations/{userId}")
    Call<List<ConversationItem>> getConversations(@Path("userId") int userId);

    // GET /api/ChatRealtime/history
    @GET("/api/ChatRealtime/history")
    Call<ChatHistoryResponse> getHistory(
            @Query("userId") int userId,
            @Query("otherUserId") int otherUserId,
            @Query("pageNumber") int pageNumber,
            @Query("pageSize") int pageSize
    );

    // POST /api/ChatRealtime/send
    @POST("/api/ChatRealtime/send")
    Call<ChatMessageSendResponse> sendMessage(@Body ChatMessageSendRequest request);
}


