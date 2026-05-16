package com.upcraft.employee.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "employee")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Employee {

    @Id
    @Column(columnDefinition = "VARCHAR(36)")
    private UUID id;

    @Column(name = "tenant_id", columnDefinition = "VARCHAR(36)", nullable = false)
    private UUID tenantId;

    @Column(name = "user_id", columnDefinition = "VARCHAR(36)")
    private UUID userId;

    @Column(nullable = false)
    private String name;

    @Column
    private String department;

    @Column(name = "department_id", columnDefinition = "VARCHAR(36)")
    private UUID departmentId;

    @Column(name = "manager_id", columnDefinition = "VARCHAR(36)")
    private UUID managerId;

    @Column
    private String designation;

    @Column
    private String email;

    @Column
    private String phone;

    @Column(name = "join_date")
    private LocalDate joinDate;

    @Column
    private Long salary;

    @Column
    private String status;

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
