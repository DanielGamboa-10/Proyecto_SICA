package com.zonaacme.sica.core.domain;

/**
 * Estados posibles del ciclo de vida de una solicitud de visita o pase de acceso.
 *
 * <p><b>Principio SOLID aplicado:</b></p>
 * <ul>
 *   <li><b>SRP (Single Responsibility Principle):</b> Modela de forma unívoca la máquina de estados de las visitas.</li>
 * </ul>
 */
public enum EstadoVisita {
    PENDIENTE("Pendiente de Aprobación"),
    APROBADA("Aprobada por el Anfitrión"),
    RECHAZADA("Rechazada por el Anfitrión"),
    EN_CURSO("Visitante en Instalaciones (Check-In Realizado)"),
    COMPLETADA("Visita Finalizada (Check-Out Realizado)"),
    CANCELADA("Cancelada"),
    EXPIRADA("Expirada por no asistencia en la fecha/hora acordada");

    private final String descripcion;

    EstadoVisita(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public boolean permiteIngreso() {
        return this == APROBADA;
    }

    public boolean permiteSalida() {
        return this == EN_CURSO;
    }
}
