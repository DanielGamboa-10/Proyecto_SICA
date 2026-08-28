package com.zonaacme.sica.auth;

import com.zonaacme.sica.audit.adapters.AuditEventListener;
import com.zonaacme.sica.audit.adapters.AuditService;
import com.zonaacme.sica.audit.adapters.InMemoryAuditRepositoryAdapter;
import com.zonaacme.sica.audit.domain.BitacoraAuditoria;
import com.zonaacme.sica.auth.adapters.AuthService;
import com.zonaacme.sica.auth.adapters.InMemoryUsuarioRepositoryAdapter;
import com.zonaacme.sica.auth.domain.Rol;
import com.zonaacme.sica.auth.domain.SesionUsuario;
import com.zonaacme.sica.auth.domain.Usuario;
import com.zonaacme.sica.common.events.DomainEvent;
import com.zonaacme.sica.common.events.DomainEventPublisher;
import com.zonaacme.sica.common.exceptions.SecurityAuthorizationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AuthServiceTest {

    private InMemoryUsuarioRepositoryAdapter usuarioRepository;
    private InMemoryAuditRepositoryAdapter auditRepository;
    private DomainEventPublisher eventPublisher;
    private AuthService authService;

    @BeforeEach
    void setUp() {
        eventPublisher = DomainEventPublisher.getInstance();
        eventPublisher.reset();

        usuarioRepository = new InMemoryUsuarioRepositoryAdapter();
        auditRepository = new InMemoryAuditRepositoryAdapter();
        AuditService auditService = new AuditService(auditRepository);

        // Suscribir listener de auditoría al bus global
        AuditEventListener auditEventListener = new AuditEventListener(auditService);
        eventPublisher.subscribe(DomainEvent.class, auditEventListener);

        authService = new AuthService(usuarioRepository, eventPublisher, 3, 15, 60);
    }

    @Test
    @DisplayName("Debe autenticar exitosamente a un usuario semilla y generar registro de auditoría")
    void debeAutenticarExitosamente() {
        SesionUsuario sesion = authService.autenticar("admin", "Admin123*");

        assertNotNull(sesion);
        assertNotNull(sesion.getToken());
        assertEquals("admin", sesion.getUsername());
        assertEquals(Rol.ADMINISTRADOR, sesion.getRol());
        assertTrue(sesion.esValida());

        // Verificar registro automático en la bitácora de auditoría
        List<BitacoraAuditoria> registros = auditRepository.findAll();
        assertFalse(registros.isEmpty());
        assertTrue(registros.stream().anyMatch(r -> r.getAccion().equals("LOGIN_EXITOSO")));
    }

    @Test
    @DisplayName("Debe rechazar credenciales inválidas y registrar evento de login fallido")
    void debeRechazarPasswordInvalido() {
        assertThrows(SecurityAuthorizationException.class, () ->
                authService.autenticar("admin", "PasswordErroneo*")
        );

        List<BitacoraAuditoria> registros = auditRepository.findAll();
        assertTrue(registros.stream().anyMatch(r -> r.getAccion().equals("LOGIN_FALLIDO")));
    }

    @Test
    @DisplayName("Debe bloquear temporalmente al usuario tras 3 intentos fallidos consecutivos")
    void debeBloquearTrasTresIntentosFallidos() {
        for (int i = 0; i < 3; i++) {
            assertThrows(SecurityAuthorizationException.class, () ->
                    authService.autenticar("guardia1", "WrongPass*")
            );
        }

        Usuario guardia = usuarioRepository.findByUsername("guardia1").orElseThrow();
        assertTrue(guardia.estaBloqueado());

        // El cuarto intento incluso con contraseña correcta debe ser rechazado por cuenta bloqueada
        SecurityAuthorizationException ex = assertThrows(SecurityAuthorizationException.class, () ->
                authService.autenticar("guardia1", "Guardia123*")
        );
        assertTrue(ex.getMessage().contains("bloqueada"));
    }

    @Test
    @DisplayName("Debe validar permisos RBAC granulares correctamente")
    void debeValidarPermisosRBAC() {
        SesionUsuario sesionGuardia = authService.autenticar("guardia1", "Guardia123*");

        // Guardia tiene permiso para Check-In
        assertDoesNotThrow(() ->
                authService.validarPermiso(sesionGuardia.getToken(), "ACCESO_CHECKIN", "REGISTRAR_ENTRADA")
        );

        // Guardia NO tiene permiso para gestionar usuarios del sistema
        assertThrows(SecurityAuthorizationException.class, () ->
                authService.validarPermiso(sesionGuardia.getToken(), "USUARIOS_GESTIONAR", "CREAR_USUARIO")
        );

        // Comprobar que la violación de autorización quedó registrada en la auditoría
        List<BitacoraAuditoria> bitacoras = auditRepository.findAll();
        assertTrue(bitacoras.stream().anyMatch(b -> b.getAccion().equals("ACCESO_DENEGADO_PERMISO")));
    }
}
