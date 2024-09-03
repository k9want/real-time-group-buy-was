package com.realtimegroupbuy.rtgb.controller.product;

import com.realtimegroupbuy.rtgb.common.dto.ApiResponse;
import com.realtimegroupbuy.rtgb.controller.product.dto.ProductRegisterRequest;
import com.realtimegroupbuy.rtgb.controller.product.dto.ProductRegisterResponse;
import com.realtimegroupbuy.rtgb.controller.product.dto.ProductResponse;
import com.realtimegroupbuy.rtgb.model.Product;
import com.realtimegroupbuy.rtgb.model.Seller;
import com.realtimegroupbuy.rtgb.service.product.ProductService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @PreAuthorize("hasRole('SELLER')")
    @PostMapping("/product")
    public ApiResponse<ProductRegisterResponse> register(
        @RequestBody ProductRegisterRequest request,
        @AuthenticationPrincipal Seller seller
    ) {
        Product result = productService.register(request, seller);
        return ApiResponse.OK(ProductRegisterResponse.from(result));
    }

    @GetMapping("/products")
    public ApiResponse<List<ProductResponse>> getAllProducts(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size) {
        PageRequest pageRequest = PageRequest.of(page, size);
        Page<Product> result = productService.getAllProducts(pageRequest);
        List<ProductResponse> response = result.stream()
            .map(ProductResponse::from)
            .toList();
        return ApiResponse.OK(response);
    }

    @GetMapping("/products/{productId}")
    public ApiResponse<ProductResponse> getProductById(
        @PathVariable("productId") Long productId
    ) {
        Product result = productService.getProductById(productId);
        return ApiResponse.OK(ProductResponse.from(result));
    }
}
