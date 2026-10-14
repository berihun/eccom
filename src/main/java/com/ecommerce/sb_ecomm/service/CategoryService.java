package com.ecommerce.sb_ecomm.service;

import com.ecommerce.sb_ecomm.model.Category;

import java.util.List;

public interface CategoryService {
    List<Category> getAllCategories();
    void createCategory(Category category);
    void updateCategory(Category category);
    String deleteCategory(Long categoryId);
}
