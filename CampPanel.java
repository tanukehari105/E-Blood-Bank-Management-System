package com.bloodbank.ui.screens;

import com.bloodbank.ui.api.ApiClient;
import com.bloodbank.ui.model.DonationCamp;
import com.bloodbank.ui.util.SessionManager;
import com.bloodbank.ui.util.UIConstants;
import com.bloodbank.ui.util.UIHelper;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.IOException;
import java.util.List;

/**
 * Donation Camp management panel.
 * ADMIN: full CRUD + cancel. STAFF: view only.
 */
public class CampPanel extends JPanel {

    private JTable table;
    private DefaultTableModel tableModel;
    private List<DonationCamp> camps;

    public CampPanel() {
        setLayout(new BorderLayout(0, 0));
        setBackground(UIConstants.BG_LIGHT);
        setBorder(new EmptyBorder(20, 24, 20, 24));
        initUI();
    }

    private void initUI() {
        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(UIConstants.BG_LIGHT);
        header.setBorder(new EmptyBorder(0, 0, 16, 0));
        header.add(UIHelper.titleLabel("🏕️ Donation Camps"), BorderLayout.WEST);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actions.setBackground(UIConstants.BG_LIGHT);

        if (SessionManager.isAdmin()) {
            JButton addBtn = UIHelper.primaryButton("+ Create Camp");
            addBtn.addActionListener(e -> showCampDialog(null));
            actions.add(addBtn);
        }

        JButton refreshBtn = UIHelper.outlineButton("↻ Refresh");
        refreshBtn.addActionListener(e -> refresh());
        actions.add(refreshBtn);
        header.add(actions, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        // Table
        String[] cols = {"ID", "Camp Name", "Location", "Date", "Time", "Expected", "Actual", "Units", "Status", "Actions"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return c == 9; }
        };
        table = new JTable(tableModel);
        UIHelper.styleTable(table);
        table.getColumnModel().getColumn(0).setMaxWidth(55);
        table.getColumnModel().getColumn(7).setMaxWidth(70);
        table.getColumnModel().getColumn(8).setMaxWidth(90);
        table.getColumnModel().getColumn(9).setMinWidth(SessionManager.isAdmin() ? 200 : 80);

        table.getColumnModel().getColumn(9).setCellRenderer(new CampActionRenderer());
        table.getColumnModel().getColumn(9).setCellEditor(new CampActionEditor());

        add(UIHelper.scrollPane(table), BorderLayout.CENTER);
        refresh();
    }

    public void refresh() {
        SwingWorker<List<DonationCamp>, Void> worker = new SwingWorker<>() {
            @Override protected List<DonationCamp> doInBackground() throws Exception {
                return ApiClient.listFromJson(ApiClient.get("/camps"), DonationCamp.class);
            }
            @Override protected void done() {
                try {
                    camps = get();
                    populateTable(camps);
                } catch (Exception e) {
                    UIHelper.showError(CampPanel.this, "Failed to load camps: " + e.getMessage());
                }
            }
        };
        worker.execute();
    }

    private void populateTable(List<DonationCamp> list) {
        tableModel.setRowCount(0);
        for (DonationCamp c : list) {
            tableModel.addRow(new Object[]{
                c.id, c.campName, c.location,
                c.campDate != null ? c.campDate : "",
                c.getTimeRange(),
                c.expectedDonors, c.actualDonors, c.totalUnitsCollected,
                c.active ? "✅ Active" : "❌ Cancelled",
                "actions"
            });
        }
    }

    private DonationCamp getCampAtRow(int row) {
        if (camps == null || row < 0) return null;
        Long id = (Long) tableModel.getValueAt(row, 0);
        return camps.stream().filter(c -> c.id.equals(id)).findFirst().orElse(null);
    }

    private void showCampDialog(DonationCamp existing) {
        boolean isEdit = existing != null;
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this),
                isEdit ? "Edit Camp" : "Create Camp", true);
        dialog.setSize(500, 520);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(new EmptyBorder(20, 24, 20, 24));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        JTextField nameField = UIHelper.textField(20);
        JTextField organizerField = UIHelper.textField(20);
        JTextField locationField = UIHelper.textField(20);
        JTextField dateField = UIHelper.textField(20);
        dateField.setToolTipText("Format: YYYY-MM-DD");
        JTextField startField = UIHelper.textField(10);
        startField.setToolTipText("Format: HH:MM");
        JTextField endField = UIHelper.textField(10);
        endField.setToolTipText("Format: HH:MM");
        JTextField contactField = UIHelper.textField(20);
        JTextField expectedField = UIHelper.textField(10);
        JTextArea descArea = new JTextArea(3, 20);
        descArea.setFont(UIConstants.FONT_BODY);
        descArea.setLineWrap(true);

        if (isEdit) {
            nameField.setText(existing.campName);
            organizerField.setText(existing.organizerName != null ? existing.organizerName : "");
            locationField.setText(existing.location);
            dateField.setText(existing.campDate != null ? existing.campDate : "");
            startField.setText(existing.startTime != null ? existing.startTime : "");
            endField.setText(existing.endTime != null ? existing.endTime : "");
            contactField.setText(existing.contactNumber != null ? existing.contactNumber : "");
            expectedField.setText(String.valueOf(existing.expectedDonors));
            descArea.setText(existing.description != null ? existing.description : "");
        }

        String[] labels = {"Camp Name *", "Organizer", "Location *", "Date * (YYYY-MM-DD)",
                "Start Time (HH:MM)", "End Time (HH:MM)", "Contact Number", "Expected Donors", "Description"};
        Component[] inputs = {nameField, organizerField, locationField, dateField,
                startField, endField, contactField, expectedField, new JScrollPane(descArea)};

        for (int i = 0; i < labels.length; i++) {
            gbc.gridx = 0; gbc.gridy = i; gbc.weightx = 0.35;
            panel.add(new JLabel(labels[i]), gbc);
            gbc.gridx = 1; gbc.weightx = 0.65;
            panel.add(inputs[i], gbc);
        }

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton saveBtn = UIHelper.primaryButton(isEdit ? "Update" : "Create & Invite Donors");
        JButton cancelBtn = UIHelper.outlineButton("Cancel");
        cancelBtn.addActionListener(e -> dialog.dispose());
        saveBtn.addActionListener(e -> {
            if (nameField.getText().isBlank() || locationField.getText().isBlank() || dateField.getText().isBlank()) {
                UIHelper.showError(dialog, "Camp Name, Location, and Date are required.");
                return;
            }
            try {
                String body = buildCampJson(nameField.getText().trim(), organizerField.getText().trim(),
                        locationField.getText().trim(), dateField.getText().trim(),
                        startField.getText().trim(), endField.getText().trim(),
                        contactField.getText().trim(),
                        expectedField.getText().isBlank() ? 0 : Integer.parseInt(expectedField.getText().trim()),
                        descArea.getText().trim());
                if (isEdit) {
                    ApiClient.putRaw("/camps/" + existing.id, body);
                    UIHelper.showSuccess(dialog, "Camp updated.");
                } else {
                    ApiClient.postRaw("/camps", body);
                    UIHelper.showSuccess(dialog, "Camp created! Invitation emails sent to eligible donors.");
                }
                dialog.dispose();
                refresh();
            } catch (Exception ex) {
                UIHelper.showError(dialog, "Error: " + ex.getMessage());
            }
        });
        btnPanel.add(cancelBtn);
        btnPanel.add(saveBtn);
        gbc.gridx = 0; gbc.gridy = labels.length; gbc.gridwidth = 2;
        panel.add(btnPanel, gbc);

        dialog.add(new JScrollPane(panel));
        dialog.setVisible(true);
    }

    private String buildCampJson(String name, String organizer, String location, String date,
                                  String start, String end, String contact, int expected, String desc) {
        return String.format("{\"campName\":\"%s\",\"organizerName\":\"%s\",\"location\":\"%s\"," +
                "\"campDate\":\"%s\",\"startTime\":\"%s\",\"endTime\":\"%s\"," +
                "\"contactNumber\":\"%s\",\"expectedDonors\":%d,\"description\":\"%s\"}",
                escape(name), escape(organizer), escape(location), date,
                start.isBlank() ? "08:00" : start,
                end.isBlank() ? "17:00" : end,
                escape(contact), expected, escape(desc));
    }

    private String escape(String s) { return s == null ? "" : s.replace("\"", "\\\""); }

    // ── Action Renderers ───────────────────────────────────────────────────────

    private class CampActionRenderer implements javax.swing.table.TableCellRenderer {
        @Override public Component getTableCellRendererComponent(JTable t, Object v,
                boolean sel, boolean focus, int row, int col) {
            return buildActionPanel(row);
        }
    }

    private class CampActionEditor extends DefaultCellEditor {
        CampActionEditor() { super(new JCheckBox()); setClickCountToStart(1); }
        @Override public Component getTableCellEditorComponent(JTable t, Object v,
                boolean sel, int row, int col) { return buildActionPanel(row); }
        @Override public Object getCellEditorValue() { return ""; }
    }

    private JPanel buildActionPanel(int row) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 2));
        p.setBackground(row % 2 == 0 ? Color.WHITE : new Color(0xFAFAFA));

        if (SessionManager.isAdmin()) {
            JButton editBtn = UIHelper.infoButton("✏ Edit");
            editBtn.setFont(UIConstants.FONT_SMALL);
            editBtn.addActionListener(e -> {
                DonationCamp c = getCampAtRow(row);
                if (c != null) showCampDialog(c);
            });

            JButton cancelBtn = UIHelper.warningButton("✖ Cancel");
            cancelBtn.setFont(UIConstants.FONT_SMALL);
            cancelBtn.addActionListener(e -> {
                DonationCamp c = getCampAtRow(row);
                if (c != null && UIHelper.confirm(CampPanel.this,
                        "Cancel camp '" + c.campName + "'? Donors will be notified.")) {
                    try {
                        ApiClient.put("/camps/" + c.id + "/cancel", null);
                        refresh();
                    } catch (IOException ex) {
                        UIHelper.showError(CampPanel.this, ex.getMessage());
                    }
                }
            });

            JButton deleteBtn = UIHelper.dangerButton("🗑");
            deleteBtn.setFont(UIConstants.FONT_SMALL);
            deleteBtn.addActionListener(e -> {
                DonationCamp c = getCampAtRow(row);
                if (c != null && UIHelper.confirm(CampPanel.this, "Delete camp '" + c.campName + "'?")) {
                    try {
                        ApiClient.delete("/camps/" + c.id);
                        refresh();
                    } catch (IOException ex) {
                        UIHelper.showError(CampPanel.this, ex.getMessage());
                    }
                }
            });

            p.add(editBtn);
            p.add(cancelBtn);
            p.add(deleteBtn);
        } else {
            JLabel viewLbl = new JLabel("View Only");
            viewLbl.setFont(UIConstants.FONT_SMALL);
            viewLbl.setForeground(UIConstants.TEXT_MUTED);
            p.add(viewLbl);
        }
        return p;
    }
}
