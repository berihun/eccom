package com.ecommerce.sb_ecomm.com.ecommerce.repository;

import com.ecommerce.sb_ecomm.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    Category findCategoriesByCategoryName(String categoryName);
}
