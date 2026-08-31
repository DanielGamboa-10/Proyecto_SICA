package com.zonaacme.sica.notifications.adapters;

import com.zonaacme.sica.auth.events.UsuarioBloqueadoEvent;
import com.zonaacme.sica.common.events.DomainEvent;
import com.zonaacme.sica.common.events.DomainEventListener;
import com.zonaacme.sica.core.events.AlertaAccesoDenegadoEvent;
import com.zonaacme.sica.core.events.VisitaAprobadaEvent;
import com.zonaacme.sica.core.events.VisitaSolicitadaEvent;
import com.zonaacme.sica.notifications.domain.CanalNotificacion;
import com.zonaacme.sica.notifications.domain.Notificacion;
import com.zonaacme.sica.notifications.domain.TipoNotificacion;
import com.zonaacme.sica.notifications.ports.in.NotificationUseCase;

import java.util.Objects;

/**
 * Adaptador Observador desacoplado que reacciona a los eventos de dominio para generar y despachar
 * notificaciones y alertas en tiempo real sin interferir con las operaciones transaccionales de origen.
 *
 * <p><b>Patrón de Diseño:</b> Observer (Listener)</p>
 * <p><b>Principios SOLID aplicados:</b></p>
 * <ul>
 *   <li><b>SRP (Single Responsibility Principle):</b> Responsabilidad única de transformar eventos
 *   de negocio en notificaciones para destinatarios específicos o la central de monitoreo.</li>
 *   <li><b>OCP (Open/Closed Principle):</b> Nuevas reacciones o tipos de notificación pueden agregarse
 *   sin modificar los servicios de dominio principales.</li>
 *   <li><b>DIP (Dependency Inversion Principle):</b> Depende de la abstracción {@link NotificationUseCase}.</li>
 * </ul>
 */
public class NotificationEventListener implements DomainEventListener<DomainEvent> {

    private final NotificationUseCase notificationUseCase;

    public NotificationEventListener(NotificationUseCase notificationUseCase) {
        this.notificationUseCase = Objects.requireNonNull(notificationUseCase, "NotificationUseCase no puede ser nulo");
    }

    @Override
    public void onEvent(DomainEvent event) {
        if (event == null) {
            return;
        }

        if (event instanceof VisitaSolicitadaEvent) {
            handleVisitaSolicitada((VisitaSolicitadaEvent) event);
        } else if (event instanceof VisitaAprobadaEvent) {
            handleVisitaAprobada((VisitaAprobadaEvent) event);
        } else if (event instanceof AlertaAccesoDenegadoEvent) {
            handleAlertaAccesoDenegado((AlertaAccesoDenegadoEvent) event);
        } else if (event instanceof UsuarioBloqueadoEvent) {
            handleUsuarioBloqueado((UsuarioBloqueadoEvent) event);
        }
    }

    private void handleVisitaSolicitada(VisitaSolicitadaEvent event) {
        Notificacion notificacion = Notificacion.crear(
                event.getAnfitrionId(),
                TipoNotificacion.VISITA_SOLICITADA,
                "Nueva Solicitud de Visita Pendiente",
                String.format("El visitante '%s' solicita ingreso para visita '%s'. Motivo: %s",
                        event.getVisitanteId(), event.getVisitaId(), event.getMotivo()),
                CanalNotificacion.EMAIL
        );
        notificationUseCase.enviarNotificacion(notificacion);
    }

    private void handleVisitaAprobada(VisitaAprobadaEvent event) {
        Notificacion notificacion = Notificacion.crear(
                event.getVisitaId(),
                TipoNotificacion.VISITA_APROBADA,
                "Solicitud de Visita Aprobada",
                String.format("Su visita '%s' ha sido aprobada por el anfitrión '%s'. Observaciones: %s",
                        event.getVisitaId(), event.getAnfitrionId(), event.getObservacion()),
                CanalNotificacion.EMAIL
        );
        notificationUseCase.enviarNotificacion(notificacion);
    }

    private void handleAlertaAccesoDenegado(AlertaAccesoDenegadoEvent event) {
        Notificacion alerta = Notificacion.crear(
                "CENTRAL_SEGURIDAD",
                TipoNotificacion.ALERTA_SEGURIDAD_ACCESO_DENEGADO,
                "🚨 ALERTA: Acceso Denegado en " + event.getPuntoControlId(),
                String.format("Acceso denegado para persona '%s' en punto '%s' (Zona: %s). Causa: %s. Detalle: %s",
                        event.getPersonaId(), event.getPuntoControlId(), event.getZonaId(),
                        event.getMotivoDenegacion(), event.getDetalle()),
                CanalNotificacion.ALERTA_MONITOR_GUARDIA
        );
        notificationUseCase.enviarNotificacion(alerta);
    }

    private void handleUsuarioBloqueado(UsuarioBloqueadoEvent event) {
        Notificacion alerta = Notificacion.crear(
                "ADMIN_SEGURIDAD",
                TipoNotificacion.ALERTA_USUARIO_BLOQUEADO,
                "🚨 ALERTA CRÍTICA: Usuario Bloqueado por Seguridad",
                String.format("El usuario '%s' (ID: %s) ha sido bloqueado temporalmente hasta %s por exceso de intentos de inicio de sesión.",
                        event.getUsername(), event.getUsuarioId(), event.getBloqueadoHasta()),
                CanalNotificacion.ALERTA_MONITOR_GUARDIA
        );
        notificationUseCase.enviarNotificacion(alerta);
    }
}
