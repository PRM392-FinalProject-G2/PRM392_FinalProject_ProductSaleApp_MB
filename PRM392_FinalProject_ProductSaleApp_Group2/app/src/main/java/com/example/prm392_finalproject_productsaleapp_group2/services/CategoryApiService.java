package com.example.prm392_finalproject_productsaleapp_group2.services;

import com.example.prm392_finalproject_productsaleapp_group2.models.Category;
import com.example.prm392_finalproject_productsaleapp_group2.models.FilterResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;

public interface CategoryApiService {

    @GET("api/Categories/filter")
    Call<FilterResponse<Category>> getCategories(
            @Header("Authorization") String token);
}
