package com.bloodbank.ui.screens;

import com.bloodbank.ui.api.ApiClient;
import com.bloodbank.ui.model.AuditLog;
import com.bloodbank.ui.util.UIConstants;
import com.bloodbank.ui.util.UIHelper;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.util.List;

/**
 * Audit log viewer — ADMIN only. Shows all system events with color-coded actions.
 */
public class AuditLogPanel extends JPanel {

    private JTable table;
    private DefaultTableModel tableModel;
    private JTextField searchField;
    private List<AuditLog> logs;

    public AuditLogPanel() {
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
        header.add(UIHelper.titleLabel("📋 Audit Logs"), BorderLayout.WEST);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actions.setBackground(UIConstants.BG_LIGHT);
        searchField = UIHelper.searchField("Search by user or action...");
        searchField.setPreferredSize(new Dimension(250, 34));
        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { filterTable(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { filterTable(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { filterTable(); }
        });

        JButton refreshBtn = UIHelper.outlineButton("↻ Refresh");
        refreshBtn.addActionListener(e -> refresh());
        actions.add(searchField);
        actions.add(refreshBtn);
        header.add(actions, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        // Table
        String[] cols = {"ID", "Timestamp", "User", "Role", "Action", "Entity", "Details"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(tableModel);
        UIHelper.styleTable(table);
        table.getColumnModel().getColumn(0).setMaxWidth(55);
        table.getColumnModel().getColumn(1).setPreferredWidth(160);
        table.getColumnModel().getColumn(2).setPreferredWidth(100);
        table.getColumnModel().getColumn(3).setMaxWidth(90);
        table.getColumnModel().getColumn(4).setPreferredWidth(160);
        table.getColumnModel().getColumn(5).setPreferredWidth(100);
        table.getColumnModel().getColumn(6).setPreferredWidth(300);

        // Color-code action column
        table.getColumnModel().getColumn(4).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object value,
                    boolean sel, boolean focus, int row, int col) {
                Component c = super.getTableCellRendererComponent(t, value, sel, focus, row, col);
                if (!sel && value != null) {
                    String action = value.toString();
                    if (action.contains("DELETE") || action.contains("REJECT") || action.contains("CANCEL")) {
                        c.setForeground(UIConstants.DANGER);
                    } else if (action.contains("APPROVE") || action.contains("CREATE")) {
                        c.setForeground(UIConstants.SUCCESS);
                    } else if (action.contains("LOGIN")) {
                        c.setForeground(UIConstants.INFO);
                    } else if (action.contains("UPDATE") || action.contains("RESET")) {
                        c.setForeground(UIConstants.WARNING);
                    } else {
                        c.setForeground(UIConstants.TEXT_PRIMARY);
                    }
                }
                setBorder(new EmptyBorder(0, 8, 0, 8));
                return c;
            }
        });

        add(UIHelper.scrollPane(table), BorderLayout.CENTER);
        refresh();
    }

    public void refresh() {
        SwingWorker<List<AuditLog>, Void> worker = new SwingWorker<>() {
            @Override protected List<AuditLog> doInBackground() throws Exception {
                return ApiClient.listFromJson(ApiClient.get("/audit"), AuditLog.class);
            }
            @Override protected void done() {
                try {
                    logs = get();
                    populateTable(logs);
                } catch (Exception e) {
                    UIHelper.showError(AuditLogPanel.this, "Failed to load audit logs: " + e.getMessage());
                }
            }
        };
        worker.execute();
    }

    private void populateTable(List<AuditLog> list) {
        tableModel.setRowCount(0);
        for (AuditLog log : list) {
            String ts = log.timestamp != null
                    ? log.timestamp.replace("T", " ").substring(0, Math.min(19, log.timestamp.length()))
                    : "";
            tableModel.addRow(new Object[]{
                log.id, ts, log.username, log.role, log.action,
                log.entityType != null ? log.entityType : "",
                log.details != null ? log.details : ""
            });
        }
    }

    private void filterTable() {
        String query = searchField.getText().trim().toLowerCase();
        if (query.isBlank() || query.equals("search by user or action...") || logs == null) {
            populateTable(logs != null ? logs : List.of());
            return;
        }
        List<AuditLog> filtered = logs.stream()
                .filter(l -> (l.username != null && l.username.toLowerCase().contains(query))
                        || (l.action != null && l.action.toLowerCase().contains(query))
                        || (l.details != null && l.details.toLowerCase().contains(query)))
                .toList();
        populateTable(filtered);
    }
}
