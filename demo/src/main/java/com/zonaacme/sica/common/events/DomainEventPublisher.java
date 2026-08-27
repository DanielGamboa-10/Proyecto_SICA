package com.zonaacme.sica.common.events;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Publicador central desacoplado de eventos de dominio (Event Bus).
 *
 * <p><b>Patrón de Diseño:</b> Observer (Subject / Event Bus) y Singleton / Mediator de Eventos.</p>
 * <p><b>Principios SOLID aplicados:</b></p>
 * <ul>
 *   <li><b>SRP (Single Responsibility Principle):</b> Responsabilidad única de gestionar el registro y la
 *   notificación thread-safe de observadores de eventos.</li>
 *   <li><b>OCP (Open/Closed Principle):</b> Permite extender el comportamiento del sistema agregando nuevos
 *   módulos observadores (e.g. auditoría, notificaciones) sin alterar los casos de uso transaccionales.</li>
 * </ul>
 * <p><b>Concurrencia:</b> Utiliza {@link ConcurrentHashMap} y {@link CopyOnWriteArrayList} para garantizar
 * operaciones seguras en entornos concurrentes.</p>
 */
public class DomainEventPublisher {

    private static final DomainEventPublisher INSTANCE = new DomainEventPublisher();

    @SuppressWarnings("rawtypes")
    private final Map<Class<? extends DomainEvent>, List<DomainEventListener>> listeners = new ConcurrentHashMap<>();

    private DomainEventPublisher() {
        // Constructor privado para control de instancia singleton o inyección
    }

    /**
     * Retorna la instancia global del publicador de eventos de dominio.
     *
     * @return Instancia única de {@link DomainEventPublisher}.
     */
    public static DomainEventPublisher getInstance() {
        return INSTANCE;
    }

    /**
     * Suscribe un listener a un tipo específico de evento de dominio.
     *
     * @param <T> Tipo de evento.
     * @param eventType Clase del evento a escuchar.
     * @param listener Observador que procesará el evento.
     */
    public <T extends DomainEvent> void subscribe(Class<T> eventType, DomainEventListener<T> listener) {
        listeners.computeIfAbsent(eventType, k -> new CopyOnWriteArrayList<>()).add(listener);
    }

    /**
     * Publica un evento de dominio notificando a todos los observadores registrados para dicho tipo.
     *
     * @param <T> Tipo de evento.
     * @param event Instancia del evento a publicar.
     */
    @SuppressWarnings("unchecked")
    public <T extends DomainEvent> void publish(T event) {
        if (event == null) {
            return;
        }
        
        // Notificar a listeners del tipo exacto
        List<DomainEventListener> exactListeners = listeners.get(event.getClass());
        if (exactListeners != null) {
            for (DomainEventListener listener : exactListeners) {
                try {
                    listener.onEvent(event);
                } catch (Exception ex) {
                    // Prevenir que una falla en un listener afecte el flujo principal o a otros listeners
                    System.err.println("Error procesando evento " + event.getNombreEvento() + ": " + ex.getMessage());
                }
            }
        }

        // Notificar a listeners suscritos a la interfaz general DomainEvent
        if (!event.getClass().equals(DomainEvent.class)) {
            List<DomainEventListener> genericListeners = listeners.get(DomainEvent.class);
            if (genericListeners != null) {
                for (DomainEventListener listener : genericListeners) {
                    try {
                        listener.onEvent(event);
                    } catch (Exception ex) {
                        System.err.println("Error procesando evento genérico: " + ex.getMessage());
                    }
                }
            }
        }
    }

    /**
     * Limpia todos los suscriptores (útil para pruebas unitarias).
     */
    public void reset() {
        listeners.clear();
    }
}
