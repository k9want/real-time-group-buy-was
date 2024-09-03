package com.realtimegroupbuy.rtgb.controller.seller;

import com.realtimegroupbuy.rtgb.common.dto.ApiResponse;
import com.realtimegroupbuy.rtgb.controller.seller.dto.SellerJoinRequest;
import com.realtimegroupbuy.rtgb.controller.seller.dto.SellerLoginRequest;
import com.realtimegroupbuy.rtgb.model.Seller;
import com.realtimegroupbuy.rtgb.service.seller.SellerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/sellers")
@RequiredArgsConstructor
public class SellerController {

    private final SellerService sellerService;

    @PostMapping("/join")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<String> join(
        @RequestBody SellerJoinRequest request) {
        Seller result = sellerService.join(request.nickname(), request.username(),
            request.password());
        return ApiResponse.OK(result.getNickname());
    }

    @PostMapping("/login")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<String> login(
        @RequestBody SellerLoginRequest request) {
        String token = sellerService.login(request.username(), request.password());
        return ApiResponse.OK(token);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Seller> getSellerById(@PathVariable Long id) {
        return ResponseEntity.ok(sellerService.getSellerById(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        sellerService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
