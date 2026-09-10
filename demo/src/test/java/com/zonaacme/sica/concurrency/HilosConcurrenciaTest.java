package com.zonaacme.sica.concurrency;

import com.zonaacme.sica.audit.adapters.AuditService;
import com.zonaacme.sica.audit.adapters.InMemoryAuditRepositoryAdapter;
import com.zonaacme.sica.audit.domain.BitacoraAuditoria;
import com.zonaacme.sica.concurrency.threads.BitacoraAsyncWorkerThread;
import com.zonaacme.sica.concurrency.threads.MonitorVisitasVencidasThread;
import com.zonaacme.sica.core.adapters.InMemoryPersonaRepositoryAdapter;
import com.zonaacme.sica.core.adapters.InMemoryVisitaRepositoryAdapter;
import com.zonaacme.sica.core.adapters.InMemoryZonaRepositoryAdapter;
import com.zonaacme.sica.core.domain.EstadoVisita;
import com.zonaacme.sica.core.domain.Persona;
import com.zonaacme.sica.core.domain.SolicitudVisita;
import com.zonaacme.sica.notifications.adapters.ConsoleNotificationSenderAdapter;
import com.zonaacme.sica.notifications.adapters.InMemoryNotificationRepositoryAdapter;
import com.zonaacme.sica.notifications.adapters.NotificationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

class HilosConcurrenciaTest {

    @Test
    @DisplayName("🧵 Hilo Usuario 1: MonitorVisitasVencidasThread detecta y cierra visitas vencidas en segundo plano")
    void testMonitorVisitasVencidasThread() throws InterruptedException {
        InMemoryPersonaRepositoryAdapter personaRepo = new InMemoryPersonaRepositoryAdapter();
        InMemoryZonaRepositoryAdapter zonaRepo = new InMemoryZonaRepositoryAdapter();
        InMemoryVisitaRepositoryAdapter visitaRepo = new InMemoryVisitaRepositoryAdapter();
        InMemoryAuditRepositoryAdapter auditRepo = new InMemoryAuditRepositoryAdapter();
        AuditService auditService = new AuditService(auditRepo);
        InMemoryNotificationRepositoryAdapter notifRepo = new InMemoryNotificationRepositoryAdapter();
        NotificationService notifService = new NotificationService(notifRepo, new ConsoleNotificationSenderAdapter());

        Persona p1 = personaRepo.findAll().get(0);
        Persona p2 = personaRepo.findAll().get(1);

        // Crear una visita que ya expiró en el pasado
        SolicitudVisita visitaVencida = new SolicitudVisita(
                "VIS_TEST_VENCIDA",
                p1.getId(),
                p2.getId(),
                "Visita de prueba vencida",
                LocalDateTime.now().minusHours(3),
                LocalDateTime.now().minusHours(1),
                Collections.emptySet(),
                EstadoVisita.EN_CURSO,
                null,
                "Aprobada",
                LocalDateTime.now().minusHours(4),
                LocalDateTime.now().minusHours(3),
                null
        );
        visitaRepo.save(visitaVencida);

        // Iniciar hilo con intervalo corto de 500ms
        MonitorVisitasVencidasThread hiloMonitor = new MonitorVisitasVencidasThread(visitaRepo, auditService, notifService, 300);
        hiloMonitor.start();

        assertTrue(hiloMonitor.isAlive());

        // Esperar a que el hilo ejecute su ciclo
        Thread.sleep(700);
        hiloMonitor.detener();
        hiloMonitor.join(1000);

        // Verificar que la visita vencida fue cerrada automáticamente
        SolicitudVisita resultado = visitaRepo.findById("VIS_TEST_VENCIDA").orElseThrow();
        assertEquals(EstadoVisita.COMPLETADA, resultado.getEstado());
        assertNotNull(resultado.getFechaCheckOut());
        assertTrue(hiloMonitor.getVisitasExpiradasDetectadas() >= 1);
    }

    @Test
    @DisplayName("🧵 Hilo Usuario 2: BitacoraAsyncWorkerThread consume y persiste eventos asíncronamente")
    void testBitacoraAsyncWorkerThread() throws InterruptedException {
        InMemoryAuditRepositoryAdapter auditRepo = new InMemoryAuditRepositoryAdapter();
        BitacoraAsyncWorkerThread worker = new BitacoraAsyncWorkerThread(auditRepo);
        worker.start();

        assertTrue(worker.isAlive());

        BitacoraAuditoria e1 = BitacoraAuditoria.crear("admin", "TEST_CONCURRENTE_1", "visitas", "Prueba hilo 1", "TEST");
        BitacoraAuditoria e2 = BitacoraAuditoria.crear("guardia1", "TEST_CONCURRENTE_2", "torniquetes", "Prueba hilo 2", "TEST");

        worker.encolarEvento(e1);
        worker.encolarEvento(e2);

        Thread.sleep(400);
        worker.detener();
        worker.join(1000);

        assertTrue(worker.getTotalEventosProcesados() >= 2);
    }
}
