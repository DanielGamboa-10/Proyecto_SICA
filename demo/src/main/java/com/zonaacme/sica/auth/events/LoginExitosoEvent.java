package com.zonaacme.sica.auth.events;

import com.zonaacme.sica.common.events.DomainEvent;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Evento de dominio emitido cuando un usuario se autentica satisfactoriamente en SICA.
 *
 * <p><b>Principio SOLID aplicado:</b></p>
 * <ul>
 *   <li><b>SRP (Single Responsibility Principle):</b> Representa el hecho inmutable de un inicio de sesión exitoso.</li>
 * </ul>
 */
public final class LoginExitosoEvent implements DomainEvent {

    private final String usuarioId;
    private final String username;
    private final String rol;
    private final LocalDateTime ocurridoEn;

    public LoginExitosoEvent(String usuarioId, String username, String rol) {
        this.usuarioId = Objects.requireNonNull(usuarioId);
        this.username = Objects.requireNonNull(username);
        this.rol = Objects.requireNonNull(rol);
        this.ocurridoEn = LocalDateTime.now();
    }

    @Override
    public String getEntidadId() {
        return usuarioId;
    }

    @Override
    public String getNombreEvento() {
        return "LOGIN_EXITOSO";
    }

    @Override
    public LocalDateTime getOcurridoEn() {
        return ocurridoEn;
    }

    public String getUsername() {
        return username;
    }

    public String getRol() {
        return rol;
    }

    @Override
    public String toString() {
        return String.format("Usuario '%s' (ID: %s, Rol: %s) inició sesión exitosamente.", username, usuarioId, rol);
    }
}
