package com.realtimegroupbuy.rtgb.service.product;

import com.realtimegroupbuy.rtgb.controller.product.dto.ProductRegisterRequest;
import com.realtimegroupbuy.rtgb.exception.ResourceNotFoundException;
import com.realtimegroupbuy.rtgb.model.Product;
import com.realtimegroupbuy.rtgb.model.Seller;
import com.realtimegroupbuy.rtgb.model.enums.ProductStatus;
import com.realtimegroupbuy.rtgb.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    @Transactional
    public Product register(ProductRegisterRequest request, Seller seller) {
        Product product = Product.builder()
            .name(request.name())
            .description(request.description())
            .price(request.price())
            .stock(request.stock())
            .category(request.category())
            .seller(seller)
            .status(ProductStatus.AVAILABLE)
            .build();
        return productRepository.save(product);
    }

    @Transactional(readOnly = true)
    public Page<Product> getAllProducts(Pageable pageable) {
        return productRepository.findAllWithSeller(pageable);
    }

    @Transactional(readOnly = true)
    public Product getProductById(Long productId) {
        return productRepository.findById(productId)
            .orElseThrow(() -> new ResourceNotFoundException("Product Not Found - by productId"));
    }

}
