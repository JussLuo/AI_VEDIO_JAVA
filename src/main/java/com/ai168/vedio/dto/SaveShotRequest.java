package com.ai168.vedio.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record SaveShotRequest(
        @NotNull @Min(1) Integer shotNumber,
        @NotBlank String visualDescription,
        @NotBlank String videoPrompt,
        @NotNull @Min(1) @Max(60) Integer durationSeconds) {
}
