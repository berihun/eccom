package com.ecommerce.sb_ecomm.controller;

import com.ecommerce.sb_ecomm.config.AppConstants;
import com.ecommerce.sb_ecomm.model.Product;
import com.ecommerce.sb_ecomm.payload.ProductDTO;
import com.ecommerce.sb_ecomm.payload.ProductResponse;
import com.ecommerce.sb_ecomm.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api")
public class ProductController {
    @Autowired
    private ProductService productService;

    @PostMapping("/admin/categories/{categoryId}/product")
    public ResponseEntity<ProductDTO> addProduct(@Valid @RequestBody ProductDTO product,
                                                 @PathVariable Long categoryId) {

        ProductDTO productDTO = productService.addProduct(categoryId, product);
        return new ResponseEntity<>(productDTO, HttpStatus.CREATED);
    }

    @GetMapping("/public/products")
    public ResponseEntity<ProductResponse> getAllProducts(
            @RequestParam(value = "pageNumber", defaultValue = AppConstants.PAGE_NUMBER, required = false) Integer pageNumber,
            @RequestParam(value = "pageSize", defaultValue = AppConstants.PAGE_SIZE, required = false) Integer pageSize,
            @RequestParam(value = "sortBy" , defaultValue = AppConstants.SORT_PRODUCT_BY,  required = false) String sortBy ,
            @RequestParam(value = "sortOrder" , defaultValue = AppConstants.SORT_DIRECTION, required = false) String sortOrder
    ) {
        ProductResponse response = productService.getAllProducts(pageNumber, pageSize, sortBy, sortOrder);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/public/categories/{categoryId}/products")
    public ResponseEntity<ProductResponse> getProductByCategory(@PathVariable Long categoryId,
                                                                @RequestParam(value = "pageNumber", defaultValue = AppConstants.PAGE_NUMBER, required = false) Integer pageNumber,
                                                                @RequestParam(value = "pageSize", defaultValue = AppConstants.PAGE_SIZE, required = false) Integer pageSize,
                                                                @RequestParam(value = "sortBy" , defaultValue = AppConstants.SORT_PRODUCT_BY,  required = false) String sortBy ,
                                                                @RequestParam(value = "sortOrder" , defaultValue = AppConstants.SORT_DIRECTION, required = false) String sortOrder
    ) {
        ProductResponse response = productService.searchByCategory(categoryId,pageNumber, pageSize, sortBy, sortOrder);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/public/products/keyword/{keyword}")
    public ResponseEntity<ProductResponse> getProductByKeyword(@PathVariable String keyword,
                                                               @RequestParam(value = "pageNumber", defaultValue = AppConstants.PAGE_NUMBER, required = false) Integer pageNumber,
                                                               @RequestParam(value = "pageSize", defaultValue = AppConstants.PAGE_SIZE, required = false) Integer pageSize,
                                                               @RequestParam(value = "sortBy" , defaultValue = AppConstants.SORT_PRODUCT_BY,  required = false) String sortBy ,
                                                               @RequestParam(value = "sortOrder" , defaultValue = AppConstants.SORT_DIRECTION, required = false) String sortOrder
    ) {
        ProductResponse response = productService.searchProductByKeyword(keyword,pageNumber, pageSize, sortBy, sortOrder);
        return new ResponseEntity<>(response, HttpStatus.FOUND);
    }

    @PutMapping("/admin/products/{productId}")
    public ResponseEntity<ProductDTO> updateProduct(@Valid @RequestBody ProductDTO product,
                                                    @PathVariable Long productId) {
        ProductDTO productDTO = productService.updateProduct(productId, product);
        return new ResponseEntity<>(productDTO, HttpStatus.CREATED);
    }
    @DeleteMapping("/admin/products/{productId}")
    public ResponseEntity<ProductDTO> deleteProduct(@PathVariable Long productId) {
        ProductDTO productDTO = productService.deleteProduct(productId);
        return new ResponseEntity<>(productDTO, HttpStatus.OK);
    }
    @PutMapping("/products/{productId}/image")
    ResponseEntity<ProductDTO> updateProductImage(@PathVariable Long productId,
                                                  @RequestParam("Image") MultipartFile image) throws IOException {
    ProductDTO updatedProductDto=productService.updateProductImage(productId, image);
        return new ResponseEntity<>(updatedProductDto,HttpStatus.CREATED);

    }

}
