package com.skillbridge.Bridge.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Data
public class ServiceProviderRequest {

    private String professionalType;
    private String phone;
    private String name;
    private LocalDate dateOfBirth;
    private String photoUrl;
    private String gender;
    private String servicesOffered;
    private Integer experienceYears;
    private String serviceArea;
    private String bankAccountNumber;
    private String upiId;
    private List<Map<String, String>> workingHours;
    private List<String> daysAvailable;
    private Integer travelRadiusKm;
    @NotNull
    private Boolean termsAccepted;
    private Instant termsAcceptedAt;
}
