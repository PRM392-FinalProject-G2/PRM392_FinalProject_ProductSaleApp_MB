package com.example.prm392_finalproject_productsaleapp_group2.services;

import com.example.prm392_finalproject_productsaleapp_group2.models.ApiResponse;
import com.example.prm392_finalproject_productsaleapp_group2.models.FilterResponse;
import com.example.prm392_finalproject_productsaleapp_group2.models.Wishlist;
import com.example.prm392_finalproject_productsaleapp_group2.models.WishlistMobile;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface WishlistApiService {
    
    @GET("api/Wishlists/mobile")
    Call<FilterResponse<WishlistMobile>> getWishlistByUser(
            @Header("Authorization") String token,
            @Query("UserId") int userId,
            @Query("PageNumber") int pageNumber,
            @Query("PageSize") int pageSize
    );
    
    @POST("api/Wishlists")
    Call<Wishlist> addToWishlist(
            @Header("Authorization") String token,
            @Body Wishlist wishlist
    );
    
    @DELETE("api/Wishlists/{wishlistId}")
    Call<ApiResponse> removeFromWishlist(
            @Header("Authorization") String token,
            @Path("wishlistId") int wishlistId
    );
    
    @GET("api/Wishlists/filter")
    Call<FilterResponse<Object>> getWishlistCount(
            @Header("Authorization") String token,
            @Query("UserId") int userId
    );
}