package com.bloodbank.ui.screens;

import com.bloodbank.ui.api.ApiClient;
import com.bloodbank.ui.model.BloodInventory;
import com.bloodbank.ui.util.SessionManager;
import com.bloodbank.ui.util.UIConstants;
import com.bloodbank.ui.util.UIHelper;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class InventoryPanel extends JPanel {

    private JTable table;
    private DefaultTableModel tableModel;
    private List<BloodInventory> inventoryList = new ArrayList<>();

    public InventoryPanel() {
        setLayout(new BorderLayout());
        setBackground(UIConstants.BG_LIGHT);
        setBorder(new EmptyBorder(25, 25, 25, 25));
        initUI();
    }

    private void initUI() {
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);
        topPanel.setBorder(new EmptyBorder(0, 0, 15, 0));
        topPanel.add(UIHelper.createPageTitle("Blood Inventory"), BorderLayout.WEST);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        btnPanel.setOpaque(false);

        JButton addBtn = UIHelper.createSuccessButton("+ Add Stock");
        addBtn.addActionListener(e -> showInventoryDialog(null));

        JButton removeExpiredBtn = UIHelper.createDangerButton("Remove Expired");
        removeExpiredBtn.addActionListener(e -> removeExpired());

        JButton refreshBtn = UIHelper.createInfoButton("Refresh");
        refreshBtn.addActionListener(e -> refresh());

        btnPanel.add(addBtn);
        btnPanel.add(removeExpiredBtn);
        btnPanel.add(refreshBtn);
        topPanel.add(btnPanel, BorderLayout.EAST);
        add(topPanel, BorderLayout.NORTH);

        String[] columns = {"ID", "Batch Code", "Blood Group", "Quantity (Units)", "Expiry Date", "Added Date", "Source", "Status", "Actions"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return col == 8; }
        };

        table = new JTable(tableModel) {
            @Override
            public Component prepareRenderer(javax.swing.table.TableCellRenderer renderer, int row, int col) {
                Component c = super.prepareRenderer(renderer, row, col);
                String status = (String) getModel().getValueAt(row, 7);
                if (!isRowSelected(row)) {
                    if ("Expired".equals(status)) c.setBackground(new Color(0xFFEBEE));
                    else if ("Expiring Soon".equals(status)) c.setBackground(new Color(0xFFF8E1));
                    else c.setBackground(Color.WHITE);
                }
                return c;
            }
        };
        UIHelper.styleTable(table);
        table.getColumnModel().getColumn(0).setMaxWidth(50);
        table.getColumnModel().getColumn(1).setPreferredWidth(160);
        table.getColumnModel().getColumn(8).setMinWidth(160);
        table.getColumnModel().getColumn(8).setCellRenderer(new InventoryActionRenderer());
        table.getColumnModel().getColumn(8).setCellEditor(new InventoryActionEditor(table, this));

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(0xE0E0E0)));
        add(scrollPane, BorderLayout.CENTER);

        refresh();
    }

    public void refresh() {
        SwingWorker<List<BloodInventory>, Void> worker = new SwingWorker<List<BloodInventory>, Void>() {
            @Override
            protected List<BloodInventory> doInBackground() throws Exception {
                String json = ApiClient.get("/inventory");
                return ApiClient.listFromJson(json, BloodInventory.class);
            }
            @Override
            protected void done() {
                try {
                    inventoryList = get();
                    populateTable(inventoryList);
                } catch (Exception e) {
                    UIHelper.showError(InventoryPanel.this, "Failed to load inventory: " + e.getMessage());
                }
            }
        };
        worker.execute();
    }

    private void populateTable(List<BloodInventory> list) {
        tableModel.setRowCount(0);
        for (BloodInventory inv : list) {
            String status = inv.isExpired() ? "Expired" : inv.isExpiringSoon() ? "Expiring Soon" : "OK";
            tableModel.addRow(new Object[]{
                inv.id,
                inv.batchCode != null ? inv.batchCode : "—",
                inv.bloodGroup, inv.quantity,
                inv.expiryDate != null ? inv.expiryDate.toString() : "",
                inv.addedDate  != null ? inv.addedDate.toString()  : "",
                inv.source != null ? inv.source : "",
                status, "actions"
            });
        }
    }

    public void showInventoryDialog(BloodInventory inv) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this),
                inv == null ? "Add Blood Stock" : "Edit Blood Stock", true);
        dialog.setSize(420, 320);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(new EmptyBorder(20, 25, 20, 25));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(6, 5, 6, 5);

        JComboBox<String> bgBox = new JComboBox<>(UIConstants.BLOOD_GROUPS);
        if (inv != null) bgBox.setSelectedItem(inv.bloodGroup);

        JTextField qtyField = new JTextField(inv != null ? String.valueOf(inv.quantity) : "1");
        JTextField expiryField = new JTextField(inv != null && inv.expiryDate != null ?
                inv.expiryDate.toString() : LocalDate.now().plusDays(42).toString());
        expiryField.setToolTipText("Format: YYYY-MM-DD");

        JComboBox<String> sourceBox = new JComboBox<>(new String[]{"donation", "purchase", "transfer"});
        if (inv != null && inv.source != null) sourceBox.setSelectedItem(inv.source);

        addFormRow(form, gbc, 0, "Blood Group *", bgBox);
        addFormRow(form, gbc, 1, "Quantity (Units) *", qtyField);
        addFormRow(form, gbc, 2, "Expiry Date *", expiryField);
        addFormRow(form, gbc, 3, "Source", sourceBox);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton cancelBtn = UIHelper.createDangerButton("Cancel");
        JButton saveBtn = UIHelper.createSuccessButton("Save");

        cancelBtn.addActionListener(e -> dialog.dispose());
        saveBtn.addActionListener(e -> {
            try {
                BloodInventory item = inv != null ? inv : new BloodInventory();
                item.bloodGroup = (String) bgBox.getSelectedItem();
                item.quantity = Integer.parseInt(qtyField.getText().trim());
                item.expiryDate = LocalDate.parse(expiryField.getText().trim());
                item.source = (String) sourceBox.getSelectedItem();

                SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
                    @Override
                    protected Void doInBackground() throws Exception {
                        if (inv == null) ApiClient.post("/inventory", item);
                        else ApiClient.put("/inventory/" + inv.id, item);
                        return null;
                    }
                    @Override
                    protected void done() {
                        try { get(); dialog.dispose(); refresh(); UIHelper.showSuccess(InventoryPanel.this, "Saved successfully"); }
                        catch (Exception ex) { UIHelper.showError(dialog, ex.getMessage()); }
                    }
                };
                worker.execute();
            } catch (Exception ex) {
                UIHelper.showError(dialog, "Invalid input: " + ex.getMessage());
            }
        });

        btnPanel.add(cancelBtn); btnPanel.add(saveBtn);
        dialog.add(form, BorderLayout.CENTER);
        dialog.add(btnPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    public void deleteInventory(int row) {
        if (!SessionManager.isAdmin()) { UIHelper.showError(this, "Only admins can delete inventory"); return; }
        Long id = (Long) tableModel.getValueAt(row, 0);        if (UIHelper.showConfirm(this, "Delete this inventory record?")) {
            SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
                @Override protected Void doInBackground() throws Exception { ApiClient.delete("/inventory/delete/" + id); return null; }
                @Override protected void done() { try { get(); refresh(); } catch (Exception e) { UIHelper.showError(InventoryPanel.this, e.getMessage()); } }
            };
            worker.execute();
        }
    }

    public BloodInventory getInventoryAtRow(int row) {
        if (inventoryList == null || row < 0 || row >= tableModel.getRowCount()) return null;
        Long id = (Long) tableModel.getValueAt(row, 0);
        return inventoryList.stream().filter(i -> i.id != null && i.id.equals(id)).findFirst().orElse(null);
    }

    private void removeExpired() {
        if (UIHelper.showConfirm(this, "Remove all expired blood from inventory?")) {
            SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
                @Override protected Void doInBackground() throws Exception { ApiClient.delete("/inventory/remove-expired"); return null; }
                @Override protected void done() { try { get(); refresh(); UIHelper.showSuccess(InventoryPanel.this, "Expired blood removed"); } catch (Exception e) { UIHelper.showError(InventoryPanel.this, e.getMessage()); } }
            };
            worker.execute();
        }
    }

    private void addFormRow(JPanel panel, GridBagConstraints gbc, int row, String label, JComponent field) {
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.4;
        panel.add(new JLabel(label + ":"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.6;
        panel.add(field, gbc);
    }

    static class InventoryActionRenderer extends JPanel implements javax.swing.table.TableCellRenderer {
        InventoryActionRenderer() { setLayout(new FlowLayout(FlowLayout.CENTER, 3, 3)); setOpaque(true); }
        @Override
        public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int r, int c) {
            removeAll();
            setBackground(sel ? t.getSelectionBackground() : t.getBackground());
            JButton edit = new JButton("Edit"); edit.setFont(UIConstants.FONT_SMALL); edit.setBackground(UIConstants.INFO); edit.setForeground(Color.WHITE); edit.setBorderPainted(false);
            JButton del = new JButton("Delete"); del.setFont(UIConstants.FONT_SMALL); del.setBackground(UIConstants.DANGER); del.setForeground(Color.WHITE); del.setBorderPainted(false);
            add(edit); add(del);
            return this;
        }
    }

    static class InventoryActionEditor extends DefaultCellEditor {
        private final JTable table;
        private final InventoryPanel panel;
        private int currentRow;

        InventoryActionEditor(JTable table, InventoryPanel panel) {
            super(new JCheckBox());
            this.table = table;
            this.panel = panel;
        }

        @Override
        public Component getTableCellEditorComponent(JTable t, Object v, boolean sel, int row, int col) {
            currentRow = row;
            JPanel cellPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 3, 3));
            cellPanel.setBackground(t.getSelectionBackground());
            JButton edit = new JButton("Edit"); edit.setFont(UIConstants.FONT_SMALL); edit.setBackground(UIConstants.INFO); edit.setForeground(Color.WHITE); edit.setBorderPainted(false);
            edit.addActionListener(e -> { fireEditingStopped(); BloodInventory inv = panel.getInventoryAtRow(currentRow); if (inv != null) panel.showInventoryDialog(inv); });
            JButton del = new JButton("Delete"); del.setFont(UIConstants.FONT_SMALL); del.setBackground(UIConstants.DANGER); del.setForeground(Color.WHITE); del.setBorderPainted(false);
            del.addActionListener(e -> { fireEditingStopped(); panel.deleteInventory(currentRow); });
            cellPanel.add(edit); cellPanel.add(del);
            return cellPanel;
        }

        @Override
        public Object getCellEditorValue() { return ""; }
    }
}
