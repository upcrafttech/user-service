package com.upcraft.payroll.service;

import com.upcraft.payroll.entity.Payslip;
import com.upcraft.payroll.entity.PayrollRun;
import com.upcraft.payroll.entity.SalaryStructure;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class PayrollCalculationService {

    private final TaxCalculator taxCalculator;

    public PayrollCalculationService(TaxCalculator taxCalculator) {
        this.taxCalculator = taxCalculator;
    }

    public Payslip calculatePayslip(PayrollRun payrollRun, SalaryStructure salaryStructure) {
        long grossPay = calculateGrossPay(salaryStructure);
        long pf = taxCalculator.calculatePf(salaryStructure.getBasicPay());
        long esi = taxCalculator.calculateEsi(grossPay);
        long professionalTax = taxCalculator.calculateProfessionalTax(grossPay);
        long tdsMonthly = Math.round(taxCalculator.calculateTds(grossPay * 12) / 12.0);
        long totalDeductions = pf + esi + professionalTax + tdsMonthly;
        long netPay = Math.max(0L, grossPay - totalDeductions);

        Payslip payslip = new Payslip();
        payslip.setPayrollRunId(payrollRun.getId());
        payslip.setTenantId(payrollRun.getTenantId());
        payslip.setEmployeeId(salaryStructure.getEmployeeId());
        payslip.setGrossPay(grossPay);
        payslip.setTds(tdsMonthly);
        payslip.setPf(pf);
        payslip.setEsi(esi);
        payslip.setProfessionalTax(professionalTax);
        payslip.setNetPay(netPay);
        return payslip;
    }

    public long calculateGrossPay(SalaryStructure salaryStructure) {
        return zeroSafe(salaryStructure.getBasicPay())
                + zeroSafe(salaryStructure.getHra())
                + zeroSafe(salaryStructure.getOtherAllowances());
    }

    private long zeroSafe(Long value) {
        return value == null ? 0L : value;
    }
}

