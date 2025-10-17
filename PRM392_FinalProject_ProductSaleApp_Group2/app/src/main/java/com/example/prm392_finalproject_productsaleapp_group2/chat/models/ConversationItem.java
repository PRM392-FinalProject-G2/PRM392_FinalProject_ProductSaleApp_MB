package com.example.prm392_finalproject_productsaleapp_group2.chat.models;

import com.google.gson.annotations.SerializedName;

public class ConversationItem {
    @SerializedName("userId")
    private int userId;

    @SerializedName("userName")
    private String userName;

    @SerializedName("userAvatar")
    private String userAvatar;

    @SerializedName("lastMessage")
    private String lastMessage;

    @SerializedName("lastMessageTime")
    private String lastMessageTime;

    @SerializedName("unreadCount")
    private int unreadCount;

    @SerializedName("isOnline")
    private boolean isOnline;

    public int getUserId() { return userId; }
    public String getUserName() { return userName; }
    public String getUserAvatar() { return userAvatar; }
    public String getLastMessage() { return lastMessage; }
    public String getLastMessageTime() { return lastMessageTime; }
    public int getUnreadCount() { return unreadCount; }
    public boolean isOnline() { return isOnline; }
}


