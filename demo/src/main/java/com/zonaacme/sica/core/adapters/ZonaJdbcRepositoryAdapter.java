package com.zonaacme.sica.core.adapters;

import com.sica.shared.infrastructure.config.DatabaseConnection;
import com.zonaacme.sica.core.domain.PuntoControl;
import com.zonaacme.sica.core.domain.Zona;
import com.zonaacme.sica.core.ports.out.ZonaRepositoryPort;

import java.sql.*;
import java.time.LocalTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Adaptador JDBC para el repositorio de Zonas y Puntos de Control.
 * Conecta directamente a la base de datos MySQL (sica_db.zonas y sica_db.puntos_control).
 */
public class ZonaJdbcRepositoryAdapter implements ZonaRepositoryPort {

    private final Map<String, Zona> zonasPorId = new ConcurrentHashMap<>();
    private final Map<String, String> idPorCodigoZona = new ConcurrentHashMap<>();

    private final Map<String, PuntoControl> puntosPorId = new ConcurrentHashMap<>();
    private final Map<String, String> idPorCodigoPunto = new ConcurrentHashMap<>();

    public ZonaJdbcRepositoryAdapter() {
        inicializarSemillasMemoria();
        cargarDesdeMySQL();
    }

    private void inicializarSemillasMemoria() {
        Zona lobby = Zona.nueva("ZONA_LOBBY", "Lobby Principal y Recepción", "Área de recepción general y torniquetes peatonales", 200, LocalTime.of(5, 0), LocalTime.of(23, 0), false);
        Zona techHub = Zona.nueva("ZONA_TECH_HUB", "Torre de Innovación & Tech Hub", "Pisos corporativos de oficinas para empresas de tecnología", 350, LocalTime.of(6, 0), LocalTime.of(21, 0), false);
        Zona bioLabs = Zona.nueva("ZONA_BIOLABS", "Laboratorios BioGen & Nanotecnología", "Área biocontenida de investigación farmacéutica", 80, LocalTime.of(7, 0), LocalTime.of(19, 0), true);
        Zona datacenter = Zona.nueva("ZONA_DATACENTER", "Centro de Cómputo Principal (Tier IV)", "Sala de servidores críticos y telecomunicaciones", 15, LocalTime.of(0, 0), LocalTime.of(23, 59), true);
        Zona parking = Zona.nueva("ZONA_PARKING", "Estacionamiento Subterráneo S1/S2", "Bahías vehiculares para funcionarios y visitantes", 150, LocalTime.of(5, 30), LocalTime.of(22, 30), false);

        guardarZonaEnMemoria(lobby);
        guardarZonaEnMemoria(techHub);
        guardarZonaEnMemoria(bioLabs);
        guardarZonaEnMemoria(datacenter);
        guardarZonaEnMemoria(parking);

        guardarPuntoEnMemoria(PuntoControl.nuevo("PC_TORN_01", "Torniquete Peatonal 1 (Entrada Norte)", lobby.getId(), PuntoControl.TipoPunto.TORNIQUETE));
        guardarPuntoEnMemoria(PuntoControl.nuevo("PC_TORN_02", "Torniquete Peatonal 2 (Entrada Sur)", lobby.getId(), PuntoControl.TipoPunto.TORNIQUETE));
        guardarPuntoEnMemoria(PuntoControl.nuevo("PC_PUERTA_TECH", "Puerta Acceso Torre Tech Hub", techHub.getId(), PuntoControl.TipoPunto.PUERTA_AUTOMATICA));
        guardarPuntoEnMemoria(PuntoControl.nuevo("PC_BIO_ESCLUSA", "Esclusa Bioseguridad BioLabs", bioLabs.getId(), PuntoControl.TipoPunto.PUERTA_AUTOMATICA));
        guardarPuntoEnMemoria(PuntoControl.nuevo("PC_DC_RESTRINGIDO", "Esclusa de Seguridad Datacenter", datacenter.getId(), PuntoControl.TipoPunto.PUERTA_AUTOMATICA));
        guardarPuntoEnMemoria(PuntoControl.nuevo("PC_TALANQUERA_VEH", "Talanquera Vehicular Acceso S1", parking.getId(), PuntoControl.TipoPunto.VEHICULAR));
    }

    private void guardarZonaEnMemoria(Zona z) {
        zonasPorId.put(z.getId(), z);
        idPorCodigoZona.put(z.getCodigo().toUpperCase(), z.getId());
    }

    private void guardarPuntoEnMemoria(PuntoControl pc) {
        puntosPorId.put(pc.getId(), pc);
        idPorCodigoPunto.put(pc.getCodigo().toUpperCase(), pc.getId());
    }

    private Connection getConn() {
        return DatabaseConnection.getInstance().getConnection();
    }

    private void cargarDesdeMySQL() {
        Connection conn = getConn();
        if (conn != null) {
            try {
                // Cargar Zonas
                String sqlZonas = "SELECT * FROM zonas";
                try (Statement stmt = conn.createStatement();
                     ResultSet rs = stmt.executeQuery(sqlZonas)) {
                    while (rs.next()) {
                        int dbId = rs.getInt("id");
                        String codigo = rs.getString("codigo");
                        String nombre = rs.getString("nombre");
                        String desc = rs.getString("descripcion");
                        int aforo = rs.getInt("aforo_maximo");
                        Time ap = rs.getTime("hora_apertura");
                        Time ci = rs.getTime("hora_cierre");
                        boolean req = rs.getBoolean("requiere_autorizacion_especial");
                        boolean activa = rs.getBoolean("esta_activa");

                        String zid = idPorCodigoZona.getOrDefault(codigo.toUpperCase(), "ZON_" + dbId);
                        Zona z = new Zona(
                                zid,
                                codigo,
                                nombre,
                                desc,
                                aforo > 0 ? aforo : 50,
                                ap != null ? ap.toLocalTime() : LocalTime.of(6, 0),
                                ci != null ? ci.toLocalTime() : LocalTime.of(22, 0),
                                req,
                                activa
                        );
                        guardarZonaEnMemoria(z);
                    }
                }

                // Cargar Puntos de Control
                String sqlPuntos = "SELECT pc.*, z.codigo as zona_codigo FROM puntos_control pc " +
                                  "LEFT JOIN zonas z ON pc.zona_id = z.id";
                try (Statement stmt = conn.createStatement();
                     ResultSet rs = stmt.executeQuery(sqlPuntos)) {
                    while (rs.next()) {
                        int dbId = rs.getInt("id");
                        String codigo = rs.getString("codigo");
                        String nombre = rs.getString("nombre");
                        String tipoStr = rs.getString("tipo_punto");
                        boolean activo = rs.getBoolean("esta_activo");
                        String zCod = rs.getString("zona_codigo");

                        String zonaId = (zCod != null && idPorCodigoZona.containsKey(zCod.toUpperCase()))
                                ? idPorCodigoZona.get(zCod.toUpperCase())
                                : "ZON_" + rs.getInt("zona_id");

                        PuntoControl.TipoPunto tipo = PuntoControl.TipoPunto.TORNIQUETE;
                        if (tipoStr != null) {
                            if (tipoStr.contains("VEHICULAR")) tipo = PuntoControl.TipoPunto.VEHICULAR;
                            else if (tipoStr.contains("BIOMETRICA") || tipoStr.contains("PUERTA")) tipo = PuntoControl.TipoPunto.PUERTA_AUTOMATICA;
                        }

                        String pcId = idPorCodigoPunto.getOrDefault(codigo.toUpperCase(), "PC_" + dbId);
                        PuntoControl pc = new PuntoControl(pcId, codigo, nombre, zonaId, tipo, activo);
                        guardarPuntoEnMemoria(pc);
                    }
                }
            } catch (SQLException e) {
                System.err.println("⚠️ [ZonaJdbcRepo] Error al cargar zonas/puntos de MySQL: " + e.getMessage());
            }
        }
    }

    @Override
    public void saveZona(Zona zona) {
        Objects.requireNonNull(zona, "La zona no puede ser nula");
        guardarZonaEnMemoria(zona);

        Connection conn = getConn();
        if (conn != null) {
            try {
                String sql = "INSERT INTO zonas (codigo, nombre, descripcion, aforo_maximo, hora_apertura, hora_cierre, requiere_autorizacion_especial, esta_activa) " +
                             "VALUES (?, ?, ?, ?, ?, ?, ?, ?) " +
                             "ON DUPLICATE KEY UPDATE nombre = VALUES(nombre), descripcion = VALUES(descripcion), " +
                             "aforo_maximo = VALUES(aforo_maximo), esta_activa = VALUES(esta_activa)";
                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setString(1, zona.getCodigo());
                    ps.setString(2, zona.getNombre());
                    ps.setString(3, zona.getDescripcion());
                    ps.setInt(4, zona.getAforoMaximo());
                    ps.setTime(5, Time.valueOf(zona.getHoraInicioPermitida()));
                    ps.setTime(6, Time.valueOf(zona.getHoraFinPermitida()));
                    ps.setBoolean(7, zona.isRequiereAprobacionEspecial());
                    ps.setBoolean(8, zona.isActivo());
                    ps.executeUpdate();
                }
            } catch (SQLException e) {
                System.err.println("⚠️ [ZonaJdbcRepo] Error al guardar zona en MySQL: " + e.getMessage());
            }
        }
    }

    @Override
    public Optional<Zona> findZonaById(String id) {
        if (id == null) return Optional.empty();
        return Optional.ofNullable(zonasPorId.get(id));
    }

    @Override
    public Optional<Zona> findZonaByCodigo(String codigo) {
        if (codigo == null) return Optional.empty();
        String cod = codigo.trim().toUpperCase();
        if ("ZONA_RECEPCION".equals(cod)) cod = "ZONA_LOBBY";
        String id = idPorCodigoZona.get(cod);
        if (id != null) {
            return Optional.ofNullable(zonasPorId.get(id));
        }
        return Optional.empty();
    }

    @Override
    public List<Zona> findAllZonas() {
        return Collections.unmodifiableList(new ArrayList<>(zonasPorId.values()));
    }

    @Override
    public void savePuntoControl(PuntoControl puntoControl) {
        Objects.requireNonNull(puntoControl, "El punto de control no puede ser nulo");
        guardarPuntoEnMemoria(puntoControl);
    }

    @Override
    public Optional<PuntoControl> findPuntoControlById(String id) {
        if (id == null) return Optional.empty();
        return Optional.ofNullable(puntosPorId.get(id));
    }

    @Override
    public Optional<PuntoControl> findPuntoControlByCodigo(String codigo) {
        if (codigo == null) return Optional.empty();
        String cod = codigo.trim().toUpperCase();
        if ("PC_PUERTA_DC".equals(cod)) cod = "PC_DC_RESTRINGIDO";
        if ("PC_TORNIQUETE_01".equals(cod)) cod = "PC_TORN_01";
        String id = idPorCodigoPunto.get(cod);
        if (id != null) {
            return Optional.ofNullable(puntosPorId.get(id));
        }
        return Optional.empty();
    }

    @Override
    public List<PuntoControl> findAllPuntosControl() {
        return Collections.unmodifiableList(new ArrayList<>(puntosPorId.values()));
    }

    @Override
    public List<PuntoControl> findPuntosControlByZonaId(String zonaId) {
        if (zonaId == null) return Collections.emptyList();
        return puntosPorId.values().stream()
                .filter(p -> p.getZonaId().equals(zonaId))
                .collect(Collectors.collectingAndThen(Collectors.toList(), Collections::unmodifiableList));
    }
}
