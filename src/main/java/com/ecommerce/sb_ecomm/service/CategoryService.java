package com.ecommerce.sb_ecomm.service;

import com.ecommerce.sb_ecomm.payload.CategoryDTO;
import com.ecommerce.sb_ecomm.payload.CategoryResponse;

public interface CategoryService {
    CategoryResponse getAllCategories(Integer pageNumber, Integer pageSize, String sortBy, String sortOrder);
    CategoryDTO createCategory(CategoryDTO categoryDTO);
    CategoryDTO updateCategory(CategoryDTO categoryDTO, Long id);
    CategoryDTO deleteCategory(Long categoryId);
}
