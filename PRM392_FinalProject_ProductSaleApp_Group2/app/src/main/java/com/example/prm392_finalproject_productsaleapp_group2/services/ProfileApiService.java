package com.example.prm392_finalproject_productsaleapp_group2.services;

import com.example.prm392_finalproject_productsaleapp_group2.models.FilterResponse;
import com.example.prm392_finalproject_productsaleapp_group2.models.UserResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ProfileApiService {
    
    @GET("api/Users/{userId}")
    Call<UserResponse> getUserInfo(
            @Header("Authorization") String token,
            @Path("userId") int userId
    );

    @GET("api/Orders/filter")
    Call<FilterResponse<Object>> getOrdersCount(
            @Header("Authorization") String token,
            @Query("UserId") int userId
    );

    @GET("api/Wishlists/filter")
    Call<FilterResponse<Object>> getWishlistCount(
            @Header("Authorization") String token,
            @Query("UserId") int userId
    );

    @GET("api/UserVouchers/filter")
    Call<FilterResponse<Object>> getVouchersCount(
            @Header("Authorization") String token,
            @Query("UserId") int userId
    );
}
