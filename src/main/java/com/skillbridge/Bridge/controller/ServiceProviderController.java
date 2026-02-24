package com.skillbridge.Bridge.controller;

import com.skillbridge.Bridge.dto.ServiceProviderRequest;
import com.skillbridge.Bridge.entity.ServiceProvider;
import com.skillbridge.Bridge.service.ServiceProviderOnboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/service-providers")
@RequiredArgsConstructor
public class ServiceProviderController {

    private final ServiceProviderOnboardService service;

    private String clerkUserId(Jwt jwt) {
        return jwt != null ? jwt.getSubject() : null;
    }

    @GetMapping("/me")
    public ResponseEntity<?> getMe(@AuthenticationPrincipal Jwt jwt) {
        String clerkId = clerkUserId(jwt);
        if (clerkId == null) return ResponseEntity.status(401).build();
        ServiceProvider sp = service.getByClerkUserId(clerkId);
        if (sp == null) return ResponseEntity.ok(Map.of("submitted", false));
        return ResponseEntity.ok(Map.of(
                "submitted", "submitted".equals(sp.getStatus()),
                "id", sp.getId()
        ));
    }

    @GetMapping("/me/profile")
    public ResponseEntity<?> getMyProfile(@AuthenticationPrincipal Jwt jwt) {
        String clerkId = clerkUserId(jwt);
        if (clerkId == null) return ResponseEntity.status(401).build();
        Map<String, Object> profile = service.getProfileByClerkUserId(clerkId);
        if (profile == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(profile);
    }

    @PostMapping("/me/submit")
    public ResponseEntity<?> submit(@AuthenticationPrincipal Jwt jwt,
                                    @RequestBody ServiceProviderRequest request) {
        String clerkId = clerkUserId(jwt);
        if (clerkId == null) return ResponseEntity.status(401).build();
        ServiceProvider sp = service.submitServiceProvider(clerkId, request);
        return ResponseEntity.ok(Map.of(
                "id", sp.getId(),
                "status", sp.getStatus()
        ));
    }

    @PatchMapping("/me/profile")
    public ResponseEntity<?> updateMyProfile(@AuthenticationPrincipal Jwt jwt,
                                            @RequestBody Map<String, Object> updates) {
        String clerkId = clerkUserId(jwt);
        if (clerkId == null) return ResponseEntity.status(401).build();
        Map<String, Object> profile = service.updateProfileByClerkUserId(clerkId, updates);
        if (profile == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(profile);
    }
}
