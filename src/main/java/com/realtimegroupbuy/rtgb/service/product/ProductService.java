package com.realtimegroupbuy.rtgb.service.product;

import com.realtimegroupbuy.rtgb.controller.product.dto.ProductRegisterRequest;
import com.realtimegroupbuy.rtgb.model.Product;
import com.realtimegroupbuy.rtgb.model.User;
import com.realtimegroupbuy.rtgb.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    @Transactional
    public Product register(ProductRegisterRequest request, User user) {
        Product product = Product.builder()
            .name(request.name())
            .description(request.description())
            .price(request.price())
            .stock(request.stock())
            .category(request.category())
            .build();

        return productRepository.save(product);
    }
}
