package com.zonaacme.sica.ui.swing.panels;

import com.zonaacme.sica.core.adapters.InMemoryPersonaRepositoryAdapter;
import com.zonaacme.sica.core.adapters.InMemoryZonaRepositoryAdapter;
import com.zonaacme.sica.core.domain.Persona;
import com.zonaacme.sica.core.domain.PuntoControl;
import com.zonaacme.sica.core.domain.TipoPersona;
import com.zonaacme.sica.core.domain.Zona;
import com.zonaacme.sica.ui.swing.ThemeConstants;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class PersonasZonasPanel extends JPanel {

    private final InMemoryPersonaRepositoryAdapter personaRepo;
    private final InMemoryZonaRepositoryAdapter zonaRepo;

    private DefaultTableModel personasTableModel;
    private JTable personasTable;

    private DefaultTableModel zonasTableModel;
    private JTable zonasTable;

    private JPanel contentCards;
    private CardLayout contentCardLayout;
    private JButton btnTabPersonas;
    private JButton btnTabZonas;

    public PersonasZonasPanel(
            InMemoryPersonaRepositoryAdapter personaRepo,
            InMemoryZonaRepositoryAdapter zonaRepo
    ) {
        this.personaRepo = personaRepo;
        this.zonaRepo = zonaRepo;

        setLayout(new BorderLayout(20, 20));
        setBackground(ThemeConstants.BG_DARK);
        setBorder(new EmptyBorder(24, 28, 24, 28));

        initUI();
        cargarPersonas();
        cargarZonas();
    }

    private void initUI() {
        // Encabezado
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        JLabel title = new JLabel("Gestión de Personas y Zonas de Seguridad");
        title.setFont(ThemeConstants.FONT_TITLE);
        title.setForeground(ThemeConstants.TEXT_PRIMARY);

        JLabel subtitle = new JLabel("Directorio de trabajadores, visitantes y configuración de áreas restringidas");
        subtitle.setFont(ThemeConstants.FONT_BODY);
        subtitle.setForeground(ThemeConstants.TEXT_SECONDARY);

        headerPanel.add(title, BorderLayout.NORTH);
        headerPanel.add(subtitle, BorderLayout.SOUTH);

        // Barra de pestañas moderna (Segmented Switcher) con alto contraste
        JPanel tabSwitcher = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        tabSwitcher.setOpaque(false);
        tabSwitcher.setBorder(new EmptyBorder(0, 0, 8, 0));

        btnTabPersonas = ThemeConstants.createGradientButton(
                "👤 Directorio de Personas",
                ThemeConstants.ACCENT_PRIMARY,
                ThemeConstants.ACCENT_CYAN,
                Color.WHITE
        );
        btnTabZonas = ThemeConstants.createButton(
                "🏢 Zonas y Puntos de Control",
                ThemeConstants.BG_CARD,
                ThemeConstants.TEXT_SECONDARY
        );

        contentCardLayout = new CardLayout();
        contentCards = new JPanel(contentCardLayout);
        contentCards.setOpaque(false);

        btnTabPersonas.addActionListener(e -> {
            btnTabPersonas.setBackground(ThemeConstants.ACCENT_PRIMARY);
            btnTabPersonas.setForeground(Color.WHITE);
            btnTabZonas.setBackground(ThemeConstants.BG_CARD);
            btnTabZonas.setForeground(ThemeConstants.TEXT_SECONDARY);
            contentCardLayout.show(contentCards, "PERSONAS");
            cargarPersonas();
        });

        btnTabZonas.addActionListener(e -> {
            btnTabZonas.setBackground(ThemeConstants.ACCENT_PRIMARY);
            btnTabZonas.setForeground(Color.WHITE);
            btnTabPersonas.setBackground(ThemeConstants.BG_CARD);
            btnTabPersonas.setForeground(ThemeConstants.TEXT_SECONDARY);
            contentCardLayout.show(contentCards, "ZONAS");
            cargarZonas();
        });

        tabSwitcher.add(btnTabPersonas);
        tabSwitcher.add(btnTabZonas);

        // Card 1: Personas
        JPanel panelPersonas = new JPanel(new BorderLayout(0, 16));
        panelPersonas.setOpaque(false);

        JPanel toolbarPersonas = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        toolbarPersonas.setOpaque(false);

        JButton btnNuevaPersona = ThemeConstants.createButton("Registrar Persona", ThemeConstants.ACCENT_PRIMARY, Color.WHITE);
        btnNuevaPersona.addActionListener(e -> mostrarModalNuevaPersona());

        JButton btnHabilitar = ThemeConstants.createButton("Habilitar Acceso", ThemeConstants.ACCENT_SUCCESS, Color.WHITE);
        btnHabilitar.addActionListener(e -> alternarEstadoPersona(true));

        JButton btnBloquear = ThemeConstants.createButton("Bloquear Acceso", ThemeConstants.ACCENT_DANGER, Color.WHITE);
        btnBloquear.addActionListener(e -> alternarEstadoPersona(false));

        toolbarPersonas.add(btnNuevaPersona);
        toolbarPersonas.add(btnHabilitar);
        toolbarPersonas.add(btnBloquear);

        String[] colsPersonas = {"ID", "Documento", "Nombre Completo", "Tipo", "Empresa", "Email", "Teléfono", "Estado"};
        personasTableModel = new DefaultTableModel(colsPersonas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        personasTable = new JTable(personasTableModel);
        ThemeConstants.styleTable(personasTable);

        JScrollPane scrollPersonas = new JScrollPane(personasTable);
        scrollPersonas.getViewport().setBackground(ThemeConstants.BG_CARD);
        scrollPersonas.setBorder(BorderFactory.createEmptyBorder());

        JPanel cardPersonasTable = ThemeConstants.createCard();
        cardPersonasTable.setLayout(new BorderLayout(0, 12));
        cardPersonasTable.add(toolbarPersonas, BorderLayout.NORTH);
        cardPersonasTable.add(scrollPersonas, BorderLayout.CENTER);
        panelPersonas.add(cardPersonasTable, BorderLayout.CENTER);

        // Card 2: Zonas de Seguridad
        JPanel panelZonas = new JPanel(new BorderLayout(0, 16));
        panelZonas.setOpaque(false);

        String[] colsZonas = {"Código", "Nombre de Zona", "Aforo Máx", "Horario", "Puntos de Control Asociados", "Descripción"};
        zonasTableModel = new DefaultTableModel(colsZonas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        zonasTable = new JTable(zonasTableModel);
        ThemeConstants.styleTable(zonasTable);

        JScrollPane scrollZonas = new JScrollPane(zonasTable);
        scrollZonas.getViewport().setBackground(ThemeConstants.BG_CARD);
        scrollZonas.setBorder(BorderFactory.createEmptyBorder());

        JPanel cardZonasTable = ThemeConstants.createCard();
        cardZonasTable.setLayout(new BorderLayout(0, 12));
        cardZonasTable.add(scrollZonas, BorderLayout.CENTER);
        panelZonas.add(cardZonasTable, BorderLayout.CENTER);

        contentCards.add(panelPersonas, "PERSONAS");
        contentCards.add(panelZonas, "ZONAS");

        JPanel mainCenter = new JPanel(new BorderLayout(0, 12));
        mainCenter.setOpaque(false);
        mainCenter.add(tabSwitcher, BorderLayout.NORTH);
        mainCenter.add(contentCards, BorderLayout.CENTER);

        add(headerPanel, BorderLayout.NORTH);
        add(mainCenter, BorderLayout.CENTER);
    }

    public void cargarPersonas() {
        personasTableModel.setRowCount(0);
        List<Persona> lista = personaRepo.findAll();
        for (Persona p : lista) {
            String estado = p.isActivo() ? "ACTIVO" : "BLOQUEADO";
            personasTableModel.addRow(new Object[]{
                    p.getId(),
                    p.getTipoDocumento() + " " + p.getNumeroDocumento(),
                    p.getNombreCompleto(),
                    p.getTipoPersona().name(),
                    p.getEmpresa(),
                    p.getEmail(),
                    p.getTelefono(),
                    estado
            });
        }
    }

    public void cargarZonas() {
        zonasTableModel.setRowCount(0);
        List<Zona> lista = zonaRepo.findAllZonas();
        for (Zona z : lista) {
            List<PuntoControl> pcs = zonaRepo.findPuntosControlByZonaId(z.getId());
            StringBuilder pcCodes = new StringBuilder();
            for (PuntoControl pc : pcs) {
                if (pcCodes.length() > 0) pcCodes.append(", ");
                pcCodes.append(pc.getCodigo());
            }
            String horario = z.getHoraInicioPermitida() + " - " + z.getHoraFinPermitida();

            zonasTableModel.addRow(new Object[]{
                    z.getCodigo(),
                    z.getNombre(),
                    z.getAforoMaximo(),
                    horario,
                    pcCodes.toString(),
                    z.getDescripcion()
            });
        }
    }

    private void alternarEstadoPersona(boolean activar) {
        int row = personasTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Seleccione una persona de la tabla", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String personaId = (String) personasTableModel.getValueAt(row, 0);
        Persona p = personaRepo.findById(personaId).orElse(null);
        if (p != null) {
            if (activar) {
                p.activar();
            } else {
                p.desactivar();
            }
            personaRepo.save(p);
            JOptionPane.showMessageDialog(this, "Estado actualizado para: " + p.getNombreCompleto(), "Éxito", JOptionPane.INFORMATION_MESSAGE);
            cargarPersonas();
        }
    }

    private void mostrarModalNuevaPersona() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Registrar Nueva Persona", true);
        dialog.setLayout(new BorderLayout(16, 16));
        dialog.getContentPane().setBackground(ThemeConstants.BG_DARK);
        dialog.setSize(500, 520);
        dialog.setLocationRelativeTo(this);

        JPanel form = new JPanel(new GridLayout(7, 2, 12, 14));
        form.setOpaque(false);
        form.setBorder(new EmptyBorder(20, 24, 20, 24));

        JComboBox<String> comboTipoDoc = ThemeConstants.createComboBox(new String[]{"CC", "CE", "PASAPORTE", "TI"});
        JTextField txtDoc = ThemeConstants.createTextField();
        JTextField txtNombres = ThemeConstants.createTextField();
        JTextField txtApellidos = ThemeConstants.createTextField();
        JTextField txtEmpresa = ThemeConstants.createTextField();
        JTextField txtEmail = ThemeConstants.createTextField();
        JComboBox<TipoPersona> comboTipo = ThemeConstants.createComboBox(TipoPersona.values());

        form.add(ThemeConstants.createLabel("Tipo Documento:"));
        form.add(comboTipoDoc);
        form.add(ThemeConstants.createLabel("Número Documento:"));
        form.add(txtDoc);
        form.add(ThemeConstants.createLabel("Nombres:"));
        form.add(txtNombres);
        form.add(ThemeConstants.createLabel("Apellidos:"));
        form.add(txtApellidos);
        form.add(ThemeConstants.createLabel("Empresa / Dependencia:"));
        form.add(txtEmpresa);
        form.add(ThemeConstants.createLabel("Email:"));
        form.add(txtEmail);
        form.add(ThemeConstants.createLabel("Tipo de Persona:"));
        form.add(comboTipo);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 16));
        bottom.setOpaque(false);

        JButton btnCancelar = ThemeConstants.createButton("Cancelar", ThemeConstants.BG_CARD, ThemeConstants.TEXT_SECONDARY);
        btnCancelar.addActionListener(e -> dialog.dispose());

        JButton btnGuardar = ThemeConstants.createButton("Guardar Persona", ThemeConstants.ACCENT_PRIMARY, Color.WHITE);
        btnGuardar.addActionListener(e -> {
            try {
                Persona nueva = Persona.nuevo(
                        (String) comboTipoDoc.getSelectedItem(),
                        txtDoc.getText().trim(),
                        txtNombres.getText().trim(),
                        txtApellidos.getText().trim(),
                        txtEmail.getText().trim(),
                        "3001234567",
                        txtEmpresa.getText().trim(),
                        (TipoPersona) comboTipo.getSelectedItem()
                );
                personaRepo.save(nueva);
                JOptionPane.showMessageDialog(dialog, "Persona registrada con éxito", "Éxito", JOptionPane.INFORMATION_MESSAGE);
                dialog.dispose();
                cargarPersonas();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        bottom.add(btnCancelar);
        bottom.add(btnGuardar);

        dialog.add(form, BorderLayout.CENTER);
        dialog.add(bottom, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }
}
