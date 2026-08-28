package com.zonaacme.sica.auth;

import com.zonaacme.sica.auth.domain.Rol;
import com.zonaacme.sica.auth.domain.Usuario;
import com.zonaacme.sica.common.exceptions.DomainRuleException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UsuarioTest {

    @Test
    @DisplayName("Debe crear un usuario válido con intentos en cero y estado activo")
    void debeCrearUsuarioValido() {
        Usuario usuario = Usuario.nuevo("usuario1", "hash123", "salt123", "Juan Perez", "juan@zonaacme.com", Rol.GUARDIA_SEGURIDAD);

        assertNotNull(usuario.getId());
        assertEquals("usuario1", usuario.getUsername());
        assertTrue(usuario.isActivo());
        assertEquals(0, usuario.getIntentosFallidos());
        assertFalse(usuario.estaBloqueado());
        assertEquals(Rol.GUARDIA_SEGURIDAD, usuario.getRol());
    }

    @Test
    @DisplayName("Debe rechazar nombres de usuario demasiado cortos o correos inválidos")
    void debeValidarReglasDeInvariante() {
        assertThrows(DomainRuleException.class, () ->
                Usuario.nuevo("ab", "hash", "salt", "Nombre", "correo@valido.com", Rol.ADMINISTRADOR)
        );

        assertThrows(DomainRuleException.class, () ->
                Usuario.nuevo("usuarioValido", "hash", "salt", "Nombre", "correoInvalidoSinArroba", Rol.ADMINISTRADOR)
        );
    }

    @Test
    @DisplayName("Debe bloquear al usuario al alcanzar el límite máximo de intentos fallidos")
    void debeBloquearPorIntentosFallidos() {
        Usuario usuario = Usuario.nuevo("guardia_test", "hash", "salt", "Guardia", "g@zonaacme.com", Rol.GUARDIA_SEGURIDAD);

        assertFalse(usuario.registrarIntentoFallido(3, 10));
        assertEquals(1, usuario.getIntentosFallidos());
        assertFalse(usuario.estaBloqueado());

        assertFalse(usuario.registrarIntentoFallido(3, 10));
        assertEquals(2, usuario.getIntentosFallidos());
        assertFalse(usuario.estaBloqueado());

        // Tercer intento alcanza el límite
        assertTrue(usuario.registrarIntentoFallido(3, 10));
        assertEquals(3, usuario.getIntentosFallidos());
        assertTrue(usuario.estaBloqueado());
        assertNotNull(usuario.getBloqueadoHasta());
    }

    @Test
    @DisplayName("Debe verificar permisos granulares según el rol del usuario")
    void debeVerificarPermisosPorRol() {
        Usuario admin = Usuario.nuevo("admin_test", "hash", "salt", "Admin", "admin@acme.com", Rol.ADMINISTRADOR);
        Usuario guardia = Usuario.nuevo("guardia_test", "hash", "salt", "Guardia", "guardia@acme.com", Rol.GUARDIA_SEGURIDAD);

        assertTrue(admin.tienePermiso("USUARIOS_GESTIONAR"));
        assertTrue(admin.tienePermiso("ACCESO_CHECKIN"));

        assertFalse(guardia.tienePermiso("USUARIOS_GESTIONAR"));
        assertTrue(guardia.tienePermiso("ACCESO_CHECKIN"));
    }
}
