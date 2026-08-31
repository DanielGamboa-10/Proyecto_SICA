package com.zonaacme.sica.notifications.ports.out;

import com.zonaacme.sica.notifications.domain.Notificacion;

/**
 * Puerto Secundario / de Salida para la transmisión física o digital del mensaje (Email, SMS, Monitor).
 *
 * <p><b>Arquitectura Hexagonal & Principios SOLID:</b></p>
 * <ul>
 *   <li><b>DIP (Dependency Inversion Principle):</b> Desacopla proveedores de mensajería externos o adaptadores de consola.</li>
 * </ul>
 */
public interface NotificationSenderPort {

    /**
     * Envía la notificación a través del canal tecnológico especificado.
     *
     * @param notificacion Instancia inmutable del mensaje a transmitir.
     */
    void send(Notificacion notificacion);
}
