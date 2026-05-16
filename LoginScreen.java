package com.bloodbank.ui.screens;

import com.bloodbank.ui.api.ApiClient;
import com.bloodbank.ui.util.SessionManager;
import com.bloodbank.ui.util.UIConstants;
import com.bloodbank.ui.util.UIHelper;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.IOException;

/**
 * Login screen — supports ADMIN, STAFF, and HOSPITAL logins.
 * Routes to appropriate main frame based on role.
 */
public class LoginScreen extends JFrame {

    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private JLabel statusLabel;

    public LoginScreen() {
        setTitle("Blood Bank Management System — Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 580);
        setLocationRelativeTo(null);
        setResizable(false);
        initUI();
    }

    private void initUI() {
        setLayout(new BorderLayout());

        // Left branding panel
        JPanel brandPanel = createBrandPanel();
        add(brandPanel, BorderLayout.WEST);

        // Right login form
        JPanel formPanel = createFormPanel();
        add(formPanel, BorderLayout.CENTER);
    }

    private JPanel createBrandPanel() {
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, UIConstants.PRIMARY_DARK,
                        0, getHeight(), UIConstants.PRIMARY_LIGHT);
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        panel.setPreferredSize(new Dimension(380, 0));
        panel.setLayout(new GridBagLayout());

        JPanel content = new JPanel();
        content.setOpaque(false);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBorder(new EmptyBorder(0, 30, 0, 30));

        JLabel icon = new JLabel("🩸");
        icon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 64));
        icon.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel title = new JLabel("BloodBank");
        title.setFont(new Font("Segoe UI", Font.BOLD, 32));
        title.setForeground(Color.WHITE);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitle = new JLabel("Smart Healthcare Platform");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitle.setForeground(new Color(255, 255, 255, 200));
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        content.add(icon);
        content.add(Box.createVerticalStrut(12));
        content.add(title);
        content.add(Box.createVerticalStrut(6));
        content.add(subtitle);
        content.add(Box.createVerticalStrut(40));

        // Feature bullets
        String[] features = {"🏥 Hospital Management", "💉 Donation Camps", "📊 Analytics Dashboard",
                "🔔 Emergency Alerts", "📄 PDF/CSV Reports", "🔍 QR Code Tracking"};
        for (String f : features) {
            JLabel lbl = new JLabel(f);
            lbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            lbl.setForeground(new Color(255, 255, 255, 180));
            lbl.setAlignmentX(Component.CENTER_ALIGNMENT);
            content.add(lbl);
            content.add(Box.createVerticalStrut(6));
        }

        panel.add(content);
        return panel;
    }

    private JPanel createFormPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);

        JPanel form = new JPanel();
        form.setBackground(Color.WHITE);
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setBorder(new EmptyBorder(0, 50, 0, 50));
        form.setMaximumSize(new Dimension(400, 500));

        JLabel welcome = new JLabel("Welcome Back");
        welcome.setFont(new Font("Segoe UI", Font.BOLD, 26));
        welcome.setForeground(UIConstants.TEXT_PRIMARY);
        welcome.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel sub = new JLabel("Sign in to your account");
        sub.setFont(UIConstants.FONT_BODY);
        sub.setForeground(UIConstants.TEXT_SECONDARY);
        sub.setAlignmentX(Component.LEFT_ALIGNMENT);

        form.add(welcome);
        form.add(Box.createVerticalStrut(4));
        form.add(sub);
        form.add(Box.createVerticalStrut(30));

        // Username
        form.add(fieldLabel("Username"));
        form.add(Box.createVerticalStrut(4));
        usernameField = UIHelper.textField(20);
        usernameField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        usernameField.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(usernameField);
        form.add(Box.createVerticalStrut(16));

        // Password
        form.add(fieldLabel("Password"));
        form.add(Box.createVerticalStrut(4));
        passwordField = UIHelper.passwordField(20);
        passwordField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        passwordField.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(passwordField);
        form.add(Box.createVerticalStrut(24));

        // Login button
        loginButton = UIHelper.primaryButton("Sign In");
        loginButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        loginButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        loginButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        loginButton.addActionListener(e -> performLogin());
        form.add(loginButton);
        form.add(Box.createVerticalStrut(12));

        // Status label
        statusLabel = new JLabel(" ");
        statusLabel.setFont(UIConstants.FONT_SMALL);
        statusLabel.setForeground(UIConstants.DANGER);
        statusLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(statusLabel);
        form.add(Box.createVerticalStrut(20));

        // Hint
        JLabel hint = new JLabel("Roles: ADMIN · STAFF · HOSPITAL");
        hint.setFont(UIConstants.FONT_SMALL);
        hint.setForeground(UIConstants.TEXT_MUTED);
        hint.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(hint);

        // Enter key triggers login
        passwordField.addActionListener(e -> performLogin());
        usernameField.addActionListener(e -> passwordField.requestFocus());

        panel.add(form);
        return panel;
    }

    private JLabel fieldLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(UIConstants.FONT_BOLD);
        lbl.setForeground(UIConstants.TEXT_PRIMARY);
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        return lbl;
    }

    private void performLogin() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (username.isBlank() || password.isBlank()) {
            statusLabel.setText("Please enter username and password.");
            return;
        }

        loginButton.setEnabled(false);
        loginButton.setText("Signing in...");
        statusLabel.setText(" ");

        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            String errorMsg = null;
            String token, role, fullName, hospitalName;
            Long hospitalId;

            @Override
            protected Void doInBackground() {
                try {
                    String body = "{\"username\":\"" + username + "\",\"password\":\"" + password + "\"}";
                    String response = ApiClient.postRaw("/auth/login", body);
                    JsonObject json = JsonParser.parseString(response).getAsJsonObject();
                    token = json.get("token").getAsString();
                    role = json.get("role").getAsString();
                    fullName = json.has("fullName") && !json.get("fullName").isJsonNull()
                            ? json.get("fullName").getAsString() : username;
                    if ("HOSPITAL".equals(role)) {
                        hospitalName = json.has("hospitalName") ? json.get("hospitalName").getAsString() : fullName;
                        hospitalId = json.has("hospitalId") && !json.get("hospitalId").isJsonNull()
                                ? json.get("hospitalId").getAsLong() : null;
                    }
                } catch (IOException e) {
                    errorMsg = "Invalid credentials. Please try again.";
                }
                return null;
            }

            @Override
            protected void done() {
                loginButton.setEnabled(true);
                loginButton.setText("Sign In");
                if (errorMsg != null) {
                    statusLabel.setText(errorMsg);
                    return;
                }
                ApiClient.setToken(token);
                if ("HOSPITAL".equals(role)) {
                    SessionManager.setHospitalSession(token, username, hospitalName, hospitalId);
                    dispose();
                    SwingUtilities.invokeLater(() -> new HospitalMainFrame().setVisible(true));
                } else {
                    SessionManager.setSession(token, username, role, fullName);
                    dispose();
                    SwingUtilities.invokeLater(() -> new MainFrame().setVisible(true));
                }
            }
        };
        worker.execute();
    }
}
