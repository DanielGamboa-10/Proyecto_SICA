package com.zonaacme.sica.ui.swing.panels;

import com.zonaacme.sica.audit.adapters.AuditService;
import com.zonaacme.sica.audit.adapters.InMemoryAuditRepositoryAdapter;
import com.zonaacme.sica.audit.domain.BitacoraAuditoria;
import com.zonaacme.sica.ui.swing.ThemeConstants;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class AuditoriaPanel extends JPanel {

    private final InMemoryAuditRepositoryAdapter auditRepo;
    private DefaultTableModel tableModel;
    private JTextField txtBuscar;

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public AuditoriaPanel(InMemoryAuditRepositoryAdapter auditRepo) {
        this.auditRepo = auditRepo;

        setLayout(new BorderLayout(20, 20));
        setBackground(ThemeConstants.BG_DARK);
        setBorder(new EmptyBorder(24, 28, 24, 28));

        initUI();
        cargarAuditoria();
    }

    private void initUI() {
        // Encabezado
        JPanel topPanel = new JPanel(new BorderLayout(16, 16));
        topPanel.setOpaque(false);

        JPanel headerText = new JPanel(new BorderLayout());
        headerText.setOpaque(false);
        JLabel title = new JLabel("Bitácora de Auditoría Inmutable");
        title.setFont(ThemeConstants.FONT_TITLE);
        title.setForeground(ThemeConstants.TEXT_PRIMARY);

        JLabel subtitle = new JLabel("Trazabilidad y registro forense de todas las acciones y eventos del sistema");
        subtitle.setFont(ThemeConstants.FONT_BODY);
        subtitle.setForeground(ThemeConstants.TEXT_SECONDARY);

        headerText.add(title, BorderLayout.NORTH);
        headerText.add(subtitle, BorderLayout.SOUTH);

        // Barra de Búsqueda y Refrescar
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        toolbar.setOpaque(false);

        txtBuscar = ThemeConstants.createTextField();
        txtBuscar.setPreferredSize(new Dimension(200, 36));

        JButton btnBuscar = ThemeConstants.createButton("Buscar", ThemeConstants.ACCENT_PRIMARY, Color.WHITE);
        btnBuscar.addActionListener(e -> cargarAuditoria());

        JButton btnRefrescar = ThemeConstants.createButton("Refrescar", ThemeConstants.BG_CARD_HOVER, ThemeConstants.TEXT_PRIMARY);
        btnRefrescar.addActionListener(e -> {
            txtBuscar.setText("");
            cargarAuditoria();
        });

        toolbar.add(ThemeConstants.createLabel("Filtrar: "));
        toolbar.add(txtBuscar);
        toolbar.add(btnBuscar);
        toolbar.add(btnRefrescar);

        topPanel.add(headerText, BorderLayout.WEST);
        topPanel.add(toolbar, BorderLayout.EAST);

        // Tabla de Auditoría
        JPanel tableContainer = ThemeConstants.createCard();
        tableContainer.setLayout(new BorderLayout(0, 10));

        String[] columns = {"Fecha y Hora", "Acción", "Entidad Afectada", "Usuario / Actor", "Origen", "Detalle"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        JTable table = new JTable(tableModel);
        ThemeConstants.styleTable(table);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.getViewport().setBackground(ThemeConstants.BG_CARD);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        tableContainer.add(scrollPane, BorderLayout.CENTER);

        add(topPanel, BorderLayout.NORTH);
        add(tableContainer, BorderLayout.CENTER);
    }

    public void cargarAuditoria() {
        tableModel.setRowCount(0);
        String query = txtBuscar.getText().trim().toLowerCase();

        List<BitacoraAuditoria> list = auditRepo.findAll();
        for (int i = list.size() - 1; i >= 0; i--) {
            BitacoraAuditoria b = list.get(i);
            if (!query.isEmpty()) {
                boolean match = b.getAccion().toLowerCase().contains(query)
                        || b.getEntidadAfectada().toLowerCase().contains(query)
                        || b.getUsuarioId().toLowerCase().contains(query)
                        || b.getDetalle().toLowerCase().contains(query);
                if (!match) continue;
            }

            tableModel.addRow(new Object[]{
                    b.getFechaHora().format(DATE_FMT),
                    b.getAccion(),
                    b.getEntidadAfectada(),
                    b.getUsuarioId(),
                    b.getOrigen(),
                    b.getDetalle()
            });
        }
    }
}
