package com.skillbridge.Bridge.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

@Data
public class CreateCustomWorkRequest {

	@NotBlank
	@Size(max = 64)
	private String category;

	@NotBlank
	@Size(max = 8000)
	private String description;

	private LocalDate preferredDate;

	private Integer budgetMin;

	@Size(max = 512)
	private String locationText;
}
