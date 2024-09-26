package com.realtimegroupbuy.rtgb.purchaseparticiaption;

import static org.assertj.core.api.Assertions.assertThat;

import com.realtimegroupbuy.rtgb.messaging.PurchaseParticipationRequest;
import com.realtimegroupbuy.rtgb.model.Product;
import com.realtimegroupbuy.rtgb.model.PurchaseGroup;
import com.realtimegroupbuy.rtgb.model.Seller;
import com.realtimegroupbuy.rtgb.model.User;
import com.realtimegroupbuy.rtgb.model.enums.ProductStatus;
import com.realtimegroupbuy.rtgb.model.enums.PurchaseGroupStatus;
import com.realtimegroupbuy.rtgb.model.enums.UserRole;
import com.realtimegroupbuy.rtgb.repository.ProductRepository;
import com.realtimegroupbuy.rtgb.repository.PurchaseGroupRepository;
import com.realtimegroupbuy.rtgb.repository.PurchaseParticipationLogRepository;
import com.realtimegroupbuy.rtgb.repository.SellerRepository;
import com.realtimegroupbuy.rtgb.repository.UserRepository;
import com.realtimegroupbuy.rtgb.service.purchasegroup.PurchaseGroupPublisher;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("mysql")
@SpringBootTest
public class PurchaseGroupConcurrencyWithRabbitMQTest {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private PurchaseGroupPublisher purchaseGroupPublisher;

    @Autowired
    private PurchaseParticipationLogRepository logRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SellerRepository sellerRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private PurchaseGroupRepository purchaseGroupRepository;

    private User user;
    private PurchaseGroup purchaseGroup;

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
    @DisplayName("동시성 이슈 테스트 - RabbitMQ를 통한 공동구매 참여")
    void testConcurrentPurchaseGroupParticipationWithRabbitMQ() throws InterruptedException {
        int numberOfThreads = 32;
        ExecutorService executorService = Executors.newFixedThreadPool(numberOfThreads);
        CountDownLatch latch = new CountDownLatch(numberOfThreads);

        // When: 여러 스레드에서 동시에 참여 요청을 발행
        for (int i = 0; i < numberOfThreads; i++) {
            executorService.submit(() -> {
                try {
                    // RabbitMQ에 참여 요청 메시지 발행
                    PurchaseParticipationRequest request = PurchaseParticipationRequest.builder()
                        .requestId(UUID.randomUUID())
                        .userId(user.getId())
                        .purchaseGroupId(purchaseGroup.getId())
                        .orderQuantity(5)
                        .build();
                    purchaseGroupPublisher.participatePurchaseGroup(user, request.getPurchaseGroupId(), request.getOrderQuantity());
                } finally {
                    latch.countDown();
                }
            });
        }

        // 메시지 처리를 기다림 (5초 대기)
        Thread.sleep(5000);

        // 결과 확인: 로그와 데이터베이스에서 참여 결과를 확인
        PurchaseGroup updatedPurchaseGroup = purchaseGroupRepository.findById(purchaseGroup.getId()).get();
        assertThat(updatedPurchaseGroup.getCurrentPurchaseQuantity()).isEqualTo(160); // 수량이 1000이 되어야 함
    }
}