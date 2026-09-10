package com.zonaacme.sica.core.adapters;

import com.sica.shared.infrastructure.config.DatabaseConnection;
import com.zonaacme.sica.core.domain.Incidente;
import com.zonaacme.sica.core.ports.out.IncidenteRepositoryPort;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Adaptador JDBC para el repositorio de Incidentes.
 * Persiste y consulta incidentes en MySQL (incidentes) con fallback resiliente en memoria.
 */
public class IncidenteJdbcRepositoryAdapter implements IncidenteRepositoryPort {

    private final Map<String, Incidente> memoria = new ConcurrentHashMap<>();

    public IncidenteJdbcRepositoryAdapter() {
        cargarDesdeMySQL();
    }

    private Connection getConn() {
        return DatabaseConnection.getInstance().getConnection();
    }

    private void cargarDesdeMySQL() {
        Connection conn = getConn();
        if (conn != null) {
            try {
                String sql = "SELECT i.*, p.nombre as persona_nombre, COALESCE(u.nombre, u.email, 'Seguridad') as reportado_por " +
                             "FROM incidentes i " +
                             "LEFT JOIN personas p ON i.persona_id = p.id " +
                             "LEFT JOIN usuarios u ON i.reportado_por_id = u.id " +
                             "ORDER BY i.fecha DESC";
                try (Statement stmt = conn.createStatement();
                     ResultSet rs = stmt.executeQuery(sql)) {
                    while (rs.next()) {
                        Incidente inc = mapearDesdeResultSet(rs);
                        memoria.put(inc.getId(), inc);
                    }
                }
            } catch (SQLException e) {
                System.err.println("⚠️ [IncidenteJdbcRepo] Error al cargar incidentes de MySQL: " + e.getMessage());
            }
        }
    }

    private Incidente mapearDesdeResultSet(ResultSet rs) throws SQLException {
        int dbId = rs.getInt("id");
        int visitaId = rs.getInt("visita_id");
        int personaId = rs.getInt("persona_id");
        String reportadoPor = rs.getString("reportado_por");
        String gravStr = rs.getString("nivel_gravedad");
        Timestamp ts = rs.getTimestamp("fecha");
        String desc = rs.getString("descripcion");
        String acciones = rs.getString("acciones_tomadas");
        String perNombre = rs.getString("persona_nombre");

        Incidente.NivelGravedad gravedad = Incidente.NivelGravedad.MODERADO;
        if (gravStr != null) {
            try {
                gravedad = Incidente.NivelGravedad.valueOf(gravStr.toUpperCase());
            } catch (Exception ignored) {}
        }

        LocalDateTime fecha = ts != null ? ts.toLocalDateTime() : LocalDateTime.now();
        boolean bloqueo = (gravedad == Incidente.NivelGravedad.GRAVE || gravedad == Incidente.NivelGravedad.CRITICO);

        return new Incidente(
                "INC_" + dbId,
                visitaId > 0 ? "VIS_" + visitaId : null,
                personaId > 0 ? "PER_" + personaId : null,
                perNombre != null ? perNombre : "Persona #" + personaId,
                reportadoPor != null ? reportadoPor : "Admin",
                gravedad,
                fecha,
                desc != null ? desc : "",
                acciones != null ? acciones : "",
                bloqueo
        );
    }

    @Override
    public void save(Incidente incidente) {
        Objects.requireNonNull(incidente, "El incidente no puede ser nulo");
        memoria.put(incidente.getId(), incidente);

        Connection conn = getConn();
        if (conn != null) {
            try {
                Integer pId = null;
                if (incidente.getPersonaId() != null && incidente.getPersonaId().startsWith("PER_")) {
                    try { pId = Integer.parseInt(incidente.getPersonaId().substring(4)); } catch (Exception ignored) {}
                }
                Integer vId = null;
                if (incidente.getVisitaId() != null && incidente.getVisitaId().startsWith("VIS_")) {
                    try { vId = Integer.parseInt(incidente.getVisitaId().substring(4)); } catch (Exception ignored) {}
                }

                String sql = "INSERT INTO incidentes (visita_id, persona_id, reportado_por_id, nivel_gravedad, fecha, descripcion, acciones_tomadas) " +
                             "VALUES (?, ?, (SELECT id FROM usuarios WHERE email = ? OR nombre LIKE ? LIMIT 1), ?, ?, ?, ?)";
                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    if (vId != null) ps.setInt(1, vId); else ps.setNull(1, Types.INTEGER);
                    if (pId != null) ps.setInt(2, pId); else ps.setNull(2, Types.INTEGER);
                    String rep = incidente.getReportadoPor();
                    ps.setString(3, rep);
                    ps.setString(4, "%" + rep + "%");
                    ps.setString(5, incidente.getNivelGravedad().name());
                    ps.setTimestamp(6, Timestamp.valueOf(incidente.getFecha()));
                    ps.setString(7, incidente.getDescripcion());
                    ps.setString(8, incidente.getAccionesTomadas());
                    ps.executeUpdate();
                }
            } catch (SQLException e) {
                System.err.println("⚠️ [IncidenteJdbcRepo] Error al guardar incidente en MySQL: " + e.getMessage());
            }
        }
    }

    @Override
    public Optional<Incidente> findById(String id) {
        if (id == null) return Optional.empty();
        return Optional.ofNullable(memoria.get(id));
    }

    @Override
    public List<Incidente> findAll() {
        cargarDesdeMySQL();
        return new ArrayList<>(memoria.values());
    }

    @Override
    public List<Incidente> findByPersonaId(String personaId) {
        return findAll().stream()
                .filter(i -> personaId != null && personaId.equals(i.getPersonaId()))
                .collect(Collectors.toList());
    }
}
