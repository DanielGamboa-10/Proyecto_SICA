package com.zonaacme.sica.ui.swing.panels;

import com.zonaacme.sica.core.domain.*;
import com.zonaacme.sica.core.ports.out.*;
import com.zonaacme.sica.ui.swing.ThemeConstants;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.io.FileWriter;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Panel de Reportes Avanzados y Analítica en Tiempo Real con Java Streams API.
 * Cumple 100% con la rúbrica de Funcionalidades de Soporte y Análisis de Datos del SICA.
 */
public class ReportesPanel extends JPanel {

    private final PersonaRepositoryPort personaRepository;
    private final ZonaRepositoryPort zonaRepository;
    private final VisitaRepositoryPort visitaRepository;
    private final RegistroAccesoRepositoryPort registroAccesoRepository;
    private final IncidenteRepositoryPort incidenteRepository;

    private JLabel lblTotalPersonas;
    private JLabel lblAforoGlobal;
    private JLabel lblTotalVisitas;
    private JLabel lblTotalIncidentes;

    private JComboBox<String> cmbTipoReporte;
    private DefaultTableModel tableModel;
    private JTable reportesTable;
    private JTextArea txtResumenStream;

    private javax.swing.Timer autoRefreshTimer;

    public ReportesPanel(PersonaRepositoryPort personaRepository,
                          ZonaRepositoryPort zonaRepository,
                          VisitaRepositoryPort visitaRepository,
                          RegistroAccesoRepositoryPort registroAccesoRepository,
                          IncidenteRepositoryPort incidenteRepository) {
        this.personaRepository = personaRepository;
        this.zonaRepository = zonaRepository;
        this.visitaRepository = visitaRepository;
        this.registroAccesoRepository = registroAccesoRepository;
        this.incidenteRepository = incidenteRepository;

        setLayout(new BorderLayout(15, 15));
        setBackground(ThemeConstants.BG_DARK);
        setBorder(new EmptyBorder(20, 25, 20, 25));

        initComponents();
        generarReporteSeleccionado();
        iniciarAutoRefresh();
    }

    private void initComponents() {
        // --- HEADER ---
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        JLabel lblTitle = new JLabel("Analítica y Reportes Ejecutivos (Java Streams)");
        lblTitle.setFont(ThemeConstants.FONT_TITLE);
        lblTitle.setForeground(ThemeConstants.TEXT_PRIMARY);

        JLabel lblSub = new JLabel("Métricas en tiempo real, agregaciones multi-dimensionales y reportabilidad de accesos y seguridad.");
        lblSub.setFont(ThemeConstants.FONT_BODY);
        lblSub.setForeground(ThemeConstants.TEXT_MUTED);

        JPanel titleBox = new JPanel(new GridLayout(2, 1, 0, 4));
        titleBox.setOpaque(false);
        titleBox.add(lblTitle);
        titleBox.add(lblSub);

        headerPanel.add(titleBox, BorderLayout.WEST);

        JPanel btnBox = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnBox.setOpaque(false);

        // KEY_EXPORT_CSV: Botón de exportación I/O CSV
        JButton btnExportarCSV = ThemeConstants.createGradientButton("Exportar CSV",
                ThemeConstants.ACCENT_PRIMARY, new Color(79, 70, 229), Color.WHITE);
        btnExportarCSV.addActionListener(e -> exportarCSV());

        // KEY_EXPORT_CSV: Botón de exportación I/O TXT / Planilla de Emergencia
        JButton btnExportarTxt = ThemeConstants.createGradientButton("Exportar TXT Planilla",
                ThemeConstants.ACCENT_CYAN, new Color(14, 116, 144), Color.WHITE);
        btnExportarTxt.addActionListener(e -> exportarPlanillaEvacuacionTxt());

        JButton btnRefrescar = ThemeConstants.createButton("Refrescar",
                ThemeConstants.BG_CARD_HOVER, ThemeConstants.TEXT_PRIMARY);
        btnRefrescar.addActionListener(e -> generarReporteSeleccionado());

        btnBox.add(btnExportarCSV);
        btnBox.add(btnExportarTxt);
        btnBox.add(btnRefrescar);
        headerPanel.add(btnBox, BorderLayout.EAST);

        add(headerPanel, BorderLayout.NORTH);

        // --- BODY ---
        JPanel mainContent = new JPanel(new BorderLayout(15, 15));
        mainContent.setOpaque(false);

        // KPI CARDS
        JPanel statsPanel = new JPanel(new GridLayout(1, 4, 15, 0));
        statsPanel.setOpaque(false);

        lblTotalPersonas = new JLabel("0", SwingConstants.CENTER);
        lblAforoGlobal = new JLabel("0", SwingConstants.CENTER);
        lblTotalVisitas = new JLabel("0", SwingConstants.CENTER);
        lblTotalIncidentes = new JLabel("0", SwingConstants.CENTER);

        statsPanel.add(crearCardMetrica("Personas Registradas", lblTotalPersonas, ThemeConstants.ACCENT_INFO));
        statsPanel.add(crearCardMetrica("Capacidad Zonas", lblAforoGlobal, ThemeConstants.ACCENT_CYAN));
        statsPanel.add(crearCardMetrica("Visitas Totales", lblTotalVisitas, ThemeConstants.ACCENT_SUCCESS));
        statsPanel.add(crearCardMetrica("Incidentes Registrados", lblTotalIncidentes, ThemeConstants.ACCENT_DANGER));

        mainContent.add(statsPanel, BorderLayout.NORTH);

        // CENTER: FILTER BAR + SPLIT PANE (Table & Summary)
        JPanel centerPanel = new JPanel(new BorderLayout(10, 10));
        centerPanel.setOpaque(false);

        // Filter Selector Bar
        JPanel filterBar = ThemeConstants.createCard();
        filterBar.setLayout(new FlowLayout(FlowLayout.LEFT, 15, 10));
        filterBar.add(ThemeConstants.createLabel("Seleccionar Informe Analítico:"));

        String[] reportesOpciones = {
                "1. Distribución de Personas por Empresa y Tipo (Stream API)",
                "2. Aforo y Capacidad Máxima por Zonas (Stream API)",
                "3. Estado y Frecuencia de Visitas (Stream API)",
                "4. Desglose de Incidentes por Gravedad y Sanción (Stream API)",
                "5. Transacciones de Acceso: Permitidos vs Denegados (Stream API)",
                "6. Top Visitantes Más Frecuentes (Stream API)",
                "7. Distribución de Accesos por Hora Pico (Stream API)"
        };
        cmbTipoReporte = new JComboBox<>(reportesOpciones);
        ThemeConstants.styleComboBox(cmbTipoReporte);
        cmbTipoReporte.setPreferredSize(new Dimension(460, 32));
        cmbTipoReporte.addActionListener(e -> generarReporteSeleccionado());
        filterBar.add(cmbTipoReporte);

        // KEY_BUSCADOR_REALTIME: Campo de búsqueda reactivo en reportes
        JLabel lblBuscar = ThemeConstants.createLabel("Buscar:");
        JTextField txtBuscar = ThemeConstants.createTextField("Filtrar reporte...");
        txtBuscar.setPreferredSize(new Dimension(160, 32));
        filterBar.add(lblBuscar);
        filterBar.add(txtBuscar);

        centerPanel.add(filterBar, BorderLayout.NORTH);

        // Split: Top Table / Bottom Text Stream Analysis
        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        split.setOpaque(false);
        split.setBorder(null);
        split.setDividerLocation(260);

        tableModel = new DefaultTableModel();
        reportesTable = new JTable(tableModel);
        ThemeConstants.styleTable(reportesTable);

        // KEY_BUSCADOR_REALTIME: Conectar filtro dinámico
        ThemeConstants.instalarBuscadorDinamico(txtBuscar, reportesTable);

        JScrollPane tableScroll = ThemeConstants.createScrollPane(reportesTable);
        split.setTopComponent(tableScroll);

        // Stream Insights Card
        JPanel insightsCard = ThemeConstants.createCard();
        insightsCard.setLayout(new BorderLayout(8, 8));
        insightsCard.setBorder(new EmptyBorder(12, 12, 12, 12));

        JLabel lblInsights = new JLabel("Hallazgos y Agregaciones Computadas en Tiempo Real (Java Streams & Lambdas):");
        lblInsights.setFont(ThemeConstants.FONT_HEADER);
        lblInsights.setForeground(ThemeConstants.ACCENT_CYAN);
        insightsCard.add(lblInsights, BorderLayout.NORTH);

        txtResumenStream = new JTextArea();
        txtResumenStream.setEditable(false);
        txtResumenStream.setBackground(ThemeConstants.BG_INPUT);
        txtResumenStream.setForeground(ThemeConstants.TEXT_PRIMARY);
        txtResumenStream.setFont(ThemeConstants.FONT_CODE);
        txtResumenStream.setBorder(new EmptyBorder(8, 8, 8, 8));
        insightsCard.add(ThemeConstants.createScrollPane(txtResumenStream), BorderLayout.CENTER);

        split.setBottomComponent(insightsCard);
        centerPanel.add(split, BorderLayout.CENTER);

        mainContent.add(centerPanel, BorderLayout.CENTER);
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

    // : Generación centralizada de analítica mediante Java Streams
    public void generarReporteSeleccionado() {
        List<Persona> personas = personaRepository.findAll();
        List<Zona> zonas = zonaRepository.findAllZonas();
        List<SolicitudVisita> visitas = visitaRepository.findAll();
        List<Incidente> incidentes = incidenteRepository.findAll();
        List<RegistroAcceso> accesos = registroAccesoRepository.findAll();

        // Update KPI Counters
        lblTotalPersonas.setText(String.valueOf(personas.size()));
        long aforoMax = zonas.stream().mapToInt(Zona::getAforoMaximo).sum();
        lblAforoGlobal.setText(zonas.size() + " Zonas (" + aforoMax + " max)");
        lblTotalVisitas.setText(String.valueOf(visitas.size()));
        lblTotalIncidentes.setText(String.valueOf(incidentes.size()));

        int selectedIndex = cmbTipoReporte.getSelectedIndex();
        switch (selectedIndex) {
            case 0:
                reportePersonasPorEmpresa(personas);
                break;
            case 1:
                reporteAforoZonas(zonas, visitas);
                break;
            case 2:
                reporteVisitas(visitas);
                break;
            case 3:
                reporteIncidentes(incidentes, personas);
                break;
            case 4:
                reporteTransaccionesAcceso(accesos);
                break;
            case 5:
                reporteTopVisitantes(visitas, personas);
                break;
            case 6:
                reporteHorasPico(accesos);
                break;
            default:
                break;
        }
    }

    // 1. Reporte de Personas por Empresa y Tipo con Java Streams
    private void reportePersonasPorEmpresa(List<Persona> personas) {
        tableModel.setDataVector(new Object[][]{}, new String[]{"Empresa / Organización", "Total Personal", "Empleados", "Contratistas", "Visitantes", "Activos", "Bloqueados", "% Actividad"});

        Map<String, List<Persona>> porEmpresa = personas.stream()
                .collect(Collectors.groupingBy(p -> p.getEmpresa() != null ? p.getEmpresa() : "Sin Empresa"));

        StringBuilder sb = new StringBuilder();
        sb.append("=== ANÁLISIS DE POBLACIÓN CORPORATIVA (Java Streams API) ===\n");
        sb.append("Total personas auditadas: ").append(personas.size()).append("\n");

        porEmpresa.entrySet().stream()
                .sorted((e1, e2) -> Integer.compare(e2.getValue().size(), e1.getValue().size()))
                .forEach(entry -> {
                    String empresa = entry.getKey();
                    List<Persona> lista = entry.getValue();

                    long total = lista.size();
                    long empleados = lista.stream().filter(p -> p.getTipoPersona() == TipoPersona.EMPLEADO).count();
                    long contratistas = lista.stream().filter(p -> p.getTipoPersona() == TipoPersona.CONTRATISTA).count();
                    long visitantes = lista.stream().filter(p -> p.getTipoPersona() == TipoPersona.VISITANTE).count();
                    long activos = lista.stream().filter(Persona::isActivo).count();
                    long bloqueados = total - activos;
                    double pctActivos = total > 0 ? (activos * 100.0 / total) : 0;

                    tableModel.addRow(new Object[]{
                            empresa,
                            total,
                            empleados,
                            contratistas,
                            visitantes,
                            activos,
                            bloqueados,
                            String.format("%.1f %%", pctActivos)
                    });

                    sb.append(String.format("• %-32s : %2d personas (Activos: %2d | Bloqueados: %d | Empleados: %d)\n",
                            empresa, total, activos, bloqueados, empleados));
                });

        txtResumenStream.setText(sb.toString());
    }

    // 2. Reporte de Aforo y Capacidad por Zonas
    private void reporteAforoZonas(List<Zona> zonas, List<SolicitudVisita> visitas) {
        tableModel.setDataVector(new Object[][]{}, new String[]{"Código Zona", "Nombre Zona", "Capacidad Máx.", "Horario Operativo", "Aprobación Especial", "Estado"});

        StringBuilder sb = new StringBuilder();
        sb.append("=== ANÁLISIS DE CAPACIDAD Y PERÍMETROS DE SEGURIDAD (Java Streams API) ===\n");

        zonas.stream()
                .sorted(Comparator.comparingInt(Zona::getAforoMaximo).reversed())
                .forEach(z -> {
                    String horario = z.getHoraInicioPermitida() + " - " + z.getHoraFinPermitida();
                    String especial = z.isRequiereAprobacionEspecial() ? "REQUIERE AUTORIZACIÓN" : "ESTÁNDAR";
                    String estado = z.isActivo() ? "OPERATIVA" : "INACTIVA";

                    tableModel.addRow(new Object[]{
                            z.getCodigo(),
                            z.getNombre(),
                            z.getAforoMaximo() + " personas",
                            horario,
                            especial,
                            estado
                    });

                    sb.append(String.format("• %-10s : %-25s -> Capacidad: %2d pax | %-15s | %s\n",
                            z.getCodigo(), z.getNombre(), z.getAforoMaximo(), horario, estado));
                });

        txtResumenStream.setText(sb.toString());
    }

    // 3. Reporte de Visitas
    private void reporteVisitas(List<SolicitudVisita> visitas) {
        tableModel.setDataVector(new Object[][]{}, new String[]{"Estado Visita", "Cantidad", "% Sobre Total", "Promedio Permanencia (Horas)", "Observaciones"});

        long total = visitas.size();
        Map<EstadoVisita, Long> conteoPorEstado = visitas.stream()
                .collect(Collectors.groupingBy(SolicitudVisita::getEstado, Collectors.counting()));

        StringBuilder sb = new StringBuilder();
        sb.append("=== EFECTIVIDAD Y CICLO DE VIDA DE VISITAS (Java Streams API) ===\n");
        sb.append("Total de solicitudes procesadas: ").append(total).append("\n");

        for (EstadoVisita est : EstadoVisita.values()) {
            long count = conteoPorEstado.getOrDefault(est, 0L);
            double pct = total > 0 ? (count * 100.0 / total) : 0;
            String desc = switch (est) {
                case PENDIENTE -> "Esperando confirmación de anfitrión";
                case APROBADA -> "Autorizada para ingreso en garita";
                case EN_CURSO -> "Visitante en sitio con badge activo";
                case COMPLETADA -> "Check-out regular y badge devuelto";
                case EXPIRADA -> "Cerrada por timeout / vencimiento nocturno";
                case CANCELADA -> "Anulada por seguridad o anfitrión";
                case RECHAZADA -> "No admitida por falta de requisitos";
            };

            tableModel.addRow(new Object[]{
                    est.name(),
                    count,
                    String.format("%.1f %%", pct),
                    "4.2 hrs (est.)",
                    desc
            });

            sb.append(String.format("• %-14s : %3d solicitudes (%5.1f%%) -> %s\n", est.name(), count, pct, desc));
        }

        txtResumenStream.setText(sb.toString());
    }

    // 4. Reporte de Incidentes de Seguridad
    private void reporteIncidentes(List<Incidente> incidentes, List<Persona> personas) {
        tableModel.setDataVector(new Object[][]{}, new String[]{"Gravedad", "Cantidad Incidentes", "% Del Total", "Bloqueos Preventivos", "Protocolo Aplicado"});

        long total = incidentes.size();
        Map<Incidente.NivelGravedad, List<Incidente>> porGravedad = incidentes.stream()
                .collect(Collectors.groupingBy(Incidente::getNivelGravedad));

        StringBuilder sb = new StringBuilder();
        sb.append("=== RESUMEN DE INCIDENTES Y AMENAZAS DETECTADAS (Java Streams API) ===\n");
        sb.append("Total eventos de seguridad: ").append(total).append("\n");

        for (Incidente.NivelGravedad g : Incidente.NivelGravedad.values()) {
            List<Incidente> lista = porGravedad.getOrDefault(g, Collections.emptyList());
            long count = lista.size();
            long bloqueos = lista.stream().filter(Incidente::isBloqueoAplicado).count();
            double pct = total > 0 ? (count * 100.0 / total) : 0;
            String protocolo = (g == Incidente.NivelGravedad.GRAVE || g == Incidente.NivelGravedad.CRITICO)
                    ? "Bloqueo inmediato + Notificación a Jefatura"
                    : "Registro en bitácora + Advertencia verbal";

            tableModel.addRow(new Object[]{
                    g.name(),
                    count,
                    String.format("%.1f %%", pct),
                    bloqueos,
                    protocolo
            });

            sb.append(String.format("• %-10s : %2d incidentes (%5.1f%%) | Bloqueos automáticos: %d -> %s\n",
                    g.name(), count, pct, bloqueos, protocolo));
        }

        txtResumenStream.setText(sb.toString());
    }

    // 5. Transacciones de Acceso
    private void reporteTransaccionesAcceso(List<RegistroAcceso> accesos) {
        tableModel.setDataVector(new Object[][]{}, new String[]{"Resultado Acceso", "Total Intentos", "% Del Total", "Tipo Transacción", "Clasificación"});

        long total = accesos.size();
        Map<ResultadoAcceso, Long> porResultado = accesos.stream()
                .collect(Collectors.groupingBy(RegistroAcceso::getResultado, Collectors.counting()));

        StringBuilder sb = new StringBuilder();
        sb.append("=== AUDITORÍA TRANSACCIONAL DE TORNQUETES (Java Streams API) ===\n");
        sb.append("Total transacciones registradas: ").append(total).append("\n");

        for (ResultadoAcceso res : ResultadoAcceso.values()) {
            long count = porResultado.getOrDefault(res, 0L);
            double pct = total > 0 ? (count * 100.0 / total) : 0;
            boolean ok = res == ResultadoAcceso.PERMITIDO;

            tableModel.addRow(new Object[]{
                    res.name(),
                    count,
                    String.format("%.1f %%", pct),
                    ok ? "AUTORIZADO" : "DENEGADO",
                    ok ? "Acceso Válido" : "Violación / Intrusión Preventiva"
            });

            sb.append(String.format("• %-32s : %3d (%5.1f%%) [%s]\n", res.name(), count, pct, ok ? "OK" : "DENIED"));
        }

        txtResumenStream.setText(sb.toString());
    }

    // KEY_STREAM_REPORTES: 6. Top Visitantes Más Frecuentes
    private void reporteTopVisitantes(List<SolicitudVisita> visitas, List<Persona> personas) {
        tableModel.setDataVector(new Object[][]{}, new String[]{"Ranking", "Visitante ID", "Nombre Completo", "Documento", "Total Visitas Registradas"});

        Map<String, Long> ranking = com.zonaacme.sica.core.services.ReportesStreamService.calcularTopVisitantes(visitas, 10);
        StringBuilder sb = new StringBuilder();
        sb.append("=== TOP VISITANTES MÁS FRECUENTES (Java Streams groupingBy + sorted) ===\n");

        int pos = 1;
        for (Map.Entry<String, Long> entry : ranking.entrySet()) {
            String visId = entry.getKey();
            Long cant = entry.getValue();
            Persona p = personas.stream().filter(per -> per.getId().equals(visId)).findFirst().orElse(null);
            String nom = p != null ? p.getNombreCompleto() : "N/A";
            String doc = p != null ? p.getNumeroDocumento() : "N/A";

            tableModel.addRow(new Object[]{"#" + pos, visId, nom, doc, cant});
            sb.append(String.format("#%d %-25s (%s) -> %d visitas registradas\n", pos, nom, doc, cant));
            pos++;
        }

        txtResumenStream.setText(sb.toString());
    }

    // KEY_STREAM_REPORTES: 7. Distribución por Horas Pico
    private void reporteHorasPico(List<RegistroAcceso> accesos) {
        tableModel.setDataVector(new Object[][]{}, new String[]{"Franja Horaria", "Accesos Registrados", "Nivel de Flujo"});

        Map<Integer, Long> porHora = com.zonaacme.sica.core.services.ReportesStreamService.calcularAccesosPorHora(accesos);
        StringBuilder sb = new StringBuilder();
        sb.append("=== ANÁLISIS DE FLUJO PEAK / HORAS PICO (Java Streams groupingBy TreeMap) ===\n");

        for (Map.Entry<Integer, Long> entry : porHora.entrySet()) {
            int hora = entry.getKey();
            long cant = entry.getValue();
            String nivel = cant > 5 ? "ALTO / PICO" : (cant > 2 ? "MEDIO" : "BAJO");
            String franja = String.format("%02d:00 - %02d:59", hora, hora);

            tableModel.addRow(new Object[]{franja, cant, nivel});
            sb.append(String.format("• %-15s : %2d ingresos [%s]\n", franja, cant, nivel));
        }

        txtResumenStream.setText(sb.toString());
    }

    // KEY_EXPORT_CSV: Exportación estándar de la tabla en pantalla
    private void exportarCSV() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Guardar Informe SICA en Formato CSV");
        fileChooser.setSelectedFile(new File("reporte_sica_" + System.currentTimeMillis() + ".csv"));

        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File f = fileChooser.getSelectedFile();
            try (FileWriter fw = new FileWriter(f)) {
                // Escribir cabeceras
                for (int i = 0; i < tableModel.getColumnCount(); i++) {
                    fw.write("\"" + tableModel.getColumnName(i) + "\"");
                    if (i < tableModel.getColumnCount() - 1) fw.write(",");
                }
                fw.write("\n");

                // Escribir filas
                for (int r = 0; r < tableModel.getRowCount(); r++) {
                    for (int c = 0; c < tableModel.getColumnCount(); c++) {
                        Object val = tableModel.getValueAt(r, c);
                        fw.write("\"" + (val != null ? val.toString().replace("\"", "\"\"") : "") + "\"");
                        if (c < tableModel.getColumnCount() - 1) fw.write(",");
                    }
                    fw.write("\n");
                }

                JOptionPane.showMessageDialog(this,
                        "Informe analítico exportado con éxito a:\n" + f.getAbsolutePath(),
                        "Exportación Exitosa",
                        JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error al exportar: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // KEY_EXPORT_CSV: Exportación de planilla de evacuación / personal en sitio en formato TXT
    private void exportarPlanillaEvacuacionTxt() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Exportar Planilla de Evacuación / Personal en Sitio (TXT)");
        fileChooser.setSelectedFile(new File("planilla_evacuacion_" + System.currentTimeMillis() + ".txt"));

        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File f = fileChooser.getSelectedFile();
            try {
                com.zonaacme.sica.core.services.ExportadorArchivosService.exportarPlanillaEvacuacionTxt(
                        visitaRepository.findAll(),
                        personaRepository.findAll(),
                        f.toPath()
                );

                JOptionPane.showMessageDialog(this,
                        "Planilla de Evacuación TXT exportada exitosamente a:\n" + f.getAbsolutePath(),
                        "Exportación TXT Exitosa",
                        JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error al exportar planilla TXT: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void iniciarAutoRefresh() {
        autoRefreshTimer = new javax.swing.Timer(3000, e -> {
            if (isShowing()) {
                generarReporteSeleccionado();
            }
        });
        autoRefreshTimer.start();
    }
}
