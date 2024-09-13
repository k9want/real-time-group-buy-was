package com.realtimegroupbuy.rtgb.model;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.realtimegroupbuy.rtgb.model.enums.ProductStatus;
import com.realtimegroupbuy.rtgb.model.enums.PurchaseGroupStatus;
import com.realtimegroupbuy.rtgb.model.enums.UserRole;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class PurchaseGroupTest {

    private PurchaseGroup sut;

    private User user;
    private Seller seller;
    private Product product;

    @BeforeEach
    void setUp() {
        user = User.builder()
            .id(1L)
            .nickname("testUser")
            .username("test@email.com")
            .password("password")
            .role(UserRole.USER)
            .build();

        seller = Seller.builder()
            .id(1L)
            .nickname("testUser")
            .username("test@email.com")
            .password("password")
            .role(UserRole.USER)
            .build();

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

        sut = PurchaseGroup.builder()
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
    @DisplayName("공동 구매 참여 가능 여부 테스트")
    class validateParticipationTest {

        @Test
        @DisplayName("실패 - 공동구매 수량 초과로 인한 참여 실패 테스트")
        void test1() {
            //given
            Integer orderQuantity = sut.getTargetPurchaseQuantity() + 1;

            //when
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class, () -> {
                    sut.validatePurchaseGroupParticipation(orderQuantity);
                });

            //then
            assertEquals("현재 재고가 부족하여 해당 수량을 구매할 수 없습니다.", exception.getMessage());
        }

        @Test
        @DisplayName("실패 - 공동 구매 진행 상태가 아닌 경우 참여 실패")
        void test2() {
            //given
            PurchaseGroup completedPurchaseGroup = PurchaseGroup.builder()
                .id(1L)
                .product(product)
                .creator(user)
                .targetPurchaseQuantity(1000)
                .currentPurchaseQuantity(1000)
                .expiresAt(LocalDateTime.now().plusDays(7))
                .status(PurchaseGroupStatus.COMPLETED)
                .build();
            Integer orderQuantity = 1;

            //when
            IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
                completedPurchaseGroup.validatePurchaseGroupParticipation(orderQuantity);
            });

            //then
            assertEquals("현재 공동구매에 참여할 수 없습니다.", exception.getMessage());
        }

        @Test
        @DisplayName("성공 - 공동구매 참여 가능 테스트")
        void test1000() {
            //given
            Integer orderQuantity = 10;

            //when & then
            assertDoesNotThrow(() -> sut.validatePurchaseGroupParticipation(orderQuantity));
        }
    }

    @Nested
    @DisplayName("구매 진행 상황 업데이트 테스트")
    class UpdatePurchaseProgressTest {

        @Test
        @DisplayName("성공 - 목표 수량에 도달하지 않았을 경우 IN_PROGRESS 상태")
        void test1001() {
            //given
            Integer orderQuantity = sut.getTargetPurchaseQuantity() - 1;

            //when
            sut.updatePurchaseProgress(orderQuantity);

            //then
            assertEquals(orderQuantity, sut.getCurrentPurchaseQuantity());
            assertEquals(PurchaseGroupStatus.IN_PROGRESS, sut.getStatus());
        }

        @Test
        @DisplayName("성공 - 목표 수량에 도달한 경우 COMPLETED 상태")
        void test1002() {
            //given
            Integer orderQuantity = sut.getTargetPurchaseQuantity();

            //when
            sut.updatePurchaseProgress(orderQuantity);

            //then
            assertEquals(orderQuantity, sut.getCurrentPurchaseQuantity());
            assertEquals(PurchaseGroupStatus.COMPLETED, sut.getStatus());
        }
    }

    @Nested
    @DisplayName("공동구매 완료 여부 확인 테스트")
    class IsCompletedTest {

        @Test
        @DisplayName("성공 - 공동구매 그룹 상태가 IN_PROGRESS 일때 false")
        void test1001() {
            //given
            Integer orderQuantity = sut.getTargetPurchaseQuantity() - 1;

            //when
            sut.updatePurchaseProgress(orderQuantity);

            //then
            assertFalse(sut.isCompleted());
        }

        @Test
        @DisplayName("성공 - 공동구매 그룹 상태가 COMPLETED 일때 true")
        void test1002() {
            //given
            Integer orderQuantity = sut.getTargetPurchaseQuantity();

            //when
            sut.updatePurchaseProgress(orderQuantity);

            //then
            assertTrue(sut.isCompleted());
        }

    }
}