package com.zonaacme.sica.notifications.adapters;

import com.zonaacme.sica.notifications.domain.Notificacion;
import com.zonaacme.sica.notifications.ports.out.NotificationRepositoryPort;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Adaptador de persistencia secundario en memoria para la gestión de notificaciones y alertas.
 *
 * <p><b>Arquitectura Hexagonal & Concurrencia:</b></p>
 * <ul>
 *   <li>Implementación desacoplada del puerto {@link NotificationRepositoryPort}.</li>
 *   <li>Almacenamiento seguro en memoria mediante {@link ConcurrentHashMap}.</li>
 *   <li>Uso de Java Moderno (Streams API) para ordenamiento cronológico inverso (los más recientes primero).</li>
 * </ul>
 */
public class InMemoryNotificationRepositoryAdapter implements NotificationRepositoryPort {

    private final Map<String, Notificacion> storage = new ConcurrentHashMap<>();

    @Override
    public void save(Notificacion notificacion) {
        if (notificacion != null) {
            storage.put(notificacion.getId(), notificacion);
        }
    }

    @Override
    public Optional<Notificacion> findById(String id) {
        if (id == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(storage.get(id));
    }

    @Override
    public List<Notificacion> findByDestinatarioId(String destinatarioId) {
        if (destinatarioId == null) {
            return List.of();
        }
        return storage.values().stream()
                .filter(n -> n.getDestinatarioId().equalsIgnoreCase(destinatarioId))
                .sorted(Comparator.comparing(Notificacion::getFechaHora).reversed())
                .collect(Collectors.toList());
    }

    @Override
    public List<Notificacion> findAlertasSeguridad() {
        return storage.values().stream()
                .filter(n -> n.getTipo().esCritico())
                .sorted(Comparator.comparing(Notificacion::getFechaHora).reversed())
                .collect(Collectors.toList());
    }

    @Override
    public List<Notificacion> findAll() {
        return storage.values().stream()
                .sorted(Comparator.comparing(Notificacion::getFechaHora).reversed())
                .collect(Collectors.toList());
    }

    public void clear() {
        storage.clear();
    }
}
