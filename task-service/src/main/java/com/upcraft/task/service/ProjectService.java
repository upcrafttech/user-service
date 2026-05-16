package com.upcraft.task.service;

import com.upcraft.task.dto.ProjectDTO;
import com.upcraft.task.entity.Project;
import com.upcraft.task.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectRepository projectRepository;

    @Transactional(readOnly = true)
    public List<ProjectDTO> listProjects(UUID tenantId) {
        return projectRepository.findByTenantId(tenantId).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public ProjectDTO createProject(ProjectDTO dto) {
        Project project = new Project();
        project.setTenantId(dto.getTenantId());
        project.setName(dto.getName());
        project.setDescription(dto.getDescription());
        project.setStatus(dto.getStatus());
        return toDto(projectRepository.save(project));
    }

    private ProjectDTO toDto(Project project) {
        return ProjectDTO.builder()
                .id(project.getId())
                .tenantId(project.getTenantId())
                .name(project.getName())
                .description(project.getDescription())
                .status(project.getStatus())
                .build();
    }
}
