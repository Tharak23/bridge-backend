package com.skillbridge.Bridge.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "custom_work_request")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomWorkRequest {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "hire_user_id", nullable = false)
	private User hireUser;

	@Column(nullable = false, length = 64)
	private String category;

	@Column(nullable = false, columnDefinition = "TEXT")
	private String description;

	private LocalDate preferredDate;

	private Integer budgetMin;

	@Column(length = 512)
	private String locationText;

	@Column(nullable = false, length = 32)
	@Builder.Default
	private String status = "open";

	@Column(name = "linked_booking_id")
	private UUID linkedBookingId;

	@OneToMany(mappedBy = "request", cascade = CascadeType.ALL, orphanRemoval = false)
	@Builder.Default
	private List<CustomWorkApplication> applications = new ArrayList<>();

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
