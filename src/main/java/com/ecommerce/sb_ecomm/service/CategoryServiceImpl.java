package com.ecommerce.sb_ecomm.service;

import com.ecommerce.sb_ecomm.com.ecommerce.repository.CategoryRepository;
import com.ecommerce.sb_ecomm.exception.APIException;
import com.ecommerce.sb_ecomm.exception.ResourceNotFoundException;
import com.ecommerce.sb_ecomm.model.Category;
import com.ecommerce.sb_ecomm.payload.CategoryDTO;
import com.ecommerce.sb_ecomm.payload.CategoryResponse;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryServiceImpl implements CategoryService {
    private final CategoryRepository categoryRepository;
    private final ModelMapper modelMapper;

    public CategoryServiceImpl(CategoryRepository categoryRepository, ModelMapper modelMapper) {
        this.categoryRepository = categoryRepository;
        this.modelMapper = modelMapper;
    }

    @Override
    public CategoryResponse getAllCategories(Integer pageNumber, Integer pageSize, String sortBy, String sortOrder) {

        Sort sortByAndOrder = sortOrder.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(pageNumber - 1, pageSize, sortByAndOrder);
        Page<Category> categoryPage = categoryRepository.findAll(pageable);

        List<Category> categories =categoryPage.getContent();
        if (categories.isEmpty())
            throw new APIException("No category created till now !!!");

        List<CategoryDTO> categoryDTOS = categories.stream()
                .map(category -> modelMapper.map(category, CategoryDTO.class))
                .toList();

        CategoryResponse categoryResponse = new CategoryResponse();
        categoryResponse.setContent(categoryDTOS);
        categoryResponse.setPageNumber(categoryPage.getNumber());
        categoryResponse.setPageSize(categoryPage.getSize());
        categoryResponse.setTotalElements(categoryPage.getTotalElements());
        categoryResponse.setTotalPages(categoryPage.getTotalPages());
        categoryResponse.setLastPage(categoryPage.isLast());

        return categoryResponse;
    }

    @Override
    public CategoryDTO createCategory(CategoryDTO category) {
        Category savedCategory = categoryRepository.findCategoriesByCategoryName(category.getCategoryName());
        if (savedCategory != null) {
            throw new APIException("Category with the name " + category.getCategoryName() + " already exists    !!!");
        }
        Category category1 = modelMapper.map(category, Category.class);
        return modelMapper.map(categoryRepository.save(category1), CategoryDTO.class);
    }

    @Override
    public CategoryDTO updateCategory(CategoryDTO categoryDTO, Long categoryId) {
        Category cat = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "categoryId", categoryId));
//                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Request not found")));
        Category cat1 = modelMapper.map(categoryDTO, Category.class);
        cat1.setCategoryName(categoryDTO.getCategoryName());
        return modelMapper.map(categoryRepository.save(cat), CategoryDTO.class);


    }

    @Override
    public CategoryDTO deleteCategory(Long categoryId) {
        Category cat = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "categoryId", categoryId));

        categoryRepository.delete(cat);
        return modelMapper.map(cat, CategoryDTO.class);
    }


}
