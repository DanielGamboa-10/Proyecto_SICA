package com.zonaacme.sica.notifications;

import com.zonaacme.sica.notifications.domain.CanalNotificacion;
import com.zonaacme.sica.notifications.domain.Notificacion;
import com.zonaacme.sica.notifications.domain.TipoNotificacion;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class NotificacionTest {

    @Test
    @DisplayName("Creación válida de una notificación con factory method")
    void crearNotificacionValida() {
        Notificacion notificacion = Notificacion.crear(
                "USER_123",
                TipoNotificacion.VISITA_SOLICITADA,
                "Visita Pendiente",
                "El visitante Juan Pérez solicita acceso",
                CanalNotificacion.EMAIL
        );

        assertNotNull(notificacion.getId());
        assertEquals("USER_123", notificacion.getDestinatarioId());
        assertEquals(TipoNotificacion.VISITA_SOLICITADA, notificacion.getTipo());
        assertEquals("Visita Pendiente", notificacion.getAsunto());
        assertEquals("El visitante Juan Pérez solicita acceso", notificacion.getCuerpo());
        assertEquals(CanalNotificacion.EMAIL, notificacion.getCanal());
        assertNotNull(notificacion.getFechaHora());
        assertFalse(notificacion.isLeida());
    }

    @Test
    @DisplayName("Creación rápida de alerta de seguridad para la central")
    void crearAlertaSeguridadDirecta() {
        Notificacion alerta = Notificacion.alertaSeguridad(
                "Acceso Denegado",
                "Intento de ingreso no autorizado en Puerta Principal"
        );

        assertNotNull(alerta.getId());
        assertEquals("CENTRAL_SEGURIDAD", alerta.getDestinatarioId());
        assertEquals(TipoNotificacion.ALERTA_SEGURIDAD_ACCESO_DENEGADO, alerta.getTipo());
        assertTrue(alerta.getTipo().esCritico());
        assertEquals(CanalNotificacion.ALERTA_MONITOR_GUARDIA, alerta.getCanal());
        assertFalse(alerta.isLeida());
    }

    @Test
    @DisplayName("Marcar notificación como leída")
    void marcarComoLeida() {
        Notificacion notificacion = Notificacion.crear(
                "USER_456",
                TipoNotificacion.VISITA_APROBADA,
                "Aprobada",
                "Su visita ha sido confirmada",
                CanalNotificacion.SMS
        );

        assertFalse(notificacion.isLeida());
        notificacion.marcarComoLeida();
        assertTrue(notificacion.isLeida());
    }

    @Test
    @DisplayName("Invariantes: No permite campos nulos en el constructor")
    void validacionInvariantesCamposNulos() {
        assertThrows(NullPointerException.class, () ->
                Notificacion.crear(null, TipoNotificacion.VISITA_SOLICITADA, "Asunto", "Cuerpo", CanalNotificacion.EMAIL));

        assertThrows(NullPointerException.class, () ->
                Notificacion.crear("USER_1", null, "Asunto", "Cuerpo", CanalNotificacion.EMAIL));

        assertThrows(NullPointerException.class, () ->
                Notificacion.crear("USER_1", TipoNotificacion.VISITA_SOLICITADA, null, "Cuerpo", CanalNotificacion.EMAIL));

        assertThrows(NullPointerException.class, () ->
                Notificacion.crear("USER_1", TipoNotificacion.VISITA_SOLICITADA, "Asunto", null, CanalNotificacion.EMAIL));

        assertThrows(NullPointerException.class, () ->
                Notificacion.crear("USER_1", TipoNotificacion.VISITA_SOLICITADA, "Asunto", "Cuerpo", null));
    }
}
