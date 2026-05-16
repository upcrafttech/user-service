package com.upcraft.client;

import com.upcraft.dto.PayslipDTO;
import com.upcraft.dto.SalaryStructureDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.UUID;

/**
 * Client for Payroll Service inter-service communication
 */
@Slf4j
@Component
public class PayrollServiceClient extends BaseServiceClient {

    @Value("${service.payroll.url:http://payroll-service:8085}")
    private String payrollServiceUrl;

    public PayrollServiceClient(RestTemplate restTemplate) {
        super(restTemplate);
    }

    public SalaryStructureDTO getSalaryStructure(UUID employeeId, String authToken) {
        log.info("Fetching salary structure for employee: {}", employeeId);
        String url = payrollServiceUrl + "/api/salary-structures?employeeId=" + employeeId;
        return get("payroll-service", url, SalaryStructureDTO.class, authToken);
    }

    public SalaryStructureDTO createSalaryStructure(SalaryStructureDTO salaryStructureDTO, String authToken) {
        log.info("Creating salary structure for employee: {}", salaryStructureDTO.getEmployeeId());
        String url = payrollServiceUrl + "/api/salary-structures";
        return post("payroll-service", url, salaryStructureDTO, SalaryStructureDTO.class, authToken);
    }

    public SalaryStructureDTO updateSalaryStructure(UUID employeeId, SalaryStructureDTO salaryStructureDTO, String authToken) {
        log.info("Updating salary structure for employee: {}", employeeId);
        String url = payrollServiceUrl + "/api/salary-structures/" + employeeId;
        return put("payroll-service", url, salaryStructureDTO, SalaryStructureDTO.class, authToken);
    }

    public String runPayroll(UUID tenantId, String periodStart, String periodEnd, String authToken) {
        log.info("Running payroll for tenant: {} from {} to {}", tenantId, periodStart, periodEnd);
        String url = payrollServiceUrl + "/api/payroll/runs?tenantId=" + tenantId +
                     "&periodStart=" + periodStart + "&periodEnd=" + periodEnd;
        return post("payroll-service", url, null, String.class, authToken);
    }

    public PayslipDTO[] getPayslips(UUID payrollRunId, String authToken) {
        log.info("Fetching payslips for payroll run: {}", payrollRunId);
        String url = payrollServiceUrl + "/api/payroll/runs/" + payrollRunId;
        return get("payroll-service", url, PayslipDTO[].class, authToken);
    }
}
