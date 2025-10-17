package com.example.prm392_finalproject_productsaleapp_group2.chat.models;

public class ChatMessageSendRequest {
    private int senderId;
    private int receiverId;
    private String message;

    public ChatMessageSendRequest(int senderId, int receiverId, String message) {
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.message = message;
    }

    public int getSenderId() { return senderId; }
    public int getReceiverId() { return receiverId; }
    public String getMessage() { return message; }
}


