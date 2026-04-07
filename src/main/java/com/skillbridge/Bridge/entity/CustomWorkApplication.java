package com.skillbridge.Bridge.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
		name = "custom_work_application",
		uniqueConstraints = @UniqueConstraint(columnNames = {"request_id", "service_provider_id"})
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomWorkApplication {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "request_id", nullable = false)
	private CustomWorkRequest request;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "service_provider_id", nullable = false)
	private ServiceProvider serviceProvider;

	@Column(columnDefinition = "TEXT")
	private String message;

	@Column(nullable = false, length = 32)
	@Builder.Default
	private String status = "pending";

	@Column(nullable = false, updatable = false)
	private Instant createdAt;

	@PrePersist
	void prePersist() {
		if (createdAt == null) createdAt = Instant.now();
	}
}
