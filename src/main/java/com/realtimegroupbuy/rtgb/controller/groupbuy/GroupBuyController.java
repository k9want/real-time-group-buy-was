package com.realtimegroupbuy.rtgb.controller.groupbuy;

import com.realtimegroupbuy.rtgb.common.dto.ApiResponse;
import com.realtimegroupbuy.rtgb.controller.groupbuy.dto.CreateGroupBuyRequest;
import com.realtimegroupbuy.rtgb.controller.groupbuy.dto.CreateGroupBuyResponse;
import com.realtimegroupbuy.rtgb.model.GroupBuy;
import com.realtimegroupbuy.rtgb.model.User;
import com.realtimegroupbuy.rtgb.service.groupbuy.GroupBuyService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/group-buys")
@RequiredArgsConstructor
public class GroupBuyController {

    private final GroupBuyService groupBuyService;
    @PreAuthorize("hasRole('USER')")
    @PostMapping
    public ApiResponse<CreateGroupBuyResponse> createGroupBuy(
        @RequestBody CreateGroupBuyRequest request,
        @AuthenticationPrincipal User user
    ) {
        GroupBuy result = groupBuyService.create(user, request.productId(), request.targetQuantity(),
            request.expiresAt());
        return ApiResponse.OK(CreateGroupBuyResponse.from(result));
    }
}
