package com.example.prm392_finalproject_productsaleapp_group2.net;

public final class ApiConfig {
    // For emulator use: http://10.0.2.2:8080
    // For physical Hậu's device use: http://192.168.1.8:8080
    // For physical huy's device use: http://192.168.1.3:8080
    public static final String BASE_URL = "https://prm392-finalproject-productsaleapp-be.onrender.com"; // Your computer's WiFi IP

    private ApiConfig() {}

    public static String endpoint(String path) {
        if (path == null) return BASE_URL;
        return BASE_URL + path;
    }
}