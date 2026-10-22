package com.ecommerce.sb_ecomm.controller;

import com.ecommerce.sb_ecomm.config.AppConstants;
import com.ecommerce.sb_ecomm.model.Category;
import com.ecommerce.sb_ecomm.payload.CategoryDTO;
import com.ecommerce.sb_ecomm.payload.CategoryResponse;
import com.ecommerce.sb_ecomm.service.CategoryService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;

@RestController
public class CategoryController {
    private CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping("/api/public/categories")
    public ResponseEntity<CategoryResponse> getAllCategories(@RequestParam(value = "pageNumber", defaultValue = AppConstants.PAGE_NUMBER, required = false) Integer pageNumber,
                                                             @RequestParam(value = "pageSize", defaultValue = AppConstants.PAGE_SIZE, required = false) Integer pageSize,
                                                             @RequestParam(value = "sortBy" , defaultValue = AppConstants.SORT_CATEGORY_BY,  required = false) String sortBy ,
                                                             @RequestParam(value = "sortOrder" , defaultValue = AppConstants.SORT_DIRECTION, required = false) String sortOrder) {
        return new ResponseEntity<>(categoryService.getAllCategories(pageNumber, pageSize, sortBy, sortOrder), HttpStatus.OK);
    }

    @PostMapping("/api/public/categories")
    public ResponseEntity<CategoryDTO> createCategory(@Valid @RequestBody CategoryDTO categoryDto) {
        CategoryDTO categoryDTO = categoryService.createCategory(categoryDto);
        return new ResponseEntity<>(categoryDTO, HttpStatus.CREATED);
    }

    @DeleteMapping("/api/admin/categories/{categoryId}")
    public ResponseEntity<CategoryDTO> deleteCategory(@PathVariable Long categoryId) {
        CategoryDTO savedCategory = categoryService.deleteCategory(categoryId);
        return new ResponseEntity<>(savedCategory, HttpStatus.OK);
    }

    @PutMapping("/api/admin/categories/{categoryId}")
    public ResponseEntity<CategoryDTO> updateCategory(@RequestBody CategoryDTO categoryDTO, @PathVariable Long categoryId) {

        CategoryDTO categoryDTO1 = categoryService.updateCategory(categoryDTO, categoryId);
        return new ResponseEntity<>(categoryDTO1, HttpStatus.OK);
    }

    @GetMapping("/echo")
    public ResponseEntity<String> getEcho(@RequestParam("message") String message) {
        return new ResponseEntity<>(message, HttpStatus.OK);
    }

}
