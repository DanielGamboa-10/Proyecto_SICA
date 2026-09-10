package com.zonaacme.sica.concurrency;

import com.zonaacme.sica.audit.adapters.AuditService;
import com.zonaacme.sica.audit.ports.out.AuditRepositoryPort;
import com.zonaacme.sica.concurrency.threads.BitacoraAsyncWorkerThread;
import com.zonaacme.sica.concurrency.threads.MonitorVisitasVencidasThread;
import com.zonaacme.sica.concurrency.threads.SimuladorTorniquetesThread;
import com.zonaacme.sica.core.adapters.ControlAccesoService;
import com.zonaacme.sica.core.ports.out.PersonaRepositoryPort;
import com.zonaacme.sica.core.ports.out.VisitaRepositoryPort;
import com.zonaacme.sica.core.ports.out.ZonaRepositoryPort;
import com.zonaacme.sica.notifications.adapters.NotificationService;

import java.util.ArrayList;
import java.util.List;

/**
 * Gestor Centralizado de Concurrencia y Ciclo de Vida de los Hilos de Usuario SICA.
 */
public class GestorHilosSica {

    private static GestorHilosSica instance;

    private MonitorVisitasVencidasThread monitorVisitasThread;
    private BitacoraAsyncWorkerThread bitacoraAsyncThread;
    private SimuladorTorniquetesThread simuladorTorniquetesThread;

    private GestorHilosSica() {}

    public static synchronized GestorHilosSica getInstance() {
        if (instance == null) {
            instance = new GestorHilosSica();
        }
        return instance;
    }

    public void inicializarHilos(
            VisitaRepositoryPort visitaRepo,
            AuditService auditService,
            AuditRepositoryPort auditRepo,
            NotificationService notificationService
    ) {
        // 1. Iniciar Hilo Monitor de Visitas Vencidas
        if (monitorVisitasThread == null || !monitorVisitasThread.isAlive()) {
            monitorVisitasThread = new MonitorVisitasVencidasThread(visitaRepo, auditService, notificationService);
            monitorVisitasThread.start();
        }

        // 2. Iniciar Hilo Trabajador Asíncrono de Auditoría
        if (bitacoraAsyncThread == null || !bitacoraAsyncThread.isAlive()) {
            bitacoraAsyncThread = new BitacoraAsyncWorkerThread(auditRepo);
            bitacoraAsyncThread.start();
        }
    }

    public void inicializarSimulador(
            ControlAccesoService controlAccesoService,
            PersonaRepositoryPort personaRepo,
            ZonaRepositoryPort zonaRepo,
            String tokenSesion
    ) {
        if (simuladorTorniquetesThread == null || !simuladorTorniquetesThread.isAlive()) {
            simuladorTorniquetesThread = new SimuladorTorniquetesThread(controlAccesoService, personaRepo, zonaRepo, tokenSesion);
        }
    }

    public MonitorVisitasVencidasThread getMonitorVisitasThread() {
        return monitorVisitasThread;
    }

    public BitacoraAsyncWorkerThread getBitacoraAsyncThread() {
        return bitacoraAsyncThread;
    }

    public SimuladorTorniquetesThread getSimuladorTorniquetesThread() {
        return simuladorTorniquetesThread;
    }

    public List<ThreadInfo> obtenerEstadoHilos() {
        List<ThreadInfo> lista = new ArrayList<>();
        if (monitorVisitasThread != null) {
            lista.add(new ThreadInfo(
                    monitorVisitasThread.getName(),
                    monitorVisitasThread.getState().name(),
                    monitorVisitasThread.isAlive(),
                    "Escaneó visitas (Expiradas detectadas: " + monitorVisitasThread.getVisitasExpiradasDetectadas() + ")"
            ));
        }
        if (bitacoraAsyncThread != null) {
            lista.add(new ThreadInfo(
                    bitacoraAsyncThread.getName(),
                    bitacoraAsyncThread.getState().name(),
                    bitacoraAsyncThread.isAlive(),
                    "Consumidor DB (Procesados: " + bitacoraAsyncThread.getTotalEventosProcesados() + ", En cola: " + bitacoraAsyncThread.getEventosEnCola() + ")"
            ));
        }
        if (simuladorTorniquetesThread != null) {
            lista.add(new ThreadInfo(
                    simuladorTorniquetesThread.getName(),
                    simuladorTorniquetesThread.getState().name(),
                    simuladorTorniquetesThread.isEjecutando(),
                    "Simulador de torniquetes (Pasos: " + simuladorTorniquetesThread.getPasosSimulados() + ")"
            ));
        }
        return lista;
    }

    public void detenerTodos() {
        System.out.println("🛑 [Gestor Hilos] Deteniendo todos los hilos de fondo...");
        if (monitorVisitasThread != null) monitorVisitasThread.detener();
        if (bitacoraAsyncThread != null) bitacoraAsyncThread.detener();
        if (simuladorTorniquetesThread != null) simuladorTorniquetesThread.interrupt();
    }

    public static class ThreadInfo {
        public final String nombre;
        public final String estado;
        public final boolean activo;
        public final String detalle;

        public ThreadInfo(String nombre, String estado, boolean activo, String detalle) {
            this.nombre = nombre;
            this.estado = estado;
            this.activo = activo;
            this.detalle = detalle;
        }
    }
}
