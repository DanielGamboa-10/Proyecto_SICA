package com.zonaacme.sica.common.events;

/**
 * Contrato de observador para componentes interesados en reaccionar a eventos del dominio.
 *
 * <p><b>Patrón de Diseño:</b> Observer (Subscriber)</p>
 * <p><b>Principio SOLID aplicado:</b></p>
 * <ul>
 *   <li><b>DIP (Dependency Inversion Principle):</b> Los publicadores de eventos no dependen de implementaciones
 *   concretas de listeners, sino de esta abstracción.</li>
 *   <li><b>OCP (Open/Closed Principle):</b> Nuevos observadores pueden agregarse al sistema sin modificar
 *   el código existente de emisión de eventos.</li>
 * </ul>
 *
 * @param <T> Tipo específico de evento que extiende de {@link DomainEvent}.
 */
@FunctionalInterface
public interface DomainEventListener<T extends DomainEvent> {

    /**
     * Método ejecutado al dispararse el evento del dominio correspondiente.
     *
     * @param event Instancia inmutable del evento emitido.
     */
    void onEvent(T event);
}
