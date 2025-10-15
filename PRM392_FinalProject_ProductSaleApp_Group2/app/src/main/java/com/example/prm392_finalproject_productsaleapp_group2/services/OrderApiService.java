package com.example.prm392_finalproject_productsaleapp_group2.services;

import com.example.prm392_finalproject_productsaleapp_group2.models.FilterResponse;
import com.example.prm392_finalproject_productsaleapp_group2.models.Order;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface OrderApiService {
    @GET("api/Orders/filter")
    Call<FilterResponse<Order>> filterOrders(
            @Header("Authorization") String token,
            @Query("OrderId") Integer orderId,
            @Query("UserId") Integer userId,
            @Query("CartId") Integer cartId,
            @Query("OrderStatus") String orderStatus,
            @Query("PageNumber") Integer pageNumber,
            @Query("PageSize") Integer pageSize
    );

    @GET("api/Orders/{orderId}")
    Call<Order> getOrderById(
            @Header("Authorization") String token,
            @Path("orderId") int orderId
    );
}
