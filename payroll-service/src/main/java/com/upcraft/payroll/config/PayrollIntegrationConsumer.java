package com.upcraft.payroll.config;

import com.upcraft.payroll.entity.SalaryStructure;
import com.upcraft.payroll.repository.SalaryStructureRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class PayrollIntegrationConsumer {

    private final SalaryStructureRepository salaryStructureRepository;

    @RabbitListener(queues = "${payroll.bonus.queue:payroll.bonus.queue}")
    public void consumeBonusEvent(String payload) {
        // Expected format: tenantId,employeeId,bonusAmountInCents
        try {
            String[] parts = payload.split(",");
            if (parts.length < 3) {
                return;
            }
            UUID tenantId = UUID.fromString(parts[0].trim());
            UUID employeeId = UUID.fromString(parts[1].trim());
            long bonus = Long.parseLong(parts[2].trim());
            salaryStructureRepository.findByTenantIdAndEmployeeId(tenantId, employeeId).ifPresent(s -> {
                long updated = (s.getOtherAllowances() == null ? 0L : s.getOtherAllowances()) + bonus;
                s.setOtherAllowances(updated);
                salaryStructureRepository.save(s);
                log.info("Applied bonus event. tenantId={}, employeeId={}, bonus={}", tenantId, employeeId, bonus);
            });
        } catch (Exception ex) {
            log.error("Failed bonus event payload: {}", payload, ex);
        }
    }
}
