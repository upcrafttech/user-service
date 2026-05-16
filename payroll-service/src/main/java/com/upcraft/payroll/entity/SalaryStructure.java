package com.upcraft.payroll.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "salary_structure")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SalaryStructure {

    @Id
    @Column(columnDefinition = "VARCHAR(36)")
    private UUID id;

    @Column(name = "tenant_id", columnDefinition = "VARCHAR(36)", nullable = false)
    private UUID tenantId;

    @Column(name = "employee_id", columnDefinition = "VARCHAR(36)", nullable = false)
    private UUID employeeId;

    @Column(name = "basic_pay", nullable = false)
    private Long basicPay;

    @Column(name = "hra", nullable = false)
    private Long hra;

    @Column(name = "other_allowances", nullable = false)
    private Long otherAllowances;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        if (id == null) {
            id = UUID.randomUUID();
        }
        if (basicPay == null) {
            basicPay = 0L;
        }
        if (hra == null) {
            hra = 0L;
        }
        if (otherAllowances == null) {
            otherAllowances = 0L;
        }
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}

