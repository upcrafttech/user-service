package com.upcraft.employee.service;

import com.upcraft.employee.dto.EmployeeHierarchyUpdateRequest;
import com.upcraft.employee.dto.EmployeeHierarchyNodeDTO;
import com.upcraft.dto.EmployeeDTO;
import com.upcraft.employee.entity.Department;
import com.upcraft.employee.entity.Employee;
import com.upcraft.employee.repository.DepartmentRepository;
import com.upcraft.employee.repository.EmployeeRepository;
import com.upcraft.exception.ResourceNotFoundException;
import com.upcraft.exception.ValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;

    @Transactional(readOnly = true)
    public Page<EmployeeDTO> listEmployees(UUID tenantId, String dept, String status, java.time.LocalDate joinedAfter, Pageable pageable) {
        if (tenantId == null) {
            throw new ValidationException("tenantId", "tenantId is required");
        }
        Page<Employee> employees = employeeRepository.findWithFilters(tenantId, dept, status, joinedAfter, pageable);
        return employees.map(this::toDTO);
    }

    @Transactional(readOnly = true)
    public com.upcraft.employee.dto.HRStatsDTO getHRStats(UUID tenantId) {
        long headcount = employeeRepository.count(); // Simplified for tenant
        return com.upcraft.employee.dto.HRStatsDTO.builder()
                .headcount(headcount)
                .retentionRate(94.5)
                .openPositions(12)
                .hiringVelocityDays(18)
                .headcountTrend(java.util.Arrays.asList(
                        new com.upcraft.employee.dto.HRStatsDTO.HeadcountDataPoint("Jan", 100L),
                        new com.upcraft.employee.dto.HRStatsDTO.HeadcountDataPoint("Feb", 105L),
                        new com.upcraft.employee.dto.HRStatsDTO.HeadcountDataPoint("Mar", 110L)
                ))
                .build();
    }

    @Transactional(readOnly = true)
    public com.upcraft.employee.dto.HeadcountTrendDTO getHeadcountTrend(UUID tenantId, String period) {
        return com.upcraft.employee.dto.HeadcountTrendDTO.builder()
                .labels(java.util.Arrays.asList("Q1", "Q2", "Q3", "Q4"))
                .values(java.util.Arrays.asList(90L, 95L, 105L, 110L))
                .build();
    }

    @Transactional(readOnly = true)
    public List<com.upcraft.employee.dto.DepartmentDistributionDTO> getDepartmentDistribution(UUID tenantId) {
        return java.util.Arrays.asList(
                new com.upcraft.employee.dto.DepartmentDistributionDTO("Engineering", 45L, "#4f46e5"),
                new com.upcraft.employee.dto.DepartmentDistributionDTO("Product", 15L, "#10b981"),
                new com.upcraft.employee.dto.DepartmentDistributionDTO("Design", 10L, "#f59e0b")
        );
    }

    @Transactional(readOnly = true)
    public List<EmployeeDTO> getRecentJoiners(UUID tenantId, int days) {
        java.time.LocalDate date = java.time.LocalDate.now().minusDays(days);
        return employeeRepository.findWithFilters(tenantId, null, null, date, Pageable.unpaged())
                .getContent().stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Transactional
    public EmployeeDTO createEmployee(EmployeeDTO employeeDTO) {
        Employee employee = new Employee();
        employee.setTenantId(employeeDTO.getTenantId());
        employee.setUserId(employeeDTO.getUserId());
        employee.setName(employeeDTO.getName());
        employee.setDepartment(employeeDTO.getDepartment());
        employee.setDepartmentId(employeeDTO.getDepartmentId());
        employee.setManagerId(employeeDTO.getManagerId());
        employee.setDesignation(employeeDTO.getDesignation());
        employee.setEmail(employeeDTO.getEmail());
        employee.setPhone(employeeDTO.getPhone());
        employee.setJoinDate(employeeDTO.getJoinDate() != null ? employeeDTO.getJoinDate() : java.time.LocalDate.now());
        employee.setSalary(employeeDTO.getSalary());
        employee.setStatus(employeeDTO.getStatus() != null ? employeeDTO.getStatus() : "ACTIVE");

        Employee savedEmployee = employeeRepository.save(employee);
        log.info("Employee created: {}", savedEmployee.getId());
        return toDTO(savedEmployee);
    }

    @Transactional(readOnly = true)
    public EmployeeDTO getEmployeeById(UUID id) {
        return employeeRepository.findById(id)
                .map(this::toDTO)
                .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", id));
    }

    @Transactional(readOnly = true)
    public EmployeeDTO getEmployeeByUserId(UUID tenantId, UUID userId) {
        return employeeRepository.findByTenantIdAndUserId(tenantId, userId)
                .map(this::toDTO)
                .orElseThrow(() -> new ResourceNotFoundException("Employee", "userId", userId));
    }

    @Transactional
    public EmployeeDTO updateEmployee(UUID id, EmployeeDTO employeeDTO) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", id));

        if (employeeDTO.getName() != null) employee.setName(employeeDTO.getName());
        if (employeeDTO.getDepartment() != null) employee.setDepartment(employeeDTO.getDepartment());
        if (employeeDTO.getDesignation() != null) employee.setDesignation(employeeDTO.getDesignation());
        if (employeeDTO.getEmail() != null) employee.setEmail(employeeDTO.getEmail());
        if (employeeDTO.getPhone() != null) employee.setPhone(employeeDTO.getPhone());
        if (employeeDTO.getSalary() != null) employee.setSalary(employeeDTO.getSalary());

        Employee updatedEmployee = employeeRepository.save(employee);
        log.info("Employee updated: {}", updatedEmployee.getId());
        return toDTO(updatedEmployee);
    }

    @Transactional
    public EmployeeDTO updateHierarchy(UUID employeeId, EmployeeHierarchyUpdateRequest request) {
        if (request == null) {
            throw new ValidationException("payload", "payload is required");
        }

        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", employeeId));

        if (request.getManagerId() != null) {
            Employee manager = employeeRepository.findById(request.getManagerId())
                    .orElseThrow(() -> new ResourceNotFoundException("Manager", "id", request.getManagerId()));
            if (!employee.getTenantId().equals(manager.getTenantId())) {
                throw new ValidationException("managerId", "manager must belong to same tenant");
            }
            if (employeeId.equals(manager.getId())) {
                throw new ValidationException("managerId", "employee cannot be own manager");
            }
            employee.setManagerId(manager.getId());
        }
        if (request.getDepartmentId() != null) {
            employee.setDepartmentId(request.getDepartmentId());
        }

        Employee updated = employeeRepository.save(employee);
        return toDTO(updated);
    }

    @Transactional
    public void deleteEmployee(UUID id) {
        employeeRepository.deleteById(id);
        log.info("Employee deleted: {}", id);
    }

    @Transactional(readOnly = true)
    public List<EmployeeHierarchyNodeDTO> getManagerChain(UUID tenantId, UUID employeeId) {
        Employee employee = employeeRepository.findById(employeeId)
                .filter(e -> tenantId.equals(e.getTenantId()))
                .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", employeeId));

        List<Employee> tenantEmployees = employeeRepository.findByTenantId(tenantId);
        Map<UUID, Employee> employeeMap = tenantEmployees.stream()
                .collect(Collectors.toMap(Employee::getId, e -> e, (a, b) -> a, HashMap::new));
        Map<UUID, Department> departmentMap = loadDepartmentMap(tenantId, tenantEmployees);

        List<EmployeeHierarchyNodeDTO> chain = new ArrayList<>();
        Set<UUID> visited = new HashSet<>();
        Employee current = employee;
        while (current != null && current.getManagerId() != null) {
            if (!visited.add(current.getId())) {
                break;
            }
            Employee manager = employeeMap.get(current.getManagerId());
            if (manager == null) {
                break;
            }
            chain.add(toHierarchyNode(current, manager, departmentMap.get(current.getDepartmentId())));
            current = manager;
        }
        return chain;
    }

    @Transactional(readOnly = true)
    public List<EmployeeHierarchyNodeDTO> getDirectReports(UUID tenantId, UUID managerId) {
        Employee manager = employeeRepository.findById(managerId)
                .filter(e -> tenantId.equals(e.getTenantId()))
                .orElseThrow(() -> new ResourceNotFoundException("Manager", "id", managerId));

        List<Employee> reports = employeeRepository.findByTenantIdAndManagerId(tenantId, manager.getId());
        Map<UUID, Department> departmentMap = loadDepartmentMap(tenantId, reports);
        return reports.stream()
                .map(report -> toHierarchyNode(report, manager, departmentMap.get(report.getDepartmentId())))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<com.upcraft.employee.dto.ShiftDTO> getShifts(UUID tenantId, UUID employeeId) {
        return java.util.Arrays.asList(
                com.upcraft.employee.dto.ShiftDTO.builder()
                        .type("Regular")
                        .startTime(java.time.LocalTime.of(9, 0))
                        .endTime(java.time.LocalTime.of(18, 0))
                        .location("Office - HQ")
                        .date(java.time.LocalDate.now())
                        .build(),
                com.upcraft.employee.dto.ShiftDTO.builder()
                        .type("Regular")
                        .startTime(java.time.LocalTime.of(9, 0))
                        .endTime(java.time.LocalTime.of(18, 0))
                        .location("Remote")
                        .date(java.time.LocalDate.now().plusDays(1))
                        .build()
        );
    }

    private EmployeeDTO toDTO(Employee employee) {
        return EmployeeDTO.builder()
                .id(employee.getId())
                .tenantId(employee.getTenantId())
                .userId(employee.getUserId())
                .name(employee.getName())
                .department(employee.getDepartment())
                .departmentId(employee.getDepartmentId())
                .managerId(employee.getManagerId())
                .designation(employee.getDesignation())
                .email(employee.getEmail())
                .phone(employee.getPhone())
                .joinDate(employee.getJoinDate())
                .salary(employee.getSalary())
                .status(employee.getStatus())
                .createdAt(employee.getCreatedAt())
                .build();
    }

    private Map<UUID, Department> loadDepartmentMap(UUID tenantId, List<Employee> employees) {
        List<UUID> deptIds = employees.stream()
                .map(Employee::getDepartmentId)
                .filter(id -> id != null)
                .distinct()
                .collect(Collectors.toList());
        if (deptIds.isEmpty()) {
            return Map.of();
        }
        return departmentRepository.findByTenantIdOrderByNameAsc(tenantId).stream()
                .filter(d -> deptIds.contains(d.getId()))
                .collect(Collectors.toMap(Department::getId, d -> d, (a, b) -> a, HashMap::new));
    }

    private EmployeeHierarchyNodeDTO toHierarchyNode(Employee employee, Employee manager, Department department) {
        return EmployeeHierarchyNodeDTO.builder()
                .employeeId(employee.getId())
                .employeeName(employee.getName())
                .managerId(manager == null ? null : manager.getId())
                .managerName(manager == null ? null : manager.getName())
                .departmentId(employee.getDepartmentId())
                .departmentName(department == null ? null : department.getName())
                .build();
    }
}
