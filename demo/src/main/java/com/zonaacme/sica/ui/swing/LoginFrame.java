package com.zonaacme.sica.ui.swing;

import com.zonaacme.sica.audit.adapters.AuditService;
import com.zonaacme.sica.audit.ports.out.AuditRepositoryPort;
import com.zonaacme.sica.auth.adapters.AuthService;
import com.zonaacme.sica.auth.domain.SesionUsuario;
import com.zonaacme.sica.auth.ports.out.UsuarioRepositoryPort;
import com.zonaacme.sica.core.adapters.ControlAccesoService;
import com.zonaacme.sica.core.adapters.VisitaService;
import com.zonaacme.sica.core.ports.out.*;
import com.zonaacme.sica.notifications.adapters.NotificationService;
import com.zonaacme.sica.notifications.ports.out.NotificationRepositoryPort;

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

    private final UsuarioRepositoryPort usuarioRepo;
    private final PersonaRepositoryPort personaRepo;
    private final ZonaRepositoryPort zonaRepo;
    private final VisitaRepositoryPort visitaRepo;
    private final RegistroAccesoRepositoryPort registroAccesoRepo;
    private final AuditRepositoryPort auditRepo;
    private final NotificationRepositoryPort notificationRepo;
    private final IncidenteRepositoryPort incidenteRepo;

    private JTextField txtUsername;
    private JPasswordField txtPassword;
    private JLabel lblError;

    public LoginFrame(
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
            IncidenteRepositoryPort incidenteRepo
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

        setTitle("SICA — Acceso al Sistema de Control de Acceso");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setMinimumSize(new Dimension(600, 500));
        setLocationRelativeTo(null);
        getContentPane().setBackground(ThemeConstants.BG_DARK);

        initUI();
    }

    private void initUI() {
        // Panel con animación dinámica de alta fidelidad (Cyber Matrix, Aurora Nebulas & Red de Nodos Láser)
        JPanel animatedBackground = new JPanel(new GridBagLayout()) {
            private float animPhase = 0.0f;
            private final int NUM_PARTICLES = 50;
            private final float[] px = new float[NUM_PARTICLES];
            private final float[] py = new float[NUM_PARTICLES];
            private final float[] vx = new float[NUM_PARTICLES];
            private final float[] vy = new float[NUM_PARTICLES];
            private final float[] size = new float[NUM_PARTICLES];
            private final Color[] pColors = new Color[NUM_PARTICLES];
            private boolean initialized = false;
            private javax.swing.Timer bgTimer;

            {
                bgTimer = new javax.swing.Timer(25, e -> {
                    animPhase = (animPhase + 0.03f) % (float)(2 * Math.PI);
                    updateParticles();
                    repaint();
                });
                bgTimer.start();
            }

            private void initParticles(int w, int h) {
                java.util.Random rnd = new java.util.Random(42);
                Color[] palette = new Color[]{
                        new Color(56, 189, 248),   // Cyber Cyan
                        new Color(168, 85, 247),  // Neon Purple
                        new Color(217, 70, 239),  // Neon Magenta
                        new Color(52, 211, 153)   // Emerald Green
                };
                for (int i = 0; i < NUM_PARTICLES; i++) {
                    px[i] = rnd.nextFloat() * w;
                    py[i] = rnd.nextFloat() * h;
                    vx[i] = (rnd.nextFloat() - 0.5f) * 1.2f;
                    vy[i] = (rnd.nextFloat() - 0.5f) * 1.2f;
                    size[i] = 2.0f + rnd.nextFloat() * 3.0f;
                    pColors[i] = palette[rnd.nextInt(palette.length)];
                }
                initialized = true;
            }

            private void updateParticles() {
                int w = getWidth();
                int h = getHeight();
                if (w <= 0 || h <= 0) return;
                if (!initialized) initParticles(w, h);

                for (int i = 0; i < NUM_PARTICLES; i++) {
                    px[i] += vx[i];
                    py[i] += vy[i];
                    if (px[i] < 0) px[i] = w;
                    if (px[i] > w) px[i] = 0;
                    if (py[i] < 0) py[i] = h;
                    if (py[i] > h) py[i] = 0;
                }
            }

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int w = getWidth();
                int h = getHeight();
                if (!initialized && w > 0 && h > 0) initParticles(w, h);

                // 1. Fondo base negro obsidiana profundo
                GradientPaint baseGp = new GradientPaint(0, 0, new Color(5, 3, 12), 0, h, new Color(14, 8, 28));
                g2.setPaint(baseGp);
                g2.fillRect(0, 0, w, h);

                // 2. Cuadrícula Cyber Digital de Perspectiva en el fondo
                g2.setColor(new Color(147, 51, 234, 18));
                g2.setStroke(new BasicStroke(1.0f));
                int gridSpacing = 48;
                for (int x = 0; x < w; x += gridSpacing) {
                    g2.drawLine(x, 0, x, h);
                }
                for (int y = 0; y < h; y += gridSpacing) {
                    g2.drawLine(0, y, w, y);
                }

                // 3. Orbe 1: Nebulosa Púrpura Eléctrico Viva
                int orb1X = (int) (w * 0.28 + Math.sin(animPhase) * (w * 0.20));
                int orb1Y = (int) (h * 0.32 + Math.cos(animPhase * 0.8) * (h * 0.18));
                RadialGradientPaint rgp1 = new RadialGradientPaint(
                        new Point(orb1X, orb1Y),
                        Math.max(380, w / 3),
                        new float[]{0.0f, 0.45f, 1.0f},
                        new Color[]{new Color(147, 51, 234, 130), new Color(126, 34, 206, 50), new Color(5, 3, 12, 0)}
                );
                g2.setPaint(rgp1);
                g2.fillRect(0, 0, w, h);

                // 4. Orbe 2: Nebulosa Magenta / Fuchsia Neón
                int orb2X = (int) (w * 0.72 - Math.cos(animPhase * 1.1) * (w * 0.20));
                int orb2Y = (int) (h * 0.68 - Math.sin(animPhase * 0.9) * (h * 0.18));
                RadialGradientPaint rgp2 = new RadialGradientPaint(
                        new Point(orb2X, orb2Y),
                        Math.max(340, w / 3),
                        new float[]{0.0f, 0.5f, 1.0f},
                        new Color[]{new Color(217, 70, 239, 110), new Color(168, 85, 247, 40), new Color(5, 3, 12, 0)}
                );
                g2.setPaint(rgp2);
                g2.fillRect(0, 0, w, h);

                // 5. Orbe 3: Nebulosa Cyber Cyan Eléctrica
                int orb3X = (int) (w * 0.50 + Math.cos(animPhase * 0.7) * (w * 0.18));
                int orb3Y = (int) (h * 0.82 + Math.sin(animPhase * 1.2) * (h * 0.14));
                RadialGradientPaint rgp3 = new RadialGradientPaint(
                        new Point(orb3X, orb3Y),
                        Math.max(300, w / 4),
                        new float[]{0.0f, 0.5f, 1.0f},
                        new Color[]{new Color(56, 189, 248, 85), new Color(99, 102, 241, 30), new Color(5, 3, 12, 0)}
                );
                g2.setPaint(rgp3);
                g2.fillRect(0, 0, w, h);

                // 6. Red de Constelación Cyber (Láseres inter-nodos)
                for (int i = 0; i < NUM_PARTICLES; i++) {
                    for (int j = i + 1; j < NUM_PARTICLES; j++) {
                        float dx = px[i] - px[j];
                        float dy = py[i] - py[j];
                        float dist = (float) Math.sqrt(dx * dx + dy * dy);
                        if (dist < 120) {
                            int alpha = (int) ((1.0f - dist / 120.0f) * 65);
                            g2.setColor(new Color(168, 85, 247, alpha));
                            g2.setStroke(new BasicStroke(0.9f));
                            g2.drawLine((int) px[i], (int) py[i], (int) px[j], (int) py[j]);
                        }
                    }
                }

                // 7. Dibujar partículas brillantes con halo
                for (int i = 0; i < NUM_PARTICLES; i++) {
                    int pAlpha = (int) (140 + 100 * Math.sin(animPhase * 2.5 + i));
                    pAlpha = Math.max(40, Math.min(255, pAlpha));
                    Color c = pColors[i];
                    g2.setColor(new Color(c.getRed(), c.getGreen(), c.getBlue(), pAlpha / 2));
                    g2.fillOval((int) px[i] - 2, (int) py[i] - 2, (int) size[i] + 4, (int) size[i] + 4);

                    g2.setColor(new Color(255, 255, 255, pAlpha));
                    g2.fillOval((int) px[i], (int) py[i], (int) size[i], (int) size[i]);
                }

                g2.dispose();
                super.paintComponent(g);
            }
        };
        animatedBackground.setOpaque(false);

        // Tarjeta Central Glassmorphism de Alta Gama con Borde Neón Dinámico Ultra-Elegante
        JPanel loginCard = new JPanel() {
            private float cardPulse = 0.0f;
            private javax.swing.Timer cardTimer;
            {
                cardTimer = new javax.swing.Timer(30, e -> {
                    cardPulse = (cardPulse + 0.04f) % (float)(2 * Math.PI);
                    repaint();
                });
                cardTimer.start();
            }

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int w = getWidth();
                int h = getHeight();

                // 1. Resplandor ambiental suave y lujoso (Sin círculos invasivos)
                int pulseAlpha = (int) (50 + 25 * Math.sin(cardPulse));
                g2.setColor(new Color(147, 51, 234, pulseAlpha));
                g2.fill(new RoundRectangle2D.Float(0, 0, w, h, 36, 36));

                // 2. Fondo Glassmorphism translúcido multicapa
                GradientPaint gp = new GradientPaint(
                        0, 0, new Color(24, 16, 46, 245),
                        0, h, new Color(12, 8, 26, 252)
                );
                g2.setPaint(gp);
                g2.fill(new RoundRectangle2D.Float(2, 2, w - 4, h - 4, 30, 30));

                // 3. Borde con gradiente animado continuo (Cyber Cyan -> Magenta -> Purple)
                int shift = (int) (w * (0.5f + 0.5f * Math.sin(cardPulse)));
                GradientPaint borderGp = new GradientPaint(
                        shift - 150, 0, ThemeConstants.ACCENT_CYAN,
                        shift + 150, h, ThemeConstants.ACCENT_MAGENTA,
                        true
                );
                g2.setPaint(borderGp);
                g2.setStroke(new BasicStroke(1.6f));
                g2.draw(new RoundRectangle2D.Float(2, 2, w - 5, h - 5, 30, 30));

                // 4. Haz de luz sutil en la arista superior
                int topBeamX = (int) (w * (0.5f + 0.4f * Math.sin(cardPulse)));
                GradientPaint topGleam = new GradientPaint(
                        topBeamX - 80, 2, new Color(255, 255, 255, 0),
                        topBeamX, 2, new Color(255, 255, 255, 180),
                        true
                );
                g2.setPaint(topGleam);
                g2.drawLine(25, 2, w - 25, 2);

                g2.dispose();
                super.paintComponent(g);
            }
        };
        loginCard.setLayout(new BorderLayout(0, 16));
        loginCard.setOpaque(false);
        loginCard.setPreferredSize(new Dimension(500, 600));
        loginCard.setBorder(new EmptyBorder(28, 36, 28, 36));

        // Encabezado con Isotipo Vectorial de Seguridad
        JPanel header = new JPanel(new BorderLayout(0, 8));
        header.setOpaque(false);

        // Icono Isotipo Vectorial: Emblema Hexagonal / Cyber Crest de Seguridad Extravagante
        JPanel logoBadge = new JPanel() {
            private float pulse = 0.0f;
            private javax.swing.Timer timer;
            {
                timer = new javax.swing.Timer(35, e -> {
                    pulse = (pulse + 0.05f) % (float)(2 * Math.PI);
                    repaint();
                });
                timer.start();
            }

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int cx = getWidth() / 2;
                int cy = getHeight() / 2;

                // 1. Aura hexagonal exterior con resplandor neón
                Polygon hexAura = new Polygon();
                int hr = 30;
                for (int i = 0; i < 6; i++) {
                    double angle = Math.PI / 3 * i + Math.PI / 6;
                    hexAura.addPoint((int)(cx + hr * Math.cos(angle)), (int)(cy + hr * Math.sin(angle)));
                }
                int auraAlpha = (int) (60 + 40 * Math.sin(pulse));
                g2.setColor(new Color(56, 189, 248, Math.max(15, auraAlpha)));
                g2.setStroke(new BasicStroke(2.0f));
                g2.draw(hexAura);

                // 2. Base del escudo con gradiente púrpura real a fucsia brillante
                Polygon shieldBase = new Polygon();
                shieldBase.addPoint(cx, cy - 22);
                shieldBase.addPoint(cx + 20, cy - 12);
                shieldBase.addPoint(cx + 20, cy + 8);
                shieldBase.addPoint(cx, cy + 24);
                shieldBase.addPoint(cx - 20, cy + 8);
                shieldBase.addPoint(cx - 20, cy - 12);

                GradientPaint baseGp = new GradientPaint(
                        cx - 20, cy - 22, ThemeConstants.ACCENT_PRIMARY,
                        cx + 20, cy + 24, ThemeConstants.ACCENT_MAGENTA
                );
                g2.setPaint(baseGp);
                g2.fill(shieldBase);

                // 3. Borde doble pulido blanco brillante
                g2.setColor(new Color(255, 255, 255, 230));
                g2.setStroke(new BasicStroke(1.6f));
                g2.draw(shieldBase);

                // 4. Escudo interior vector
                Polygon innerShield = new Polygon();
                innerShield.addPoint(cx, cy - 14);
                innerShield.addPoint(cx + 12, cy - 7);
                innerShield.addPoint(cx + 12, cy + 4);
                innerShield.addPoint(cx, cy + 15);
                innerShield.addPoint(cx - 12, cy + 4);
                innerShield.addPoint(cx - 12, cy - 7);
                g2.setColor(new Color(15, 10, 32, 220));
                g2.fill(innerShield);
                g2.setColor(new Color(56, 189, 248));
                g2.setStroke(new BasicStroke(1.4f));
                g2.draw(innerShield);

                // 5. Cerradura digital cian neón en el centro
                g2.setColor(new Color(56, 189, 248));
                g2.fillOval(cx - 3, cy - 5, 6, 6);
                Polygon key = new Polygon();
                key.addPoint(cx - 2, cy - 1);
                key.addPoint(cx + 2, cy - 1);
                key.addPoint(cx + 3, cy + 5);
                key.addPoint(cx - 3, cy + 5);
                g2.fill(key);

                g2.dispose();
                super.paintComponent(g);
            }
        };
        logoBadge.setPreferredSize(new Dimension(0, 60));
        logoBadge.setOpaque(false);

        JPanel titleTextPanel = new JPanel(new GridLayout(2, 1, 0, 3));
        titleTextPanel.setOpaque(false);

        JLabel lblTitle = new JLabel("Bienvenido a SICA", SwingConstants.CENTER);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 23));
        lblTitle.setForeground(ThemeConstants.TEXT_PRIMARY);

        JLabel lblSubtitle = new JLabel("Control de Acceso • Complejo Zona Acme", SwingConstants.CENTER);
        lblSubtitle.setFont(ThemeConstants.FONT_BODY_BOLD);
        lblSubtitle.setForeground(ThemeConstants.TEXT_SECONDARY);

        titleTextPanel.add(lblTitle);
        titleTextPanel.add(lblSubtitle);

        header.add(logoBadge, BorderLayout.NORTH);
        header.add(titleTextPanel, BorderLayout.CENTER);

        // Formulario con Etiquetas Elegantes y Botón de Ojo (Show/Hide Password)
        JPanel form = new JPanel(new GridLayout(4, 1, 0, 8));
        form.setOpaque(false);

        JLabel lblUser = new JLabel("  Usuario / Identificación:");
        lblUser.setFont(ThemeConstants.FONT_HEADER);
        lblUser.setForeground(ThemeConstants.TEXT_SECONDARY);
        txtUsername = ThemeConstants.createTextField("admin");

        JLabel lblPass = new JLabel("  Contraseña:");
        lblPass.setFont(ThemeConstants.FONT_HEADER);
        lblPass.setForeground(ThemeConstants.TEXT_SECONDARY);

        // Panel de Contraseña con Botón de Ojo Integrado
        JPanel passContainer = new JPanel(new BorderLayout(6, 0));
        passContainer.setOpaque(false);

        txtPassword = ThemeConstants.createPasswordField();
        txtPassword.setText("Admin123*");

        JButton btnToggleEye = new JButton("👁") {
            private boolean show = false;
            {
                setFont(new Font("Segoe UI Symbol", Font.PLAIN, 15));
                setForeground(ThemeConstants.TEXT_MUTED);
                setOpaque(false);
                setContentAreaFilled(false);
                setBorderPainted(false);
                setFocusPainted(false);
                setCursor(new Cursor(Cursor.HAND_CURSOR));
                setToolTipText("Mostrar/Ocultar contraseña");
                addActionListener(e -> {
                    show = !show;
                    if (show) {
                        txtPassword.setEchoChar((char) 0);
                        setForeground(ThemeConstants.ACCENT_CYAN);
                    } else {
                        txtPassword.setEchoChar('•');
                        setForeground(ThemeConstants.TEXT_MUTED);
                    }
                });
            }
        };

        passContainer.add(txtPassword, BorderLayout.CENTER);
        passContainer.add(btnToggleEye, BorderLayout.EAST);

        form.add(lblUser);
        form.add(txtUsername);
        form.add(lblPass);
        form.add(passContainer);

        // Error Label
        lblError = new JLabel("", SwingConstants.CENTER);
        lblError.setFont(ThemeConstants.FONT_SMALL);
        lblError.setForeground(ThemeConstants.ACCENT_DANGER);

        // Botón Login de Alta Conversión con Flecha Indicativa
        JButton btnLogin = ThemeConstants.createGradientButton(
                "Iniciar Sesión  →",
                ThemeConstants.ACCENT_PRIMARY,
                ThemeConstants.ACCENT_MAGENTA,
                Color.WHITE
        );
        btnLogin.setFont(new Font("Segoe UI", Font.BOLD, 15));
        btnLogin.setPreferredSize(new Dimension(0, 46));
        btnLogin.addActionListener(e -> intentarLogin());

        // Perfiles Rápidos estilizados como Chips / Pills Compactos
        JPanel quickProfilesWrapper = new JPanel(new BorderLayout(0, 6));
        quickProfilesWrapper.setOpaque(false);

        JLabel lblProfilesTitle = new JLabel("Perfiles Rápidos de Acceso:", SwingConstants.LEFT);
        lblProfilesTitle.setFont(ThemeConstants.FONT_SMALL);
        lblProfilesTitle.setForeground(ThemeConstants.TEXT_MUTED);
        quickProfilesWrapper.add(lblProfilesTitle, BorderLayout.NORTH);

        JPanel quickProfilesGrid = new JPanel(new GridLayout(2, 2, 8, 8));
        quickProfilesGrid.setOpaque(false);

        JButton btnAdmin = crearProfileChip("Superadmin", "admin", "Admin123*");
        JButton btnGuarda = crearProfileChip("Guarda", "guardia1", "Guardia123*");
        JButton btnFunc = crearProfileChip("Funcionario", "funcionario1", "Func123*");
        JButton btnSuper = crearProfileChip("Supervisor", "super1", "Super123*");

        quickProfilesGrid.add(btnAdmin);
        quickProfilesGrid.add(btnGuarda);
        quickProfilesGrid.add(btnFunc);
        quickProfilesGrid.add(btnSuper);
        quickProfilesWrapper.add(quickProfilesGrid, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new BorderLayout(0, 8));
        bottomPanel.setOpaque(false);
        bottomPanel.add(lblError, BorderLayout.NORTH);
        bottomPanel.add(btnLogin, BorderLayout.CENTER);
        bottomPanel.add(quickProfilesWrapper, BorderLayout.SOUTH);

        loginCard.add(header, BorderLayout.NORTH);
        loginCard.add(form, BorderLayout.CENTER);
        loginCard.add(bottomPanel, BorderLayout.SOUTH);

        animatedBackground.add(loginCard);
        setContentPane(animatedBackground);
    }

    private JButton crearProfileChip(String label, String user, String pass) {
        JButton btn = new JButton(label) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                boolean hover = getModel().isRollover();
                Color bg = hover ? ThemeConstants.BG_CARD_HOVER : ThemeConstants.BG_INPUT;
                g2.setColor(bg);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 12, 12));

                Color border = hover ? ThemeConstants.ACCENT_PURPLE : ThemeConstants.BORDER_COLOR;
                g2.setColor(border);
                g2.setStroke(new BasicStroke(1.2f));
                g2.draw(new RoundRectangle2D.Float(0, 0, getWidth() - 1, getHeight() - 1, 12, 12));

                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(ThemeConstants.FONT_SMALL);
        btn.setForeground(ThemeConstants.TEXT_PRIMARY);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(0, 32));
        btn.addActionListener(e -> {
            txtUsername.setText(user);
            txtPassword.setText(pass);
        });
        return btn;
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
                        usuarioRepo, personaRepo, zonaRepo, visitaRepo, registroAccesoRepo, auditRepo, notificationRepo, incidenteRepo, sesion
                );
                mainFrame.setExtendedState(JFrame.MAXIMIZED_BOTH);
                mainFrame.setVisible(true);
            });
        } catch (Exception ex) {
            lblError.setText(ex.getMessage());
        }
    }
}
