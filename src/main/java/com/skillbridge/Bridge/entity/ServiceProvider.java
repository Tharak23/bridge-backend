package com.skillbridge.Bridge.entity;

import jakarta.persistence.*;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import lombok.*;

@Entity
@Table(name = "service_provider")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ServiceProvider {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    private String professionalType;
    private LocalDate dateOfBirth;
    private String photoUrl;
    private String gender;
    private String servicesOffered;
    private Integer experienceYears;
    private String serviceArea;
    private String bankAccountNumber;
    private String upiId;

    @Column(name = "working_hours", columnDefinition = "text")
    private String workingHoursJson; // JSON: [{"day":"monday","start":"09:00","end":"17:00"},...]

    @Column(name = "days_available", columnDefinition = "text")
    private String daysAvailableJson; // JSON: ["monday","tuesday",...]

    private Integer travelRadiusKm;
    private Instant termsAcceptedAt;

    @Column(nullable = false, length = 32)
    @Builder.Default
    private String status = "draft"; // draft | submitted

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    @PrePersist
    void prePersist() {
        Instant now = Instant.now();
        if (createdAt == null) createdAt = now;
        if (updatedAt == null) updatedAt = now;
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = Instant.now();
    }
}
