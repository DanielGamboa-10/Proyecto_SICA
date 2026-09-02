package com.zonaacme.sica.ui.swing.panels;

import com.zonaacme.sica.core.adapters.InMemoryPersonaRepositoryAdapter;
import com.zonaacme.sica.core.adapters.InMemoryRegistroAccesoRepositoryAdapter;
import com.zonaacme.sica.core.adapters.InMemoryVisitaRepositoryAdapter;
import com.zonaacme.sica.core.domain.EstadoVisita;
import com.zonaacme.sica.core.domain.RegistroAcceso;
import com.zonaacme.sica.core.domain.ResultadoAcceso;
import com.zonaacme.sica.notifications.adapters.InMemoryNotificationRepositoryAdapter;
import com.zonaacme.sica.ui.swing.ThemeConstants;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class DashboardPanel extends JPanel {

    private final InMemoryVisitaRepositoryAdapter visitaRepo;
    private final InMemoryRegistroAccesoRepositoryAdapter registroAccesoRepo;
    private final InMemoryPersonaRepositoryAdapter personaRepo;
    private final InMemoryNotificationRepositoryAdapter notificationRepo;

    private JLabel lblVisitasActivas;
    private JLabel lblAccesosHoy;
    private JLabel lblAlertas;
    private JLabel lblPersonasTotal;
    private DefaultTableModel accesosTableModel;

    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm:ss");

    public DashboardPanel(
            InMemoryVisitaRepositoryAdapter visitaRepo,
            InMemoryRegistroAccesoRepositoryAdapter registroAccesoRepo,
            InMemoryPersonaRepositoryAdapter personaRepo,
            InMemoryNotificationRepositoryAdapter notificationRepo
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

        // Tarjetas de Métricas (Stats) con Gradientes
        JPanel statsPanel = new JPanel(new GridLayout(1, 4, 20, 0));
        statsPanel.setOpaque(false);

        lblVisitasActivas = new JLabel("0", SwingConstants.CENTER);
        lblAccesosHoy = new JLabel("0", SwingConstants.CENTER);
        lblAlertas = new JLabel("0", SwingConstants.CENTER);
        lblPersonasTotal = new JLabel("0", SwingConstants.CENTER);

        statsPanel.add(crearModernStatCard("Visitas Activas", lblVisitasActivas, new Color(6, 182, 212), new Color(59, 130, 246)));
        statsPanel.add(crearModernStatCard("Accesos Registrados", lblAccesosHoy, new Color(16, 185, 129), new Color(5, 150, 105)));
        statsPanel.add(crearModernStatCard("Alertas de Seguridad", lblAlertas, new Color(239, 68, 68), new Color(185, 28, 28)));
        statsPanel.add(crearModernStatCard("Personas Registradas", lblPersonasTotal, new Color(99, 102, 241), new Color(139, 92, 246)));

        // Tabla de Actividad Reciente de Accesos
        JPanel tableContainer = ThemeConstants.createCard();
        tableContainer.setLayout(new BorderLayout(0, 16));

        JPanel tableHeaderBar = new JPanel(new BorderLayout());
        tableHeaderBar.setOpaque(false);

        JLabel tableTitle = new JLabel("Últimos Movimientos Registrados en Torniquetes y Puntos de Control");
        tableTitle.setFont(ThemeConstants.FONT_SUBTITLE);
        tableTitle.setForeground(ThemeConstants.TEXT_PRIMARY);

        JButton btnRefrescar = ThemeConstants.createButton("Actualizar", ThemeConstants.BG_CARD_HOVER, ThemeConstants.TEXT_PRIMARY);
        btnRefrescar.addActionListener(e -> refrescarDatos());

        tableHeaderBar.add(tableTitle, BorderLayout.WEST);
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

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.getViewport().setBackground(ThemeConstants.BG_CARD);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        tableContainer.add(scrollPane, BorderLayout.CENTER);

        // Disposición General
        JPanel topContainer = new JPanel(new BorderLayout(0, 24));
        topContainer.setOpaque(false);
        topContainer.add(headerPanel, BorderLayout.NORTH);
        topContainer.add(statsPanel, BorderLayout.CENTER);

        add(topContainer, BorderLayout.NORTH);
        add(tableContainer, BorderLayout.CENTER);
    }

    private JPanel crearModernStatCard(String label, JLabel valueLabel, Color color1, Color color2) {
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Fondo elegante con degradado
                GradientPaint gp = new GradientPaint(0, 0, ThemeConstants.BG_CARD, 0, getHeight(), new Color(18, 25, 45));
                g2.setPaint(gp);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 18, 18));

                // Barra superior de acento coloreada
                GradientPaint barGp = new GradientPaint(0, 0, color1, getWidth(), 0, color2);
                g2.setPaint(barGp);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), 5, 5, 5));

                // Borde suave
                g2.setColor(ThemeConstants.BORDER_COLOR);
                g2.setStroke(new BasicStroke(1.2f));
                g2.draw(new RoundRectangle2D.Float(0, 0, getWidth() - 1, getHeight() - 1, 18, 18));

                g2.dispose();
                super.paintComponent(g);
            }
        };
        card.setOpaque(false);
        card.setLayout(new BorderLayout(12, 12));
        card.setBorder(new EmptyBorder(18, 22, 18, 22));

        JLabel textLabel = new JLabel(label, SwingConstants.CENTER);
        textLabel.setFont(ThemeConstants.FONT_HEADER);
        textLabel.setForeground(ThemeConstants.TEXT_SECONDARY);

        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 36));
        valueLabel.setForeground(color1);

        card.add(textLabel, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);

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
