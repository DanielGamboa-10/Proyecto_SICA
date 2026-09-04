package com.zonaacme.sica.ui.swing;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

public final class ThemeConstants {

    private ThemeConstants() {}

    // Paleta de Colores Ultra-Moderna (Cyber Security Dark Slate)
    public static final Color BG_DARK = new Color(11, 15, 25);            // Slate 950
    public static final Color BG_SIDEBAR = new Color(17, 24, 39);         // Slate 900
    public static final Color BG_HEADER = new Color(15, 23, 42);          // Slate 900
    public static final Color BG_CARD = new Color(24, 32, 54);            // Slate 800 Elevated
    public static final Color BG_CARD_HOVER = new Color(36, 48, 77);      // Slate 700 Hover
    public static final Color BG_INPUT = new Color(13, 19, 33);           // Dark Input
    public static final Color BG_TABLE_HEADER = new Color(26, 36, 60);    // Header Slate
    public static final Color BG_TABLE_ROW_ALT = new Color(16, 22, 38);   // Alternating row

    // Colores de Acento Vibrantes y Gradientes
    public static final Color ACCENT_PRIMARY = new Color(99, 102, 241);   // Indigo Neon
    public static final Color ACCENT_CYAN = new Color(6, 182, 212);       // Cyan Cyber
    public static final Color ACCENT_SUCCESS = new Color(16, 185, 129);   // Emerald
    public static final Color ACCENT_DANGER = new Color(239, 68, 68);     // Crimson
    public static final Color ACCENT_WARNING = new Color(245, 158, 11);   // Amber Gold
    public static final Color ACCENT_INFO = new Color(59, 130, 246);      // Sky Blue

    // Textos
    public static final Color TEXT_PRIMARY = new Color(248, 250, 252);    // Pure White/Slate 50
    public static final Color TEXT_SECONDARY = new Color(203, 213, 225);  // Slate 300 (Alto Contraste)
    public static final Color TEXT_MUTED = new Color(148, 163, 184);      // Slate 400
    public static final Color BORDER_COLOR = new Color(51, 65, 85);       // Border Glow
    public static final Color BORDER_HIGHLIGHT = new Color(99, 102, 241, 140);

    // Tipografías
    public static final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 24);
    public static final Font FONT_SUBTITLE = new Font("Segoe UI", Font.BOLD, 17);
    public static final Font FONT_HEADER = new Font("Segoe UI", Font.BOLD, 14);
    public static final Font FONT_BODY = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_BODY_BOLD = new Font("Segoe UI", Font.BOLD, 13);
    public static final Font FONT_SMALL = new Font("Segoe UI", Font.PLAIN, 11);
    public static final Font FONT_CODE = new Font("Consolas", Font.BOLD, 13);

    public static JLabel createLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(FONT_BODY_BOLD);
        lbl.setForeground(TEXT_PRIMARY);
        return lbl;
    }

    /**
     * Crea un botón moderno con gradiente dinámico, esquinas redondeadas y animación hover.
     */
    public static JButton createGradientButton(String text, Color colorStart, Color colorEnd, Color fg) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                Color start = colorStart;
                Color end = colorEnd;
                if (getModel().isPressed()) {
                    start = colorStart.darker();
                    end = colorEnd.darker();
                } else if (getModel().isRollover()) {
                    start = colorStart.brighter();
                    end = colorEnd.brighter();
                }

                GradientPaint gp = new GradientPaint(0, 0, start, getWidth(), getHeight(), end);
                g2.setPaint(gp);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 12, 12));

                // Borde sutil
                g2.setColor(new Color(255, 255, 255, 50));
                g2.setStroke(new BasicStroke(1.2f));
                g2.draw(new RoundRectangle2D.Float(0, 0, getWidth() - 1, getHeight() - 1, 12, 12));

                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(FONT_BODY_BOLD);
        btn.setForeground(fg);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(10, 20, 10, 20));
        return btn;
    }

    public static JButton createButton(String text, Color bg, Color fg) {
        return createGradientButton(text, bg, bg.darker(), fg);
    }

    /**
     * Crea un panel tipo tarjeta (Glassmorphism card) con borde brillante y sombra.
     */
    public static JPanel createCard() {
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Fondo de tarjeta con gradiente sutil
                GradientPaint gp = new GradientPaint(0, 0, BG_CARD, 0, getHeight(), new Color(18, 25, 45));
                g2.setPaint(gp);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 18, 18));

                // Borde sutilmente iluminado
                g2.setColor(BORDER_COLOR);
                g2.setStroke(new BasicStroke(1.4f));
                g2.draw(new RoundRectangle2D.Float(0, 0, getWidth() - 1, getHeight() - 1, 18, 18));

                g2.dispose();
                super.paintComponent(g);
            }
        };
        card.setOpaque(false);
        card.setBorder(new EmptyBorder(18, 20, 18, 20));
        return card;
    }

    public static JTextField createTextField() {
        JTextField tf = new JTextField();
        tf.setBackground(BG_INPUT);
        tf.setForeground(TEXT_PRIMARY);
        tf.setCaretColor(ACCENT_CYAN);
        tf.setFont(FONT_BODY);
        tf.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1, true),
                new EmptyBorder(10, 12, 10, 12)
        ));
        return tf;
    }

    public static JPasswordField createPasswordField() {
        JPasswordField pf = new JPasswordField();
        pf.setBackground(BG_INPUT);
        pf.setForeground(TEXT_PRIMARY);
        pf.setCaretColor(ACCENT_CYAN);
        pf.setFont(FONT_BODY);
        pf.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1, true),
                new EmptyBorder(10, 12, 10, 12)
        ));
        return pf;
    }

    public static <T> JComboBox<T> createComboBox(T[] items) {
        JComboBox<T> combo = new JComboBox<>(items);
        styleComboBox(combo);
        return combo;
    }

    public static <T> JComboBox<T> createComboBox(ComboBoxModel<T> model) {
        JComboBox<T> combo = new JComboBox<>(model);
        styleComboBox(combo);
        return combo;
    }

    public static <T> void styleComboBox(JComboBox<T> combo) {
        combo.setBackground(BG_INPUT);
        combo.setForeground(TEXT_PRIMARY);
        combo.setFont(FONT_BODY);
        combo.setFocusable(false);

        combo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                setOpaque(true);
                if (isSelected) {
                    setBackground(new Color(99, 102, 241));
                    setForeground(Color.WHITE);
                } else {
                    setBackground(new Color(13, 19, 33));
                    setForeground(new Color(248, 250, 252));
                }
                setFont(FONT_BODY);
                setBorder(new EmptyBorder(8, 12, 8, 12));
                return c;
            }
        });

        combo.setUI(new javax.swing.plaf.basic.BasicComboBoxUI() {
            @Override
            protected JButton createArrowButton() {
                JButton btn = super.createArrowButton();
                btn.setBackground(new Color(26, 36, 60));
                btn.setBorder(BorderFactory.createEmptyBorder());
                return btn;
            }

            @Override
            public void paintCurrentValueBackground(Graphics g, Rectangle bounds, boolean hasFocus) {
                g.setColor(new Color(13, 19, 33));
                g.fillRect(bounds.x, bounds.y, bounds.width, bounds.height);
            }
        });

        combo.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1, true),
                new EmptyBorder(4, 8, 4, 8)
        ));
    }

    /**
     * Aplica diseño visual moderno, alto contraste y badges coloreados en las tablas.
     * Fuerza un HeaderRenderer personalizado para evitar que el Look & Feel de Windows dibuje cabeceras blancas.
     */
    public static void styleTable(JTable table) {
        table.setBackground(BG_CARD);
        table.setForeground(TEXT_PRIMARY);
        table.setGridColor(new Color(45, 58, 88));
        table.setFont(FONT_BODY);
        table.setRowHeight(42);
        table.setSelectionBackground(new Color(99, 102, 241, 150));
        table.setSelectionForeground(Color.WHITE);
        table.setShowVerticalLines(false);
        table.setShowHorizontalLines(true);

        JTableHeader header = table.getTableHeader();
        header.setPreferredSize(new Dimension(0, 44));
        header.setReorderingAllowed(false);

        // Header Renderer personalizado con texto blanco nítido y fondo oscuro contrastado
        header.setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel lbl = new JLabel(value != null ? value.toString().toUpperCase() : "", SwingConstants.CENTER);
                lbl.setOpaque(true);
                lbl.setBackground(BG_TABLE_HEADER);
                lbl.setForeground(new Color(255, 255, 255)); // Blanco puro
                lbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
                lbl.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0, 0, 2, 1, ACCENT_PRIMARY),
                        new EmptyBorder(10, 8, 10, 8)
                ));
                return lbl;
            }
        });

        // Renderizador de Celdas con alto contraste y badges
        DefaultTableCellRenderer cellRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(t, value, isSelected, hasFocus, row, column);
                setHorizontalAlignment(JLabel.CENTER);

                if (!isSelected) {
                    c.setBackground(row % 2 == 0 ? BG_CARD : BG_TABLE_ROW_ALT);
                    String valStr = value != null ? value.toString() : "";
                    if (valStr.contains("PERMITIDO") || valStr.contains("ACTIVO") || valStr.contains("APROBADA") || valStr.contains("DENTRO")) {
                        c.setForeground(new Color(52, 211, 153)); // Verde Esmeralda
                        setFont(FONT_BODY_BOLD);
                    } else if (valStr.contains("DENEGADO") || valStr.contains("BLOQUEADO") || valStr.contains("RECHAZADA") || valStr.contains("ALERTA")) {
                        c.setForeground(new Color(248, 113, 113)); // Rojo Coral
                        setFont(FONT_BODY_BOLD);
                    } else if (valStr.contains("PENDIENTE")) {
                        c.setForeground(new Color(251, 191, 36)); // Ámbar Dorado
                        setFont(FONT_BODY_BOLD);
                    } else {
                        c.setForeground(TEXT_PRIMARY); // Blanco nítido
                        setFont(FONT_BODY);
                    }
                } else {
                    c.setBackground(new Color(99, 102, 241, 180));
                    c.setForeground(Color.WHITE);
                }
                return c;
            }
        };

        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(cellRenderer);
        }
    }
}
