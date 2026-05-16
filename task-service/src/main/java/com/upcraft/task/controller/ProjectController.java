package com.upcraft.task.controller;

import com.upcraft.dto.ApiResponse;
import com.upcraft.task.dto.ProjectDTO;
import com.upcraft.task.service.ProjectService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
@Tag(name = "Projects", description = "Project management for grouping tasks")
public class ProjectController {

    private final ProjectService projectService;

    @GetMapping
    @Operation(summary = "List projects", description = "Get all projects for a tenant")
    public ResponseEntity<ApiResponse<List<ProjectDTO>>> listProjects(@RequestParam UUID tenantId) {
        return ResponseEntity.ok(ApiResponse.success(projectService.listProjects(tenantId)));
    }

    @PostMapping
    @Operation(summary = "Create project", description = "Create a new project")
    public ResponseEntity<ApiResponse<ProjectDTO>> createProject(@RequestBody ProjectDTO projectDTO) {
        return ResponseEntity.ok(ApiResponse.success(projectService.createProject(projectDTO)));
    }
}
