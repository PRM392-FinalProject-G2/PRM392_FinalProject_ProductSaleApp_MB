package com.example.prm392_finalproject_productsaleapp_group2.chat.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class ChatHistoryResponse {
    @SerializedName("pageNumber")
    private int pageNumber;
    @SerializedName("pageSize")
    private int pageSize;
    @SerializedName("totalItems")
    private int totalItems;
    @SerializedName("totalPages")
    private int totalPages;
    @SerializedName("items")
    private List<ChatMessage> items;

    public int getPageNumber() { return pageNumber; }
    public int getPageSize() { return pageSize; }
    public int getTotalItems() { return totalItems; }
    public int getTotalPages() { return totalPages; }
    public List<ChatMessage> getItems() { return items; }
}


