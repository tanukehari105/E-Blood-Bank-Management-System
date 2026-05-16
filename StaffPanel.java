package com.bloodbank.ui.screens;

import com.bloodbank.ui.api.ApiClient;
import com.bloodbank.ui.model.User;
import com.bloodbank.ui.util.UIConstants;
import com.bloodbank.ui.util.UIHelper;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StaffPanel extends JPanel {

    private JTable staffTable;
    private DefaultTableModel tableModel;
    private JLabel totalStaffLabel;
    private JLabel totalUsersLabel;

    public StaffPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(20, 20, 20, 20));
        initUI();
        loadStaffData();
    }

    private void initUI() {
        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Color.WHITE);

        JLabel titleLabel = new JLabel("Staff Management");
        titleLabel.setFont(UIConstants.FONT_TITLE);
        headerPanel.add(titleLabel, BorderLayout.WEST);

        JButton addButton = UIHelper.createPrimaryButton("+ Add Staff");
        addButton.addActionListener(e -> showAddStaffDialog());
        headerPanel.add(addButton, BorderLayout.EAST);

        add(headerPanel, BorderLayout.NORTH);

        // Stats Panel
        JPanel statsPanel = new JPanel(new GridLayout(1, 2, 15, 0));
        statsPanel.setBackground(Color.WHITE);
        statsPanel.setBorder(new EmptyBorder(15, 0, 15, 0));

        totalStaffLabel = new JLabel("Total Staff: 0");
        totalStaffLabel.setFont(UIConstants.FONT_BOLD);
        totalStaffLabel.setForeground(UIConstants.PRIMARY);

        totalUsersLabel = new JLabel("Total Users: 0");
        totalUsersLabel.setFont(UIConstants.FONT_BOLD);
        totalUsersLabel.setForeground(UIConstants.SUCCESS);

        statsPanel.add(totalStaffLabel);
        statsPanel.add(totalUsersLabel);

        // Table
        String[] columns = {"ID", "Username", "Full Name", "Role", "Actions"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 4; // Only Actions column
            }
        };

        staffTable = new JTable(tableModel);
        staffTable.setFont(UIConstants.FONT_BODY);
        staffTable.setRowHeight(40);
        staffTable.getTableHeader().setFont(UIConstants.FONT_BOLD);
        staffTable.getTableHeader().setBackground(UIConstants.SIDEBAR_BG);
        staffTable.getTableHeader().setForeground(Color.WHITE);

        // Add delete button renderer and editor
        staffTable.getColumn("Actions").setCellRenderer((table, value, isSelected, hasFocus, row, column) -> {
            JButton deleteBtn = new JButton("Delete");
            deleteBtn.setBackground(UIConstants.DANGER);
            deleteBtn.setForeground(Color.WHITE);
            deleteBtn.setFocusPainted(false);
            deleteBtn.setBorderPainted(false);
            return deleteBtn;
        });

        staffTable.getColumn("Actions").setCellEditor(new DefaultCellEditor(new JCheckBox()) {
            private final JButton button;
            private String label;
            private boolean clicked;
            private int row;

            {
                button = new JButton();
                button.setBackground(UIConstants.DANGER);
                button.setForeground(Color.WHITE);
                button.setFocusPainted(false);
                button.setBorderPainted(false);
                button.addActionListener(e -> {
                    fireEditingStopped();
                    clicked = true;
                });
            }

            @Override
            public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
                this.row = row;
                label = "Delete";
                button.setText(label);
                clicked = false;
                return button;
            }

            @Override
            public Object getCellEditorValue() {
                if (clicked) {
                    Long userId = (Long) tableModel.getValueAt(row, 0);
                    String username = (String) tableModel.getValueAt(row, 1);
                    deleteStaff(userId, username);
                }
                return label;
            }
        });

        JScrollPane scrollPane = new JScrollPane(staffTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));

        // Center panel with stats and table
        JPanel centerPanel = new JPanel(new BorderLayout(0, 10));
        centerPanel.setBackground(Color.WHITE);
        centerPanel.add(statsPanel, BorderLayout.NORTH);
        centerPanel.add(scrollPane, BorderLayout.CENTER);

        add(centerPanel, BorderLayout.CENTER);
    }

    private void loadStaffData() {
        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            List<User> users;
            Map<String, Long> counts;

            @Override
            protected Void doInBackground() {
                try {
                    String response = ApiClient.get("/users");
                    users = ApiClient.listFromJson(response, User.class);

                    String countsResponse = ApiClient.get("/users/count");
                    counts = ApiClient.fromJson(countsResponse, Map.class);
                } catch (Exception e) {
                    // Error loading staff data
                    System.err.println("Error loading staff data: " + e.getMessage());
                }
                return null;
            }

            @Override
            protected void done() {
                if (users != null) {
                    tableModel.setRowCount(0);
                    for (User user : users) {
                        tableModel.addRow(new Object[]{
                                user.getId(),
                                user.getUsername(),
                                user.getFullName(),
                                user.getRole(),
                                "Delete"
                        });
                    }
                }

                if (counts != null) {
                    Object staffCount = counts.get("staff");
                    Object totalCount = counts.get("total");
                    totalStaffLabel.setText("Total Staff: " + (staffCount != null ? staffCount.toString() : "0"));
                    totalUsersLabel.setText("Total Users: " + (totalCount != null ? totalCount.toString() : "0"));
                }
            }
        };
        worker.execute();
    }

    private void showAddStaffDialog() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Add New Staff", true);
        dialog.setSize(400, 350);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        JTextField usernameField = new JTextField(20);
        JPasswordField passwordField = new JPasswordField(20);
        JTextField fullNameField = new JTextField(20);
        JComboBox<String> roleCombo = new JComboBox<>(new String[]{"STAFF", "ADMIN"});

        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("Username:"), gbc);
        gbc.gridx = 1;
        panel.add(usernameField, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(new JLabel("Password:"), gbc);
        gbc.gridx = 1;
        panel.add(passwordField, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        panel.add(new JLabel("Full Name:"), gbc);
        gbc.gridx = 1;
        panel.add(fullNameField, gbc);

        gbc.gridx = 0; gbc.gridy = 3;
        panel.add(new JLabel("Role:"), gbc);
        gbc.gridx = 1;
        panel.add(roleCombo, gbc);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton saveButton = UIHelper.createPrimaryButton("Create");
        JButton cancelButton = UIHelper.createSecondaryButton("Cancel");

        saveButton.addActionListener(e -> {
            String username = usernameField.getText().trim();
            String password = new String(passwordField.getPassword());
            String fullName = fullNameField.getText().trim();
            String role = (String) roleCombo.getSelectedItem();

            if (username.isEmpty() || password.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Username and password are required!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            createStaff(username, password, fullName, role, dialog);
        });

        cancelButton.addActionListener(e -> dialog.dispose());

        buttonPanel.add(cancelButton);
        buttonPanel.add(saveButton);

        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 2;
        panel.add(buttonPanel, gbc);

        dialog.add(panel);
        dialog.setVisible(true);
    }

    private void createStaff(String username, String password, String fullName, String role, JDialog dialog) {
        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            String errorMsg;

            @Override
            protected Void doInBackground() {
                try {
                    Map<String, String> payload = new HashMap<>();
                    payload.put("username", username);
                    payload.put("password", password);
                    payload.put("fullName", fullName.isEmpty() ? username : fullName);
                    payload.put("role", role);

                    ApiClient.post("/users", payload);
                } catch (Exception e) {
                    errorMsg = e.getMessage();
                }
                return null;
            }

            @Override
            protected void done() {
                if (errorMsg == null) {
                    JOptionPane.showMessageDialog(dialog, "Staff created successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                    dialog.dispose();
                    loadStaffData();
                } else {
                    JOptionPane.showMessageDialog(dialog, "Error: " + errorMsg, "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    private void deleteStaff(Long userId, String username) {
        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to delete user: " + username + "?",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            String errorMsg;

            @Override
            protected Void doInBackground() {
                try {
                    ApiClient.delete("/users/" + userId);
                } catch (Exception e) {
                    errorMsg = e.getMessage();
                }
                return null;
            }

            @Override
            protected void done() {
                if (errorMsg == null) {
                    JOptionPane.showMessageDialog(StaffPanel.this, "Staff deleted successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                    loadStaffData();
                } else {
                    JOptionPane.showMessageDialog(StaffPanel.this, "Error: " + errorMsg, "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }
}
