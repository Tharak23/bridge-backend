package com.skillbridge.Bridge.repository;

import com.skillbridge.Bridge.entity.Booking;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface BookingRepository extends JpaRepository<Booking, UUID> {

    @EntityGraph(attributePaths = {"hireUser", "serviceProvider", "serviceProvider.user"})
    List<Booking> findByHireUserClerkUserIdOrderByCreatedAtDesc(String clerkUserId);

    @EntityGraph(attributePaths = {"hireUser", "serviceProvider", "serviceProvider.user"})
    List<Booking> findByServiceProviderUserClerkUserIdOrderByCreatedAtDesc(String clerkUserId);

    List<Booking> findByServiceProviderUserClerkUserIdAndStatusOrderByCreatedAtDesc(String clerkUserId, String status);

    @EntityGraph(attributePaths = {"hireUser", "serviceProvider", "serviceProvider.user"})
    List<Booking> findByStatusOrderByCreatedAtDesc(String status);

    long countByStatus(String status);
}
