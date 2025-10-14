package com.example.prm392_finalproject_productsaleapp_group2.services;

import com.example.prm392_finalproject_productsaleapp_group2.models.Brand;
import com.example.prm392_finalproject_productsaleapp_group2.models.FilterResponse;
import com.example.prm392_finalproject_productsaleapp_group2.models.Product;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Query;

public interface BrandApiService {
    @GET("api/Brands/filter")
    Call<FilterResponse<Brand>> getBrands(@Header("Authorization") String token,
                                          @Query("PageNumber") int pageNumber,
                                          @Query("PageSize") int pageSize);


}
