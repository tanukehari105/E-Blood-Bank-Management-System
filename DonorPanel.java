package com.bloodbank.ui.screens;

import com.bloodbank.ui.api.ApiClient;
import com.bloodbank.ui.model.Donor;
import com.bloodbank.ui.util.SessionManager;
import com.bloodbank.ui.util.UIConstants;
import com.bloodbank.ui.util.UIHelper;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Donor management panel.
 *
 * Gmail is REQUIRED when adding a donor — it is used for:
 *   • Camp invitation emails  (when admin creates a camp)
 *   • "Your blood was used"   (when a blood request is approved)
 *   • Emergency donor alerts  (when stock is insufficient)
 */
public class DonorPanel extends JPanel {

    private JTable table;
    private DefaultTableModel tableModel;
    private List<Donor> donors = new ArrayList<>();
    private JTextField searchField;
    private JComboBox<String> bloodGroupFilter;

    public DonorPanel() {
        setLayout(new BorderLayout());
        setBackground(UIConstants.BG_LIGHT);
        setBorder(new EmptyBorder(25, 25, 25, 25));
        initUI();
    }

    // ── UI Setup ──────────────────────────────────────────────────────────────

    private void initUI() {
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);

        JPanel headerRow = new JPanel(new BorderLayout());
        headerRow.setOpaque(false);
        headerRow.setBorder(new EmptyBorder(0, 0, 10, 0));
        headerRow.add(UIHelper.titleLabel("👥 Donor Management"), BorderLayout.WEST);

        JButton addBtn = UIHelper.successButton("+ Add Donor");
        addBtn.addActionListener(e -> showDonorDialog(null));
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        btnPanel.setOpaque(false);
        btnPanel.add(addBtn);
        headerRow.add(btnPanel, BorderLayout.EAST);

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        toolbar.setOpaque(false);

        searchField = UIHelper.searchField("Search by name, contact or email...");
        searchField.setPreferredSize(new Dimension(260, 34));
        searchField.addActionListener(e -> performSearch());

        JButton searchBtn = UIHelper.infoButton("Search");
        searchBtn.addActionListener(e -> performSearch());

        String[] bgOptions = {"All Blood Groups","A+","A-","B+","B-","AB+","AB-","O+","O-"};
        bloodGroupFilter = new JComboBox<>(bgOptions);
        bloodGroupFilter.setFont(UIConstants.FONT_BODY);
        bloodGroupFilter.addActionListener(e -> filterByBloodGroup());

        JButton eligibleBtn = UIHelper.warningButton("Eligible Only");
        eligibleBtn.addActionListener(e -> showEligibleOnly());

        JButton refreshBtn = UIHelper.outlineButton("↻ Refresh");
        refreshBtn.addActionListener(e -> refresh());

        toolbar.add(searchField);
        toolbar.add(searchBtn);
        toolbar.add(new JLabel("  Blood Group:"));
        toolbar.add(bloodGroupFilter);
        toolbar.add(eligibleBtn);
        toolbar.add(refreshBtn);

        topPanel.add(headerRow, BorderLayout.NORTH);
        topPanel.add(toolbar, BorderLayout.SOUTH);
        add(topPanel, BorderLayout.NORTH);

        // Table — Email column visible so staff can see at a glance
        String[] columns = {
            "ID", "Name", "Age", "Gender", "Blood Group",
            "Contact", "Gmail", "Last Donation", "Eligible", "Actions"
        };
        tableModel = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int row, int col) { return col == 9; }
        };

        table = new JTable(tableModel);
        UIHelper.styleTable(table);
        table.getColumnModel().getColumn(0).setMaxWidth(50);
        table.getColumnModel().getColumn(2).setMaxWidth(50);
        table.getColumnModel().getColumn(3).setMaxWidth(70);
        table.getColumnModel().getColumn(4).setMaxWidth(90);
        table.getColumnModel().getColumn(8).setMaxWidth(70);
        table.getColumnModel().getColumn(9).setMinWidth(160);
        table.getColumnModel().getColumn(9).setCellRenderer(new ActionButtonRenderer());
        table.getColumnModel().getColumn(9).setCellEditor(new ActionButtonEditor(table, this));

        // Highlight rows where email is missing (orange tint)
        table.setDefaultRenderer(Object.class, new javax.swing.table.DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object value,
                    boolean isSelected, boolean hasFocus, int row, int col) {
                Component c = super.getTableCellRendererComponent(
                        t, value, isSelected, hasFocus, row, col);
                if (!isSelected) {
                    String email = (String) t.getModel().getValueAt(row, 6);
                    if (email == null || email.isBlank()) {
                        c.setBackground(new Color(0xFFF3E0)); // orange tint = no email
                    } else {
                        c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(0xFAFAFA));
                    }
                }
                setBorder(new EmptyBorder(0, 8, 0, 8));
                return c;
            }
        });

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(0xE0E0E0)));

        // Legend below table
        JPanel legend = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 4));
        legend.setBackground(UIConstants.BG_LIGHT);
        JPanel orangeSwatch = new JPanel();
        orangeSwatch.setBackground(new Color(0xFFF3E0));
        orangeSwatch.setPreferredSize(new Dimension(14, 14));
        orangeSwatch.setBorder(new LineBorder(new Color(0xE0E0E0)));
        legend.add(orangeSwatch);
        JLabel legendLbl = UIHelper.mutedLabel("= Gmail missing — camp emails will NOT be sent to this donor");
        legend.add(legendLbl);

        JPanel center = new JPanel(new BorderLayout());
        center.add(scrollPane, BorderLayout.CENTER);
        center.add(legend, BorderLayout.SOUTH);
        add(center, BorderLayout.CENTER);

        refresh();
    }

    // ── Data Loading ──────────────────────────────────────────────────────────

    public void refresh() {
        new SwingWorker<List<Donor>, Void>() {
            @Override protected List<Donor> doInBackground() throws Exception {
                return ApiClient.listFromJson(ApiClient.get("/donors"), Donor.class);
            }
            @Override protected void done() {
                try { donors = get(); populateTable(donors); }
                catch (Exception e) {
                    UIHelper.showError(DonorPanel.this, "Failed to load donors: " + e.getMessage());
                }
            }
        }.execute();
    }

    private void performSearch() {
        String query = searchField.getText().trim();
        if (query.isEmpty() || query.startsWith("Search by")) { refresh(); return; }
        new SwingWorker<List<Donor>, Void>() {
            @Override protected List<Donor> doInBackground() throws Exception {
                return ApiClient.listFromJson(
                    ApiClient.get("/donors/search?query="
                        + java.net.URLEncoder.encode(query, "UTF-8")), Donor.class);
            }
            @Override protected void done() {
                try { populateTable(get()); }
                catch (Exception e) { UIHelper.showError(DonorPanel.this, e.getMessage()); }
            }
        }.execute();
    }

    private void filterByBloodGroup() {
        String selected = (String) bloodGroupFilter.getSelectedItem();
        if ("All Blood Groups".equals(selected)) { refresh(); return; }
        new SwingWorker<List<Donor>, Void>() {
            @Override protected List<Donor> doInBackground() throws Exception {
                return ApiClient.listFromJson(
                    ApiClient.get("/donors/blood-group/"
                        + java.net.URLEncoder.encode(selected, "UTF-8")), Donor.class);
            }
            @Override protected void done() {
                try { populateTable(get()); }
                catch (Exception e) { UIHelper.showError(DonorPanel.this, e.getMessage()); }
            }
        }.execute();
    }

    private void showEligibleOnly() {
        new SwingWorker<List<Donor>, Void>() {
            @Override protected List<Donor> doInBackground() throws Exception {
                return ApiClient.listFromJson(ApiClient.get("/donors/eligible"), Donor.class);
            }
            @Override protected void done() {
                try { populateTable(get()); }
                catch (Exception e) { UIHelper.showError(DonorPanel.this, e.getMessage()); }
            }
        }.execute();
    }

    private void populateTable(List<Donor> donorList) {
        tableModel.setRowCount(0);
        for (Donor d : donorList) {
            tableModel.addRow(new Object[]{
                d.id, d.name, d.age, d.gender, d.bloodGroup,
                d.contact != null ? d.contact : "",
                d.email   != null ? d.email   : "",
                d.lastDonationDate != null ? d.lastDonationDate : "Never",
                d.isEligible() ? "✅ Yes" : "❌ No",
                "actions"
            });
        }
    }

    // ── Add / Edit Dialog ─────────────────────────────────────────────────────

    public void showDonorDialog(Donor donor) {
        boolean isEdit = donor != null;

        JDialog dialog = new JDialog(
                (Frame) SwingUtilities.getWindowAncestor(this),
                isEdit ? "Edit Donor" : "Add New Donor", true);
        dialog.setSize(540, 600);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());

        // ── Form panel ────────────────────────────────────────────────────────
        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Color.WHITE);
        form.setBorder(new EmptyBorder(20, 28, 10, 28));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(6, 5, 6, 5);

        // Fields
        JTextField nameField    = UIHelper.textField(22);
        JTextField ageField     = UIHelper.textField(10);
        JComboBox<String> genderBox = UIHelper.comboBox(new String[]{"Male","Female","Other"});
        JComboBox<String> bgBox     = UIHelper.comboBox(UIConstants.BLOOD_GROUPS);
        JTextField contactField = UIHelper.textField(22);
        JTextField addressField = UIHelper.textField(22);
        JTextField lastDonField = UIHelper.textField(22);
        lastDonField.setToolTipText("Format: YYYY-MM-DD  e.g. 2026-05-20  (leave blank if never donated)");

        // Live date format hint
        JLabel dateHint = new JLabel("  e.g. 2026-05-20");
        dateHint.setFont(UIConstants.FONT_SMALL);
        dateHint.setForeground(UIConstants.TEXT_MUTED);

        // Gmail field — styled prominently, required
        JTextField emailField = UIHelper.textField(22);
        emailField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        emailField.setToolTipText("Required — used for camp invitations and blood-usage notifications");

        // Live validation indicator next to email
        JLabel emailStatus = new JLabel("  ⚠ Required");
        emailStatus.setFont(UIConstants.FONT_SMALL);
        emailStatus.setForeground(UIConstants.WARNING);

        // Pre-fill for edit
        if (isEdit) {
            nameField.setText(donor.name != null ? donor.name : "");
            ageField.setText(String.valueOf(donor.age));
            if (donor.gender != null) genderBox.setSelectedItem(donor.gender);
            if (donor.bloodGroup != null) bgBox.setSelectedItem(donor.bloodGroup);
            contactField.setText(donor.contact != null ? donor.contact : "");
            emailField.setText(donor.email != null ? donor.email : "");
            addressField.setText(donor.address != null ? donor.address : "");
            lastDonField.setText(donor.lastDonationDate != null ? donor.lastDonationDate : "");
            updateEmailStatus(emailField.getText(), emailStatus);
        }

        // Live email validation as user types
        emailField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e)  { updateEmailStatus(emailField.getText(), emailStatus); }
            public void removeUpdate(DocumentEvent e)  { updateEmailStatus(emailField.getText(), emailStatus); }
            public void changedUpdate(DocumentEvent e) { updateEmailStatus(emailField.getText(), emailStatus); }
        });

        // ── Layout rows ───────────────────────────────────────────────────────
        int row = 0;
        addRow(form, gbc, row++, "Full Name *",               nameField,    null);
        addRow(form, gbc, row++, "Age *  (18–65)",            ageField,     null);
        addRow(form, gbc, row++, "Gender",                    genderBox,    null);
        addRow(form, gbc, row++, "Blood Group *",             bgBox,        null);
        addRow(form, gbc, row++, "Contact Number",            contactField, null);
        addRow(form, gbc, row++, "Gmail * 📧",                emailField,   emailStatus);
        addRow(form, gbc, row++, "Address",                   addressField, null);
        addRow(form, gbc, row++, "Last Donation (YYYY-MM-DD)",lastDonField, dateHint);

        // Info banner
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 3; gbc.weightx = 1.0;
        JPanel infoBanner = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 6));
        infoBanner.setBackground(new Color(0xE3F2FD));
        infoBanner.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0x90CAF9), 1),
                new EmptyBorder(4, 8, 4, 8)));
        JLabel infoLbl = new JLabel(
            "📧  Gmail is used to send: camp invitations · blood-usage notifications · emergency alerts");
        infoLbl.setFont(UIConstants.FONT_SMALL);
        infoLbl.setForeground(new Color(0x1565C0));
        infoBanner.add(infoLbl);
        form.add(infoBanner, gbc);
        gbc.gridwidth = 1;

        // ── Buttons ───────────────────────────────────────────────────────────
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 12));
        btnPanel.setBackground(Color.WHITE);
        JButton cancelBtn = UIHelper.outlineButton("Cancel");
        JButton saveBtn   = UIHelper.primaryButton(isEdit ? "Update Donor" : "Add Donor");

        cancelBtn.addActionListener(e -> dialog.dispose());

        saveBtn.addActionListener(e -> {
            // ── Validation ────────────────────────────────────────────────────
            String name  = nameField.getText().trim();
            String email = emailField.getText().trim();
            String ageStr = ageField.getText().trim();

            if (name.isEmpty()) {
                UIHelper.showError(dialog, "Full Name is required.");
                nameField.requestFocus();
                return;
            }
            if (ageStr.isEmpty()) {
                UIHelper.showError(dialog, "Age is required.");
                ageField.requestFocus();
                return;
            }
            int age;
            try {
                age = Integer.parseInt(ageStr);
                if (age < 18 || age > 65) {
                    UIHelper.showError(dialog, "Donor age must be between 18 and 65.");
                    ageField.requestFocus();
                    return;
                }
            } catch (NumberFormatException ex) {
                UIHelper.showError(dialog, "Age must be a valid number.");
                ageField.requestFocus();
                return;
            }
            // Gmail required and must be valid
            if (email.isEmpty()) {
                UIHelper.showError(dialog,
                    "Gmail is required.\n\nIt is used to send camp invitations and blood-usage notifications.");
                emailField.requestFocus();
                return;
            }
            if (!isValidEmail(email)) {
                UIHelper.showError(dialog,
                    "Please enter a valid email address (e.g. donor@gmail.com).");
                emailField.requestFocus();
                return;
            }

            // ── Build payload ─────────────────────────────────────────────────
            Donor d = isEdit ? donor : new Donor();
            d.name             = name;
            d.age              = age;
            d.gender           = (String) genderBox.getSelectedItem();
            d.bloodGroup       = (String) bgBox.getSelectedItem();
            d.contact          = contactField.getText().trim();
            d.email            = email;
            d.address          = addressField.getText().trim();
            String ld          = lastDonField.getText().trim();
            // Normalize and validate the date — convert DD/MM/YYYY or DD-MM-YYYY to YYYY-MM-DD
            if (!ld.isEmpty()) {
                String normalized = normalizeDate(ld);
                if (normalized == null) {
                    UIHelper.showError(dialog,
                        "Invalid date format: '" + ld + "'\n\nPlease use YYYY-MM-DD format.\nExample: 2026-05-20");
                    lastDonField.requestFocus();
                    lastDonField.selectAll();
                    return;
                }
                d.lastDonationDate = normalized;
            } else {
                d.lastDonationDate = null;
            }

            saveBtn.setEnabled(false);
            saveBtn.setText("Saving...");

            new SwingWorker<Void, Void>() {
                @Override protected Void doInBackground() throws Exception {
                    if (!isEdit) ApiClient.post("/donors", d);
                    else         ApiClient.put("/donors/" + donor.id, d);
                    return null;
                }
                @Override protected void done() {
                    saveBtn.setEnabled(true);
                    saveBtn.setText(isEdit ? "Update Donor" : "Add Donor");
                    try {
                        get();
                        dialog.dispose();
                        refresh();
                        UIHelper.showSuccess(DonorPanel.this,
                            isEdit ? "Donor updated successfully."
                                   : "Donor added! They will receive camp invitation emails at: " + d.email);
                    } catch (Exception ex) {
                        UIHelper.showError(dialog, "Failed to save: " + ex.getMessage());
                    }
                }
            }.execute();
        });

        btnPanel.add(cancelBtn);
        btnPanel.add(saveBtn);

        dialog.add(new JScrollPane(form), BorderLayout.CENTER);
        dialog.add(btnPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    // ── Delete ────────────────────────────────────────────────────────────────

    public void deleteDonor(int row) {
        if (!SessionManager.isAdmin()) {
            UIHelper.showError(this, "Only admins can delete donors.");
            return;
        }
        Long id   = (Long)   tableModel.getValueAt(row, 0);
        String name = (String) tableModel.getValueAt(row, 1);
        if (UIHelper.confirm(this, "Delete donor '" + name + "'? This cannot be undone.")) {
            new SwingWorker<Void, Void>() {
                @Override protected Void doInBackground() throws Exception {
                    ApiClient.delete("/donors/delete/" + id);
                    return null;
                }
                @Override protected void done() {
                    try { get(); refresh(); }
                    catch (Exception e) { UIHelper.showError(DonorPanel.this, e.getMessage()); }
                }
            }.execute();
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    public Donor getDonorAtRow(int row) {
        if (donors == null || row < 0 || row >= tableModel.getRowCount()) return null;
        Long id = (Long) tableModel.getValueAt(row, 0);
        return donors.stream()
                .filter(d -> d.id != null && d.id.equals(id))
                .findFirst().orElse(null);
    }

    /** Updates the live email status label as the user types */
    private void updateEmailStatus(String email, JLabel statusLabel) {
        if (email == null || email.isBlank()) {
            statusLabel.setText("  ⚠ Required");
            statusLabel.setForeground(UIConstants.WARNING);
        } else if (!isValidEmail(email)) {
            statusLabel.setText("  ✗ Invalid email");
            statusLabel.setForeground(UIConstants.DANGER);
        } else {
            statusLabel.setText("  ✓ Valid");
            statusLabel.setForeground(UIConstants.SUCCESS);
        }
    }

    /** Basic email format check */
    private boolean isValidEmail(String email) {
        return email != null
                && email.contains("@")
                && email.contains(".")
                && email.indexOf("@") < email.lastIndexOf(".")
                && email.length() > 5;
    }

    /**
     * Normalizes a date string to ISO format YYYY-MM-DD.
     * Accepts:
     *   YYYY-MM-DD  → returned as-is
     *   DD/MM/YYYY  → converted
     *   DD-MM-YYYY  → converted
     *   MM/DD/YYYY  → rejected (ambiguous, user must use YYYY-MM-DD)
     * Returns null if the date is invalid or unrecognized.
     */
    private String normalizeDate(String input) {
        if (input == null || input.isBlank()) return null;
        input = input.trim();

        // Already ISO: YYYY-MM-DD
        if (input.matches("\\d{4}-\\d{2}-\\d{2}")) {
            try {
                java.time.LocalDate.parse(input);
                return input;
            } catch (Exception e) {
                return null;
            }
        }

        // DD/MM/YYYY or DD-MM-YYYY
        if (input.matches("\\d{2}[/\\-]\\d{2}[/\\-]\\d{4}")) {
            String[] parts = input.split("[/\\-]");
            String converted = parts[2] + "-" + parts[1] + "-" + parts[0];
            try {
                java.time.LocalDate.parse(converted);
                return converted;
            } catch (Exception e) {
                return null;
            }
        }

        return null; // unrecognized format
    }

    /**
     * Adds a form row with an optional status label in a third column.
     * label | field | statusLabel (optional)
     */
    private void addRow(JPanel panel, GridBagConstraints gbc, int row,
                        String label, JComponent field, JLabel statusLabel) {
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.28; gbc.gridwidth = 1;
        JLabel lbl = new JLabel(label + ":");
        lbl.setFont(UIConstants.FONT_BODY);
        // Highlight required fields
        if (label.contains("*")) lbl.setForeground(UIConstants.TEXT_PRIMARY);
        panel.add(lbl, gbc);

        gbc.gridx = 1; gbc.weightx = 0.55;
        panel.add(field, gbc);

        gbc.gridx = 2; gbc.weightx = 0.17;
        if (statusLabel != null) {
            panel.add(statusLabel, gbc);
        } else {
            panel.add(new JLabel(""), gbc);
        }
    }

    // ── Action Button Renderer / Editor ───────────────────────────────────────

    static class ActionButtonRenderer extends JPanel
            implements javax.swing.table.TableCellRenderer {
        ActionButtonRenderer() {
            setLayout(new FlowLayout(FlowLayout.CENTER, 4, 3));
            setOpaque(true);
        }
        @Override
        public Component getTableCellRendererComponent(JTable t, Object v,
                boolean sel, boolean foc, int r, int c) {
            removeAll();
            setBackground(sel ? t.getSelectionBackground() : t.getBackground());
            add(btn("✏ Edit",   UIConstants.INFO));
            add(btn("🗑 Delete", UIConstants.DANGER));
            return this;
        }
        private JButton btn(String text, Color bg) {
            JButton b = new JButton(text);
            b.setFont(UIConstants.FONT_SMALL);
            b.setBackground(bg);
            b.setForeground(Color.WHITE);
            b.setBorderPainted(false);
            b.setFocusPainted(false);
            return b;
        }
    }

    static class ActionButtonEditor extends DefaultCellEditor {
        private final DonorPanel panel;
        private int currentRow;

        ActionButtonEditor(JTable table, DonorPanel panel) {
            super(new JCheckBox());
            this.panel = panel;
            setClickCountToStart(1);
        }

        @Override
        public Component getTableCellEditorComponent(JTable t, Object v,
                boolean sel, int row, int col) {
            currentRow = row;
            JPanel p = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 3));
            p.setBackground(t.getSelectionBackground());

            JButton edit = btn("✏ Edit", UIConstants.INFO);
            edit.addActionListener(e -> {
                fireEditingStopped();
                Donor d = panel.getDonorAtRow(currentRow);
                if (d != null) panel.showDonorDialog(d);
            });

            JButton del = btn("🗑 Delete", UIConstants.DANGER);
            del.addActionListener(e -> {
                fireEditingStopped();
                panel.deleteDonor(currentRow);
            });

            p.add(edit); p.add(del);
            return p;
        }

        @Override public Object getCellEditorValue() { return ""; }

        private JButton btn(String text, Color bg) {
            JButton b = new JButton(text);
            b.setFont(UIConstants.FONT_SMALL);
            b.setBackground(bg);
            b.setForeground(Color.WHITE);
            b.setBorderPainted(false);
            b.setFocusPainted(false);
            return b;
        }
    }
}
