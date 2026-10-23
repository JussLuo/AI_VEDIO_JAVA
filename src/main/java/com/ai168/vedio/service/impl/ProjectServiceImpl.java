package com.ai168.vedio.service.impl;

import com.ai168.vedio.dto.CreateProjectRequest;
import com.ai168.vedio.mapper.ProjectMapper;
import com.ai168.vedio.entity.Project;
import com.ai168.vedio.service.ProjectService;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class ProjectServiceImpl implements ProjectService {
    private final ProjectMapper projectMapper;

    public ProjectServiceImpl(ProjectMapper projectMapper) { this.projectMapper = projectMapper; }
    @Override public Project createProject(CreateProjectRequest request) {
        Instant now = Instant.now();
        Project project = new Project(UUID.randomUUID().toString(), request.name().trim(), request.description(), now, now);
        projectMapper.insert(project);
        return project;
    }
    @Override public List<Project> listProjects() { return projectMapper.selectAll(); }
    @Override public Optional<Project> findProjectById(String id) { return projectMapper.selectById(id); }
}
