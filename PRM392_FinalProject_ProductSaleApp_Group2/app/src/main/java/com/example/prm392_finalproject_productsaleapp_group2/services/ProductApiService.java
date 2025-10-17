package com.example.prm392_finalproject_productsaleapp_group2.services;

import com.example.prm392_finalproject_productsaleapp_group2.models.FilterResponse;
import com.example.prm392_finalproject_productsaleapp_group2.models.Product;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ProductApiService {

    @GET("api/Products/filter")
    Call<FilterResponse<Product>> getProducts(
            @Header("Authorization") String token,
            @Query("PageNumber") int pageNumber,
            @Query("PageSize") int pageSize);

    @GET("api/Products/filter")
    Call<FilterResponse<Product>> getProductsByCategory(
            @Header("Authorization") String token,
            @Query("CategoryId") int categoryId,
            @Query("PageNumber") int pageNumber,
            @Query("PageSize") int pageSize);

    @GET("api/Products/{productId}")
    Call<Product> getProductById(
            @Header("Authorization") String token,
            @Path("productId") int productId);

    @GET("api/Products/filter")
    Call<FilterResponse<Product>> getProducts(
            @Header("Authorization") String token,
            @Query("categoryIds") List<Integer> categoryIds,
            @Query("search") String search,
            @Query("brandIds") List<Integer> brandIds,
            @Query("minPrice") Double minPrice,
            @Query("maxPrice") Double maxPrice,
            @Query("averageRating") Double averageRating,
            @Query("sortBy") String sortBy,
            @Query("pageNumber") int pageNumber,
            @Query("pageSize") int pageSize
    );

}
