package com.zonaacme.sica.ui.swing;

import com.zonaacme.sica.audit.adapters.AuditService;
import com.zonaacme.sica.audit.adapters.InMemoryAuditRepositoryAdapter;
import com.zonaacme.sica.auth.adapters.AuthService;
import com.zonaacme.sica.auth.adapters.InMemoryUsuarioRepositoryAdapter;
import com.zonaacme.sica.auth.domain.SesionUsuario;
import com.zonaacme.sica.core.adapters.*;
import com.zonaacme.sica.notifications.adapters.InMemoryNotificationRepositoryAdapter;
import com.zonaacme.sica.notifications.adapters.NotificationService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

public class LoginFrame extends JFrame {

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

    private JTextField txtUsername;
    private JPasswordField txtPassword;
    private JLabel lblError;

    public LoginFrame(
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
            InMemoryNotificationRepositoryAdapter notificationRepo
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

        setTitle("SICA — Acceso al Sistema de Control de Acceso");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setMinimumSize(new Dimension(600, 500));
        setLocationRelativeTo(null);
        getContentPane().setBackground(ThemeConstants.BG_DARK);

        initUI();
    }

    private void initUI() {
        setLayout(new GridBagLayout());

        JPanel loginCard = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, new Color(24, 32, 54), 0, getHeight(), new Color(15, 23, 42));
                g2.setPaint(gp);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 20, 20));
                g2.setColor(ThemeConstants.BORDER_HIGHLIGHT);
                g2.setStroke(new BasicStroke(1.5f));
                g2.draw(new RoundRectangle2D.Float(0, 0, getWidth() - 1, getHeight() - 1, 20, 20));
                g2.dispose();
                super.paintComponent(g);
            }
        };
        loginCard.setOpaque(false);
        loginCard.setPreferredSize(new Dimension(440, 520));
        loginCard.setLayout(new BorderLayout(0, 18));
        loginCard.setBorder(new EmptyBorder(28, 32, 28, 32));

        // Header Card
        JPanel header = new JPanel(new GridLayout(2, 1, 6, 6));
        header.setOpaque(false);

        JLabel title = new JLabel("SICA — ZONA ACME", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setForeground(ThemeConstants.TEXT_PRIMARY);

        JLabel subtitle = new JLabel("Control de Acceso Seguro & RBAC", SwingConstants.CENTER);
        subtitle.setFont(ThemeConstants.FONT_BODY);
        subtitle.setForeground(ThemeConstants.ACCENT_CYAN);

        header.add(title);
        header.add(subtitle);

        // Formulario
        JPanel form = new JPanel(new GridLayout(4, 1, 8, 8));
        form.setOpaque(false);

        JLabel lblUser = new JLabel("Usuario o Email:");
        lblUser.setFont(ThemeConstants.FONT_BODY_BOLD);
        lblUser.setForeground(ThemeConstants.TEXT_PRIMARY);
        txtUsername = ThemeConstants.createTextField();
        txtUsername.setText("admin");

        JLabel lblPass = new JLabel("Contraseña:");
        lblPass.setFont(ThemeConstants.FONT_BODY_BOLD);
        lblPass.setForeground(ThemeConstants.TEXT_PRIMARY);
        txtPassword = ThemeConstants.createPasswordField();
        txtPassword.setText("Admin123*");

        form.add(lblUser);
        form.add(txtUsername);
        form.add(lblPass);
        form.add(txtPassword);

        // Error message
        lblError = new JLabel("", SwingConstants.CENTER);
        lblError.setFont(ThemeConstants.FONT_SMALL);
        lblError.setForeground(ThemeConstants.ACCENT_DANGER);

        // Botón de Ingreso con Gradiente
        JButton btnLogin = ThemeConstants.createGradientButton(
                "Iniciar Sesión",
                new Color(99, 102, 241),
                new Color(6, 182, 212),
                Color.WHITE
        );
        btnLogin.setFont(new Font("Segoe UI", Font.BOLD, 15));
        btnLogin.setPreferredSize(new Dimension(0, 44));
        btnLogin.addActionListener(e -> intentarLogin());

        // Selector rápido de perfiles
        JPanel quickProfiles = new JPanel(new GridLayout(2, 2, 8, 8));
        quickProfiles.setOpaque(false);
        quickProfiles.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(ThemeConstants.BORDER_COLOR),
                "Perfiles Rápidos de Prueba",
                0,
                0,
                ThemeConstants.FONT_SMALL,
                ThemeConstants.TEXT_SECONDARY
        ));

        JButton btnAdmin = ThemeConstants.createButton("Superadmin", ThemeConstants.BG_CARD_HOVER, ThemeConstants.TEXT_PRIMARY);
        btnAdmin.addActionListener(e -> { txtUsername.setText("admin"); txtPassword.setText("Admin123*"); });

        JButton btnGuarda = ThemeConstants.createButton("Guarda", ThemeConstants.BG_CARD_HOVER, ThemeConstants.TEXT_PRIMARY);
        btnGuarda.addActionListener(e -> { txtUsername.setText("guardia1"); txtPassword.setText("Guardia123*"); });

        JButton btnFunc = ThemeConstants.createButton("Funcionario", ThemeConstants.BG_CARD_HOVER, ThemeConstants.TEXT_PRIMARY);
        btnFunc.addActionListener(e -> { txtUsername.setText("funcionario1"); txtPassword.setText("Func123*"); });

        JButton btnSuper = ThemeConstants.createButton("Supervisor", ThemeConstants.BG_CARD_HOVER, ThemeConstants.TEXT_PRIMARY);
        btnSuper.addActionListener(e -> { txtUsername.setText("super1"); txtPassword.setText("Super123*"); });

        quickProfiles.add(btnAdmin);
        quickProfiles.add(btnGuarda);
        quickProfiles.add(btnFunc);
        quickProfiles.add(btnSuper);

        JPanel bottomPanel = new JPanel(new BorderLayout(0, 10));
        bottomPanel.setOpaque(false);
        bottomPanel.add(lblError, BorderLayout.NORTH);
        bottomPanel.add(btnLogin, BorderLayout.CENTER);
        bottomPanel.add(quickProfiles, BorderLayout.SOUTH);

        loginCard.add(header, BorderLayout.NORTH);
        loginCard.add(form, BorderLayout.CENTER);
        loginCard.add(bottomPanel, BorderLayout.SOUTH);

        add(loginCard);
    }

    private void intentarLogin() {
        lblError.setText("");
        String user = txtUsername.getText().trim();
        String pass = new String(txtPassword.getPassword());

        if (user.isEmpty() || pass.isEmpty()) {
            lblError.setText("Debe completar todos los campos");
            return;
        }

        try {
            SesionUsuario sesion = authService.autenticar(user, pass);
            dispose();
            SwingUtilities.invokeLater(() -> {
                MainDashboardFrame mainFrame = new MainDashboardFrame(
                        authService, visitaService, controlAccesoService, auditService, notificationService,
                        usuarioRepo, personaRepo, zonaRepo, visitaRepo, registroAccesoRepo, auditRepo, notificationRepo, sesion
                );
                mainFrame.setExtendedState(JFrame.MAXIMIZED_BOTH);
                mainFrame.setVisible(true);
            });
        } catch (Exception ex) {
            lblError.setText(ex.getMessage());
        }
    }
}
