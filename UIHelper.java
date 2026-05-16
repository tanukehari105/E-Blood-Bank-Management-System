package com.bloodbank.ui.util;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Factory methods for consistently styled UI components.
 * Provides both new-style (primaryButton) and legacy-alias (createPrimaryButton) names
 * so all panels compile without changes.
 */
public class UIHelper {

    // ══════════════════════════════════════════════════════════════════════════
    // BUTTONS — primary API
    // ══════════════════════════════════════════════════════════════════════════

    public static JButton primaryButton(String text) {
        return styledButton(text, UIConstants.PRIMARY, Color.WHITE);
    }

    public static JButton successButton(String text) {
        return styledButton(text, UIConstants.SUCCESS, Color.WHITE);
    }

    public static JButton dangerButton(String text) {
        return styledButton(text, UIConstants.DANGER, Color.WHITE);
    }

    public static JButton warningButton(String text) {
        return styledButton(text, UIConstants.WARNING, Color.WHITE);
    }

    public static JButton infoButton(String text) {
        return styledButton(text, UIConstants.INFO, Color.WHITE);
    }

    public static JButton outlineButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(UIConstants.FONT_BODY);
        btn.setForeground(UIConstants.PRIMARY);
        btn.setBackground(Color.WHITE);
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UIConstants.PRIMARY, 1),
                new EmptyBorder(6, 14, 6, 14)));
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        addHoverEffect(btn, Color.WHITE, new Color(0xFFF5F5));
        return btn;
    }

    // ── Legacy aliases (used by older panels) ─────────────────────────────────

    public static JButton createPrimaryButton(String text)   { return primaryButton(text); }
    public static JButton createSuccessButton(String text)   { return successButton(text); }
    public static JButton createDangerButton(String text)    { return dangerButton(text); }
    public static JButton createWarningButton(String text)   { return warningButton(text); }
    public static JButton createInfoButton(String text)      { return infoButton(text); }
    public static JButton createSecondaryButton(String text) { return outlineButton(text); }

    private static JButton styledButton(String text, Color bg, Color fg) {
        JButton btn = new JButton(text);
        btn.setFont(UIConstants.FONT_BODY);
        btn.setForeground(fg);
        btn.setBackground(bg);
        btn.setBorder(new EmptyBorder(7, 16, 7, 16));
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setOpaque(true);
        addHoverEffect(btn, bg, bg.darker());
        return btn;
    }

    private static void addHoverEffect(JButton btn, Color normal, Color hover) {
        btn.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { btn.setBackground(hover); }
            @Override public void mouseExited(MouseEvent e)  { btn.setBackground(normal); }
        });
    }

    // ══════════════════════════════════════════════════════════════════════════
    // LABELS
    // ══════════════════════════════════════════════════════════════════════════

    public static JLabel titleLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(UIConstants.FONT_TITLE);
        lbl.setForeground(UIConstants.TEXT_PRIMARY);
        return lbl;
    }

    /** Alias used by older panels */
    public static JLabel createPageTitle(String text) { return titleLabel(text); }

    public static JLabel subtitleLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(UIConstants.FONT_SUBTITLE);
        lbl.setForeground(UIConstants.TEXT_PRIMARY);
        return lbl;
    }

    public static JLabel bodyLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(UIConstants.FONT_BODY);
        lbl.setForeground(UIConstants.TEXT_PRIMARY);
        return lbl;
    }

    public static JLabel mutedLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(UIConstants.FONT_SMALL);
        lbl.setForeground(UIConstants.TEXT_MUTED);
        return lbl;
    }

    // ══════════════════════════════════════════════════════════════════════════
    // TEXT FIELDS
    // ══════════════════════════════════════════════════════════════════════════

    public static JTextField textField(int columns) {
        JTextField tf = new JTextField(columns);
        tf.setFont(UIConstants.FONT_BODY);
        tf.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0xDDE1E7), 1),
                new EmptyBorder(6, 10, 6, 10)));
        return tf;
    }

    public static JPasswordField passwordField(int columns) {
        JPasswordField pf = new JPasswordField(columns);
        pf.setFont(UIConstants.FONT_BODY);
        pf.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0xDDE1E7), 1),
                new EmptyBorder(6, 10, 6, 10)));
        return pf;
    }

    public static JComboBox<String> comboBox(String[] items) {
        JComboBox<String> cb = new JComboBox<>(items);
        cb.setFont(UIConstants.FONT_BODY);
        return cb;
    }

    public static JTextField searchField(String placeholder) {
        JTextField tf = new JTextField(20);
        tf.setFont(UIConstants.FONT_BODY);
        tf.setText(placeholder);
        tf.setForeground(UIConstants.TEXT_MUTED);
        tf.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0xDDE1E7), 1),
                new EmptyBorder(6, 10, 6, 10)));
        tf.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override public void focusGained(java.awt.event.FocusEvent e) {
                if (tf.getText().equals(placeholder)) {
                    tf.setText("");
                    tf.setForeground(UIConstants.TEXT_PRIMARY);
                }
            }
            @Override public void focusLost(java.awt.event.FocusEvent e) {
                if (tf.getText().isBlank()) {
                    tf.setText(placeholder);
                    tf.setForeground(UIConstants.TEXT_MUTED);
                }
            }
        });
        return tf;
    }

    /** Alias used by older panels */
    public static JTextField createSearchField(String placeholder) { return searchField(placeholder); }

    // ══════════════════════════════════════════════════════════════════════════
    // TABLES
    // ══════════════════════════════════════════════════════════════════════════

    public static void styleTable(JTable table) {
        table.setFont(UIConstants.FONT_BODY);
        table.setRowHeight(UIConstants.TABLE_ROW_HEIGHT);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setSelectionBackground(new Color(0xFDE8E8));
        table.setSelectionForeground(UIConstants.TEXT_PRIMARY);
        table.setBackground(Color.WHITE);
        table.setFillsViewportHeight(true);

        JTableHeader header = table.getTableHeader();
        header.setFont(UIConstants.FONT_BOLD);
        header.setBackground(UIConstants.PRIMARY);
        header.setForeground(Color.WHITE);
        header.setPreferredSize(new Dimension(0, UIConstants.TABLE_HEADER_HEIGHT));
        header.setReorderingAllowed(false);

        // Alternating row renderer
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object value,
                    boolean isSelected, boolean hasFocus, int row, int col) {
                Component c = super.getTableCellRendererComponent(t, value, isSelected, hasFocus, row, col);
                if (!isSelected) {
                    c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(0xFAFAFA));
                }
                setBorder(new EmptyBorder(0, 8, 0, 8));
                return c;
            }
        });
    }

    public static JScrollPane scrollPane(JTable table) {
        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(BorderFactory.createLineBorder(new Color(0xE0E0E0), 1));
        sp.getViewport().setBackground(Color.WHITE);
        return sp;
    }

    // ══════════════════════════════════════════════════════════════════════════
    // CARDS & SECTIONS
    // ══════════════════════════════════════════════════════════════════════════

    public static JPanel card(String title, String value, Color accentColor) {
        JPanel panel = new JPanel(new BorderLayout(0, 8));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0xE8E8E8), 1),
                new EmptyBorder(18, 20, 18, 20)));

        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(UIConstants.FONT_SMALL);
        titleLbl.setForeground(UIConstants.TEXT_SECONDARY);

        JLabel valueLbl = new JLabel(value);
        valueLbl.setFont(new Font("Segoe UI", Font.BOLD, 28));
        valueLbl.setForeground(accentColor);

        JPanel accent = new JPanel();
        accent.setBackground(accentColor);
        accent.setPreferredSize(new Dimension(4, 0));

        panel.add(accent, BorderLayout.WEST);
        JPanel content = new JPanel(new BorderLayout(0, 4));
        content.setBackground(Color.WHITE);
        content.setBorder(new EmptyBorder(0, 12, 0, 0));
        content.add(titleLbl, BorderLayout.NORTH);
        content.add(valueLbl, BorderLayout.CENTER);
        panel.add(content, BorderLayout.CENTER);

        return panel;
    }

    public static JPanel sectionHeader(String title) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(UIConstants.BG_LIGHT);
        panel.setBorder(new EmptyBorder(0, 0, 12, 0));

        JLabel lbl = new JLabel(title);
        lbl.setFont(UIConstants.FONT_SUBTITLE);
        lbl.setForeground(UIConstants.TEXT_PRIMARY);
        panel.add(lbl, BorderLayout.WEST);

        JSeparator sep = new JSeparator();
        sep.setForeground(new Color(0xE0E0E0));
        panel.add(sep, BorderLayout.SOUTH);

        return panel;
    }

    public static JPanel alertPanel(String message, Color bg, Color border) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(bg);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 4, 0, 0, border),
                new EmptyBorder(8, 12, 8, 12)));
        JLabel lbl = new JLabel(message);
        lbl.setFont(UIConstants.FONT_BODY);
        lbl.setForeground(UIConstants.TEXT_PRIMARY);
        panel.add(lbl);
        return panel;
    }

    // ══════════════════════════════════════════════════════════════════════════
    // DIALOGS
    // ══════════════════════════════════════════════════════════════════════════

    public static void showError(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    public static void showSuccess(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Success", JOptionPane.INFORMATION_MESSAGE);
    }

    public static boolean confirm(Component parent, String message) {
        return JOptionPane.showConfirmDialog(parent, message, "Confirm",
                JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;
    }

    /** Alias used by older panels */
    public static boolean showConfirm(Component parent, String message) { return confirm(parent, message); }

    public static String prompt(Component parent, String message) {
        return JOptionPane.showInputDialog(parent, message);
    }
}
