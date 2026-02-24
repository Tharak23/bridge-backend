package com.skillbridge.Bridge.controller;

import com.skillbridge.Bridge.dto.HireOnboardRequest;
import com.skillbridge.Bridge.entity.User;
import com.skillbridge.Bridge.service.UserOnboardService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserOnboardService userOnboardService;

    private String clerkUserId(Jwt jwt) {
        return jwt != null ? jwt.getSubject() : null;
    }

    @GetMapping("/me")
    public ResponseEntity<?> getMe(@AuthenticationPrincipal Jwt jwt) {
        String clerkId = clerkUserId(jwt);
        if (clerkId == null) return ResponseEntity.status(401).build();
        User user = userOnboardService.getByClerkUserId(clerkId);
        if (user == null) return ResponseEntity.ok(Map.of("onboarded", false));
        return ResponseEntity.ok(Map.of(
                "onboarded", true,
                "role", user.getRole(),
                "name", user.getName() != null ? user.getName() : "",
                "phone", user.getPhone() != null ? user.getPhone() : ""
        ));
    }

    @PostMapping("/me/hire")
    public ResponseEntity<?> onboardHire(@AuthenticationPrincipal Jwt jwt,
                                         @Valid @RequestBody HireOnboardRequest request) {
        String clerkId = clerkUserId(jwt);
        if (clerkId == null) return ResponseEntity.status(401).build();
        User user = userOnboardService.upsertHireUser(clerkId, request);
        return ResponseEntity.ok(Map.of(
                "id", user.getId(),
                "role", "hire"
        ));
    }
}
