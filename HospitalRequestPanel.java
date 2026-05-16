package com.bloodbank.ui.screens;

import com.bloodbank.ui.api.ApiClient;
import com.bloodbank.ui.model.BloodRequest;
import com.bloodbank.ui.util.SessionManager;
import com.bloodbank.ui.util.UIConstants;
import com.bloodbank.ui.util.UIHelper;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.IOException;
import java.util.List;

/**
 * Hospital blood request panel — submit requests and track status.
 */
public class HospitalRequestPanel extends JPanel {

    private JTable table;
    private DefaultTableModel tableModel;
    private List<BloodRequest> requests;

    public HospitalRequestPanel() {
        setLayout(new BorderLayout(0, 0));
        setBackground(UIConstants.BG_LIGHT);
        setBorder(new EmptyBorder(20, 24, 20, 24));
        initUI();
    }

    private void initUI() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(UIConstants.BG_LIGHT);
        header.setBorder(new EmptyBorder(0, 0, 16, 0));
        header.add(UIHelper.titleLabel("📋 Blood Requests"), BorderLayout.WEST);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actions.setBackground(UIConstants.BG_LIGHT);
        JButton newBtn = UIHelper.primaryButton("+ New Request");
        newBtn.addActionListener(e -> showRequestDialog());
        JButton refreshBtn = UIHelper.outlineButton("↻ Refresh");
        refreshBtn.addActionListener(e -> refresh());
        actions.add(newBtn);
        actions.add(refreshBtn);
        header.add(actions, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        String[] cols = {"ID", "Patient", "Blood Group", "Units", "Urgency", "Status", "Request Date", "Notes"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(tableModel);
        UIHelper.styleTable(table);
        table.getColumnModel().getColumn(0).setMaxWidth(55);
        table.getColumnModel().getColumn(3).setMaxWidth(70);
        table.getColumnModel().getColumn(4).setMaxWidth(90);
        table.getColumnModel().getColumn(5).setMaxWidth(130);

        // Color-code status and urgency
        table.getColumnModel().getColumn(5).setCellRenderer(new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable t, Object v,
                    boolean sel, boolean focus, int row, int col) {
                Component c = super.getTableCellRendererComponent(t, v, sel, focus, row, col);
                if (!sel && v != null) {
                    String s = v.toString();
                    if (s.contains("APPROVED")) c.setForeground(UIConstants.SUCCESS);
                    else if (s.contains("REJECTED")) c.setForeground(UIConstants.DANGER);
                    else c.setForeground(UIConstants.WARNING);
                }
                setBorder(new EmptyBorder(0, 8, 0, 8));
                return c;
            }
        });

        add(UIHelper.scrollPane(table), BorderLayout.CENTER);
        refresh();
    }

    public void refresh() {
        SwingWorker<List<BloodRequest>, Void> worker = new SwingWorker<>() {
            @Override protected List<BloodRequest> doInBackground() throws Exception {
                Long hid = SessionManager.getHospitalId();
                String endpoint = hid != null ? "/hospital-dashboard/requests" : "/requests";
                return ApiClient.listFromJson(ApiClient.get(endpoint), BloodRequest.class);
            }
            @Override protected void done() {
                try {
                    requests = get();
                    populateTable(requests);
                } catch (Exception e) {
                    UIHelper.showError(HospitalRequestPanel.this, "Failed to load requests: " + e.getMessage());
                }
            }
        };
        worker.execute();
    }

    private void populateTable(List<BloodRequest> list) {
        tableModel.setRowCount(0);
        for (BloodRequest r : list) {
            tableModel.addRow(new Object[]{
                r.id, r.patientName, r.bloodGroup, r.units,
                r.urgencyLevel != null ? r.urgencyLevel : "NORMAL",
                r.status, r.requestDate != null ? r.requestDate : "",
                r.notes != null ? r.notes : ""
            });
        }
    }

    private void showRequestDialog() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this),
                "Submit Blood Request", true);
        dialog.setSize(440, 380);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(new EmptyBorder(20, 24, 20, 24));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(6, 5, 6, 5);

        JTextField patientField = UIHelper.textField(20);
        JComboBox<String> bloodGroupCb = UIHelper.comboBox(UIConstants.BLOOD_GROUPS);
        JTextField unitsField = UIHelper.textField(10);
        JComboBox<String> urgencyCb = UIHelper.comboBox(UIConstants.URGENCY_LEVELS);
        JTextField contactField = UIHelper.textField(20);
        JTextArea notesArea = new JTextArea(2, 20);
        notesArea.setFont(UIConstants.FONT_BODY);

        String[] labels = {"Patient Name *", "Blood Group *", "Units *", "Urgency", "Contact", "Notes"};
        Component[] inputs = {patientField, bloodGroupCb, unitsField, urgencyCb, contactField, new JScrollPane(notesArea)};

        for (int i = 0; i < labels.length; i++) {
            gbc.gridx = 0; gbc.gridy = i; gbc.weightx = 0.35;
            panel.add(new JLabel(labels[i]), gbc);
            gbc.gridx = 1; gbc.weightx = 0.65;
            panel.add(inputs[i], gbc);
        }

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton submitBtn = UIHelper.primaryButton("Submit Request");
        JButton cancelBtn = UIHelper.outlineButton("Cancel");
        cancelBtn.addActionListener(e -> dialog.dispose());
        submitBtn.addActionListener(e -> {
            if (patientField.getText().isBlank() || unitsField.getText().isBlank()) {
                UIHelper.showError(dialog, "Patient name and units are required.");
                return;
            }
            try {
                int units = Integer.parseInt(unitsField.getText().trim());
                String body = String.format(
                    "{\"patientName\":\"%s\",\"bloodGroup\":\"%s\",\"units\":%d," +
                    "\"urgencyLevel\":\"%s\",\"contactNumber\":\"%s\",\"notes\":\"%s\"}",
                    escape(patientField.getText().trim()),
                    bloodGroupCb.getSelectedItem(),
                    units,
                    urgencyCb.getSelectedItem(),
                    escape(contactField.getText().trim()),
                    escape(notesArea.getText().trim()));
                ApiClient.postRaw("/hospital-dashboard/requests", body);
                UIHelper.showSuccess(dialog, "Blood request submitted successfully.");
                dialog.dispose();
                refresh();
            } catch (NumberFormatException ex) {
                UIHelper.showError(dialog, "Units must be a valid number.");
            } catch (IOException ex) {
                UIHelper.showError(dialog, "Error: " + ex.getMessage());
            }
        });
        btnPanel.add(cancelBtn);
        btnPanel.add(submitBtn);
        gbc.gridx = 0; gbc.gridy = labels.length; gbc.gridwidth = 2;
        panel.add(btnPanel, gbc);

        dialog.add(panel);
        dialog.setVisible(true);
    }

    private String escape(String s) { return s == null ? "" : s.replace("\"", "\\\""); }
}
