package com.ecommerce.sb_ecomm.service;

import com.ecommerce.sb_ecomm.model.Category;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
@Service
public class CategoryServiceImpl implements CategoryService {
    private List<Category> categoryList = new ArrayList<>();
    private Long nextId=1L;
    @Override
    public List<Category> getAllCategories() {
        return categoryList;
    }

    @Override
    public void createCategory(Category category) {
        category.setCategoryId(nextId++);
        categoryList.add(category);
    }

    @Override
    public void updateCategory(Category category) {

    }

    @Override
    public String deleteCategory(Long categoryId) {
        Category category=categoryList.stream()
                .filter(c->c.getCategoryId().equals(categoryId))
                .findFirst().orElseThrow(()-> new ResponseStatusException(HttpStatus.NOT_FOUND, "Resource not found"));
        categoryList.remove(category);

        if(category==null)
            return "Category Not Found";
        return "Category with categoryId:"+category.getCategoryId()+" has been deleted successfully";
    }


}
