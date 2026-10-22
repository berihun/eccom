package com.ecommerce.sb_ecomm.com.ecommerce.repository;

import com.ecommerce.sb_ecomm.model.Category;
import com.ecommerce.sb_ecomm.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {
    Page<Product> findByCategoryOrderByPriceAsc(Category category, Pageable pageable);

    Page<Product> findProductByProductNameLikeIgnoreCase(String keyword , Pageable pageable);
}
