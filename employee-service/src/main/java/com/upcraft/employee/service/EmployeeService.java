package com.upcraft.employee.service;

import com.upcraft.dto.EmployeeDTO;
import com.upcraft.employee.entity.Employee;
import com.upcraft.employee.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmployeeService {

    private final EmployeeRepository employeeRepository;

    @Transactional(readOnly = true)
    public Page<EmployeeDTO> listEmployees(UUID tenantId, Pageable pageable) {
        Page<Employee> employees = employeeRepository.findByTenantId(tenantId, pageable);
        List<EmployeeDTO> dtos = employees.getContent().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
        return new PageImpl<>(dtos, pageable, employees.getTotalElements());
    }

    @Transactional
    public EmployeeDTO createEmployee(EmployeeDTO employeeDTO) {
        Employee employee = new Employee();
        employee.setTenantId(employeeDTO.getTenantId());
        employee.setUserId(employeeDTO.getUserId());
        employee.setName(employeeDTO.getName());
        employee.setDepartment(employeeDTO.getDepartment());
        employee.setDesignation(employeeDTO.getDesignation());
        employee.setEmail(employeeDTO.getEmail());
        employee.setPhone(employeeDTO.getPhone());
        employee.setJoinDate(employeeDTO.getJoinDate());
        employee.setSalary(employeeDTO.getSalary());

        Employee savedEmployee = employeeRepository.save(employee);
        log.info("Employee created: {}", savedEmployee.getId());
        return toDTO(savedEmployee);
    }

    @Transactional(readOnly = true)
    public EmployeeDTO getEmployeeById(UUID id) {
        return employeeRepository.findById(id)
                .map(this::toDTO)
                .orElseThrow(() -> new RuntimeException("Employee not found"));
    }

    @Transactional
    public EmployeeDTO updateEmployee(UUID id, EmployeeDTO employeeDTO) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Employee not found"));

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
    public void deleteEmployee(UUID id) {
        employeeRepository.deleteById(id);
        log.info("Employee deleted: {}", id);
    }

    private EmployeeDTO toDTO(Employee employee) {
        return EmployeeDTO.builder()
                .id(employee.getId())
                .tenantId(employee.getTenantId())
                .userId(employee.getUserId())
                .name(employee.getName())
                .department(employee.getDepartment())
                .designation(employee.getDesignation())
                .email(employee.getEmail())
                .phone(employee.getPhone())
                .joinDate(employee.getJoinDate())
                .salary(employee.getSalary())
                .createdAt(employee.getCreatedAt())
                .build();
    }
}
