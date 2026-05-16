package com.upcraft.payroll.service;

import com.upcraft.exception.ResourceNotFoundException;
import com.upcraft.payroll.entity.Payslip;
import com.upcraft.payroll.repository.PayslipRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PayslipDocumentService {

    private final PayslipRepository payslipRepository;

    public byte[] generatePayslipPdf(UUID payslipId) {
        Payslip payslip = payslipRepository.findById(payslipId)
                .orElseThrow(() -> new ResourceNotFoundException("Payslip", "id", payslipId));

        String line1 = "Payslip " + payslip.getId();
        String line2 = "Employee " + payslip.getEmployeeId();
        String line3 = "Gross " + payslip.getGrossPay() + " Net " + payslip.getNetPay();
        String content = "BT /F1 12 Tf 50 750 Td (" + escape(line1) + ") Tj T* (" + escape(line2) + ") Tj T* (" + escape(line3) + ") Tj ET";
        return buildMinimalPdf(content);
    }

    private byte[] buildMinimalPdf(String contentStream) {
        String obj1 = "1 0 obj<< /Type /Catalog /Pages 2 0 R >>endobj\n";
        String obj2 = "2 0 obj<< /Type /Pages /Kids [3 0 R] /Count 1 >>endobj\n";
        String obj3 = "3 0 obj<< /Type /Page /Parent 2 0 R /MediaBox [0 0 595 842] /Resources << /Font << /F1 4 0 R >> >> /Contents 5 0 R >>endobj\n";
        String obj4 = "4 0 obj<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica >>endobj\n";
        String obj5 = "5 0 obj<< /Length " + contentStream.length() + " >>stream\n" + contentStream + "\nendstream endobj\n";

        StringBuilder pdf = new StringBuilder();
        pdf.append("%PDF-1.4\n");
        int x1 = pdf.length(); pdf.append(obj1);
        int x2 = pdf.length(); pdf.append(obj2);
        int x3 = pdf.length(); pdf.append(obj3);
        int x4 = pdf.length(); pdf.append(obj4);
        int x5 = pdf.length(); pdf.append(obj5);
        int xref = pdf.length();
        pdf.append("xref\n0 6\n");
        pdf.append("0000000000 65535 f \n");
        pdf.append(String.format("%010d 00000 n \n", x1));
        pdf.append(String.format("%010d 00000 n \n", x2));
        pdf.append(String.format("%010d 00000 n \n", x3));
        pdf.append(String.format("%010d 00000 n \n", x4));
        pdf.append(String.format("%010d 00000 n \n", x5));
        pdf.append("trailer<< /Root 1 0 R /Size 6 >>\nstartxref\n").append(xref).append("\n%%EOF");
        return pdf.toString().getBytes(StandardCharsets.US_ASCII);
    }

    private String escape(String text) {
        return text.replace("\\", "\\\\").replace("(", "\\(").replace(")", "\\)");
    }
}
