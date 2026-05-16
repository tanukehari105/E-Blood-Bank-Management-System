package com.bloodbank.service;

import com.bloodbank.entity.*;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.util.List;

/**
 * Report service — generates PDF and CSV exports for all entity types.
 * Uses iText 5 for PDF generation.
 */
@Service
public class ReportService {

    @Autowired private DonorService donorService;
    @Autowired private InventoryService inventoryService;
    @Autowired private BloodRequestService requestService;
    @Autowired private DonationCampService campService;
    @Autowired private HospitalService hospitalService;
    @Autowired private AuditLogService auditLogService;

    private static final Font TITLE_FONT = new Font(Font.FontFamily.HELVETICA, 18, Font.BOLD, new BaseColor(192, 57, 43));
    private static final Font HEADER_FONT = new Font(Font.FontFamily.HELVETICA, 10, Font.BOLD, BaseColor.WHITE);
    private static final Font CELL_FONT = new Font(Font.FontFamily.HELVETICA, 9, Font.NORMAL, BaseColor.DARK_GRAY);
    private static final BaseColor HEADER_BG = new BaseColor(192, 57, 43);
    private static final BaseColor ROW_ALT = new BaseColor(253, 245, 245);

    // ── PDF Generators ─────────────────────────────────────────────────────────

    public byte[] generateDonorsPdf() throws DocumentException {
        List<Donor> donors = donorService.getAllDonors();
        Document doc = new Document(PageSize.A4.rotate());
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PdfWriter.getInstance(doc, out);
        doc.open();

        addTitle(doc, "Donors Report", "Generated: " + LocalDate.now());

        PdfPTable table = createTable(7);
        addHeaders(table, "ID", "Name", "Age", "Gender", "Blood Group", "Contact", "Last Donation");
        boolean alt = false;
        for (Donor d : donors) {
            BaseColor bg = alt ? ROW_ALT : BaseColor.WHITE;
            addRow(table, bg,
                    str(d.getId()), d.getName(), str(d.getAge()), d.getGender(),
                    d.getBloodGroup(), d.getContact(),
                    d.getLastDonationDate() != null ? d.getLastDonationDate().toString() : "Never");
            alt = !alt;
        }
        doc.add(table);
        addFooter(doc, donors.size() + " donors");
        doc.close();
        return out.toByteArray();
    }

    public byte[] generateInventoryPdf() throws DocumentException {
        List<BloodInventory> items = inventoryService.getAllInventory();
        Document doc = new Document(PageSize.A4.rotate());
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PdfWriter.getInstance(doc, out);
        doc.open();

        addTitle(doc, "Blood Inventory Report", "Generated: " + LocalDate.now());

        PdfPTable table = createTable(5);
        addHeaders(table, "ID", "Blood Group", "Quantity (Units)", "Added Date", "Expiry Date");
        boolean alt = false;
        for (BloodInventory i : items) {
            boolean expired = i.getExpiryDate() != null && i.getExpiryDate().isBefore(LocalDate.now());
            BaseColor bg = expired ? new BaseColor(255, 235, 235) : (alt ? ROW_ALT : BaseColor.WHITE);
            addRow(table, bg,
                    str(i.getId()), i.getBloodGroup(), str(i.getQuantity()),
                    i.getAddedDate() != null ? i.getAddedDate().toString() : "",
                    i.getExpiryDate() != null ? i.getExpiryDate().toString() : "");
            alt = !alt;
        }
        doc.add(table);
        addFooter(doc, items.size() + " inventory records");
        doc.close();
        return out.toByteArray();
    }

    public byte[] generateRequestsPdf() throws DocumentException {
        List<BloodRequest> requests = requestService.getAllRequests();
        Document doc = new Document(PageSize.A4.rotate());
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PdfWriter.getInstance(doc, out);
        doc.open();

        addTitle(doc, "Blood Requests Report", "Generated: " + LocalDate.now());

        PdfPTable table = createTable(7);
        addHeaders(table, "ID", "Patient", "Hospital", "Blood Group", "Units", "Urgency", "Status");
        boolean alt = false;
        for (BloodRequest r : requests) {
            BaseColor bg = "CRITICAL".equals(r.getUrgencyLevel()) ? new BaseColor(255, 235, 235) :
                           (alt ? ROW_ALT : BaseColor.WHITE);
            addRow(table, bg,
                    str(r.getId()), r.getPatientName(),
                    r.getHospital() != null ? r.getHospital() : "",
                    r.getBloodGroup(), str(r.getUnits()),
                    r.getUrgencyLevel(), r.getStatus());
            alt = !alt;
        }
        doc.add(table);
        addFooter(doc, requests.size() + " requests");
        doc.close();
        return out.toByteArray();
    }

    public byte[] generateCampsPdf() throws DocumentException {
        List<DonationCamp> camps = campService.getAllCamps();
        Document doc = new Document(PageSize.A4.rotate());
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PdfWriter.getInstance(doc, out);
        doc.open();

        addTitle(doc, "Donation Camps Report", "Generated: " + LocalDate.now());

        PdfPTable table = createTable(7);
        addHeaders(table, "ID", "Camp Name", "Location", "Date", "Expected", "Actual Donors", "Units Collected");
        boolean alt = false;
        for (DonationCamp c : camps) {
            BaseColor bg = alt ? ROW_ALT : BaseColor.WHITE;
            addRow(table, bg,
                    str(c.getId()), c.getCampName(), c.getLocation(),
                    c.getCampDate() != null ? c.getCampDate().toString() : "",
                    str(c.getExpectedDonors()), str(c.getActualDonors()),
                    str(c.getTotalUnitsCollected()));
            alt = !alt;
        }
        doc.add(table);
        addFooter(doc, camps.size() + " camps");
        doc.close();
        return out.toByteArray();
    }

    public byte[] generateHospitalsPdf() throws DocumentException {
        List<Hospital> hospitals = hospitalService.getAllHospitals();
        Document doc = new Document(PageSize.A4.rotate());
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PdfWriter.getInstance(doc, out);
        doc.open();

        addTitle(doc, "Hospitals Report", "Generated: " + LocalDate.now());

        PdfPTable table = createTable(5);
        addHeaders(table, "ID", "Hospital Name", "Email", "Phone", "Status");
        boolean alt = false;
        for (Hospital h : hospitals) {
            BaseColor bg = alt ? ROW_ALT : BaseColor.WHITE;
            addRow(table, bg,
                    str(h.getId()), h.getHospitalName(), h.getEmail(),
                    h.getPhone() != null ? h.getPhone() : "",
                    h.isActive() ? "Active" : "Inactive");
            alt = !alt;
        }
        doc.add(table);
        addFooter(doc, hospitals.size() + " hospitals");
        doc.close();
        return out.toByteArray();
    }

    public byte[] generateAllocationsPdf() throws DocumentException {
        List<BloodAllocation> allocations = inventoryService.getAllAllocations();
        Document doc = new Document(PageSize.A4.rotate());
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PdfWriter.getInstance(doc, out);
        doc.open();

        addTitle(doc, "Blood Allocations Report", "Generated: " + LocalDate.now());

        PdfPTable table = createTable(6);
        addHeaders(table, "ID", "Request ID", "Blood Group", "Units Allocated", "Allocation Date", "Batch Expiry");
        boolean alt = false;
        for (BloodAllocation a : allocations) {
            BaseColor bg = alt ? ROW_ALT : BaseColor.WHITE;
            addRow(table, bg,
                    str(a.getId()), str(a.getRequestId()), a.getBloodGroup(),
                    str(a.getUnitsAllocated()),
                    a.getAllocationDate() != null ? a.getAllocationDate().toString() : "",
                    a.getBatchExpiryDate() != null ? a.getBatchExpiryDate().toString() : "");
            alt = !alt;
        }
        doc.add(table);
        addFooter(doc, allocations.size() + " allocation records");
        doc.close();
        return out.toByteArray();
    }

    // ── CSV Generators ─────────────────────────────────────────────────────────

    public String generateDonorsCsv() {
        StringBuilder sb = new StringBuilder("ID,Name,Age,Gender,Blood Group,Contact,Email,Last Donation\n");
        for (Donor d : donorService.getAllDonors()) {
            sb.append(csv(d.getId(), d.getName(), d.getAge(), d.getGender(), d.getBloodGroup(),
                    d.getContact(), d.getEmail(),
                    d.getLastDonationDate() != null ? d.getLastDonationDate() : "Never")).append("\n");
        }
        return sb.toString();
    }

    public String generateInventoryCsv() {
        StringBuilder sb = new StringBuilder("ID,Blood Group,Quantity,Added Date,Expiry Date,Source\n");
        for (BloodInventory i : inventoryService.getAllInventory()) {
            sb.append(csv(i.getId(), i.getBloodGroup(), i.getQuantity(),
                    i.getAddedDate(), i.getExpiryDate(), i.getSource())).append("\n");
        }
        return sb.toString();
    }

    public String generateRequestsCsv() {
        StringBuilder sb = new StringBuilder("ID,Patient,Hospital,Blood Group,Units,Urgency,Status,Request Date\n");
        for (BloodRequest r : requestService.getAllRequests()) {
            sb.append(csv(r.getId(), r.getPatientName(), r.getHospital(), r.getBloodGroup(),
                    r.getUnits(), r.getUrgencyLevel(), r.getStatus(), r.getRequestDate())).append("\n");
        }
        return sb.toString();
    }

    public String generateCampsCsv() {
        StringBuilder sb = new StringBuilder("ID,Camp Name,Location,Date,Expected,Actual,Units Collected\n");
        for (DonationCamp c : campService.getAllCamps()) {
            sb.append(csv(c.getId(), c.getCampName(), c.getLocation(), c.getCampDate(),
                    c.getExpectedDonors(), c.getActualDonors(), c.getTotalUnitsCollected())).append("\n");
        }
        return sb.toString();
    }

    public String generateHospitalsCsv() {
        StringBuilder sb = new StringBuilder("ID,Hospital Name,Email,Phone,Address,Status\n");
        for (Hospital h : hospitalService.getAllHospitals()) {
            sb.append(csv(h.getId(), h.getHospitalName(), h.getEmail(), h.getPhone(),
                    h.getAddress(), h.isActive() ? "Active" : "Inactive")).append("\n");
        }
        return sb.toString();
    }

    // ── Private Helpers ────────────────────────────────────────────────────────

    private PdfPTable createTable(int cols) throws DocumentException {
        PdfPTable table = new PdfPTable(cols);
        table.setWidthPercentage(100);
        table.setSpacingBefore(10f);
        return table;
    }

    private void addHeaders(PdfPTable table, String... headers) {
        for (String h : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(h, HEADER_FONT));
            cell.setBackgroundColor(HEADER_BG);
            cell.setPadding(6);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(cell);
        }
    }

    private void addRow(PdfPTable table, BaseColor bg, String... values) {
        for (String v : values) {
            PdfPCell cell = new PdfPCell(new Phrase(v != null ? v : "", CELL_FONT));
            cell.setBackgroundColor(bg);
            cell.setPadding(5);
            table.addCell(cell);
        }
    }

    private void addTitle(Document doc, String title, String subtitle) throws DocumentException {
        Paragraph t = new Paragraph(title, TITLE_FONT);
        t.setAlignment(Element.ALIGN_CENTER);
        doc.add(t);
        Font subFont = new Font(Font.FontFamily.HELVETICA, 10, Font.ITALIC, BaseColor.GRAY);
        Paragraph s = new Paragraph(subtitle, subFont);
        s.setAlignment(Element.ALIGN_CENTER);
        s.setSpacingAfter(15f);
        doc.add(s);
    }

    private void addFooter(Document doc, String summary) throws DocumentException {
        Font f = new Font(Font.FontFamily.HELVETICA, 9, Font.ITALIC, BaseColor.GRAY);
        Paragraph p = new Paragraph("Total: " + summary + " | Blood Bank Management System", f);
        p.setAlignment(Element.ALIGN_RIGHT);
        p.setSpacingBefore(10f);
        doc.add(p);
    }

    private String str(Object o) {
        return o != null ? o.toString() : "";
    }

    private String csv(Object... values) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < values.length; i++) {
            if (i > 0) sb.append(",");
            String v = values[i] != null ? values[i].toString() : "";
            if (v.contains(",") || v.contains("\"")) v = "\"" + v.replace("\"", "\"\"") + "\"";
            sb.append(v);
        }
        return sb.toString();
    }
}
