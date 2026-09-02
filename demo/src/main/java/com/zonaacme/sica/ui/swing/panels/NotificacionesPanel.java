package com.zonaacme.sica.ui.swing.panels;

import com.zonaacme.sica.notifications.adapters.InMemoryNotificationRepositoryAdapter;
import com.zonaacme.sica.notifications.adapters.NotificationService;
import com.zonaacme.sica.notifications.domain.CanalNotificacion;
import com.zonaacme.sica.notifications.domain.Notificacion;
import com.zonaacme.sica.notifications.domain.TipoNotificacion;
import com.zonaacme.sica.ui.swing.ThemeConstants;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class NotificacionesPanel extends JPanel {

    private final NotificationService notificationService;
    private final InMemoryNotificationRepositoryAdapter notificationRepo;
    private DefaultTableModel tableModel;
    private JTable notificacionesTable;

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public NotificacionesPanel(NotificationService notificationService, InMemoryNotificationRepositoryAdapter notificationRepo) {
        this.notificationService = notificationService;
        this.notificationRepo = notificationRepo;

        setLayout(new BorderLayout(20, 20));
        setBackground(ThemeConstants.BG_DARK);
        setBorder(new EmptyBorder(24, 28, 24, 28));

        initUI();
        cargarNotificaciones();
    }

    private void initUI() {
        // Encabezado
        JPanel topPanel = new JPanel(new BorderLayout(16, 16));
        topPanel.setOpaque(false);

        JPanel headerText = new JPanel(new BorderLayout());
        headerText.setOpaque(false);
        JLabel title = new JLabel("Centro de Notificaciones y Alertas");
        title.setFont(ThemeConstants.FONT_TITLE);
        title.setForeground(ThemeConstants.TEXT_PRIMARY);

        JLabel subtitle = new JLabel("Monitoreo de alertas de seguridad, avisos de visita y confirmaciones");
        subtitle.setFont(ThemeConstants.FONT_BODY);
        subtitle.setForeground(ThemeConstants.TEXT_SECONDARY);

        headerText.add(title, BorderLayout.NORTH);
        headerText.add(subtitle, BorderLayout.SOUTH);

        // Barra de Herramientas
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        toolbar.setOpaque(false);

        JButton btnEmitirAlerta = ThemeConstants.createButton("Emitir Alerta Manual", ThemeConstants.ACCENT_DANGER, Color.WHITE);
        btnEmitirAlerta.addActionListener(e -> emitirAlertaManual());

        JButton btnMarcarLeida = ThemeConstants.createButton("Marcar como Leída", ThemeConstants.BG_CARD_HOVER, ThemeConstants.TEXT_PRIMARY);
        btnMarcarLeida.addActionListener(e -> marcarSeleccionadaLeida());

        JButton btnRefrescar = ThemeConstants.createButton("Refrescar", ThemeConstants.BG_CARD_HOVER, ThemeConstants.TEXT_PRIMARY);
        btnRefrescar.addActionListener(e -> cargarNotificaciones());

        toolbar.add(btnEmitirAlerta);
        toolbar.add(btnMarcarLeida);
        toolbar.add(btnRefrescar);

        topPanel.add(headerText, BorderLayout.WEST);
        topPanel.add(toolbar, BorderLayout.EAST);

        // Tabla de Notificaciones
        JPanel tableContainer = ThemeConstants.createCard();
        tableContainer.setLayout(new BorderLayout(0, 10));

        String[] columns = {"ID", "Fecha/Hora", "Tipo", "Canal", "Destinatario", "Asunto", "Mensaje", "Estado"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        notificacionesTable = new JTable(tableModel);
        ThemeConstants.styleTable(notificacionesTable);

        JScrollPane scrollPane = new JScrollPane(notificacionesTable);
        scrollPane.getViewport().setBackground(ThemeConstants.BG_CARD);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        tableContainer.add(scrollPane, BorderLayout.CENTER);

        add(topPanel, BorderLayout.NORTH);
        add(tableContainer, BorderLayout.CENTER);
    }

    public void cargarNotificaciones() {
        tableModel.setRowCount(0);
        List<Notificacion> list = notificationRepo.findAll();
        for (Notificacion n : list) {
            String tipoBadge = n.getTipo().esCritico() ? "ALERTA" : "NOTIFICACIÓN";
            String estadoBadge = n.isLeida() ? "Leída" : "No Leída";

            tableModel.addRow(new Object[]{
                    n.getId(),
                    n.getFechaHora().format(DATE_FMT),
                    tipoBadge,
                    n.getCanal().name(),
                    n.getDestinatarioId(),
                    n.getAsunto(),
                    n.getCuerpo(),
                    estadoBadge
            });
        }
    }

    private void marcarSeleccionadaLeida() {
        int row = notificacionesTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Seleccione una notificación de la tabla", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String notifId = (String) tableModel.getValueAt(row, 0);
        notificationService.marcarComoLeida(notifId);
        cargarNotificaciones();
    }

    private void emitirAlertaManual() {
        String mensaje = JOptionPane.showInputDialog(this, "Ingrese el mensaje de alerta general para seguridad:", "Alerta de Seguridad Manual");
        if (mensaje == null || mensaje.trim().isEmpty()) return;

        Notificacion alerta = Notificacion.crear(
                "CENTRAL_SEGURIDAD",
                TipoNotificacion.ALERTA_SEGURIDAD_ACCESO_DENEGADO,
                "ALERTA MANUAL: " + mensaje.trim(),
                "Emitida manualmente por el operador de control de seguridad.",
                CanalNotificacion.CONSOLA_INTERNA
        );
        notificationService.enviarNotificacion(alerta);

        JOptionPane.showMessageDialog(this, "Alerta emitida y transmitida.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
        cargarNotificaciones();
    }
}
