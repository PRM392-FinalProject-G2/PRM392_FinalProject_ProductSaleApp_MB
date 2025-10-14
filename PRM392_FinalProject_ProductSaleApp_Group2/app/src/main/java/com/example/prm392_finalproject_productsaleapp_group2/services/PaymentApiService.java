package com.example.prm392_finalproject_productsaleapp_group2.services;

import com.example.prm392_finalproject_productsaleapp_group2.models.PaymentCreateResponse;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.POST;

public interface PaymentApiService {

    class CreateOrderRequest {
        public int cartId;
        public int userId;
        public String paymentMethod;
        public String billingAddress;
        public String orderStatus;
        public Integer voucherId;
    }

    @POST("api/Payments/vnpay/create-order")
    Call<PaymentCreateResponse> createVnpayOrder(
            @Header("Authorization") String token,
            @Body CreateOrderRequest request
    );
}


