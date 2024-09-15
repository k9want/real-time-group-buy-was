package com.realtimegroupbuy.rtgb.service.purchasegroup;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

import com.realtimegroupbuy.rtgb.model.Order;
import com.realtimegroupbuy.rtgb.model.Product;
import com.realtimegroupbuy.rtgb.model.PurchaseGroup;
import com.realtimegroupbuy.rtgb.model.Seller;
import com.realtimegroupbuy.rtgb.model.User;
import com.realtimegroupbuy.rtgb.model.enums.OrderStatus;
import com.realtimegroupbuy.rtgb.model.enums.ProductStatus;
import com.realtimegroupbuy.rtgb.model.enums.PurchaseGroupStatus;
import com.realtimegroupbuy.rtgb.model.enums.UserRole;
import com.realtimegroupbuy.rtgb.service.order.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PurchaseGroupFacadeTest {

    @InjectMocks
    private PurchaseGroupFacade sut;

    @Mock
    private PurchaseGroupService purchaseGroupService;

    @Mock
    private OrderService orderService;

    private User user;
    @BeforeEach
    void setUp() {
        user = User.builder()
            .id(1L)
            .nickname("testUser")
            .build();

        Seller seller = Seller.builder()
            .id(1L)
            .nickname("testSeller")
            .role(UserRole.SELLER)
            .build();


    }


    @Nested
    @DisplayName("공동 구매 참여 테스트")
    class ParticipatePurchaseGroupTest {

        @Test
        @DisplayName("실패 - IN_PROGRESS가 아닌 경우 공동구매 참여 불가능")
        void test1() {
            //given
            PurchaseGroup completedPurchaseGroup =
                PurchaseGroup
                .builder()
                    .id(1L)
                    .targetPurchaseQuantity(1000)
                    .currentPurchaseQuantity(1000)
                    .status(PurchaseGroupStatus.COMPLETED)
                    .build();

            given(purchaseGroupService.participateInPurchaseGroup(completedPurchaseGroup.getId(), 1))
                .willThrow(new IllegalStateException("현재 공동구매에 참여할 수 없습니다."));

            //when & then
            assertThatThrownBy(() -> sut.participatePurchaseGroup(user, completedPurchaseGroup.getId(), 1))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("현재 공동구매에 참여할 수 없습니다.");
        }

        @Test
        @DisplayName("실패 - 주문 수량이 재고 수량보다 많은 경우")
        void test2() {
            //given


            PurchaseGroup inProgressPurchaseGroup =
                PurchaseGroup
                    .builder()
                    .id(1L)
                    .targetPurchaseQuantity(1000)
                    .currentPurchaseQuantity(950)
                    .status(PurchaseGroupStatus.IN_PROGRESS)
                    .build();

            given(purchaseGroupService.participateInPurchaseGroup(inProgressPurchaseGroup.getId(),
                inProgressPurchaseGroup.getTargetPurchaseQuantity() - inProgressPurchaseGroup.getCurrentPurchaseQuantity() + 1))
                .willThrow(new IllegalStateException("현재 재고가 부족하여 해당 수량을 구매할 수 없습니다."));

            //when & then
            assertThatThrownBy(() -> sut.participatePurchaseGroup(user, inProgressPurchaseGroup.getId(),
                inProgressPurchaseGroup.getTargetPurchaseQuantity() - inProgressPurchaseGroup.getCurrentPurchaseQuantity() + 1)) // 목표 수량 - 현재 수량 + 1  -> 즉, 초과
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("현재 재고가 부족하여 해당 수량을 구매할 수 없습니다.");
        }
    }

    @Test
    @DisplayName("성공 - 공동구매 참여 성공 및 결제 완료")
    void test1000() {
        //given
        // 상품 설정
        Product product = Product.builder()
            .id(1L)
            .name("Test Product")
            .price(10000.0)
            .stock(2000)
            .status(ProductStatus.AVAILABLE)
            .build();

        PurchaseGroup purchaseGroup = PurchaseGroup.builder()
            .id(1L)
            .product(product)
            .targetPurchaseQuantity(1000)
            .currentPurchaseQuantity(1000)
            .status(PurchaseGroupStatus.COMPLETED)
            .build();

        Order order = Order.builder()
            .id(1L)
            .user(user)
            .purchaseGroup(purchaseGroup)
            .quantity(50)
            .status(OrderStatus.SUCCESS)
            .build();

        Integer validOrderQuantity = purchaseGroup.getTargetPurchaseQuantity() - purchaseGroup.getCurrentPurchaseQuantity();

        given(purchaseGroupService.participateInPurchaseGroup(purchaseGroup.getId(), validOrderQuantity)).willReturn(purchaseGroup);
        given(orderService.create(user, purchaseGroup, validOrderQuantity)).willReturn(order);
        given(orderService.completePayment(order)).willReturn(order);

        //when
        Order result = sut.participatePurchaseGroup(user, purchaseGroup.getId(), validOrderQuantity);

        //then
        assertThat(result).isEqualTo(order);
        assertThat(purchaseGroup.getStatus()).isEqualTo(PurchaseGroupStatus.COMPLETED);
        assertThat(result.getStatus()).isEqualTo(OrderStatus.SUCCESS);
    }
}
