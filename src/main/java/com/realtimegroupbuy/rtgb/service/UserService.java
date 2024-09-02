package com.realtimegroupbuy.rtgb.service;

import com.realtimegroupbuy.rtgb.exception.CustomBadRequestException;
import com.realtimegroupbuy.rtgb.exception.ResourceNotFoundException;
import com.realtimegroupbuy.rtgb.model.User;
import com.realtimegroupbuy.rtgb.repository.UserRepository;
import com.realtimegroupbuy.rtgb.util.JwtTokenUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder encoder;
    private final JwtTokenUtils jwtTokenUtils;

    @Transactional
    public User join(String nickname, String userName, String password) {
        // 이미 존재하는 사용자명인지 확인
        userRepository.findByUsername(userName).ifPresent(it -> {
            throw new CustomBadRequestException("Username already exists");
        });

        User user = User.builder()
            .nickname(nickname)
            .username(userName)
            .password(encoder.encode(password))
            .build();

        // 사용자 등록
        return userRepository.save(user);
    }

    public String login(String userName, String password) {
        User user = userRepository.findByUsername(userName)
            .orElseThrow(() -> new ResourceNotFoundException("Username Not Found"));

        if (!encoder.matches(password, user.getPassword())) {
            throw new CustomBadRequestException("Invalid password");
        }

        return jwtTokenUtils.generateToken(userName);
    }

    public User getUserById(Long id) {
        return userRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("User Not Found"));
    }

    public User loadUserByUsername(String username) {
        return userRepository.findByUsername(username)
            .orElseThrow(() -> new ResourceNotFoundException("User Not Found"));
    }

    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }


}
