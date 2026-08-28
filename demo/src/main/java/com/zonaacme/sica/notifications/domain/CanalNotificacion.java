package com.zonaacme.sica.notifications.domain;

/**
 * Canales o medios a través de los cuales se despachan las notificaciones y alertas en SICA.
 *
 * <p><b>Principio SOLID aplicado:</b></p>
 * <ul>
 *   <li><b>SRP (Single Responsibility Principle):</b> Discrimina los medios físicos y digitales de notificación.</li>
 * </ul>
 */
public enum CanalNotificacion {
    EMAIL("Correo Electrónico"),
    SMS("Mensajería de Texto SMS"),
    CONSOLA_INTERNA("Notificación en Consola / Sistema Interno"),
    ALERTA_MONITOR_GUARDIA("Pantalla de Monitoreo de Seguridad");

    private final String descripcion;

    CanalNotificacion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }
}
