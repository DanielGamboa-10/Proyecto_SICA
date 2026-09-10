package com.zonaacme.sica.ui.swing.panels;

import com.zonaacme.sica.core.domain.EstadoVisita;
import com.zonaacme.sica.core.domain.RegistroAcceso;
import com.zonaacme.sica.core.domain.ResultadoAcceso;
import com.zonaacme.sica.core.ports.out.PersonaRepositoryPort;
import com.zonaacme.sica.core.ports.out.RegistroAccesoRepositoryPort;
import com.zonaacme.sica.core.ports.out.VisitaRepositoryPort;
import com.zonaacme.sica.notifications.ports.out.NotificationRepositoryPort;
import com.zonaacme.sica.ui.swing.ThemeConstants;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class DashboardPanel extends JPanel {

    private final VisitaRepositoryPort visitaRepo;
    private final RegistroAccesoRepositoryPort registroAccesoRepo;
    private final PersonaRepositoryPort personaRepo;
    private final NotificationRepositoryPort notificationRepo;

    private JLabel lblVisitasActivas;
    private JLabel lblAccesosHoy;
    private JLabel lblAlertas;
    private JLabel lblPersonasTotal;
    private DefaultTableModel accesosTableModel;

    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm:ss");

    public DashboardPanel(
            VisitaRepositoryPort visitaRepo,
            RegistroAccesoRepositoryPort registroAccesoRepo,
            PersonaRepositoryPort personaRepo,
            NotificationRepositoryPort notificationRepo
    ) {
        this.visitaRepo = visitaRepo;
        this.registroAccesoRepo = registroAccesoRepo;
        this.personaRepo = personaRepo;
        this.notificationRepo = notificationRepo;

        setLayout(new BorderLayout(24, 24));
        setBackground(ThemeConstants.BG_DARK);
        setBorder(new EmptyBorder(28, 36, 28, 36));

        initUI();
        refrescarDatos();
        iniciarAutoRefresh();
    }

    private void iniciarAutoRefresh() {
        Timer timer = new Timer(2000, e -> {
            if (isShowing()) {
                refrescarDatos();
            }
        });
        timer.start();
    }

    private void initUI() {
        // Encabezado
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        JLabel title = new JLabel("Panel de Control y Monitoreo en Vivo");
        title.setFont(ThemeConstants.FONT_TITLE);
        title.setForeground(ThemeConstants.TEXT_PRIMARY);

        JLabel subtitle = new JLabel("Visualización en tiempo real del estado de seguridad y flujo de accesos en Zona Acme");
        subtitle.setFont(ThemeConstants.FONT_BODY);
        subtitle.setForeground(ThemeConstants.TEXT_SECONDARY);

        headerPanel.add(title, BorderLayout.NORTH);
        headerPanel.add(subtitle, BorderLayout.SOUTH);

        // Tarjetas de Métricas (Stats) con Gradientes e Iconografía
        JPanel statsPanel = new JPanel(new GridLayout(1, 4, 18, 0));
        statsPanel.setOpaque(false);

        lblVisitasActivas = new JLabel("0", SwingConstants.LEFT);
        lblAccesosHoy = new JLabel("0", SwingConstants.LEFT);
        lblAlertas = new JLabel("0", SwingConstants.LEFT);
        lblPersonasTotal = new JLabel("0", SwingConstants.LEFT);

        statsPanel.add(crearModernStatCard("Visitas Activas", lblVisitasActivas, "En instalaciones hoy", new Color(56, 189, 248), new Color(59, 130, 246), "👥"));
        statsPanel.add(crearModernStatCard("Accesos Registrados", lblAccesosHoy, "Eventos de paso", new Color(52, 211, 153), new Color(16, 185, 129), "✓"));
        statsPanel.add(crearModernStatCard("Alertas de Seguridad", lblAlertas, "Incidentes y bloqueos", new Color(251, 113, 133), new Color(239, 68, 68), "⚠"));
        statsPanel.add(crearModernStatCard("Personas Registradas", lblPersonasTotal, "Directorio maestro", new Color(192, 132, 252), new Color(147, 51, 234), "📇"));

        // Tabla de Actividad Reciente de Accesos
        JPanel tableContainer = ThemeConstants.createCard();
        tableContainer.setLayout(new BorderLayout(0, 16));

        JPanel tableHeaderBar = new JPanel(new BorderLayout());
        tableHeaderBar.setOpaque(false);

        JPanel titleWithBadge = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        titleWithBadge.setOpaque(false);

        JLabel liveDot = new JLabel("●");
        liveDot.setFont(new Font("Segoe UI", Font.BOLD, 14));
        liveDot.setForeground(ThemeConstants.ACCENT_CYAN);

        JLabel tableTitle = new JLabel("Monitoreo en Vivo — Últimos Movimientos en Torniquetes y Puntos de Control");
        tableTitle.setFont(ThemeConstants.FONT_SUBTITLE);
        tableTitle.setForeground(ThemeConstants.TEXT_PRIMARY);

        titleWithBadge.add(liveDot);
        titleWithBadge.add(tableTitle);

        JButton btnRefrescar = ThemeConstants.createGradientButton("↻  Actualizar", new Color(147, 51, 234), new Color(126, 34, 206), Color.WHITE);
        btnRefrescar.addActionListener(e -> refrescarDatos());

        tableHeaderBar.add(titleWithBadge, BorderLayout.WEST);
        tableHeaderBar.add(btnRefrescar, BorderLayout.EAST);
        tableContainer.add(tableHeaderBar, BorderLayout.NORTH);

        String[] columns = {"Hora", "Persona Identificada", "Punto de Control", "Sentido", "Resultado", "Observaciones / Diagnóstico"};
        accesosTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        JTable table = new JTable(accesosTableModel);
        ThemeConstants.styleTable(table);

        JScrollPane scrollPane = ThemeConstants.createScrollPane(table);
        tableContainer.add(scrollPane, BorderLayout.CENTER);

        // Disposición General
        JPanel topContainer = new JPanel(new BorderLayout(0, 24));
        topContainer.setOpaque(false);
        topContainer.add(headerPanel, BorderLayout.NORTH);
        topContainer.add(statsPanel, BorderLayout.CENTER);

        add(topContainer, BorderLayout.NORTH);
        add(tableContainer, BorderLayout.CENTER);
    }

    private JPanel crearModernStatCard(String label, JLabel valueLabel, String subtitle, Color color1, Color color2, String iconText) {
        JPanel card = new JPanel() {
            private float pulse = 0.0f;
            private javax.swing.Timer timer;
            {
                timer = new javax.swing.Timer(40, e -> {
                    pulse = (pulse + 0.05f) % (float)(2 * Math.PI);
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

                // Fondo Glassmorphism profundo con leve gradiente
                GradientPaint gp = new GradientPaint(0, 0, new Color(22, 16, 42), 0, h, new Color(13, 9, 25));
                g2.setPaint(gp);
                g2.fill(new RoundRectangle2D.Float(0, 0, w, h, 18, 18));

                // Resplandor ambiental respirante en la esquina de la métrica
                int glowAlpha = (int) (20 + 15 * Math.sin(pulse));
                g2.setColor(new Color(color1.getRed(), color1.getGreen(), color1.getBlue(), Math.max(5, glowAlpha)));
                g2.fillOval(w - 90, -30, 120, 120);

                // Barra superior de acento con gradiente y haz luminoso deslizante
                GradientPaint barGp = new GradientPaint(0, 0, color1, w, 0, color2);
                g2.setPaint(barGp);
                g2.fill(new RoundRectangle2D.Float(0, 0, w, 4, 4, 4));

                int beamX = (int) (w * (0.5f + 0.4f * Math.sin(pulse)));
                GradientPaint beam = new GradientPaint(
                        beamX - 30, 0, new Color(255, 255, 255, 0),
                        beamX, 0, new Color(255, 255, 255, 120),
                        true
                );
                g2.setPaint(beam);
                g2.fillRect(0, 0, w, 4);

                // Borde fino luminoso
                int borderAlpha = (int) (80 + 40 * Math.sin(pulse));
                g2.setColor(new Color(147, 51, 234, Math.min(255, borderAlpha)));
                g2.setStroke(new BasicStroke(1.2f));
                g2.draw(new RoundRectangle2D.Float(0, 0, w - 1, h - 1, 18, 18));

                g2.dispose();
                super.paintComponent(g);
            }
        };
        card.setOpaque(false);
        card.setLayout(new BorderLayout(10, 10));
        card.setBorder(new EmptyBorder(16, 20, 16, 20));

        // Header de la tarjeta (Título + Icono descriptivo)
        JPanel topRow = new JPanel(new BorderLayout());
        topRow.setOpaque(false);

        JLabel textLabel = new JLabel(label);
        textLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        textLabel.setForeground(ThemeConstants.TEXT_SECONDARY);

        // Icon Badge Container con micro-pulsación
        JPanel iconBadge = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 4)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(color1.getRed(), color1.getGreen(), color1.getBlue(), 35));
                g2.fillOval(0, 0, getWidth(), getHeight());
                g2.setColor(new Color(color1.getRed(), color1.getGreen(), color1.getBlue(), 140));
                g2.setStroke(new BasicStroke(1.1f));
                g2.drawOval(0, 0, getWidth() - 1, getHeight() - 1);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        iconBadge.setOpaque(false);
        iconBadge.setPreferredSize(new Dimension(36, 36));

        JLabel iconLbl = new JLabel(iconText, SwingConstants.CENTER);
        iconLbl.setFont(new Font("Segoe UI Emoji", Font.BOLD, 16));
        iconLbl.setForeground(color1);
        iconBadge.add(iconLbl);

        topRow.add(textLabel, BorderLayout.WEST);
        topRow.add(iconBadge, BorderLayout.EAST);

        // Cuerpo (Número grande + subtítulo descriptivo)
        JPanel centerPanel = new JPanel(new GridLayout(2, 1, 0, 0));
        centerPanel.setOpaque(false);

        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 34));
        valueLabel.setForeground(color1);

        JLabel subLabel = new JLabel(subtitle);
        subLabel.setFont(ThemeConstants.FONT_SMALL);
        subLabel.setForeground(ThemeConstants.TEXT_MUTED);

        centerPanel.add(valueLabel);
        centerPanel.add(subLabel);

        card.add(topRow, BorderLayout.NORTH);
        card.add(centerPanel, BorderLayout.CENTER);

        return card;
    }

    public void refrescarDatos() {
        long activas = visitaRepo.findAll().stream()
                .filter(v -> v.getEstado() == EstadoVisita.EN_CURSO)
                .count();
        lblVisitasActivas.setText(String.valueOf(activas));

        List<RegistroAcceso> accesos = registroAccesoRepo.findAll();
        lblAccesosHoy.setText(String.valueOf(accesos.size()));

        lblAlertas.setText(String.valueOf(notificationRepo.findAlertasSeguridad().size()));
        lblPersonasTotal.setText(String.valueOf(personaRepo.findAll().size()));

        // Actualizar tabla
        accesosTableModel.setRowCount(0);
        int start = Math.max(0, accesos.size() - 25);
        for (int i = accesos.size() - 1; i >= start; i--) {
            RegistroAcceso acc = accesos.get(i);
            String personaNombre = personaRepo.findById(acc.getPersonaId())
                    .map(p -> p.getNombreCompleto() + " (" + p.getTipoDocumento() + " " + p.getNumeroDocumento() + ")")
                    .orElse(acc.getPersonaId());
            String resultadoBadge = acc.getResultado() == ResultadoAcceso.PERMITIDO ? "PERMITIDO" : "DENEGADO";

            accesosTableModel.addRow(new Object[]{
                    acc.getFechaHora().format(TIME_FMT),
                    personaNombre,
                    acc.getPuntoControlId(),
                    acc.getTipoAcceso().name(),
                    resultadoBadge,
                    acc.getObservaciones()
            });
        }
    }
}
