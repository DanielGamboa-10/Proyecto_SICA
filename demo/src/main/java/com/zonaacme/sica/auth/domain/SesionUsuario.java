package com.zonaacme.sica.auth.domain;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Representa una sesión autenticada activa en el contexto de seguridad de SICA.
 *
 * <p><b>Principios de Diseño y SOLID:</b></p>
 * <ul>
 *   <li><b>SRP (Single Responsibility Principle):</b> Modela exclusivamente la vigencia,
 *   token y facultades de autorización de una sesión de usuario en memoria.</li>
 *   <li><b>Seguridad:</b> Garantiza la no expiración de sesión y verificación directa de permisos RBAC.</li>
 * </ul>
 */
public class SesionUsuario {

    private final String token;
    private final String usuarioId;
    private final String username;
    private final String nombreCompleto;
    private final Rol rol;
    private final LocalDateTime fechaInicio;
    private LocalDateTime fechaExpiracion;
    private boolean activa;

    public SesionUsuario(String token, String usuarioId, String username,
                         String nombreCompleto, Rol rol, LocalDateTime fechaInicio,
                         LocalDateTime fechaExpiracion, boolean activa) {
        this.token = Objects.requireNonNull(token, "El token de sesión no puede ser nulo");
        this.usuarioId = Objects.requireNonNull(usuarioId, "El ID de usuario no puede ser nulo");
        this.username = Objects.requireNonNull(username, "El username no puede ser nulo");
        this.nombreCompleto = Objects.requireNonNull(nombreCompleto, "El nombre completo no puede ser nulo");
        this.rol = Objects.requireNonNull(rol, "El rol no puede ser nulo");
        this.fechaInicio = Objects.requireNonNull(fechaInicio, "La fecha de inicio no puede ser nula");
        this.fechaExpiracion = Objects.requireNonNull(fechaExpiracion, "La fecha de expiración no puede ser nula");
        this.activa = activa;
    }

    /**
     * Crea una nueva sesión activa con duración configurada en minutos.
     */
    public static SesionUsuario crear(Usuario usuario, int duracionMinutos) {
        LocalDateTime ahora = LocalDateTime.now();
        return new SesionUsuario(
                UUID.randomUUID().toString(),
                usuario.getId(),
                usuario.getUsername(),
                usuario.getNombreCompleto(),
                usuario.getRol(),
                ahora,
                ahora.plusMinutes(duracionMinutos),
                true
        );
    }

    /**
     * Determina si la sesión se encuentra activa y vigente en el tiempo.
     */
    public boolean esValida() {
        return activa && LocalDateTime.now().isBefore(fechaExpiracion);
    }

    /**
     * Invalida de forma explícita la sesión (Logout).
     */
    public void cerrar() {
        this.activa = false;
    }

    /**
     * Extiende la vigencia de la sesión activa en un número de minutos.
     */
    public void renovar(int minutosAdicionales) {
        if (esValida()) {
            this.fechaExpiracion = LocalDateTime.now().plusMinutes(minutosAdicionales);
        }
    }

    /**
     * Comprueba si el usuario autenticado en esta sesión posee un permiso atómico.
     */
    public boolean tienePermiso(String codigoPermiso) {
        if (!esValida()) {
            return false;
        }
        return rol.tienePermiso(codigoPermiso);
    }

    public String getToken() {
        return token;
    }

    public String getUsuarioId() {
        return usuarioId;
    }

    public String getUsername() {
        return username;
    }

    public String getNombreCompleto() {
        return nombreCompleto;
    }

    public Rol getRol() {
        return rol;
    }

    public LocalDateTime getFechaInicio() {
        return fechaInicio;
    }

    public LocalDateTime getFechaExpiracion() {
        return fechaExpiracion;
    }

    public boolean isActiva() {
        return activa;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SesionUsuario that = (SesionUsuario) o;
        return Objects.equals(token, that.token);
    }

    @Override
    public int hashCode() {
        return Objects.hash(token);
    }

    @Override
    public String toString() {
        return "SesionUsuario{" +
                "token='" + token + '\'' +
                ", username='" + username + '\'' +
                ", rol=" + rol +
                ", activa=" + activa +
                ", valida=" + esValida() +
                '}';
    }
}
