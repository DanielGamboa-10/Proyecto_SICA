package com.zonaacme.sica.concurrency.threads;

import com.zonaacme.sica.audit.domain.BitacoraAuditoria;
import com.zonaacme.sica.audit.ports.out.AuditRepositoryPort;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 🧵 HILO DEFINIDO POR EL USUARIO: Trabajador Asíncrono de Auditoría (Worker Thread).
 *
 * <p><b>Propósito Concurrente:</b></p>
 * Implementa el patrón Productor-Consumidor usando una {@link BlockingQueue} concurrente.
 * Los eventos de negocio de la UI se encolan instantáneamente sin bloquear el hilo principal (EDT),
 * y este hilo en segundo plano consume y persiste los registros en la base de datos MySQL de forma asíncrona.
 */
public class BitacoraAsyncWorkerThread extends Thread {

    private final AuditRepositoryPort auditRepo;
    private final BlockingQueue<BitacoraAuditoria> colaEventos = new LinkedBlockingQueue<>(5000);
    private final AtomicBoolean ejecutando = new AtomicBoolean(true);
    private long totalEventosProcesados = 0;

    public BitacoraAsyncWorkerThread(AuditRepositoryPort auditRepo) {
        super("SICA-BitacoraAsyncWorker-Thread");
        this.auditRepo = auditRepo;
        setDaemon(true);
    }

    public void encolarEvento(BitacoraAuditoria evento) {
        if (evento != null) {
            boolean encolado = colaEventos.offer(evento);
            if (!encolado) {
                System.err.println("[HILO-AUDITORIA-WARN] Cola de auditoria llena. Guardando directamente.");
                auditRepo.save(evento);
            }
        }
    }

    @Override
    public void run() {
        System.out.println("[HILO-AUDITORIA] " + getName() + " INICIADO (Consumidor de auditoria activo)...");

        while (ejecutando.get() || !colaEventos.isEmpty()) {
            try {
                BitacoraAuditoria evento = colaEventos.poll(500, TimeUnit.MILLISECONDS);
                if (evento != null) {
                    auditRepo.save(evento);
                    totalEventosProcesados++;
                }
            } catch (InterruptedException e) {
                System.out.println("[HILO-AUDITORIA] " + getName() + " vaciando cola residual antes de terminar...");
                break;
            } catch (Exception e) {
                System.err.println("[HILO-AUDITORIA-ERROR] Error procesando evento en " + getName() + ": " + e.getMessage());
            }
        }

        // Vaciar eventos pendientes
        BitacoraAuditoria residual;
        while ((residual = colaEventos.poll()) != null) {
            try {
                auditRepo.save(residual);
                totalEventosProcesados++;
            } catch (Exception ignored) {}
        }

        System.out.println("[HILO-AUDITORIA] " + getName() + " FINALIZADO. Total procesados: " + totalEventosProcesados);
    }

    public void detener() {
        this.ejecutando.set(false);
        this.interrupt();
    }

    public long getTotalEventosProcesados() {
        return totalEventosProcesados;
    }

    public int getEventosEnCola() {
        return colaEventos.size();
    }

    public boolean isEjecutando() {
        return ejecutando.get() && isAlive();
    }
}
