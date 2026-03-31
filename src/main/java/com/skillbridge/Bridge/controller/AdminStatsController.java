package com.skillbridge.Bridge.controller;

import com.skillbridge.Bridge.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminStatsController {

    private final UserRepository userRepository;
    private final ServiceProviderRepository serviceProviderRepository;
    private final BookingRepository bookingRepository;
    private final CustomWorkRequestRepository customWorkRequestRepository;

    @Value("${BRIDGE_ADMIN_KEY:}")
    private String adminKey;

    @GetMapping("/stats")
    public ResponseEntity<?> stats(@RequestHeader(value = "X-Bridge-Admin-Key", required = false) String key) {
        if (adminKey == null || adminKey.isBlank()) {
            return ResponseEntity.status(503).body(Map.of(
                    "error", "Admin stats disabled. Set BRIDGE_ADMIN_KEY in the backend environment."
            ));
        }
        if (key == null || !adminKey.equals(key)) {
            return ResponseEntity.status(403).body(Map.of("error", "Invalid or missing X-Bridge-Admin-Key"));
        }

        long totalUsers = userRepository.count();
        long hireRoleCount = userRepository.countByRole("hire");
        long spRoleCount = userRepository.countByRole("service_provider");
        long providers = serviceProviderRepository.count();
        long bookings = bookingRepository.count();
        long pendingBookings = bookingRepository.countByStatus("pending_acceptance");
        long openCustom = customWorkRequestRepository.countByStatus("open");
        long totalCustom = customWorkRequestRepository.count();

        Map<String, Object> db = new HashMap<>();
        db.put("totalUsers", totalUsers);
        db.put("usersWithRoleHire", hireRoleCount);
        db.put("usersWithRoleServiceProvider", spRoleCount);
        db.put("serviceProviderProfiles", providers);
        db.put("totalBookings", bookings);
        db.put("pendingAcceptanceBookings", pendingBookings);
        db.put("openCustomWorkRequests", openCustom);
        db.put("totalCustomWorkRequests", totalCustom);

        Map<String, Object> dummy = new HashMap<>();
        dummy.put("headlineTotalUsers", "12,847");
        dummy.put("headlineActiveBookings", "3,291");
        dummy.put("headlineRevenue", "₹8.2L");
        dummy.put("headlineMessages", "1,024");
        dummy.put("note", "Illustrative marketing figures — not from your database.");

        Map<String, Object> body = new HashMap<>();
        body.put("fromDatabase", db);
        body.put("dummyAnalytics", dummy);
        return ResponseEntity.ok(body);
    }
}
