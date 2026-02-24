package com.skillbridge.Bridge.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class HireOnboardRequest {

    @NotBlank
    private String name;

    @NotBlank
    private String phone;

    private String addressLine;
    private String city;
    private String state;
    private String pincode;
    private BigDecimal latitude;
    private BigDecimal longitude;
}
