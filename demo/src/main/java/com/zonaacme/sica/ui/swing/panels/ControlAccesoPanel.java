package com.zonaacme.sica.ui.swing.panels;

import com.zonaacme.sica.auth.domain.SesionUsuario;
import com.zonaacme.sica.core.adapters.ControlAccesoService;
import com.zonaacme.sica.core.adapters.InMemoryPersonaRepositoryAdapter;
import com.zonaacme.sica.core.adapters.InMemoryZonaRepositoryAdapter;
import com.zonaacme.sica.core.domain.*;
import com.zonaacme.sica.ui.swing.ThemeConstants;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

public class ControlAccesoPanel extends JPanel {

    private final ControlAccesoService controlAccesoService;
    private final InMemoryPersonaRepositoryAdapter personaRepo;
    private final InMemoryZonaRepositoryAdapter zonaRepo;
    private SesionUsuario sesionActual;

    private JComboBox<String> comboTipoDoc;
    private JTextField txtNumeroDoc;
    private JComboBox<PuntoControlComboItem> comboPuntosControl;
    private JComboBox<TipoAcceso> comboTipoAcceso;

    private JPanel terminalDisplay;
    private JLabel lblStatusBadge;
    private JLabel lblStatusDetail;
    private JLabel lblStatusPersona;
    private JPanel lightIndicator;

    private Runnable onAccessRegisteredCallback;

    public ControlAccesoPanel(
            ControlAccesoService controlAccesoService,
            InMemoryPersonaRepositoryAdapter personaRepo,
            InMemoryZonaRepositoryAdapter zonaRepo,
            SesionUsuario sesionActual
    ) {
        this.controlAccesoService = controlAccesoService;
        this.personaRepo = personaRepo;
        this.zonaRepo = zonaRepo;
        this.sesionActual = sesionActual;

        setLayout(new BorderLayout(24, 24));
        setBackground(ThemeConstants.BG_DARK);
        setBorder(new EmptyBorder(28, 36, 28, 36));

        initUI();
    }

    public void setSesionActual(SesionUsuario sesionActual) {
        this.sesionActual = sesionActual;
    }

    public void setOnAccessRegisteredCallback(Runnable callback) {
        this.onAccessRegisteredCallback = callback;
    }

    private void initUI() {
        // Encabezado
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        JLabel title = new JLabel("Control de Accesos y Torniquetes");
        title.setFont(ThemeConstants.FONT_TITLE);
        title.setForeground(ThemeConstants.TEXT_PRIMARY);

        JLabel subtitle = new JLabel("Validación de credenciales, horarios de seguridad y registro de ingresos/salidas en tiempo real");
        subtitle.setFont(ThemeConstants.FONT_BODY);
        subtitle.setForeground(ThemeConstants.TEXT_SECONDARY);

        headerPanel.add(title, BorderLayout.NORTH);
        headerPanel.add(subtitle, BorderLayout.SOUTH);

        // Contenedor Central Split
        JPanel mainContent = new JPanel(new GridLayout(1, 2, 28, 0));
        mainContent.setOpaque(false);

        // Panel Izquierdo: Formulario de Punto de Control
        JPanel formCard = ThemeConstants.createCard();
        formCard.setLayout(new BorderLayout(0, 20));

        JLabel formTitle = new JLabel("Validación en Punto de Control");
        formTitle.setFont(ThemeConstants.FONT_SUBTITLE);
        formTitle.setForeground(ThemeConstants.TEXT_PRIMARY);
        formCard.add(formTitle, BorderLayout.NORTH);

        JPanel formGrid = new JPanel(new GridLayout(4, 2, 16, 18));
        formGrid.setOpaque(false);

        comboTipoDoc = ThemeConstants.createComboBox(new String[]{"CC", "CE", "PASAPORTE", "TI"});
        txtNumeroDoc = ThemeConstants.createTextField();
        txtNumeroDoc.setText("10102020"); // Default seed employee

        // Puntos de control
        List<PuntoControl> puntos = zonaRepo.findAllPuntosControl();
        DefaultComboBoxModel<PuntoControlComboItem> pcModel = new DefaultComboBoxModel<>();
        for (PuntoControl pc : puntos) {
            String nombreZona = zonaRepo.findZonaById(pc.getZonaId()).map(Zona::getNombre).orElse("Zona");
            pcModel.addElement(new PuntoControlComboItem(pc.getId(), pc.getCodigo() + " - " + pc.getNombre() + " (" + nombreZona + ")"));
        }
        comboPuntosControl = new JComboBox<>(pcModel);
        comboPuntosControl.setBackground(ThemeConstants.BG_INPUT);
        comboPuntosControl.setForeground(ThemeConstants.TEXT_PRIMARY);
        comboPuntosControl.setFont(ThemeConstants.FONT_BODY);

        comboTipoAcceso = ThemeConstants.createComboBox(new TipoAcceso[]{TipoAcceso.ENTRADA, TipoAcceso.SALIDA});

        formGrid.add(crearLabel("Tipo de Documento:"));
        formGrid.add(comboTipoDoc);

        formGrid.add(crearLabel("Número de Documento:"));
        formGrid.add(txtNumeroDoc);

        formGrid.add(crearLabel("Punto de Control:"));
        formGrid.add(comboPuntosControl);

        formGrid.add(crearLabel("Sentido de Paso:"));
        formGrid.add(comboTipoAcceso);

        JButton btnValidar = ThemeConstants.createGradientButton(
                "Validar y Registrar Acceso",
                new Color(99, 102, 241),
                new Color(6, 182, 212),
                Color.WHITE
        );
        btnValidar.setFont(new Font("Segoe UI", Font.BOLD, 15));
        btnValidar.setPreferredSize(new Dimension(0, 48));
        btnValidar.addActionListener(e -> procesarAcceso());

        formCard.add(formGrid, BorderLayout.CENTER);
        formCard.add(btnValidar, BorderLayout.SOUTH);

        // Panel Derecho: Terminal Visual de Torniquete
        JPanel rightContainer = ThemeConstants.createCard();
        rightContainer.setLayout(new BorderLayout(0, 18));

        JLabel resultTitle = new JLabel("Terminal Visual del Torniquete / Barrera");
        resultTitle.setFont(ThemeConstants.FONT_SUBTITLE);
        resultTitle.setForeground(ThemeConstants.TEXT_PRIMARY);

        // LCD Terminal Box
        terminalDisplay = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(6, 10, 20));
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 16, 16));
                g2.setColor(new Color(30, 41, 67));
                g2.setStroke(new BasicStroke(2f));
                g2.draw(new RoundRectangle2D.Float(0, 0, getWidth() - 1, getHeight() - 1, 16, 16));
                g2.dispose();
                super.paintComponent(g);
            }
        };
        terminalDisplay.setOpaque(false);
        terminalDisplay.setLayout(new BorderLayout(10, 10));
        terminalDisplay.setBorder(new EmptyBorder(24, 20, 24, 20));

        // Luz LED Indicador
        lightIndicator = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillOval(getWidth() / 2 - 14, 2, 28, 28);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        lightIndicator.setOpaque(false);
        lightIndicator.setBackground(new Color(75, 85, 99));
        lightIndicator.setPreferredSize(new Dimension(0, 36));

        JPanel textStatusPanel = new JPanel(new GridLayout(3, 1, 6, 6));
        textStatusPanel.setOpaque(false);

        lblStatusBadge = new JLabel("ESPERANDO CREDENCIAL", SwingConstants.CENTER);
        lblStatusBadge.setFont(new Font("Consolas", Font.BOLD, 22));
        lblStatusBadge.setForeground(ThemeConstants.TEXT_MUTED);

        lblStatusPersona = new JLabel("Presente documento o carnet en el lector", SwingConstants.CENTER);
        lblStatusPersona.setFont(ThemeConstants.FONT_HEADER);
        lblStatusPersona.setForeground(ThemeConstants.TEXT_SECONDARY);

        lblStatusDetail = new JLabel("Listo para procesar eventos de paso", SwingConstants.CENTER);
        lblStatusDetail.setFont(ThemeConstants.FONT_SMALL);
        lblStatusDetail.setForeground(ThemeConstants.TEXT_MUTED);

        textStatusPanel.add(lblStatusBadge);
        textStatusPanel.add(lblStatusPersona);
        textStatusPanel.add(lblStatusDetail);

        terminalDisplay.add(lightIndicator, BorderLayout.NORTH);
        terminalDisplay.add(textStatusPanel, BorderLayout.CENTER);

        // Presets Rápidos de Demostración
        JPanel quickPresets = new JPanel(new GridLayout(3, 1, 8, 8));
        quickPresets.setOpaque(false);
        quickPresets.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(ThemeConstants.BORDER_COLOR),
                "Casos de Prueba Rápidos",
                0,
                0,
                ThemeConstants.FONT_SMALL,
                ThemeConstants.ACCENT_CYAN
        ));

        JButton btnEmpleadoValido = ThemeConstants.createButton("1. Empleado Válido (CC 10102020)", ThemeConstants.BG_CARD_HOVER, ThemeConstants.TEXT_PRIMARY);
        btnEmpleadoValido.addActionListener(e -> {
            txtNumeroDoc.setText("10102020");
            comboTipoDoc.setSelectedItem("CC");
        });

        JButton btnVisitanteValido = ThemeConstants.createButton("2. Invitado con Visita (CC 80809090)", ThemeConstants.BG_CARD_HOVER, ThemeConstants.TEXT_PRIMARY);
        btnVisitanteValido.addActionListener(e -> {
            txtNumeroDoc.setText("80809090");
            comboTipoDoc.setSelectedItem("CC");
        });

        JButton btnBloqueado = ThemeConstants.createButton("3. Persona Bloqueada (CC 99998888)", ThemeConstants.BG_CARD_HOVER, ThemeConstants.TEXT_PRIMARY);
        btnBloqueado.addActionListener(e -> {
            txtNumeroDoc.setText("99998888");
            comboTipoDoc.setSelectedItem("CC");
        });

        quickPresets.add(btnEmpleadoValido);
        quickPresets.add(btnVisitanteValido);
        quickPresets.add(btnBloqueado);

        rightContainer.add(resultTitle, BorderLayout.NORTH);
        rightContainer.add(terminalDisplay, BorderLayout.CENTER);
        rightContainer.add(quickPresets, BorderLayout.SOUTH);

        mainContent.add(formCard);
        mainContent.add(rightContainer);

        add(headerPanel, BorderLayout.NORTH);
        add(mainContent, BorderLayout.CENTER);
    }

    private JLabel crearLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(ThemeConstants.FONT_BODY_BOLD);
        lbl.setForeground(ThemeConstants.TEXT_PRIMARY);
        return lbl;
    }

    private void procesarAcceso() {
        String tipoDoc = (String) comboTipoDoc.getSelectedItem();
        String numDoc = txtNumeroDoc.getText().trim();
        PuntoControlComboItem selectedPc = (PuntoControlComboItem) comboPuntosControl.getSelectedItem();

        if (selectedPc == null || numDoc.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Debe ingresar un número de documento válido", "Validación", JOptionPane.WARNING_MESSAGE);
            return;
        }

        TipoAcceso tipoAcceso = (TipoAcceso) comboTipoAcceso.getSelectedItem();

        try {
            Optional<Persona> personaOpt = personaRepo.findByDocumento(tipoDoc, numDoc);
            String personaId = personaOpt.map(Persona::getId).orElse("PERSONA_DESCONOCIDA_" + numDoc);

            RegistroAcceso registro;
            if (tipoAcceso == TipoAcceso.ENTRADA) {
                registro = controlAccesoService.registrarIngreso(personaId, selectedPc.id, sesionActual.getToken());
            } else {
                registro = controlAccesoService.registrarSalida(personaId, selectedPc.id, sesionActual.getToken());
            }

            if (registro.getResultado() == ResultadoAcceso.PERMITIDO) {
                lblStatusBadge.setText("ACCESO PERMITIDO");
                lblStatusBadge.setForeground(ThemeConstants.ACCENT_SUCCESS);
                lightIndicator.setBackground(ThemeConstants.ACCENT_SUCCESS);
            } else {
                lblStatusBadge.setText("DENEGADO: " + registro.getResultado().name());
                lblStatusBadge.setForeground(ThemeConstants.ACCENT_DANGER);
                lightIndicator.setBackground(ThemeConstants.ACCENT_DANGER);
            }

            String personaNombre = personaOpt
                    .map(p -> p.getNombreCompleto() + " (" + p.getTipoPersona() + ")")
                    .orElse("Doc: " + tipoDoc + " " + numDoc);

            lblStatusPersona.setText(personaNombre);
            lblStatusDetail.setText(registro.getObservaciones() + " [" + registro.getFechaHora().format(DateTimeFormatter.ofPattern("HH:mm:ss")) + "]");

            lightIndicator.repaint();

            if (onAccessRegisteredCallback != null) {
                onAccessRegisteredCallback.run();
            }

        } catch (Exception ex) {
            lblStatusBadge.setText("ALERTA DE SEGURIDAD");
            lblStatusBadge.setForeground(ThemeConstants.ACCENT_WARNING);
            lightIndicator.setBackground(ThemeConstants.ACCENT_WARNING);
            lblStatusPersona.setText("Excepción de autorización");
            lblStatusDetail.setText(ex.getMessage());
            lightIndicator.repaint();
        }
    }

    private static class PuntoControlComboItem {
        final String id;
        final String label;

        PuntoControlComboItem(String id, String label) {
            this.id = id;
            this.label = label;
        }

        @Override
        public String toString() {
            return label;
        }
    }
}
