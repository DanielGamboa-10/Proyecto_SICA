package com.zonaacme.sica.notifications;

import com.zonaacme.sica.auth.events.UsuarioBloqueadoEvent;
import com.zonaacme.sica.common.events.DomainEvent;
import com.zonaacme.sica.common.events.DomainEventPublisher;
import com.zonaacme.sica.core.domain.ResultadoAcceso;
import com.zonaacme.sica.core.events.AlertaAccesoDenegadoEvent;
import com.zonaacme.sica.core.events.VisitaAprobadaEvent;
import com.zonaacme.sica.core.events.VisitaSolicitadaEvent;
import com.zonaacme.sica.notifications.adapters.ConsoleNotificationSenderAdapter;
import com.zonaacme.sica.notifications.adapters.InMemoryNotificationRepositoryAdapter;
import com.zonaacme.sica.notifications.adapters.NotificationEventListener;
import com.zonaacme.sica.notifications.adapters.NotificationService;
import com.zonaacme.sica.notifications.domain.CanalNotificacion;
import com.zonaacme.sica.notifications.domain.Notificacion;
import com.zonaacme.sica.notifications.domain.TipoNotificacion;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class NotificationEventListenerTest {

    private NotificationService notificationService;
    private ConsoleNotificationSenderAdapter sender;
    private DomainEventPublisher eventPublisher;

    @BeforeEach
    void setUp() {
        eventPublisher = DomainEventPublisher.getInstance();
        eventPublisher.reset();

        InMemoryNotificationRepositoryAdapter repository = new InMemoryNotificationRepositoryAdapter();
        sender = new ConsoleNotificationSenderAdapter();
        notificationService = new NotificationService(repository, sender);

        NotificationEventListener listener = new NotificationEventListener(notificationService);
        eventPublisher.subscribe(DomainEvent.class, listener);
    }

    @Test
    @DisplayName("Evento VisitaSolicitadaEvent genera notificación para el anfitrión")
    void reaccionaAVisitaSolicitada() {
        VisitaSolicitadaEvent evento = new VisitaSolicitadaEvent(
                "VISITA_001",
                "VISITANTE_PEDRO",
                "ANFITRION_MARIA",
                "Entrevista de trabajo"
        );

        eventPublisher.publish(evento);

        List<Notificacion> notificaciones = notificationService.consultarPorDestinatario("ANFITRION_MARIA");
        assertEquals(1, notificaciones.size());
        Notificacion notif = notificaciones.get(0);
        assertEquals(TipoNotificacion.VISITA_SOLICITADA, notif.getTipo());
        assertTrue(notif.getCuerpo().contains("VISITANTE_PEDRO"));
    }

    @Test
    @DisplayName("Evento VisitaAprobadaEvent genera notificación para el visitante")
    void reaccionaAVisitaAprobada() {
        VisitaAprobadaEvent evento = new VisitaAprobadaEvent(
                "VISITA_002",
                "ANFITRION_MARIA",
                "Acceso autorizado con credencial temporal"
        );

        eventPublisher.publish(evento);

        List<Notificacion> notificaciones = notificationService.consultarPorDestinatario("VISITA_002");
        assertEquals(1, notificaciones.size());
        Notificacion notif = notificaciones.get(0);
        assertEquals(TipoNotificacion.VISITA_APROBADA, notif.getTipo());
        assertTrue(notif.getCuerpo().contains("ANFITRION_MARIA"));
    }

    @Test
    @DisplayName("Evento AlertaAccesoDenegadoEvent despacha alerta al monitor de guardia")
    void reaccionaAAlertaAccesoDenegado() {
        AlertaAccesoDenegadoEvent alerta = new AlertaAccesoDenegadoEvent(
                "PERSONA_DESCONOCIDA",
                "PC_TORNIQUETE_01",
                "ZONA_RECEPCION",
                ResultadoAcceso.DENEGADO_SIN_VISITA_APROBADA,
                "Intento de acceso fuera de horario"
        );

        eventPublisher.publish(alerta);

        List<Notificacion> alertas = notificationService.consultarAlertasSeguridad();
        assertEquals(1, alertas.size());
        Notificacion notifAlerta = alertas.get(0);
        assertEquals("CENTRAL_SEGURIDAD", notifAlerta.getDestinatarioId());
        assertEquals(CanalNotificacion.ALERTA_MONITOR_GUARDIA, notifAlerta.getCanal());
        assertEquals(TipoNotificacion.ALERTA_SEGURIDAD_ACCESO_DENEGADO, notifAlerta.getTipo());
    }

    @Test
    @DisplayName("Evento UsuarioBloqueadoEvent despacha alerta crítica a administradores")
    void reaccionaAUsuarioBloqueado() {
        UsuarioBloqueadoEvent bloqueo = new UsuarioBloqueadoEvent(
                "USR_999",
                "hacker_user",
                LocalDateTime.now().plusMinutes(15)
        );

        eventPublisher.publish(bloqueo);

        List<Notificacion> alertas = notificationService.consultarAlertasSeguridad();
        assertEquals(1, alertas.size());
        Notificacion notif = alertas.get(0);
        assertEquals("ADMIN_SEGURIDAD", notif.getDestinatarioId());
        assertEquals(TipoNotificacion.ALERTA_USUARIO_BLOQUEADO, notif.getTipo());
        assertTrue(notif.getCuerpo().contains("hacker_user"));
    }
}
