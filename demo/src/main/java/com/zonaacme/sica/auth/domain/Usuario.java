package com.zonaacme.sica.auth.domain;

import com.zonaacme.sica.common.exceptions.DomainRuleException;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Entidad de dominio que representa a un usuario del sistema SICA.
 *
 * <p><b>Principios de Diseño y SOLID:</b></p>
 * <ul>
 *   <li><b>SRP (Single Responsibility Principle):</b> Encapsula las reglas de negocio sobre el estado del usuario,
 *   política de bloqueo por intentos fallidos de autenticación y verificación de autorizaciones por rol.</li>
 *   <li><b>Encapsulación e Invariantes:</b> Protege su estado interno evitando modificaciones arbitrarias sin
 *   pasar por los métodos de dominio que validan las reglas de negocio.</li>
 * </ul>
 */
public class Usuario {

    private final String id;
    private final String username;
    private String passwordHash;
    private String salt;
    private String nombreCompleto;
    private String email;
    private Rol rol;
    private boolean activo;
    private int intentosFallidos;
    private LocalDateTime bloqueadoHasta;
    private final LocalDateTime fechaCreacion;

    /**
     * Constructor completo para reconstrucción o persistencia.
     */
    public Usuario(String id, String username, String passwordHash, String salt,
                   String nombreCompleto, String email, Rol rol, boolean activo,
                   int intentosFallidos, LocalDateTime bloqueadoHasta, LocalDateTime fechaCreacion) {
        this.id = Objects.requireNonNull(id, "El ID de usuario no puede ser nulo");
        this.username = validarUsername(username);
        this.passwordHash = Objects.requireNonNull(passwordHash, "El hash de contraseña no puede ser nulo");
        this.salt = Objects.requireNonNull(salt, "El salt no puede ser nulo");
        this.nombreCompleto = Objects.requireNonNull(nombreCompleto, "El nombre completo no puede ser nulo").trim();
        this.email = validarEmail(email);
        this.rol = Objects.requireNonNull(rol, "El rol no puede ser nulo");
        this.activo = activo;
        this.intentosFallidos = Math.max(0, intentosFallidos);
        this.bloqueadoHasta = bloqueadoHasta;
        this.fechaCreacion = fechaCreacion != null ? fechaCreacion : LocalDateTime.now();
    }

    /**
     * Método fábrica para crear un nuevo usuario activo con intentos inicializados en 0.
     */
    public static Usuario nuevo(String username, String passwordHash, String salt,
                                String nombreCompleto, String email, Rol rol) {
        return new Usuario(
                UUID.randomUUID().toString(),
                username,
                passwordHash,
                salt,
                nombreCompleto,
                email,
                rol,
                true,
                0,
                null,
                LocalDateTime.now()
        );
    }

    private static String validarUsername(String username) {
        if (username == null || username.trim().length() < 3) {
            throw new DomainRuleException("El nombre de usuario debe tener al menos 3 caracteres.");
        }
        return username.trim().toLowerCase();
    }

    private static String validarEmail(String email) {
        if (email == null || !email.contains("@") || !email.contains(".")) {
            throw new DomainRuleException("El correo electrónico proporcionado no es válido: " + email);
        }
        return email.trim().toLowerCase();
    }

    /**
     * Verifica si la cuenta se encuentra actualmente bloqueada por tiempo o por estado inactivo.
     */
    public boolean estaBloqueado() {
        if (!activo) {
            return true;
        }
        if (bloqueadoHasta != null) {
            if (LocalDateTime.now().isBefore(bloqueadoHasta)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Registra un intento de acceso fallido y evalúa el bloqueo temporal de la cuenta.
     *
     * @param maxIntentos Umbral máximo de intentos permitidos.
     * @param minutosBloqueo Duración en minutos del bloqueo temporal.
     * @return {@code true} si este intento provocó el bloqueo de la cuenta; {@code false} si aún no alcanza el límite.
     */
    public boolean registrarIntentoFallido(int maxIntentos, int minutosBloqueo) {
        this.intentosFallidos++;
        if (this.intentosFallidos >= maxIntentos) {
            this.bloqueadoHasta = LocalDateTime.now().plusMinutes(minutosBloqueo);
            return true;
        }
        return false;
    }

    /**
     * Restablece el contador de intentos fallidos y limpia el bloqueo temporal tras un login exitoso.
     */
    public void reiniciarIntentosFallidos() {
        this.intentosFallidos = 0;
        this.bloqueadoHasta = null;
    }

    /**
     * Actualiza las credenciales del usuario con un nuevo hash y salt.
     */
    public void cambiarPassword(String nuevoHash, String nuevoSalt) {
        this.passwordHash = Objects.requireNonNull(nuevoHash, "El nuevo hash no puede ser nulo");
        this.salt = Objects.requireNonNull(nuevoSalt, "El nuevo salt no puede ser nulo");
        reiniciarIntentosFallidos();
    }

    /**
     * Asigna un nuevo rol de seguridad al usuario.
     */
    public void cambiarRol(Rol nuevoRol) {
        this.rol = Objects.requireNonNull(nuevoRol, "El nuevo rol no puede ser nulo");
    }

    public void activar() {
        this.activo = true;
    }

    public void desactivar() {
        this.activo = false;
    }

    /**
     * Consulta si el usuario tiene un permiso RBAC específico delegado a través de su rol asignado.
     */
    public boolean tienePermiso(String codigoPermiso) {
        if (!activo || estaBloqueado()) {
            return false;
        }
        return rol.tienePermiso(codigoPermiso);
    }

    public String getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public String getSalt() {
        return salt;
    }

    public String getNombreCompleto() {
        return nombreCompleto;
    }

    public String getEmail() {
        return email;
    }

    public Rol getRol() {
        return rol;
    }

    public boolean isActivo() {
        return activo;
    }

    public int getIntentosFallidos() {
        return intentosFallidos;
    }

    public LocalDateTime getBloqueadoHasta() {
        return bloqueadoHasta;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Usuario usuario = (Usuario) o;
        return Objects.equals(id, usuario.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Usuario{" +
                "id='" + id + '\'' +
                ", username='" + username + '\'' +
                ", rol=" + rol +
                ", activo=" + activo +
                ", intentosFallidos=" + intentosFallidos +
                '}';
    }
}
