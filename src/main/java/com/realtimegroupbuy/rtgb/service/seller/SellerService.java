package com.realtimegroupbuy.rtgb.service.seller;

import com.realtimegroupbuy.rtgb.exception.CustomBadRequestException;
import com.realtimegroupbuy.rtgb.exception.ResourceNotFoundException;
import com.realtimegroupbuy.rtgb.model.Seller;
import com.realtimegroupbuy.rtgb.model.enums.UserRole;
import com.realtimegroupbuy.rtgb.repository.SellerRepository;
import com.realtimegroupbuy.rtgb.util.JwtTokenUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SellerService {

    private final SellerRepository sellerRepository;
    private final BCryptPasswordEncoder encoder;
    private final JwtTokenUtils jwtTokenUtils;

    @Transactional
    public Seller join(String nickname, String userName, String password) {
        // 이미 존재하는 사용자명인지 확인
        sellerRepository.findByUsername(userName).ifPresent(it -> {
            throw new CustomBadRequestException("Username already exists");
        });

        Seller seller = Seller.builder()
            .nickname(nickname)
            .username(userName)
            .password(encoder.encode(password))
            .role(UserRole.SELLER)
            .build();

        // 사용자 등록
        return sellerRepository.save(seller);
    }

    public String login(String userName, String password) {
        Seller seller = sellerRepository.findByUsername(userName)
            .orElseThrow(() -> new ResourceNotFoundException("Username Not Found"));

        if (!encoder.matches(password, seller.getPassword())) {
            throw new CustomBadRequestException("Invalid password");
        }

        return jwtTokenUtils.generateToken(userName);
    }

    public Seller getSellerById(Long id) {
        return sellerRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Seller Not Found"));
    }

    public void delete(Long id) {sellerRepository.deleteById(id);
    }

}
