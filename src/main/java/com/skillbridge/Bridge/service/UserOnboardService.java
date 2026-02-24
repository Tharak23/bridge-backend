package com.skillbridge.Bridge.service;

import com.skillbridge.Bridge.dto.HireOnboardRequest;
import com.skillbridge.Bridge.entity.User;
import com.skillbridge.Bridge.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class UserOnboardService {

    private final UserRepository userRepository;

    @Transactional
    public User upsertHireUser(String clerkUserId, HireOnboardRequest req) {
        User user = userRepository.findByClerkUserId(clerkUserId)
                .orElse(User.builder()
                        .clerkUserId(clerkUserId)
                        .role("hire")
                        .build());
        user.setName(req.getName());
        user.setPhone(req.getPhone());
        user.setAddressLine(req.getAddressLine());
        user.setCity(req.getCity());
        user.setState(req.getState());
        user.setPincode(req.getPincode());
        user.setLatitude(req.getLatitude());
        user.setLongitude(req.getLongitude());
        return userRepository.save(user);
    }

    public User getByClerkUserId(String clerkUserId) {
        return userRepository.findByClerkUserId(clerkUserId).orElse(null);
    }
}
