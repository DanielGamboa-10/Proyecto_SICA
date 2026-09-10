package com.zonaacme.sica.ui.swing;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

public final class ThemeConstants {

    private ThemeConstants() {}

    // Paleta de Colores Ultra-Elegante (Obsidian Jet Black & Royal Neon Violet)
    public static final Color BG_DARK = new Color(9, 7, 18);              // Pure Deep Obsidian Black
    public static final Color BG_SIDEBAR = new Color(13, 10, 25);         // Jet Amethyst Black
    public static final Color BG_HEADER = new Color(18, 13, 34);          // Midnight Velvet Purple
    public static final Color BG_CARD = new Color(22, 16, 42);            // Elevated Velvet Glass
    public static final Color BG_CARD_HOVER = new Color(42, 26, 80);      // Purple Glow Hover
    public static final Color BG_INPUT = new Color(12, 9, 24);            // Inset Dark Onyx
    public static final Color BG_TABLE_HEADER = new Color(30, 20, 58);    // Royal Violet Header
    public static final Color BG_TABLE_ROW_ALT = new Color(16, 12, 30);   // Alternating Onyx Row

    // Colores de Acento Vibrantes y Gradientes Neón
    public static final Color ACCENT_PRIMARY = new Color(147, 51, 234);   // Electric Purple 600
    public static final Color ACCENT_PURPLE = new Color(168, 85, 247);    // Neon Purple 500
    public static final Color ACCENT_MAGENTA = new Color(217, 70, 239);   // Neon Fuchsia
    public static final Color ACCENT_CYAN = new Color(56, 189, 248);      // Cyber Cyan
    public static final Color ACCENT_SUCCESS = new Color(16, 185, 129);   // Emerald
    public static final Color ACCENT_DANGER = new Color(244, 63, 94);     // Rose Neon
    public static final Color ACCENT_WARNING = new Color(245, 158, 11);   // Amber Gold
    public static final Color ACCENT_INFO = new Color(99, 102, 241);      // Indigo Neon

    // Textos
    public static final Color TEXT_PRIMARY = new Color(250, 250, 255);    // Pure White
    public static final Color TEXT_SECONDARY = new Color(216, 180, 254);  // Soft Lavender
    public static final Color TEXT_MUTED = new Color(167, 139, 250);      // Muted Purple Slate
    public static final Color BORDER_COLOR = new Color(92, 58, 158, 220); // Neon Violet Border
    public static final Color BORDER_HIGHLIGHT = new Color(216, 180, 254, 200);

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
     * Crea un botón moderno con gradiente púrpura dinámico, micro-animación de brillo fluido y esquinas redondeadas.
     */
    public static JButton createGradientButton(String text, Color colorStart, Color colorEnd, Color fg) {
        JButton btn = new JButton(text) {
            private float glowAlpha = 0.0f;
            private float shinePos = -1.0f;
            private float idlePulse = 0.0f;
            private javax.swing.Timer animTimer;

            {
                animTimer = new javax.swing.Timer(25, e -> {
                    idlePulse = (idlePulse + 0.06f) % (float)(2 * Math.PI);
                    boolean isHover = getModel().isRollover();
                    if (isHover) {
                        glowAlpha = Math.min(1.0f, glowAlpha + 0.14f);
                        shinePos += 0.07f;
                        if (shinePos > 2.2f) shinePos = -0.4f;
                    } else {
                        if (glowAlpha > 0.0f) {
                            glowAlpha = Math.max(0.0f, glowAlpha - 0.10f);
                        }
                        shinePos = -1.0f;
                    }
                    repaint();
                });
                animTimer.start();
            }

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                Color start = colorStart;
                Color end = colorEnd;
                if (getModel().isPressed()) {
                    start = colorStart.darker();
                    end = colorEnd.darker();
                }

                // Gradiente principal oscuro-morado de alta elegancia
                GradientPaint gp = new GradientPaint(0, 0, start, getWidth(), getHeight(), end);
                g2.setPaint(gp);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 14, 14));

                // Resplandor de animación hover y haz de luz en movimiento
                if (glowAlpha > 0.01f) {
                    g2.setColor(new Color(255, 255, 255, (int)(45 * glowAlpha)));
                    g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 14, 14));

                    if (shinePos >= 0.0f && shinePos <= 1.8f) {
                        int shineX = (int) (getWidth() * shinePos);
                        GradientPaint shine = new GradientPaint(
                                shineX - 35, 0, new Color(255, 255, 255, 0),
                                shineX, 0, new Color(255, 255, 255, (int)(95 * glowAlpha)),
                                true
                        );
                        g2.setPaint(shine);
                        g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 14, 14));
                    }
                }

                // Borde neón con respiración activa y resplandor púrpura
                int borderBrightness = (int) (140 + 60 * Math.sin(idlePulse) + 55 * glowAlpha);
                borderBrightness = Math.max(80, Math.min(255, borderBrightness));
                Color borderColor = new Color(192, 132, 252, borderBrightness);
                g2.setColor(borderColor);
                g2.setStroke(new BasicStroke(1.4f));
                g2.draw(new RoundRectangle2D.Float(0, 0, getWidth() - 1, getHeight() - 1, 14, 14));

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
     * Crea un panel tipo tarjeta (Glassmorphism card) con tonalidad obsidiana-púrpura y borde resplandeciente dinámico.
     */
    public static JPanel createCard() {
        JPanel card = new JPanel() {
            private float pulse = 0.0f;
            private javax.swing.Timer timer;
            {
                timer = new javax.swing.Timer(40, e -> {
                    pulse = (pulse + 0.04f) % (float)(2 * Math.PI);
                    repaint();
                });
                timer.start();
            }

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int w = getWidth();
                int h = getHeight();

                // Fondo de tarjeta con gradiente negro obsidiana a púrpura profundo
                GradientPaint gp = new GradientPaint(0, 0, BG_CARD, 0, h, new Color(14, 10, 28));
                g2.setPaint(gp);
                g2.fill(new RoundRectangle2D.Float(0, 0, w, h, 18, 18));

                // Haz sutil superior viajando
                int gleamX = (int) (w * (0.5f + 0.4f * Math.sin(pulse)));
                GradientPaint topGleam = new GradientPaint(
                        gleamX - 60, 0, new Color(168, 85, 247, 0),
                        gleamX, 0, new Color(217, 70, 239, 130),
                        true
                );
                g2.setPaint(topGleam);
                g2.fillRect(0, 0, w, 2);

                // Borde con resplandor púrpura elegante
                int bAlpha = (int) (140 + 40 * Math.sin(pulse));
                g2.setColor(new Color(147, 51, 234, Math.min(255, bAlpha)));
                g2.setStroke(new BasicStroke(1.4f));
                g2.draw(new RoundRectangle2D.Float(0, 0, w - 1, h - 1, 18, 18));

                g2.dispose();
                super.paintComponent(g);
            }
        };
        card.setOpaque(false);
        card.setBorder(new EmptyBorder(18, 20, 18, 20));
        return card;
    }

    public static JTextField createTextField() {
        return createTextField("");
    }

    public static JTextField createTextField(String placeholder) {
        JTextField tf = new JTextField();
        if (placeholder != null && !placeholder.isEmpty()) {
            tf.setText(placeholder);
        }
        tf.setBackground(BG_INPUT);
        tf.setForeground(TEXT_PRIMARY);
        tf.setCaretColor(ACCENT_PURPLE);
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
        pf.setCaretColor(ACCENT_PURPLE);
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
                    setBackground(ACCENT_PRIMARY);
                    setForeground(Color.WHITE);
                } else {
                    setBackground(BG_INPUT);
                    setForeground(TEXT_PRIMARY);
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
                btn.setBackground(BG_TABLE_HEADER);
                btn.setBorder(BorderFactory.createEmptyBorder());
                return btn;
            }

            @Override
            public void paintCurrentValueBackground(Graphics g, Rectangle bounds, boolean hasFocus) {
                g.setColor(BG_INPUT);
                g.fillRect(bounds.x, bounds.y, bounds.width, bounds.height);
            }
        });

        combo.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1, true),
                new EmptyBorder(4, 8, 4, 8)
        ));
    }

    public static JScrollPane createScrollPane(Component view) {
        JScrollPane sp = new JScrollPane(view);
        styleScrollPane(sp);
        return sp;
    }

    public static void styleScrollPane(JScrollPane sp) {
        sp.setBackground(BG_CARD);
        sp.setOpaque(true);
        if (sp.getViewport() != null) {
            sp.getViewport().setBackground(BG_CARD);
            sp.getViewport().setOpaque(true);
        }
        sp.setBorder(BorderFactory.createLineBorder(BORDER_COLOR, 1));
        sp.getVerticalScrollBar().setUnitIncrement(16);
    }

    /**
     * Aplica diseño visual moderno, alto contraste y badges coloreados en las tablas.
     */
    public static void styleTable(JTable table) {
        table.setBackground(BG_CARD);
        table.setForeground(TEXT_PRIMARY);
        table.setGridColor(new Color(56, 38, 96));
        table.setFont(FONT_BODY);
        table.setRowHeight(42);
        table.setSelectionBackground(new Color(139, 92, 246, 170));
        table.setSelectionForeground(Color.WHITE);
        table.setShowVerticalLines(false);
        table.setShowHorizontalLines(true);

        JTableHeader header = table.getTableHeader();
        header.setPreferredSize(new Dimension(0, 44));
        header.setReorderingAllowed(false);

        header.setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel lbl = new JLabel(value != null ? value.toString().toUpperCase() : "", SwingConstants.CENTER);
                lbl.setOpaque(true);
                lbl.setBackground(BG_TABLE_HEADER);
                lbl.setForeground(new Color(255, 255, 255));
                lbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
                lbl.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0, 0, 2, 1, ACCENT_PURPLE),
                        new EmptyBorder(10, 8, 10, 8)
                ));
                return lbl;
            }
        });

        DefaultTableCellRenderer cellRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel lbl = (JLabel) super.getTableCellRendererComponent(t, value, isSelected, hasFocus, row, column);
                lbl.setHorizontalAlignment(JLabel.CENTER);
                lbl.setBorder(new EmptyBorder(4, 10, 4, 10));

                String valStr = value != null ? value.toString() : "";
                boolean isStatusColumn = valStr.equals("PERMITIDO") || valStr.equals("DENEGADO") ||
                                         valStr.equals("APROBADA") || valStr.equals("RECHAZADA") ||
                                         valStr.equals("PENDIENTE") || valStr.equals("EN_CURSO") ||
                                         valStr.equals("FINALIZADA") || valStr.equals("ACTIVO") ||
                                         valStr.equals("BLOQUEADO") || valStr.equals("CRITICO") ||
                                         valStr.equals("GRAVE") || valStr.equals("MODERADO");

                if (!isSelected) {
                    lbl.setBackground(row % 2 == 0 ? BG_CARD : BG_TABLE_ROW_ALT);
                    if (valStr.contains("PERMITIDO") || valStr.contains("ACTIVO") || valStr.contains("APROBADA") || valStr.contains("DENTRO") || valStr.contains("OPERATIVA")) {
                        lbl.setForeground(new Color(52, 211, 153)); // Soft Emerald
                        lbl.setFont(FONT_BODY_BOLD);
                    } else if (valStr.contains("DENEGADO") || valStr.contains("BLOQUEADO") || valStr.contains("RECHAZADA") || valStr.contains("ALERTA") || valStr.contains("CRITICO") || valStr.contains("GRAVE")) {
                        lbl.setForeground(new Color(251, 113, 133)); // Soft Rose Crimson
                        lbl.setFont(FONT_BODY_BOLD);
                    } else if (valStr.contains("PENDIENTE") || valStr.contains("MODERADO")) {
                        lbl.setForeground(new Color(251, 191, 36)); // Soft Amber
                        lbl.setFont(FONT_BODY_BOLD);
                    } else {
                        lbl.setForeground(TEXT_PRIMARY);
                        lbl.setFont(FONT_BODY);
                    }
                } else {
                    lbl.setBackground(new Color(139, 92, 246, 180));
                    lbl.setForeground(Color.WHITE);
                }
                return lbl;
            }
        };

        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(cellRenderer);
        }
    }
}
