package com.ai168.vedio.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateScriptRequest(@NotBlank String title, @NotBlank String content) {
}
