package com.skillbridge.Bridge.controller;

import com.skillbridge.Bridge.repository.ServiceProviderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/services")
@RequiredArgsConstructor
public class ServicesController {

    private final ServiceProviderRepository serviceProviderRepository;

    // Dummy categories aligned with onboarding (Plumbing, Electrical, etc.)
    private static final List<Map<String, String>> CATEGORIES = List.of(
            Map.of("id", "plumbing", "name", "Plumbing", "slug", "plumbing", "icon", "wrench"),
            Map.of("id", "electrical", "name", "Electrical", "slug", "electrical", "icon", "zap"),
            Map.of("id", "ac_appliances", "name", "AC & Appliances", "slug", "ac_appliances", "icon", "wind"),
            Map.of("id", "cleaning_pest", "name", "Cleaning & Pest", "slug", "cleaning_pest", "icon", "sparkles"),
            Map.of("id", "salon_spa", "name", "Salon & Spa", "slug", "salon_spa", "icon", "scissors"),
            Map.of("id", "electronics", "name", "Electronics", "slug", "electronics", "icon", "tv"),
            Map.of("id", "other", "name", "Other", "slug", "other", "icon", "more-horizontal")
    );

    // Sub-services by category: electronics -> TV, Fridge, AC, Washing Machine etc.
    private static final Map<String, List<Map<String, Object>>> SUB_SERVICES = Map.ofEntries(
            Map.entry("electronics", List.of(
                    Map.<String, Object>of("id", "tv-repair", "name", "TV Repair", "slug", "tv-repair", "price", 199, "rating", 4.78, "reviews", 27000),
                    Map.<String, Object>of("id", "fridge-repair", "name", "Fridge Repair", "slug", "fridge-repair", "price", 199, "rating", 4.74, "reviews", 19000),
                    Map.<String, Object>of("id", "ac-repair", "name", "AC Repair", "slug", "ac-repair", "price", 299, "rating", 4.75, "reviews", 19000),
                    Map.<String, Object>of("id", "washing-machine", "name", "Washing Machine", "slug", "washing-machine", "price", 199, "rating", 4.72, "reviews", 15000)
            )),
            Map.entry("plumbing", List.of(
                    Map.<String, Object>of("id", "tap-repair", "name", "Tap & Leak Repair", "slug", "tap-repair", "price", 149, "rating", 4.8, "reviews", 32000),
                    Map.<String, Object>of("id", "geyser", "name", "Geyser Repair", "slug", "geyser", "price", 249, "rating", 4.76, "reviews", 18000)
            )),
            Map.entry("electrical", List.of(
                    Map.<String, Object>of("id", "wiring", "name", "Wiring & Switch", "slug", "wiring", "price", 199, "rating", 4.77, "reviews", 22000),
                    Map.<String, Object>of("id", "fan", "name", "Fan & Light", "slug", "fan", "price", 149, "rating", 4.75, "reviews", 25000)
            )),
            Map.entry("ac_appliances", List.of(
                    Map.<String, Object>of("id", "ac-service", "name", "AC Service", "slug", "ac-service", "price", 349, "rating", 4.79, "reviews", 28000),
                    Map.<String, Object>of("id", "ac-install", "name", "AC Installation", "slug", "ac-install", "price", 499, "rating", 4.72, "reviews", 12000)
            )),
            Map.entry("cleaning_pest", List.of(
                    Map.<String, Object>of("id", "home-cleaning", "name", "Home Cleaning", "slug", "home-cleaning", "price", 399, "rating", 4.81, "reviews", 45000),
                    Map.<String, Object>of("id", "pest-control", "name", "Pest Control", "slug", "pest-control", "price", 499, "rating", 4.78, "reviews", 21000)
            )),
            Map.entry("salon_spa", List.of(
                    Map.<String, Object>of("id", "haircut", "name", "Haircut at Home", "slug", "haircut", "price", 199, "rating", 4.74, "reviews", 15000),
                    Map.<String, Object>of("id", "massage", "name", "Massage & Spa", "slug", "massage", "price", 599, "rating", 4.82, "reviews", 8000)
            )),
            Map.entry("other", List.of(
                    Map.<String, Object>of("id", "other", "name", "Other Service", "slug", "other", "price", 199, "rating", 4.7, "reviews", 5000)
            ))
    );

    @GetMapping("/categories")
    public ResponseEntity<List<Map<String, String>>> categories() {
        return ResponseEntity.ok(CATEGORIES);
    }

    @GetMapping
    public ResponseEntity<?> listByCategory(@RequestParam String category) {
        List<Map<String, Object>> list = SUB_SERVICES.getOrDefault(category, List.of());
        return ResponseEntity.ok(list);
    }

    @GetMapping("/professionals-count")
    public ResponseEntity<Map<String, Long>> professionalsCount(@RequestParam(required = false) String category,
                                                                 @RequestParam(required = false) String slug) {
        String keyword = slug != null && !slug.isBlank() ? slug : (category != null ? category : "");
        long count = 0;
        if (!keyword.isBlank()) {
            count = serviceProviderRepository.countByStatusAndProfessionalTypeOrServicesOfferedContaining("submitted", keyword);
        }
        if (count == 0) {
            count = serviceProviderRepository.countByStatus("submitted");
            if (count == 0) count = 1; // dummy: show at least 1 for demo
        }
        return ResponseEntity.ok(Map.of("count", count));
    }
}
