package com.example.prm392_finalproject_productsaleapp_group2.services;

import com.example.prm392_finalproject_productsaleapp_group2.models.CartResponse;
import com.example.prm392_finalproject_productsaleapp_group2.models.CartItem;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface CartApiService {
    
    @GET("api/Carts/filter")
    Call<CartResponse> getCartItems(
            @Header("Authorization") String token,
            @Query("UserId") int userId,
            @Query("Status") String status
    );
    
    @PUT("api/CartItems/{cartItemId}")
    Call<CartItem> updateCartItem(
            @Header("Authorization") String token,
            @Path("cartItemId") int cartItemId,
            @Body CartItem cartItem
    );
    
    @DELETE("api/CartItems/{cartItemId}")
    Call<Void> deleteCartItem(
            @Header("Authorization") String token,
            @Path("cartItemId") int cartItemId
    );
    @POST("api/CartItems")
    Call<CartItem> addCartItem(
            @Header("Authorization") String token,
            @Body CartItem cartItem
    );
}

