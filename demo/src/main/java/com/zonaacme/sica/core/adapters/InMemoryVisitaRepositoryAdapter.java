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

    public void reset() {
        visitasPorId.clear();
    }
}
