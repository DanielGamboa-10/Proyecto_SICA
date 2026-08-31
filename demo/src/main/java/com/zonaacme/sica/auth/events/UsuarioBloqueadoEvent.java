package com.zonaacme.sica.auth.events;

import com.zonaacme.sica.common.events.DomainEvent;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Evento de dominio emitido cuando un usuario es bloqueado temporalmente por superar intentos fallidos.
 *
 * <p><b>Principio SOLID aplicado:</b></p>
 * <ul>
 *   <li><b>SRP (Single Responsibility Principle):</b> Modela exclusivamente el hecho de bloqueo de seguridad.</li>
 * </ul>
 */
public final class UsuarioBloqueadoEvent implements DomainEvent {

    private final String usuarioId;
    private final String username;
    private final LocalDateTime bloqueadoHasta;
    private final LocalDateTime ocurridoEn;

    public UsuarioBloqueadoEvent(String usuarioId, String username, LocalDateTime bloqueadoHasta) {
        this.usuarioId = Objects.requireNonNull(usuarioId);
        this.username = Objects.requireNonNull(username);
        this.bloqueadoHasta = Objects.requireNonNull(bloqueadoHasta);
        this.ocurridoEn = LocalDateTime.now();
    }

    @Override
    public String getEntidadId() {
        return usuarioId;
    }

    @Override
    public String getNombreEvento() {
        return "USUARIO_BLOQUEADO";
    }

    @Override
    public LocalDateTime getOcurridoEn() {
        return ocurridoEn;
    }

    public String getUsuarioId() {
        return usuarioId;
    }

    public String getUsername() {
        return username;
    }

    public LocalDateTime getBloqueadoHasta() {
        return bloqueadoHasta;
    }

    @Override
    public String toString() {
        return String.format("Usuario '%s' (ID: %s) ha sido bloqueado temporalmente hasta %s por exceso de intentos fallidos.",
                username, usuarioId, bloqueadoHasta);
    }
}
