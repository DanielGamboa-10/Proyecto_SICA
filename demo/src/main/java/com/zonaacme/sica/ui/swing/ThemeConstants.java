package com.zonaacme.sica.ui.swing;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

public final class ThemeConstants {

    private ThemeConstants() {}

    public static boolean isLightMode = false;

    // Paleta de Colores Dinámica (Soporta Modo Oscuro y Modo Claro)
    public static Color BG_DARK = new Color(9, 7, 18);              // Pure Deep Obsidian Black
    public static Color BG_SIDEBAR = new Color(13, 10, 25);         // Jet Amethyst Black
    public static Color BG_HEADER = new Color(18, 13, 34);          // Midnight Velvet Purple
    public static Color BG_CARD = new Color(22, 16, 42);            // Elevated Velvet Glass
    public static Color BG_CARD_HOVER = new Color(42, 26, 80);      // Purple Glow Hover
    public static Color BG_INPUT = new Color(12, 9, 24);            // Inset Dark Onyx
    public static Color BG_TABLE_HEADER = new Color(30, 20, 58);    // Royal Violet Header
    public static Color BG_TABLE_ROW_ALT = new Color(16, 12, 30);   // Alternating Onyx Row

    // Colores de Acento Vibrantes y Gradientes Neón
    public static Color ACCENT_PRIMARY = new Color(147, 51, 234);   // Electric Purple 600
    public static Color ACCENT_PURPLE = new Color(168, 85, 247);    // Neon Purple 500
    public static Color ACCENT_MAGENTA = new Color(217, 70, 239);   // Neon Fuchsia
    public static Color ACCENT_CYAN = new Color(56, 189, 248);      // Cyber Cyan
    public static Color ACCENT_SUCCESS = new Color(16, 185, 129);   // Emerald
    public static Color ACCENT_DANGER = new Color(244, 63, 94);     // Rose Neon
    public static Color ACCENT_WARNING = new Color(245, 158, 11);   // Amber Gold
    public static Color ACCENT_INFO = new Color(99, 102, 241);      // Indigo Neon

    // Textos
    public static Color TEXT_PRIMARY = new Color(250, 250, 255);    // Pure White
    public static Color TEXT_SECONDARY = new Color(216, 180, 254);  // Soft Lavender
    public static Color TEXT_MUTED = new Color(167, 139, 250);      // Muted Purple Slate
    public static Color BORDER_COLOR = new Color(92, 58, 158, 220); // Neon Violet Border
    public static Color BORDER_HIGHLIGHT = new Color(216, 180, 254, 200);

    /**
     * Alterna la paleta entre Modo Oscuro (Obsidian Neon) y Modo Claro (Pearl Slate Moderno de Alto Contraste).
     */
    public static void setLightMode(boolean light) {
        isLightMode = light;
        if (light) {
            BG_DARK = new Color(245, 247, 250);             // Clean Slate 50
            BG_SIDEBAR = new Color(255, 255, 255);          // Pure White
            BG_HEADER = new Color(255, 255, 255);           // Pure White
            BG_CARD = new Color(255, 255, 255);             // Crisp Card White
            BG_CARD_HOVER = new Color(243, 232, 255);       // Soft Purple Glow
            BG_INPUT = new Color(255, 255, 255);            // White Input
            BG_TABLE_HEADER = new Color(109, 40, 217);      // Deep Royal Purple 700
            BG_TABLE_ROW_ALT = new Color(248, 250, 252);    // Very light slate alt row

            TEXT_PRIMARY = new Color(15, 23, 42);           // Ultra Crisp Slate 900
            TEXT_SECONDARY = new Color(51, 65, 85);         // High Contrast Slate 700
            TEXT_MUTED = new Color(100, 116, 139);          // Slate 500
            BORDER_COLOR = new Color(226, 232, 240);        // Slate 200
            BORDER_HIGHLIGHT = new Color(147, 51, 234, 220);
        } else {
            BG_DARK = new Color(9, 7, 18);
            BG_SIDEBAR = new Color(13, 10, 25);
            BG_HEADER = new Color(18, 13, 34);
            BG_CARD = new Color(22, 16, 42);
            BG_CARD_HOVER = new Color(42, 26, 80);
            BG_INPUT = new Color(12, 9, 24);
            BG_TABLE_HEADER = new Color(30, 20, 58);
            BG_TABLE_ROW_ALT = new Color(16, 12, 30);

            TEXT_PRIMARY = new Color(250, 250, 255);
            TEXT_SECONDARY = new Color(216, 180, 254);
            TEXT_MUTED = new Color(167, 139, 250);
            BORDER_COLOR = new Color(92, 58, 158, 220);
            BORDER_HIGHLIGHT = new Color(216, 180, 254, 200);
        }
    }

    /**
     * Recorre recursivamente un contenedor Swing y sincroniza colores de fondo, tablas, textos y scrollpanes.
     */
    public static void aplicarTemaRecursivo(Component comp) {
        if (comp == null) return;

        if (comp instanceof JTable) {
            styleTable((JTable) comp);
        } else if (comp instanceof JScrollPane) {
            styleScrollPane((JScrollPane) comp);
        } else if (comp instanceof JTextField) {
            JTextField tf = (JTextField) comp;
            tf.setBackground(BG_INPUT);
            tf.setForeground(TEXT_PRIMARY);
            tf.setCaretColor(ACCENT_PURPLE);
            tf.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(BORDER_COLOR, 1, true),
                    new EmptyBorder(10, 12, 10, 12)
            ));
        } else if (comp instanceof JTextArea) {
            comp.setBackground(BG_INPUT);
            comp.setForeground(TEXT_PRIMARY);
        } else if (comp instanceof JComboBox) {
            styleComboBox((JComboBox<?>) comp);
        } else if (comp instanceof JLabel) {
            JLabel lbl = (JLabel) comp;
            Color fg = lbl.getForeground();
            if (fg != null && (fg.equals(ACCENT_SUCCESS) || fg.equals(ACCENT_DANGER) || fg.equals(ACCENT_WARNING) || fg.equals(ACCENT_CYAN) || fg.equals(Color.WHITE))) {
                // conservar badge
            } else {
                lbl.setForeground(TEXT_PRIMARY);
            }
        } else if (comp instanceof JPanel) {
            JPanel p = (JPanel) comp;
            if (p.isOpaque()) {
                p.setBackground(BG_DARK);
            }
        }

        if (comp instanceof Container) {
            for (Component child : ((Container) comp).getComponents()) {
                aplicarTemaRecursivo(child);
            }
        }
    }

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

                // Fondo de tarjeta
                Color bottomColor = isLightMode ? new Color(248, 250, 252) : new Color(14, 10, 28);
                GradientPaint gp = new GradientPaint(0, 0, BG_CARD, 0, h, bottomColor);
                g2.setPaint(gp);
                g2.fill(new RoundRectangle2D.Float(0, 0, w, h, 18, 18));

                // Haz sutil superior viajando
                int gleamX = (int) (w * (0.5f + 0.4f * Math.sin(pulse)));
                GradientPaint topGleam = new GradientPaint(
                        gleamX - 60, 0, new Color(168, 85, 247, 0),
                        gleamX, 0, new Color(217, 70, 239, isLightMode ? 90 : 130),
                        true
                );
                g2.setPaint(topGleam);
                g2.fillRect(0, 0, w, 2);

                // Borde con resplandor neón elegante
                int bAlpha = (int) (140 + 40 * Math.sin(pulse));
                Color bCol = isLightMode ? new Color(168, 85, 247, Math.min(255, bAlpha)) : new Color(147, 51, 234, Math.min(255, bAlpha));
                g2.setColor(bCol);
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
                JButton btn = new JButton() {
                    @Override
                    protected void paintComponent(Graphics g) {
                        Graphics2D g2 = (Graphics2D) g.create();
                        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                        g2.setColor(BG_INPUT);
                        g2.fillRect(0, 0, getWidth(), getHeight());

                        // Dibujar flecha chevron centrada y de color contrastante
                        int cx = getWidth() / 2;
                        int cy = getHeight() / 2;
                        g2.setColor(isLightMode ? new Color(79, 70, 229) : ACCENT_PURPLE);
                        g2.setStroke(new BasicStroke(2.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                        g2.drawLine(cx - 5, cy - 2, cx, cy + 3);
                        g2.drawLine(cx, cy + 3, cx + 5, cy - 2);
                        g2.dispose();
                    }
                };
                btn.setContentAreaFilled(false);
                btn.setBorderPainted(false);
                btn.setFocusPainted(false);
                btn.setOpaque(false);
                btn.setBorder(BorderFactory.createEmptyBorder(0, 4, 0, 8));
                btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
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
        table.setGridColor(isLightMode ? new Color(226, 232, 240) : new Color(56, 38, 96));
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
                lbl.setForeground(Color.WHITE);
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

                if (!isSelected) {
                    lbl.setBackground(row % 2 == 0 ? BG_CARD : BG_TABLE_ROW_ALT);
                    if (valStr.contains("PERMITIDO") || valStr.contains("ACTIVO") || valStr.contains("APROBADA") || valStr.contains("DENTRO") || valStr.contains("OPERATIVA")) {
                        lbl.setForeground(isLightMode ? new Color(16, 185, 129) : new Color(52, 211, 153)); // Emerald
                        lbl.setFont(FONT_BODY_BOLD);
                    } else if (valStr.contains("DENEGADO") || valStr.contains("BLOQUEADO") || valStr.contains("RECHAZADA") || valStr.contains("ALERTA") || valStr.contains("CRITICO") || valStr.contains("GRAVE")) {
                        lbl.setForeground(isLightMode ? new Color(225, 29, 72) : new Color(251, 113, 133)); // Rose Crimson
                        lbl.setFont(FONT_BODY_BOLD);
                    } else if (valStr.contains("PENDIENTE") || valStr.contains("MODERADO")) {
                        lbl.setForeground(isLightMode ? new Color(217, 119, 6) : new Color(251, 191, 36)); // Amber
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

    /**
     * KEY_BUSCADOR_REALTIME: Conecta un campo de búsqueda en vivo con un JTable mediante TableRowSorter y DocumentListener.
     */
    public static void instalarBuscadorDinamico(JTextField searchField, JTable table) {
        if (table.getModel() instanceof javax.swing.table.TableModel) {
            @SuppressWarnings("unchecked")
            javax.swing.table.TableRowSorter<javax.swing.table.TableModel> sorter =
                    new javax.swing.table.TableRowSorter<>(table.getModel());
            table.setRowSorter(sorter);

            searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
                private void filtrar() {
                    String texto = searchField.getText().trim();
                    if (texto.isEmpty()) {
                        sorter.setRowFilter(null);
                    } else {
                        // Búsqueda regex no case-sensitive ("(?i)" + regex)
                        sorter.setRowFilter(javax.swing.RowFilter.regexFilter("(?i)" + java.util.regex.Pattern.quote(texto)));
                    }
                }

                @Override
                public void insertUpdate(javax.swing.event.DocumentEvent e) { filtrar(); }

                @Override
                public void removeUpdate(javax.swing.event.DocumentEvent e) { filtrar(); }

                @Override
                public void changedUpdate(javax.swing.event.DocumentEvent e) { filtrar(); }
            });
        }
    }
}
