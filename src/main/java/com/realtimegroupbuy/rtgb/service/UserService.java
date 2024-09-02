package com.realtimegroupbuy.rtgb.service;

import com.realtimegroupbuy.rtgb.exception.ResourceNotFoundException;
import com.realtimegroupbuy.rtgb.model.User;
import com.realtimegroupbuy.rtgb.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public User getUserById(Long id) {
        return userRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("User Not Found"));
    }

    public User createUser(User user) {
        return userRepository.save(user);
    }

    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }

}
