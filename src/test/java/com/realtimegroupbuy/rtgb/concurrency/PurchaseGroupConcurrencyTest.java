package com.realtimegroupbuy.rtgb.concurrency;

import static org.assertj.core.api.Assertions.assertThat;

import com.realtimegroupbuy.rtgb.model.Product;
import com.realtimegroupbuy.rtgb.model.PurchaseGroup;
import com.realtimegroupbuy.rtgb.model.Seller;
import com.realtimegroupbuy.rtgb.model.User;
import com.realtimegroupbuy.rtgb.model.enums.ProductStatus;
import com.realtimegroupbuy.rtgb.model.enums.PurchaseGroupStatus;
import com.realtimegroupbuy.rtgb.model.enums.UserRole;
import com.realtimegroupbuy.rtgb.repository.ProductRepository;
import com.realtimegroupbuy.rtgb.repository.PurchaseGroupRepository;
import com.realtimegroupbuy.rtgb.repository.SellerRepository;
import com.realtimegroupbuy.rtgb.repository.UserRepository;
import com.realtimegroupbuy.rtgb.service.purchasegroup.PurchaseGroupFacade;
import java.time.LocalDateTime;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ActiveProfiles("mysql") // application-mysql.yml 사용
@ExtendWith(SpringExtension.class)
@SpringBootTest
public class PurchaseGroupConcurrencyTest {

    @Autowired
    private PurchaseGroupFacade sut;

    private User user;
    private PurchaseGroup purchaseGroup;

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private SellerRepository sellerRepository;
    @Autowired
    private PurchaseGroupRepository purchaseGroupRepository;

    @BeforeEach
    void setUp() {
        user = User.builder()
            .id(1L)
            .nickname("testUser")
            .username("testUser@test.com")
            .password("password")
            .role(UserRole.USER)
            .build();


        Seller seller = Seller.builder()
            .id(1L)
            .nickname("testSeller")
            .username("testSeller@test.com")
            .password("password")
            .role(UserRole.SELLER)
            .build();

        // 상품 설정
        Product product = Product.builder()
            .id(1L)
            .name("Test Product")
            .description("Test Description")
            .price(10000.0)
            .stock(10000)
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

        userRepository.save(user);
        sellerRepository.save(seller);
        productRepository.save(product);
        purchaseGroupRepository.save(purchaseGroup);
    }

    @Test
    @WithMockUser(username = "testUser@test.com", roles = {"USER"})
    @DisplayName("동시성 이슈 테스트 - 여러 스레드가 동시에 공동구매 참여")
    void testConcurrentPurchaseGroupParticipation() throws InterruptedException {
        int numberOfThreads = 10;
        ExecutorService executorService = Executors.newFixedThreadPool(numberOfThreads);
        CountDownLatch latch = new CountDownLatch(numberOfThreads);

        for (int i = 0; i < numberOfThreads; i++) {
            final int threadNumber = i; // 스레드 번호를 기록
            executorService.submit(() -> {
                try {
                    // 스레드 시작 시 PurchaseGroup을 DB에서 다시 조회
                    PurchaseGroup pg = purchaseGroupRepository.findById(purchaseGroup.getId()).get();
                    System.out.println("스레드 " + threadNumber + " 시작: 현재 수량 - " + pg.getCurrentPurchaseQuantity());

                    sut.participatePurchaseGroup(user, pg.getId(), 100);

                    // DB에서 값을 다시 읽어 최종 결과 확인
                    PurchaseGroup updatedPurchaseGroup = purchaseGroupRepository.findById(pg.getId()).get();
                    System.out.println("스레드 " + threadNumber + " 완료: 현재 수량 - " + updatedPurchaseGroup.getCurrentPurchaseQuantity());
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();

        // 결과 확인
        PurchaseGroup updatedPurchaseGroup = purchaseGroupRepository.findById(purchaseGroup.getId()).get();
        assertThat(updatedPurchaseGroup.getCurrentPurchaseQuantity()).isEqualTo(1000); // 수량이 1000이 되어야 함
        assertThat(updatedPurchaseGroup.getStatus()).isEqualTo(PurchaseGroupStatus.COMPLETED); // 상태가 COMPLETED여야 함
    }
}