package com.zonaacme.sica.core.adapters;

import com.sica.shared.infrastructure.config.DatabaseConnection;
import com.zonaacme.sica.core.domain.Persona;
import com.zonaacme.sica.core.domain.TipoPersona;
import com.zonaacme.sica.core.ports.out.PersonaRepositoryPort;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Adaptador JDBC para el repositorio de Personas.
 * Conecta directamente a la base de datos MySQL (sica_db.personas).
 */
public class PersonaJdbcRepositoryAdapter implements PersonaRepositoryPort {

    private final Map<String, Persona> memoriaPorId = new ConcurrentHashMap<>();
    private final Map<String, String> idPorDoc = new ConcurrentHashMap<>();

    public PersonaJdbcRepositoryAdapter() {
        inicializarSemillaMemoria();
        cargarDesdeMySQL();
    }

    private void inicializarSemillaMemoria() {
        Persona p1 = Persona.nuevo("CC", "10102020", "Roberto", "Gómez (Ciberseguridad)", "rgomez@acmedefense.com", "3001234567", "Acme CyberDefense Labs", TipoPersona.EMPLEADO);
        Persona p2 = Persona.nuevo("CC", "10203040", "Valentina", "Duque (Científica Senior)", "vduque@biogenacme.com", "3109876543", "BioGen Innovations S.A.", TipoPersona.EMPLEADO);
        Persona p3 = Persona.nuevo("CC", "10304050", "Mauricio", "Restrepo (Arquitecto Cloud)", "mrestrepo@quantumdynamics.com", "3156781234", "Quantum Dynamics & Robotics", TipoPersona.EMPLEADO);
        Persona p4 = Persona.nuevo("CC", "10405060", "Sofía", "Carvajal (Líder DevOps)", "scarvajal@apexcloud.io", "3204567890", "Apex Cloud Systems Corp", TipoPersona.EMPLEADO);
        Persona p5 = Persona.nuevo("CC", "80809090", "Mario Alberto", "Visitante (Auditor ISO 27001)", "mario.auditor@certivalid.com", "3118901234", "Acme CyberDefense Labs", TipoPersona.VISITANTE);
        Persona p6 = Persona.nuevo("CC", "70708080", "Elena", "Torres (Redes Cisco)", "elena.redes@telecomexpert.com", "3187654321", "Apex Cloud Systems Corp", TipoPersona.CONTRATISTA);
        Persona p7 = Persona.nuevo("CC", "60607070", "Julian", "Arango (Consultor Genética)", "jarango@biotechconsulting.org", "3012349876", "BioGen Innovations S.A.", TipoPersona.VISITANTE);
        Persona p8 = Persona.nuevo("CC", "99998888", "Persona Restringida", "(Sancionado)", "restringido@correo.com", "3000000000", "Acme CyberDefense Labs", TipoPersona.VISITANTE);
        p8.desactivar();

        guardarEnMemoria(p1);
        guardarEnMemoria(p2);
        guardarEnMemoria(p3);
        guardarEnMemoria(p4);
        guardarEnMemoria(p5);
        guardarEnMemoria(p6);
        guardarEnMemoria(p7);
        guardarEnMemoria(p8);
    }

    private void guardarEnMemoria(Persona p) {
        memoriaPorId.put(p.getId(), p);
        idPorDoc.put(generarClave(p.getTipoDocumento(), p.getNumeroDocumento()), p.getId());
    }

    private String generarClave(String tipo, String num) {
        return (tipo != null ? tipo.trim().toUpperCase() : "CC") + "_" + (num != null ? num.trim() : "");
    }

    private Connection getConn() {
        return DatabaseConnection.getInstance().getConnection();
    }

    private Persona mapearDesdeResultSet(ResultSet rs) throws SQLException {
        int dbId = rs.getInt("id");
        String doc = rs.getString("documento_identidad");
        String tipoDoc = rs.getString("tipo_documento");
        String nombreCompleto = rs.getString("nombre");
        String email = rs.getString("email");
        String telefono = rs.getString("telefono");
        String empresa = rs.getString("empresa_nombre");
        String tipoStr = rs.getString("tipo_persona");
        int estadoAcceso = rs.getInt("estado_acceso_id");
        Timestamp fr = rs.getTimestamp("fecha_registro");

        String nombres = nombreCompleto != null ? nombreCompleto : "Sin Nombre";
        String apellidos = "Registrado";
        if (nombreCompleto != null && nombreCompleto.contains(" ")) {
            int idx = nombreCompleto.indexOf(" ");
            nombres = nombreCompleto.substring(0, idx);
            apellidos = nombreCompleto.substring(idx + 1);
        }

        TipoPersona tipo = TipoPersona.VISITANTE;
        if (tipoStr != null) {
            if (tipoStr.equalsIgnoreCase("Trabajador") || tipoStr.equalsIgnoreCase("Empleado")) {
                tipo = TipoPersona.EMPLEADO;
            } else if (tipoStr.equalsIgnoreCase("Contratista")) {
                tipo = TipoPersona.CONTRATISTA;
            } else if (tipoStr.equalsIgnoreCase("Proveedor")) {
                tipo = TipoPersona.PROVEEDOR;
            }
        }

        String personaId = "PER_" + dbId;
        // Preservar UUID si ya existe en memoria con el mismo documento
        String clave = generarClave(tipoDoc, doc);
        if (idPorDoc.containsKey(clave)) {
            personaId = idPorDoc.get(clave);
        }

        boolean activo = (estadoAcceso != 2);
        LocalDateTime fecha = fr != null ? fr.toLocalDateTime() : LocalDateTime.now();

        return new Persona(
                personaId,
                tipoDoc != null ? tipoDoc : "CC",
                doc != null ? doc : "00000000",
                nombres,
                apellidos,
                email != null ? email : (doc + "@zonaacme.com"),
                telefono != null ? telefono : "3001234567",
                empresa != null ? empresa : "Zona Acme Corp",
                tipo,
                activo,
                fecha
        );
    }

    private void cargarDesdeMySQL() {
        Connection conn = getConn();
        if (conn != null) {
            try {
                String sql = "SELECT p.*, e.nombre as empresa_nombre FROM personas p " +
                             "LEFT JOIN empresas e ON p.empresa_id = e.id";
                try (Statement stmt = conn.createStatement();
                     ResultSet rs = stmt.executeQuery(sql)) {
                    while (rs.next()) {
                        Persona p = mapearDesdeResultSet(rs);
                        guardarEnMemoria(p);
                    }
                }
            } catch (SQLException e) {
                System.err.println("⚠️ [PersonaJdbcRepo] Error al cargar personas de MySQL: " + e.getMessage());
            }
        }
    }

    @Override
    public void save(Persona persona) {
        Objects.requireNonNull(persona, "La persona no puede ser nula");
        guardarEnMemoria(persona);

        Connection conn = getConn();
        if (conn != null) {
            try {
                int estadoId = persona.isActivo() ? 1 : 2;
                String tipoStr = "Trabajador";
                if (persona.getTipoPersona() == TipoPersona.VISITANTE) tipoStr = "Invitado";
                else if (persona.getTipoPersona() == TipoPersona.CONTRATISTA) tipoStr = "Contratista";
                else if (persona.getTipoPersona() == TipoPersona.PROVEEDOR) tipoStr = "Proveedor";

                String sql = "INSERT INTO personas (documento_identidad, tipo_documento, nombre, email, telefono, tipo_persona, estado_acceso_id) " +
                             "VALUES (?, ?, ?, ?, ?, ?, ?) " +
                             "ON DUPLICATE KEY UPDATE nombre = VALUES(nombre), email = VALUES(email), " +
                             "telefono = VALUES(telefono), tipo_persona = VALUES(tipo_persona), estado_acceso_id = VALUES(estado_acceso_id)";
                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setString(1, persona.getNumeroDocumento());
                    ps.setString(2, persona.getTipoDocumento());
                    ps.setString(3, persona.getNombreCompleto());
                    ps.setString(4, persona.getEmail());
                    ps.setString(5, persona.getTelefono());
                    ps.setString(6, tipoStr);
                    ps.setInt(7, estadoId);
                    ps.executeUpdate();
                }
            } catch (SQLException e) {
                System.err.println("⚠️ [PersonaJdbcRepo] Error al guardar persona en MySQL: " + e.getMessage());
            }
        }
    }

    @Override
    public Optional<Persona> findById(String id) {
        if (id == null) return Optional.empty();

        Connection conn = getConn();
        if (conn != null && id.startsWith("PER_")) {
            try {
                String cleanId = id.substring(4);
                String sql = "SELECT p.*, e.nombre as empresa_nombre FROM personas p " +
                             "LEFT JOIN empresas e ON p.empresa_id = e.id WHERE p.id = ?";
                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setInt(1, Integer.parseInt(cleanId));
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            Persona p = mapearDesdeResultSet(rs);
                            guardarEnMemoria(p);
                            return Optional.of(p);
                        }
                    }
                }
            } catch (Exception ignored) {}
        }

        return Optional.ofNullable(memoriaPorId.get(id));
    }

    @Override
    public Optional<Persona> findByDocumento(String tipoDocumento, String numeroDocumento) {
        if (numeroDocumento == null) return Optional.empty();
        String clave = generarClave(tipoDocumento, numeroDocumento);

        Connection conn = getConn();
        if (conn != null) {
            try {
                String sql = "SELECT p.*, e.nombre as empresa_nombre FROM personas p " +
                             "LEFT JOIN empresas e ON p.empresa_id = e.id " +
                             "WHERE p.documento_identidad = ? AND (p.tipo_documento = ? OR ? IS NULL)";
                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setString(1, numeroDocumento.trim());
                    ps.setString(2, tipoDocumento != null ? tipoDocumento.trim().toUpperCase() : "CC");
                    ps.setString(3, tipoDocumento);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            Persona p = mapearDesdeResultSet(rs);
                            guardarEnMemoria(p);
                            return Optional.of(p);
                        }
                    }
                }
            } catch (SQLException e) {
                System.err.println("⚠️ [PersonaJdbcRepo] Error al buscar documento en MySQL: " + e.getMessage());
            }
        }

        String id = idPorDoc.get(clave);
        if (id != null) {
            return Optional.ofNullable(memoriaPorId.get(id));
        }
        return Optional.empty();
    }

    @Override
    public List<Persona> findAll() {
        Map<String, Persona> mapa = new LinkedHashMap<>(memoriaPorId);

        Connection conn = getConn();
        if (conn != null) {
            try {
                String sql = "SELECT p.*, e.nombre as empresa_nombre FROM personas p " +
                             "LEFT JOIN empresas e ON p.empresa_id = e.id";
                try (Statement stmt = conn.createStatement();
                     ResultSet rs = stmt.executeQuery(sql)) {
                    while (rs.next()) {
                        Persona p = mapearDesdeResultSet(rs);
                        mapa.put(p.getId(), p);
                    }
                }
            } catch (SQLException e) {
                System.err.println("⚠️ [PersonaJdbcRepo] Error al listar personas en MySQL: " + e.getMessage());
            }
        }

        return new ArrayList<>(mapa.values());
    }

    @Override
    public boolean existsByDocumento(String tipoDocumento, String numeroDocumento) {
        return findByDocumento(tipoDocumento, numeroDocumento).isPresent();
    }
}
