package com.ai168.vedio.service;

import com.ai168.vedio.dto.ShotDraft;
import java.util.List;

public interface ShotDraftService {
    List<ShotDraft> generateDrafts(String scriptId);
}
