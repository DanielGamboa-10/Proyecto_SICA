package com.zonaacme.sica.app;

import com.zonaacme.sica.audit.domain.BitacoraAuditoria;
import com.zonaacme.sica.auth.domain.SesionUsuario;
import com.zonaacme.sica.core.domain.*;
import com.zonaacme.sica.notifications.domain.Notificacion;
import com.zonaacme.sica.notifications.domain.TipoNotificacion;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class SicaEndToEndTest {

    private SicaApplication app;

    @BeforeEach
    void setUp() {
        app = new SicaApplication();
    }

    @Test
    @DisplayName("Flujo End-to-End completo: Autenticación, Visita, Notificaciones, Control de Acceso y Auditoría Forense")
    void flujoCompletoEndToEnd() {
        // 1. Autenticación RBAC
        SesionUsuario adminSession = app.getAuthService().autenticar("admin", "Admin123*");
        assertNotNull(adminSession);
        assertNotNull(adminSession.getToken());

        SesionUsuario guardiaSession = app.getAuthService().autenticar("guardia1", "Guardia123*");
        assertNotNull(guardiaSession);

        // 2. Consulta de entidades base
        Persona empleado = app.getPersonaRepo().findByDocumento("CC", "10102020").orElseThrow();
        Persona visitante = app.getPersonaRepo().findByDocumento("CC", "80809090").orElseThrow();
        Zona recepcion = app.getZonaRepo().findZonaByCodigo("ZONA_RECEPCION").orElseThrow();
        PuntoControl torniquete = app.getZonaRepo().findPuntoControlByCodigo("PC_TORN_01").orElseThrow();
        PuntoControl servidores = app.getZonaRepo().findPuntoControlByCodigo("PC_PUERTA_DC").orElseThrow();

        // 3. Radicar y aprobar solicitud de visita
        LocalDateTime ahora = LocalDateTime.now();
        SolicitudVisita visita = app.getVisitaService().solicitarVisita(
                visitante.getId(),
                empleado.getId(),
                "Revisión de Auditoría Externa",
                ahora.minusMinutes(10),
                ahora.plusHours(3),
                Set.of(recepcion.getId()),
                "Placas ABC-987",
                adminSession.getToken()
        );
        assertNotNull(visita);
        assertEquals(EstadoVisita.PENDIENTE, visita.getEstado());

        // Verificar notificación automática generada para el anfitrión
        List<Notificacion> notifsAnfitrion = app.getNotificationService().consultarPorDestinatario(empleado.getNombreCompleto());
        assertEquals(1, notifsAnfitrion.size());
        assertEquals(TipoNotificacion.VISITA_SOLICITADA, notifsAnfitrion.get(0).getTipo());

        // Aprobar visita
        app.getVisitaService().aprobarVisita(visita.getId(), "Visita formalmente aprobada", adminSession.getToken());
        SolicitudVisita visitaAprobada = app.getVisitaRepo().findById(visita.getId()).orElseThrow();
        assertEquals(EstadoVisita.APROBADA, visitaAprobada.getEstado());

        // Verificar notificación generada para la visita
        List<Notificacion> notifsVisita = app.getNotificationService().consultarPorDestinatario(visita.getId());
        assertEquals(1, notifsVisita.size());
        assertEquals(TipoNotificacion.VISITA_APROBADA, notifsVisita.get(0).getTipo());

        // 4. Intento de acceso en zona no autorizada (Servidores) -> Debe ser denegado y generar alerta
        RegistroAcceso accesoDenegado = app.getControlAccesoService().registrarIngreso(
                visitante.getId(),
                servidores.getId(),
                guardiaSession.getToken()
        );
        assertFalse(accesoDenegado.esExitoso());
        assertFalse(accesoDenegado.getResultado().esPermitido());

        // Verificar alerta generada para el monitor de guardia
        List<Notificacion> alertas = app.getNotificationService().consultarAlertasSeguridad();
        assertFalse(alertas.isEmpty());
        assertTrue(alertas.stream().anyMatch(a -> a.getTipo() == TipoNotificacion.ALERTA_SEGURIDAD_ACCESO_DENEGADO));

        // 5. Check-In en torniquete autorizado (Recepción) -> Permitido
        RegistroAcceso checkIn = app.getControlAccesoService().registrarIngreso(
                visitante.getId(),
                torniquete.getId(),
                guardiaSession.getToken()
        );
        assertTrue(checkIn.esExitoso());
        assertEquals(ResultadoAcceso.PERMITIDO, checkIn.getResultado());
        assertEquals(TipoAcceso.ENTRADA, checkIn.getTipoAcceso());

        // 6. Check-Out en torniquete -> Permitido
        RegistroAcceso checkOut = app.getControlAccesoService().registrarSalida(
                visitante.getId(),
                torniquete.getId(),
                guardiaSession.getToken()
        );
        assertTrue(checkOut.esExitoso());
        assertEquals(TipoAcceso.SALIDA, checkOut.getTipoAcceso());

        // 7. Verificación de Auditoría Inmutable Forense
        List<BitacoraAuditoria> historial = app.getAuditService().consultarHistorialCompleto();
        assertFalse(historial.isEmpty());
        assertTrue(historial.stream().anyMatch(h -> "LOGIN_EXITOSO".equals(h.getAccion())));
        assertTrue(historial.stream().anyMatch(h -> "VISITA_SOLICITADA".equals(h.getAccion())));
        assertTrue(historial.stream().anyMatch(h -> "VISITA_APROBADA".equals(h.getAccion())));
        assertTrue(historial.stream().anyMatch(h -> "ALERTA_ACCESO_DENEGADO".equals(h.getAccion())));
        assertTrue(historial.stream().anyMatch(h -> "ACCESO_REGISTRADO".equals(h.getAccion())));
    }
}
