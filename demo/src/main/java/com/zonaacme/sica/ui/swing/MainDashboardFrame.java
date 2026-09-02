package com.zonaacme.sica.ui.swing;

import com.zonaacme.sica.audit.adapters.AuditService;
import com.zonaacme.sica.audit.adapters.InMemoryAuditRepositoryAdapter;
import com.zonaacme.sica.auth.adapters.AuthService;
import com.zonaacme.sica.auth.adapters.InMemoryUsuarioRepositoryAdapter;
import com.zonaacme.sica.auth.domain.SesionUsuario;
import com.zonaacme.sica.core.adapters.*;
import com.zonaacme.sica.notifications.adapters.InMemoryNotificationRepositoryAdapter;
import com.zonaacme.sica.notifications.adapters.NotificationService;
import com.zonaacme.sica.ui.swing.panels.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.List;

public class MainDashboardFrame extends JFrame {

    private final AuthService authService;
    private final VisitaService visitaService;
    private final ControlAccesoService controlAccesoService;
    private final AuditService auditService;
    private final NotificationService notificationService;

    private final InMemoryUsuarioRepositoryAdapter usuarioRepo;
    private final InMemoryPersonaRepositoryAdapter personaRepo;
    private final InMemoryZonaRepositoryAdapter zonaRepo;
    private final InMemoryVisitaRepositoryAdapter visitaRepo;
    private final InMemoryRegistroAccesoRepositoryAdapter registroAccesoRepo;
    private final InMemoryAuditRepositoryAdapter auditRepo;
    private final InMemoryNotificationRepositoryAdapter notificationRepo;

    private SesionUsuario sesionUsuario;

    private JPanel cardPanel;
    private CardLayout cardLayout;
    private List<JButton> sidebarButtons = new ArrayList<>();

    private DashboardPanel dashboardPanel;
    private ControlAccesoPanel controlAccesoPanel;
    private VisitasPanel visitasPanel;
    private PersonasZonasPanel personasZonasPanel;
    private AuditoriaPanel auditoriaPanel;
    private NotificacionesPanel notificacionesPanel;

    public MainDashboardFrame(
            AuthService authService,
            VisitaService visitaService,
            ControlAccesoService controlAccesoService,
            AuditService auditService,
            NotificationService notificationService,
            InMemoryUsuarioRepositoryAdapter usuarioRepo,
            InMemoryPersonaRepositoryAdapter personaRepo,
            InMemoryZonaRepositoryAdapter zonaRepo,
            InMemoryVisitaRepositoryAdapter visitaRepo,
            InMemoryRegistroAccesoRepositoryAdapter registroAccesoRepo,
            InMemoryAuditRepositoryAdapter auditRepo,
            InMemoryNotificationRepositoryAdapter notificationRepo,
            SesionUsuario sesionUsuario
    ) {
        this.authService = authService;
        this.visitaService = visitaService;
        this.controlAccesoService = controlAccesoService;
        this.auditService = auditService;
        this.notificationService = notificationService;
        this.usuarioRepo = usuarioRepo;
        this.personaRepo = personaRepo;
        this.zonaRepo = zonaRepo;
        this.visitaRepo = visitaRepo;
        this.registroAccesoRepo = registroAccesoRepo;
        this.auditRepo = auditRepo;
        this.notificationRepo = notificationRepo;
        this.sesionUsuario = sesionUsuario;

        setTitle("SICA — Sistema Integrado de Control de Acceso | Complejo Empresarial Zona Acme");
        
        // PANTALLA COMPLETA / MAXIMIZADA
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setMinimumSize(new Dimension(1200, 750));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        getContentPane().setBackground(ThemeConstants.BG_DARK);

        initUI();
    }

    private void initUI() {
        setLayout(new BorderLayout());

        // Header Superior Estilizado con Glassmorphism
        JPanel header = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                GradientPaint gp = new GradientPaint(0, 0, new Color(15, 23, 42), getWidth(), 0, new Color(24, 32, 54));
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.setColor(ThemeConstants.BORDER_COLOR);
                g2.drawLine(0, getHeight() - 1, getWidth(), getHeight() - 1);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        header.setOpaque(false);
        header.setPreferredSize(new Dimension(0, 75));
        header.setBorder(new EmptyBorder(12, 28, 12, 28));

        // Brand Logo
        JPanel brandPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 4));
        brandPanel.setOpaque(false);

        JPanel brandText = new JPanel(new GridLayout(2, 1, 0, 0));
        brandText.setOpaque(false);
        JLabel titleLabel = new JLabel("SICA — ZONA ACME");
        titleLabel.setFont(ThemeConstants.FONT_TITLE);
        titleLabel.setForeground(ThemeConstants.TEXT_PRIMARY);

        JLabel subtitleLabel = new JLabel("SISTEMA INTEGRADO DE CONTROL DE ACCESO");
        subtitleLabel.setFont(new Font("Segoe UI", Font.BOLD, 10));
        subtitleLabel.setForeground(ThemeConstants.ACCENT_CYAN);

        brandText.add(titleLabel);
        brandText.add(subtitleLabel);
        brandPanel.add(brandText);

        // User Info & Actions
        JPanel userPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 16, 6));
        userPanel.setOpaque(false);

        // Status pill
        JPanel statusPill = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 2));
        statusPill.setBackground(new Color(16, 185, 129, 30));
        statusPill.setBorder(BorderFactory.createLineBorder(ThemeConstants.ACCENT_SUCCESS, 1, true));
        JLabel statusDot = new JLabel("●");
        statusDot.setForeground(ThemeConstants.ACCENT_SUCCESS);
        JLabel statusText = new JLabel("EN VIVO");
        statusText.setFont(ThemeConstants.FONT_SMALL);
        statusText.setForeground(ThemeConstants.ACCENT_SUCCESS);
        statusPill.add(statusDot);
        statusPill.add(statusText);

        JLabel userBadge = new JLabel("Usuario: " + sesionUsuario.getUsername() + "  [" + sesionUsuario.getRol().name() + "]");
        userBadge.setFont(ThemeConstants.FONT_BODY_BOLD);
        userBadge.setForeground(ThemeConstants.TEXT_PRIMARY);

        JButton btnLogout = ThemeConstants.createGradientButton(
                "Cerrar Sesión",
                new Color(239, 68, 68),
                new Color(185, 28, 28),
                Color.WHITE
        );
        btnLogout.addActionListener(e -> {
            authService.cerrarSesion(sesionUsuario.getToken());
            dispose();
            SwingUtilities.invokeLater(() -> {
                LoginFrame loginFrame = new LoginFrame(
                        authService, visitaService, controlAccesoService, auditService, notificationService,
                        usuarioRepo, personaRepo, zonaRepo, visitaRepo, registroAccesoRepo, auditRepo, notificationRepo
                );
                loginFrame.setExtendedState(JFrame.MAXIMIZED_BOTH);
                loginFrame.setVisible(true);
            });
        });

        userPanel.add(statusPill);
        userPanel.add(userBadge);
        userPanel.add(btnLogout);

        header.add(brandPanel, BorderLayout.WEST);
        header.add(userPanel, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        // Sidebar Izquierda
        JPanel sidebar = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                GradientPaint gp = new GradientPaint(0, 0, new Color(13, 19, 33), 0, getHeight(), new Color(9, 13, 23));
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.setColor(ThemeConstants.BORDER_COLOR);
                g2.drawLine(getWidth() - 1, 0, getWidth() - 1, getHeight());
                g2.dispose();
                super.paintComponent(g);
            }
        };
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setOpaque(false);
        sidebar.setPreferredSize(new Dimension(260, 0));
        sidebar.setBorder(new EmptyBorder(24, 16, 24, 16));

        cardLayout = new CardLayout();
        cardPanel = new JPanel(cardLayout);
        cardPanel.setBackground(ThemeConstants.BG_DARK);

        // Instanciar Paneles Visuales
        dashboardPanel = new DashboardPanel(visitaRepo, registroAccesoRepo, personaRepo, notificationRepo);
        controlAccesoPanel = new ControlAccesoPanel(controlAccesoService, personaRepo, zonaRepo, sesionUsuario);
        controlAccesoPanel.setOnAccessRegisteredCallback(() -> {
            dashboardPanel.refrescarDatos();
            auditoriaPanel.cargarAuditoria();
            notificacionesPanel.cargarNotificaciones();
        });

        visitasPanel = new VisitasPanel(visitaService, visitaRepo, personaRepo, zonaRepo, sesionUsuario);
        personasZonasPanel = new PersonasZonasPanel(personaRepo, zonaRepo);
        auditoriaPanel = new AuditoriaPanel(auditRepo);
        notificacionesPanel = new NotificacionesPanel(notificationService, notificationRepo);

        cardPanel.add(dashboardPanel, "DASHBOARD");
        cardPanel.add(controlAccesoPanel, "CONTROL_ACCESO");
        cardPanel.add(visitasPanel, "VISITAS");
        cardPanel.add(personasZonasPanel, "PERSONAS_ZONAS");
        cardPanel.add(auditoriaPanel, "AUDITORIA");
        cardPanel.add(notificacionesPanel, "NOTIFICACIONES");

        // Botones del Sidebar
        sidebar.add(crearNavButton("Dashboard General", "DASHBOARD", true));
        sidebar.add(Box.createRigidArea(new Dimension(0, 10)));
        sidebar.add(crearNavButton("Control de Accesos", "CONTROL_ACCESO", false));
        sidebar.add(Box.createRigidArea(new Dimension(0, 10)));
        sidebar.add(crearNavButton("Gestión de Visitas", "VISITAS", false));
        sidebar.add(Box.createRigidArea(new Dimension(0, 10)));
        sidebar.add(crearNavButton("Personas y Zonas", "PERSONAS_ZONAS", false));
        sidebar.add(Box.createRigidArea(new Dimension(0, 10)));
        sidebar.add(crearNavButton("Bitácora Auditoría", "AUDITORIA", false));
        sidebar.add(Box.createRigidArea(new Dimension(0, 10)));
        sidebar.add(crearNavButton("Centro de Alertas", "NOTIFICACIONES", false));

        sidebar.add(Box.createVerticalGlue());

        JLabel versionLabel = new JLabel("SICA v1.0.0 Enterprise • Java 25", SwingConstants.CENTER);
        versionLabel.setFont(ThemeConstants.FONT_SMALL);
        versionLabel.setForeground(ThemeConstants.TEXT_MUTED);
        versionLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        sidebar.add(versionLabel);

        add(sidebar, BorderLayout.WEST);
        add(cardPanel, BorderLayout.CENTER);
    }

    private JButton crearNavButton(String label, String cardName, boolean active) {
        JButton btn = new JButton(label) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                boolean isActive = (getBackground().equals(ThemeConstants.ACCENT_PRIMARY));
                if (isActive) {
                    GradientPaint gp = new GradientPaint(0, 0, ThemeConstants.ACCENT_PRIMARY, getWidth(), 0, ThemeConstants.ACCENT_CYAN);
                    g2.setPaint(gp);
                    g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 12, 12));
                } else if (getModel().isRollover()) {
                    g2.setColor(ThemeConstants.BG_CARD_HOVER);
                    g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 12, 12));
                }
                g2.dispose();
                super.paintComponent(g);
            }
        };

        btn.setFont(ThemeConstants.FONT_BODY_BOLD);
        btn.setForeground(active ? Color.WHITE : ThemeConstants.TEXT_SECONDARY);
        btn.setBackground(active ? ThemeConstants.ACCENT_PRIMARY : ThemeConstants.BG_SIDEBAR);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(12, 18, 12, 18));
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        btn.setHorizontalAlignment(SwingConstants.LEFT);

        btn.addActionListener(e -> {
            for (JButton b : sidebarButtons) {
                b.setBackground(ThemeConstants.BG_SIDEBAR);
                b.setForeground(ThemeConstants.TEXT_SECONDARY);
            }
            btn.setBackground(ThemeConstants.ACCENT_PRIMARY);
            btn.setForeground(Color.WHITE);
            cardLayout.show(cardPanel, cardName);

            // Refrescar datos
            if ("DASHBOARD".equals(cardName)) dashboardPanel.refrescarDatos();
            if ("VISITAS".equals(cardName)) visitasPanel.cargarVisitas();
            if ("PERSONAS_ZONAS".equals(cardName)) { personasZonasPanel.cargarPersonas(); personasZonasPanel.cargarZonas(); }
            if ("AUDITORIA".equals(cardName)) auditoriaPanel.cargarAuditoria();
            if ("NOTIFICACIONES".equals(cardName)) notificacionesPanel.cargarNotificaciones();
        });

        sidebarButtons.add(btn);
        return btn;
    }
}
