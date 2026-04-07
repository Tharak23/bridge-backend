package com.skillbridge.Bridge.service;

import com.skillbridge.Bridge.dto.*;
import com.skillbridge.Bridge.entity.*;
import com.skillbridge.Bridge.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CustomWorkRequestService {

	private static final int MAX_TITLE_LEN = 120;

	private final CustomWorkRequestRepository requestRepository;
	private final CustomWorkApplicationRepository applicationRepository;
	private final UserRepository userRepository;
	private final ServiceProviderRepository serviceProviderRepository;
	private final BookingRepository bookingRepository;

	@Transactional
	public Map<String, Object> create(String hireClerkId, CreateCustomWorkRequest dto) {
		User hire = userRepository.findByClerkUserId(hireClerkId)
				.orElseThrow(() -> new IllegalArgumentException("User not found"));
		CustomWorkRequest r = CustomWorkRequest.builder()
				.hireUser(hire)
				.category(dto.getCategory())
				.description(dto.getDescription())
				.preferredDate(dto.getPreferredDate())
				.budgetMin(dto.getBudgetMin())
				.locationText(dto.getLocationText())
				.status("open")
				.build();
		r = requestRepository.save(r);
		return toDetailMap(r);
	}

	@Transactional(readOnly = true)
	public List<Map<String, Object>> listMine(String hireClerkId) {
		return requestRepository.findByHireUserClerkUserIdOrderByCreatedAtDesc(hireClerkId).stream()
				.map(this::toDetailMap)
				.collect(Collectors.toList());
	}

	@Transactional(readOnly = true)
	public Map<String, Object> getOne(String hireClerkId, UUID requestId) {
		CustomWorkRequest r = requestRepository.findById(requestId)
				.orElseThrow(() -> new IllegalArgumentException("Request not found"));
		if (!r.getHireUser().getClerkUserId().equals(hireClerkId)) {
			throw new IllegalArgumentException("Not your request");
		}
		return toDetailMap(r);
	}

	@Transactional(readOnly = true)
	public List<Map<String, Object>> feedForProviders(String providerClerkId) {
		ServiceProvider sp = serviceProviderRepository.findByUserClerkUserId(providerClerkId)
				.orElseThrow(() -> new IllegalArgumentException("Service provider not found"));
		return requestRepository.findByStatusOrderByCreatedAtDesc("open").stream()
				.filter(r -> !applicationRepository.existsByRequestIdAndServiceProviderId(r.getId(), sp.getId()))
				.map(this::toFeedMap)
				.collect(Collectors.toList());
	}

	@Transactional
	public Map<String, Object> apply(UUID requestId, String providerClerkId, ApplyCustomWorkRequest dto) {
		CustomWorkRequest r = requestRepository.findById(requestId)
				.orElseThrow(() -> new IllegalArgumentException("Request not found"));
		if (!"open".equals(r.getStatus())) {
			throw new IllegalArgumentException("Request is not open for applications");
		}
		ServiceProvider sp = serviceProviderRepository.findByUserClerkUserId(providerClerkId)
				.orElseThrow(() -> new IllegalArgumentException("Service provider not found"));
		if (applicationRepository.existsByRequestIdAndServiceProviderId(r.getId(), sp.getId())) {
			throw new IllegalArgumentException("You already applied to this request");
		}
		CustomWorkApplication app = CustomWorkApplication.builder()
				.request(r)
				.serviceProvider(sp)
				.message(dto.getMessage() != null ? dto.getMessage().trim() : null)
				.status("pending")
				.build();
		applicationRepository.save(app);
		return Map.of(
				"ok", true,
				"applicationId", app.getId().toString()
		);
	}

	@Transactional
	public Map<String, Object> selectProvider(String hireClerkId, UUID requestId, SelectCustomApplicationRequest body) {
		CustomWorkRequest r = requestRepository.findById(requestId)
				.orElseThrow(() -> new IllegalArgumentException("Request not found"));
		if (!r.getHireUser().getClerkUserId().equals(hireClerkId)) {
			throw new IllegalArgumentException("Not your request");
		}
		if (!"open".equals(r.getStatus())) {
			throw new IllegalArgumentException("Request is not open");
		}
		CustomWorkApplication chosen = applicationRepository.findByIdAndRequestId(body.getApplicationId(), requestId)
				.orElseThrow(() -> new IllegalArgumentException("Application not found"));
		if (!"pending".equals(chosen.getStatus())) {
			throw new IllegalArgumentException("Application is not pending");
		}

		List<CustomWorkApplication> all = applicationRepository.findByRequestIdOrderByCreatedAtAsc(requestId);
		for (CustomWorkApplication a : all) {
			if (a.getId().equals(chosen.getId())) {
				a.setStatus("selected");
			} else if ("pending".equals(a.getStatus())) {
				a.setStatus("rejected");
			}
		}
		applicationRepository.saveAll(all);

		ServiceProvider sp = chosen.getServiceProvider();
		BigDecimal price = r.getBudgetMin() != null ? BigDecimal.valueOf(r.getBudgetMin()) : BigDecimal.ZERO;
		Instant scheduled = r.getPreferredDate() != null
				? r.getPreferredDate().atStartOfDay(ZoneId.of("Asia/Kolkata")).toInstant()
				: null;

		Booking b = Booking.builder()
				.hireUser(r.getHireUser())
				.serviceProvider(sp)
				.serviceName(truncateTitle("Custom: " + r.getDescription()))
				.serviceSlug("custom-work")
				.serviceCategory(r.getCategory())
				.price(price)
				.quantity(1)
				.status("accepted")
				.scheduledAt(scheduled)
				.locationText(r.getLocationText())
				.hireNotes(r.getDescription())
				.build();
		b = bookingRepository.save(b);

		r.setStatus("assigned");
		r.setLinkedBookingId(b.getId());
		requestRepository.save(r);

		Map<String, Object> out = new HashMap<>();
		out.put("request", toDetailMap(requestRepository.findById(requestId).orElse(r)));
		out.put("bookingId", b.getId().toString());
		out.put("assignedProviderName", providerDisplayName(sp));
		return out;
	}

	@Transactional
	public Map<String, Object> update(String hireClerkId, UUID requestId, UpdateCustomWorkRequest dto) {
		CustomWorkRequest r = requestRepository.findById(requestId)
				.orElseThrow(() -> new IllegalArgumentException("Request not found"));
		if (!r.getHireUser().getClerkUserId().equals(hireClerkId)) {
			throw new IllegalArgumentException("Not your request");
		}
		if (!"open".equals(r.getStatus())) {
			throw new IllegalArgumentException("Only open requests can be edited");
		}
		r.setCategory(dto.getCategory());
		r.setDescription(dto.getDescription());
		r.setPreferredDate(dto.getPreferredDate());
		r.setBudgetMin(dto.getBudgetMin());
		r.setLocationText(dto.getLocationText());
		r = requestRepository.save(r);
		return toDetailMap(r);
	}

	@Transactional
	public Map<String, Object> cancel(String hireClerkId, UUID requestId) {
		CustomWorkRequest r = requestRepository.findById(requestId)
				.orElseThrow(() -> new IllegalArgumentException("Request not found"));
		if (!r.getHireUser().getClerkUserId().equals(hireClerkId)) {
			throw new IllegalArgumentException("Not your request");
		}
		if (!"open".equals(r.getStatus())) {
			throw new IllegalArgumentException("Only open requests can be cancelled");
		}
		r.setStatus("cancelled");
		requestRepository.save(r);
		return toDetailMap(r);
	}

	@Transactional
	public void delete(String hireClerkId, UUID requestId) {
		CustomWorkRequest r = requestRepository.findById(requestId)
				.orElseThrow(() -> new IllegalArgumentException("Request not found"));
		if (!r.getHireUser().getClerkUserId().equals(hireClerkId)) {
			throw new IllegalArgumentException("Not your request");
		}
		if (!"open".equals(r.getStatus())) {
			throw new IllegalArgumentException("Only open requests without assignment can be deleted");
		}
		applicationRepository.deleteAll(applicationRepository.findByRequestIdOrderByCreatedAtAsc(requestId));
		requestRepository.delete(r);
	}

	private Map<String, Object> toFeedMap(CustomWorkRequest r) {
		User hire = r.getHireUser();
		Map<String, Object> m = new HashMap<>();
		m.put("id", r.getId().toString());
		m.put("category", r.getCategory());
		m.put("description", r.getDescription());
		m.put("preferredDate", r.getPreferredDate() != null ? r.getPreferredDate().toString() : null);
		m.put("budgetMin", r.getBudgetMin());
		m.put("locationText", r.getLocationText() != null ? r.getLocationText() : "");
		m.put("hireCity", hire.getCity() != null ? hire.getCity() : "");
		m.put("hireName", hire.getName() != null ? hire.getName() : "Customer");
		m.put("createdAt", r.getCreatedAt() != null ? r.getCreatedAt().toString() : null);
		return m;
	}

	private Map<String, Object> toDetailMap(CustomWorkRequest r) {
		Map<String, Object> m = toFeedMap(r);
		m.put("status", r.getStatus());
		m.put("linkedBookingId", r.getLinkedBookingId() != null ? r.getLinkedBookingId().toString() : null);
		List<Map<String, Object>> apps = applicationRepository.findByRequestIdOrderByCreatedAtAsc(r.getId()).stream()
				.map(this::applicationToMap)
				.collect(Collectors.toList());
		m.put("applications", apps);
		String assignedName = null;
		if ("assigned".equals(r.getStatus())) {
			assignedName = apps.stream()
					.filter(a -> "selected".equals(a.get("status")))
					.map(a -> (String) a.get("providerName"))
					.findFirst()
					.orElse(null);
		}
		m.put("assignedProviderName", assignedName);
		return m;
	}

	private Map<String, Object> applicationToMap(CustomWorkApplication a) {
		Map<String, Object> m = new HashMap<>();
		m.put("id", a.getId().toString());
		m.put("providerId", a.getServiceProvider().getId().toString());
		m.put("providerName", providerDisplayName(a.getServiceProvider()));
		m.put("message", a.getMessage() != null ? a.getMessage() : "");
		m.put("status", a.getStatus());
		m.put("appliedAt", a.getCreatedAt() != null ? a.getCreatedAt().toString() : null);
		return m;
	}

	private static String providerDisplayName(ServiceProvider sp) {
		if (sp.getUser() != null && sp.getUser().getName() != null && !sp.getUser().getName().isBlank()) {
			return sp.getUser().getName();
		}
		if (sp.getProfessionalType() != null && !sp.getProfessionalType().isBlank()) {
			return sp.getProfessionalType();
		}
		return "Professional";
	}

	private static String truncateTitle(String s) {
		if (s == null) return "Custom work";
		String t = s.replace('\n', ' ').trim();
		if (t.length() <= MAX_TITLE_LEN) return t;
		return t.substring(0, MAX_TITLE_LEN - 1) + "…";
	}
}
