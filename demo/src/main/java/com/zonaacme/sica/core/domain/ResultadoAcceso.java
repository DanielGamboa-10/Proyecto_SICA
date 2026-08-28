package com.zonaacme.sica.core.domain;

/**
 * Resultado de la evaluación de las reglas de negocio en una solicitud de acceso físico.
 *
 * <p><b>Principio SOLID aplicado:</b></p>
 * <ul>
 *   <li><b>SRP (Single Responsibility Principle):</b> Categoriza inequívocamente si un intento
 *   de acceso fue concedido o la razón exacta por la cual fue denegado.</li>
 * </ul>
 */
public enum ResultadoAcceso {
    PERMITIDO("Acceso Autorizado y Concedido"),
    DENEGADO_PERSONA_INACTIVA("Denegado: La persona se encuentra inactiva o deshabilitada"),
    DENEGADO_SIN_VISITA_APROBADA("Denegado: No cuenta con una solicitud de visita activa y aprobada"),
    DENEGADO_FUERA_DE_HORARIO("Denegado: Intento de ingreso fuera de la franja horaria autorizada"),
    DENEGADO_ZONA_NO_AUTORIZADA("Denegado: La zona solicitada no está incluida en los permisos de la visita"),
    DENEGADO_PUNTO_CONTROL_INACTIVO("Denegado: El torniquete o punto de control se encuentra fuera de servicio"),
    DENEGADO_AFORO_MAXIMO("Denegado: La zona ha alcanzado su capacidad o aforo máximo permitido");

    private final String mensaje;

    ResultadoAcceso(String mensaje) {
        this.mensaje = mensaje;
    }

    public String getMensaje() {
        return mensaje;
    }

    public boolean esPermitido() {
        return this == PERMITIDO;
    }
}
