package com.zonaacme.sica.notifications.ports.in;

import com.zonaacme.sica.notifications.domain.Notificacion;
import java.util.List;
import java.util.Optional;

/**
 * Puerto Primario / de Entrada para el caso de uso de gestión y despacho de notificaciones y alertas.
 *
 * <p><b>Principios SOLID aplicados:</b></p>
 * <ul>
 *   <li><b>ISP (Interface Segregation Principle):</b> Expone exclusivamente operaciones de envío,
 *   lectura y consulta de notificaciones del sistema.</li>
 *   <li><b>DIP (Dependency Inversion Principle):</b> Abstrae la lógica de alertas para clientes y observadores.</li>
 * </ul>
 */
public interface NotificationUseCase {

    /**
     * Despacha una notificación persistiendo su registro y enviándola por el canal configurado.
     */
    void enviarNotificacion(Notificacion notificacion);

    /**
     * Consulta las notificaciones dirigidas a un destinatario específico (ej. anfitrión o guardia).
     */
    List<Notificacion> consultarPorDestinatario(String destinatarioId);

    /**
     * Consulta todas las alertas críticas de seguridad.
     */
    List<Notificacion> consultarAlertasSeguridad();

    /**
     * Marca una notificación como leída.
     */
    void marcarComoLeida(String notificacionId);

    /**
     * Consulta una notificación por su identificador único.
     */
    Optional<Notificacion> consultarPorId(String notificacionId);
}
