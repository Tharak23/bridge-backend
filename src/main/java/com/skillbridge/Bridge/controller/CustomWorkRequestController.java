package com.skillbridge.Bridge.controller;

import com.skillbridge.Bridge.dto.*;
import com.skillbridge.Bridge.service.CustomWorkRequestService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/custom-requests")
@RequiredArgsConstructor
public class CustomWorkRequestController {

	private final CustomWorkRequestService customWorkRequestService;

	private String clerkUserId(Jwt jwt) {
		return jwt != null ? jwt.getSubject() : null;
	}

	@PostMapping
	public ResponseEntity<?> create(@AuthenticationPrincipal Jwt jwt,
			@Valid @RequestBody CreateCustomWorkRequest body) {
		String clerkId = clerkUserId(jwt);
		if (clerkId == null) return ResponseEntity.status(401).build();
		try {
			return ResponseEntity.ok(customWorkRequestService.create(clerkId, body));
		} catch (IllegalArgumentException e) {
			return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
		}
	}

	@GetMapping("/my")
	public ResponseEntity<?> myRequests(@AuthenticationPrincipal Jwt jwt) {
		String clerkId = clerkUserId(jwt);
		if (clerkId == null) return ResponseEntity.status(401).build();
		return ResponseEntity.ok(customWorkRequestService.listMine(clerkId));
	}

	@GetMapping("/feed")
	public ResponseEntity<?> feed(@AuthenticationPrincipal Jwt jwt) {
		String clerkId = clerkUserId(jwt);
		if (clerkId == null) return ResponseEntity.status(401).build();
		try {
			return ResponseEntity.ok(customWorkRequestService.feedForProviders(clerkId));
		} catch (IllegalArgumentException e) {
			return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
		}
	}

	@GetMapping("/{id}")
	public ResponseEntity<?> one(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id) {
		String clerkId = clerkUserId(jwt);
		if (clerkId == null) return ResponseEntity.status(401).build();
		try {
			return ResponseEntity.ok(customWorkRequestService.getOne(clerkId, id));
		} catch (IllegalArgumentException e) {
			return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
		}
	}

	@PostMapping("/{id}/apply")
	public ResponseEntity<?> apply(@AuthenticationPrincipal Jwt jwt,
			@PathVariable UUID id,
			@RequestBody(required = false) ApplyCustomWorkRequest body) {
		String clerkId = clerkUserId(jwt);
		if (clerkId == null) return ResponseEntity.status(401).build();
		try {
			ApplyCustomWorkRequest b = body != null ? body : new ApplyCustomWorkRequest();
			return ResponseEntity.ok(customWorkRequestService.apply(id, clerkId, b));
		} catch (IllegalArgumentException e) {
			return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
		}
	}

	@PatchMapping("/{id}/select-provider")
	public ResponseEntity<?> selectProvider(@AuthenticationPrincipal Jwt jwt,
			@PathVariable UUID id,
			@Valid @RequestBody SelectCustomApplicationRequest body) {
		String clerkId = clerkUserId(jwt);
		if (clerkId == null) return ResponseEntity.status(401).build();
		try {
			return ResponseEntity.ok(customWorkRequestService.selectProvider(clerkId, id, body));
		} catch (IllegalArgumentException e) {
			return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
		}
	}

	@PutMapping("/{id}")
	public ResponseEntity<?> update(@AuthenticationPrincipal Jwt jwt,
			@PathVariable UUID id,
			@Valid @RequestBody UpdateCustomWorkRequest body) {
		String clerkId = clerkUserId(jwt);
		if (clerkId == null) return ResponseEntity.status(401).build();
		try {
			return ResponseEntity.ok(customWorkRequestService.update(clerkId, id, body));
		} catch (IllegalArgumentException e) {
			return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
		}
	}

	@PatchMapping("/{id}/cancel")
	public ResponseEntity<?> cancel(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id) {
		String clerkId = clerkUserId(jwt);
		if (clerkId == null) return ResponseEntity.status(401).build();
		try {
			return ResponseEntity.ok(customWorkRequestService.cancel(clerkId, id));
		} catch (IllegalArgumentException e) {
			return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
		}
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<?> delete(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id) {
		String clerkId = clerkUserId(jwt);
		if (clerkId == null) return ResponseEntity.status(401).build();
		try {
			customWorkRequestService.delete(clerkId, id);
			return ResponseEntity.noContent().build();
		} catch (IllegalArgumentException e) {
			return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
		}
	}
}
