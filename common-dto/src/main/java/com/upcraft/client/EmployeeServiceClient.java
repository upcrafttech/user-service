package com.upcraft.client;

import com.upcraft.dto.EmployeeDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.UUID;

/**
 * Client for Employee Service inter-service communication
 */
@Slf4j
@Component
public class EmployeeServiceClient extends BaseServiceClient {

    @Value("${service.employee.url:http://employee-service:8083}")
    private String employeeServiceUrl;

    public EmployeeServiceClient(RestTemplate restTemplate) {
        super(restTemplate);
    }

    public EmployeeDTO getEmployeeById(UUID employeeId, String authToken) {
        log.info("Fetching employee with ID: {}", employeeId);
        String url = employeeServiceUrl + "/api/employees/" + employeeId;
        return get("employee-service", url, EmployeeDTO.class, authToken);
    }

    public EmployeeDTO getEmployeeByUserId(UUID userId, String authToken) {
        log.info("Fetching employee with user ID: {}", userId);
        String url = employeeServiceUrl + "/api/employees?userId=" + userId;
        return get("employee-service", url, EmployeeDTO.class, authToken);
    }

    public EmployeeDTO createEmployee(EmployeeDTO employeeDTO, String authToken) {
        log.info("Creating new employee: {}", employeeDTO.getName());
        String url = employeeServiceUrl + "/api/employees";
        return post("employee-service", url, employeeDTO, EmployeeDTO.class, authToken);
    }

    public EmployeeDTO updateEmployee(UUID employeeId, EmployeeDTO employeeDTO, String authToken) {
        log.info("Updating employee: {}", employeeId);
        String url = employeeServiceUrl + "/api/employees/" + employeeId;
        return put("employee-service", url, employeeDTO, EmployeeDTO.class, authToken);
    }

    public boolean deleteEmployee(UUID employeeId, String authToken) {
        log.info("Deleting employee: {}", employeeId);
        String url = employeeServiceUrl + "/api/employees/" + employeeId;
        return delete("employee-service", url, authToken);
    }

    public String recordAttendance(UUID employeeId, String action, String authToken) {
        log.info("Recording attendance for employee: {} - action: {}", employeeId, action);
        String url = employeeServiceUrl + "/api/employees/" + employeeId + "/attendance?action=" + action;
        return post("employee-service", url, null, String.class, authToken);
    }
}
