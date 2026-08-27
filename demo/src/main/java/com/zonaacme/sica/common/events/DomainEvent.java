package com.zonaacme.sica.common.events;

import java.time.LocalDateTime;

/**
 * Contrato base para todos los eventos del dominio en la arquitectura Hexagonal / Event-Driven.
 *
 * <p><b>Principio SOLID aplicado:</b></p>
 * <ul>
 *   <li><b>ISP (Interface Segregation Principle):</b> Define una interfaz mínima, inmutable y cohesiva
 *   para representar la ocurrencia de un hecho de negocio.</li>
 * </ul>
 */
public interface DomainEvent {

    /**
     * Obtiene el identificador único o referencia de la entidad afectada por el evento.
     *
     * @return Identificador de la entidad.
     */
    String getEntidadId();

    /**
     * Obtiene el nombre descriptivo del evento de negocio.
     *
     * @return Nombre del evento.
     */
    String getNombreEvento();

    /**
     * Obtiene la estampa de tiempo exacta en que se generó el evento.
     *
     * @return Fecha y hora de ocurrencia.
     */
    LocalDateTime getOcurridoEn();
}
