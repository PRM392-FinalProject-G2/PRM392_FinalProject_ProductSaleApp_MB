package com.example.prm392_finalproject_productsaleapp_group2.models;

import java.io.Serializable;
import java.util.List;

public class ProductFilterBM implements Serializable {
    public Integer ProductId;
    public String Search;
    public List<Integer> CategoryIds;
    public List<Integer> BrandIds;
    public Double MinPrice;
    public Double MaxPrice;
    public Double AverageRating;
    public String SortBy;
}
