package com.example.prm392_finalproject_productsaleapp_group2.services;

import com.example.prm392_finalproject_productsaleapp_group2.models.UserVoucher;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Path;

public interface VoucherApiService {

    @GET("api/UserVouchers/user/{userId}/active-valid")
    Call<java.util.List<UserVoucher>> getUserVouchers(
            @Header("Authorization") String token,
            @Path("userId") int userId
    );
}







