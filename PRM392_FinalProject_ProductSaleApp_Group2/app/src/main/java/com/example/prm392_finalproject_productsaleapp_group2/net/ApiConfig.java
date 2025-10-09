package com.example.prm392_finalproject_productsaleapp_group2.net;

public final class ApiConfig {
    public static final String BASE_URL = "http://10.0.2.2:8080"; // LanIP của Hậu

    private ApiConfig() {}

    public static String endpoint(String path) {
        if (path == null) return BASE_URL;
        return BASE_URL + path;
    }
}