package com.zonaacme.sica.ui.swing.panels;

import com.zonaacme.sica.auth.domain.SesionUsuario;
import com.zonaacme.sica.core.adapters.InMemoryPersonaRepositoryAdapter;
import com.zonaacme.sica.core.adapters.InMemoryVisitaRepositoryAdapter;
import com.zonaacme.sica.core.adapters.InMemoryZonaRepositoryAdapter;
import com.zonaacme.sica.core.adapters.VisitaService;
import com.zonaacme.sica.core.domain.EstadoVisita;
import com.zonaacme.sica.core.domain.Persona;
import com.zonaacme.sica.core.domain.SolicitudVisita;
import com.zonaacme.sica.core.domain.Zona;
import com.zonaacme.sica.ui.swing.ThemeConstants;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class VisitasPanel extends JPanel {

    private final VisitaService visitaService;
    private final InMemoryVisitaRepositoryAdapter visitaRepo;
    private final InMemoryPersonaRepositoryAdapter personaRepo;
    private final InMemoryZonaRepositoryAdapter zonaRepo;
    private SesionUsuario sesionActual;

    private JTable visitasTable;
    private DefaultTableModel tableModel;
    private JComboBox<String> comboFiltroEstado;

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public VisitasPanel(
            VisitaService visitaService,
            InMemoryVisitaRepositoryAdapter visitaRepo,
            InMemoryPersonaRepositoryAdapter personaRepo,
            InMemoryZonaRepositoryAdapter zonaRepo,
            SesionUsuario sesionActual
    ) {
        this.visitaService = visitaService;
        this.visitaRepo = visitaRepo;
        this.personaRepo = personaRepo;
        this.zonaRepo = zonaRepo;
        this.sesionActual = sesionActual;

        setLayout(new BorderLayout(20, 20));
        setBackground(ThemeConstants.BG_DARK);
        setBorder(new EmptyBorder(24, 28, 24, 28));

        initUI();
        cargarVisitas();
    }

    public void setSesionActual(SesionUsuario sesionActual) {
        this.sesionActual = sesionActual;
    }

    private void initUI() {
        // Encabezado con Botones de Acción
        JPanel topPanel = new JPanel(new BorderLayout(16, 16));
        topPanel.setOpaque(false);

        JPanel headerText = new JPanel(new BorderLayout());
        headerText.setOpaque(false);
        JLabel title = new JLabel("Gestión y Aprobación de Visitas");
        title.setFont(ThemeConstants.FONT_TITLE);
        title.setForeground(ThemeConstants.TEXT_PRIMARY);

        JLabel subtitle = new JLabel("Pre-registro, validación de anfitriones, aprobación y control de estancia");
        subtitle.setFont(ThemeConstants.FONT_BODY);
        subtitle.setForeground(ThemeConstants.TEXT_SECONDARY);

        headerText.add(title, BorderLayout.NORTH);
        headerText.add(subtitle, BorderLayout.SOUTH);

        // Barra de Herramientas / Acciones
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        toolbar.setOpaque(false);

        comboFiltroEstado = ThemeConstants.createComboBox(new String[]{
                "TODOS", "PENDIENTE", "APROBADA", "EN_CURSO", "COMPLETADA", "RECHAZADA", "CANCELADA"
        });
        comboFiltroEstado.addActionListener(e -> cargarVisitas());

        JButton btnNuevaVisita = ThemeConstants.createButton("Nueva Solicitud", ThemeConstants.ACCENT_PRIMARY, Color.WHITE);
        btnNuevaVisita.addActionListener(e -> mostrarModalNuevaVisita());

        JButton btnAprobar = ThemeConstants.createButton("Aprobar", ThemeConstants.ACCENT_SUCCESS, Color.WHITE);
        btnAprobar.addActionListener(e -> accionarAprobacion(true));

        JButton btnRechazar = ThemeConstants.createButton("Rechazar", ThemeConstants.ACCENT_DANGER, Color.WHITE);
        btnRechazar.addActionListener(e -> accionarAprobacion(false));

        JButton btnCheckIn = ThemeConstants.createButton("Check-In", ThemeConstants.ACCENT_INFO, Color.WHITE);
        btnCheckIn.addActionListener(e -> accionarCheckIn());

        JButton btnCheckOut = ThemeConstants.createButton("Check-Out", ThemeConstants.BG_CARD_HOVER, ThemeConstants.TEXT_PRIMARY);
        btnCheckOut.addActionListener(e -> accionarCheckOut());

        toolbar.add(ThemeConstants.createLabel("Filtrar: "));
        toolbar.add(comboFiltroEstado);
        toolbar.add(btnNuevaVisita);
        toolbar.add(btnAprobar);
        toolbar.add(btnRechazar);
        toolbar.add(btnCheckIn);
        toolbar.add(btnCheckOut);

        topPanel.add(headerText, BorderLayout.WEST);
        topPanel.add(toolbar, BorderLayout.EAST);

        // Tabla de Visitas
        JPanel tableContainer = ThemeConstants.createCard();
        tableContainer.setLayout(new BorderLayout(0, 10));

        String[] columns = {
                "ID Solicitud", "Visitante", "Documento", "Anfitrión", "Motivo", "Inicio Previsto", "Fin Previsto", "Estado"
        };
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        visitasTable = new JTable(tableModel);
        ThemeConstants.styleTable(visitasTable);
        visitasTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JScrollPane scrollPane = new JScrollPane(visitasTable);
        scrollPane.getViewport().setBackground(ThemeConstants.BG_CARD);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        tableContainer.add(scrollPane, BorderLayout.CENTER);

        add(topPanel, BorderLayout.NORTH);
        add(tableContainer, BorderLayout.CENTER);
    }

    public void cargarVisitas() {
        tableModel.setRowCount(0);
        String filtro = (String) comboFiltroEstado.getSelectedItem();

        List<SolicitudVisita> visitas = visitaRepo.findAll();
        for (SolicitudVisita v : visitas) {
            if (!"TODOS".equals(filtro) && !v.getEstado().name().equalsIgnoreCase(filtro)) {
                continue;
            }

            Persona vis = personaRepo.findById(v.getVisitanteId()).orElse(null);
            Persona anf = personaRepo.findById(v.getAnfitrionId()).orElse(null);

            String visitanteNombre = vis != null ? vis.getNombreCompleto() : v.getVisitanteId();
            String doc = vis != null ? vis.getTipoDocumento() + " " + vis.getNumeroDocumento() : "N/A";
            String anfitrionNombre = anf != null ? anf.getNombreCompleto() : v.getAnfitrionId();

            tableModel.addRow(new Object[]{
                    v.getId(),
                    visitanteNombre,
                    doc,
                    anfitrionNombre,
                    v.getMotivo(),
                    v.getFechaHoraInicio().format(DATE_FMT),
                    v.getFechaHoraFin().format(DATE_FMT),
                    v.getEstado().name()
            });
        }
    }

    private void mostrarModalNuevaVisita() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Nueva Solicitud de Visita", true);
        dialog.setLayout(new BorderLayout(16, 16));
        dialog.getContentPane().setBackground(ThemeConstants.BG_DARK);
        dialog.setSize(500, 480);
        dialog.setLocationRelativeTo(this);

        JPanel content = new JPanel(new GridLayout(6, 2, 10, 12));
        content.setOpaque(false);
        content.setBorder(new EmptyBorder(20, 20, 20, 20));

        // Personas para anfitrión y visitante
        List<Persona> personas = personaRepo.findAll();
        DefaultComboBoxModel<PersonaComboItem> visitanteModel = new DefaultComboBoxModel<>();
        DefaultComboBoxModel<PersonaComboItem> anfitrionModel = new DefaultComboBoxModel<>();

        for (Persona p : personas) {
            visitanteModel.addElement(new PersonaComboItem(p.getId(), p.getNombreCompleto() + " (" + p.getTipoPersona() + ")"));
            anfitrionModel.addElement(new PersonaComboItem(p.getId(), p.getNombreCompleto() + " (" + p.getTipoPersona() + ")"));
        }

        JComboBox<PersonaComboItem> comboVisitante = new JComboBox<>(visitanteModel);
        comboVisitante.setBackground(ThemeConstants.BG_INPUT);
        comboVisitante.setForeground(ThemeConstants.TEXT_PRIMARY);

        JComboBox<PersonaComboItem> comboAnfitrion = new JComboBox<>(anfitrionModel);
        comboAnfitrion.setBackground(ThemeConstants.BG_INPUT);
        comboAnfitrion.setForeground(ThemeConstants.TEXT_PRIMARY);

        JTextField txtMotivo = ThemeConstants.createTextField();
        txtMotivo.setText("Reunión de consultoría técnica");

        JTextField txtHoras = ThemeConstants.createTextField();
        txtHoras.setText("4");

        JTextField txtPlaca = ThemeConstants.createTextField();
        txtPlaca.setText("ABC-123");

        content.add(ThemeConstants.createLabel("Visitante:"));
        content.add(comboVisitante);
        content.add(ThemeConstants.createLabel("Anfitrión:"));
        content.add(comboAnfitrion);
        content.add(ThemeConstants.createLabel("Motivo de Visita:"));
        content.add(txtMotivo);
        content.add(ThemeConstants.createLabel("Duración (Horas):"));
        content.add(txtHoras);
        content.add(ThemeConstants.createLabel("Placa Vehículo (Opcional):"));
        content.add(txtPlaca);

        JButton btnGuardar = ThemeConstants.createButton("Crear Solicitud", ThemeConstants.ACCENT_PRIMARY, Color.WHITE);
        btnGuardar.addActionListener(e -> {
            try {
                PersonaComboItem selVis = (PersonaComboItem) comboVisitante.getSelectedItem();
                PersonaComboItem selAnf = (PersonaComboItem) comboAnfitrion.getSelectedItem();
                if (selVis == null || selAnf == null) return;

                int horas = Integer.parseInt(txtHoras.getText().trim());
                LocalDateTime inicio = LocalDateTime.now();
                LocalDateTime fin = inicio.plusHours(horas);

                Set<String> zonas = new HashSet<>();
                zonaRepo.findAllZonas().forEach(z -> zonas.add(z.getId()));

                visitaService.solicitarVisita(
                        selVis.id,
                        selAnf.id,
                        txtMotivo.getText().trim(),
                        inicio,
                        fin,
                        zonas,
                        txtPlaca.getText().trim().isEmpty() ? null : txtPlaca.getText().trim(),
                        sesionActual.getToken()
                );

                JOptionPane.showMessageDialog(dialog, "¡Solicitud de visita registrada con éxito!", "Éxito", JOptionPane.INFORMATION_MESSAGE);
                dialog.dispose();
                cargarVisitas();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        dialog.add(content, BorderLayout.CENTER);
        dialog.add(btnGuardar, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private void accionarAprobacion(boolean aprobar) {
        int row = visitasTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Seleccione una visita de la tabla", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String visitaId = (String) tableModel.getValueAt(row, 0);
        String obs = JOptionPane.showInputDialog(this, "Ingrese una observación / motivo:", aprobar ? "Visita autorizada por recepción" : "Rechazada por política interna");
        if (obs == null) return;

        try {
            if (aprobar) {
                visitaService.aprobarVisita(visitaId, obs, sesionActual.getToken());
                JOptionPane.showMessageDialog(this, "Visita " + visitaId + " aprobada correctamente.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
            } else {
                visitaService.rechazarVisita(visitaId, obs, sesionActual.getToken());
                JOptionPane.showMessageDialog(this, "Visita " + visitaId + " rechazada.", "Aviso", JOptionPane.INFORMATION_MESSAGE);
            }
            cargarVisitas();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void accionarCheckIn() {
        int row = visitasTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Seleccione una visita de la tabla", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String visitaId = (String) tableModel.getValueAt(row, 0);
        try {
            SolicitudVisita v = visitaRepo.findById(visitaId).orElseThrow();
            v.registrarIngreso();
            visitaRepo.save(v);
            JOptionPane.showMessageDialog(this, "Check-In registrado para la visita " + visitaId, "Éxito", JOptionPane.INFORMATION_MESSAGE);
            cargarVisitas();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void accionarCheckOut() {
        int row = visitasTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Seleccione una visita de la tabla", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String visitaId = (String) tableModel.getValueAt(row, 0);
        try {
            SolicitudVisita v = visitaRepo.findById(visitaId).orElseThrow();
            v.registrarSalida();
            visitaRepo.save(v);
            JOptionPane.showMessageDialog(this, "Check-Out registrado para la visita " + visitaId, "Éxito", JOptionPane.INFORMATION_MESSAGE);
            cargarVisitas();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private static class PersonaComboItem {
        final String id;
        final String label;

        PersonaComboItem(String id, String label) {
            this.id = id;
            this.label = label;
        }

        @Override
        public String toString() {
            return label;
        }
    }
}
