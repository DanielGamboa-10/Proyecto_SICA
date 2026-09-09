package com.zonaacme.sica.core.adapters;

import com.zonaacme.sica.core.domain.EstadoVisita;
import com.zonaacme.sica.core.domain.SolicitudVisita;
import com.zonaacme.sica.core.ports.out.VisitaRepositoryPort;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Adaptador secundario para almacenamiento de solicitudes de visita en memoria.
 *
 * <p><b>Arquitectura Hexagonal & Principios SOLID:</b></p>
 * <ul>
 *   <li><b>LSP (Liskov Substitution Principle):</b> Implementa {@link VisitaRepositoryPort} sin ataduras tecnológicas.</li>
 *   <li><b>Concurrencia:</b> Soporta operaciones concurrentes seguras con {@link ConcurrentHashMap}.</li>
 * </ul>
 */
public class InMemoryVisitaRepositoryAdapter implements VisitaRepositoryPort {

    private final Map<String, SolicitudVisita> visitasPorId = new ConcurrentHashMap<>();

    @Override
    public void save(SolicitudVisita visita) {
        Objects.requireNonNull(visita, "La visita no puede ser nula");
        visitasPorId.put(visita.getId(), visita);
    }

    @Override
    public Optional<SolicitudVisita> findById(String id) {
        if (id == null) return Optional.empty();
        return Optional.ofNullable(visitasPorId.get(id));
    }

    @Override
    public List<SolicitudVisita> findByVisitanteId(String visitanteId) {
        if (visitanteId == null) return Collections.emptyList();
        return visitasPorId.values().stream()
                .filter(v -> v.getVisitanteId().equals(visitanteId))
                .collect(Collectors.collectingAndThen(Collectors.toList(), Collections::unmodifiableList));
    }

    @Override
    public List<SolicitudVisita> findByAnfitrionId(String anfitrionId) {
        if (anfitrionId == null) return Collections.emptyList();
        return visitasPorId.values().stream()
                .filter(v -> v.getAnfitrionId().equals(anfitrionId))
                .collect(Collectors.collectingAndThen(Collectors.toList(), Collections::unmodifiableList));
    }

    @Override
    public List<SolicitudVisita> findAll() {
        return Collections.unmodifiableList(new ArrayList<>(visitasPorId.values()));
    }

    @Override
    public Optional<SolicitudVisita> findVisitaActivaPorVisitante(String visitanteId) {
        if (visitanteId == null) return Optional.empty();
        return visitasPorId.values().stream()
                .filter(v -> v.getVisitanteId().equals(visitanteId))
                .filter(v -> v.getEstado() == EstadoVisita.APROBADA || v.getEstado() == EstadoVisita.EN_CURSO)
                .findFirst();
    }

    public void inicializarSemilla(com.zonaacme.sica.core.ports.out.PersonaRepositoryPort personaRepo, com.zonaacme.sica.core.ports.out.ZonaRepositoryPort zonaRepo) {
        try {
            var marioOpt = personaRepo.findByDocumento("CC", "80809090");
            var robertoOpt = personaRepo.findByDocumento("CC", "10102020");
            var valentinaOpt = personaRepo.findByDocumento("CC", "10203040");
            var julianOpt = personaRepo.findByDocumento("CC", "60607070");
            var sofiaOpt = personaRepo.findByDocumento("CC", "10405060");
            var mauricioOpt = personaRepo.findByDocumento("CC", "10304050");
            var elenaOpt = personaRepo.findByDocumento("CC", "70708080");

            java.util.Set<String> todasLasZonas = zonaRepo.findAllZonas().stream()
                    .map(z -> z.getId())
                    .collect(Collectors.toSet());

            // Flujo 1: Mario Alberto (CC 80809090) Pre-registrado Aprobado
            if (marioOpt.isPresent() && robertoOpt.isPresent()) {
                SolicitudVisita v1 = new SolicitudVisita(
                        "VIS-DEMO-001",
                        marioOpt.get().getId(),
                        robertoOpt.get().getId(),
                        "Auditoría Anual ISO 27001",
                        java.time.LocalDateTime.now().minusHours(2),
                        java.time.LocalDateTime.now().plusHours(8),
                        todasLasZonas,
                        EstadoVisita.APROBADA,
                        "ABC-123",
                        "Aprobada previamente por el anfitrión Roberto Gómez",
                        java.time.LocalDateTime.now().minusDays(1),
                        null,
                        null
                );
                save(v1);
            }

            // Flujo 2: Julian Arango (CC 60607070) No anunciado (Pendiente de Aprobación en Tiempo Real)
            if (julianOpt.isPresent() && valentinaOpt.isPresent()) {
                SolicitudVisita v2 = new SolicitudVisita(
                        "VIS-DEMO-002",
                        julianOpt.get().getId(),
                        valentinaOpt.get().getId(),
                        "Reunión urgente de laboratorio",
                        java.time.LocalDateTime.now().minusHours(1),
                        java.time.LocalDateTime.now().plusHours(4),
                        todasLasZonas,
                        EstadoVisita.PENDIENTE,
                        "XYZ-789",
                        null,
                        java.time.LocalDateTime.now(),
                        null,
                        null
                );
                save(v2);
            }

            // Flujo 3: Sofía Carvajal (CC 10405060) Carnet Olvidado (Pase Temporal)
            if (sofiaOpt.isPresent() && mauricioOpt.isPresent()) {
                SolicitudVisita v3 = new SolicitudVisita(
                        "VIS-DEMO-003",
                        sofiaOpt.get().getId(),
                        mauricioOpt.get().getId(),
                        "Olvido de carnet físico en recepción",
                        java.time.LocalDateTime.now().minusMinutes(30),
                        java.time.LocalDateTime.now().plusHours(6),
                        todasLasZonas,
                        EstadoVisita.PENDIENTE,
                        null,
                        null,
                        java.time.LocalDateTime.now(),
                        null,
                        null
                );
                save(v3);
            }

            // Flujo 4: Elena Torres (CC 70708080) Salida Olvidada (En curso desde turno anterior)
            if (elenaOpt.isPresent() && sofiaOpt.isPresent()) {
                SolicitudVisita v4 = new SolicitudVisita(
                        "VIS-DEMO-004",
                        elenaOpt.get().getId(),
                        sofiaOpt.get().getId(),
                        "Mantenimiento preventivo enlaces de fibra",
                        java.time.LocalDateTime.now().minusDays(1),
                        java.time.LocalDateTime.now().minusHours(4),
                        todasLasZonas,
                        EstadoVisita.EN_CURSO,
                        "KLR-456",
                        "Aprobada",
                        java.time.LocalDateTime.now().minusDays(1),
                        java.time.LocalDateTime.now().minusDays(1).plusMinutes(10),
                        null
                );
                save(v4);
            }
            System.out.println("📋 [SICA] Semilla de visitas inicializada correctamente (4 flujos de rúbrica listos).");
        } catch (Exception e) {
            System.err.println("⚠️ [SICA] Error al inicializar semilla de visitas: " + e.getMessage());
        }
    }

    public void reset() {
        visitasPorId.clear();
    }
}
