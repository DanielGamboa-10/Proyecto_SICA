package com.zonaacme.sica.concurrency.threads;

import com.zonaacme.sica.audit.adapters.AuditService;
import com.zonaacme.sica.audit.domain.BitacoraAuditoria;
import com.zonaacme.sica.core.domain.EstadoVisita;
import com.zonaacme.sica.core.domain.SolicitudVisita;
import com.zonaacme.sica.core.ports.out.VisitaRepositoryPort;
import com.zonaacme.sica.notifications.adapters.NotificationService;
import com.zonaacme.sica.notifications.domain.Notificacion;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 🧵 HILO DEFINIDO POR EL USUARIO: Monitor de Visitas Vencidas.
 *
 * <p><b>Propósito Concurrente:</b></p>
 * Se ejecuta en un hilo de fondo dedicado (Daemon) de manera periódica (cada 5 segundos)
 * para identificar visitas cuya fecha límite ya expiró y regularizar su estado automáticamente,
 * garantizando la seguridad perimetral de Zona Acme sin intervención humana manual.
 */
public class MonitorVisitasVencidasThread extends Thread {

    private final VisitaRepositoryPort visitaRepo;
    private final AuditService auditService;
    private final NotificationService notificationService;
    private final AtomicBoolean ejecutando = new AtomicBoolean(true);
    private final long intervaloMs;

    private int visitasExpiradasDetectadas = 0;
    private LocalDateTime ultimoEscaneo;

    public MonitorVisitasVencidasThread(
            VisitaRepositoryPort visitaRepo,
            AuditService auditService,
            NotificationService notificationService
    ) {
        this(visitaRepo, auditService, notificationService, 5000);
    }

    public MonitorVisitasVencidasThread(
            VisitaRepositoryPort visitaRepo,
            AuditService auditService,
            NotificationService notificationService,
            long intervaloMs
    ) {
        super("SICA-MonitorVisitasVencidas-Thread");
        this.visitaRepo = visitaRepo;
        this.auditService = auditService;
        this.notificationService = notificationService;
        this.intervaloMs = intervaloMs;
        setDaemon(true);
    }

    @Override
    public void run() {
        System.out.println("[HILO-MONITOR] " + getName() + " INICIADO (Escaneando cada " + (intervaloMs / 1000) + "s)...");

        while (ejecutando.get() && !isInterrupted()) {
            try {
                escanearVisitasExpiradas();
                Thread.sleep(intervaloMs);
            } catch (InterruptedException e) {
                System.out.println("[HILO-MONITOR] " + getName() + " detenido de forma limpia.");
                break;
            } catch (Exception e) {
                System.err.println("[HILO-MONITOR-ERROR] Error en ciclo de " + getName() + ": " + e.getMessage());
            }
        }

        System.out.println("[HILO-MONITOR] " + getName() + " FINALIZADO.");
    }

    private void escanearVisitasExpiradas() {
        this.ultimoEscaneo = LocalDateTime.now();
        List<SolicitudVisita> visitas = visitaRepo.findAll();

        for (SolicitudVisita v : visitas) {
            if (v.getEstado() == EstadoVisita.EN_CURSO && v.getFechaHoraFin().isBefore(ultimoEscaneo)) {
                // Regularizar salida olvidada
                try {
                    v.registrarSalida();
                    visitaRepo.save(v);
                    visitasExpiradasDetectadas++;

                    String detalle = String.format("Visita %s cerrada automaticamente por horario limite excedido (%s)",
                            v.getId(), v.getFechaHoraFin());

                    auditService.registrarEvento(BitacoraAuditoria.crear(
                            "SISTEMA_HILOS", "CIERRE_AUTOMATICO_VISITA", "visitas", detalle, "THREAD_MONITOR"
                    ));

                    notificationService.enviarNotificacion(Notificacion.alertaSeguridad(
                            "ALERTA VISITA VENCIDA", detalle
                    ));

                    System.out.println("[HILO-MONITOR] " + detalle);
                } catch (Exception ignored) {}
            } else if (v.getEstado() == EstadoVisita.PENDIENTE && v.getFechaHoraFin().isBefore(ultimoEscaneo)) {
                try {
                    v.cancelar("Expirada automáticamente por tiempo límite transcurrido");
                    visitaRepo.save(v);
                    visitasExpiradasDetectadas++;
                } catch (Exception ignored) {}
            }
        }
    }

    public void detener() {
        this.ejecutando.set(false);
        this.interrupt();
    }

    public int getVisitasExpiradasDetectadas() {
        return visitasExpiradasDetectadas;
    }

    public LocalDateTime getUltimoEscaneo() {
        return ultimoEscaneo;
    }

    public boolean isEjecutando() {
        return ejecutando.get() && isAlive();
    }
}
