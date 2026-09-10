package com.zonaacme.sica.core.services;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * ============================================================================
 * PALABRA CLAVE DE BÚSQUEDA EXAMEN: KEY_MODO_EVACUACION
 * FUNCIONALIDAD #11: Botón de Emergencia / Modo Evacuación Global
 * ============================================================================
 * Administra de forma segura y concurrente (Thread-Safe) el estado de emergencia
 * global del complejo empresarial.
 * - Si está ACTIVO: Bloquea todos los ingresos (ENTRADA) y permite la apertura
 *   inmediata de todas las salidas (SALIDA) sin validación restrictiva.
 */
public final class GestorEvacuacionEmergencia {

    private static final GestorEvacuacionEmergencia INSTANCE = new GestorEvacuacionEmergencia();

    // Bandera atómica thread-safe para lectura y escritura concurrente sin bloqueos
    private final AtomicBoolean modoEvacuacionActivo = new AtomicBoolean(false);

    private GestorEvacuacionEmergencia() {}

    public static GestorEvacuacionEmergencia getInstance() {
        return INSTANCE;
    }

    /**
     * KEY_MODO_EVACUACION: Activa el protocolo de evacuación masiva.
     */
    public void activarModoEvacuacion() {
        activarModoEvacuacion("OPERADOR_SICA");
    }

    public void activarModoEvacuacion(String activadoPor) {
        modoEvacuacionActivo.set(true);
        System.out.println("[ALERTA-CRITICA] !!! PROTOCOLO DE EVACUACION GLOBAL ACTIVADO POR: " + activadoPor + " !!!");
    }

    /**
     * KEY_MODO_EVACUACION: Desactiva el protocolo de evacuación y vuelve a operación normal.
     */
    public void desactivarModoEvacuacion() {
        modoEvacuacionActivo.set(false);
        System.out.println("[INFO-SEGURIDAD] Protocolo de evacuación finalizado. Operación normal restablecida.");
    }

    /**
     * KEY_MODO_EVACUACION: Consulta el estado de emergencia concurrente.
     */
    public boolean isModoEvacuacionActivo() {
        return modoEvacuacionActivo.get();
    }
}
