package com.upcraft.employee.service;

import com.upcraft.employee.dto.LeavePolicyDTO;
import com.upcraft.employee.entity.LeaveBalance;
import com.upcraft.employee.entity.LeavePolicy;
import com.upcraft.employee.entity.LeaveType;
import com.upcraft.employee.repository.LeaveBalanceRepository;
import com.upcraft.employee.repository.LeavePolicyRepository;
import com.upcraft.exception.ResourceNotFoundException;
import com.upcraft.exception.ValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LeavePolicyService {

    private final LeavePolicyRepository leavePolicyRepository;
    private final LeaveBalanceRepository leaveBalanceRepository;

    @Transactional(readOnly = true)
    public LeavePolicy getActivePolicy(UUID tenantId, LeaveType leaveType) {
        return leavePolicyRepository.findByTenantIdAndLeaveTypeAndActiveTrue(tenantId, leaveType)
                .orElseThrow(() -> new ResourceNotFoundException("LeavePolicy", "leaveType", leaveType));
    }

    @Transactional
    public LeaveBalance ensureLeaveBalance(UUID tenantId, UUID employeeId, LeaveType leaveType, int year) {
        return leaveBalanceRepository.findByTenantIdAndEmployeeIdAndLeaveTypeAndYear(tenantId, employeeId, leaveType, year)
                .orElseGet(() -> createBalance(tenantId, employeeId, leaveType, year));
    }

    @Transactional
    public LeaveBalance ensureLeaveBalanceForUpdate(UUID tenantId, UUID employeeId, LeaveType leaveType, int year) {
        return leaveBalanceRepository.findWithLock(tenantId, employeeId, leaveType, year)
                .orElseGet(() -> createBalance(tenantId, employeeId, leaveType, year));
    }

    @Transactional(readOnly = true)
    public boolean isInBlackoutWindow(UUID tenantId, LeaveType leaveType, LocalDate startDate, LocalDate endDate) {
        LeavePolicy policy = getActivePolicy(tenantId, leaveType);
        if (policy.getBlackoutStart() == null || policy.getBlackoutEnd() == null) {
            return false;
        }
        return !(endDate.isBefore(policy.getBlackoutStart()) || startDate.isAfter(policy.getBlackoutEnd()));
    }

    @Transactional(readOnly = true)
    public List<LeavePolicyDTO> listPolicies(UUID tenantId) {
        if (tenantId == null) {
            throw new ValidationException("tenantId", "tenantId is required");
        }
        return leavePolicyRepository.findByTenantIdOrderByLeaveTypeAsc(tenantId).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public LeavePolicyDTO upsertPolicy(LeavePolicyDTO request) {
        if (request == null || request.getTenantId() == null) {
            throw new ValidationException("tenantId", "tenantId is required");
        }
        if (request.getLeaveType() == null) {
            throw new ValidationException("leaveType", "leaveType is required");
        }
        if (request.getAnnualAllocation() == null || request.getAnnualAllocation() < 0) {
            throw new ValidationException("annualAllocation", "annualAllocation must be zero or positive");
        }
        if (request.getMonthlyAccrual() == null || request.getMonthlyAccrual() < 0) {
            throw new ValidationException("monthlyAccrual", "monthlyAccrual must be zero or positive");
        }
        if (request.getMaxCarryForward() == null || request.getMaxCarryForward() < 0) {
            throw new ValidationException("maxCarryForward", "maxCarryForward must be zero or positive");
        }

        LeavePolicy policy = leavePolicyRepository.findByTenantIdAndLeaveType(request.getTenantId(), request.getLeaveType())
                .orElseGet(LeavePolicy::new);
        policy.setTenantId(request.getTenantId());
        policy.setLeaveType(request.getLeaveType());
        policy.setAnnualAllocation(request.getAnnualAllocation());
        policy.setMonthlyAccrual(request.getMonthlyAccrual());
        policy.setMaxCarryForward(request.getMaxCarryForward());
        policy.setBlackoutStart(request.getBlackoutStart());
        policy.setBlackoutEnd(request.getBlackoutEnd());
        policy.setActive(request.getActive() == null ? Boolean.TRUE : request.getActive());
        return toDto(leavePolicyRepository.save(policy));
    }

    private LeaveBalance createBalance(UUID tenantId, UUID employeeId, LeaveType leaveType, int year) {
        LeavePolicy policy = getActivePolicy(tenantId, leaveType);
        int carryForward = calculateCarryForward(tenantId, employeeId, leaveType, year - 1, policy.getMaxCarryForward());
        int allocated = policy.getAnnualAllocation() + carryForward;
        LeaveBalance balance = new LeaveBalance();
        balance.setTenantId(tenantId);
        balance.setEmployeeId(employeeId);
        balance.setLeaveType(leaveType);
        balance.setYear(year);
        balance.setAllocated(allocated);
        balance.setUsed(0);
        balance.setAvailable(allocated);
        balance.setCarryForward(carryForward);
        return leaveBalanceRepository.save(balance);
    }

    private int calculateCarryForward(UUID tenantId, UUID employeeId, LeaveType leaveType, int previousYear, int maxCarryForward) {
        return leaveBalanceRepository.findByTenantIdAndEmployeeIdAndLeaveTypeAndYear(tenantId, employeeId, leaveType, previousYear)
                .map(previous -> Math.min(previous.getAvailable(), maxCarryForward))
                .orElse(0);
    }

    private LeavePolicyDTO toDto(LeavePolicy policy) {
        return LeavePolicyDTO.builder()
                .id(policy.getId())
                .tenantId(policy.getTenantId())
                .leaveType(policy.getLeaveType())
                .annualAllocation(policy.getAnnualAllocation())
                .monthlyAccrual(policy.getMonthlyAccrual())
                .maxCarryForward(policy.getMaxCarryForward())
                .blackoutStart(policy.getBlackoutStart())
                .blackoutEnd(policy.getBlackoutEnd())
                .active(policy.getActive())
                .createdAt(policy.getCreatedAt())
                .updatedAt(policy.getUpdatedAt())
                .build();
    }
}
