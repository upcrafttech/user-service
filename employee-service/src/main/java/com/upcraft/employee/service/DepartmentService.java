package com.upcraft.employee.service;

import com.upcraft.employee.dto.DepartmentDTO;
import com.upcraft.employee.entity.Department;
import com.upcraft.employee.repository.DepartmentRepository;
import com.upcraft.exception.ValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DepartmentService {

    private final DepartmentRepository departmentRepository;

    @Transactional
    public DepartmentDTO createDepartment(DepartmentDTO request) {
        if (request == null || request.getTenantId() == null) {
            throw new ValidationException("tenantId", "tenantId is required");
        }
        if (request.getName() == null || request.getName().isBlank()) {
            throw new ValidationException("name", "name is required");
        }

        Department department = new Department();
        department.setTenantId(request.getTenantId());
        department.setName(request.getName().trim());
        department.setCode(request.getCode());
        department.setActive(request.getActive() == null ? Boolean.TRUE : request.getActive());

        return toDto(departmentRepository.save(department));
    }

    @Transactional(readOnly = true)
    public List<DepartmentDTO> listDepartments(UUID tenantId) {
        if (tenantId == null) {
            throw new ValidationException("tenantId", "tenantId is required");
        }
        return departmentRepository.findByTenantIdAndActiveTrueOrderByNameAsc(tenantId)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public DepartmentDTO getDepartment(UUID tenantId, UUID departmentId) {
        validateTenantAndDepartmentId(tenantId, departmentId);
        Department department = departmentRepository.findByTenantIdAndId(tenantId, departmentId)
                .orElseThrow(() -> new ValidationException("departmentId", "department not found for tenant"));
        return toDto(department);
    }

    @Transactional
    public DepartmentDTO updateDepartment(UUID tenantId, UUID departmentId, DepartmentDTO request) {
        validateTenantAndDepartmentId(tenantId, departmentId);
        if (request == null) {
            throw new ValidationException("payload", "payload is required");
        }
        Department department = departmentRepository.findByTenantIdAndId(tenantId, departmentId)
                .orElseThrow(() -> new ValidationException("departmentId", "department not found for tenant"));

        if (request.getName() != null && !request.getName().isBlank()) {
            department.setName(request.getName().trim());
        }
        if (request.getCode() != null) {
            department.setCode(request.getCode().trim());
        }
        if (request.getActive() != null) {
            department.setActive(request.getActive());
        }
        return toDto(departmentRepository.save(department));
    }

    @Transactional
    public void deactivateDepartment(UUID tenantId, UUID departmentId) {
        validateTenantAndDepartmentId(tenantId, departmentId);
        Department department = departmentRepository.findByTenantIdAndId(tenantId, departmentId)
                .orElseThrow(() -> new ValidationException("departmentId", "department not found for tenant"));
        department.setActive(Boolean.FALSE);
        departmentRepository.save(department);
    }

    private void validateTenantAndDepartmentId(UUID tenantId, UUID departmentId) {
        if (tenantId == null) {
            throw new ValidationException("tenantId", "tenantId is required");
        }
        if (departmentId == null) {
            throw new ValidationException("departmentId", "departmentId is required");
        }
    }

    private DepartmentDTO toDto(Department department) {
        return DepartmentDTO.builder()
                .id(department.getId())
                .tenantId(department.getTenantId())
                .name(department.getName())
                .code(department.getCode())
                .active(department.getActive())
                .createdAt(department.getCreatedAt())
                .updatedAt(department.getUpdatedAt())
                .build();
    }
}
