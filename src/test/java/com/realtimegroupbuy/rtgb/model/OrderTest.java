package com.realtimegroupbuy.rtgb.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.realtimegroupbuy.rtgb.model.enums.OrderStatus;
import com.realtimegroupbuy.rtgb.model.enums.ProductStatus;
import com.realtimegroupbuy.rtgb.model.enums.PurchaseGroupStatus;
import com.realtimegroupbuy.rtgb.model.enums.UserRole;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class OrderTest {

    private User user;
    private Seller seller;
    private Product product;
    private PurchaseGroup purchaseGroup;

    @BeforeEach
    void setUp() {
        // 사용자 및 판매자 설정
        user = User.builder()
            .id(1L)
            .nickname("testUser")
            .username("test@email.com")
            .password("password")
            .role(UserRole.USER)
            .build();

        seller = Seller.builder()
            .id(1L)
            .nickname("testSeller")
            .username("seller@email.com")
            .password("password")
            .role(UserRole.SELLER)
            .build();

        // 상품 설정
        product = Product.builder()
            .id(1L)
            .name("Test Product")
            .description("Test Description")
            .price(10000.0)
            .stock(2000)
            .category("Test Category")
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
    @DisplayName("Order 생성 테스트")
    class CreateOrderTest {

        @Test
        @DisplayName("실패 - 주문 수량 0 또는 음수인 경우 예외 발생")
        void test1() {
            // given
            Integer invalidQuantity = 0;

            // when
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> {
                    Order.builder()
                        .user(user)
                        .purchaseGroup(purchaseGroup)
                        .quantity(invalidQuantity)
                        .status(OrderStatus.PENDING)
                        .build();
                });

            // then
            assertEquals("주문 수량은 1개 이상이어야 합니다.", exception.getMessage());
        }

        @Test
        @DisplayName("성공 - 주문 생성")
        void test1000() {
            // given
            Integer orderQuantity = 10;

            // when
            Order order = Order.builder()
                .user(user)
                .purchaseGroup(purchaseGroup)
                .quantity(orderQuantity)
                .status(OrderStatus.PENDING)
                .build();

            // then
            assertNotNull(order);
            assertEquals(order.getUser(), user);
            assertEquals(order.getPurchaseGroup(), purchaseGroup);
            assertEquals(order.getQuantity(), orderQuantity);
            assertEquals(order.getTotalAmount(), product.getPrice() * orderQuantity);
            assertEquals(order.getStatus(), OrderStatus.PENDING);
        }
    }


    @Nested
    @DisplayName("결제 완료 테스트")
    class CompletePaymentTest {

        @Test
        @DisplayName("실패 - 이미 결제 완료[APPROVE]된 주문 결제 시 예외 발생")
        void test1() {
            // given
            Order order = Order.builder()
                .user(user)
                .purchaseGroup(purchaseGroup)
                .quantity(10)
                .status(OrderStatus.APPROVE)
                .build();

            // when
            IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
                order.completePayment();
            });

            // then
            assertEquals("결제가 이미 완료되었습니다.", exception.getMessage());
        }

        @Test
        @DisplayName("성공 - 결제 완료 상태 변경")
        void test1000() {
            // given
            Order order = Order.builder()
                .user(user)
                .purchaseGroup(purchaseGroup)
                .quantity(10)
                .status(OrderStatus.PENDING)
                .build();

            // when
            order.completePayment();

            // then
            assertEquals(OrderStatus.APPROVE, order.getStatus());
        }
    }

    @Nested
    @DisplayName("공동구매 완료[COMPLETED]일 경우 주문 성공으로 변경")
    class successTest {

        @Test
        @DisplayName("성공 - 공동구매 완료 상태 변경 테스트")
        void test1000() {
            // Given
            Order order = Order.builder()
                .user(user)
                .purchaseGroup(purchaseGroup)
                .quantity(10)
                .status(OrderStatus.APPROVE)
                .build();

            // When
            order.success();

            // Then
            assertEquals(OrderStatus.SUCCESS, order.getStatus());
        }
    }
}