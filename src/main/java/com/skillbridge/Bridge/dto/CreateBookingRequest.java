package com.skillbridge.Bridge.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;

@Data
public class CreateBookingRequest {

    @NotBlank
    private String serviceName;

    @NotBlank
    private String serviceSlug;

    @NotBlank
    private String serviceCategory;

    @NotNull
    @DecimalMin("0")
    private BigDecimal price;

    @NotNull
    @Min(1)
    private Integer quantity = 1;

    private Instant scheduledAt;
    private String locationText;
    private String hireNotes;
}
