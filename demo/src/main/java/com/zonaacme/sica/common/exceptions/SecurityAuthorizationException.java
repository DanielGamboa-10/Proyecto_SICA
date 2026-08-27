package com.zonaacme.sica.common.exceptions;

/**
 * Excepción lanzada cuando un usuario autenticado intenta ejecutar una operación
 * para la cual su rol no cuenta con el permiso atómico granular requerido en el motor RBAC.
 *
 * <p><b>Principio SOLID aplicado:</b></p>
 * <ul>
 *   <li><b>SRP (Single Responsibility Principle):</b> Especializada exclusivamente en comunicar
 *   violaciones de seguridad y autorización granular en la capa de seguridad y aplicación.</li>
 * </ul>
 */
public class SecurityAuthorizationException extends RuntimeException {

    private final String usuarioId;
    private final String permisoRequerido;

    public SecurityAuthorizationException(String message) {
        super(message);
        this.usuarioId = null;
        this.permisoRequerido = null;
    }

    public SecurityAuthorizationException(String usuarioId, String permisoRequerido, String message) {
        super(message);
        this.usuarioId = usuarioId;
        this.permisoRequerido = permisoRequerido;
    }

    public String getUsuarioId() {
        return usuarioId;
    }

    public String getPermisoRequerido() {
        return permisoRequerido;
    }
}
