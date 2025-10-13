package com.example.prm392_finalproject_productsaleapp_group2.services;

import com.example.prm392_finalproject_productsaleapp_group2.models.ApiResponse;
import com.example.prm392_finalproject_productsaleapp_group2.models.ChangePasswordRequest;
import com.example.prm392_finalproject_productsaleapp_group2.models.OtpRequest;
import com.example.prm392_finalproject_productsaleapp_group2.models.VerifyOtpRequest;
import com.example.prm392_finalproject_productsaleapp_group2.models.VerifyOtpResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface AuthApiService {
    
    @POST("api/Authentication/request-otp")
    Call<ApiResponse> requestOtp(@Body OtpRequest otpRequest);

    @POST("api/Authentication/verify-otp")
    Call<VerifyOtpResponse> verifyOtp(@Body VerifyOtpRequest verifyOtpRequest);

    @POST("api/Authentication/change-password")
    Call<ApiResponse> changePassword(@Body ChangePasswordRequest changePasswordRequest);
}

