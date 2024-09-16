package com.realtimegroupbuy.rtgb.service.purchasegroup;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;

import com.realtimegroupbuy.rtgb.model.Product;
import com.realtimegroupbuy.rtgb.model.PurchaseGroup;
import com.realtimegroupbuy.rtgb.model.Seller;
import com.realtimegroupbuy.rtgb.model.User;
import com.realtimegroupbuy.rtgb.model.enums.ProductStatus;
import com.realtimegroupbuy.rtgb.model.enums.PurchaseGroupStatus;
import com.realtimegroupbuy.rtgb.model.enums.UserRole;
import com.realtimegroupbuy.rtgb.repository.ProductRepository;
import com.realtimegroupbuy.rtgb.repository.PurchaseGroupRepository;
import com.realtimegroupbuy.rtgb.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PurchaseGroupServiceTest {

    @InjectMocks
    private PurchaseGroupService sut;

    @Mock
    private PurchaseGroupRepository purchaseGroupRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private UserRepository userRepository;

    private User user;
    private Product product;
    private PurchaseGroup purchaseGroup;

    @BeforeEach
    void setUp() {
        user = User.builder()
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
            .seller(Seller.builder().id(1L).nickname("testSeller").build())
            .status(ProductStatus.AVAILABLE)
            .build();

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
    @DisplayName("공동 구매 그룹 생성")
    class CreatePurchaseGroupTest {

        @Test
        @DisplayName("실패 - 최소 주문 수량 미달")
        void test1() {
            //given
            Integer invalidQuantity = 9; // 최소 주문 수량은 10개
            LocalDateTime expiredAt = LocalDateTime.now().plusDays(7);

            //when & //then
            assertThatThrownBy(() -> sut.create(user, product.getId(), invalidQuantity, expiredAt))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("최소 공동 구매 수량은 10개 이상입니다.");
        }

        @Test
        @DisplayName("실패 - 유효 기간 미달")
        void test2() {
            //given
            Integer validQuantity = 100;
            LocalDateTime invalidExpiredAt = LocalDateTime.now().plusHours(23);

            // when & then
            assertThatThrownBy(
                () -> sut.create(user, product.getId(), validQuantity, invalidExpiredAt))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("최소 공동 구매 유효 기간은 1일입니다.");
        }


        @Test
        @DisplayName("성공 - 공동 구매 그룹 생성")
        void test1000() {
            // given
            Integer originProductStock = product.getStock();
            Integer validQuantity = 1000;
            LocalDateTime expiredAt = LocalDateTime.now().plusDays(7);

            given(userRepository.findById(anyLong())).willReturn(Optional.of(user));
            given(productRepository.findById(anyLong())).willReturn(Optional.of(product));
            given(purchaseGroupRepository.save(any(PurchaseGroup.class))).willReturn(purchaseGroup);

            // when
            PurchaseGroup createdGroup = sut.create(user, product.getId(), validQuantity,
                expiredAt);

            // then
            assertThat(product.getStock()).isEqualTo(originProductStock - validQuantity);
            assertThat(createdGroup.getTargetPurchaseQuantity()).isEqualTo(validQuantity);
            assertThat(createdGroup.getProduct()).isEqualTo(product);
            assertThat(createdGroup.getCreator()).isEqualTo(user);
            assertThat(createdGroup.getStatus()).isEqualTo(PurchaseGroupStatus.IN_PROGRESS);
        }
    }

    @Nested
    @DisplayName("공동 구매 참여 테스트")
    class ParticipatePurchaseGroupTest {

        @Test
        @DisplayName("실패 - 재고 부족으로 참여 실패")
        void test1() {
            // given
            given(purchaseGroupRepository.findByIdWithPessimisticLock(anyLong())).willReturn(
                Optional.of(purchaseGroup));
            Integer invalidOderQuantity =
                purchaseGroup.getTargetPurchaseQuantity() + 1; // 목표 수량보다 많음

            // when & then
            assertThatThrownBy(
                () -> sut.participateInPurchaseGroup(purchaseGroup.getId(), invalidOderQuantity))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("현재 재고보다 많은 수량을 주문할 수 없습니다.");
        }

        @Test
        @DisplayName("성공 - 공동 구매 참여 [목표 수량 채웠을 경우 - 공동구매 그룹 상태 IN_PROGRESS]")
        void test1000() {
            // given
            given(purchaseGroupRepository.findByIdWithPessimisticLock(anyLong())).willReturn(Optional.of(purchaseGroup));
            Integer validOrderQuantity = purchaseGroup.getTargetPurchaseQuantity();

            // when
            PurchaseGroup updatedGroup = sut.participateInPurchaseGroup(purchaseGroup.getId(), validOrderQuantity);

            // then
            assertThat(updatedGroup.getCurrentPurchaseQuantity()).isEqualTo(validOrderQuantity);
            assertThat(updatedGroup.getStatus()).isEqualTo(PurchaseGroupStatus.IN_PROGRESS);
        }
    }

    @Nested
    @DisplayName("공동 구매 그룹 조회 테스트")
    class GetPurchaseGroupTest {

        @Test
        @DisplayName("실패 - 존재하지 않는 그룹 조회")
        void test1() {
            // given
            given(purchaseGroupRepository.findById(anyLong())).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> sut.getPurchaseGroup(purchaseGroup.getId()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("공동 구매 그룹이 존재하지 않습니다.");
        }

        @Test
        @DisplayName("성공 - 공동 구매 그룹 조회")
        void test1000() {
            // given
            given(purchaseGroupRepository.findById(anyLong())).willReturn(Optional.of(purchaseGroup));

            // when
            PurchaseGroup foundGroup = sut.getPurchaseGroup(purchaseGroup.getId());

            // then
            assertThat(foundGroup.getId()).isEqualTo(purchaseGroup.getId());
            assertThat(foundGroup.getTargetPurchaseQuantity()).isEqualTo(purchaseGroup.getTargetPurchaseQuantity());
        }
    }
}