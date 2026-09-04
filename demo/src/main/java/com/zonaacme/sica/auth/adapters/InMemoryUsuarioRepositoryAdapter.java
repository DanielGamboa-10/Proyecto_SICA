package com.zonaacme.sica.auth.adapters;

import com.zonaacme.sica.auth.domain.Rol;
import com.zonaacme.sica.auth.domain.Usuario;
import com.zonaacme.sica.auth.ports.out.UsuarioRepositoryPort;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Adaptador secundario que implementa {@link UsuarioRepositoryPort} en memoria.
 *
 * <p><b>Arquitectura Hexagonal & Principios SOLID:</b></p>
 * <ul>
 *   <li><b>DIP (Dependency Inversion Principle):</b> Implementa el contrato del puerto sin atar la lógica de negocio.</li>
 *   <li><b>LSP (Liskov Substitution Principle):</b> Puede ser sustituido directamente por un adaptador de base de datos SQL o NoSQL.</li>
 *   <li><b>Concurrencia:</b> Utiliza {@link ConcurrentHashMap} para garantizar operaciones concurrentes seguras.</li>
 * </ul>
 */
public class InMemoryUsuarioRepositoryAdapter implements UsuarioRepositoryPort {

    private final Map<String, Usuario> usuariosPorId = new ConcurrentHashMap<>();
    private final Map<String, String> idPorUsername = new ConcurrentHashMap<>();
    private final Map<String, String> idPorEmail = new ConcurrentHashMap<>();

    public InMemoryUsuarioRepositoryAdapter() {
        // Inicializar usuarios semilla para pruebas y demostración
        inicializarUsuariosSemilla();
    }

    private void inicializarUsuariosSemilla() {
        crearUsuarioSemilla("admin", "Admin123*", "Administrador General", "admin@zonaacme.com", Rol.ADMINISTRADOR);
        crearUsuarioSemilla("guardia1", "Guardia123*", "Carlos Vigilante (Guarda)", "guardia1@zonaacme.com", Rol.GUARDIA_SEGURIDAD);
        crearUsuarioSemilla("funcionario1", "Func123*", "Dr. Mauricio Restrepo (Funcionario)", "funcionario1@zonaacme.com", Rol.ANFITRION_EMPLEADO);
        crearUsuarioSemilla("super1", "Super123*", "Capitán Fernando Rojas (Supervisor)", "super1@zonaacme.com", Rol.AUDITOR);
        crearUsuarioSemilla("recepcion1", "Recepcion123*", "Ana Recepción", "recepcion1@zonaacme.com", Rol.RECEPCIONISTA);
        crearUsuarioSemilla("anfitrion1", "Anfitrion123*", "Roberto Empleado", "anfitrion1@zonaacme.com", Rol.ANFITRION_EMPLEADO);
        crearUsuarioSemilla("auditor1", "Auditor123*", "Laura Auditora", "auditor1@zonaacme.com", Rol.AUDITOR);
    }

    private void crearUsuarioSemilla(String username, String password, String nombre, String email, Rol rol) {
        String salt = PasswordHasher.generarSalt();
        String hash = PasswordHasher.hashPassword(password, salt);
        Usuario usuario = Usuario.nuevo(username, hash, salt, nombre, email, rol);
        save(usuario);
    }

    @Override
    public void save(Usuario usuario) {
        Objects.requireNonNull(usuario, "El usuario a guardar no puede ser nulo");
        usuariosPorId.put(usuario.getId(), usuario);
        idPorUsername.put(usuario.getUsername().toLowerCase(), usuario.getId());
        idPorEmail.put(usuario.getEmail().toLowerCase(), usuario.getId());
    }

    @Override
    public Optional<Usuario> findById(String id) {
        if (id == null) return Optional.empty();
        return Optional.ofNullable(usuariosPorId.get(id));
    }

    @Override
    public Optional<Usuario> findByUsername(String username) {
        if (username == null) return Optional.empty();
        String id = idPorUsername.get(username.trim().toLowerCase());
        if (id == null) return Optional.empty();
        return Optional.ofNullable(usuariosPorId.get(id));
    }

    @Override
    public Optional<Usuario> findByEmail(String email) {
        if (email == null) return Optional.empty();
        String id = idPorEmail.get(email.trim().toLowerCase());
        if (id == null) return Optional.empty();
        return Optional.ofNullable(usuariosPorId.get(id));
    }

    @Override
    public List<Usuario> findAll() {
        return Collections.unmodifiableList(new ArrayList<>(usuariosPorId.values()));
    }

    @Override
    public boolean existsByUsername(String username) {
        if (username == null) return false;
        return idPorUsername.containsKey(username.trim().toLowerCase());
    }

    @Override
    public boolean existsByEmail(String email) {
        if (email == null) return false;
        return idPorEmail.containsKey(email.trim().toLowerCase());
    }

    /**
     * Limpia la memoria (útil para pruebas unitarias).
     */
    public void reset() {
        usuariosPorId.clear();
        idPorUsername.clear();
        idPorEmail.clear();
    }
}
