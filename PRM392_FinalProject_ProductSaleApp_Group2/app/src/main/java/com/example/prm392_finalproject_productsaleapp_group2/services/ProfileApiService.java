package com.example.prm392_finalproject_productsaleapp_group2.services;

import com.example.prm392_finalproject_productsaleapp_group2.models.FilterResponse;
import com.example.prm392_finalproject_productsaleapp_group2.models.UserResponse;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ProfileApiService {
    
    @GET("api/Users/{userId}")
    Call<UserResponse> getUserInfo(
            @Header("Authorization") String token,
            @Path("userId") int userId
    );

    @Multipart
    @PUT("api/Users/{userId}")
    Call<UserResponse> updateUserProfile(
            @Header("Authorization") String token,
            @Path("userId") int userId,
            @Part("Email") RequestBody email,
            @Part("PhoneNumber") RequestBody phoneNumber,
            @Part("Address") RequestBody address,
            @Part MultipartBody.Part avatarFile
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
