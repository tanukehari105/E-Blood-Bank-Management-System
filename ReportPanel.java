package com.bloodbank.ui.screens;

import com.bloodbank.ui.api.ApiClient;
import com.bloodbank.ui.model.BloodAllocation;
import com.bloodbank.ui.model.BloodInventory;
import com.bloodbank.ui.model.BloodRequest;
import com.bloodbank.ui.model.Donation;
import com.bloodbank.ui.model.DonationCamp;
import com.bloodbank.ui.model.Donor;
import com.bloodbank.ui.model.Hospital;
import com.bloodbank.ui.util.UIConstants;
import com.bloodbank.ui.util.UIHelper;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.*;
import java.util.List;

/**
 * Reports panel — generate and export data as CSV or PDF.
 * Supports: Donors, Inventory, Donations, Requests, Camps, Hospitals, Allocations.
 */
public class ReportPanel extends JPanel {

    private JTable table;
    private DefaultTableModel tableModel;
    private JComboBox<String> reportTypeBox;
    private String currentReportType;

    private static final String[] REPORT_TYPES = {
        "Donor Report", "Inventory Report", "Donation Report",
        "Request History", "Camps Report", "Hospitals Report", "Allocations Report"
    };

    public ReportPanel() {
        setLayout(new BorderLayout());
        setBackground(UIConstants.BG_LIGHT);
        setBorder(new EmptyBorder(25, 25, 25, 25));
        initUI();
    }

    private void initUI() {
        // Header
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);
        topPanel.setBorder(new EmptyBorder(0, 0, 15, 0));
        topPanel.add(UIHelper.titleLabel("📄 Reports"), BorderLayout.WEST);

        // Toolbar
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        toolbar.setOpaque(false);

        reportTypeBox = new JComboBox<>(REPORT_TYPES);
        reportTypeBox.setFont(UIConstants.FONT_BODY);
        reportTypeBox.setPreferredSize(new Dimension(220, 36));

        JButton generateBtn = UIHelper.primaryButton("▶ Generate");
        generateBtn.addActionListener(e -> generateReport());

        JButton exportCsvBtn = UIHelper.successButton("⬇ Export CSV");
        exportCsvBtn.addActionListener(e -> exportCSV());

        JButton exportPdfBtn = UIHelper.infoButton("📄 Export PDF");
        exportPdfBtn.addActionListener(e -> exportPDF());

        toolbar.add(new JLabel("Report Type:"));
        toolbar.add(reportTypeBox);
        toolbar.add(generateBtn);
        toolbar.add(exportCsvBtn);
        toolbar.add(exportPdfBtn);

        topPanel.add(toolbar, BorderLayout.SOUTH);
        add(topPanel, BorderLayout.NORTH);

        // Table
        tableModel = new DefaultTableModel() {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(tableModel);
        UIHelper.styleTable(table);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(0xE0E0E0)));
        add(scrollPane, BorderLayout.CENTER);
    }

    private void generateReport() {
        currentReportType = (String) reportTypeBox.getSelectedItem();
        switch (currentReportType) {
            case "Donor Report"       -> loadDonorReport();
            case "Inventory Report"   -> loadInventoryReport();
            case "Donation Report"    -> loadDonationReport();
            case "Request History"    -> loadRequestReport();
            case "Camps Report"       -> loadCampsReport();
            case "Hospitals Report"   -> loadHospitalsReport();
            case "Allocations Report" -> loadAllocationsReport();
        }
    }

    private void loadDonorReport() {
        new SwingWorker<List<Donor>, Void>() {
            @Override protected List<Donor> doInBackground() throws Exception {
                return ApiClient.listFromJson(ApiClient.get("/donors"), Donor.class);
            }
            @Override protected void done() {
                try {
                    List<Donor> list = get();
                    tableModel.setColumnIdentifiers(new String[]{
                        "ID", "Name", "Age", "Gender", "Blood Group", "Contact", "Email", "Last Donation", "Eligible"});
                    tableModel.setRowCount(0);
                    for (Donor d : list) {
                        tableModel.addRow(new Object[]{
                            d.id, d.name, d.age, d.gender, d.bloodGroup, d.contact,
                            d.email != null ? d.email : "",
                            d.lastDonationDate != null ? d.lastDonationDate : "Never",
                            d.isEligible() ? "Yes" : "No"});
                    }
                } catch (Exception e) { UIHelper.showError(ReportPanel.this, e.getMessage()); }
            }
        }.execute();
    }

    private void loadInventoryReport() {
        new SwingWorker<List<BloodInventory>, Void>() {
            @Override protected List<BloodInventory> doInBackground() throws Exception {
                return ApiClient.listFromJson(ApiClient.get("/inventory"), BloodInventory.class);
            }
            @Override protected void done() {
                try {
                    List<BloodInventory> list = get();
                    tableModel.setColumnIdentifiers(new String[]{
                        "ID", "Blood Group", "Quantity", "Expiry Date", "Added Date", "Source", "Status"});
                    tableModel.setRowCount(0);
                    for (BloodInventory inv : list) {
                        String status = inv.isExpired() ? "Expired"
                                : inv.isExpiringSoon() ? "Expiring Soon" : "OK";
                        tableModel.addRow(new Object[]{
                            inv.id, inv.bloodGroup, inv.quantity,
                            inv.expiryDate, inv.addedDate, inv.source, status});
                    }
                } catch (Exception e) { UIHelper.showError(ReportPanel.this, e.getMessage()); }
            }
        }.execute();
    }

    private void loadDonationReport() {
        new SwingWorker<List<Donation>, Void>() {
            @Override protected List<Donation> doInBackground() throws Exception {
                return ApiClient.listFromJson(ApiClient.get("/donations"), Donation.class);
            }
            @Override protected void done() {
                try {
                    List<Donation> list = get();
                    tableModel.setColumnIdentifiers(new String[]{
                        "ID", "Donor", "Blood Group", "Quantity", "Date", "Camp ID", "Notes"});
                    tableModel.setRowCount(0);
                    for (Donation d : list) {
                        tableModel.addRow(new Object[]{
                            d.id, d.donor != null ? d.donor.name : "",
                            d.bloodGroup, d.quantity, d.donationDate,
                            d.campId != null ? d.campId : "",
                            d.notes != null ? d.notes : ""});
                    }
                } catch (Exception e) { UIHelper.showError(ReportPanel.this, e.getMessage()); }
            }
        }.execute();
    }

    private void loadRequestReport() {
        new SwingWorker<List<BloodRequest>, Void>() {
            @Override protected List<BloodRequest> doInBackground() throws Exception {
                return ApiClient.listFromJson(ApiClient.get("/requests"), BloodRequest.class);
            }
            @Override protected void done() {
                try {
                    List<BloodRequest> list = get();
                    tableModel.setColumnIdentifiers(new String[]{
                        "ID", "Patient", "Hospital", "Blood Group", "Units", "Urgency", "Status",
                        "Request Date", "Processed Date"});
                    tableModel.setRowCount(0);
                    for (BloodRequest r : list) {
                        tableModel.addRow(new Object[]{
                            r.id, r.patientName, r.hospital, r.bloodGroup, r.units,
                            r.urgencyLevel, r.status, r.requestDate, r.processedDate});
                    }
                } catch (Exception e) { UIHelper.showError(ReportPanel.this, e.getMessage()); }
            }
        }.execute();
    }

    private void loadCampsReport() {
        new SwingWorker<List<DonationCamp>, Void>() {
            @Override protected List<DonationCamp> doInBackground() throws Exception {
                return ApiClient.listFromJson(ApiClient.get("/camps"), DonationCamp.class);
            }
            @Override protected void done() {
                try {
                    List<DonationCamp> list = get();
                    tableModel.setColumnIdentifiers(new String[]{
                        "ID", "Camp Name", "Location", "Date", "Expected", "Actual Donors",
                        "Units Collected", "Status"});
                    tableModel.setRowCount(0);
                    for (DonationCamp c : list) {
                        tableModel.addRow(new Object[]{
                            c.id, c.campName, c.location, c.campDate,
                            c.expectedDonors, c.actualDonors, c.totalUnitsCollected,
                            c.active ? "Active" : "Cancelled"});
                    }
                } catch (Exception e) { UIHelper.showError(ReportPanel.this, e.getMessage()); }
            }
        }.execute();
    }

    private void loadHospitalsReport() {
        new SwingWorker<List<Hospital>, Void>() {
            @Override protected List<Hospital> doInBackground() throws Exception {
                return ApiClient.listFromJson(ApiClient.get("/hospitals"), Hospital.class);
            }
            @Override protected void done() {
                try {
                    List<Hospital> list = get();
                    tableModel.setColumnIdentifiers(new String[]{
                        "ID", "Hospital Name", "Email", "Phone", "Address", "Status"});
                    tableModel.setRowCount(0);
                    for (Hospital h : list) {
                        tableModel.addRow(new Object[]{
                            h.id, h.hospitalName, h.email,
                            h.phone != null ? h.phone : "",
                            h.address != null ? h.address : "",
                            h.active ? "Active" : "Inactive"});
                    }
                } catch (Exception e) { UIHelper.showError(ReportPanel.this, e.getMessage()); }
            }
        }.execute();
    }

    private void loadAllocationsReport() {
        new SwingWorker<List<BloodAllocation>, Void>() {
            @Override protected List<BloodAllocation> doInBackground() throws Exception {
                return ApiClient.listFromJson(ApiClient.get("/inventory/allocations"), BloodAllocation.class);
            }
            @Override protected void done() {
                try {
                    List<BloodAllocation> list = get();
                    tableModel.setColumnIdentifiers(new String[]{
                        "ID", "Request ID", "Blood Group", "Units Allocated",
                        "Allocation Date", "Batch Expiry", "Donor"});
                    tableModel.setRowCount(0);
                    for (BloodAllocation a : list) {
                        tableModel.addRow(new Object[]{
                            a.id, a.requestId, a.bloodGroup, a.unitsAllocated,
                            a.allocationDate, a.batchExpiryDate,
                            a.donorName != null ? a.donorName : "N/A"});
                    }
                } catch (Exception e) { UIHelper.showError(ReportPanel.this, e.getMessage()); }
            }
        }.execute();
    }

    // ── CSV Export ────────────────────────────────────────────────────────────

    private void exportCSV() {
        if (tableModel.getRowCount() == 0) {
            UIHelper.showError(this, "Generate a report first.");
            return;
        }
        JFileChooser fc = new JFileChooser();
        String defaultName = (currentReportType != null
                ? currentReportType.replace(" ", "_") : "report") + ".csv";
        fc.setSelectedFile(new File(defaultName));
        if (fc.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;

        File file = fc.getSelectedFile();
        try (PrintWriter pw = new PrintWriter(new FileWriter(file))) {
            // Headers
            StringBuilder header = new StringBuilder();
            for (int i = 0; i < tableModel.getColumnCount(); i++) {
                if (i > 0) header.append(",");
                header.append(escapeCsv(tableModel.getColumnName(i)));
            }
            pw.println(header);
            // Rows
            for (int r = 0; r < tableModel.getRowCount(); r++) {
                StringBuilder row = new StringBuilder();
                for (int c = 0; c < tableModel.getColumnCount(); c++) {
                    if (c > 0) row.append(",");
                    Object val = tableModel.getValueAt(r, c);
                    row.append(escapeCsv(val != null ? val.toString() : ""));
                }
                pw.println(row);
            }
            UIHelper.showSuccess(this, "CSV exported to: " + file.getAbsolutePath());
        } catch (Exception e) {
            UIHelper.showError(this, "Failed to export CSV: " + e.getMessage());
        }
    }

    // ── PDF Export (via backend API) ──────────────────────────────────────────

    private void exportPDF() {
        if (currentReportType == null) {
            UIHelper.showError(this, "Generate a report first.");
            return;
        }

        String endpoint = switch (currentReportType) {
            case "Donor Report"       -> "/reports/donors/pdf";
            case "Inventory Report"   -> "/reports/inventory/pdf";
            case "Request History"    -> "/reports/requests/pdf";
            case "Camps Report"       -> "/reports/camps/pdf";
            case "Hospitals Report"   -> "/reports/hospitals/pdf";
            case "Allocations Report" -> "/reports/allocations/pdf";
            default -> null;
        };

        if (endpoint == null) {
            UIHelper.showError(this, "PDF export not available for: " + currentReportType);
            return;
        }

        JFileChooser fc = new JFileChooser();
        String defaultName = currentReportType.replace(" ", "_") + ".pdf";
        fc.setSelectedFile(new File(defaultName));
        if (fc.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;

        File file = fc.getSelectedFile();
        String ep = endpoint;

        new SwingWorker<byte[], Void>() {
            @Override protected byte[] doInBackground() throws Exception {
                // Fetch raw bytes from backend PDF endpoint
                okhttp3.OkHttpClient client = new okhttp3.OkHttpClient();
                okhttp3.Request req = new okhttp3.Request.Builder()
                        .url("http://localhost:8080/api" + ep)
                        .header("Authorization", "Bearer " + com.bloodbank.ui.util.SessionManager.getToken())
                        .get().build();
                try (okhttp3.Response resp = client.newCall(req).execute()) {
                    if (!resp.isSuccessful()) throw new IOException("HTTP " + resp.code());
                    return resp.body() != null ? resp.body().bytes() : new byte[0];
                }
            }
            @Override protected void done() {
                try {
                    byte[] data = get();
                    try (FileOutputStream fos = new FileOutputStream(file)) {
                        fos.write(data);
                    }
                    UIHelper.showSuccess(ReportPanel.this, "PDF exported to: " + file.getAbsolutePath());
                } catch (Exception e) {
                    UIHelper.showError(ReportPanel.this, "Failed to export PDF: " + e.getMessage());
                }
            }
        }.execute();
    }

    private String escapeCsv(String value) {
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}
