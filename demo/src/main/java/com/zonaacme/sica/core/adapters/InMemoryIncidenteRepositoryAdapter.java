package com.zonaacme.sica.core.adapters;

import com.zonaacme.sica.core.domain.Incidente;
import com.zonaacme.sica.core.ports.out.IncidenteRepositoryPort;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Adaptador de repositorio en memoria para incidentes.
 */
public class InMemoryIncidenteRepositoryAdapter implements IncidenteRepositoryPort {

    private final Map<String, Incidente> memoria = new ConcurrentHashMap<>();

    @Override
    public void save(Incidente incidente) {
        Objects.requireNonNull(incidente, "El incidente no puede ser nulo");
        memoria.put(incidente.getId(), incidente);
    }

    @Override
    public Optional<Incidente> findById(String id) {
        if (id == null) return Optional.empty();
        return Optional.ofNullable(memoria.get(id));
    }

    @Override
    public List<Incidente> findAll() {
        return new ArrayList<>(memoria.values());
    }

    @Override
    public List<Incidente> findByPersonaId(String personaId) {
        if (personaId == null) return Collections.emptyList();
        return memoria.values().stream()
                .filter(i -> personaId.equals(i.getPersonaId()))
                .collect(Collectors.toList());
    }
}
