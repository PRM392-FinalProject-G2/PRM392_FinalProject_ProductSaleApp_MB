package com.example.prm392_finalproject_productsaleapp_group2.chat.models;

import com.google.gson.annotations.SerializedName;

public class ChatMessage {
    @SerializedName("chatMessageId")
    private int chatMessageId;
    @SerializedName("senderId")
    private int senderId;
    @SerializedName("receiverId")
    private int receiverId;
    @SerializedName("message")
    private String message;
    @SerializedName("sentAt")
    private String sentAt;
    @SerializedName("senderName")
    private String senderName;
    @SerializedName("senderAvatar")
    private String senderAvatar;
    @SerializedName("receiverName")
    private String receiverName;
    @SerializedName("receiverAvatar")
    private String receiverAvatar;

    public int getChatMessageId() { return chatMessageId; }
    public int getSenderId() { return senderId; }
    public int getReceiverId() { return receiverId; }
    public String getMessage() { return message; }
    public String getSentAt() { return sentAt; }
    public String getSenderName() { return senderName; }
    public String getSenderAvatar() { return senderAvatar; }
    public String getReceiverName() { return receiverName; }
    public String getReceiverAvatar() { return receiverAvatar; }
}


