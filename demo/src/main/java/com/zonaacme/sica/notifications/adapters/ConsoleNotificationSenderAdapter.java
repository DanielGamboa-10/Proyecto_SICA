package com.zonaacme.sica.notifications.adapters;

import com.zonaacme.sica.notifications.domain.Notificacion;
import com.zonaacme.sica.notifications.ports.out.NotificationSenderPort;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Adaptador de salida que despacha notificaciones formateadas hacia la consola/monitores de seguridad
 * y almacena una bitácora en memoria de los envíos realizados.
 *
 * <p><b>Arquitectura Hexagonal & Principios SOLID:</b></p>
 * <ul>
 *   <li><b>DIP (Dependency Inversion Principle):</b> Implementa {@link NotificationSenderPort}, permitiendo
 *   reemplazar o complementar la emisión a proveedores externos (ej. SendGrid, Twilio) sin afectar la lógica de negocio.</li>
 *   <li><b>SRP (Single Responsibility Principle):</b> Responsabilidad única de dar formato y emitir los mensajes según el canal.</li>
 * </ul>
 */
public class ConsoleNotificationSenderAdapter implements NotificationSenderPort {

    private final List<Notificacion> notificacionesEnviadas = new CopyOnWriteArrayList<>();

    @Override
    public void send(Notificacion notificacion) {
        if (notificacion == null) {
            return;
        }

        notificacionesEnviadas.add(notificacion);

        // Formateo visual y despacho según el canal tecnológico asignado
        switch (notificacion.getCanal()) {
            case ALERTA_MONITOR_GUARDIA:
                System.out.println(String.format("[MONITOR SEGURIDAD] (%s) %s -> %s: %s",
                        notificacion.getTipo().getNivel(),
                        notificacion.getAsunto(),
                        notificacion.getDestinatarioId(),
                        notificacion.getCuerpo()));
                break;
            case EMAIL:
                System.out.println(String.format("[EMAIL] Para: %s | Asunto: %s | Cuerpo: %s",
                        notificacion.getDestinatarioId(),
                        notificacion.getAsunto(),
                        notificacion.getCuerpo()));
                break;
            case SMS:
                System.out.println(String.format("[SMS] Para: %s | Mensaje: %s",
                        notificacion.getDestinatarioId(),
                        notificacion.getCuerpo()));
                break;
            case CONSOLA_INTERNA:
            default:
                System.out.println(String.format("[NOTIFICACIÓN INTERNA] Para: %s | %s - %s",
                        notificacion.getDestinatarioId(),
                        notificacion.getAsunto(),
                        notificacion.getCuerpo()));
                break;
        }
    }

    public List<Notificacion> getNotificacionesEnviadas() {
        return Collections.unmodifiableList(notificacionesEnviadas);
    }

    public void clear() {
        notificacionesEnviadas.clear();
    }
}
