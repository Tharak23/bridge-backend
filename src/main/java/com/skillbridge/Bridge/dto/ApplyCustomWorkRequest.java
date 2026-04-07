package com.skillbridge.Bridge.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ApplyCustomWorkRequest {

	@Size(max = 2000)
	private String message;
}
