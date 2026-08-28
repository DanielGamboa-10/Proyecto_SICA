package com.zonaacme.sica.auth.adapters;

import com.zonaacme.sica.auth.domain.Rol;
import com.zonaacme.sica.auth.domain.SesionUsuario;
import com.zonaacme.sica.auth.domain.Usuario;
import com.zonaacme.sica.auth.events.AccesoDenegadoPorPermisoEvent;
import com.zonaacme.sica.auth.events.LoginExitosoEvent;
import com.zonaacme.sica.auth.events.LoginFallidoEvent;
import com.zonaacme.sica.auth.events.UsuarioBloqueadoEvent;
import com.zonaacme.sica.auth.ports.in.AuthUseCase;
import com.zonaacme.sica.auth.ports.out.UsuarioRepositoryPort;
import com.zonaacme.sica.common.events.DomainEventPublisher;
import com.zonaacme.sica.common.exceptions.DomainRuleException;
import com.zonaacme.sica.common.exceptions.EntityNotFoundException;
import com.zonaacme.sica.common.exceptions.SecurityAuthorizationException;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Servicio de aplicación que implementa el caso de uso {@link AuthUseCase} de SICA.
 *
 * <p><b>Arquitectura Hexagonal & Principios SOLID:</b></p>
 * <ul>
 *   <li><b>SRP (Single Responsibility Principle):</b> Orquesta la autenticación, gobierno de sesiones,
 *   validaciones RBAC y emisión de eventos de dominio hacia la bitácora de auditoría.</li>
 *   <li><b>DIP (Dependency Inversion Principle):</b> Depende de abstracciones de puertos ({@link UsuarioRepositoryPort})
 *   y del bus de eventos ({@link DomainEventPublisher}).</li>
 *   <li><b>Seguridad:</b> Bloqueo progresivo por intentos fallidos y mensajes genéricos para mitigar enumeración de usuarios.</li>
 * </ul>
 */
public class AuthService implements AuthUseCase {

    private static final int DEFAULT_MAX_INTENTOS = 3;
    private static final int DEFAULT_MINUTOS_BLOQUEO = 15;
    private static final int DEFAULT_DURACION_SESION_MINUTOS = 60;

    private final UsuarioRepositoryPort usuarioRepository;
    private final DomainEventPublisher eventPublisher;
    private final Map<String, SesionUsuario> sesionesActivas = new ConcurrentHashMap<>();

    private final int maxIntentos;
    private final int minutosBloqueo;
    private final int duracionSesionMinutos;

    public AuthService(UsuarioRepositoryPort usuarioRepository, DomainEventPublisher eventPublisher) {
        this(usuarioRepository, eventPublisher, DEFAULT_MAX_INTENTOS, DEFAULT_MINUTOS_BLOQUEO, DEFAULT_DURACION_SESION_MINUTOS);
    }

    public AuthService(UsuarioRepositoryPort usuarioRepository, DomainEventPublisher eventPublisher,
                       int maxIntentos, int minutosBloqueo, int duracionSesionMinutos) {
        this.usuarioRepository = Objects.requireNonNull(usuarioRepository, "UsuarioRepositoryPort no puede ser nulo");
        this.eventPublisher = Objects.requireNonNull(eventPublisher, "DomainEventPublisher no puede ser nulo");
        this.maxIntentos = maxIntentos;
        this.minutosBloqueo = minutosBloqueo;
        this.duracionSesionMinutos = duracionSesionMinutos;
    }

    @Override
    public SesionUsuario autenticar(String username, String password) {
        if (username == null || username.isBlank() || password == null || password.isBlank()) {
            eventPublisher.publish(new LoginFallidoEvent(
                    username != null ? username : "ANONIMO",
                    "Credenciales vacías o nulas",
                    0
            ));
            throw new SecurityAuthorizationException("Las credenciales de acceso no pueden estar vacías.");
        }

        Optional<Usuario> usuarioOpt = usuarioRepository.findByUsername(username);
        if (usuarioOpt.isEmpty()) {
            eventPublisher.publish(new LoginFallidoEvent(username, "Usuario no existe", 1));
            throw new SecurityAuthorizationException("Credenciales de acceso inválidas.");
        }

        Usuario usuario = usuarioOpt.get();

        if (usuario.estaBloqueado()) {
            eventPublisher.publish(new LoginFallidoEvent(username, "Cuenta inactiva o temporalmente bloqueada", usuario.getIntentosFallidos()));
            throw new SecurityAuthorizationException("La cuenta se encuentra temporalmente bloqueada por exceso de intentos o inactividad.");
        }

        boolean passwordValida = PasswordHasher.verificar(password, usuario.getSalt(), usuario.getPasswordHash());

        if (!passwordValida) {
            boolean provocoBloqueo = usuario.registrarIntentoFallido(maxIntentos, minutosBloqueo);
            usuarioRepository.save(usuario);

            eventPublisher.publish(new LoginFallidoEvent(username, "Contraseña incorrecta", usuario.getIntentosFallidos()));

            if (provocoBloqueo) {
                eventPublisher.publish(new UsuarioBloqueadoEvent(usuario.getId(), usuario.getUsername(), usuario.getBloqueadoHasta()));
            }

            throw new SecurityAuthorizationException("Credenciales de acceso inválidas.");
        }

        // Login exitoso
        usuario.reiniciarIntentosFallidos();
        usuarioRepository.save(usuario);

        SesionUsuario sesion = SesionUsuario.crear(usuario, duracionSesionMinutos);
        sesionesActivas.put(sesion.getToken(), sesion);

        eventPublisher.publish(new LoginExitosoEvent(usuario.getId(), usuario.getUsername(), usuario.getRol().name()));

        return sesion;
    }

    @Override
    public void cerrarSesion(String token) {
        if (token != null) {
            SesionUsuario sesion = sesionesActivas.remove(token);
            if (sesion != null) {
                sesion.cerrar();
            }
        }
    }

    @Override
    public Optional<SesionUsuario> obtenerSesion(String token) {
        if (token == null) {
            return Optional.empty();
        }
        SesionUsuario sesion = sesionesActivas.get(token);
        if (sesion != null && sesion.esValida()) {
            return Optional.of(sesion);
        } else if (sesion != null && !sesion.esValida()) {
            sesionesActivas.remove(token);
        }
        return Optional.empty();
    }

    @Override
    public void validarPermiso(String token, String codigoPermiso, String operacion) {
        Optional<SesionUsuario> sesionOpt = obtenerSesion(token);
        if (sesionOpt.isEmpty()) {
            throw new SecurityAuthorizationException("Sesión inexistente, expirada o inválida. Requiere iniciar sesión.");
        }

        SesionUsuario sesion = sesionOpt.get();
        if (!sesion.tienePermiso(codigoPermiso)) {
            eventPublisher.publish(new AccesoDenegadoPorPermisoEvent(
                    sesion.getUsuarioId(),
                    sesion.getUsername(),
                    codigoPermiso,
                    operacion
            ));
            throw new SecurityAuthorizationException(
                    sesion.getUsuarioId(),
                    codigoPermiso,
                    String.format("Acceso denegado: El usuario '%s' con rol '%s' no posee el permiso '%s' para ejecutar '%s'.",
                            sesion.getUsername(), sesion.getRol().getNombreLegible(), codigoPermiso, operacion)
            );
        }
    }

    @Override
    public Usuario registrarUsuario(String username, String password, String nombreCompleto,
                                   String email, Rol rol, String tokenAdmin) {
        validarPermiso(tokenAdmin, "USUARIOS_GESTIONAR", "REGISTRAR_USUARIO");

        if (usuarioRepository.existsByUsername(username)) {
            throw new DomainRuleException("Ya existe un usuario registrado con el nombre de usuario: " + username);
        }
        if (usuarioRepository.existsByEmail(email)) {
            throw new DomainRuleException("Ya existe un usuario registrado con el correo: " + email);
        }
        if (password == null || password.length() < 6) {
            throw new DomainRuleException("La contraseña debe tener una longitud mínima de 6 caracteres.");
        }

        String salt = PasswordHasher.generarSalt();
        String hash = PasswordHasher.hashPassword(password, salt);
        Usuario nuevoUsuario = Usuario.nuevo(username, hash, salt, nombreCompleto, email, rol);
        usuarioRepository.save(nuevoUsuario);

        return nuevoUsuario;
    }

    @Override
    public void cambiarPassword(String usuarioId, String passwordActual, String nuevaPassword) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new EntityNotFoundException("Usuario", usuarioId));

        if (!PasswordHasher.verificar(passwordActual, usuario.getSalt(), usuario.getPasswordHash())) {
            throw new SecurityAuthorizationException("La contraseña actual no coincide.");
        }

        if (nuevaPassword == null || nuevaPassword.length() < 6) {
            throw new DomainRuleException("La nueva contraseña debe tener al menos 6 caracteres.");
        }

        String nuevoSalt = PasswordHasher.generarSalt();
        String nuevoHash = PasswordHasher.hashPassword(nuevaPassword, nuevoSalt);
        usuario.cambiarPassword(nuevoHash, nuevoSalt);
        usuarioRepository.save(usuario);
    }

    @Override
    public void bloquearUsuario(String usuarioId, int minutos, String motivo, String tokenAdmin) {
        validarPermiso(tokenAdmin, "USUARIOS_GESTIONAR", "BLOQUEAR_USUARIO");

        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new EntityNotFoundException("Usuario", usuarioId));

        usuario.registrarIntentoFallido(1, minutos);
        usuarioRepository.save(usuario);

        eventPublisher.publish(new UsuarioBloqueadoEvent(usuario.getId(), usuario.getUsername(), usuario.getBloqueadoHasta()));
    }

    @Override
    public void desbloquearUsuario(String usuarioId, String tokenAdmin) {
        validarPermiso(tokenAdmin, "USUARIOS_GESTIONAR", "DESBLOQUEAR_USUARIO");

        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new EntityNotFoundException("Usuario", usuarioId));

        usuario.reiniciarIntentosFallidos();
        usuario.activar();
        usuarioRepository.save(usuario);
    }
}
