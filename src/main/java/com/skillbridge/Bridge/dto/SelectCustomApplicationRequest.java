package com.skillbridge.Bridge.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class SelectCustomApplicationRequest {

    @NotNull
    private UUID applicationId;
}
