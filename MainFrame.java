package com.bloodbank.ui.screens;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

import com.bloodbank.ui.api.ApiClient;
import com.bloodbank.ui.util.SessionManager;
import com.bloodbank.ui.util.UIConstants;

/**
 * Main application frame for ADMIN and STAFF roles.
 * Sidebar navigation with CardLayout content area.
 * Role-aware: ADMIN sees Hospital, Staff, Audit, and Allocation panels.
 */
public class MainFrame extends JFrame {

    private JPanel contentPanel;
    private CardLayout cardLayout;
    private JButton activeButton;

    // ── Panels ────────────────────────────────────────────────────────────────
    private DashboardPanel dashboardPanel;
    private DonorPanel donorPanel;
    private InventoryPanel inventoryPanel;
    private DonationPanel donationPanel;
    private RequestPanel requestPanel;
    private CampPanel campPanel;
    private ReportPanel reportPanel;
    private AllocationPanel allocationPanel;

    // ADMIN-only panels
    private HospitalPanel hospitalPanel;
    private StaffPanel staffPanel;
    private AuditLogPanel auditLogPanel;

    public MainFrame() {
        setTitle("Blood Bank Management System — Smart Healthcare Platform");
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

        // Create all panels
        dashboardPanel  = new DashboardPanel();
        donorPanel      = new DonorPanel();
        inventoryPanel  = new InventoryPanel();
        donationPanel   = new DonationPanel();
        requestPanel    = new RequestPanel();
        campPanel       = new CampPanel();
        reportPanel     = new ReportPanel();
        allocationPanel = new AllocationPanel();

        contentPanel.add(dashboardPanel,  "dashboard");
        contentPanel.add(donorPanel,      "donors");
        contentPanel.add(inventoryPanel,  "inventory");
        contentPanel.add(donationPanel,   "donations");
        contentPanel.add(requestPanel,    "requests");
        contentPanel.add(campPanel,       "camps");
        contentPanel.add(reportPanel,     "reports");
        contentPanel.add(allocationPanel, "allocations");

        // ADMIN-only panels
        if (SessionManager.isAdmin()) {
            hospitalPanel = new HospitalPanel();
            staffPanel    = new StaffPanel();
            auditLogPanel = new AuditLogPanel();
            contentPanel.add(hospitalPanel, "hospitals");
            contentPanel.add(staffPanel,    "staff");
            contentPanel.add(auditLogPanel, "audit");
        }

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
        logoPanel.setBackground(UIConstants.PRIMARY_DARK);
        logoPanel.setMaximumSize(new Dimension(UIConstants.SIDEBAR_WIDTH, 72));
        JLabel logoIcon = new JLabel("🩸");
        logoIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 22));
        JLabel logoText = new JLabel("BloodBank");
        logoText.setFont(new Font("Segoe UI", Font.BOLD, 16));
        logoText.setForeground(Color.WHITE);
        logoPanel.add(logoIcon);
        logoPanel.add(logoText);
        sidebar.add(logoPanel);

        // User info
        JPanel userPanel = new JPanel();
        userPanel.setBackground(new Color(0x243342));
        userPanel.setMaximumSize(new Dimension(UIConstants.SIDEBAR_WIDTH, 65));
        userPanel.setLayout(new BoxLayout(userPanel, BoxLayout.Y_AXIS));
        userPanel.setBorder(new EmptyBorder(10, 15, 10, 15));
        JLabel nameLabel = new JLabel(SessionManager.getFullName());
        nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        nameLabel.setForeground(Color.WHITE);
        JLabel roleLabel = new JLabel(SessionManager.getRole());
        roleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        roleLabel.setForeground(new Color(255, 255, 255, 150));
        userPanel.add(nameLabel);
        userPanel.add(Box.createVerticalStrut(3));
        userPanel.add(roleLabel);
        sidebar.add(userPanel);
        sidebar.add(Box.createVerticalStrut(8));

        // Navigation items — common to both ADMIN and STAFF
        addNavButton(sidebar, "📊", "Dashboard",   "dashboard");
        addNavButton(sidebar, "👥", "Donors",       "donors");
        addNavButton(sidebar, "🩸", "Inventory",    "inventory");
        addNavButton(sidebar, "💉", "Donations",    "donations");
        addNavButton(sidebar, "📋", "Requests",     "requests");
        addNavButton(sidebar, "🏕️", "Camps",        "camps");
        addNavButton(sidebar, "🔗", "Allocations",  "allocations");
        addNavButton(sidebar, "📄", "Reports",      "reports");

        // ADMIN-only navigation
        if (SessionManager.isAdmin()) {
            sidebar.add(createSectionDivider("ADMIN"));
            addNavButton(sidebar, "🏥", "Hospitals",    "hospitals");
            addNavButton(sidebar, "👨‍💼", "Staff",        "staff");
            addNavButton(sidebar, "📋", "Audit Logs",   "audit");
        }

        sidebar.add(Box.createVerticalGlue());

        // Logout button
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

    private void addNavButton(JPanel sidebar, String icon, String label, String card) {
        JButton btn = createNavButton(icon, label, card);
        sidebar.add(btn);
        if ("dashboard".equals(card)) setActiveButton(btn);
    }

    private JPanel createSectionDivider(String label) {
        JPanel divider = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 4));
        divider.setBackground(UIConstants.SIDEBAR_BG);
        divider.setMaximumSize(new Dimension(UIConstants.SIDEBAR_WIDTH, 28));
        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 10));
        lbl.setForeground(new Color(255, 255, 255, 80));
        divider.add(lbl);
        return divider;
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
            refreshPanel(card);
        });

        return btn;
    }

    private void setActiveButton(JButton btn) {
        if (activeButton != null) {
            activeButton.setBackground(UIConstants.SIDEBAR_BG);
            activeButton.setForeground(UIConstants.SIDEBAR_TEXT);
        }
        activeButton = btn;
        btn.setBackground(UIConstants.SIDEBAR_ACTIVE);
        btn.setForeground(Color.WHITE);
    }

    private void refreshPanel(String card) {
        switch (card) {
            case "dashboard"   -> dashboardPanel.refresh();
            case "donors"      -> donorPanel.refresh();
            case "inventory"   -> inventoryPanel.refresh();
            case "donations"   -> donationPanel.refresh();
            case "requests"    -> requestPanel.refresh();
            case "camps"       -> campPanel.refresh();
            case "allocations" -> allocationPanel.refresh();
            case "hospitals"   -> { if (hospitalPanel != null) hospitalPanel.refresh(); }
            case "audit"       -> { if (auditLogPanel != null) auditLogPanel.refresh(); }
        }
    }

    private void logout() {
        int choice = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to logout?", "Confirm", JOptionPane.YES_NO_OPTION);
        if (choice == JOptionPane.YES_OPTION) {
            SessionManager.clearSession();
            ApiClient.clearToken();
            dispose();
            SwingUtilities.invokeLater(() -> new LoginScreen().setVisible(true));
        }
    }
}
