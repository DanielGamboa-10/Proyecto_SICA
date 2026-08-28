package com.zonaacme.sica.auth.events;

import com.zonaacme.sica.common.events.DomainEvent;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Evento de dominio emitido ante un intento fallido de autenticación.
 *
 * <p><b>Principio SOLID aplicado:</b></p>
 * <ul>
 *   <li><b>SRP (Single Responsibility Principle):</b> Modela los datos inmutables de un intento no autorizado.</li>
 * </ul>
 */
public final class LoginFallidoEvent implements DomainEvent {

    private final String username;
    private final String motivo;
    private final int intentosAcumulados;
    private final LocalDateTime ocurridoEn;

    public LoginFallidoEvent(String username, String motivo, int intentosAcumulados) {
        this.username = Objects.requireNonNull(username);
        this.motivo = Objects.requireNonNull(motivo);
        this.intentosAcumulados = intentosAcumulados;
        this.ocurridoEn = LocalDateTime.now();
    }

    @Override
    public String getEntidadId() {
        return username;
    }

    @Override
    public String getNombreEvento() {
        return "LOGIN_FALLIDO";
    }

    @Override
    public LocalDateTime getOcurridoEn() {
        return ocurridoEn;
    }

    public String getUsername() {
        return username;
    }

    public String getMotivo() {
        return motivo;
    }

    public int getIntentosAcumulados() {
        return intentosAcumulados;
    }

    @Override
    public String toString() {
        return String.format("Intento de login fallido para usuario '%s'. Motivo: %s (Intentos: %d)",
                username, motivo, intentosAcumulados);
    }
}
