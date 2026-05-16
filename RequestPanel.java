package com.bloodbank.ui.screens;

import com.bloodbank.ui.api.ApiClient;
import com.bloodbank.ui.model.BloodRequest;
import com.bloodbank.ui.model.Donor;
import com.bloodbank.ui.util.UIConstants;
import com.bloodbank.ui.util.UIHelper;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RequestPanel extends JPanel {

    private JTable table;
    private DefaultTableModel tableModel;
    private List<BloodRequest> requests = new ArrayList<>();

    public RequestPanel() {
        setLayout(new BorderLayout());
        setBackground(UIConstants.BG_LIGHT);
        setBorder(new EmptyBorder(25, 25, 25, 25));
        initUI();
    }

    private void initUI() {
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);
        topPanel.setBorder(new EmptyBorder(0, 0, 15, 0));
        topPanel.add(UIHelper.createPageTitle("Blood Requests"), BorderLayout.WEST);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        btnPanel.setOpaque(false);

        JButton newReqBtn = UIHelper.createSuccessButton("+ New Request");
        newReqBtn.addActionListener(e -> showRequestDialog());

        JButton refreshBtn = UIHelper.createInfoButton("Refresh");
        refreshBtn.addActionListener(e -> refresh());

        btnPanel.add(newReqBtn);
        btnPanel.add(refreshBtn);
        topPanel.add(btnPanel, BorderLayout.EAST);
        add(topPanel, BorderLayout.NORTH);

        String[] columns = {"ID", "Patient", "Hospital", "Blood Group", "Units", "Status", "Request Date", "Actions"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return c == 7; }
        };

        table = new JTable(tableModel) {
            @Override
            public Component prepareRenderer(javax.swing.table.TableCellRenderer renderer, int row, int col) {
                Component c = super.prepareRenderer(renderer, row, col);
                if (!isRowSelected(row)) {
                    String status = (String) getModel().getValueAt(row, 5);
                    if ("APPROVED".equals(status)) c.setBackground(new Color(0xE8F5E9));
                    else if ("REJECTED".equals(status)) c.setBackground(new Color(0xFFEBEE));
                    else c.setBackground(Color.WHITE);
                }
                return c;
            }
        };
        UIHelper.styleTable(table);
        table.getColumnModel().getColumn(0).setMaxWidth(50);
        table.getColumnModel().getColumn(7).setMinWidth(220);
        table.getColumnModel().getColumn(7).setCellRenderer(new RequestActionRenderer());
        table.getColumnModel().getColumn(7).setCellEditor(new RequestActionEditor(table, this));

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(0xE0E0E0)));
        add(scrollPane, BorderLayout.CENTER);

        refresh();
    }

    public void refresh() {
        SwingWorker<List<BloodRequest>, Void> worker = new SwingWorker<List<BloodRequest>, Void>() {
            @Override
            protected List<BloodRequest> doInBackground() throws Exception {
                String json = ApiClient.get("/requests");
                return ApiClient.listFromJson(json, BloodRequest.class);
            }
            @Override
            protected void done() {
                try {
                    requests = get();
                    tableModel.setRowCount(0);
                    for (BloodRequest r : requests) {
                        tableModel.addRow(new Object[]{
                            r.id, r.patientName, r.hospital, r.bloodGroup, r.units,
                            r.status, r.requestDate != null ? r.requestDate.toString() : "", "actions"
                        });
                    }
                } catch (Exception e) {
                    UIHelper.showError(RequestPanel.this, "Failed to load requests: " + e.getMessage());
                }
            }
        };
        worker.execute();
    }

    private void showRequestDialog() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "New Blood Request", true);
        dialog.setSize(460, 380);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(new EmptyBorder(20, 25, 20, 25));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(6, 5, 6, 5);

        JTextField patientField = new JTextField();
        JTextField hospitalField = new JTextField();
        JComboBox<String> bgBox = new JComboBox<>(UIConstants.BLOOD_GROUPS);
        JTextField unitsField = new JTextField("1");
        JTextField contactField = new JTextField();
        JTextField notesField = new JTextField();

        addFormRow(form, gbc, 0, "Patient Name *", patientField);
        addFormRow(form, gbc, 1, "Hospital *", hospitalField);
        addFormRow(form, gbc, 2, "Blood Group *", bgBox);
        addFormRow(form, gbc, 3, "Units Required *", unitsField);
        addFormRow(form, gbc, 4, "Contact Number", contactField);
        addFormRow(form, gbc, 5, "Notes", notesField);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton cancelBtn = UIHelper.createDangerButton("Cancel");
        JButton saveBtn = UIHelper.createSuccessButton("Submit");

        cancelBtn.addActionListener(e -> dialog.dispose());
        saveBtn.addActionListener(e -> {
            if (patientField.getText().trim().isEmpty() || hospitalField.getText().trim().isEmpty()) {
                UIHelper.showError(dialog, "Patient name and hospital are required");
                return;
            }
            try {
                BloodRequest req = new BloodRequest();
                req.patientName = patientField.getText().trim();
                req.hospital = hospitalField.getText().trim();
                req.bloodGroup = (String) bgBox.getSelectedItem();
                req.units = Integer.parseInt(unitsField.getText().trim());
                req.contactNumber = contactField.getText().trim();
                req.notes = notesField.getText().trim();

                SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
                    @Override protected Void doInBackground() throws Exception { ApiClient.post("/requests", req); return null; }
                    @Override protected void done() {
                        try { get(); dialog.dispose(); refresh(); UIHelper.showSuccess(RequestPanel.this, "Request submitted successfully"); }
                        catch (Exception ex) { UIHelper.showError(dialog, ex.getMessage()); }
                    }
                };
                worker.execute();
            } catch (NumberFormatException ex) {
                UIHelper.showError(dialog, "Invalid units value");
            }
        });

        btnPanel.add(cancelBtn); btnPanel.add(saveBtn);
        dialog.add(form, BorderLayout.CENTER);
        dialog.add(btnPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    public void approveRequest(int row) {
        Long id = (Long) tableModel.getValueAt(row, 0);
        String bloodGroup = (String) tableModel.getValueAt(row, 3);
        if (!UIHelper.showConfirm(this, "Approve this blood request?")) return;

        SwingWorker<String, Void> worker = new SwingWorker<String, Void>() {
            @Override
            protected String doInBackground() throws Exception {
                return ApiClient.put("/requests/" + id + "/approve", null);
            }
            @Override
            protected void done() {
                try {
                    get();
                    refresh();
                    UIHelper.showSuccess(RequestPanel.this, "Request approved. Inventory updated.");
                } catch (Exception e) {
                    String msg = e.getMessage();
                    if (msg != null && msg.contains("Insufficient")) {
                        int choice = JOptionPane.showConfirmDialog(RequestPanel.this,
                                msg + "\n\nFind eligible donors for " + bloodGroup + "?",
                                "Insufficient Stock", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                        if (choice == JOptionPane.YES_OPTION) showMatchingDonors(bloodGroup);
                    } else {
                        UIHelper.showError(RequestPanel.this, "Failed: " + msg);
                    }
                }
            }
        };
        worker.execute();
    }

    public void rejectRequest(int row) {
        Long id = (Long) tableModel.getValueAt(row, 0);
        String reason = JOptionPane.showInputDialog(this, "Reason for rejection (optional):");
        if (reason == null) return;

        Map<String, String> payload = new HashMap<>();
        payload.put("reason", reason);

        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            @Override protected Void doInBackground() throws Exception { ApiClient.put("/requests/" + id + "/reject", payload); return null; }
            @Override protected void done() { try { get(); refresh(); } catch (Exception e) { UIHelper.showError(RequestPanel.this, e.getMessage()); } }
        };
        worker.execute();
    }

    private void showMatchingDonors(String bloodGroup) {
        SwingWorker<List<Donor>, Void> worker = new SwingWorker<List<Donor>, Void>() {
            @Override
            protected List<Donor> doInBackground() throws Exception {
                String json = ApiClient.get("/requests/match-donors/" + java.net.URLEncoder.encode(bloodGroup, "UTF-8"));
                return ApiClient.listFromJson(json, Donor.class);
            }
            @Override
            protected void done() {
                try {
                    List<Donor> donors = get();
                    if (donors.isEmpty()) { UIHelper.showError(RequestPanel.this, "No eligible donors found for " + bloodGroup); return; }
                    StringBuilder sb = new StringBuilder("Eligible donors for " + bloodGroup + ":\n\n");
                    for (Donor d : donors) {
                        sb.append("• ").append(d.name).append(" | ").append(d.contact)
                          .append(" | Last donated: ").append(d.lastDonationDate != null ? d.lastDonationDate : "Never").append("\n");
                    }
                    JTextArea area = new JTextArea(sb.toString());
                    area.setEditable(false);
                    area.setFont(UIConstants.FONT_BODY);
                    JScrollPane sp = new JScrollPane(area);
                    sp.setPreferredSize(new Dimension(500, 300));
                    JOptionPane.showMessageDialog(RequestPanel.this, sp, "Smart Donor Matching", JOptionPane.INFORMATION_MESSAGE);
                } catch (Exception e) {
                    UIHelper.showError(RequestPanel.this, e.getMessage());
                }
            }
        };
        worker.execute();
    }

    public BloodRequest getRequestAtRow(int row) {
        if (requests == null || row < 0 || row >= tableModel.getRowCount()) return null;
        Long id = (Long) tableModel.getValueAt(row, 0);
        return requests.stream().filter(r -> r.id != null && r.id.equals(id)).findFirst().orElse(null);
    }

    private void addFormRow(JPanel panel, GridBagConstraints gbc, int row, String label, JComponent field) {
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.35;
        panel.add(new JLabel(label + ":"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.65;
        panel.add(field, gbc);
    }

    static class RequestActionRenderer extends JPanel implements javax.swing.table.TableCellRenderer {
        RequestActionRenderer() { setLayout(new FlowLayout(FlowLayout.CENTER, 2, 3)); setOpaque(true); }
        @Override
        public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int r, int c) {
            removeAll();
            setBackground(sel ? t.getSelectionBackground() : t.getBackground());
            String status = (String) t.getModel().getValueAt(r, 5);
            if ("PENDING".equals(status)) {
                JButton approve = new JButton("Approve"); approve.setFont(UIConstants.FONT_SMALL); approve.setBackground(UIConstants.ACCENT); approve.setForeground(Color.WHITE); approve.setBorderPainted(false);
                JButton reject = new JButton("Reject"); reject.setFont(UIConstants.FONT_SMALL); reject.setBackground(UIConstants.DANGER); reject.setForeground(Color.WHITE); reject.setBorderPainted(false);
                add(approve); add(reject);
            } else {
                JLabel lbl = new JLabel(status); lbl.setFont(UIConstants.FONT_SMALL);
                lbl.setForeground("APPROVED".equals(status) ? UIConstants.ACCENT : UIConstants.DANGER);
                add(lbl);
            }
            return this;
        }
    }

    static class RequestActionEditor extends DefaultCellEditor {
        private final JTable table;
        private final RequestPanel panel;
        private int currentRow;

        RequestActionEditor(JTable table, RequestPanel panel) {
            super(new JCheckBox());
            this.table = table;
            this.panel = panel;
        }

        @Override
        public Component getTableCellEditorComponent(JTable t, Object v, boolean sel, int row, int col) {
            currentRow = row;
            JPanel cellPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 2, 3));
            cellPanel.setBackground(t.getSelectionBackground());
            String status = (String) t.getModel().getValueAt(row, 5);
            if ("PENDING".equals(status)) {
                JButton approve = new JButton("Approve"); approve.setFont(UIConstants.FONT_SMALL); approve.setBackground(UIConstants.ACCENT); approve.setForeground(Color.WHITE); approve.setBorderPainted(false);
                approve.addActionListener(e -> { fireEditingStopped(); panel.approveRequest(currentRow); });
                JButton reject = new JButton("Reject"); reject.setFont(UIConstants.FONT_SMALL); reject.setBackground(UIConstants.DANGER); reject.setForeground(Color.WHITE); reject.setBorderPainted(false);
                reject.addActionListener(e -> { fireEditingStopped(); panel.rejectRequest(currentRow); });
                cellPanel.add(approve); cellPanel.add(reject);
            } else {
                JLabel lbl = new JLabel(status); lbl.setFont(UIConstants.FONT_SMALL);
                lbl.setForeground("APPROVED".equals(status) ? UIConstants.ACCENT : UIConstants.DANGER);
                cellPanel.add(lbl);
            }
            return cellPanel;
        }

        @Override
        public Object getCellEditorValue() { return ""; }
    }
}
