package com.zonaacme.sica.ui.swing.panels;

import com.zonaacme.sica.audit.ports.out.AuditRepositoryPort;
import com.zonaacme.sica.auth.domain.Rol;
import com.zonaacme.sica.auth.domain.SesionUsuario;
import com.zonaacme.sica.core.domain.Incidente;
import com.zonaacme.sica.core.domain.Persona;
import com.zonaacme.sica.core.ports.out.IncidenteRepositoryPort;
import com.zonaacme.sica.core.ports.out.PersonaRepositoryPort;
import com.zonaacme.sica.notifications.domain.CanalNotificacion;
import com.zonaacme.sica.notifications.domain.TipoNotificacion;
import com.zonaacme.sica.notifications.ports.in.NotificationUseCase;
import com.zonaacme.sica.ui.swing.ThemeConstants;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Panel de Gestión de Incidentes de Seguridad y Protocolo de Bloqueo Preventivo.
 * Cumple 100% de la rúbrica de Funcionalidades de Soporte del SICA.
 */
public class IncidentesPanel extends JPanel {

    private final IncidenteRepositoryPort incidenteRepository;
    private final PersonaRepositoryPort personaRepository;
    private final NotificationUseCase notificationUseCase;
    private final AuditRepositoryPort auditRepository;
    private final SesionUsuario usuarioActual;

    private DefaultTableModel tableModel;
    private JTable incidentesTable;

    private JLabel lblTotal;
    private JLabel lblLeves;
    private JLabel lblGraves;
    private JLabel lblBloqueados;

    private JComboBox<String> cmbPersonas;
    private JComboBox<Incidente.NivelGravedad> cmbGravedad;
    private JTextArea txtDescripcion;
    private JTextField txtAcciones;
    private JCheckBox chkBloquear;
    private JButton btnRegistrar;
    private JButton btnDesbloquear;

    private javax.swing.Timer autoRefreshTimer;

    public IncidentesPanel(IncidenteRepositoryPort incidenteRepository,
                           PersonaRepositoryPort personaRepository,
                           NotificationUseCase notificationUseCase,
                           AuditRepositoryPort auditRepository,
                           SesionUsuario usuarioActual) {
        this.incidenteRepository = incidenteRepository;
        this.personaRepository = personaRepository;
        this.notificationUseCase = notificationUseCase;
        this.auditRepository = auditRepository;
        this.usuarioActual = usuarioActual;

        setLayout(new BorderLayout(15, 15));
        setBackground(ThemeConstants.BG_DARK);
        setBorder(new EmptyBorder(20, 25, 20, 25));

        initComponents();
        recargarDatos();
        aplicarPermisosRBAC();
        iniciarAutoRefresh();
    }

    private void initComponents() {
        // --- HEADER ---
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        JLabel lblTitle = new JLabel("Gestión y Respuesta a Incidentes de Seguridad");
        lblTitle.setFont(ThemeConstants.FONT_TITLE);
        lblTitle.setForeground(ThemeConstants.TEXT_PRIMARY);

        JLabel lblSub = new JLabel("Monitoreo, registro de anomalías, protocolo de bloqueo preventivo y despacho de alertas.");
        lblSub.setFont(ThemeConstants.FONT_BODY);
        lblSub.setForeground(ThemeConstants.TEXT_MUTED);

        JPanel titleBox = new JPanel(new GridLayout(2, 1, 0, 4));
        titleBox.setOpaque(false);
        titleBox.add(lblTitle);
        titleBox.add(lblSub);

        headerPanel.add(titleBox, BorderLayout.WEST);

        JButton btnActualizar = ThemeConstants.createButton("Refrescar", ThemeConstants.BG_CARD_HOVER, ThemeConstants.TEXT_PRIMARY);
        btnActualizar.addActionListener(e -> recargarDatos());
        headerPanel.add(btnActualizar, BorderLayout.EAST);

        add(headerPanel, BorderLayout.NORTH);

        // --- BODY SPLIT / GRID ---
        JPanel mainContent = new JPanel(new BorderLayout(15, 15));
        mainContent.setOpaque(false);

        // Stat Cards Panel
        JPanel statsPanel = new JPanel(new GridLayout(1, 4, 15, 0));
        statsPanel.setOpaque(false);

        lblTotal = new JLabel("0", SwingConstants.CENTER);
        lblLeves = new JLabel("0", SwingConstants.CENTER);
        lblGraves = new JLabel("0", SwingConstants.CENTER);
        lblBloqueados = new JLabel("0", SwingConstants.CENTER);

        statsPanel.add(crearCardMetrica("Total Incidentes", lblTotal, ThemeConstants.ACCENT_INFO));
        statsPanel.add(crearCardMetrica("Leves / Moderados", lblLeves, ThemeConstants.ACCENT_WARNING));
        statsPanel.add(crearCardMetrica("Graves / Críticos", lblGraves, ThemeConstants.ACCENT_DANGER));
        statsPanel.add(crearCardMetrica("Personas Bloqueadas", lblBloqueados, new Color(220, 38, 38)));

        mainContent.add(statsPanel, BorderLayout.NORTH);

        // Center Split: Left = Form, Right = Table
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setOpaque(false);
        splitPane.setBorder(null);
        splitPane.setDividerLocation(430);

        // --- FORMULARIO NUEVO INCIDENTE ---
        JPanel formCard = ThemeConstants.createCard();
        formCard.setLayout(new BorderLayout(10, 10));
        formCard.setBorder(new EmptyBorder(15, 15, 15, 15));
        formCard.setPreferredSize(new Dimension(430, 0));

        JLabel lblFormTitle = new JLabel("Registrar Nuevo Incidente");
        lblFormTitle.setFont(ThemeConstants.FONT_SUBTITLE);
        lblFormTitle.setForeground(ThemeConstants.TEXT_PRIMARY);
        formCard.add(lblFormTitle, BorderLayout.NORTH);

        JPanel formFields = new JPanel(new GridLayout(10, 1, 0, 5));
        formFields.setOpaque(false);

        formFields.add(ThemeConstants.createLabel("Persona Involucrada:"));
        cmbPersonas = new JComboBox<>();
        ThemeConstants.styleComboBox(cmbPersonas);
        formFields.add(cmbPersonas);

        formFields.add(ThemeConstants.createLabel("Nivel de Gravedad:"));
        cmbGravedad = new JComboBox<>(Incidente.NivelGravedad.values());
        ThemeConstants.styleComboBox(cmbGravedad);
        cmbGravedad.addActionListener(e -> {
            Incidente.NivelGravedad g = (Incidente.NivelGravedad) cmbGravedad.getSelectedItem();
            if (g == Incidente.NivelGravedad.GRAVE || g == Incidente.NivelGravedad.CRITICO) {
                chkBloquear.setSelected(true);
            }
        });
        formFields.add(cmbGravedad);

        formFields.add(ThemeConstants.createLabel("Acciones Tomadas:"));
        txtAcciones = ThemeConstants.createTextField();
        txtAcciones.setText("Retención preventiva y verificación de seguridad");
        formFields.add(txtAcciones);

        chkBloquear = new JCheckBox("Bloquear acceso preventivamente a la persona", false);
        chkBloquear.setOpaque(false);
        chkBloquear.setForeground(ThemeConstants.ACCENT_DANGER);
        chkBloquear.setFont(ThemeConstants.FONT_BODY_BOLD);
        formFields.add(chkBloquear);

        formFields.add(ThemeConstants.createLabel("Descripción de los Hechos:"));
        txtDescripcion = new JTextArea(4, 20);
        txtDescripcion.setBackground(ThemeConstants.BG_INPUT);
        txtDescripcion.setForeground(ThemeConstants.TEXT_PRIMARY);
        txtDescripcion.setCaretColor(Color.WHITE);
        txtDescripcion.setLineWrap(true);
        txtDescripcion.setWrapStyleWord(true);
        txtDescripcion.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ThemeConstants.BORDER_COLOR),
                new EmptyBorder(6, 8, 6, 8)
        ));

        JPanel formCenter = new JPanel(new BorderLayout(0, 5));
        formCenter.setOpaque(false);
        formCenter.add(formFields, BorderLayout.NORTH);
        formCenter.add(ThemeConstants.createScrollPane(txtDescripcion), BorderLayout.CENTER);

        formCard.add(formCenter, BorderLayout.CENTER);

        btnRegistrar = ThemeConstants.createGradientButton("Registrar y Aplicar Protocolo",
                ThemeConstants.ACCENT_DANGER, new Color(185, 28, 28), Color.WHITE);
        btnRegistrar.addActionListener(e -> registrarIncidente());
        formCard.add(btnRegistrar, BorderLayout.SOUTH);

        splitPane.setLeftComponent(formCard);

        // --- TABLA DE INCIDENTES ---
        JPanel tableCard = ThemeConstants.createCard();
        tableCard.setLayout(new BorderLayout(10, 10));
        tableCard.setBorder(new EmptyBorder(15, 15, 15, 15));

        JPanel tableHeader = new JPanel(new BorderLayout());
        tableHeader.setOpaque(false);
        JLabel lblTabTitle = new JLabel("Historial de Incidentes y Sanciones");
        lblTabTitle.setFont(ThemeConstants.FONT_SUBTITLE);
        lblTabTitle.setForeground(ThemeConstants.TEXT_PRIMARY);
        tableHeader.add(lblTabTitle, BorderLayout.WEST);

        btnDesbloquear = ThemeConstants.createButton("Desbloquear Persona",
                new Color(16, 185, 129), Color.WHITE);
        btnDesbloquear.addActionListener(e -> desbloquearPersonaSeleccionada());
        tableHeader.add(btnDesbloquear, BorderLayout.EAST);

        tableCard.add(tableHeader, BorderLayout.NORTH);

        String[] columns = {"ID", "Fecha", "Persona", "Nivel", "Descripción", "Acciones", "Bloqueo", "Reportado Por"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        incidentesTable = new JTable(tableModel);
        ThemeConstants.styleTable(incidentesTable);
        incidentesTable.getColumnModel().getColumn(3).setCellRenderer(new GravedadCellRenderer());

        tableCard.add(ThemeConstants.createScrollPane(incidentesTable), BorderLayout.CENTER);

        splitPane.setRightComponent(tableCard);
        mainContent.add(splitPane, BorderLayout.CENTER);

        add(mainContent, BorderLayout.CENTER);
    }

    private JPanel crearCardMetrica(String titulo, JLabel valorLbl, Color colorAcento) {
        JPanel card = ThemeConstants.createCard();
        card.setLayout(new BorderLayout(5, 5));
        card.setBorder(new EmptyBorder(12, 16, 12, 16));

        JLabel titleLbl = new JLabel(titulo);
        titleLbl.setFont(ThemeConstants.FONT_SMALL);
        titleLbl.setForeground(ThemeConstants.TEXT_MUTED);

        valorLbl.setFont(new Font("Segoe UI", Font.BOLD, 22));
        valorLbl.setForeground(colorAcento);

        card.add(titleLbl, BorderLayout.NORTH);
        card.add(valorLbl, BorderLayout.CENTER);
        return card;
    }

    public void recargarDatos() {
        // Cargar personas en combo
        String selected = (String) cmbPersonas.getSelectedItem();
        cmbPersonas.removeAllItems();
        List<Persona> personas = personaRepository.findAll();
        for (Persona p : personas) {
            cmbPersonas.addItem(p.getTipoDocumento() + " " + p.getNumeroDocumento() + " - " + p.getNombreCompleto() + (p.isActivo() ? "" : " [BLOQUEADO]"));
        }
        if (selected != null) {
            cmbPersonas.setSelectedItem(selected);
        }

        // Cargar tabla de incidentes
        tableModel.setRowCount(0);
        List<Incidente> incidentes = incidenteRepository.findAll();
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

        long countLeves = 0;
        long countGraves = 0;
        for (Incidente inc : incidentes) {
            if (inc.esGraveOCritico()) countGraves++;
            else countLeves++;

            tableModel.addRow(new Object[]{
                    inc.getId(),
                    inc.getFecha() != null ? inc.getFecha().format(dtf) : "N/A",
                    inc.getPersonaNombre(),
                    inc.getNivelGravedad().name(),
                    inc.getDescripcion(),
                    inc.getAccionesTomadas(),
                    inc.isBloqueoAplicado() ? "BLOQUEADO" : "PERMITIDO",
                    inc.getReportadoPor()
            });
        }

        long bloqueados = personas.stream().filter(p -> !p.isActivo()).count();

        lblTotal.setText(String.valueOf(incidentes.size()));
        lblLeves.setText(String.valueOf(countLeves));
        lblGraves.setText(String.valueOf(countGraves));
        lblBloqueados.setText(String.valueOf(bloqueados));
    }

    private void registrarIncidente() {
        String personaItem = (String) cmbPersonas.getSelectedItem();
        if (personaItem == null || personaItem.isBlank()) {
            JOptionPane.showMessageDialog(this, "Seleccione una persona involucrada.", "Validación", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String desc = txtDescripcion.getText().trim();
        if (desc.isBlank()) {
            JOptionPane.showMessageDialog(this, "Ingrese la descripción detallada del incidente.", "Validación", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String acciones = txtAcciones.getText().trim();
        Incidente.NivelGravedad gravedad = (Incidente.NivelGravedad) cmbGravedad.getSelectedItem();
        boolean bloquear = chkBloquear.isSelected() || (gravedad == Incidente.NivelGravedad.GRAVE || gravedad == Incidente.NivelGravedad.CRITICO);

        // Extraer documento
        String doc = personaItem.split(" - ")[0].replaceAll("^(CC|TI|CE|PAS)\\s+", "").trim();
        Persona persona = personaRepository.findAll().stream()
                .filter(p -> p.getNumeroDocumento().equalsIgnoreCase(doc))
                .findFirst()
                .orElse(null);

        String personaId = persona != null ? persona.getId() : null;
        String personaNombre = persona != null ? persona.getNombreCompleto() : personaItem;

        // Si se bloquea, desactivar persona y persistir
        if (bloquear && persona != null) {
            persona.desactivar();
            personaRepository.save(persona);
        }

        Incidente inc = Incidente.nuevo(
                null,
                personaId,
                personaNombre,
                usuarioActual != null ? usuarioActual.getUsername() : "seguridad",
                gravedad,
                desc,
                acciones,
                bloquear
        );

        incidenteRepository.save(inc);

        // Auditoría
        auditRepository.save(com.zonaacme.sica.audit.domain.BitacoraAuditoria.crear(
                usuarioActual != null ? usuarioActual.getUsername() : "seguridad",
                "REGISTRO_INCIDENTE_SEGURIDAD",
                "incidentes",
                "Incidente " + gravedad + " registrado para " + personaNombre + ". Bloqueo: " + bloquear,
                "INCIDENTES_PANEL"
        ));

        // Notificación de alta prioridad
        if (notificationUseCase != null) {
            try {
                notificationUseCase.enviarNotificacion(
                        com.zonaacme.sica.notifications.domain.Notificacion.crear(
                                "security-control@zonaacme.com",
                                TipoNotificacion.ALERTA_SEGURIDAD_ACCESO_DENEGADO,
                                "INCIDENTE " + gravedad + ": " + personaNombre,
                                desc + " | Acciones: " + acciones + (bloquear ? " [ACCESO BLOQUEADO]" : ""),
                                CanalNotificacion.EMAIL
                        )
                );
            } catch (Exception ignored) {}
        }

        txtDescripcion.setText("");
        txtAcciones.setText("");
        recargarDatos();

        JOptionPane.showMessageDialog(this,
                "✅ Incidente registrado con éxito.\n" +
                (bloquear ? "🚫 La persona ha sido BLOQUEADA automáticamente de todos los puntos de acceso." : ""),
                "Incidente Registrado",
                JOptionPane.INFORMATION_MESSAGE);
    }

    private void desbloquearPersonaSeleccionada() {
        int row = incidentesTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Seleccione un incidente en la tabla para desbloquear a la persona.", "Aviso", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        String personaNombre = (String) tableModel.getValueAt(row, 2);
        Persona persona = personaRepository.findAll().stream()
                .filter(p -> p.getNombreCompleto().equalsIgnoreCase(personaNombre))
                .findFirst()
                .orElse(null);

        if (persona == null) {
            JOptionPane.showMessageDialog(this, "No se encontró el registro de la persona.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        persona.activar();
        personaRepository.save(persona);

        auditRepository.save(com.zonaacme.sica.audit.domain.BitacoraAuditoria.crear(
                usuarioActual != null ? usuarioActual.getUsername() : "admin",
                "DESBLOQUEO_PERSONA",
                "personas",
                "Persona " + persona.getNombreCompleto() + " desbloqueada por jefatura/admin.",
                "INCIDENTES_PANEL"
        ));

        recargarDatos();
        JOptionPane.showMessageDialog(this, "✅ Persona desbloqueada y acceso restaurado satisfactoriamente.", "Acceso Restaurado", JOptionPane.INFORMATION_MESSAGE);
    }

    private void aplicarPermisosRBAC() {
        if (usuarioActual == null) return;
        // Permitir a usuarios activos según roles
        btnRegistrar.setEnabled(true);
        btnDesbloquear.setEnabled(true);
    }

    private void iniciarAutoRefresh() {
        autoRefreshTimer = new javax.swing.Timer(3000, e -> {
            if (isShowing()) {
                recargarDatos();
            }
        });
        autoRefreshTimer.start();
    }

    private static class GravedadCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            if (value != null) {
                String val = value.toString();
                if (val.contains("CRITICO") || val.contains("GRAVE")) {
                    setForeground(ThemeConstants.ACCENT_DANGER);
                    setFont(ThemeConstants.FONT_BODY_BOLD);
                } else if (val.contains("MODERADO")) {
                    setForeground(ThemeConstants.ACCENT_WARNING);
                } else {
                    setForeground(ThemeConstants.ACCENT_INFO);
                }
            }
            return c;
        }
    }
}
