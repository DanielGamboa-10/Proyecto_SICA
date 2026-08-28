package com.zonaacme.sica.auth.events;

import com.zonaacme.sica.common.events.DomainEvent;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Evento de dominio emitido cuando un usuario intenta realizar una operación sin poseer el permiso RBAC granular.
 *
 * <p><b>Principio SOLID aplicado:</b></p>
 * <ul>
 *   <li><b>SRP (Single Responsibility Principle):</b> Modela los datos de una violación de autorización RBAC.</li>
 * </ul>
 */
public final class AccesoDenegadoPorPermisoEvent implements DomainEvent {

    private final String usuarioId;
    private final String username;
    private final String permisoRequerido;
    private final String operacionIntentada;
    private final LocalDateTime ocurridoEn;

    public AccesoDenegadoPorPermisoEvent(String usuarioId, String username, String permisoRequerido, String operacionIntentada) {
        this.usuarioId = Objects.requireNonNull(usuarioId);
        this.username = Objects.requireNonNull(username);
        this.permisoRequerido = Objects.requireNonNull(permisoRequerido);
        this.operacionIntentada = operacionIntentada != null ? operacionIntentada : "OPERACION_NO_ESPECIFICADA";
        this.ocurridoEn = LocalDateTime.now();
    }

    @Override
    public String getEntidadId() {
        return usuarioId;
    }

    @Override
    public String getNombreEvento() {
        return "ACCESO_DENEGADO_PERMISO";
    }

    @Override
    public LocalDateTime getOcurridoEn() {
        return ocurridoEn;
    }

    public String getUsername() {
        return username;
    }

    public String getPermisoRequerido() {
        return permisoRequerido;
    }

    public String getOperacionIntentada() {
        return operacionIntentada;
    }

    @Override
    public String toString() {
        return String.format("Acceso denegado al usuario '%s' (ID: %s) para la operación '%s'. Permiso faltante: '%s'.",
                username, usuarioId, operacionIntentada, permisoRequerido);
    }
}
