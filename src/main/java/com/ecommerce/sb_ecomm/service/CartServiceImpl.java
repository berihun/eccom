package com.ecommerce.sb_ecomm.service;

import com.ecommerce.sb_ecomm.exception.APIException;
import com.ecommerce.sb_ecomm.exception.ResourceNotFoundException;
import com.ecommerce.sb_ecomm.model.Cart;
import com.ecommerce.sb_ecomm.model.CartItem;
import com.ecommerce.sb_ecomm.model.Product;
import com.ecommerce.sb_ecomm.payload.CartDTO;
import com.ecommerce.sb_ecomm.payload.ProductDTO;
import com.ecommerce.sb_ecomm.repository.CartItemRepository;
import com.ecommerce.sb_ecomm.repository.CartRepository;
import com.ecommerce.sb_ecomm.repository.ProductRepository;
import com.ecommerce.sb_ecomm.util.AuthUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {
    private final CartRepository cartRepository;
    private final ProductRepository productRepository;
    private final CartItemRepository cartItemRepository;
    private final AuthUtil authUtil;
    private final ModelMapper modelMapper;


    @Override
    public CartDTO addProduct(Long productId, Integer quantity) {
        //Find exissting cart or create one
        Cart cart = createCart();
        //Retrieve product details and Perform validation , check stock, check user exists
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "productId", productId));

        CartItem cartItm = cartItemRepository.findCartItemByProductIdAndCartId(
                cart.getCartId(),
                productId
        );
        if (cartItm != null)
            throw new APIException("Product " + product.getProductName() + " already exists");

        if (product.getQuantity() == 0)
            throw new APIException(product.getProductName() + " is not available");

        if (product.getQuantity() < quantity)
            throw new APIException(" Please, make an order of the " +
                    product.getProductName() +
                    "less than or equal to the quantity " + product.getQuantity() + ".");
        //Create Cart Item
        CartItem newCartItem = new CartItem();
        newCartItem.setProduct(product);
        newCartItem.setQuantity(quantity);
        newCartItem.setCart(cart);
        newCartItem.setProductPrice(product.getSpecialPrice());
        // Save Cart Item
        cartItemRepository.save(newCartItem);
        product.setQuantity(product.getQuantity());
        cart.setTotalPrice(cart.getTotalPrice() + (product.getSpecialPrice() + quantity));
        cartRepository.save(cart);
        //Return updated cart
        CartDTO cartDTO = modelMapper.map(cart, CartDTO.class);

        List<CartItem> cartItems = cart.getCartItems();

        Stream<ProductDTO> productDTOStream = cartItems.stream()
                .map(item -> {
                    ProductDTO prod = modelMapper.map(item.getProduct(), ProductDTO.class);
                    prod.setQuantity(item.getQuantity());
                    return prod;
                });

        cartDTO.setProducts(productDTOStream.toList());

        return cartDTO;
    }

    @Override
    public List<CartDTO> getAllCarts() {
        List<Cart> carts = cartRepository.findAll();

        if (carts.isEmpty())
            throw new APIException("No carts found.");

        List<CartDTO> cartDTOS = carts.stream()
                .map(cart -> {
                    CartDTO cartDTO = modelMapper.map(cart, CartDTO.class);
                    List<ProductDTO> productDTOS = cart.getCartItems().stream()
                            .map(p -> modelMapper.map(p.getProduct(), ProductDTO.class))
                            .toList();
                    cartDTO.setProducts(productDTOS);
                    return cartDTO;
                }).toList();

        return cartDTOS;
    }

    @Override
    public CartDTO getCart(String emailId, Long cartId) {
        Cart cart = cartRepository.findCartByEmailAndCartId(emailId, cartId);
        if (cart == null)
            throw new ResourceNotFoundException("Car ", "cartId", cartId);
        CartDTO cartDTO = modelMapper.map(cart, CartDTO.class);
        cart.getCartItems().forEach(c ->
                c.getProduct().setQuantity(c.getQuantity()));
        List<ProductDTO> products = cart.getCartItems().stream()
                .map(p -> modelMapper.map(p.getProduct(), ProductDTO.class))
                .toList();
        cartDTO.setProducts(products);
        return modelMapper.map(cartDTO, CartDTO.class);
    }

    @Transactional
    @Override
    public CartDTO updateProductQuantityInCart(Long productId, Integer quantity) {

        String email = authUtil.loggedInEmail();
        Cart userCart = cartRepository.findCartByEmail(email);

        Cart cart = cartRepository.findById(userCart.getCartId())
                .orElseThrow(() -> new ResourceNotFoundException("Cart ", "cartId", userCart.getCartId()));

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "productId", productId));


        if (product.getQuantity() == 0)
            throw new APIException(product.getProductName() + " is not available");

        if (product.getQuantity() < quantity)
            throw new APIException(" Please, make an order of the " +
                    product.getProductName() +
                    "less than or equal to the quantity " + product.getQuantity() + ".");

        CartItem cartItem = cartItemRepository.findCartItemByProductIdAndCartId(productId, cart.getCartId());

        if (cartItem == null)
            throw new APIException("Product " + product.getProductName() + " not available in the cart!");

        //calculate to prevent negative quantities

        int newQuantity = cartItem.getQuantity() + quantity;
        if (newQuantity < 0)
            throw new APIException("The resulting can't be negative!");

        if (newQuantity == 0) {
            deleteProductFromCart(productId, productId);
        } else {
            cartItem.setProductPrice(product.getSpecialPrice());
            cartItem.setQuantity(cartItem.getQuantity() + quantity);
            cartItem.setDiscount(product.getDiscount());
            cart.setTotalPrice(cart.getTotalPrice() + (cartItem.getProductPrice() + quantity));
            cartRepository.save(cart);
        }

        CartItem updatedCartItem = cartItemRepository.save(cartItem);
        if (updatedCartItem.getQuantity() == 0) {
            cartItemRepository.deleteById(updatedCartItem.getCartItemId());
        }

        CartDTO cartDTO = modelMapper.map(cart, CartDTO.class);
        List<CartItem> cartItems = cart.getCartItems();

        Stream<ProductDTO> productDTOStream = cartItems.stream().map(
                item -> {
                    ProductDTO prod = modelMapper.map(item.getProduct(), ProductDTO.class);
                    prod.setQuantity(item.getQuantity());
                    return prod;
                });
        cartDTO.setProducts(productDTOStream.toList());
        return cartDTO;
    }

    @Transactional
    @Override
    public String deleteProductFromCart(Long cartId, Long productId) {

        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart ", "cartId", cartId));

        CartItem cartItem = cartItemRepository.findCartItemByProductIdAndCartId(productId, cart.getCartId());
        if (cartItem == null)
            throw new ResourceNotFoundException("Product ", "productId", productId);

        cart.setTotalPrice(cart.getTotalPrice() -
                (cartItem.getProductPrice() * cartItem.getQuantity()));

        cartItemRepository.deleteCartItemByProductIdAndCartId(cartId, productId);
        return "Product " + cartItem.getProduct().getProductName() + " removed from cart !!!";
    }

    @Override
    public void updateProductInCart(Long cartId, Long productId) {

        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart ", "cartId", cartId));

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "productId", productId));

        CartItem cartItem=cartItemRepository.findCartItemByProductIdAndCartId(productId, cart.getCartId());

        if (cartItem == null)
            throw new APIException("Product "+ product.getProductName()+" not available");

        double cartPrice=cart.getTotalPrice()-
                (cartItem.getProductPrice() * cartItem.getQuantity());

        cartItem.setProductPrice(product.getSpecialPrice());
        cart.setTotalPrice(cartPrice+(cartItem.getProductPrice() * cartItem.getQuantity() ));
        cartItemRepository.save(cartItem);



    }

    private Cart createCart() {
        Cart cart = cartRepository.findCartByEmail(authUtil.loggedInEmail());
        if (cart != null) {
            return cart;
        } else {
            Cart newCart = new Cart();
            newCart.setTotalPrice(0.00);
            newCart.setUser(authUtil.loggedInUser());
            return cartRepository.save(newCart);
        }
    }
}
