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

    class CalculateAmountRequest {
        public int cartId;
        public int userId;
        public Integer voucherId;
    }

    class CreatePaymentRequest {
        public int cartId;
        public int userId;
        public Integer voucherId;
        public long finalAmount;
        public String paymentMethod;
        public String billingAddress;
    }

    @POST("api/Payments/vnpay/create-order")
    Call<PaymentCreateResponse> createVnpayOrder(
            @Header("Authorization") String token,
            @Body CreateOrderRequest request
    );

    @POST("api/Payments/vnpay/calculate-amount")
    Call<PaymentCreateResponse> calculateVnpayAmount(
            @Header("Authorization") String token,
            @Body CalculateAmountRequest request
    );

    @POST("api/Payments/vnpay/create-payment")
    Call<PaymentCreateResponse> createVnpayPayment(
            @Header("Authorization") String token,
            @Body CreatePaymentRequest request
    );
}


