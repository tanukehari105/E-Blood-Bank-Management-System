package com.bloodbank.ui.util;

import java.awt.*;

/**
 * Centralized UI constants for consistent styling across all screens.
 */
public class UIConstants {

    // ── Window ────────────────────────────────────────────────────────────────
    public static final Dimension WINDOW_SIZE = new Dimension(1400, 850);
    public static final int SIDEBAR_WIDTH = 220;

    // ── Primary Colors ────────────────────────────────────────────────────────
    public static final Color PRIMARY = new Color(0xC0392B);       // Blood red
    public static final Color PRIMARY_DARK = new Color(0x96281B);  // Dark red
    public static final Color PRIMARY_LIGHT = new Color(0xE74C3C); // Light red
    public static final Color ACCENT = new Color(0x2980B9);        // Blue accent

    // ── Sidebar ───────────────────────────────────────────────────────────────
    public static final Color SIDEBAR_BG = new Color(0x1C2833);
    public static final Color SIDEBAR_HOVER = new Color(0x2C3E50);
    public static final Color SIDEBAR_ACTIVE = new Color(0xC0392B);
    public static final Color SIDEBAR_TEXT = new Color(0xBDC3C7);

    // ── Background ────────────────────────────────────────────────────────────
    public static final Color BG_LIGHT = new Color(0xF5F6FA);
    public static final Color BG_WHITE = Color.WHITE;
    public static final Color BG_CARD = Color.WHITE;

    // ── Text ──────────────────────────────────────────────────────────────────
    public static final Color TEXT_PRIMARY = new Color(0x2C3E50);
    public static final Color TEXT_SECONDARY = new Color(0x7F8C8D);
    public static final Color TEXT_MUTED = new Color(0x95A5A6);

    // ── Status Colors ─────────────────────────────────────────────────────────
    public static final Color SUCCESS = new Color(0x27AE60);
    public static final Color WARNING = new Color(0xF39C12);
    public static final Color DANGER = new Color(0xE74C3C);
    public static final Color INFO = new Color(0x2980B9);
    public static final Color CRITICAL_BG = new Color(0xFFEBEE);
    public static final Color URGENT_BG = new Color(0xFFF3E0);
    public static final Color NORMAL_BG = new Color(0xE8F5E9);

    // ── Stock Level Colors ────────────────────────────────────────────────────
    public static final Color STOCK_EMPTY = new Color(0xE74C3C);
    public static final Color STOCK_LOW = new Color(0xF39C12);
    public static final Color STOCK_OK = new Color(0x27AE60);

    // ── Fonts ─────────────────────────────────────────────────────────────────
    public static final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 22);
    public static final Font FONT_SUBTITLE = new Font("Segoe UI", Font.BOLD, 16);
    public static final Font FONT_BODY = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_SMALL = new Font("Segoe UI", Font.PLAIN, 11);
    public static final Font FONT_BOLD = new Font("Segoe UI", Font.BOLD, 13);
    public static final Font FONT_MONO = new Font("Consolas", Font.PLAIN, 12);

    // ── Blood Groups ──────────────────────────────────────────────────────────
    public static final String[] BLOOD_GROUPS = {"A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-"};

    // ── Urgency Levels ────────────────────────────────────────────────────────
    public static final String[] URGENCY_LEVELS = {"NORMAL", "URGENT", "CRITICAL"};

    // ── Table Row Height ──────────────────────────────────────────────────────
    public static final int TABLE_ROW_HEIGHT = 32;
    public static final int TABLE_HEADER_HEIGHT = 36;

    // ── Border Radius (for custom painting) ──────────────────────────────────
    public static final int CARD_RADIUS = 12;
    public static final int BUTTON_RADIUS = 6;
}
