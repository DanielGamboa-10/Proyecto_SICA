package com.zonaacme.sica.auth.adapters;

import com.sica.shared.infrastructure.config.DatabaseConnection;
import com.zonaacme.sica.auth.domain.Rol;
import com.zonaacme.sica.auth.domain.Usuario;
import com.zonaacme.sica.auth.ports.out.UsuarioRepositoryPort;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Adaptador JDBC para el repositorio de Usuarios.
 * Conecta directamente a la base de datos MySQL (sica_db.usuarios).
 * Mantiene sincronización bidireccional y fallback en memoria.
 */
public class UsuarioJdbcRepositoryAdapter implements UsuarioRepositoryPort {

    private final Map<String, Usuario> memoriaBackup = new ConcurrentHashMap<>();

    public UsuarioJdbcRepositoryAdapter() {
        inicializarSemillasEnMemoria();
    }

    private void inicializarSemillasEnMemoria() {
        crearUsuarioMemoria("admin", "Admin123*", "Administrador General SICA", "admin@zonaacme.com", Rol.ADMINISTRADOR);
        crearUsuarioMemoria("guardia1", "Guardia123*", "Carlos Vigilante (Guarda)", "guardia.porteria1@zonaacme.com", Rol.GUARDIA_SEGURIDAD);
        crearUsuarioMemoria("funcionario1", "Func123*", "Dr. Mauricio Restrepo (Funcionario)", "m.restrepo@quantumdynamics.com", Rol.ANFITRION_EMPLEADO);
        crearUsuarioMemoria("super1", "Super123*", "Capitán Fernando Rojas (Supervisor)", "supervisor.seguridad@zonaacme.com", Rol.AUDITOR);
        crearUsuarioMemoria("anfitrion1", "Anfitrion123*", "Dra. Valentina Duque", "v.duque@biogenacme.com", Rol.ANFITRION_EMPLEADO);
        crearUsuarioMemoria("auditor1", "Auditor123*", "Laura Auditora", "auditor1@zonaacme.com", Rol.AUDITOR);
        crearUsuarioMemoria("recepcion1", "Recepcion123*", "Ana Recepción", "recepcion1@zonaacme.com", Rol.RECEPCIONISTA);
    }

    private void crearUsuarioMemoria(String username, String pass, String nombre, String email, Rol rol) {
        String salt = PasswordHasher.generarSalt();
        String hash = PasswordHasher.hashPassword(pass, salt);
        Usuario u = Usuario.nuevo(username, hash, salt, nombre, email, rol);
        memoriaBackup.put(u.getId(), u);
    }

    private Connection getConn() {
        return DatabaseConnection.getInstance().getConnection();
    }

    private Rol mapRol(int rolId, String rolNombre) {
        if (rolNombre != null) {
            String rn = rolNombre.toLowerCase();
            if (rn.contains("admin") || rn.contains("superusuario")) return Rol.ADMINISTRADOR;
            if (rn.contains("supervisor") || rn.contains("auditor")) return Rol.AUDITOR;
            if (rn.contains("guarda") || rn.contains("guardia")) return Rol.GUARDIA_SEGURIDAD;
            if (rn.contains("funcionario") || rn.contains("empleado") || rn.contains("anfitrion")) return Rol.ANFITRION_EMPLEADO;
            if (rn.contains("recep")) return Rol.RECEPCIONISTA;
        }
        switch (rolId) {
            case 1: return Rol.ADMINISTRADOR;
            case 2: return Rol.AUDITOR;
            case 3: return Rol.GUARDIA_SEGURIDAD;
            case 4: return Rol.ANFITRION_EMPLEADO;
            default: return Rol.ADMINISTRADOR;
        }
    }

    private int mapRolToId(Rol rol) {
        if (rol == null) return 1;
        switch (rol) {
            case ADMINISTRADOR: return 1;
            case AUDITOR: return 2;
            case GUARDIA_SEGURIDAD: return 3;
            case ANFITRION_EMPLEADO: return 4;
            case RECEPCIONISTA: return 3;
            default: return 1;
        }
    }

    private Usuario mapearDesdeResultSet(ResultSet rs) throws SQLException {
        int dbId = rs.getInt("id");
        String nombre = rs.getString("nombre");
        String email = rs.getString("email");
        String password = rs.getString("password");
        int rolId = rs.getInt("rol_id");
        boolean activo = rs.getBoolean("esta_activo");
        Timestamp fc = rs.getTimestamp("fecha_creacion");

        String rolNombre = "";
        try {
            rolNombre = rs.getString("nombre_rol");
        } catch (Exception ignored) {}

        Rol rol = mapRol(rolId, rolNombre);

        // Extraer username del email o de la primera palabra del nombre
        String username = email;
        if (email != null && email.contains("@")) {
            username = email.substring(0, email.indexOf("@"));
        }

        String userId = "USR_" + dbId;
        LocalDateTime fecha = fc != null ? fc.toLocalDateTime() : LocalDateTime.now();

        return new Usuario(
                userId,
                username,
                password,
                "", // Salt vacío para que coincida con DB
                nombre,
                email != null ? email : (username + "@zonaacme.com"),
                rol,
                activo,
                0,
                null,
                fecha
        );
    }

    @Override
    public void save(Usuario usuario) {
        Objects.requireNonNull(usuario, "El usuario no puede ser nulo");
        memoriaBackup.put(usuario.getId(), usuario);

        Connection conn = getConn();
        if (conn != null) {
            try {
                String sql = "INSERT INTO usuarios (nombre, email, password, rol_id, esta_activo) " +
                             "VALUES (?, ?, ?, ?, ?) " +
                             "ON DUPLICATE KEY UPDATE nombre = VALUES(nombre), password = VALUES(password), " +
                             "rol_id = VALUES(rol_id), esta_activo = VALUES(esta_activo)";
                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setString(1, usuario.getNombreCompleto());
                    ps.setString(2, usuario.getEmail());
                    ps.setString(3, usuario.getPasswordHash());
                    ps.setInt(4, mapRolToId(usuario.getRol()));
                    ps.setBoolean(5, usuario.isActivo());
                    ps.executeUpdate();
                }
            } catch (SQLException e) {
                System.err.println("⚠️ [UsuarioJdbcRepo] Error al guardar usuario en MySQL: " + e.getMessage());
            }
        }
    }

    @Override
    public Optional<Usuario> findById(String id) {
        if (id == null) return Optional.empty();

        Connection conn = getConn();
        if (conn != null) {
            try {
                String cleanId = id.startsWith("USR_") ? id.substring(4) : id;
                String sql = "SELECT u.*, r.nombre_rol FROM usuarios u LEFT JOIN roles r ON u.rol_id = r.id WHERE u.id = ?";
                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setInt(1, Integer.parseInt(cleanId));
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            return Optional.of(mapearDesdeResultSet(rs));
                        }
                    }
                }
            } catch (Exception ignored) {}
        }

        return Optional.ofNullable(memoriaBackup.get(id));
    }

    @Override
    public Optional<Usuario> findByUsername(String username) {
        if (username == null || username.isBlank()) return Optional.empty();
        String uLower = username.trim().toLowerCase();

        // 1. Buscar en memoria primero si es alias conocido (ej. admin, guardia1, super1)
        for (Usuario u : memoriaBackup.values()) {
            if (u.getUsername().equalsIgnoreCase(uLower) || u.getEmail().equalsIgnoreCase(uLower)) {
                return Optional.of(u);
            }
        }

        // 2. Consultar directamente a MySQL
        Connection conn = getConn();
        if (conn != null) {
            try {
                String sql = "SELECT u.*, r.nombre_rol FROM usuarios u LEFT JOIN roles r ON u.rol_id = r.id " +
                             "WHERE LOWER(u.email) = ? OR LOWER(u.email) LIKE ? OR LOWER(u.nombre) LIKE ?";
                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setString(1, uLower);
                    ps.setString(2, uLower + "@%");
                    ps.setString(3, "%" + uLower + "%");
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            Usuario u = mapearDesdeResultSet(rs);
                            memoriaBackup.put(u.getId(), u);
                            return Optional.of(u);
                        }
                    }
                }
            } catch (SQLException e) {
                System.err.println("⚠️ [UsuarioJdbcRepo] Error al buscar usuario por username en MySQL: " + e.getMessage());
            }
        }

        return Optional.empty();
    }

    @Override
    public Optional<Usuario> findByEmail(String email) {
        if (email == null || email.isBlank()) return Optional.empty();
        String eLower = email.trim().toLowerCase();

        for (Usuario u : memoriaBackup.values()) {
            if (u.getEmail().equalsIgnoreCase(eLower)) {
                return Optional.of(u);
            }
        }

        Connection conn = getConn();
        if (conn != null) {
            try {
                String sql = "SELECT u.*, r.nombre_rol FROM usuarios u LEFT JOIN roles r ON u.rol_id = r.id WHERE LOWER(u.email) = ?";
                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setString(1, eLower);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            Usuario u = mapearDesdeResultSet(rs);
                            memoriaBackup.put(u.getId(), u);
                            return Optional.of(u);
                        }
                    }
                }
            } catch (SQLException e) {
                System.err.println("⚠️ [UsuarioJdbcRepo] Error al buscar usuario por email en MySQL: " + e.getMessage());
            }
        }

        return Optional.empty();
    }

    @Override
    public List<Usuario> findAll() {
        Map<String, Usuario> resultado = new LinkedHashMap<>(memoriaBackup);

        Connection conn = getConn();
        if (conn != null) {
            try {
                String sql = "SELECT u.*, r.nombre_rol FROM usuarios u LEFT JOIN roles r ON u.rol_id = r.id";
                try (Statement stmt = conn.createStatement();
                     ResultSet rs = stmt.executeQuery(sql)) {
                    while (rs.next()) {
                        Usuario u = mapearDesdeResultSet(rs);
                        resultado.put(u.getId(), u);
                    }
                }
            } catch (SQLException e) {
                System.err.println("⚠️ [UsuarioJdbcRepo] Error al listar usuarios en MySQL: " + e.getMessage());
            }
        }

        return new ArrayList<>(resultado.values());
    }

    @Override
    public boolean existsByUsername(String username) {
        return findByUsername(username).isPresent();
    }

    @Override
    public boolean existsByEmail(String email) {
        return findByEmail(email).isPresent();
    }
}
