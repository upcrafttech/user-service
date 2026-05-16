package com.upcraft.employee.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "attendance_exception")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AttendanceException {

    @Id
    @Column(columnDefinition = "VARCHAR(36)")
    private UUID id;

    @Column(name = "tenant_id", columnDefinition = "VARCHAR(36)", nullable = false)
    private UUID tenantId;

    @Column(name = "employee_id", columnDefinition = "VARCHAR(36)", nullable = false)
    private UUID employeeId;

    @Column(name = "exception_type")
    private String exceptionType;

    @Column
    private LocalDate date;

    @Column
    private String description;

    @Column
    private String status; // PENDING, RESOLVED

    @Column(name = "resolved_by")
    private String resolvedBy;

    @Column
    private String notes;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (id == null) {
            id = UUID.randomUUID();
        }
        createdAt = LocalDateTime.now();
        if (status == null) {
            status = "PENDING";
        }
    }
}
