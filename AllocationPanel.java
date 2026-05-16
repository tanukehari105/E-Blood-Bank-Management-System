package com.bloodbank.ui.screens;

import com.bloodbank.ui.api.ApiClient;
import com.bloodbank.ui.model.BloodAllocation;
import com.bloodbank.ui.util.UIConstants;
import com.bloodbank.ui.util.UIHelper;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.util.List;

/**
 * Blood Allocation viewer — shows FIFO allocation audit trail.
 */
public class AllocationPanel extends JPanel {

    private JTable table;
    private DefaultTableModel tableModel;
    private JTextField searchField;
    private List<BloodAllocation> allocations;

    public AllocationPanel() {
        setLayout(new java.awt.BorderLayout(0, 0));
        setBackground(UIConstants.BG_LIGHT);
        setBorder(new EmptyBorder(20, 24, 20, 24));
        initUI();
    }

    private void initUI() {
        JPanel header = new JPanel(new java.awt.BorderLayout());
        header.setBackground(UIConstants.BG_LIGHT);
        header.setBorder(new EmptyBorder(0, 0, 16, 0));
        header.add(UIHelper.titleLabel("🔗 Blood Allocations (FIFO Audit)"), java.awt.BorderLayout.WEST);

        JPanel actions = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, 8, 0));
        actions.setBackground(UIConstants.BG_LIGHT);
        searchField = UIHelper.searchField("Filter by blood group or request ID...");
        searchField.setPreferredSize(new java.awt.Dimension(280, 34));
        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { filterTable(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { filterTable(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { filterTable(); }
        });
        JButton refreshBtn = UIHelper.outlineButton("↻ Refresh");
        refreshBtn.addActionListener(e -> refresh());
        actions.add(searchField);
        actions.add(refreshBtn);
        header.add(actions, java.awt.BorderLayout.EAST);
        add(header, java.awt.BorderLayout.NORTH);

        String[] cols = {"ID", "Request ID", "Blood Group", "Units Allocated", "Allocation Date", "Batch Expiry", "Donor"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(tableModel);
        UIHelper.styleTable(table);
        table.getColumnModel().getColumn(0).setMaxWidth(55);
        table.getColumnModel().getColumn(1).setMaxWidth(90);
        table.getColumnModel().getColumn(3).setMaxWidth(120);

        add(UIHelper.scrollPane(table), java.awt.BorderLayout.CENTER);
        refresh();
    }

    public void refresh() {
        SwingWorker<List<BloodAllocation>, Void> worker = new SwingWorker<>() {
            @Override protected List<BloodAllocation> doInBackground() throws Exception {
                return ApiClient.listFromJson(ApiClient.get("/inventory/allocations"), BloodAllocation.class);
            }
            @Override protected void done() {
                try {
                    allocations = get();
                    populateTable(allocations);
                } catch (Exception e) {
                    UIHelper.showError(AllocationPanel.this, "Failed to load allocations: " + e.getMessage());
                }
            }
        };
        worker.execute();
    }

    private void populateTable(List<BloodAllocation> list) {
        tableModel.setRowCount(0);
        for (BloodAllocation a : list) {
            tableModel.addRow(new Object[]{
                a.id, a.requestId, a.bloodGroup, a.unitsAllocated,
                a.allocationDate != null ? a.allocationDate : "",
                a.batchExpiryDate != null ? a.batchExpiryDate : "",
                a.donorName != null ? a.donorName : "N/A"
            });
        }
    }

    private void filterTable() {
        String query = searchField.getText().trim().toLowerCase();
        if (query.isBlank() || allocations == null) { populateTable(allocations != null ? allocations : List.of()); return; }
        List<BloodAllocation> filtered = allocations.stream()
                .filter(a -> (a.bloodGroup != null && a.bloodGroup.toLowerCase().contains(query))
                        || (a.requestId != null && a.requestId.toString().contains(query)))
                .toList();
        populateTable(filtered);
    }
}
