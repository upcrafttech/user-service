package com.upcraft.payroll.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "payslip")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Payslip {

    @Id
    @Column(columnDefinition = "VARCHAR(36)")
    private UUID id;

    @Column(name = "payroll_run_id", columnDefinition = "VARCHAR(36)", nullable = false)
    private UUID payrollRunId;

    @Column(name = "tenant_id", columnDefinition = "VARCHAR(36)", nullable = false)
    private UUID tenantId;

    @Column(name = "employee_id", columnDefinition = "VARCHAR(36)", nullable = false)
    private UUID employeeId;

    @Column(name = "gross_pay", nullable = false)
    private Long grossPay;

    @Column(name = "tds", nullable = false)
    private Long tds;

    @Column(name = "pf", nullable = false)
    private Long pf;

    @Column(name = "esi", nullable = false)
    private Long esi;

    @Column(name = "professional_tax", nullable = false)
    private Long professionalTax;

    @Column(name = "net_pay", nullable = false)
    private Long netPay;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (id == null) {
            id = UUID.randomUUID();
        }
        createdAt = LocalDateTime.now();
    }
}

