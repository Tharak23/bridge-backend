package com.skillbridge.Bridge.repository;

import com.skillbridge.Bridge.entity.Booking;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface BookingRepository extends JpaRepository<Booking, UUID> {

    List<Booking> findByHireUserClerkUserIdOrderByCreatedAtDesc(String clerkUserId);

    List<Booking> findByServiceProviderUserClerkUserIdOrderByCreatedAtDesc(String clerkUserId);

    List<Booking> findByServiceProviderUserClerkUserIdAndStatusOrderByCreatedAtDesc(String clerkUserId, String status);

    List<Booking> findByStatusOrderByCreatedAtDesc(String status);
}
