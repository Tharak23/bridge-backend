package com.skillbridge.Bridge.repository;

import com.skillbridge.Bridge.entity.ServiceProvider;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface ServiceProviderRepository extends JpaRepository<ServiceProvider, UUID> {

    Optional<ServiceProvider> findByUserClerkUserId(String clerkUserId);

    long countByStatus(String status);

    long countByStatusAndProfessionalType(String status, String professionalType);

    @Query("SELECT COUNT(sp) FROM ServiceProvider sp WHERE sp.status = :status AND (sp.professionalType = :keyword OR LOWER(sp.servicesOffered) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    long countByStatusAndProfessionalTypeOrServicesOfferedContaining(@Param("status") String status, @Param("keyword") String keyword);
}
