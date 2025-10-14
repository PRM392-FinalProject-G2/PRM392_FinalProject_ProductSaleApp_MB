package com.example.prm392_finalproject_productsaleapp_group2.services;

import com.example.prm392_finalproject_productsaleapp_group2.models.FilterResponse;
import com.example.prm392_finalproject_productsaleapp_group2.models.UserVoucher;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Query;

public interface VoucherApiService {

    @GET("api/UserVouchers/filter")
    Call<FilterResponse<UserVoucher>> getUserVouchers(
            @Header("Authorization") String token,
            @Query("UserId") int userId,
            @Query("IsUsed") boolean isUsed
    );
}





