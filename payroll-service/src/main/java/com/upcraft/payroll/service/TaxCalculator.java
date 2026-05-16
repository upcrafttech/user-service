package com.upcraft.payroll.service;

import org.springframework.stereotype.Component;

@Component
public class TaxCalculator {

    public long calculateTds(long annualGrossPay) {
        if (annualGrossPay <= 700_000L) {
            return 0L; // rebate under default regime approximation
        }
        long tax;
        if (annualGrossPay <= 300_000L) {
            tax = 0L;
        } else if (annualGrossPay <= 700_000L) {
            tax = Math.round((annualGrossPay - 300_000L) * 0.05);
        } else if (annualGrossPay <= 1_000_000L) {
            tax = Math.round((400_000L * 0.05) + (annualGrossPay - 700_000L) * 0.10);
        } else if (annualGrossPay <= 1_200_000L) {
            tax = Math.round((400_000L * 0.05) + (300_000L * 0.10) + (annualGrossPay - 1_000_000L) * 0.15);
        } else if (annualGrossPay <= 1_500_000L) {
            tax = Math.round((400_000L * 0.05) + (300_000L * 0.10) + (300_000L * 0.15) + (annualGrossPay - 1_200_000L) * 0.20);
        } else {
            tax = Math.round((400_000L * 0.05) + (300_000L * 0.10) + (300_000L * 0.15) + (300_000L * 0.20) + (annualGrossPay - 1_500_000L) * 0.30);
        }
        if (annualGrossPay > 5_000_000L) {
            tax = Math.round(tax * 1.10); // simplified surcharge
        }
        return Math.round(tax * 1.04); // cess
    }

    public long calculatePf(long monthlyBasicPay) {
        long cappedBasic = Math.min(monthlyBasicPay, 15_000L);
        return Math.round(cappedBasic * 0.12);
    }

    public long calculateEsi(long monthlyGrossPay) {
        if (monthlyGrossPay > 21_000L) {
            return 0L;
        }
        return Math.round(monthlyGrossPay * 0.0075);
    }

    public long calculateProfessionalTax(long monthlyGrossPay) {
        if (monthlyGrossPay < 15_000L) {
            return 0L;
        }
        if (monthlyGrossPay < 25_000L) {
            return 150L;
        }
        return 200L;
    }
}
