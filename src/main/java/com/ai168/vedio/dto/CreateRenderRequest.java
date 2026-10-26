package com.ai168.vedio.dto;

import jakarta.validation.constraints.NotEmpty;
import java.util.List;

/** 创建剧本合成任务请求。 */
public record CreateRenderRequest(@NotEmpty List<String> videoTaskIds) {
}
