package com.bloodbank.ui.screens;

import com.bloodbank.ui.api.ApiClient;
import com.bloodbank.ui.model.DashboardStats;
import com.bloodbank.ui.util.UIConstants;
import com.bloodbank.ui.util.UIHelper;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;
import java.util.Map;

/**
 * Enhanced dashboard — stat cards, blood stock grid, alerts, activity log, bar chart.
 */
public class DashboardPanel extends JPanel {

    private JPanel statsRow;
    private JPanel stockGrid;
    private JPanel alertsPanel;
    private JPanel activityPanel;
    private BloodStockChart chart;

    public DashboardPanel() {
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
        JLabel title = UIHelper.titleLabel("📊 Dashboard");
        JButton refreshBtn = UIHelper.outlineButton("↻ Refresh");
        refreshBtn.addActionListener(e -> refresh());
        header.add(title, BorderLayout.WEST);
        header.add(refreshBtn, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        // Main scroll area
        JPanel mainContent = new JPanel();
        mainContent.setLayout(new BoxLayout(mainContent, BoxLayout.Y_AXIS));
        mainContent.setBackground(UIConstants.BG_LIGHT);

        // Stat cards row
        statsRow = new JPanel(new GridLayout(1, 6, 12, 0));
        statsRow.setBackground(UIConstants.BG_LIGHT);
        statsRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));
        for (int i = 0; i < 6; i++) statsRow.add(UIHelper.card("Loading...", "—", UIConstants.PRIMARY));
        mainContent.add(statsRow);
        mainContent.add(Box.createVerticalStrut(16));

        // Middle row: stock grid + chart
        JPanel midRow = new JPanel(new GridLayout(1, 2, 16, 0));
        midRow.setBackground(UIConstants.BG_LIGHT);
        midRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 280));

        // Stock grid
        JPanel stockSection = new JPanel(new BorderLayout(0, 8));
        stockSection.setBackground(UIConstants.BG_LIGHT);
        stockSection.add(UIHelper.sectionHeader("🩸 Blood Stock"), BorderLayout.NORTH);
        stockGrid = new JPanel(new GridLayout(2, 4, 8, 8));
        stockGrid.setBackground(UIConstants.BG_LIGHT);
        for (String bg : UIConstants.BLOOD_GROUPS) stockGrid.add(buildStockCard(bg, 0));
        stockSection.add(stockGrid, BorderLayout.CENTER);
        midRow.add(stockSection);

        // Chart
        JPanel chartSection = new JPanel(new BorderLayout(0, 8));
        chartSection.setBackground(UIConstants.BG_LIGHT);
        chartSection.add(UIHelper.sectionHeader("📈 Stock Chart"), BorderLayout.NORTH);
        chart = new BloodStockChart();
        chartSection.add(chart, BorderLayout.CENTER);
        midRow.add(chartSection);

        mainContent.add(midRow);
        mainContent.add(Box.createVerticalStrut(16));

        // Bottom row: alerts + activity
        JPanel bottomRow = new JPanel(new GridLayout(1, 2, 16, 0));
        bottomRow.setBackground(UIConstants.BG_LIGHT);
        bottomRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 260));

        // Alerts
        JPanel alertsSection = new JPanel(new BorderLayout(0, 8));
        alertsSection.setBackground(UIConstants.BG_LIGHT);
        alertsSection.add(UIHelper.sectionHeader("🔔 Alerts"), BorderLayout.NORTH);
        alertsPanel = new JPanel();
        alertsPanel.setLayout(new BoxLayout(alertsPanel, BoxLayout.Y_AXIS));
        alertsPanel.setBackground(Color.WHITE);
        alertsPanel.setBorder(new EmptyBorder(8, 8, 8, 8));
        JScrollPane alertsScroll = new JScrollPane(alertsPanel);
        alertsScroll.setBorder(BorderFactory.createLineBorder(new Color(0xE0E0E0)));
        alertsSection.add(alertsScroll, BorderLayout.CENTER);
        bottomRow.add(alertsSection);

        // Recent activity
        JPanel activitySection = new JPanel(new BorderLayout(0, 8));
        activitySection.setBackground(UIConstants.BG_LIGHT);
        activitySection.add(UIHelper.sectionHeader("📝 Recent Activity"), BorderLayout.NORTH);
        activityPanel = new JPanel();
        activityPanel.setLayout(new BoxLayout(activityPanel, BoxLayout.Y_AXIS));
        activityPanel.setBackground(Color.WHITE);
        activityPanel.setBorder(new EmptyBorder(8, 8, 8, 8));
        JScrollPane activityScroll = new JScrollPane(activityPanel);
        activityScroll.setBorder(BorderFactory.createLineBorder(new Color(0xE0E0E0)));
        activitySection.add(activityScroll, BorderLayout.CENTER);
        bottomRow.add(activitySection);

        mainContent.add(bottomRow);

        JScrollPane scrollPane = new JScrollPane(mainContent);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        add(scrollPane, BorderLayout.CENTER);

        refresh();
    }

    public void refresh() {
        SwingWorker<DashboardStats, Void> worker = new SwingWorker<>() {
            @Override protected DashboardStats doInBackground() throws Exception {
                return ApiClient.fromJson(ApiClient.get("/dashboard"), DashboardStats.class);
            }
            @Override protected void done() {
                try {
                    DashboardStats stats = get();
                    updateStats(stats);
                } catch (Exception e) {
                    // Silently fail on background refresh
                }
            }
        };
        worker.execute();
    }

    private void updateStats(DashboardStats stats) {
        // Stat cards
        statsRow.removeAll();
        statsRow.add(UIHelper.card("Total Donors", String.valueOf(stats.totalDonors), UIConstants.INFO));
        statsRow.add(UIHelper.card("Blood Units", String.valueOf(stats.totalAvailableUnits), UIConstants.SUCCESS));
        statsRow.add(UIHelper.card("Pending Requests", String.valueOf(stats.pendingRequests), UIConstants.WARNING));
        statsRow.add(UIHelper.card("Total Donations", String.valueOf(stats.totalDonations), UIConstants.PRIMARY));
        statsRow.add(UIHelper.card("Hospitals", String.valueOf(stats.totalHospitals), new Color(0x8E44AD)));
        statsRow.add(UIHelper.card("🚨 Critical", String.valueOf(stats.criticalRequests), UIConstants.DANGER));
        statsRow.revalidate();

        // Stock grid
        stockGrid.removeAll();
        Map<String, Integer> stock = stats.stockByBloodGroup;
        if (stock != null) {
            for (String bg : UIConstants.BLOOD_GROUPS) {
                int qty = stock.getOrDefault(bg, 0);
                stockGrid.add(buildStockCard(bg, qty));
            }
            chart.updateData(stock);
        }
        stockGrid.revalidate();

        // Alerts
        alertsPanel.removeAll();
        addAlerts(stats.emergencyAlerts, "🚨", UIConstants.DANGER, new Color(0xFFEBEE));
        addAlerts(stats.lowStockAlerts, "⚠️", UIConstants.WARNING, new Color(0xFFF8E1));
        addAlerts(stats.expiryAlerts, "⏰", UIConstants.INFO, new Color(0xE3F2FD));
        if (alertsPanel.getComponentCount() == 0) {
            alertsPanel.add(UIHelper.mutedLabel("  ✅ No active alerts"));
        }
        alertsPanel.revalidate();

        // Activity
        activityPanel.removeAll();
        if (stats.recentActivity != null) {
            for (Map<String, Object> entry : stats.recentActivity) {
                String action = String.valueOf(entry.getOrDefault("action", ""));
                String user = String.valueOf(entry.getOrDefault("user", ""));
                String details = String.valueOf(entry.getOrDefault("details", ""));
                String ts = String.valueOf(entry.getOrDefault("timestamp", ""));
                if (ts.length() > 19) ts = ts.substring(0, 19).replace("T", " ");

                JPanel row = new JPanel(new BorderLayout(8, 0));
                row.setBackground(Color.WHITE);
                row.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(0xF0F0F0)));
                row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));

                JLabel actionLbl = new JLabel(action);
                actionLbl.setFont(UIConstants.FONT_BOLD);
                actionLbl.setForeground(UIConstants.PRIMARY);
                actionLbl.setPreferredSize(new Dimension(160, 20));

                JLabel detailLbl = new JLabel(details.length() > 50 ? details.substring(0, 50) + "…" : details);
                detailLbl.setFont(UIConstants.FONT_SMALL);
                detailLbl.setForeground(UIConstants.TEXT_SECONDARY);

                JLabel tsLbl = new JLabel(ts);
                tsLbl.setFont(UIConstants.FONT_SMALL);
                tsLbl.setForeground(UIConstants.TEXT_MUTED);
                tsLbl.setPreferredSize(new Dimension(140, 20));

                row.add(actionLbl, BorderLayout.WEST);
                row.add(detailLbl, BorderLayout.CENTER);
                row.add(tsLbl, BorderLayout.EAST);
                row.setBorder(new EmptyBorder(6, 8, 6, 8));
                activityPanel.add(row);
            }
        }
        if (activityPanel.getComponentCount() == 0) {
            activityPanel.add(UIHelper.mutedLabel("  No recent activity"));
        }
        activityPanel.revalidate();
        activityPanel.repaint();
    }

    private void addAlerts(List<String> alerts, String icon, Color textColor, Color bgColor) {
        if (alerts == null) return;
        for (String alert : alerts) {
            JPanel row = UIHelper.alertPanel(icon + " " + alert, bgColor, textColor);
            row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
            alertsPanel.add(row);
            alertsPanel.add(Box.createVerticalStrut(4));
        }
    }

    private JPanel buildStockCard(String bloodGroup, int quantity) {
        JPanel card = new JPanel(new BorderLayout(0, 4));
        Color color = quantity == 0 ? UIConstants.STOCK_EMPTY
                : quantity <= 5 ? UIConstants.STOCK_LOW : UIConstants.STOCK_OK;
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0xE8E8E8), 1),
                new EmptyBorder(10, 10, 10, 10)));

        JLabel bgLbl = new JLabel(bloodGroup, SwingConstants.CENTER);
        bgLbl.setFont(new Font("Segoe UI", Font.BOLD, 18));
        bgLbl.setForeground(color);

        JLabel qtyLbl = new JLabel(quantity + " u", SwingConstants.CENTER);
        qtyLbl.setFont(UIConstants.FONT_SMALL);
        qtyLbl.setForeground(UIConstants.TEXT_SECONDARY);

        card.add(bgLbl, BorderLayout.CENTER);
        card.add(qtyLbl, BorderLayout.SOUTH);
        return card;
    }

    // ── Inner Chart Component ──────────────────────────────────────────────────

    private static class BloodStockChart extends JPanel {
        private Map<String, Integer> data;

        BloodStockChart() {
            setBackground(Color.WHITE);
            setBorder(BorderFactory.createLineBorder(new Color(0xE8E8E8)));
            setPreferredSize(new Dimension(0, 220));
        }

        void updateData(Map<String, Integer> data) {
            this.data = data;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (data == null || data.isEmpty()) return;

            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth(), h = getHeight();
            int padding = 40, barGap = 8;
            int maxVal = data.values().stream().mapToInt(Integer::intValue).max().orElse(1);
            if (maxVal == 0) maxVal = 1;

            String[] groups = UIConstants.BLOOD_GROUPS;
            int barWidth = (w - 2 * padding - (groups.length - 1) * barGap) / groups.length;
            int chartH = h - 2 * padding;

            // Y-axis label
            g2.setFont(UIConstants.FONT_SMALL);
            g2.setColor(UIConstants.TEXT_MUTED);
            g2.drawString("Units", 4, padding - 5);

            for (int i = 0; i < groups.length; i++) {
                int qty = data.getOrDefault(groups[i], 0);
                int barH = (int) ((double) qty / maxVal * chartH);
                int x = padding + i * (barWidth + barGap);
                int y = h - padding - barH;

                Color barColor = qty == 0 ? UIConstants.STOCK_EMPTY
                        : qty <= 5 ? UIConstants.STOCK_LOW : UIConstants.STOCK_OK;

                g2.setColor(barColor);
                g2.fillRoundRect(x, y, barWidth, barH, 4, 4);

                // Value label
                g2.setColor(UIConstants.TEXT_PRIMARY);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 10));
                String val = String.valueOf(qty);
                int valX = x + (barWidth - g2.getFontMetrics().stringWidth(val)) / 2;
                if (barH > 14) g2.drawString(val, valX, y + 12);
                else g2.drawString(val, valX, y - 2);

                // Blood group label
                g2.setColor(UIConstants.TEXT_SECONDARY);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 10));
                String bg = groups[i];
                int bgX = x + (barWidth - g2.getFontMetrics().stringWidth(bg)) / 2;
                g2.drawString(bg, bgX, h - padding + 14);
            }

            // Baseline
            g2.setColor(new Color(0xE0E0E0));
            g2.drawLine(padding - 5, h - padding, w - padding + 5, h - padding);
        }
    }
}
