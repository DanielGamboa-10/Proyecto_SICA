package com.zonaacme.sica.notifications;

import com.zonaacme.sica.notifications.adapters.ConsoleNotificationSenderAdapter;
import com.zonaacme.sica.notifications.adapters.InMemoryNotificationRepositoryAdapter;
import com.zonaacme.sica.notifications.adapters.NotificationService;
import com.zonaacme.sica.notifications.domain.CanalNotificacion;
import com.zonaacme.sica.notifications.domain.Notificacion;
import com.zonaacme.sica.notifications.domain.TipoNotificacion;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class NotificationServiceTest {

    private NotificationService notificationService;
    private InMemoryNotificationRepositoryAdapter repository;
    private ConsoleNotificationSenderAdapter sender;

    @BeforeEach
    void setUp() {
        repository = new InMemoryNotificationRepositoryAdapter();
        sender = new ConsoleNotificationSenderAdapter();
        notificationService = new NotificationService(repository, sender);
    }

    @Test
    @DisplayName("Despachar notificación la persiste y la envía a través del sender")
    void enviarNotificacionPersisteYEnvia() {
        Notificacion notificacion = Notificacion.crear(
                "ANFITRION_01",
                TipoNotificacion.VISITA_SOLICITADA,
                "Visita Pendiente",
                "Nueva solicitud recibida",
                CanalNotificacion.EMAIL
        );

        notificationService.enviarNotificacion(notificacion);

        // Validar persistencia en repositorio
        Optional<Notificacion> guardada = notificationService.consultarPorId(notificacion.getId());
        assertTrue(guardada.isPresent());
        assertEquals("ANFITRION_01", guardada.get().getDestinatarioId());

        // Validar emisión a través del canal
        assertEquals(1, sender.getNotificacionesEnviadas().size());
        assertEquals(notificacion.getId(), sender.getNotificacionesEnviadas().get(0).getId());
    }

    @Test
    @DisplayName("Consultar notificaciones filtradas por destinatario")
    void consultarPorDestinatario() {
        Notificacion n1 = Notificacion.crear("USER_A", TipoNotificacion.VISITA_SOLICITADA, "T1", "C1", CanalNotificacion.EMAIL);
        Notificacion n2 = Notificacion.crear("USER_B", TipoNotificacion.VISITA_APROBADA, "T2", "C2", CanalNotificacion.SMS);
        Notificacion n3 = Notificacion.crear("USER_A", TipoNotificacion.VISITA_APROBADA, "T3", "C3", CanalNotificacion.EMAIL);

        notificationService.enviarNotificacion(n1);
        notificationService.enviarNotificacion(n2);
        notificationService.enviarNotificacion(n3);

        List<Notificacion> paraUserA = notificationService.consultarPorDestinatario("USER_A");
        assertEquals(2, paraUserA.size());
        assertTrue(paraUserA.stream().allMatch(n -> n.getDestinatarioId().equals("USER_A")));
    }

    @Test
    @DisplayName("Consultar exclusivamente alertas de seguridad críticas")
    void consultarAlertasSeguridad() {
        Notificacion n1 = Notificacion.crear("USER_A", TipoNotificacion.VISITA_SOLICITADA, "T1", "C1", CanalNotificacion.EMAIL);
        Notificacion alerta1 = Notificacion.alertaSeguridad("Acceso denegado", "Zona restringida");
        Notificacion alerta2 = Notificacion.crear("ADMIN", TipoNotificacion.ALERTA_USUARIO_BLOQUEADO, "Bloqueo", "Intrusión", CanalNotificacion.ALERTA_MONITOR_GUARDIA);

        notificationService.enviarNotificacion(n1);
        notificationService.enviarNotificacion(alerta1);
        notificationService.enviarNotificacion(alerta2);

        List<Notificacion> alertas = notificationService.consultarAlertasSeguridad();
        assertEquals(2, alertas.size());
        assertTrue(alertas.stream().allMatch(a -> a.getTipo().esCritico()));
    }

    @Test
    @DisplayName("Marcar notificación como leída actualiza el repositorio")
    void marcarComoLeida() {
        Notificacion n1 = Notificacion.crear("USER_A", TipoNotificacion.VISITA_SOLICITADA, "T1", "C1", CanalNotificacion.EMAIL);
        notificationService.enviarNotificacion(n1);

        assertFalse(notificationService.consultarPorId(n1.getId()).orElseThrow().isLeida());

        notificationService.marcarComoLeida(n1.getId());

        assertTrue(notificationService.consultarPorId(n1.getId()).orElseThrow().isLeida());
    }
}
