package com.skillbridge.Bridge.repository;

import com.skillbridge.Bridge.entity.CustomWorkRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface CustomWorkRequestRepository extends JpaRepository<CustomWorkRequest, UUID> {

	List<CustomWorkRequest> findByHireUserClerkUserIdOrderByCreatedAtDesc(String clerkUserId);

	List<CustomWorkRequest> findByStatusOrderByCreatedAtDesc(String status);

	long countByStatus(String status);
}
