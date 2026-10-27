package com.ecommerce.sb_ecomm.service;

import com.ecommerce.sb_ecomm.repository.CategoryRepository;
import com.ecommerce.sb_ecomm.repository.ProductRepository;
import com.ecommerce.sb_ecomm.exception.APIException;
import com.ecommerce.sb_ecomm.exception.ResourceNotFoundException;
import com.ecommerce.sb_ecomm.model.Category;
import com.ecommerce.sb_ecomm.model.Product;
import com.ecommerce.sb_ecomm.payload.ProductDTO;
import com.ecommerce.sb_ecomm.payload.ProductResponse;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Service
public class ProductServiceImpl implements ProductService {
    private final ProductRepository productRepository;

    private final CategoryRepository categoryRepository;

    private final ModelMapper modelMapper;
    private final FileService fileService;

    @Value("${project.images}")
    private String path;


    public ProductServiceImpl(ProductRepository productRepository, CategoryRepository categoryRepository, ModelMapper modelMapper, FileService fileService) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.modelMapper = modelMapper;
        this.fileService = fileService;
    }


    @Override
    public ProductDTO addProduct(Long categoryId, ProductDTO productDTO) {
        //check if  product is already exists
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Category", "categoryId", categoryId));

        boolean isProductNotPresent = true;
        List<Product> products = category.getProducts();
        Optional<Product> matchedProduct = products.stream()
                .filter(ll -> ll.getProductName().equalsIgnoreCase(productDTO.getProductName()))
                .findFirst();

        if (matchedProduct.isPresent())
            throw new APIException("Product name already exists");

        Product product = modelMapper.map(productDTO, Product.class);
        product.setCategory(category);
        Double specialPrice = product.getPrice() -
                ((product.getDiscount() * 0.01) * product.getPrice());
        product.setImage("default.png");
        product.setSpecialPrice(specialPrice);

        Product savedProduct = productRepository.save(product);
        return modelMapper.map(savedProduct, ProductDTO.class);
    }

    @Override
    public ProductResponse getAllProducts(Integer pageNumber, Integer pageSize, String sortBy, String sortOrder) {

        Sort sortByAndOrder = sortOrder.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(pageNumber - 1, pageSize, sortByAndOrder);
        //check if the product size is 0
        Page<Product> products = productRepository.findAll(pageable);


        List<ProductDTO> productDTOS = products.stream()
                .map(product -> modelMapper.map(product, ProductDTO.class))
                .toList();
        ProductResponse respons = new ProductResponse();
        respons.setContent(productDTOS);
        respons.setPageNumber(products.getNumber());
        respons.setPageSize(products.getSize());
        respons.setTotalElements(products.getTotalElements());
        respons.setTotalPages(products.getTotalPages());
        respons.setLastPage(products.isLast());
        return respons;
    }

    @Override
    public ProductResponse searchByCategory(Long categoryId, Integer pageNumber, Integer pageSize, String sortBy, String sortOrder) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Category", "categoryId", categoryId));
        Sort sortByAndOrder = sortOrder.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(pageNumber - 1, pageSize, sortByAndOrder);
        //check if the product size is 0

        Page<Product> products = productRepository.findByCategoryOrderByPriceAsc(category,pageable);
        if (products.isEmpty())
            throw new APIException("Product not found!!!");
        List<ProductDTO> productDTOS = products.stream()
                .map(product -> modelMapper.map(product, ProductDTO.class))
                .toList();

        ProductResponse respons = new ProductResponse();
        respons.setContent(productDTOS);
        respons.setPageNumber(products.getNumber());
        respons.setPageSize(products.getSize());
        respons.setTotalElements(products.getTotalElements());
        respons.setTotalPages(products.getTotalPages());
        respons.setLastPage(products.isLast());
        return respons;
    }

    @Override
    public ProductResponse searchProductByKeyword(String keyword, Integer pageNumber, Integer pageSize, String sortBy, String sortOrder) {


        Sort sortByAndOrder = sortOrder.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(pageNumber - 1, pageSize, sortByAndOrder);
        Page<Product> products = productRepository.findProductByProductNameLikeIgnoreCase('%' + keyword + '%',pageable);
        List<ProductDTO> productDTOS = products.stream()
                .map(product -> modelMapper.map(product, ProductDTO.class))
                .toList();

        ProductResponse respons = new ProductResponse();
        respons.setContent(productDTOS);
        respons.setPageNumber(products.getNumber());
        respons.setPageSize(products.getSize());
        respons.setTotalElements(products.getTotalElements());
        respons.setTotalPages(products.getTotalPages());
        respons.setLastPage(products.isLast());
        return respons;
    }

    @Override
    public ProductDTO updateProduct(Long productId, ProductDTO productDTO) {
        Product product1 = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "productId", productId));
        // update the product
        Product product = modelMapper.map(productDTO, Product.class);
        product1.setProductName(product.getProductName());
        product1.setDescription(product.getDescription());
        product1.setQuantity(product.getQuantity());
        product1.setDiscount(product.getDiscount());
        product1.setPrice(product.getPrice());
        product1.setSpecialPrice(product.getSpecialPrice());

        //save
        Product product2 = productRepository.save(product1);
        return modelMapper.map(product2, ProductDTO.class);
    }

    @Override
    public ProductDTO deleteProduct(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "productId", productId));
        productRepository.delete(product);
        return modelMapper.map(product, ProductDTO.class);
    }

    @Override
    public ProductDTO updateProductImage(Long productId, MultipartFile image) throws IOException {
        //get product
        Product productFromDb = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "productId", productId));

        //upload image to server
        //get file name of uploaded image
        String fileName = fileService.uploadImage(path, image);
        //updating the new file name to the product
        productFromDb.setImage(fileName);
        //save to db
        Product product = productRepository.save(productFromDb);

        //return DTO after mapping product to DTO
        return modelMapper.map(product, ProductDTO.class);
    }

}
