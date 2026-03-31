package com.skillbridge.Bridge.repository;

import com.skillbridge.Bridge.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByClerkUserId(String clerkUserId);

    boolean existsByClerkUserId(String clerkUserId);

    long countByRole(String role);
}
