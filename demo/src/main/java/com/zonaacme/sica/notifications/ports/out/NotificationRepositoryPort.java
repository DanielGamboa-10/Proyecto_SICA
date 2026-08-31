package com.zonaacme.sica.notifications.ports.out;

import com.zonaacme.sica.notifications.domain.Notificacion;
import java.util.List;
import java.util.Optional;

/**
 * Puerto Secundario / de Salida para la persistencia y recuperación de notificaciones y alertas.
 *
 * <p><b>Arquitectura Hexagonal & Principios SOLID:</b></p>
 * <ul>
 *   <li><b>DIP (Dependency Inversion Principle):</b> Desacopla la persistencia de mensajes de la lógica de negocio.</li>
 * </ul>
 */
public interface NotificationRepositoryPort {

    void save(Notificacion notificacion);

    Optional<Notificacion> findById(String id);

    List<Notificacion> findByDestinatarioId(String destinatarioId);

    List<Notificacion> findAlertasSeguridad();

    List<Notificacion> findAll();
}
