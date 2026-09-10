package com.zonaacme.sica.core.adapters;

import com.sica.shared.infrastructure.config.DatabaseConnection;
import com.zonaacme.sica.core.domain.EstadoVisita;
import com.zonaacme.sica.core.domain.Persona;
import com.zonaacme.sica.core.domain.SolicitudVisita;
import com.zonaacme.sica.core.ports.out.VisitaRepositoryPort;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Adaptador JDBC para el repositorio de Visitas.
 * Conecta directamente a la base de datos MySQL (sica_db.visitas).
 */
public class VisitaJdbcRepositoryAdapter implements VisitaRepositoryPort {

    private final Map<String, SolicitudVisita> memoriaPorId = new ConcurrentHashMap<>();

    public VisitaJdbcRepositoryAdapter() {
        cargarDesdeMySQL();
    }

    public void inicializarSemilla(PersonaJdbcRepositoryAdapter personaRepo, ZonaJdbcRepositoryAdapter zonaRepo) {
        if (!memoriaPorId.isEmpty()) return;

        Optional<Persona> marioOpt = personaRepo.findByDocumento("CC", "80809090");
        Optional<Persona> robertoOpt = personaRepo.findByDocumento("CC", "10102020");
        Optional<Persona> valentinaOpt = personaRepo.findByDocumento("CC", "10203040");
        Optional<Persona> julianOpt = personaRepo.findByDocumento("CC", "60607070");
        Optional<Persona> elenaOpt = personaRepo.findByDocumento("CC", "70708080");
        Optional<Persona> sofiaOpt = personaRepo.findByDocumento("CC", "10405060");

        Set<String> todasZonas = zonaRepo.findAllZonas().stream().map(z -> z.getId()).collect(Collectors.toSet());
        LocalDateTime ahora = LocalDateTime.now();

        if (marioOpt.isPresent() && robertoOpt.isPresent()) {
            SolicitudVisita v1 = new SolicitudVisita(
                    "VIS_1",
                    marioOpt.get().getId(),
                    robertoOpt.get().getId(),
                    "Auditoría Anual ISO 27001",
                    ahora.minusHours(1),
                    ahora.plusHours(7),
                    todasZonas,
                    EstadoVisita.EN_CURSO,
                    "ABC-123",
                    "Aprobado por jefatura",
                    ahora.minusDays(1),
                    ahora.minusMinutes(45),
                    null
            );
            save(v1);
        }

        if (julianOpt.isPresent() && valentinaOpt.isPresent()) {
            SolicitudVisita v2 = new SolicitudVisita(
                    "VIS_2",
                    julianOpt.get().getId(),
                    valentinaOpt.get().getId(),
                    "Reunión urgente de laboratorio",
                    ahora.plusHours(1),
                    ahora.plusHours(3),
                    todasZonas,
                    EstadoVisita.PENDIENTE,
                    "XYZ-789",
                    null,
                    ahora,
                    null,
                    null
            );
            save(v2);
        }

        if (elenaOpt.isPresent() && sofiaOpt.isPresent()) {
            SolicitudVisita v3 = new SolicitudVisita(
                    "VIS_3",
                    elenaOpt.get().getId(),
                    sofiaOpt.get().getId(),
                    "Mantenimiento preventivo enlaces de fibra",
                    ahora.minusDays(1),
                    ahora.minusDays(1).plusHours(4),
                    todasZonas,
                    EstadoVisita.COMPLETADA,
                    "KLR-456",
                    "Aprobado",
                    ahora.minusDays(1),
                    ahora.minusDays(1).plusMinutes(10),
                    ahora.minusDays(1).plusHours(4)
            );
            save(v3);
        }
    }

    private Connection getConn() {
        return DatabaseConnection.getInstance().getConnection();
    }

    private int mapEstadoToId(EstadoVisita estado) {
        if (estado == null) return 1;
        switch (estado) {
            case PENDIENTE: return 1;
            case APROBADA: return 2;
            case EN_CURSO: return 3;
            case COMPLETADA: return 4;
            case RECHAZADA: return 5;
            case CANCELADA: return 6;
            default: return 1;
        }
    }

    private EstadoVisita mapIdToEstado(int estadoId) {
        switch (estadoId) {
            case 1: return EstadoVisita.PENDIENTE;
            case 2: return EstadoVisita.APROBADA;
            case 3: return EstadoVisita.EN_CURSO;
            case 4: case 7: return EstadoVisita.COMPLETADA;
            case 5: return EstadoVisita.RECHAZADA;
            case 6: return EstadoVisita.CANCELADA;
            case 8: return EstadoVisita.PENDIENTE;
            default: return EstadoVisita.PENDIENTE;
        }
    }

    private void cargarDesdeMySQL() {
        Connection conn = getConn();
        if (conn != null) {
            try {
                String sql = "SELECT * FROM visitas";
                try (Statement stmt = conn.createStatement();
                     ResultSet rs = stmt.executeQuery(sql)) {
                    while (rs.next()) {
                        int dbId = rs.getInt("id");
                        int personaId = rs.getInt("persona_id");
                        int anfitrionId = rs.getInt("anfitrion_id");
                        String motivo = rs.getString("motivo");
                        Timestamp fIni = rs.getTimestamp("fecha_prevista_inicio");
                        Timestamp fFin = rs.getTimestamp("fecha_prevista_fin");
                        Timestamp fEnt = rs.getTimestamp("fecha_entrada");
                        Timestamp fSal = rs.getTimestamp("fecha_salida");
                        int estadoId = rs.getInt("estado_visita_id");
                        String placa = rs.getString("vehiculo_placa");

                        String visId = "VIS_" + dbId;
                        LocalDateTime ini = fIni != null ? fIni.toLocalDateTime() : LocalDateTime.now().minusHours(1);
                        LocalDateTime fin = fFin != null ? fFin.toLocalDateTime() : ini.plusHours(4);
                        LocalDateTime ent = fEnt != null ? fEnt.toLocalDateTime() : null;
                        LocalDateTime sal = fSal != null ? fSal.toLocalDateTime() : null;

                        SolicitudVisita v = new SolicitudVisita(
                                visId,
                                "PER_" + personaId,
                                "PER_" + anfitrionId,
                                motivo != null ? motivo : "Visita comercial",
                                ini,
                                fin,
                                Collections.emptySet(),
                                mapIdToEstado(estadoId),
                                placa,
                                null,
                                ini.minusHours(1),
                                ent,
                                sal
                        );
                        memoriaPorId.put(v.getId(), v);
                    }
                }
            } catch (SQLException e) {
                System.err.println("⚠️ [VisitaJdbcRepo] Error al cargar visitas de MySQL: " + e.getMessage());
            }
        }
    }

    @Override
    public void save(SolicitudVisita visita) {
        Objects.requireNonNull(visita, "La visita no puede ser nula");
        memoriaPorId.put(visita.getId(), visita);

        Connection conn = getConn();
        if (conn != null) {
            try {
                int pId = 1;
                int aId = 1;
                try {
                    String visPid = visita.getVisitanteId();
                    if (visPid.startsWith("PER_")) pId = Integer.parseInt(visPid.substring(4));
                    String anfPid = visita.getAnfitrionId();
                    if (anfPid.startsWith("PER_")) aId = Integer.parseInt(anfPid.substring(4));
                } catch (Exception ignored) {}

                String sql = "INSERT INTO visitas (persona_id, anfitrion_id, motivo, fecha_prevista_inicio, " +
                             "fecha_prevista_fin, fecha_entrada, fecha_salida, estado_visita_id, vehiculo_placa) " +
                             "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setInt(1, pId);
                    ps.setInt(2, aId);
                    ps.setString(3, visita.getMotivo());
                    ps.setTimestamp(4, Timestamp.valueOf(visita.getFechaHoraInicio()));
                    ps.setTimestamp(5, Timestamp.valueOf(visita.getFechaHoraFin()));
                    ps.setTimestamp(6, visita.getFechaCheckIn() != null ? Timestamp.valueOf(visita.getFechaCheckIn()) : null);
                    ps.setTimestamp(7, visita.getFechaCheckOut() != null ? Timestamp.valueOf(visita.getFechaCheckOut()) : null);
                    ps.setInt(8, mapEstadoToId(visita.getEstado()));
                    ps.setString(9, visita.getPlacaVehiculo());
                    ps.executeUpdate();
                }
            } catch (SQLException e) {
                System.err.println("⚠️ [VisitaJdbcRepo] Error al persistir visita en MySQL: " + e.getMessage());
            }
        }
    }

    @Override
    public Optional<SolicitudVisita> findById(String id) {
        if (id == null) return Optional.empty();
        return Optional.ofNullable(memoriaPorId.get(id));
    }

    @Override
    public List<SolicitudVisita> findByVisitanteId(String visitanteId) {
        if (visitanteId == null) return Collections.emptyList();
        return memoriaPorId.values().stream()
                .filter(v -> v.getVisitanteId().equals(visitanteId))
                .collect(Collectors.toList());
    }

    @Override
    public List<SolicitudVisita> findByAnfitrionId(String anfitrionId) {
        if (anfitrionId == null) return Collections.emptyList();
        return memoriaPorId.values().stream()
                .filter(v -> v.getAnfitrionId().equals(anfitrionId))
                .collect(Collectors.toList());
    }

    @Override
    public List<SolicitudVisita> findAll() {
        return new ArrayList<>(memoriaPorId.values());
    }

    @Override
    public Optional<SolicitudVisita> findVisitaActivaPorVisitante(String visitanteId) {
        if (visitanteId == null) return Optional.empty();
        LocalDateTime ahora = LocalDateTime.now();
        return memoriaPorId.values().stream()
                .filter(v -> v.getVisitanteId().equals(visitanteId))
                .filter(v -> v.getEstado() == EstadoVisita.APROBADA || v.getEstado() == EstadoVisita.EN_CURSO)
                .filter(v -> v.estaVigente(ahora))
                .findFirst();
    }
}
