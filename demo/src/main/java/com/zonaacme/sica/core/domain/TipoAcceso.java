package com.zonaacme.sica.core.domain;

/**
 * Dirección o sentido del movimiento de paso físico en el punto de control.
 *
 * <p><b>Principio SOLID aplicado:</b></p>
 * <ul>
 *   <li><b>SRP (Single Responsibility Principle):</b> Discrimina el sentido direccional de las transacciones de acceso.</li>
 * </ul>
 */
public enum TipoAcceso {
    ENTRADA("Ingreso al recinto / Check-In"),
    SALIDA("Egreso del recinto / Check-Out");

    private final String descripcion;

    TipoAcceso(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }
}
