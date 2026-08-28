package com.zonaacme.sica.auth.ports.in;

import com.zonaacme.sica.auth.domain.Rol;
import com.zonaacme.sica.auth.domain.SesionUsuario;
import com.zonaacme.sica.auth.domain.Usuario;
import java.util.Optional;

/**
 * Puerto Primario / de Entrada (Driver Port) para los casos de uso de Autenticación y Autorización RBAC.
 *
 * <p><b>Principios SOLID aplicados:</b></p>
 * <ul>
 *   <li><b>ISP (Interface Segregation Principle):</b> Expone operaciones estrictamente vinculadas al ciclo
 *   de vida de autenticación, verificación de permisos RBAC y gobierno de credenciales.</li>
 *   <li><b>DIP (Dependency Inversion Principle):</b> Los controladores y clientes dependen de esta abstracción.</li>
 * </ul>
 */
public interface AuthUseCase {

    /**
     * Autentica a un usuario mediante sus credenciales de acceso.
     *
     * @param username Nombre de usuario.
     * @param password Contraseña en texto plano.
     * @return {@link SesionUsuario} activa con token generado.
     */
    SesionUsuario autenticar(String username, String password);

    /**
     * Cierra la sesión activa identificada por su token (Logout).
     *
     * @param token Token de sesión.
     */
    void cerrarSesion(String token);

    /**
     * Consulta una sesión activa en el sistema.
     *
     * @param token Token de sesión.
     * @return {@link Optional} con la sesión si existe y es válida.
     */
    Optional<SesionUsuario> obtenerSesion(String token);

    /**
     * Valida si el usuario portador del token posee el permiso RBAC granular requerido.
     * Lanza {@link com.zonaacme.sica.common.exceptions.SecurityAuthorizationException} si no está autorizado.
     *
     * @param token Token de sesión del usuario.
     * @param codigoPermiso Código del permiso requerido (e.g., "ACCESO_CHECKIN").
     * @param operacion Nombre de la operación intentada para fines de auditoría.
     */
    void validarPermiso(String token, String codigoPermiso, String operacion);

    /**
     * Registra un nuevo usuario en el sistema. Requiere permiso administrativo.
     */
    Usuario registrarUsuario(String username, String password, String nombreCompleto, String email, Rol rol, String tokenAdmin);

    /**
     * Permite a un usuario cambiar su contraseña validando la anterior.
     */
    void cambiarPassword(String usuarioId, String passwordActual, String nuevaPassword);

    /**
     * Bloquea manualmente a un usuario por motivos de seguridad administrativa.
     */
    void bloquearUsuario(String usuarioId, int minutos, String motivo, String tokenAdmin);

    /**
     * Desbloquea a un usuario y restablece sus intentos fallidos.
     */
    void desbloquearUsuario(String usuarioId, String tokenAdmin);
}
