package com.upcraft.employee.service;

import com.upcraft.employee.dto.LeaveRequestDTO;
import com.upcraft.employee.dto.LeaveStatsDTO;
import com.upcraft.employee.dto.LeaveBalanceDTO;
import com.upcraft.employee.entity.Employee;
import com.upcraft.employee.entity.LeaveBalance;
import com.upcraft.employee.entity.LeavePolicy;
import com.upcraft.employee.entity.LeaveRequest;
import com.upcraft.employee.entity.LeaveStatus;
import com.upcraft.employee.entity.LeaveType;
import com.upcraft.employee.repository.EmployeeRepository;
import com.upcraft.employee.repository.LeaveBalanceRepository;
import com.upcraft.employee.repository.LeaveRequestRepository;
import com.upcraft.exception.AuthorizationException;
import com.upcraft.exception.ResourceNotFoundException;
import com.upcraft.exception.ValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class LeaveRequestService {

    private final LeaveRequestRepository leaveRequestRepository;
    private final EmployeeRepository employeeRepository;
    private final LeavePolicyService leavePolicyService;
    private final LeaveBalanceRepository leaveBalanceRepository;

    @Transactional
    public LeaveRequestDTO createLeaveRequest(LeaveRequestDTO requestDTO) {
        validateCreateRequest(requestDTO);
        Employee employee = ensureEmployeeExists(requestDTO.getTenantId(), requestDTO.getEmployeeId());
        validateNoOverlap(requestDTO);
        validateBlackoutWindow(requestDTO);
        validateLeaveBalanceOnRequest(requestDTO);

        LeaveRequest leaveRequest = new LeaveRequest();
        leaveRequest.setTenantId(requestDTO.getTenantId());
        leaveRequest.setEmployeeId(requestDTO.getEmployeeId());
        leaveRequest.setLeaveType(requestDTO.getLeaveType());
        leaveRequest.setStartDate(requestDTO.getStartDate());
        leaveRequest.setEndDate(requestDTO.getEndDate());
        leaveRequest.setRequestedDays(calculateRequestedDays(requestDTO.getStartDate(), requestDTO.getEndDate()));
        leaveRequest.setReason(requestDTO.getReason());
        leaveRequest.setAlternateApproverId(resolveAlternateApprover(requestDTO, employee));
        leaveRequest.setStatus(LeaveStatus.PENDING);

        LeaveRequest saved = leaveRequestRepository.save(leaveRequest);
        log.info("Leave request created: {}", saved.getId());
        return toDto(saved);
    }

    @Transactional(readOnly = true)
    public List<LeaveRequestDTO> listLeaveRequests(UUID tenantId, UUID employeeId, UUID departmentId, LeaveStatus status, com.upcraft.employee.entity.LeaveType type) {
        if (tenantId == null) {
            throw new ValidationException("tenantId", "tenantId is required");
        }
        // departmentId is currently ignored in the repository but present for API parity
        return leaveRequestRepository.findWithFilters(tenantId, employeeId, status, type)
                .stream().map(this::toDto).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Map<String, Object> checkConflicts(UUID tenantId, UUID employeeId, LocalDate from, LocalDate to) {
        List<LeaveRequest> overlaps = leaveRequestRepository.findByTenantIdAndStartDateLessThanEqualAndEndDateGreaterThanEqualOrderByCreatedAtDesc(
                tenantId, to, from);
        
        List<LeaveRequest> otherOverlaps = overlaps.stream()
                .filter(r -> !r.getEmployeeId().equals(employeeId) && r.getStatus() == LeaveStatus.APPROVED)
                .collect(Collectors.toList());

        return Map.of(
            "hasConflict", !otherOverlaps.isEmpty(),
            "overlappingEmployees", otherOverlaps.stream().map(LeaveRequest::getEmployeeId).collect(Collectors.toList())
        );
    }

    @Transactional
    public LeaveRequestDTO queryLeave(UUID id, String queryNote, UUID queriedBy) {
        LeaveRequest req = leaveRequestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("LeaveRequest", "id", id));
        req.setQueryNote(queryNote);
        req.setQueriedBy(queriedBy);
        return toDto(leaveRequestRepository.save(req));
    }

    @Transactional(readOnly = true)
    public String exportLeaves(UUID tenantId, LeaveStatus status) {
        List<LeaveRequest> requests = leaveRequestRepository.findWithFilters(tenantId, null, status, null);
        StringBuilder csv = new StringBuilder("ID,EmployeeID,Type,From,To,Days,Status,Reason,QueryNote\n");
        for (LeaveRequest r : requests) {
            csv.append(String.format("%s,%s,%s,%s,%s,%d,%s,%s,%s\n",
                    r.getId(), r.getEmployeeId(), r.getLeaveType(), r.getStartDate(), r.getEndDate(),
                    r.getRequestedDays(), r.getStatus(), r.getReason(), r.getQueryNote()));
        }
        return csv.toString();
    }

    @Transactional
    public LeaveRequestDTO decideLeaveRequest(UUID id, LeaveStatus status, UUID approvedBy) {
        if (status == null || status == LeaveStatus.PENDING) {
            throw new ValidationException("status", "status must be APPROVED or REJECTED");
        }
        if (approvedBy == null) {
            throw new ValidationException("approvedBy", "approvedBy is required");
        }

        LeaveRequest leaveRequest = leaveRequestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("LeaveRequest", "id", id));

        if (leaveRequest.getStatus() != LeaveStatus.PENDING) {
            throw new ValidationException("status", "leave request is already finalized");
        }

        validateApproverAccess(leaveRequest, approvedBy);

        if (status == LeaveStatus.APPROVED) {
            deductBalance(leaveRequest);
        }

        leaveRequest.setStatus(status);
        leaveRequest.setApprovedBy(approvedBy);
        leaveRequest.setApprovedAt(LocalDateTime.now());

        LeaveRequest saved = leaveRequestRepository.save(leaveRequest);
        log.info("Leave request {} updated to {}", saved.getId(), saved.getStatus());
        return toDto(saved);
    }

    @Transactional(readOnly = true)
    public List<LeaveBalanceDTO> getLeaveBalances(UUID tenantId, UUID employeeId, Integer year) {
        if (tenantId == null) {
            throw new ValidationException("tenantId", "tenantId is required");
        }
        if (employeeId == null) {
            throw new ValidationException("employeeId", "employeeId is required");
        }
        int resolvedYear = year == null ? LocalDate.now().getYear() : year;
        ensureEmployeeExists(tenantId, employeeId);

        return leaveBalanceRepository.findByTenantIdAndEmployeeIdAndYearOrderByLeaveTypeAsc(tenantId, employeeId, resolvedYear)
                .stream()
                .map(this::toBalanceDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public LeaveStatsDTO getLeaveStats(UUID tenantId) {
        if (tenantId == null) {
            throw new ValidationException("tenantId", "tenantId is required");
        }
        long pending = leaveRequestRepository.countByTenantIdAndStatus(tenantId, LeaveStatus.PENDING);
        long onLeaveToday = leaveRequestRepository.countByTenantIdAndStatusAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
                tenantId, LeaveStatus.APPROVED, LocalDate.now(), LocalDate.now());
        
        return LeaveStatsDTO.builder()
                .pendingRequests(pending)
                .onLeaveToday(onLeaveToday)
                .approvalRate("94.2%") // Placeholder
                .urgentExceptions(3) // Placeholder
                .build();
    }

    @Transactional
    public List<LeaveRequestDTO> bulkApprove(UUID tenantId, List<UUID> ids, UUID approvedBy) {
        if (tenantId == null || ids == null || ids.isEmpty() || approvedBy == null) {
            throw new ValidationException("missing required fields for bulk approve");
        }
        List<LeaveRequest> requests = leaveRequestRepository.findByTenantIdAndIdIn(tenantId, ids);
        List<LeaveRequestDTO> results = new java.util.ArrayList<>();
        for (LeaveRequest req : requests) {
            if (req.getStatus() == LeaveStatus.PENDING) {
                results.add(decideLeaveRequest(req.getId(), LeaveStatus.APPROVED, approvedBy));
            }
        }
        return results;
    }

    private void validateCreateRequest(LeaveRequestDTO requestDTO) {
        if (requestDTO == null) {
            throw new ValidationException("Leave request payload is required");
        }
        if (requestDTO.getTenantId() == null) {
            throw new ValidationException("tenantId", "tenantId is required");
        }
        if (requestDTO.getEmployeeId() == null) {
            throw new ValidationException("employeeId", "employeeId is required");
        }
        if (requestDTO.getLeaveType() == null) {
            throw new ValidationException("leaveType", "leaveType is required");
        }
        LocalDate startDate = requestDTO.getStartDate();
        LocalDate endDate = requestDTO.getEndDate();
        if (startDate == null || endDate == null) {
            throw new ValidationException("dateRange", "startDate and endDate are required");
        }
        if (endDate.isBefore(startDate)) {
            throw new ValidationException("dateRange", "endDate must be greater than or equal to startDate");
        }
    }

    private Employee ensureEmployeeExists(UUID tenantId, UUID employeeId) {
        return employeeRepository.findById(employeeId)
                .filter(employee -> tenantId.equals(employee.getTenantId()))
                .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", employeeId));
    }

    private void validateNoOverlap(LeaveRequestDTO requestDTO) {
        List<LeaveStatus> activeStatuses = Arrays.asList(LeaveStatus.PENDING, LeaveStatus.APPROVED);
        List<LeaveRequest> overlaps = leaveRequestRepository
                .findByTenantIdAndEmployeeIdAndStatusInAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
                        requestDTO.getTenantId(),
                        requestDTO.getEmployeeId(),
                        activeStatuses,
                        requestDTO.getEndDate(),
                        requestDTO.getStartDate()
                );

        if (!overlaps.isEmpty()) {
            throw new ValidationException("dateRange", "leave request overlaps with existing pending/approved request");
        }
    }

    private void validateBlackoutWindow(LeaveRequestDTO requestDTO) {
        if (requestDTO.getLeaveType() == LeaveType.UNPAID) {
            return;
        }
        boolean inBlackout = leavePolicyService.isInBlackoutWindow(
                requestDTO.getTenantId(),
                requestDTO.getLeaveType(),
                requestDTO.getStartDate(),
                requestDTO.getEndDate()
        );
        if (inBlackout) {
            throw new ValidationException("dateRange", "leave request falls in blackout window");
        }
    }

    private void validateLeaveBalanceOnRequest(LeaveRequestDTO requestDTO) {
        if (requestDTO.getLeaveType() == LeaveType.UNPAID) {
            return;
        }
        int year = requestDTO.getStartDate().getYear();
        int requestedDays = calculateRequestedDays(requestDTO.getStartDate(), requestDTO.getEndDate());
        LeaveBalance balance = leavePolicyService.ensureLeaveBalance(
                requestDTO.getTenantId(),
                requestDTO.getEmployeeId(),
                requestDTO.getLeaveType(),
                year
        );
        if (balance.getAvailable() < requestedDays) {
            throw new ValidationException("leaveBalance", "insufficient leave balance for requested range");
        }
    }

    private void validateApproverAccess(LeaveRequest leaveRequest, UUID approvedBy) {
        Employee employee = ensureEmployeeExists(leaveRequest.getTenantId(), leaveRequest.getEmployeeId());
        if (approvedBy.equals(employee.getManagerId()) || approvedBy.equals(leaveRequest.getAlternateApproverId())) {
            return;
        }
        throw new AuthorizationException("approver must be manager or alternate approver");
    }

    private void deductBalance(LeaveRequest leaveRequest) {
        if (leaveRequest.getLeaveType() == LeaveType.UNPAID) {
            return;
        }
        int leaveYear = leaveRequest.getStartDate().getYear();
        LeaveBalance balance = leavePolicyService.ensureLeaveBalanceForUpdate(
                leaveRequest.getTenantId(),
                leaveRequest.getEmployeeId(),
                leaveRequest.getLeaveType(),
                leaveYear
        );
        if (balance.getAvailable() < leaveRequest.getRequestedDays()) {
            throw new ValidationException("leaveBalance", "insufficient leave balance at approval time");
        }
        balance.setUsed(balance.getUsed() + leaveRequest.getRequestedDays());
        balance.setAvailable(balance.getAllocated() - balance.getUsed());
        leaveBalanceRepository.save(balance);
    }

    private int calculateRequestedDays(LocalDate startDate, LocalDate endDate) {
        return (int) ChronoUnit.DAYS.between(startDate, endDate) + 1;
    }

    private UUID resolveAlternateApprover(LeaveRequestDTO requestDTO, Employee employee) {
        if (requestDTO.getAlternateApproverId() != null) {
            return requestDTO.getAlternateApproverId();
        }
        return employee.getManagerId();
    }

    private LeaveRequestDTO toDto(LeaveRequest leaveRequest) {
        return LeaveRequestDTO.builder()
                .id(leaveRequest.getId())
                .tenantId(leaveRequest.getTenantId())
                .employeeId(leaveRequest.getEmployeeId())
                .leaveType(leaveRequest.getLeaveType())
                .status(leaveRequest.getStatus())
                .startDate(leaveRequest.getStartDate())
                .endDate(leaveRequest.getEndDate())
                .requestedDays(leaveRequest.getRequestedDays())
                .reason(leaveRequest.getReason())
                .alternateApproverId(leaveRequest.getAlternateApproverId())
                .approvedBy(leaveRequest.getApprovedBy())
                .approvedAt(leaveRequest.getApprovedAt())
                .queryNote(leaveRequest.getQueryNote())
                .createdAt(leaveRequest.getCreatedAt())
                .updatedAt(leaveRequest.getUpdatedAt())
                .build();
    }

    private LeaveBalanceDTO toBalanceDto(LeaveBalance leaveBalance) {
        return LeaveBalanceDTO.builder()
                .id(leaveBalance.getId())
                .tenantId(leaveBalance.getTenantId())
                .employeeId(leaveBalance.getEmployeeId())
                .leaveType(leaveBalance.getLeaveType())
                .year(leaveBalance.getYear())
                .allocated(leaveBalance.getAllocated())
                .used(leaveBalance.getUsed())
                .available(leaveBalance.getAvailable())
                .carryForward(leaveBalance.getCarryForward())
                .updatedAt(leaveBalance.getUpdatedAt())
                .build();
    }
}
