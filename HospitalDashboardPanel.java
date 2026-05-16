package com.bloodbank.ui.screens;

import com.bloodbank.ui.api.ApiClient;
import com.bloodbank.ui.model.DashboardStats;
import com.bloodbank.ui.util.UIConstants;
import com.bloodbank.ui.util.UIHelper;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.Map;

/**
 * Hospital dashboard — shows blood stock availability and request summary.
 */
public class HospitalDashboardPanel extends JPanel {

    private JPanel stockGrid;
    private JLabel pendingLabel;
    private JLabel totalUnitsLabel;

    public HospitalDashboardPanel() {
        setLayout(new BorderLayout(0, 16));
        setBackground(UIConstants.BG_LIGHT);
        setBorder(new EmptyBorder(20, 24, 20, 24));
        initUI();
    }

    private void initUI() {
        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(UIConstants.BG_LIGHT);
        header.setBorder(new EmptyBorder(0, 0, 16, 0));
        JLabel title = UIHelper.titleLabel("📊 Hospital Dashboard");
        JButton refreshBtn = UIHelper.outlineButton("↻ Refresh");
        refreshBtn.addActionListener(e -> refresh());
        header.add(title, BorderLayout.WEST);
        header.add(refreshBtn, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        // Stats row
        JPanel statsRow = new JPanel(new GridLayout(1, 2, 16, 0));
        statsRow.setBackground(UIConstants.BG_LIGHT);
        pendingLabel = new JLabel("—");
        totalUnitsLabel = new JLabel("—");
        statsRow.add(UIHelper.card("My Pending Requests", "—", UIConstants.WARNING));
        statsRow.add(UIHelper.card("Total Blood Available", "—", UIConstants.SUCCESS));
        add(statsRow, BorderLayout.NORTH);

        // Blood stock grid
        JPanel stockSection = new JPanel(new BorderLayout(0, 10));
        stockSection.setBackground(UIConstants.BG_LIGHT);
        stockSection.add(UIHelper.sectionHeader("🩸 Blood Stock Availability"), BorderLayout.NORTH);

        stockGrid = new JPanel(new GridLayout(2, 4, 12, 12));
        stockGrid.setBackground(UIConstants.BG_LIGHT);
        for (String bg : UIConstants.BLOOD_GROUPS) {
            stockGrid.add(buildStockCard(bg, 0));
        }
        stockSection.add(stockGrid, BorderLayout.CENTER);
        add(stockSection, BorderLayout.CENTER);

        refresh();
    }

    public void refresh() {
        SwingWorker<DashboardStats, Void> worker = new SwingWorker<>() {
            @Override protected DashboardStats doInBackground() throws Exception {
                String json = ApiClient.get("/hospital-dashboard");
                return ApiClient.fromJson(json, DashboardStats.class);
            }
            @Override protected void done() {
                try {
                    DashboardStats stats = get();
                    updateStockGrid(stats.stockByBloodGroup);
                } catch (Exception e) {
                    // Silently fail on refresh
                }
            }
        };
        worker.execute();
    }

    private void updateStockGrid(Map<String, Integer> stock) {
        stockGrid.removeAll();
        if (stock == null) { stockGrid.revalidate(); return; }
        for (String bg : UIConstants.BLOOD_GROUPS) {
            int qty = stock.getOrDefault(bg, 0);
            stockGrid.add(buildStockCard(bg, qty));
        }
        stockGrid.revalidate();
        stockGrid.repaint();
    }

    private JPanel buildStockCard(String bloodGroup, int quantity) {
        JPanel card = new JPanel(new BorderLayout(0, 6));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0xE8E8E8), 1),
                new EmptyBorder(16, 16, 16, 16)));

        Color color = quantity == 0 ? UIConstants.STOCK_EMPTY
                : quantity <= 5 ? UIConstants.STOCK_LOW : UIConstants.STOCK_OK;

        JLabel bgLabel = new JLabel(bloodGroup, SwingConstants.CENTER);
        bgLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        bgLabel.setForeground(color);

        JLabel qtyLabel = new JLabel(quantity + " units", SwingConstants.CENTER);
        qtyLabel.setFont(UIConstants.FONT_BODY);
        qtyLabel.setForeground(UIConstants.TEXT_SECONDARY);

        String statusText = quantity == 0 ? "❌ Empty" : quantity <= 5 ? "⚠️ Low" : "✅ Available";
        JLabel statusLabel = new JLabel(statusText, SwingConstants.CENTER);
        statusLabel.setFont(UIConstants.FONT_SMALL);
        statusLabel.setForeground(color);

        card.add(bgLabel, BorderLayout.NORTH);
        card.add(qtyLabel, BorderLayout.CENTER);
        card.add(statusLabel, BorderLayout.SOUTH);
        return card;
    }
}
