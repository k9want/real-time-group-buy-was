package com.realtimegroupbuy.rtgb.controller.product;

import com.realtimegroupbuy.rtgb.common.dto.ApiResponse;
import com.realtimegroupbuy.rtgb.controller.product.dto.ProductRegisterRequest;
import com.realtimegroupbuy.rtgb.model.Product;
import com.realtimegroupbuy.rtgb.model.User;
import com.realtimegroupbuy.rtgb.service.product.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @PostMapping("/register")
    public ApiResponse<Product> register(
        @RequestBody ProductRegisterRequest request,
        @AuthenticationPrincipal User user
    ) {
        Product result = productService.register(request, user);
        return ApiResponse.OK(result);
    }
}
