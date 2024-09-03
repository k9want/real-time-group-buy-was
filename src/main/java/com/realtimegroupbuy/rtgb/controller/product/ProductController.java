package com.realtimegroupbuy.rtgb.controller.product;

import com.realtimegroupbuy.rtgb.common.dto.ApiResponse;
import com.realtimegroupbuy.rtgb.controller.product.dto.ProductRegisterRequest;
import com.realtimegroupbuy.rtgb.controller.product.dto.ProductRegisterResponse;
import com.realtimegroupbuy.rtgb.model.Product;
import com.realtimegroupbuy.rtgb.model.Seller;
import com.realtimegroupbuy.rtgb.service.product.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/product")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @PreAuthorize("hasRole('ROLE_SELLER')")
    @PostMapping
    public ApiResponse<ProductRegisterResponse> register(
        @RequestBody ProductRegisterRequest request,
        @AuthenticationPrincipal Seller seller
    ) {
        Product result = productService.register(request, seller);
        return ApiResponse.OK(ProductRegisterResponse.from(result));
    }
}
