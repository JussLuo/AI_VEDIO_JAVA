package com.ai168.vedio.dto;

import jakarta.validation.constraints.NotBlank;

public record UpdateScriptRequest(@NotBlank String title, @NotBlank String content, @NotBlank String status) {
}
