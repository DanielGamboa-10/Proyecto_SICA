package com.zonaacme.sica.audit.adapters;

import com.sica.shared.infrastructure.config.DatabaseConnection;
import com.zonaacme.sica.audit.domain.BitacoraAuditoria;
import com.zonaacme.sica.audit.ports.out.AuditRepositoryPort;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

/**
 * Adaptador JDBC para el repositorio de Auditoría.
 * Conecta directamente a la base de datos MySQL (sica_db.bitacora_auditoria).
 */
public class AuditJdbcRepositoryAdapter implements AuditRepositoryPort {

    private final List<BitacoraAuditoria> memoriaAudit = new CopyOnWriteArrayList<>();

    public AuditJdbcRepositoryAdapter() {
        inicializarSemillaMemoria();
        cargarDesdeMySQL();
    }

    private void inicializarSemillaMemoria() {
        memoriaAudit.add(new BitacoraAuditoria(
                "AUD_1", "admin@zonaacme.com", "LOGIN_EXITOSO", "usuarios", "Inicio de sesión Superusuario",
                LocalDateTime.now().minusHours(4), "192.168.1.10"
        ));
        memoriaAudit.add(new BitacoraAuditoria(
                "AUD_2", "guardia1@zonaacme.com", "CHECKIN_VISITA", "visitas", "Check-in autorizado para Mario Alberto Visitante",
                LocalDateTime.now().minusHours(2), "192.168.1.25"
        ));
        memoriaAudit.add(new BitacoraAuditoria(
                "AUD_3", "guardia1@zonaacme.com", "ALERTA_ACCESO_DENEGADO", "personas", "Detección de persona bloqueada CC 99998888",
                LocalDateTime.now().minusHours(1), "192.168.1.25"
        ));
    }

    private Connection getConn() {
        return DatabaseConnection.getInstance().getConnection();
    }

    private void cargarDesdeMySQL() {
        Connection conn = getConn();
        if (conn != null) {
            try {
                String sql = "SELECT * FROM bitacora_auditoria ORDER BY fecha_hora DESC LIMIT 100";
                try (Statement stmt = conn.createStatement();
                     ResultSet rs = stmt.executeQuery(sql)) {
                    while (rs.next()) {
                        long dbId = rs.getLong("id");
                        int uId = rs.getInt("usuario_id");
                        Timestamp fh = rs.getTimestamp("fecha_hora");
                        String accion = rs.getString("accion_realizada");
                        String tabla = rs.getString("tabla_afectada");
                        String ip = rs.getString("direccion_ip");
                        String detalle = rs.getString("detalles");

                        BitacoraAuditoria b = new BitacoraAuditoria(
                                "AUD_" + dbId,
                                uId > 0 ? "USR_" + uId : "SISTEMA",
                                accion != null ? accion : "OPERACION",
                                tabla != null ? tabla : "GENERAL",
                                detalle != null ? detalle : "",
                                fh != null ? fh.toLocalDateTime() : LocalDateTime.now(),
                                ip != null ? ip : "127.0.0.1"
                        );
                        memoriaAudit.add(0, b);
                    }
                }
            } catch (SQLException e) {
                System.err.println("⚠️ [AuditJdbcRepo] Error al cargar auditoría de MySQL: " + e.getMessage());
            }
        }
    }

    @Override
    public void save(BitacoraAuditoria bitacora) {
        Objects.requireNonNull(bitacora, "El registro de auditoría no puede ser nulo");
        memoriaAudit.add(bitacora);

        Connection conn = getConn();
        if (conn != null) {
            try {
                Integer uId = null;
                try {
                    String uidStr = bitacora.getUsuarioId();
                    if (uidStr.startsWith("USR_")) uId = Integer.parseInt(uidStr.substring(4));
                    else if (uidStr.matches("\\d+")) uId = Integer.parseInt(uidStr);
                } catch (Exception ignored) {}

                String sql = "INSERT INTO bitacora_auditoria (usuario_id, fecha_hora, accion_realizada, tabla_afectada, direccion_ip, detalles) " +
                             "VALUES (?, ?, ?, ?, ?, ?)";
                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    if (uId != null) ps.setInt(1, uId); else ps.setNull(1, Types.INTEGER);
                    ps.setTimestamp(2, Timestamp.valueOf(bitacora.getFechaHora()));
                    ps.setString(3, bitacora.getAccion());
                    ps.setString(4, bitacora.getEntidadAfectada());
                    ps.setString(5, bitacora.getOrigen());
                    ps.setString(6, bitacora.getDetalle());
                    ps.executeUpdate();
                }
            } catch (SQLException e) {
                System.err.println("⚠️ [AuditJdbcRepo] Error al registrar auditoría en MySQL: " + e.getMessage());
            }
        }
    }

    @Override
    public List<BitacoraAuditoria> findAll() {
        return Collections.unmodifiableList(new ArrayList<>(memoriaAudit));
    }

    @Override
    public List<BitacoraAuditoria> findByUsuarioId(String usuarioId) {
        if (usuarioId == null) return Collections.emptyList();
        return memoriaAudit.stream()
                .filter(b -> b.getUsuarioId().equalsIgnoreCase(usuarioId))
                .collect(Collectors.toList());
    }

    @Override
    public List<BitacoraAuditoria> findByEntidadAfectada(String entidadAfectada) {
        if (entidadAfectada == null) return Collections.emptyList();
        return memoriaAudit.stream()
                .filter(b -> b.getEntidadAfectada().equalsIgnoreCase(entidadAfectada))
                .collect(Collectors.toList());
    }
}
