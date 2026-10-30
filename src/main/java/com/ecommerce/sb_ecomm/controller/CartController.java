package com.ecommerce.sb_ecomm.controller;

import com.ecommerce.sb_ecomm.model.Cart;
import com.ecommerce.sb_ecomm.payload.CartDTO;
import com.ecommerce.sb_ecomm.payload.ProductDTO;
import com.ecommerce.sb_ecomm.repository.CartRepository;
import com.ecommerce.sb_ecomm.service.CartService;
import com.ecommerce.sb_ecomm.util.AuthUtil;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class CartController {
    private final CartService cartService;
    private final AuthUtil authUtil;
    private final CartRepository cartRepository;
    private final ModelMapper modelMapper;

    public CartController(CartService cartService, AuthUtil authUtil, CartRepository cartRepository, ModelMapper modelMapper) {
        this.cartService = cartService;
        this.authUtil = authUtil;
        this.cartRepository = cartRepository;
        this.modelMapper = modelMapper;
    }

    @PostMapping("/cart/prudcucts/{productId}/quantity/{quantity}")
    public ResponseEntity<CartDTO> addProductToCart(@PathVariable Long productId, @PathVariable Integer quantity) {
        CartDTO cartDTO = cartService.addProduct(productId, quantity);
        return new ResponseEntity<CartDTO>(cartDTO, HttpStatus.CREATED);
    }

    @GetMapping("/carts")
    public ResponseEntity<List<CartDTO>> getCarts() {
        List<CartDTO> cartDTOS = cartService.getAllCarts();
        return new ResponseEntity<List<CartDTO>>(cartDTOS, HttpStatus.FOUND);

    }

    @GetMapping("/carts/users")
    public ResponseEntity<CartDTO> getCartById() {
        String emailId = authUtil.loggedInEmail();
        Cart cart = cartRepository.findCartByEmail(emailId);
        CartDTO cartDTO = cartService.getCart(emailId, cart.getCartId());

        return new ResponseEntity<CartDTO>(cartDTO, HttpStatus.OK);
    }

    @PutMapping("/cart/products/{productId}/quantity/{operation}")
    public ResponseEntity<CartDTO> updateCartProduct(@PathVariable Long productId, @PathVariable String operation) {

        CartDTO cartDTO = cartService.updateProductQuantityInCart(productId,
                operation.equalsIgnoreCase("delete") ? -1 : 1);

        return new ResponseEntity<CartDTO>(cartDTO, HttpStatus.OK);
    }

    @DeleteMapping("/cart/{cartId}/product/{productId}")
    public ResponseEntity<String> deleteProductFromCart(@PathVariable Long cartId, @PathVariable Long productId ) {

      String status=  cartService.deleteProductFromCart(cartId,productId);
      return new ResponseEntity<String>(status, HttpStatus.OK);
    }
}
