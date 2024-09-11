package com.realtimegroupbuy.rtgb.controller.purchasegroup;

import com.realtimegroupbuy.rtgb.common.dto.ApiResponse;
import com.realtimegroupbuy.rtgb.controller.purchasegroup.dto.CreatePurchaseGroupRequest;
import com.realtimegroupbuy.rtgb.controller.purchasegroup.dto.CreatePurchaseGroupResponse;
import com.realtimegroupbuy.rtgb.model.PurchaseGroup;
import com.realtimegroupbuy.rtgb.model.User;
import com.realtimegroupbuy.rtgb.service.purchasegroup.PurchaseGroupService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/purchase-groups")
@RequiredArgsConstructor
public class PurchaseGroupController {

    private final PurchaseGroupService purchaseGroupService;
    @PreAuthorize("hasRole('USER')")
    @PostMapping
    public ApiResponse<CreatePurchaseGroupResponse> createPurchaseGroup(
        @RequestBody CreatePurchaseGroupRequest request,
        @AuthenticationPrincipal User user
    ) {
        PurchaseGroup result = purchaseGroupService.create(user, request.productId(), request.targetQuantity(),
            request.expiresAt());
        return ApiResponse.OK(CreatePurchaseGroupResponse.from(result));
    }
}
