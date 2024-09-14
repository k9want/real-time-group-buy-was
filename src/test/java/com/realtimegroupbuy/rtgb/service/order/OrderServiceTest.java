package com.realtimegroupbuy.rtgb.service.order;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;

import com.realtimegroupbuy.rtgb.model.Order;
import com.realtimegroupbuy.rtgb.model.Product;
import com.realtimegroupbuy.rtgb.model.PurchaseGroup;
import com.realtimegroupbuy.rtgb.model.Seller;
import com.realtimegroupbuy.rtgb.model.User;
import com.realtimegroupbuy.rtgb.model.enums.OrderStatus;
import com.realtimegroupbuy.rtgb.model.enums.ProductStatus;
import com.realtimegroupbuy.rtgb.model.enums.PurchaseGroupStatus;
import com.realtimegroupbuy.rtgb.model.enums.UserRole;
import com.realtimegroupbuy.rtgb.repository.OrderRepository;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @InjectMocks
    private OrderService sut;

    @Mock
    private OrderRepository orderRepository;

    private User user;
    private PurchaseGroup purchaseGroup;

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

        // 상품 설정
        Product product = Product.builder()
            .id(1L)
            .name("Test Product")
            .price(10000.0)
            .stock(2000)
            .seller(seller)
            .status(ProductStatus.AVAILABLE)
            .build();

        // 공동구매 그룹 설정
        purchaseGroup = PurchaseGroup.builder()
            .id(1L)
            .product(product)
            .creator(user)
            .targetPurchaseQuantity(1000)
            .currentPurchaseQuantity(0)
            .expiresAt(LocalDateTime.now().plusDays(7))
            .status(PurchaseGroupStatus.IN_PROGRESS)
            .build();
    }

    @Nested
    @DisplayName("주문 생성")
    class CreateOrderTest {

        @Test
        @DisplayName("실패 - 주문 수량이 0 이하일 때 예외 발생")
        void test() {
            //given
            Integer invalidOrderQuantity = 0;

            //when & then
            assertThatThrownBy(() -> sut.create(user, purchaseGroup, invalidOrderQuantity))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("주문 수량은 1개 이상이어야 합니다.");
        }

        @Test
        @DisplayName("성공 - 주문 생성")
        void test1000() {
            //given
            Integer orderQuantity = purchaseGroup.getTargetPurchaseQuantity();
            Order order = Order.builder()
                .user(user)
                .purchaseGroup(purchaseGroup)
                .quantity(orderQuantity)
                .status(OrderStatus.PENDING)
                .build();

            given(orderRepository.save(any(Order.class))).willReturn(order);

            //when
            Order createdOrder = sut.create(user, purchaseGroup, orderQuantity);

            //then
            assertThat(createdOrder).isNotNull();
            assertThat(order.getTotalAmount()).isEqualTo(purchaseGroup.getProduct().getPrice() * orderQuantity);
            assertThat(createdOrder.getStatus()).isEqualTo(OrderStatus.PENDING);
            then(orderRepository).should(times(1)).save(any(Order.class));
        }
    }


    @Nested
    @DisplayName("결제 완료")
    class CompletePaymentTests {

        @Test
        @DisplayName("실패 - 결제 완료 시 이미 결제 완료된 APPROVE 상태일 때 예외 발생")
        void test1() {
            // given
            Order approvedOrder = Order.builder()
                .user(user)
                .purchaseGroup(purchaseGroup)
                .quantity(purchaseGroup.getTargetPurchaseQuantity())
                .status(OrderStatus.APPROVE)
                .build();

            // when & then
            assertThatThrownBy(() -> sut.completePayment(approvedOrder))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("결제가 이미 완료되었습니다.");
        }

        @Test
        @DisplayName("성공 - 결제 완료")
        void test1000() {
            // given
            Order pendingOrder = Order.builder()
                .user(user)
                .purchaseGroup(purchaseGroup)
                .quantity(purchaseGroup.getTargetPurchaseQuantity())
                .status(OrderStatus.PENDING)
                .build();
            given(orderRepository.save(any(Order.class))).willReturn(pendingOrder);

            // when
            Order completedOrder = sut.completePayment(pendingOrder);

            // then
            assertThat(completedOrder.getStatus()).isEqualTo(OrderStatus.APPROVE);
            then(orderRepository).should(times(1)).save(pendingOrder);
        }
    }
}