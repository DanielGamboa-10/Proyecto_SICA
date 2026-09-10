package com.zonaacme.sica.core.adapters;

import com.sica.shared.infrastructure.config.DatabaseConnection;
import com.zonaacme.sica.core.domain.RegistroAcceso;
import com.zonaacme.sica.core.domain.ResultadoAcceso;
import com.zonaacme.sica.core.domain.TipoAcceso;
import com.zonaacme.sica.core.ports.out.RegistroAccesoRepositoryPort;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Adaptador JDBC para el repositorio de Registros de Acceso físico.
 * Conecta directamente a la base de datos MySQL (sica_db.registros_acceso).
 */
public class RegistroAccesoJdbcRepositoryAdapter implements RegistroAccesoRepositoryPort {

    private final Map<String, RegistroAcceso> memoriaPorId = new ConcurrentHashMap<>();

    public RegistroAccesoJdbcRepositoryAdapter() {
        cargarDesdeMySQL();
    }

    private Connection getConn() {
        return DatabaseConnection.getInstance().getConnection();
    }

    private void cargarDesdeMySQL() {
        Connection conn = getConn();
        if (conn != null) {
            try {
                String sql = "SELECT * FROM registros_acceso ORDER BY fecha_hora DESC LIMIT 100";
                try (Statement stmt = conn.createStatement();
                     ResultSet rs = stmt.executeQuery(sql)) {
                    while (rs.next()) {
                        long dbId = rs.getLong("id");
                        int personaId = rs.getInt("persona_id");
                        int puntoId = rs.getInt("punto_control_id");
                        int visId = rs.getInt("visita_id");
                        Timestamp fh = rs.getTimestamp("fecha_hora");
                        String tipoStr = rs.getString("tipo_acceso");
                        String resStr = rs.getString("resultado");
                        String obs = rs.getString("observacion");

                        TipoAcceso tipo = "SALIDA".equalsIgnoreCase(tipoStr) ? TipoAcceso.SALIDA : TipoAcceso.ENTRADA;
                        ResultadoAcceso resultado = ResultadoAcceso.PERMITIDO;
                        if (resStr != null && resStr.contains("DENEGADO")) {
                            if (resStr.contains("BLOQUEADA") || resStr.contains("INACTIVA")) resultado = ResultadoAcceso.DENEGADO_PERSONA_INACTIVA;
                            else if (resStr.contains("VISITA")) resultado = ResultadoAcceso.DENEGADO_SIN_VISITA_APROBADA;
                            else if (resStr.contains("HORARIO")) resultado = ResultadoAcceso.DENEGADO_FUERA_DE_HORARIO;
                            else if (resStr.contains("AFORO")) resultado = ResultadoAcceso.DENEGADO_AFORO_MAXIMO;
                            else resultado = ResultadoAcceso.DENEGADO_PUNTO_CONTROL_INACTIVO;
                        }

                        RegistroAcceso reg = new RegistroAcceso(
                                "REG_" + dbId,
                                "PER_" + personaId,
                                "PC_" + puntoId,
                                "ZON_1",
                                tipo,
                                fh != null ? fh.toLocalDateTime() : LocalDateTime.now(),
                                resultado,
                                obs != null ? obs : "Registro de paso",
                                visId > 0 ? "VIS_" + visId : null
                        );
                        memoriaPorId.put(reg.getId(), reg);
                    }
                }
            } catch (SQLException e) {
                System.err.println("⚠️ [RegistroAccesoJdbcRepo] Error al cargar registros de MySQL: " + e.getMessage());
            }
        }
    }

    @Override
    public void save(RegistroAcceso registro) {
        Objects.requireNonNull(registro, "El registro no puede ser nulo");
        memoriaPorId.put(registro.getId(), registro);

        Connection conn = getConn();
        if (conn != null) {
            try {
                int pId = 1;
                int pcId = 1;
                Integer vId = null;

                try {
                    String pidStr = registro.getPersonaId();
                    if (pidStr.startsWith("PER_")) pId = Integer.parseInt(pidStr.substring(4));
                    String pcStr = registro.getPuntoControlId();
                    if (pcStr.startsWith("PC_")) pcId = Integer.parseInt(pcStr.substring(3));
                    if (registro.getVisitaId() != null && registro.getVisitaId().startsWith("VIS_")) {
                        vId = Integer.parseInt(registro.getVisitaId().substring(4));
                    }
                } catch (Exception ignored) {}

                String resStr = "PERMITIDO";
                if (!registro.esExitoso()) {
                    if (registro.getResultado() == ResultadoAcceso.DENEGADO_PERSONA_INACTIVA) resStr = "DENEGADO_PERSONA_BLOQUEADA";
                    else if (registro.getResultado() == ResultadoAcceso.DENEGADO_SIN_VISITA_APROBADA) resStr = "DENEGADO_SIN_VISITA";
                    else if (registro.getResultado() == ResultadoAcceso.DENEGADO_FUERA_DE_HORARIO) resStr = "DENEGADO_FUERA_HORARIO";
                    else resStr = "DENEGADO_PUNTO_INACTIVO";
                }

                String sql = "INSERT INTO registros_acceso (persona_id, punto_control_id, visita_id, fecha_hora, tipo_acceso, resultado, observacion) " +
                             "VALUES (?, ?, ?, ?, ?, ?, ?)";
                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setInt(1, pId);
                    ps.setInt(2, pcId);
                    if (vId != null) ps.setInt(3, vId); else ps.setNull(3, Types.INTEGER);
                    ps.setTimestamp(4, Timestamp.valueOf(registro.getFechaHora()));
                    ps.setString(5, registro.getTipoAcceso().name());
                    ps.setString(6, resStr);
                    ps.setString(7, registro.getObservaciones());
                    ps.executeUpdate();
                }
            } catch (SQLException e) {
                System.err.println("⚠️ [RegistroAccesoJdbcRepo] Error al insertar registro en MySQL: " + e.getMessage());
            }
        }
    }

    @Override
    public Optional<RegistroAcceso> findById(String id) {
        if (id == null) return Optional.empty();
        return Optional.ofNullable(memoriaPorId.get(id));
    }

    @Override
    public List<RegistroAcceso> findAll() {
        return new ArrayList<>(memoriaPorId.values());
    }

    @Override
    public List<RegistroAcceso> findByPersonaId(String personaId) {
        if (personaId == null) return Collections.emptyList();
        return memoriaPorId.values().stream()
                .filter(r -> r.getPersonaId().equals(personaId))
                .collect(Collectors.toList());
    }

    @Override
    public List<RegistroAcceso> findByPuntoControlId(String puntoControlId) {
        if (puntoControlId == null) return Collections.emptyList();
        return memoriaPorId.values().stream()
                .filter(r -> r.getPuntoControlId().equals(puntoControlId))
                .collect(Collectors.toList());
    }

    @Override
    public List<RegistroAcceso> findByZonaId(String zonaId) {
        if (zonaId == null) return Collections.emptyList();
        return memoriaPorId.values().stream()
                .filter(r -> r.getZonaId().equals(zonaId))
                .collect(Collectors.toList());
    }
}
