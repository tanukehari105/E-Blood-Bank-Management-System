package com.bloodbank.ui.screens;

import com.bloodbank.ui.api.ApiClient;
import com.bloodbank.ui.util.SessionManager;
import com.bloodbank.ui.util.UIConstants;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Main frame for HOSPITAL role — limited to dashboard, blood stock, and requests.
 */
public class HospitalMainFrame extends JFrame {

    private JPanel contentPanel;
    private CardLayout cardLayout;
    private JButton activeButton;

    private HospitalDashboardPanel dashboardPanel;
    private HospitalRequestPanel requestPanel;

    public HospitalMainFrame() {
        setTitle("Blood Bank — Hospital Portal: " + SessionManager.getHospitalName());
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(UIConstants.WINDOW_SIZE);
        setLocationRelativeTo(null);
        setMinimumSize(new Dimension(1100, 700));
        initUI();
    }

    private void initUI() {
        setLayout(new BorderLayout());
        add(createSidebar(), BorderLayout.WEST);

        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        contentPanel.setBackground(UIConstants.BG_LIGHT);

        dashboardPanel = new HospitalDashboardPanel();
        requestPanel = new HospitalRequestPanel();

        contentPanel.add(dashboardPanel, "dashboard");
        contentPanel.add(requestPanel, "requests");

        add(contentPanel, BorderLayout.CENTER);
        cardLayout.show(contentPanel, "dashboard");
    }

    private JPanel createSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setBackground(UIConstants.SIDEBAR_BG);
        sidebar.setPreferredSize(new Dimension(UIConstants.SIDEBAR_WIDTH, 0));
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));

        // Logo
        JPanel logoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 18));
        logoPanel.setBackground(new Color(0x1A5276));
        logoPanel.setMaximumSize(new Dimension(UIConstants.SIDEBAR_WIDTH, 72));
        JLabel logoIcon = new JLabel("🏥");
        logoIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 22));
        JLabel logoText = new JLabel("Hospital");
        logoText.setFont(new Font("Segoe UI", Font.BOLD, 16));
        logoText.setForeground(Color.WHITE);
        logoPanel.add(logoIcon);
        logoPanel.add(logoText);
        sidebar.add(logoPanel);

        // Hospital info
        JPanel infoPanel = new JPanel();
        infoPanel.setBackground(new Color(0x243342));
        infoPanel.setMaximumSize(new Dimension(UIConstants.SIDEBAR_WIDTH, 65));
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setBorder(new EmptyBorder(10, 15, 10, 15));
        JLabel nameLabel = new JLabel(SessionManager.getHospitalName());
        nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        nameLabel.setForeground(Color.WHITE);
        JLabel roleLabel = new JLabel("HOSPITAL");
        roleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        roleLabel.setForeground(new Color(255, 255, 255, 150));
        infoPanel.add(nameLabel);
        infoPanel.add(Box.createVerticalStrut(3));
        infoPanel.add(roleLabel);
        sidebar.add(infoPanel);
        sidebar.add(Box.createVerticalStrut(8));

        // Nav
        JButton dashBtn = createNavButton("📊", "Dashboard", "dashboard");
        JButton reqBtn = createNavButton("📋", "Blood Requests", "requests");
        sidebar.add(dashBtn);
        sidebar.add(reqBtn);
        setActiveButton(dashBtn);

        sidebar.add(Box.createVerticalGlue());

        // Logout
        JButton logoutBtn = new JButton("  🚪  Logout");
        logoutBtn.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        logoutBtn.setForeground(new Color(255, 100, 100));
        logoutBtn.setBackground(UIConstants.SIDEBAR_BG);
        logoutBtn.setBorderPainted(false);
        logoutBtn.setFocusPainted(false);
        logoutBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        logoutBtn.setMaximumSize(new Dimension(UIConstants.SIDEBAR_WIDTH, 45));
        logoutBtn.setHorizontalAlignment(SwingConstants.LEFT);
        logoutBtn.setBorder(new EmptyBorder(10, 15, 10, 15));
        logoutBtn.addActionListener(e -> logout());
        sidebar.add(logoutBtn);
        sidebar.add(Box.createVerticalStrut(10));

        return sidebar;
    }

    private JButton createNavButton(String icon, String label, String card) {
        JButton btn = new JButton("  " + icon + "  " + label);
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        btn.setForeground(UIConstants.SIDEBAR_TEXT);
        btn.setBackground(UIConstants.SIDEBAR_BG);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setMaximumSize(new Dimension(UIConstants.SIDEBAR_WIDTH, 42));
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setBorder(new EmptyBorder(8, 15, 8, 15));
        btn.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) {
                if (btn != activeButton) btn.setBackground(UIConstants.SIDEBAR_HOVER);
            }
            @Override public void mouseExited(MouseEvent e) {
                if (btn != activeButton) btn.setBackground(UIConstants.SIDEBAR_BG);
            }
        });
        btn.addActionListener(e -> {
            setActiveButton(btn);
            cardLayout.show(contentPanel, card);
            if ("dashboard".equals(card)) dashboardPanel.refresh();
            else if ("requests".equals(card)) requestPanel.refresh();
        });
        return btn;
    }

    private void setActiveButton(JButton btn) {
        if (activeButton != null) {
            activeButton.setBackground(UIConstants.SIDEBAR_BG);
            activeButton.setForeground(UIConstants.SIDEBAR_TEXT);
        }
        activeButton = btn;
        btn.setBackground(new Color(0x1A5276));
        btn.setForeground(Color.WHITE);
    }

    private void logout() {
        if (JOptionPane.showConfirmDialog(this, "Logout?", "Confirm",
                JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            SessionManager.clearSession();
            ApiClient.clearToken();
            dispose();
            SwingUtilities.invokeLater(() -> new LoginScreen().setVisible(true));
        }
    }
}
