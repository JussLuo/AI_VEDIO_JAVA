package com.ai168.vedio.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record SaveAssetRequest(
        @NotBlank String assetType,
        @NotBlank String name,
        String shotId,
        @NotBlank String filePath,
        @NotBlank String mimeType,
        @NotNull @Min(0) Long fileSize) {
}
