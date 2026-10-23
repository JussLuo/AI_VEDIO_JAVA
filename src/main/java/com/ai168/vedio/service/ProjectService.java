package com.ai168.vedio.service;

import com.ai168.vedio.dto.CreateProjectRequest;
import com.ai168.vedio.entity.Project;
import java.util.List;
import java.util.Optional;

public interface ProjectService {
    Project createProject(CreateProjectRequest request);
    List<Project> listProjects();
    Optional<Project> findProjectById(String id);
}
