package com.skillbridge.Bridge.repository;

import com.skillbridge.Bridge.entity.CustomWorkApplication;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CustomWorkApplicationRepository extends JpaRepository<CustomWorkApplication, UUID> {

    boolean existsByRequestIdAndServiceProviderId(UUID requestId, UUID serviceProviderId);

    List<CustomWorkApplication> findByRequestIdOrderByCreatedAtAsc(UUID requestId);

    Optional<CustomWorkApplication> findByIdAndRequestId(UUID id, UUID requestId);
}
