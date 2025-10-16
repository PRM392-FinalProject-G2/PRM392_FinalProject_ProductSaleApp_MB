package com.example.prm392_finalproject_productsaleapp_group2.chat.models;

import com.google.gson.annotations.SerializedName;

public class ChatMessageSendResponse {
    @SerializedName("success")
    private boolean success;
    @SerializedName("chatMessageId")
    private int chatMessageId;
    @SerializedName("message")
    private String message;

    public boolean isSuccess() { return success; }
    public int getChatMessageId() { return chatMessageId; }
    public String getMessage() { return message; }
}


