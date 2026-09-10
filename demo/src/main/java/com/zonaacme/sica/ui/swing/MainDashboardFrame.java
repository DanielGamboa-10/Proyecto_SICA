package com.zonaacme.sica.ui.swing;

import com.zonaacme.sica.audit.adapters.AuditService;
import com.zonaacme.sica.audit.ports.out.AuditRepositoryPort;
import com.zonaacme.sica.auth.adapters.AuthService;
import com.zonaacme.sica.auth.domain.Rol;
import com.zonaacme.sica.auth.domain.SesionUsuario;
import com.zonaacme.sica.auth.ports.out.UsuarioRepositoryPort;
import com.zonaacme.sica.core.adapters.ControlAccesoService;
import com.zonaacme.sica.core.adapters.VisitaService;
import com.zonaacme.sica.core.ports.out.*;
import com.zonaacme.sica.notifications.adapters.NotificationService;
import com.zonaacme.sica.notifications.ports.out.NotificationRepositoryPort;
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

    private final UsuarioRepositoryPort usuarioRepo;
    private final PersonaRepositoryPort personaRepo;
    private final ZonaRepositoryPort zonaRepo;
    private final VisitaRepositoryPort visitaRepo;
    private final RegistroAccesoRepositoryPort registroAccesoRepo;
    private final AuditRepositoryPort auditRepo;
    private final NotificationRepositoryPort notificationRepo;
    private final IncidenteRepositoryPort incidenteRepo;

    private SesionUsuario sesionUsuario;

    private JPanel cardPanel;
    private CardLayout cardLayout;
    private List<JButton> sidebarButtons = new ArrayList<>();

    private DashboardPanel dashboardPanel;
    private ControlAccesoPanel controlAccesoPanel;
    private VisitasPanel visitasPanel;
    private PersonasZonasPanel personasZonasPanel;
    private IncidentesPanel incidentesPanel;
    private ReportesPanel reportesPanel;
    private AuditoriaPanel auditoriaPanel;
    private NotificacionesPanel notificacionesPanel;
    private HilosConcurrenciaPanel hilosConcurrenciaPanel;

    public MainDashboardFrame(
            AuthService authService,
            VisitaService visitaService,
            ControlAccesoService controlAccesoService,
            AuditService auditService,
            NotificationService notificationService,
            UsuarioRepositoryPort usuarioRepo,
            PersonaRepositoryPort personaRepo,
            ZonaRepositoryPort zonaRepo,
            VisitaRepositoryPort visitaRepo,
            RegistroAccesoRepositoryPort registroAccesoRepo,
            AuditRepositoryPort auditRepo,
            NotificationRepositoryPort notificationRepo,
            IncidenteRepositoryPort incidenteRepo,
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
        this.incidenteRepo = incidenteRepo;
        this.sesionUsuario = sesionUsuario;

        setTitle("SICA — Sistema Integrado de Control de Acceso | Complejo Empresarial Zona Acme");
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setMinimumSize(new Dimension(1200, 750));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        getContentPane().setBackground(ThemeConstants.BG_DARK);

        initUI();
    }

    private void initUI() {
        setLayout(new BorderLayout());

        // Header Superior con haz de luz dinámico animado (Multi-layered Cyber Wave)
        JPanel header = new JPanel(new BorderLayout()) {
            private float animPhase = 0.0f;
            private javax.swing.Timer animTimer;
            {
                animTimer = new javax.swing.Timer(30, e -> {
                    animPhase = (animPhase + 0.035f) % (float)(2 * Math.PI);
                    repaint();
                });
                animTimer.start();
            }

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int w = getWidth();
                int h = getHeight();

                // Fondo negro obsidiana a púrpura profundo
                GradientPaint gp = new GradientPaint(0, 0, ThemeConstants.BG_HEADER, w, 0, new Color(24, 14, 46));
                g2.setPaint(gp);
                g2.fillRect(0, 0, w, h);

                // Haz de luz animado 1 (Neon Magenta Wave)
                int beam1X = (int) (w * (0.5f + 0.45f * Math.sin(animPhase)));
                GradientPaint beam1 = new GradientPaint(
                        beam1X - 180, h - 2, new Color(147, 51, 234, 0),
                        beam1X, h - 2, new Color(217, 70, 239, 240),
                        true
                );
                g2.setPaint(beam1);
                g2.fillRect(0, h - 3, w, 3);

                // Haz de luz animado 2 (Cyber Cyan Echo)
                int beam2X = (int) (w * (0.5f - 0.45f * Math.cos(animPhase * 0.9)));
                GradientPaint beam2 = new GradientPaint(
                        beam2X - 120, h - 2, new Color(56, 189, 248, 0),
                        beam2X, h - 2, new Color(56, 189, 248, 190),
                        true
                );
                g2.setPaint(beam2);
                g2.fillRect(0, h - 2, w, 2);

                g2.dispose();
                super.paintComponent(g);
            }
        };
        header.setOpaque(false);
        header.setPreferredSize(new Dimension(0, 75));
        header.setBorder(new EmptyBorder(12, 28, 12, 28));

        // Brand Logo con Isotipo de Seguridad Moderno y Aura
        JPanel brandPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 14, 4));
        brandPanel.setOpaque(false);

        // Isotipo Vectorial de Seguridad con micro-pulsación
        JPanel logoIcon = new JPanel() {
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

                // Aura ambiental pulsante
                int auraAlpha = (int) (40 + 30 * Math.sin(pulse));
                g2.setColor(new Color(147, 51, 234, Math.max(10, auraAlpha)));
                g2.fillRoundRect(0, 0, w, h, 16, 16);

                // Fondo del isotipo (Gradiente púrpura/magenta vibrante)
                GradientPaint bgGp = new GradientPaint(0, 0, ThemeConstants.ACCENT_PRIMARY, w, h, ThemeConstants.ACCENT_MAGENTA);
                g2.setPaint(bgGp);
                g2.fill(new RoundRectangle2D.Float(3, 3, w - 6, h - 6, 12, 12));

                // Borde resplandeciente
                g2.setColor(new Color(255, 255, 255, 140));
                g2.setStroke(new BasicStroke(1.4f));
                g2.draw(new RoundRectangle2D.Float(3, 3, w - 7, h - 7, 12, 12));

                // Escudo central vector
                int cx = w / 2;
                int cy = h / 2 - 1;
                Polygon shield = new Polygon();
                shield.addPoint(cx - 9, cy - 8);
                shield.addPoint(cx + 9, cy - 8);
                shield.addPoint(cx + 9, cy + 2);
                shield.addPoint(cx, cy + 10);
                shield.addPoint(cx - 9, cy + 2);

                g2.setColor(Color.WHITE);
                g2.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2.draw(shield);

                // Llave digital / cerradura en el interior
                g2.setColor(new Color(56, 189, 248)); // Cyan Neón
                g2.fillOval(cx - 3, cy - 4, 6, 6);
                g2.fillRect(cx - 1, cy + 1, 3, 4);

                g2.dispose();
                super.paintComponent(g);
            }
        };
        logoIcon.setPreferredSize(new Dimension(44, 44));
        logoIcon.setOpaque(false);

        JPanel brandText = new JPanel(new GridLayout(2, 1, 0, 2));
        brandText.setOpaque(false);
        JLabel titleLabel = new JLabel("SICA — ZONA ACME");
        titleLabel.setFont(ThemeConstants.FONT_TITLE);
        titleLabel.setForeground(ThemeConstants.TEXT_PRIMARY);

        JLabel subtitleLabel = new JLabel("CONTROL DE ACCESO & SEGURIDAD DIGITAL • COMPLEJO EMPRESARIAL");
        subtitleLabel.setFont(new Font("Segoe UI", Font.BOLD, 10));
        subtitleLabel.setForeground(ThemeConstants.ACCENT_PURPLE);

        brandText.add(titleLabel);
        brandText.add(subtitleLabel);

        brandPanel.add(logoIcon);
        brandPanel.add(brandText);

        // User Info & Actions
        JPanel userPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 14, 6));
        userPanel.setOpaque(false);

        // Status pill con micro-animación de radar expansivo en tiempo real
        JPanel statusPill = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 4)) {
            private float pulse = 0.0f;
            private javax.swing.Timer pulseTimer;
            {
                pulseTimer = new javax.swing.Timer(30, e -> {
                    pulse = (pulse + 0.05f) % (float)(2 * Math.PI);
                    repaint();
                });
                pulseTimer.start();
            }
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int w = getWidth();
                int h = getHeight();

                // Fondo con pulso esmeralda
                int bgAlpha = (int) (25 + 25 * Math.sin(pulse));
                g2.setColor(new Color(16, 185, 129, Math.max(10, bgAlpha)));
                g2.fillRoundRect(0, 0, w, h, 16, 16);

                // Ondas concéntricas de radar expansivo en el borde
                int rGlow = (int) (140 + 90 * Math.sin(pulse));
                g2.setColor(new Color(16, 185, 129, Math.min(255, rGlow)));
                g2.setStroke(new BasicStroke(1.3f));
                g2.drawRoundRect(0, 0, w - 1, h - 1, 16, 16);

                g2.dispose();
                super.paintComponent(g);
            }
        };
        statusPill.setOpaque(false);
        JLabel statusDot = new JLabel("●");
        statusDot.setFont(new Font("Segoe UI", Font.BOLD, 12));
        statusDot.setForeground(ThemeConstants.ACCENT_SUCCESS);
        JLabel statusText = new JLabel("MYSQL LIVE");
        statusText.setFont(new Font("Segoe UI", Font.BOLD, 11));
        statusText.setForeground(ThemeConstants.ACCENT_SUCCESS);
        statusPill.add(statusDot);
        statusPill.add(statusText);

        // Contenedor de Usuario Activo con Tarjeta Glassmorphism y Rol en Chip con Breathing Glow
        JPanel userBadgeCard = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 4)) {
            private float userPulse = 0.0f;
            private javax.swing.Timer userTimer;
            {
                userTimer = new javax.swing.Timer(40, e -> {
                    userPulse = (userPulse + 0.05f) % (float)(2 * Math.PI);
                    repaint();
                });
                userTimer.start();
            }

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(30, 20, 55, 200));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);

                int borderAlpha = (int) (120 + 80 * Math.sin(userPulse));
                g2.setColor(new Color(147, 51, 234, Math.min(255, borderAlpha)));
                g2.setStroke(new BasicStroke(1.2f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 14, 14);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        userBadgeCard.setOpaque(false);
        userBadgeCard.setBorder(new EmptyBorder(2, 10, 2, 10));

        JLabel userIcon = new JLabel("👤");
        userIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 13));
        userIcon.setForeground(ThemeConstants.TEXT_SECONDARY);

        JLabel userNameLabel = new JLabel(sesionUsuario.getUsername());
        userNameLabel.setFont(ThemeConstants.FONT_BODY_BOLD);
        userNameLabel.setForeground(ThemeConstants.TEXT_PRIMARY);

        JLabel roleChip = new JLabel(sesionUsuario.getRol().getNombreLegible());
        roleChip.setFont(new Font("Segoe UI", Font.BOLD, 10));
        roleChip.setForeground(ThemeConstants.ACCENT_CYAN);

        userBadgeCard.add(userIcon);
        userBadgeCard.add(userNameLabel);
        userBadgeCard.add(new JLabel("•"));
        userBadgeCard.add(roleChip);

        JButton btnLogout = ThemeConstants.createGradientButton(
                "Cerrar Sesión",
                new Color(244, 63, 94),
                new Color(190, 18, 60),
                Color.WHITE
        );
        btnLogout.addActionListener(e -> {
            authService.cerrarSesion(sesionUsuario.getToken());
            dispose();
            SwingUtilities.invokeLater(() -> {
                LoginFrame loginFrame = new LoginFrame(
                        authService, visitaService, controlAccesoService, auditService, notificationService,
                        usuarioRepo, personaRepo, zonaRepo, visitaRepo, registroAccesoRepo, auditRepo, notificationRepo, incidenteRepo
                );
                loginFrame.setExtendedState(JFrame.MAXIMIZED_BOTH);
                loginFrame.setVisible(true);
            });
        });

        userPanel.add(statusPill);
        userPanel.add(userBadgeCard);
        userPanel.add(btnLogout);

        header.add(brandPanel, BorderLayout.WEST);
        header.add(userPanel, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        // Sidebar Izquierda con gradiente negro obsidiana a púrpura noche
        JPanel sidebar = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                GradientPaint gp = new GradientPaint(0, 0, ThemeConstants.BG_SIDEBAR, 0, getHeight(), new Color(8, 6, 16));
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
        sidebar.setPreferredSize(new Dimension(270, 0));
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
            reportesPanel.generarReporteSeleccionado();
        });

        visitasPanel = new VisitasPanel(visitaService, visitaRepo, personaRepo, zonaRepo, sesionUsuario);
        personasZonasPanel = new PersonasZonasPanel(personaRepo, zonaRepo, sesionUsuario);
        incidentesPanel = new IncidentesPanel(
                incidenteRepo,
                personaRepo,
                notificationService,
                auditRepo,
                sesionUsuario
        );
        reportesPanel = new ReportesPanel(personaRepo, zonaRepo, visitaRepo, registroAccesoRepo, incidenteRepo);
        auditoriaPanel = new AuditoriaPanel(auditRepo);
        notificacionesPanel = new NotificacionesPanel(notificationService, notificationRepo);
        hilosConcurrenciaPanel = new HilosConcurrenciaPanel();

        cardPanel.add(dashboardPanel, "DASHBOARD");
        cardPanel.add(controlAccesoPanel, "CONTROL_ACCESO");
        cardPanel.add(visitasPanel, "VISITAS");
        cardPanel.add(personasZonasPanel, "PERSONAS_ZONAS");
        cardPanel.add(incidentesPanel, "INCIDENTES");
        cardPanel.add(reportesPanel, "REPORTES");
        cardPanel.add(auditoriaPanel, "AUDITORIA");
        cardPanel.add(notificacionesPanel, "NOTIFICACIONES");
        cardPanel.add(hilosConcurrenciaPanel, "HILOS");

        // 🛡️ REGLAS ESTRICTAS DE CONTROL DE ACCESO BASADO EN ROLES (RBAC):
        Rol rol = sesionUsuario.getRol();
        String primerCard = "DASHBOARD";

        // 1. Superusuario / Administrador (Acceso 100% Completo)
        if (rol == Rol.ADMINISTRADOR) {
            primerCard = "DASHBOARD";
            sidebar.add(crearNavButton("❖  Vista General", "DASHBOARD", true));
            sidebar.add(Box.createRigidArea(new Dimension(0, 8)));
            sidebar.add(crearNavButton("⚡  Control de Accesos", "CONTROL_ACCESO", false));
            sidebar.add(Box.createRigidArea(new Dimension(0, 8)));
            sidebar.add(crearNavButton("👥  Gestión de Visitas", "VISITAS", false));
            sidebar.add(Box.createRigidArea(new Dimension(0, 8)));
            sidebar.add(crearNavButton("🏢  Personas y Zonas", "PERSONAS_ZONAS", false));
            sidebar.add(Box.createRigidArea(new Dimension(0, 8)));
            sidebar.add(crearNavButton("🛡  Incidentes y Bloqueos", "INCIDENTES", false));
            sidebar.add(Box.createRigidArea(new Dimension(0, 8)));
            sidebar.add(crearNavButton("📊  Analítica y Reportes", "REPORTES", false));
            sidebar.add(Box.createRigidArea(new Dimension(0, 8)));
            sidebar.add(crearNavButton("📋  Bitácora Auditoría", "AUDITORIA", false));
            sidebar.add(Box.createRigidArea(new Dimension(0, 8)));
            sidebar.add(crearNavButton("🔔  Centro de Alertas", "NOTIFICACIONES", false));

        // 2. Supervisor / Auditor
        } else if (rol == Rol.AUDITOR) {
            primerCard = "DASHBOARD";
            sidebar.add(crearNavButton("❖  Vista General", "DASHBOARD", true));
            sidebar.add(Box.createRigidArea(new Dimension(0, 8)));
            sidebar.add(crearNavButton("📊  Analítica y Reportes", "REPORTES", false));
            sidebar.add(Box.createRigidArea(new Dimension(0, 8)));
            sidebar.add(crearNavButton("🛡  Incidentes y Bloqueos", "INCIDENTES", false));
            sidebar.add(Box.createRigidArea(new Dimension(0, 8)));
            sidebar.add(crearNavButton("📋  Bitácora Auditoría", "AUDITORIA", false));
            sidebar.add(Box.createRigidArea(new Dimension(0, 8)));
            sidebar.add(crearNavButton("🔔  Centro de Alertas", "NOTIFICACIONES", false));

        // 3. Guarda de Seguridad
        } else if (rol == Rol.GUARDIA_SEGURIDAD) {
            primerCard = "CONTROL_ACCESO";
            sidebar.add(crearNavButton("⚡  Control de Accesos", "CONTROL_ACCESO", true));
            sidebar.add(Box.createRigidArea(new Dimension(0, 8)));
            sidebar.add(crearNavButton("🎫  Check-In de Visitas", "VISITAS", false));
            sidebar.add(Box.createRigidArea(new Dimension(0, 8)));
            sidebar.add(crearNavButton("🛡  Incidentes y Bloqueos", "INCIDENTES", false));
            sidebar.add(Box.createRigidArea(new Dimension(0, 8)));
            sidebar.add(crearNavButton("🔔  Centro de Alertas", "NOTIFICACIONES", false));

        // 4. Funcionario de Empresa (Anfitrión)
        } else if (rol == Rol.ANFITRION_EMPLEADO) {
            primerCard = "VISITAS";
            sidebar.add(crearNavButton("📝  Solicitudes de Visita", "VISITAS", true));

        // 5. Recepcionista
        } else {
            primerCard = "CONTROL_ACCESO";
            sidebar.add(crearNavButton("⚡  Control de Accesos", "CONTROL_ACCESO", true));
            sidebar.add(Box.createRigidArea(new Dimension(0, 8)));
            sidebar.add(crearNavButton("👥  Gestión de Visitas", "VISITAS", false));
            sidebar.add(Box.createRigidArea(new Dimension(0, 8)));
            sidebar.add(crearNavButton("🛡  Incidentes y Bloqueos", "INCIDENTES", false));
        }

        sidebar.add(Box.createVerticalGlue());

        // Footer del Sidebar con tarjeta de rol estilizada
        JPanel footerCard = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(20, 14, 38));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.setColor(new Color(147, 51, 234, 90));
                g2.setStroke(new BasicStroke(1.0f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        footerCard.setLayout(new BoxLayout(footerCard, BoxLayout.Y_AXIS));
        footerCard.setOpaque(false);
        footerCard.setBorder(new EmptyBorder(10, 12, 10, 12));
        footerCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 65));

        JLabel rolIndicator = new JLabel("ROL: " + rol.getNombreLegible().toUpperCase());
        rolIndicator.setFont(new Font("Segoe UI", Font.BOLD, 11));
        rolIndicator.setForeground(ThemeConstants.ACCENT_CYAN);
        rolIndicator.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel versionLabel = new JLabel("SICA v2.0 • Security Suite");
        versionLabel.setFont(ThemeConstants.FONT_SMALL);
        versionLabel.setForeground(ThemeConstants.TEXT_MUTED);
        versionLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        footerCard.add(rolIndicator);
        footerCard.add(Box.createRigidArea(new Dimension(0, 4)));
        footerCard.add(versionLabel);

        sidebar.add(footerCard);

        add(sidebar, BorderLayout.WEST);
        add(cardPanel, BorderLayout.CENTER);

        cardLayout.show(cardPanel, primerCard);
    }

    private JButton crearNavButton(String label, String cardName, boolean active) {
        JButton btn = new JButton(label) {
            private float navPulse = 0.0f;
            private javax.swing.Timer navTimer;
            {
                navTimer = new javax.swing.Timer(35, e -> {
                    if (getBackground().equals(ThemeConstants.ACCENT_PRIMARY)) {
                        navPulse = (navPulse + 0.06f) % (float)(2 * Math.PI);
                        repaint();
                    }
                });
                navTimer.start();
            }

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                boolean isActive = (getBackground().equals(ThemeConstants.ACCENT_PRIMARY));
                if (isActive) {
                    // Fondo suave translúcido para la pestaña activa
                    GradientPaint gp = new GradientPaint(0, 0, new Color(147, 51, 234, 70), getWidth(), 0, new Color(217, 70, 239, 35));
                    g2.setPaint(gp);
                    g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 12, 12));

                    // Borde fino elegante con respiración activa
                    int bAlpha = (int) (140 + 60 * Math.sin(navPulse));
                    g2.setColor(new Color(168, 85, 247, Math.min(255, bAlpha)));
                    g2.setStroke(new BasicStroke(1.3f));
                    g2.draw(new RoundRectangle2D.Float(0, 0, getWidth() - 1, getHeight() - 1, 12, 12));

                    // Indicador luminoso vertical en el borde izquierdo con haz de luz dinámico
                    int barY = (int) ((getHeight() - 20) * (0.5f + 0.35f * Math.sin(navPulse)));
                    GradientPaint barGp = new GradientPaint(0, 4, ThemeConstants.ACCENT_CYAN, 0, getHeight() - 4, ThemeConstants.ACCENT_MAGENTA);
                    g2.setPaint(barGp);
                    g2.fillRoundRect(2, 5, 5, getHeight() - 10, 4, 4);

                    // Destello brillante en la barra vertical
                    g2.setColor(Color.WHITE);
                    g2.fillOval(3, barY + 4, 3, 6);
                } else if (getModel().isRollover()) {
                    g2.setColor(ThemeConstants.BG_CARD_HOVER);
                    g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 12, 12));
                    g2.setColor(new Color(168, 85, 247, 100));
                    g2.setStroke(new BasicStroke(1.0f));
                    g2.draw(new RoundRectangle2D.Float(0, 0, getWidth() - 1, getHeight() - 1, 12, 12));
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

            // Refrescar datos del panel activo
            if ("DASHBOARD".equals(cardName)) dashboardPanel.refrescarDatos();
            if ("VISITAS".equals(cardName)) visitasPanel.cargarVisitas();
            if ("PERSONAS_ZONAS".equals(cardName)) { personasZonasPanel.cargarPersonas(); personasZonasPanel.cargarZonas(); }
            if ("INCIDENTES".equals(cardName)) incidentesPanel.recargarDatos();
            if ("REPORTES".equals(cardName)) reportesPanel.generarReporteSeleccionado();
            if ("AUDITORIA".equals(cardName)) auditoriaPanel.cargarAuditoria();
            if ("NOTIFICACIONES".equals(cardName)) notificacionesPanel.cargarNotificaciones();
            if ("HILOS".equals(cardName)) hilosConcurrenciaPanel.refrescarTabla();
        });

        sidebarButtons.add(btn);
        return btn;
    }
}
