package com.upcraft.payroll.service;

import com.upcraft.payroll.dto.YearEndPayrollReportDTO;
import com.upcraft.payroll.entity.Payslip;
import com.upcraft.payroll.repository.PayslipRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class YearEndReportService {

    private final PayslipRepository payslipRepository;

    public List<YearEndPayrollReportDTO> yearEndReport(UUID tenantId, Integer year) {
        int y = year == null ? LocalDateTime.now().getYear() : year;
        LocalDateTime from = LocalDateTime.of(y, 1, 1, 0, 0);
        LocalDateTime to = from.plusYears(1);
        List<Payslip> payslips = payslipRepository.findForYear(tenantId, from, to);

        Map<UUID, List<Payslip>> byEmployee = payslips.stream().collect(Collectors.groupingBy(Payslip::getEmployeeId));
        return byEmployee.entrySet().stream().map(e -> {
            long gross = e.getValue().stream().mapToLong(p -> zero(p.getGrossPay())).sum();
            long net = e.getValue().stream().mapToLong(p -> zero(p.getNetPay())).sum();
            long tds = e.getValue().stream().mapToLong(p -> zero(p.getTds())).sum();
            long pf = e.getValue().stream().mapToLong(p -> zero(p.getPf())).sum();
            long esi = e.getValue().stream().mapToLong(p -> zero(p.getEsi())).sum();
            long pt = e.getValue().stream().mapToLong(p -> zero(p.getProfessionalTax())).sum();
            return YearEndPayrollReportDTO.builder()
                    .tenantId(tenantId)
                    .employeeId(e.getKey())
                    .year(y)
                    .totalGrossPay(gross)
                    .totalNetPay(net)
                    .totalTds(tds)
                    .totalPf(pf)
                    .totalEsi(esi)
                    .totalProfessionalTax(pt)
                    .build();
        }).collect(Collectors.toList());
    }

    private long zero(Long value) { return value == null ? 0L : value; }
}
