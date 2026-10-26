package com.ai168.vedio.dto;

/** 待人工确认的分镜草稿。 */
public record ShotDraft(
        Integer shotNumber,
        String visualDescription,
        String videoPrompt,
        Integer durationSeconds) {
}
