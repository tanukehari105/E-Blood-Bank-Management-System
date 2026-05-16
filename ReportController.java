package com.bloodbank.controller;

import com.bloodbank.service.ReportService;
import com.itextpdf.text.DocumentException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

/**
 * Report controller — PDF and CSV exports for all entity types.
 * Accessible by ADMIN and STAFF.
 */
@RestController
@RequestMapping("/api/reports")
public class ReportController {

    @Autowired
    private ReportService reportService;

    // ── PDF Endpoints ──────────────────────────────────────────────────────────

    @GetMapping("/donors/pdf")
    public ResponseEntity<byte[]> donorsPdf() throws DocumentException {
        return pdfResponse(reportService.generateDonorsPdf(), "donors_" + LocalDate.now() + ".pdf");
    }

    @GetMapping("/inventory/pdf")
    public ResponseEntity<byte[]> inventoryPdf() throws DocumentException {
        return pdfResponse(reportService.generateInventoryPdf(), "inventory_" + LocalDate.now() + ".pdf");
    }

    @GetMapping("/requests/pdf")
    public ResponseEntity<byte[]> requestsPdf() throws DocumentException {
        return pdfResponse(reportService.generateRequestsPdf(), "requests_" + LocalDate.now() + ".pdf");
    }

    @GetMapping("/camps/pdf")
    public ResponseEntity<byte[]> campsPdf() throws DocumentException {
        return pdfResponse(reportService.generateCampsPdf(), "camps_" + LocalDate.now() + ".pdf");
    }

    @GetMapping("/hospitals/pdf")
    public ResponseEntity<byte[]> hospitalsPdf() throws DocumentException {
        return pdfResponse(reportService.generateHospitalsPdf(), "hospitals_" + LocalDate.now() + ".pdf");
    }

    @GetMapping("/allocations/pdf")
    public ResponseEntity<byte[]> allocationsPdf() throws DocumentException {
        return pdfResponse(reportService.generateAllocationsPdf(), "allocations_" + LocalDate.now() + ".pdf");
    }

    // ── CSV Endpoints ──────────────────────────────────────────────────────────

    @GetMapping("/donors/csv")
    public ResponseEntity<byte[]> donorsCsv() {
        return csvResponse(reportService.generateDonorsCsv(), "donors_" + LocalDate.now() + ".csv");
    }

    @GetMapping("/inventory/csv")
    public ResponseEntity<byte[]> inventoryCsv() {
        return csvResponse(reportService.generateInventoryCsv(), "inventory_" + LocalDate.now() + ".csv");
    }

    @GetMapping("/requests/csv")
    public ResponseEntity<byte[]> requestsCsv() {
        return csvResponse(reportService.generateRequestsCsv(), "requests_" + LocalDate.now() + ".csv");
    }

    @GetMapping("/camps/csv")
    public ResponseEntity<byte[]> campsCsv() {
        return csvResponse(reportService.generateCampsCsv(), "camps_" + LocalDate.now() + ".csv");
    }

    @GetMapping("/hospitals/csv")
    public ResponseEntity<byte[]> hospitalsCsv() {
        return csvResponse(reportService.generateHospitalsCsv(), "hospitals_" + LocalDate.now() + ".csv");
    }

    // ── Helpers ────────────────────────────────────────────────────────────────

    private ResponseEntity<byte[]> pdfResponse(byte[] data, String filename) {
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(data);
    }

    private ResponseEntity<byte[]> csvResponse(String data, String filename) {
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(data.getBytes());
    }
}
