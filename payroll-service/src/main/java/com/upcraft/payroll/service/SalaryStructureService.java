package com.upcraft.payroll.service;

import com.upcraft.dto.SalaryStructureDTO;
import com.upcraft.exception.ValidationException;
import com.upcraft.payroll.entity.SalaryStructure;
import com.upcraft.payroll.repository.SalaryStructureRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SalaryStructureService {

    private final SalaryStructureRepository salaryStructureRepository;

    @Transactional
    public SalaryStructureDTO createSalaryStructure(SalaryStructureDTO dto) {
        if (dto == null) {
            throw new ValidationException("payload", "salary structure payload is required");
        }
        if (dto.getTenantId() == null || dto.getEmployeeId() == null) {
            throw new ValidationException("tenantId/employeeId", "tenantId and employeeId are required");
        }

        SalaryStructure entity = new SalaryStructure();
        entity.setTenantId(dto.getTenantId());
        entity.setEmployeeId(dto.getEmployeeId());
        entity.setBasicPay(zeroSafe(dto.getBasicPay()));
        entity.setHra(zeroSafe(dto.getHra()));
        entity.setOtherAllowances(zeroSafe(dto.getOtherAllowances()));

        SalaryStructure saved = salaryStructureRepository.save(entity);
        log.info("Salary structure created for employee {}", saved.getEmployeeId());
        return toDto(saved);
    }

    @Transactional(readOnly = true)
    public List<SalaryStructureDTO> listSalaryStructures(UUID tenantId) {
        if (tenantId == null) {
            throw new ValidationException("tenantId", "tenantId is required");
        }
        return salaryStructureRepository.findByTenantId(tenantId).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    private long zeroSafe(Long value) {
        return value == null ? 0L : value;
    }

    private SalaryStructureDTO toDto(SalaryStructure salaryStructure) {
        return SalaryStructureDTO.builder()
                .id(salaryStructure.getId())
                .tenantId(salaryStructure.getTenantId())
                .employeeId(salaryStructure.getEmployeeId())
                .basicPay(salaryStructure.getBasicPay())
                .hra(salaryStructure.getHra())
                .otherAllowances(salaryStructure.getOtherAllowances())
                .build();
    }
}

