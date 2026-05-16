package com.upcraft.employee.controller;

import com.upcraft.dto.ApiResponse;
import com.upcraft.employee.dto.LeaveDecisionRequest;
import com.upcraft.employee.dto.LeaveBalanceDTO;
import com.upcraft.employee.dto.LeaveRequestDTO;
import com.upcraft.employee.dto.LeaveStatsDTO;
import com.upcraft.employee.entity.LeaveStatus;
import com.upcraft.employee.service.LeaveRequestService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/leaves")
@RequiredArgsConstructor
@Tag(name = "Leave Requests", description = "Leave request and approval endpoints")
public class LeaveRequestController {

    private final LeaveRequestService leaveRequestService;

    @PostMapping
    @Operation(summary = "Create leave request", description = "Create a new leave request")
    public ResponseEntity<ApiResponse<LeaveRequestDTO>> createLeaveRequest(@RequestBody LeaveRequestDTO requestDTO) {
        LeaveRequestDTO leaveRequest = leaveRequestService.createLeaveRequest(requestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(leaveRequest));
    }

    @GetMapping
    @Operation(summary = "List leave requests", description = "List leave requests by tenant and optional filters")
    public ResponseEntity<ApiResponse<List<LeaveRequestDTO>>> listLeaveRequests(
            @RequestParam UUID tenantId,
            @RequestParam(required = false) UUID employeeId,
            @RequestParam(required = false) UUID departmentId,
            @RequestParam(required = false) LeaveStatus status,
            @RequestParam(required = false) com.upcraft.employee.entity.LeaveType type) {
        List<LeaveRequestDTO> leaveRequests = leaveRequestService.listLeaveRequests(tenantId, employeeId, departmentId, status, type);
        return ResponseEntity.ok(ApiResponse.success(leaveRequests));
    }

    @GetMapping("/conflict-check")
    @Operation(summary = "Check leave conflicts", description = "Check if leave request overlaps with others in same department")
    public ResponseEntity<ApiResponse<java.util.Map<String, Object>>> checkConflicts(
            @RequestParam UUID tenantId,
            @RequestParam UUID employeeId,
            @RequestParam @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) java.time.LocalDate from,
            @RequestParam @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) java.time.LocalDate to) {
        return ResponseEntity.ok(ApiResponse.success(leaveRequestService.checkConflicts(tenantId, employeeId, from, to)));
    }

    @GetMapping(value = "/export", produces = "text/csv")
    @Operation(summary = "Export leaves", description = "Export leave requests as CSV")
    public ResponseEntity<String> exportLeaves(
            @RequestParam UUID tenantId,
            @RequestParam(required = false) LeaveStatus status) {
        String csv = leaveRequestService.exportLeaves(tenantId, status);
        return ResponseEntity.ok()
                .header(org.springframework.http.HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=leaves.csv")
                .contentType(org.springframework.http.MediaType.parseMediaType("text/csv"))
                .body(csv);
    }

    @PostMapping("/{id}/query")
    @Operation(summary = "Query leave request", description = "Ask for more information or clarification on a leave request")
    public ResponseEntity<ApiResponse<LeaveRequestDTO>> queryLeave(
            @PathVariable UUID id,
            @RequestParam String queryNote,
            @RequestParam UUID queriedBy) {
        return ResponseEntity.ok(ApiResponse.success(leaveRequestService.queryLeave(id, queryNote, queriedBy)));
    }

    @PostMapping("/{id}/decision")
    @Operation(summary = "Approve or reject leave", description = "Update leave request status with approver ID")
    public ResponseEntity<ApiResponse<LeaveRequestDTO>> decideLeave(
            @PathVariable UUID id,
            @RequestParam LeaveStatus status,
            @RequestParam UUID approvedBy) {
        try {
            LeaveRequestDTO response = leaveRequestService.decideLeaveRequest(id, status, approvedBy);
            return ResponseEntity.ok(ApiResponse.success(response));
        } catch (Exception e) {
            log.error("Error deciding leave request: {}", id, e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage(), "LEAVE_DECISION_ERROR"));
        }
    }

    @GetMapping("/stats")
    @Operation(summary = "Get leave stats", description = "Get summary stats for leave requests by tenant")
    public ResponseEntity<ApiResponse<LeaveStatsDTO>> getStats(@RequestParam UUID tenantId) {
        return ResponseEntity.ok(ApiResponse.success(leaveRequestService.getLeaveStats(tenantId)));
    }

    @PostMapping("/bulk-approve")
    @Operation(summary = "Bulk approve leaves", description = "Approve multiple leave requests at once")
    public ResponseEntity<ApiResponse<List<LeaveRequestDTO>>> bulkApprove(
            @RequestParam UUID tenantId,
            @RequestBody List<UUID> ids,
            @RequestParam UUID approvedBy) {
        return ResponseEntity.ok(ApiResponse.success(leaveRequestService.bulkApprove(tenantId, ids, approvedBy)));
    }

    @GetMapping("/balance")
    @Operation(summary = "Get leave balances", description = "Get leave balances by tenant and employee")
    public ResponseEntity<ApiResponse<List<LeaveBalanceDTO>>> getLeaveBalances(
            @RequestParam UUID tenantId,
            @RequestParam UUID employeeId,
            @RequestParam(required = false) Integer year) {
        List<LeaveBalanceDTO> balances = leaveRequestService.getLeaveBalances(tenantId, employeeId, year);
        return ResponseEntity.ok(ApiResponse.success(balances));
    }
}
