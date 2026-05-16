package com.bloodbank.ui.screens;

import com.bloodbank.ui.api.ApiClient;
import com.bloodbank.ui.model.Donation;
import com.bloodbank.ui.model.Donor;
import com.bloodbank.ui.util.UIConstants;
import com.bloodbank.ui.util.UIHelper;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Donation Panel — full workflow:
 *
 *  1. Staff clicks "+ Register Donation"
 *  2. Dialog opens with a SEARCH box — search existing donors by name/contact
 *  3. If donor found → select them
 *  4. If donor NOT found → click "Add New Donor" → inline form to add donor first
 *  5. Enter units donated
 *  6. Submit → blood added to inventory linked to that donor
 *  7. When that blood is later used for a request → email sent to donor automatically
 */
public class DonationPanel extends JPanel {

    private JTable table;
    private DefaultTableModel tableModel;

    public DonationPanel() {
        setLayout(new BorderLayout());
        setBackground(UIConstants.BG_LIGHT);
        setBorder(new EmptyBorder(25, 25, 25, 25));
        initUI();
    }

    // ── UI Setup ──────────────────────────────────────────────────────────────

    private void initUI() {
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);
        topPanel.setBorder(new EmptyBorder(0, 0, 15, 0));
        topPanel.add(UIHelper.titleLabel("💉 Donation Records"), BorderLayout.WEST);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btnPanel.setOpaque(false);

        JButton registerBtn = UIHelper.successButton("+ Register Donation");
        registerBtn.addActionListener(e -> openDonationWorkflow());

        JButton refreshBtn = UIHelper.outlineButton("↻ Refresh");
        refreshBtn.addActionListener(e -> refresh());

        btnPanel.add(registerBtn);
        btnPanel.add(refreshBtn);
        topPanel.add(btnPanel, BorderLayout.EAST);
        add(topPanel, BorderLayout.NORTH);

        // Table
        String[] columns = {"ID", "Donor Name", "Blood Group", "Units", "Batch Code", "Date", "Notes"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(tableModel);
        UIHelper.styleTable(table);
        table.getColumnModel().getColumn(0).setMaxWidth(55);
        table.getColumnModel().getColumn(2).setMaxWidth(90);
        table.getColumnModel().getColumn(3).setMaxWidth(60);
        table.getColumnModel().getColumn(4).setPreferredWidth(160);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(0xE0E0E0)));
        add(scrollPane, BorderLayout.CENTER);

        refresh();
    }

    // ── Data ──────────────────────────────────────────────────────────────────

    public void refresh() {
        new SwingWorker<List<Donation>, Void>() {
            @Override protected List<Donation> doInBackground() throws Exception {
                return ApiClient.listFromJson(ApiClient.get("/donations"), Donation.class);
            }
            @Override protected void done() {
                try {
                    List<Donation> donations = get();
                    tableModel.setRowCount(0);
                    for (Donation d : donations) {
                        tableModel.addRow(new Object[]{
                            d.id,
                            d.donor != null ? d.donor.name : "Unknown",
                            d.bloodGroup,
                            d.quantity,
                            "",   // batch code not in Donation model — shown in Inventory panel
                            d.donationDate != null ? d.donationDate.toString() : "",
                            d.notes != null ? d.notes : ""
                        });
                    }
                } catch (Exception e) {
                    UIHelper.showError(DonationPanel.this, "Failed to load donations: " + e.getMessage());
                }
            }
        }.execute();
    }

    // ── Step 1: Donation Workflow Dialog ──────────────────────────────────────

    /**
     * Main entry point — opens the donation registration dialog.
     * Staff can search for an existing donor or add a new one.
     */
    private void openDonationWorkflow() {
        JDialog dialog = new JDialog(
                (Frame) SwingUtilities.getWindowAncestor(this),
                "Register Blood Donation", true);
        dialog.setSize(560, 520);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());

        JPanel main = new JPanel(new BorderLayout(0, 12));
        main.setBackground(Color.WHITE);
        main.setBorder(new EmptyBorder(20, 24, 16, 24));

        // ── Title ─────────────────────────────────────────────────────────────
        JLabel title = new JLabel("💉 Register Blood Donation");
        title.setFont(UIConstants.FONT_TITLE);
        title.setForeground(UIConstants.PRIMARY);
        main.add(title, BorderLayout.NORTH);

        // ── Center: search + donor info + units ───────────────────────────────
        JPanel center = new JPanel();
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        center.setBackground(Color.WHITE);

        // Search row
        JPanel searchRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        searchRow.setBackground(Color.WHITE);
        JTextField searchField = UIHelper.textField(22);
        searchField.setToolTipText("Search by name or contact number");
        JButton searchBtn = UIHelper.infoButton("🔍 Search");
        JButton addNewBtn = UIHelper.warningButton("+ Add New Donor");
        searchRow.add(new JLabel("Search Donor:"));
        searchRow.add(searchField);
        searchRow.add(searchBtn);
        searchRow.add(addNewBtn);
        center.add(searchRow);

        // Results list
        DefaultListModel<Donor> listModel = new DefaultListModel<>();
        JList<Donor> donorList = new JList<>(listModel);
        donorList.setFont(UIConstants.FONT_BODY);
        donorList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        donorList.setCellRenderer(new DonorListRenderer());
        JScrollPane listScroll = new JScrollPane(donorList);
        listScroll.setPreferredSize(new Dimension(500, 130));
        listScroll.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(0xDDE1E7), 1),
                new EmptyBorder(2, 2, 2, 2)));
        center.add(Box.createVerticalStrut(6));
        center.add(listScroll);

        // Selected donor info card
        JPanel donorCard = new JPanel(new GridLayout(2, 2, 8, 4));
        donorCard.setBackground(new Color(0xF0FFF4));
        donorCard.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(0x27AE60), 1),
                new EmptyBorder(10, 14, 10, 14)));
        donorCard.setVisible(false);
        JLabel cardName  = new JLabel(); cardName.setFont(UIConstants.FONT_BOLD);
        JLabel cardBG    = new JLabel(); cardBG.setFont(UIConstants.FONT_BOLD); cardBG.setForeground(UIConstants.PRIMARY);
        JLabel cardEmail = new JLabel(); cardEmail.setFont(UIConstants.FONT_SMALL); cardEmail.setForeground(UIConstants.TEXT_SECONDARY);
        JLabel cardElig  = new JLabel(); cardElig.setFont(UIConstants.FONT_SMALL);
        donorCard.add(cardName); donorCard.add(cardBG);
        donorCard.add(cardEmail); donorCard.add(cardElig);
        center.add(Box.createVerticalStrut(8));
        center.add(donorCard);

        // Units + notes row
        JPanel unitsRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 4));
        unitsRow.setBackground(Color.WHITE);
        JTextField unitsField = UIHelper.textField(6);
        unitsField.setText("1");
        JTextField notesField = UIHelper.textField(18);
        unitsRow.add(new JLabel("Units Donated *:"));
        unitsRow.add(unitsField);
        unitsRow.add(new JLabel("  Notes:"));
        unitsRow.add(notesField);
        center.add(Box.createVerticalStrut(8));
        center.add(unitsRow);

        // Info banner
        JPanel infoBanner = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 4));
        infoBanner.setBackground(new Color(0xE3F2FD));
        infoBanner.setBorder(new LineBorder(new Color(0x90CAF9), 1));
        JLabel infoLbl = new JLabel(
            "📧  When this blood is used for a patient, an email will be sent to the donor automatically.");
        infoLbl.setFont(UIConstants.FONT_SMALL);
        infoLbl.setForeground(new Color(0x1565C0));
        infoBanner.add(infoLbl);
        center.add(Box.createVerticalStrut(6));
        center.add(infoBanner);

        main.add(center, BorderLayout.CENTER);

        // ── Buttons ───────────────────────────────────────────────────────────
        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btnRow.setBackground(Color.WHITE);
        JButton cancelBtn  = UIHelper.outlineButton("Cancel");
        JButton registerBtn = UIHelper.primaryButton("✔ Register Donation");
        registerBtn.setEnabled(false);
        btnRow.add(cancelBtn);
        btnRow.add(registerBtn);
        main.add(btnRow, BorderLayout.SOUTH);

        dialog.add(main);

        // ── Holder for selected donor ─────────────────────────────────────────
        final Donor[] selectedDonor = {null};

        // Enable register button only when donor is selected
        donorList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                Donor d = donorList.getSelectedValue();
                selectedDonor[0] = d;
                if (d != null) {
                    cardName.setText("👤 " + d.name + "  (Age: " + d.age + ", " + d.gender + ")");
                    cardBG.setText("🩸 Blood Group: " + d.bloodGroup);
                    cardEmail.setText("📧 " + (d.email != null && !d.email.isBlank() ? d.email : "⚠ No email — notification won't be sent"));
                    cardElig.setText(d.isEligible() ? "✅ Eligible to donate" : "❌ Not eligible (< 90 days)");
                    cardElig.setForeground(d.isEligible() ? UIConstants.SUCCESS : UIConstants.DANGER);
                    donorCard.setVisible(true);
                    registerBtn.setEnabled(d.isEligible());
                } else {
                    donorCard.setVisible(false);
                    registerBtn.setEnabled(false);
                }
                dialog.revalidate();
            }
        });

        // Search action
        searchBtn.addActionListener(e -> {
            String query = searchField.getText().trim();
            if (query.isBlank()) {
                UIHelper.showError(dialog, "Enter a name or contact number to search.");
                return;
            }
            searchBtn.setEnabled(false);
            searchBtn.setText("Searching...");
            new SwingWorker<List<Donor>, Void>() {
                @Override protected List<Donor> doInBackground() throws Exception {
                    return ApiClient.listFromJson(
                        ApiClient.get("/donors/search?query=" +
                            java.net.URLEncoder.encode(query, "UTF-8")), Donor.class);
                }
                @Override protected void done() {
                    searchBtn.setEnabled(true);
                    searchBtn.setText("🔍 Search");
                    try {
                        List<Donor> results = get();
                        listModel.clear();
                        if (results.isEmpty()) {
                            UIHelper.showError(dialog,
                                "No donor found for \"" + query + "\".\n\nClick '+ Add New Donor' to register them first.");
                        } else {
                            results.forEach(listModel::addElement);
                            donorList.setSelectedIndex(0);
                        }
                    } catch (Exception ex) {
                        UIHelper.showError(dialog, "Search failed: " + ex.getMessage());
                    }
                }
            }.execute();
        });

        // Enter key triggers search
        searchField.addActionListener(e -> searchBtn.doClick());

        // Add New Donor button — opens inline add donor dialog
        addNewBtn.addActionListener(e -> {
            dialog.setVisible(false);
            openAddDonorThenDonate(dialog, listModel, donorList, selectedDonor);
        });

        // Cancel
        cancelBtn.addActionListener(e -> dialog.dispose());

        // Register
        registerBtn.addActionListener(e -> {
            Donor donor = selectedDonor[0];
            if (donor == null) { UIHelper.showError(dialog, "Please select a donor."); return; }
            int units;
            try {
                units = Integer.parseInt(unitsField.getText().trim());
                if (units <= 0) throw new NumberFormatException();
            } catch (NumberFormatException ex) {
                UIHelper.showError(dialog, "Enter a valid number of units (e.g. 1, 2, 3).");
                unitsField.requestFocus();
                return;
            }

            registerBtn.setEnabled(false);
            registerBtn.setText("Registering...");

            Map<String, Object> payload = new HashMap<>();
            payload.put("donorId", donor.id);
            payload.put("quantity", units);
            payload.put("notes", notesField.getText().trim());

            new SwingWorker<Void, Void>() {
                @Override protected Void doInBackground() throws Exception {
                    ApiClient.post("/donations/register", payload);
                    return null;
                }
                @Override protected void done() {
                    registerBtn.setEnabled(true);
                    registerBtn.setText("✔ Register Donation");
                    try {
                        get();
                        dialog.dispose();
                        refresh();
                        String emailNote = (donor.email != null && !donor.email.isBlank())
                            ? "\n\n📧 " + donor.email + " will be notified when this blood is used."
                            : "\n\n⚠ No email on file — add donor's email to enable notifications.";
                        UIHelper.showSuccess(DonationPanel.this,
                            "✅ Donation registered!\n" +
                            "Donor: " + donor.name + " | Blood Group: " + donor.bloodGroup +
                            " | " + units + " units added to inventory." + emailNote);
                    } catch (Exception ex) {
                        UIHelper.showError(dialog, "Failed to register: " + ex.getMessage());
                    }
                }
            }.execute();
        });

        dialog.setVisible(true);
    }

    // ── Step 2 (if needed): Add New Donor then return to donation ─────────────

    /**
     * Opens the Add Donor form. After saving, returns to the donation dialog
     * with the new donor pre-selected.
     */
    private void openAddDonorThenDonate(JDialog parentDialog,
                                         DefaultListModel<Donor> listModel,
                                         JList<Donor> donorList,
                                         Donor[] selectedDonorHolder) {
        JDialog addDialog = new JDialog(
                (Frame) SwingUtilities.getWindowAncestor(this),
                "Add New Donor", true);
        addDialog.setSize(520, 560);
        addDialog.setLocationRelativeTo(this);
        addDialog.setLayout(new BorderLayout());

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Color.WHITE);
        form.setBorder(new EmptyBorder(20, 28, 10, 28));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(6, 5, 6, 5);

        JTextField nameField    = UIHelper.textField(22);
        JTextField ageField     = UIHelper.textField(8);
        JComboBox<String> genderBox = UIHelper.comboBox(new String[]{"Male","Female","Other"});
        JComboBox<String> bgBox     = UIHelper.comboBox(UIConstants.BLOOD_GROUPS);
        JTextField contactField = UIHelper.textField(22);
        JTextField emailField   = UIHelper.textField(22);
        emailField.setToolTipText("Required — used to notify donor when their blood saves a life");
        JTextField addressField = UIHelper.textField(22);

        // Live email status
        JLabel emailStatus = new JLabel("  ⚠ Required");
        emailStatus.setFont(UIConstants.FONT_SMALL);
        emailStatus.setForeground(UIConstants.WARNING);
        emailField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e)  { updateEmailStatus(emailField.getText(), emailStatus); }
            public void removeUpdate(javax.swing.event.DocumentEvent e)  { updateEmailStatus(emailField.getText(), emailStatus); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { updateEmailStatus(emailField.getText(), emailStatus); }
        });

        int row = 0;
        addFormRow(form, gbc, row++, "Full Name *",    nameField,    null);
        addFormRow(form, gbc, row++, "Age * (18-65)",  ageField,     null);
        addFormRow(form, gbc, row++, "Gender",         genderBox,    null);
        addFormRow(form, gbc, row++, "Blood Group *",  bgBox,        null);
        addFormRow(form, gbc, row++, "Contact Number", contactField, null);
        addFormRow(form, gbc, row++, "Gmail * 📧",     emailField,   emailStatus);
        addFormRow(form, gbc, row++, "Address",        addressField, null);

        // Info banner
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 3; gbc.weightx = 1.0;
        JPanel banner = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 4));
        banner.setBackground(new Color(0xE3F2FD));
        banner.setBorder(new LineBorder(new Color(0x90CAF9), 1));
        JLabel bannerLbl = new JLabel("📧  Gmail is used to notify the donor when their blood saves a patient's life.");
        bannerLbl.setFont(UIConstants.FONT_SMALL);
        bannerLbl.setForeground(new Color(0x1565C0));
        banner.add(bannerLbl);
        form.add(banner, gbc);
        gbc.gridwidth = 1;

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 12));
        btnPanel.setBackground(Color.WHITE);
        JButton backBtn = UIHelper.outlineButton("← Back");
        JButton saveBtn = UIHelper.primaryButton("Save & Continue");

        backBtn.addActionListener(e -> {
            addDialog.dispose();
            parentDialog.setVisible(true);
        });

        saveBtn.addActionListener(e -> {
            String name  = nameField.getText().trim();
            String email = emailField.getText().trim();
            String ageStr = ageField.getText().trim();

            if (name.isEmpty()) { UIHelper.showError(addDialog, "Full Name is required."); return; }
            if (ageStr.isEmpty()) { UIHelper.showError(addDialog, "Age is required."); return; }
            int age;
            try {
                age = Integer.parseInt(ageStr);
                if (age < 18 || age > 65) { UIHelper.showError(addDialog, "Age must be 18–65."); return; }
            } catch (NumberFormatException ex) { UIHelper.showError(addDialog, "Age must be a number."); return; }
            if (email.isEmpty()) {
                UIHelper.showError(addDialog,
                    "Gmail is required.\nIt is used to notify the donor when their blood saves a life.");
                return;
            }
            if (!email.contains("@") || !email.contains(".")) {
                UIHelper.showError(addDialog, "Please enter a valid email address.");
                return;
            }

            Donor d = new Donor();
            d.name       = name;
            d.age        = age;
            d.gender     = (String) genderBox.getSelectedItem();
            d.bloodGroup = (String) bgBox.getSelectedItem();
            d.contact    = contactField.getText().trim();
            d.email      = email;
            d.address    = addressField.getText().trim();

            saveBtn.setEnabled(false);
            saveBtn.setText("Saving...");

            new SwingWorker<Donor, Void>() {
                @Override protected Donor doInBackground() throws Exception {
                    String json = ApiClient.post("/donors", d);
                    return ApiClient.fromJson(json, Donor.class);
                }
                @Override protected void done() {
                    saveBtn.setEnabled(true);
                    saveBtn.setText("Save & Continue");
                    try {
                        Donor saved = get();
                        addDialog.dispose();
                        // Pre-populate the donor list with the new donor
                        listModel.clear();
                        listModel.addElement(saved);
                        donorList.setSelectedIndex(0);
                        selectedDonorHolder[0] = saved;
                        parentDialog.setVisible(true);
                        UIHelper.showSuccess(parentDialog,
                            "Donor '" + saved.name + "' added successfully!\nNow enter the units and click Register.");
                    } catch (Exception ex) {
                        UIHelper.showError(addDialog, "Failed to save donor: " + ex.getMessage());
                    }
                }
            }.execute();
        });

        btnPanel.add(backBtn);
        btnPanel.add(saveBtn);

        addDialog.add(new JScrollPane(form), BorderLayout.CENTER);
        addDialog.add(btnPanel, BorderLayout.SOUTH);
        addDialog.setVisible(true);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private void updateEmailStatus(String email, JLabel lbl) {
        if (email == null || email.isBlank()) {
            lbl.setText("  ⚠ Required"); lbl.setForeground(UIConstants.WARNING);
        } else if (!email.contains("@") || !email.contains(".")) {
            lbl.setText("  ✗ Invalid");  lbl.setForeground(UIConstants.DANGER);
        } else {
            lbl.setText("  ✓ Valid");    lbl.setForeground(UIConstants.SUCCESS);
        }
    }

    private void addFormRow(JPanel panel, GridBagConstraints gbc, int row,
                             String label, JComponent field, JLabel status) {
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.28; gbc.gridwidth = 1;
        JLabel lbl = new JLabel(label + ":");
        lbl.setFont(UIConstants.FONT_BODY);
        panel.add(lbl, gbc);
        gbc.gridx = 1; gbc.weightx = 0.55;
        panel.add(field, gbc);
        gbc.gridx = 2; gbc.weightx = 0.17;
        panel.add(status != null ? status : new JLabel(""), gbc);
    }

    // ── Donor List Renderer ───────────────────────────────────────────────────

    /** Shows donor name, blood group, eligibility, and email status in the list */
    private static class DonorListRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value,
                int index, boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if (value instanceof Donor d) {
                String emailIcon = (d.email != null && !d.email.isBlank()) ? "📧" : "⚠";
                String eligIcon  = d.isEligible() ? "✅" : "❌";
                setText(String.format("  %s  %s  |  🩸 %s  |  %s  |  %s",
                        eligIcon, d.name, d.bloodGroup,
                        d.contact != null ? d.contact : "no contact",
                        emailIcon));
                setFont(UIConstants.FONT_BODY);
                if (!isSelected) {
                    setBackground(d.isEligible() ? Color.WHITE : new Color(0xFFF3E0));
                    setForeground(d.isEligible() ? UIConstants.TEXT_PRIMARY : UIConstants.TEXT_SECONDARY);
                }
            }
            return this;
        }
    }
}
