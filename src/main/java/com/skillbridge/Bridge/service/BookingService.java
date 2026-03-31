package com.skillbridge.Bridge.service;

import com.skillbridge.Bridge.dto.CreateBookingRequest;
import com.skillbridge.Bridge.entity.Booking;
import com.skillbridge.Bridge.entity.ServiceProvider;
import com.skillbridge.Bridge.entity.User;
import com.skillbridge.Bridge.repository.BookingRepository;
import com.skillbridge.Bridge.repository.ServiceProviderRepository;
import com.skillbridge.Bridge.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookingService {

    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final ServiceProviderRepository serviceProviderRepository;

    @Transactional
    public Map<String, Object> create(String clerkUserId, CreateBookingRequest req) {
        User hireUser = userRepository.findByClerkUserId(clerkUserId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        Booking booking = Booking.builder()
                .hireUser(hireUser)
                .serviceName(req.getServiceName())
                .serviceSlug(req.getServiceSlug())
                .serviceCategory(req.getServiceCategory())
                .price(req.getPrice())
                .quantity(req.getQuantity())
                .scheduledAt(req.getScheduledAt())
                .locationText(req.getLocationText())
                .hireNotes(req.getHireNotes())
                .status("pending_acceptance")
                .build();
        booking = bookingRepository.save(booking);
        return bookingToMap(booking);
    }

    public List<Booking> findByHireUser(String clerkUserId) {
        return bookingRepository.findByHireUserClerkUserIdOrderByCreatedAtDesc(clerkUserId);
    }

    public List<Booking> findByProvider(String clerkUserId) {
        return bookingRepository.findByServiceProviderUserClerkUserIdOrderByCreatedAtDesc(clerkUserId);
    }

    public List<Booking> findPendingForProviders() {
        return bookingRepository.findByStatusOrderByCreatedAtDesc("pending_acceptance");
    }

    private Map<String, Object> bookingToMap(Booking b) {
        User hire = b.getHireUser();
        ServiceProvider sp = b.getServiceProvider();
        Map<String, Object> m = new HashMap<>();
        m.put("id", b.getId().toString());
        m.put("serviceName", b.getServiceName() != null ? b.getServiceName() : "");
        m.put("serviceSlug", b.getServiceSlug() != null ? b.getServiceSlug() : "");
        m.put("serviceCategory", b.getServiceCategory() != null ? b.getServiceCategory() : "");
        m.put("price", b.getPrice() != null ? b.getPrice() : java.math.BigDecimal.ZERO);
        m.put("quantity", b.getQuantity() != null ? b.getQuantity() : 1);
        m.put("status", b.getStatus() != null ? b.getStatus() : "");
        m.put("scheduledAt", b.getScheduledAt() != null ? b.getScheduledAt().toString() : null);
        m.put("locationText", b.getLocationText() != null ? b.getLocationText() : "");
        m.put("hireNotes", b.getHireNotes() != null ? b.getHireNotes() : "");
        m.put("createdAt", b.getCreatedAt() != null ? b.getCreatedAt().toString() : "");
        m.put("hireUserName", hire != null && hire.getName() != null ? hire.getName() : "");
        m.put("hireUserPhone", hire != null && hire.getPhone() != null ? hire.getPhone() : "");
        m.put("serviceProviderId", sp != null ? sp.getId().toString() : null);
        if (sp != null && sp.getUser() != null) {
            m.put("providerName", sp.getUser().getName() != null ? sp.getUser().getName() : "");
        } else {
            m.put("providerName", "");
        }
        return m;
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> getMyBookingsMaps(String clerkUserId) {
        return bookingRepository.findByHireUserClerkUserIdOrderByCreatedAtDesc(clerkUserId).stream()
                .map(this::bookingToMap)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> getProviderBookingsMaps(String clerkUserId) {
        return bookingRepository.findByServiceProviderUserClerkUserIdOrderByCreatedAtDesc(clerkUserId).stream()
                .map(this::bookingToMap)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> getPendingForProvidersMaps() {
        return bookingRepository.findByStatusOrderByCreatedAtDesc("pending_acceptance").stream()
                .map(this::bookingToMap)
                .collect(Collectors.toList());
    }

    @Transactional
    public Map<String, Object> accept(UUID bookingId, String providerClerkUserId) {
        Booking b = bookingRepository.findById(bookingId).orElseThrow(() -> new IllegalArgumentException("Booking not found"));
        if (!"pending_acceptance".equals(b.getStatus())) throw new IllegalArgumentException("Booking not in pending state");
        ServiceProvider sp = serviceProviderRepository.findByUserClerkUserId(providerClerkUserId)
                .orElseThrow(() -> new IllegalArgumentException("Service provider not found"));
        b.setServiceProvider(sp);
        b.setStatus("accepted");
        b = bookingRepository.save(b);
        return bookingToMap(b);
    }

    @Transactional
    public Map<String, Object> reject(UUID bookingId) {
        Booking b = bookingRepository.findById(bookingId).orElseThrow(() -> new IllegalArgumentException("Booking not found"));
        if (!"pending_acceptance".equals(b.getStatus())) throw new IllegalArgumentException("Booking not in pending state");
        b.setStatus("rejected");
        b = bookingRepository.save(b);
        return bookingToMap(b);
    }

    @Transactional
    public Map<String, Object> updateStatus(UUID bookingId, String status) {
        Booking b = bookingRepository.findById(bookingId).orElseThrow(() -> new IllegalArgumentException("Booking not found"));
        b.setStatus(status);
        b = bookingRepository.save(b);
        return bookingToMap(b);
    }
}
