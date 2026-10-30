package com.ecommerce.sb_ecomm.service;

import com.ecommerce.sb_ecomm.payload.CartDTO;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

public interface CartService {
     CartDTO addProduct(Long productId, Integer quantity);

     List<CartDTO> getAllCarts();

     CartDTO getCart(String emailId, Long cartId);

     @Transactional
     CartDTO updateProductQuantityInCart(Long productId, Integer quantity);

     String deleteProductFromCart(Long cartId, Long productId);

     void updateProductInCart(Long cartId, Long productId);
}
