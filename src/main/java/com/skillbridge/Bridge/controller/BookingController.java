package com.skillbridge.Bridge.controller;

import com.skillbridge.Bridge.dto.CreateBookingRequest;
import com.skillbridge.Bridge.service.BookingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    private String clerkUserId(Jwt jwt) {
        return jwt != null ? jwt.getSubject() : null;
    }

    @PostMapping
    public ResponseEntity<?> create(@AuthenticationPrincipal Jwt jwt,
                                   @Valid @RequestBody CreateBookingRequest request) {
        String clerkId = clerkUserId(jwt);
        if (clerkId == null) return ResponseEntity.status(401).build();
        return ResponseEntity.ok(bookingService.create(clerkId, request));
    }

    @GetMapping("/my")
    public ResponseEntity<?> myBookings(@AuthenticationPrincipal Jwt jwt) {
        String clerkId = clerkUserId(jwt);
        if (clerkId == null) return ResponseEntity.status(401).build();
        return ResponseEntity.ok(bookingService.getMyBookingsMaps(clerkId));
    }

    @GetMapping("/provider")
    public ResponseEntity<?> providerBookings(@AuthenticationPrincipal Jwt jwt) {
        String clerkId = clerkUserId(jwt);
        if (clerkId == null) return ResponseEntity.status(401).build();
        return ResponseEntity.ok(bookingService.getProviderBookingsMaps(clerkId));
    }

    @GetMapping("/provider/feed")
    public ResponseEntity<?> providerJobFeed(@AuthenticationPrincipal Jwt jwt) {
        String clerkId = clerkUserId(jwt);
        if (clerkId == null) return ResponseEntity.status(401).build();
        return ResponseEntity.ok(bookingService.getPendingForProvidersMaps());
    }

    @PatchMapping("/{id}/accept")
    public ResponseEntity<?> accept(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id) {
        String clerkId = clerkUserId(jwt);
        if (clerkId == null) return ResponseEntity.status(401).build();
        try {
            return ResponseEntity.ok(bookingService.accept(id, clerkId));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PatchMapping("/{id}/reject")
    public ResponseEntity<?> reject(@PathVariable UUID id) {
        try {
            return ResponseEntity.ok(bookingService.reject(id));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<?> updateStatus(@AuthenticationPrincipal Jwt jwt,
                                         @PathVariable UUID id,
                                         @RequestBody Map<String, String> body) {
        String status = body.get("status");
        if (status == null || status.isBlank()) return ResponseEntity.badRequest().body(Map.of("error", "status required"));
        try {
            return ResponseEntity.ok(bookingService.updateStatus(id, status));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
