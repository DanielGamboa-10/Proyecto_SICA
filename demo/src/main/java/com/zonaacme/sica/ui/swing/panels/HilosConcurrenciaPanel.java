package com.zonaacme.sica.ui.swing.panels;

import com.zonaacme.sica.concurrency.GestorHilosSica;
import com.zonaacme.sica.concurrency.threads.SimuladorTorniquetesThread;
import com.zonaacme.sica.ui.swing.ThemeConstants;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Panel de visualización y control de Hilos Definidos por el Usuario (Multithreading).
 */
public class HilosConcurrenciaPanel extends JPanel {

    private DefaultTableModel tableModel;
    private JTable hilosTable;
    private JLabel lblSimuladorStatus;
    private JButton btnToggleSimulador;
    private Timer timerRefresco;

    public HilosConcurrenciaPanel() {
        setLayout(new BorderLayout(20, 20));
        setBackground(ThemeConstants.BG_DARK);
        setBorder(new EmptyBorder(24, 28, 24, 28));

        initUI();
        refrescarTabla();

        // Timer de refresco cada 1.5 segundos para reflejar los hilos en tiempo real
        timerRefresco = new Timer(1500, e -> refrescarTabla());
        timerRefresco.start();
    }

    private void initUI() {
        // Encabezado
        JPanel topPanel = new JPanel(new BorderLayout(16, 16));
        topPanel.setOpaque(false);

        JPanel headerText = new JPanel(new BorderLayout());
        headerText.setOpaque(false);
        JLabel title = new JLabel("🧵 Monitor de Hilos y Concurrencia (Multithreading)");
        title.setFont(ThemeConstants.FONT_TITLE);
        title.setForeground(ThemeConstants.TEXT_PRIMARY);

        JLabel subtitle = new JLabel("Hilos definidos por el usuario para monitoreo en segundo plano, auditoría asíncrona y simulación");
        subtitle.setFont(ThemeConstants.FONT_BODY);
        subtitle.setForeground(ThemeConstants.TEXT_SECONDARY);

        headerText.add(title, BorderLayout.NORTH);
        headerText.add(subtitle, BorderLayout.SOUTH);

        // Toolbar
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        toolbar.setOpaque(false);

        JButton btnRefrescar = ThemeConstants.createButton("Refrescar Hilos", ThemeConstants.BG_CARD_HOVER, ThemeConstants.TEXT_PRIMARY);
        btnRefrescar.addActionListener(e -> refrescarTabla());

        btnToggleSimulador = ThemeConstants.createButton("▶ Iniciar Simulador Concurrente", ThemeConstants.ACCENT_PRIMARY, Color.WHITE);
        btnToggleSimulador.addActionListener(e -> toggleSimulador());

        toolbar.add(btnRefrescar);
        toolbar.add(btnToggleSimulador);

        topPanel.add(headerText, BorderLayout.WEST);
        topPanel.add(toolbar, BorderLayout.EAST);

        // Tabla de Hilos
        JPanel tableContainer = ThemeConstants.createCard();
        tableContainer.setLayout(new BorderLayout(0, 14));

        JLabel tableTitle = new JLabel("Estado de Hilos de Usuario en Tiempo Real:");
        tableTitle.setFont(ThemeConstants.FONT_SUBTITLE);
        tableTitle.setForeground(ThemeConstants.TEXT_PRIMARY);
        tableContainer.add(tableTitle, BorderLayout.NORTH);

        String[] columns = {"Nombre del Hilo", "Estado de Ejecución", "Activo / Vivo", "Métricas y Tareas Concurrente"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        hilosTable = new JTable(tableModel);
        ThemeConstants.styleTable(hilosTable);

        JScrollPane scrollPane = new JScrollPane(hilosTable);
        scrollPane.getViewport().setBackground(ThemeConstants.BG_CARD);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        tableContainer.add(scrollPane, BorderLayout.CENTER);

        // Panel Informativo Explicativo
        JPanel infoCard = ThemeConstants.createCard();
        infoCard.setLayout(new GridLayout(3, 1, 6, 6));

        JLabel info1 = new JLabel("1. MonitorVisitasVencidasThread: Hilo Daemon periódico que escanea y cierra visitas vencidas en MySQL.");
        info1.setFont(ThemeConstants.FONT_SMALL);
        info1.setForeground(ThemeConstants.TEXT_SECONDARY);

        JLabel info2 = new JLabel("2. BitacoraAsyncWorkerThread: Hilo Consumidor en segundo plano para registrar auditoría asíncrona sin bloquear la UI.");
        info2.setFont(ThemeConstants.FONT_SMALL);
        info2.setForeground(ThemeConstants.TEXT_SECONDARY);

        JLabel info3 = new JLabel("3. SimuladorTorniquetesThread: Hilo interactivo que genera flujo continuo de ingresos y salidas en torniquetes.");
        info3.setFont(ThemeConstants.FONT_SMALL);
        info3.setForeground(ThemeConstants.TEXT_SECONDARY);

        infoCard.add(info1);
        infoCard.add(info2);
        infoCard.add(info3);

        add(topPanel, BorderLayout.NORTH);
        add(tableContainer, BorderLayout.CENTER);
        add(infoCard, BorderLayout.SOUTH);
    }

    private void toggleSimulador() {
        SimuladorTorniquetesThread sim = GestorHilosSica.getInstance().getSimuladorTorniquetesThread();
        if (sim != null) {
            if (sim.isEjecutando()) {
                sim.pausar();
                btnToggleSimulador.setText("▶ Iniciar Simulador Concurrente");
                btnToggleSimulador.setBackground(ThemeConstants.ACCENT_PRIMARY);
            } else {
                sim.iniciar();
                btnToggleSimulador.setText("⏸ Pausar Simulador Concurrente");
                btnToggleSimulador.setBackground(ThemeConstants.ACCENT_WARNING);
            }
            refrescarTabla();
        }
    }

    public void refrescarTabla() {
        tableModel.setRowCount(0);
        List<GestorHilosSica.ThreadInfo> hilos = GestorHilosSica.getInstance().obtenerEstadoHilos();

        for (GestorHilosSica.ThreadInfo info : hilos) {
            tableModel.addRow(new Object[]{
                    info.nombre,
                    info.estado,
                    info.activo ? "✅ VIVO" : "🛑 DETENIDO",
                    info.detalle
            });
        }
    }
}
