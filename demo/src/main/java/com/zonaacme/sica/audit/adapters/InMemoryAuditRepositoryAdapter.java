package com.zonaacme.sica.audit.adapters;

import com.zonaacme.sica.audit.domain.BitacoraAuditoria;
import com.zonaacme.sica.audit.ports.out.AuditRepositoryPort;

import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

/**
 * Adaptador de persistencia secundario en memoria para la bitácora de auditoría.
 *
 * <p><b>Arquitectura Hexagonal & Concurrencia:</b></p>
 * <ul>
 *   <li>Implementación desacoplada del puerto {@link AuditRepositoryPort} para testing y ejecución standalone.</li>
 *   <li>Uso de {@link CopyOnWriteArrayList} para garantizar thread-safety en entornos concurrentes.</li>
 *   <li>Uso intensivo de Java Moderno (Stream API y Lambdas) para filtrado y ordenamiento.</li>
 * </ul>
 */
public class InMemoryAuditRepositoryAdapter implements AuditRepositoryPort {

    private final List<BitacoraAuditoria> registros = new CopyOnWriteArrayList<>();

    @Override
    public void save(BitacoraAuditoria bitacora) {
        if (bitacora != null) {
            registros.add(bitacora);
        }
    }

    @Override
    public List<BitacoraAuditoria> findAll() {
        return registros.stream()
                .sorted(Comparator.comparing(BitacoraAuditoria::getFechaHora).reversed())
                .collect(Collectors.toList());
    }

    @Override
    public List<BitacoraAuditoria> findByUsuarioId(String usuarioId) {
        return registros.stream()
                .filter(r -> r.getUsuarioId().equalsIgnoreCase(usuarioId))
                .sorted(Comparator.comparing(BitacoraAuditoria::getFechaHora).reversed())
                .collect(Collectors.toList());
    }

    @Override
    public List<BitacoraAuditoria> findByEntidadAfectada(String entidadAfectada) {
        return registros.stream()
                .filter(r -> r.getEntidadAfectada().equalsIgnoreCase(entidadAfectada))
                .sorted(Comparator.comparing(BitacoraAuditoria::getFechaHora).reversed())
                .collect(Collectors.toList());
    }

    public void clear() {
        registros.clear();
    }
}
