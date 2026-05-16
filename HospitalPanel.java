package com.bloodbank.ui.screens;

import com.bloodbank.ui.api.ApiClient;
import com.bloodbank.ui.model.Hospital;
import com.bloodbank.ui.util.UIConstants;
import com.bloodbank.ui.util.UIHelper;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.IOException;
import java.util.List;

/**
 * Hospital management panel — ADMIN only.
 * Create, edit, delete, activate/deactivate, reset passwords.
 */
public class HospitalPanel extends JPanel {

    private JTable table;
    private DefaultTableModel tableModel;
    private List<Hospital> hospitals;
    private JTextField searchField;

    public HospitalPanel() {
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

        JLabel title = UIHelper.titleLabel("🏥 Hospital Management");
        header.add(title, BorderLayout.WEST);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actions.setBackground(UIConstants.BG_LIGHT);
        searchField = UIHelper.searchField("Search hospitals...");
        searchField.setPreferredSize(new Dimension(220, 34));
        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { filterTable(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { filterTable(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { filterTable(); }
        });

        JButton addBtn = UIHelper.primaryButton("+ Add Hospital");
        addBtn.addActionListener(e -> showHospitalDialog(null));
        JButton refreshBtn = UIHelper.outlineButton("↻ Refresh");
        refreshBtn.addActionListener(e -> refresh());

        actions.add(searchField);
        actions.add(addBtn);
        actions.add(refreshBtn);
        header.add(actions, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        // Table
        String[] cols = {"ID", "Hospital Name", "Email", "Phone", "Username", "Status", "Actions"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return c == 6; }
        };
        table = new JTable(tableModel);
        UIHelper.styleTable(table);
        table.getColumnModel().getColumn(0).setMaxWidth(60);
        table.getColumnModel().getColumn(5).setMaxWidth(90);
        table.getColumnModel().getColumn(6).setMinWidth(260);

        // Action buttons renderer/editor
        table.getColumnModel().getColumn(6).setCellRenderer(new ActionButtonRenderer());
        table.getColumnModel().getColumn(6).setCellEditor(new ActionButtonEditor(table));

        add(UIHelper.scrollPane(table), BorderLayout.CENTER);
        refresh();
    }

    public void refresh() {
        SwingWorker<List<Hospital>, Void> worker = new SwingWorker<>() {
            @Override protected List<Hospital> doInBackground() throws Exception {
                String json = ApiClient.get("/hospitals");
                return ApiClient.listFromJson(json, Hospital.class);
            }
            @Override protected void done() {
                try {
                    hospitals = get();
                    populateTable(hospitals);
                } catch (Exception e) {
                    UIHelper.showError(HospitalPanel.this, "Failed to load hospitals: " + e.getMessage());
                }
            }
        };
        worker.execute();
    }

    private void populateTable(List<Hospital> list) {
        tableModel.setRowCount(0);
        for (Hospital h : list) {
            tableModel.addRow(new Object[]{
                h.id, h.hospitalName, h.email,
                h.phone != null ? h.phone : "",
                h.username,
                h.active ? "✅ Active" : "❌ Inactive",
                "actions"
            });
        }
    }

    private void filterTable() {
        String query = searchField.getText().trim().toLowerCase();
        if (query.isBlank() || query.equals("search hospitals...") || hospitals == null) {
            populateTable(hospitals != null ? hospitals : List.of());
            return;
        }
        List<Hospital> filtered = hospitals.stream()
                .filter(h -> (h.hospitalName != null && h.hospitalName.toLowerCase().contains(query))
                        || (h.email != null && h.email.toLowerCase().contains(query))
                        || (h.username != null && h.username.toLowerCase().contains(query)))
                .toList();
        populateTable(filtered);
    }

    private void showHospitalDialog(Hospital existing) {
        boolean isEdit = existing != null;
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this),
                isEdit ? "Edit Hospital" : "Add Hospital", true);
        dialog.setSize(480, 420);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(new EmptyBorder(20, 24, 20, 24));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        JTextField nameField = UIHelper.textField(20);
        JTextField emailField = UIHelper.textField(20);
        JTextField phoneField = UIHelper.textField(20);
        JTextField addressField = UIHelper.textField(20);
        JTextField usernameField = UIHelper.textField(20);
        JPasswordField passwordField = UIHelper.passwordField(20);

        if (isEdit) {
            nameField.setText(existing.hospitalName);
            emailField.setText(existing.email);
            phoneField.setText(existing.phone != null ? existing.phone : "");
            addressField.setText(existing.address != null ? existing.address : "");
            usernameField.setText(existing.username);
        }

        String[][] fields = {
            {"Hospital Name *", null}, {"Email *", null}, {"Phone", null},
            {"Address", null}, {"Username *", null}, {"Password" + (isEdit ? " (leave blank to keep)" : " *"), null}
        };
        JTextField[] inputs = {nameField, emailField, phoneField, addressField, usernameField, passwordField};

        for (int i = 0; i < fields.length; i++) {
            gbc.gridx = 0; gbc.gridy = i; gbc.weightx = 0.3;
            panel.add(new JLabel(fields[i][0]), gbc);
            gbc.gridx = 1; gbc.weightx = 0.7;
            panel.add(inputs[i], gbc);
        }

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton saveBtn = UIHelper.primaryButton(isEdit ? "Update" : "Create");
        JButton cancelBtn = UIHelper.outlineButton("Cancel");
        cancelBtn.addActionListener(e -> dialog.dispose());
        saveBtn.addActionListener(e -> {
            String name = nameField.getText().trim();
            String email = emailField.getText().trim();
            String username = usernameField.getText().trim();
            String password = new String(passwordField.getPassword());

            if (name.isBlank() || email.isBlank() || username.isBlank() || (!isEdit && password.isBlank())) {
                UIHelper.showError(dialog, "Please fill all required fields (*).");
                return;
            }

            String body = buildHospitalJson(name, email, phoneField.getText().trim(),
                    addressField.getText().trim(), username, password);
            try {
                if (isEdit) {
                    ApiClient.put("/hospitals/" + existing.id, body);
                    UIHelper.showSuccess(dialog, "Hospital updated successfully.");
                } else {
                    ApiClient.postRaw("/hospitals", body);
                    UIHelper.showSuccess(dialog, "Hospital created successfully.");
                }
                dialog.dispose();
                refresh();
            } catch (IOException ex) {
                UIHelper.showError(dialog, "Error: " + ex.getMessage());
            }
        });
        btnPanel.add(cancelBtn);
        btnPanel.add(saveBtn);

        gbc.gridx = 0; gbc.gridy = fields.length; gbc.gridwidth = 2;
        panel.add(btnPanel, gbc);

        dialog.add(panel);
        dialog.setVisible(true);
    }

    private String buildHospitalJson(String name, String email, String phone,
                                      String address, String username, String password) {
        return String.format("{\"hospitalName\":\"%s\",\"email\":\"%s\",\"phone\":\"%s\"," +
                "\"address\":\"%s\",\"username\":\"%s\",\"password\":\"%s\"}",
                escape(name), escape(email), escape(phone),
                escape(address), escape(username), escape(password));
    }

    private String escape(String s) {
        return s == null ? "" : s.replace("\"", "\\\"");
    }

    private Hospital getHospitalAtRow(int row) {
        if (hospitals == null || row < 0 || row >= tableModel.getRowCount()) return null;
        Long id = (Long) tableModel.getValueAt(row, 0);
        return hospitals.stream().filter(h -> h.id.equals(id)).findFirst().orElse(null);
    }

    // ── Action Button Renderer ─────────────────────────────────────────────────

    private class ActionButtonRenderer implements javax.swing.table.TableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable t, Object value,
                boolean sel, boolean focus, int row, int col) {
            return buildActionPanel(row);
        }
    }

    private class ActionButtonEditor extends DefaultCellEditor {
        private JPanel panel;
        private int currentRow;

        ActionButtonEditor(JTable table) {
            super(new JCheckBox());
            setClickCountToStart(1);
        }

        @Override
        public Component getTableCellEditorComponent(JTable t, Object value,
                boolean sel, int row, int col) {
            currentRow = row;
            panel = buildActionPanel(row);
            return panel;
        }

        @Override public Object getCellEditorValue() { return ""; }
    }

    private JPanel buildActionPanel(int row) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 2));
        p.setBackground(row % 2 == 0 ? Color.WHITE : new Color(0xFAFAFA));

        JButton editBtn = UIHelper.infoButton("✏ Edit");
        editBtn.setFont(UIConstants.FONT_SMALL);
        editBtn.addActionListener(e -> {
            Hospital h = getHospitalAtRow(row);
            if (h != null) showHospitalDialog(h);
        });

        JButton toggleBtn = UIHelper.warningButton("⚡ Toggle");
        toggleBtn.setFont(UIConstants.FONT_SMALL);
        toggleBtn.addActionListener(e -> {
            Hospital h = getHospitalAtRow(row);
            if (h != null && UIHelper.confirm(HospitalPanel.this,
                    "Toggle active status for " + h.hospitalName + "?")) {
                try {
                    ApiClient.put("/hospitals/" + h.id + "/toggle-active", null);
                    refresh();
                } catch (IOException ex) {
                    UIHelper.showError(HospitalPanel.this, ex.getMessage());
                }
            }
        });

        JButton resetBtn = UIHelper.outlineButton("🔑 Reset");
        resetBtn.setFont(UIConstants.FONT_SMALL);
        resetBtn.addActionListener(e -> {
            Hospital h = getHospitalAtRow(row);
            if (h != null) {
                String newPwd = UIHelper.prompt(HospitalPanel.this, "Enter new password for " + h.hospitalName + ":");
                if (newPwd != null && newPwd.length() >= 6) {
                    try {
                        ApiClient.put("/hospitals/" + h.id + "/reset-password",
                                "{\"password\":\"" + newPwd + "\"}");
                        UIHelper.showSuccess(HospitalPanel.this, "Password reset successfully.");
                    } catch (IOException ex) {
                        UIHelper.showError(HospitalPanel.this, ex.getMessage());
                    }
                } else if (newPwd != null) {
                    UIHelper.showError(HospitalPanel.this, "Password must be at least 6 characters.");
                }
            }
        });

        JButton deleteBtn = UIHelper.dangerButton("🗑 Delete");
        deleteBtn.setFont(UIConstants.FONT_SMALL);
        deleteBtn.addActionListener(e -> {
            Hospital h = getHospitalAtRow(row);
            if (h != null && UIHelper.confirm(HospitalPanel.this,
                    "Delete hospital '" + h.hospitalName + "'? This cannot be undone.")) {
                try {
                    ApiClient.delete("/hospitals/" + h.id);
                    refresh();
                } catch (IOException ex) {
                    UIHelper.showError(HospitalPanel.this, ex.getMessage());
                }
            }
        });

        p.add(editBtn);
        p.add(toggleBtn);
        p.add(resetBtn);
        p.add(deleteBtn);
        return p;
    }
}
