package com.zonaacme.sica.core.adapters;

import com.zonaacme.sica.core.domain.RegistroAcceso;
import com.zonaacme.sica.core.ports.out.RegistroAccesoRepositoryPort;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Adaptador secundario para almacenamiento inmutable de transacciones de acceso físico en memoria.
 *
 * <p><b>Arquitectura Hexagonal & Principios SOLID:</b></p>
 * <ul>
 *   <li><b>LSP (Liskov Substitution Principle):</b> Cumple el contrato {@link RegistroAccesoRepositoryPort}.</li>
 *   <li><b>Concurrencia:</b> Utiliza {@link ConcurrentHashMap} para persistencia libre de condiciones de carrera.</li>
 * </ul>
 */
public class InMemoryRegistroAccesoRepositoryAdapter implements RegistroAccesoRepositoryPort {

    private final Map<String, RegistroAcceso> registrosPorId = new ConcurrentHashMap<>();

    @Override
    public void save(RegistroAcceso registro) {
        Objects.requireNonNull(registro, "El registro de acceso no puede ser nulo");
        registrosPorId.put(registro.getId(), registro);
    }

    @Override
    public Optional<RegistroAcceso> findById(String id) {
        if (id == null) return Optional.empty();
        return Optional.ofNullable(registrosPorId.get(id));
    }

    @Override
    public List<RegistroAcceso> findAll() {
        return Collections.unmodifiableList(new ArrayList<>(registrosPorId.values()));
    }

    @Override
    public List<RegistroAcceso> findByPersonaId(String personaId) {
        if (personaId == null) return Collections.emptyList();
        return registrosPorId.values().stream()
                .filter(r -> r.getPersonaId().equals(personaId))
                .collect(Collectors.collectingAndThen(Collectors.toList(), Collections::unmodifiableList));
    }

    @Override
    public List<RegistroAcceso> findByPuntoControlId(String puntoControlId) {
        if (puntoControlId == null) return Collections.emptyList();
        return registrosPorId.values().stream()
                .filter(r -> r.getPuntoControlId().equals(puntoControlId))
                .collect(Collectors.collectingAndThen(Collectors.toList(), Collections::unmodifiableList));
    }

    @Override
    public List<RegistroAcceso> findByZonaId(String zonaId) {
        if (zonaId == null) return Collections.emptyList();
        return registrosPorId.values().stream()
                .filter(r -> r.getZonaId().equals(zonaId))
                .collect(Collectors.collectingAndThen(Collectors.toList(), Collections::unmodifiableList));
    }

    public void reset() {
        registrosPorId.clear();
    }
}
