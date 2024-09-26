package com.realtimegroupbuy.rtgb.controller.purchasegroup;

import com.realtimegroupbuy.rtgb.common.dto.ApiResponse;
import com.realtimegroupbuy.rtgb.controller.purchasegroup.dto.ParticipatePurchaseGroupRequest;
import com.realtimegroupbuy.rtgb.controller.purchasegroup.dto.ParticipatePurchaseGroupResponse;
import com.realtimegroupbuy.rtgb.model.User;
import com.realtimegroupbuy.rtgb.service.purchasegroup.PurchaseGroupPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/purchase-groups")
@RequiredArgsConstructor
public class ParticipatePurchaseGroupController {

    private final PurchaseGroupPublisher purchaseGroupPublisher;
    @PreAuthorize("hasRole('USER')")
    @PostMapping("/{purchaseGroupId}/participation")
    public ApiResponse<ParticipatePurchaseGroupResponse> participatePurchaseGroup(
        @PathVariable("purchaseGroupId") Long purchaseGroupId,
        @RequestBody ParticipatePurchaseGroupRequest request,
        @AuthenticationPrincipal User user
    ) {
        // 메시지 발행을 통해 공동 구매 참여 요청 처리
        purchaseGroupPublisher.participatePurchaseGroup(user, purchaseGroupId,
            request.orderQuantity());
        return ApiResponse.OK("공동 구매 참여 요청이 접수되었습니다.", null);
    }
}
