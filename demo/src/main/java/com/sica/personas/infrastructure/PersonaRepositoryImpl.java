package com.sica.personas.infrastructure;

import com.sica.personas.domain.Persona;
import com.sica.personas.domain.PersonaRepository;
import com.sica.shared.infrastructure.config.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Adaptador de Infraestructura.
 * Implementación de PersonaRepository usando JDBC puro.
 */
public class PersonaRepositoryImpl implements PersonaRepository {

    private final Connection connection;

    public PersonaRepositoryImpl() {
        this.connection = DatabaseConnection.getInstance().getConnection();
    }

    @Override
    public boolean save(Persona persona) {
        String sql = "INSERT INTO personas (nombre, documento_identidad, empresa_id, tipo_persona, estado_acceso_id, url_foto) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, persona.getNombre());
            stmt.setString(2, persona.getDocumentoIdentidad());
            
            if (persona.getEmpresaId() > 0) {
                stmt.setInt(3, persona.getEmpresaId());
            } else {
                stmt.setNull(3, Types.INTEGER);
            }
            
            stmt.setString(4, persona.getTipoPersona());
            stmt.setInt(5, persona.getEstadoAccesoId());
            stmt.setString(6, persona.getUrlFoto());
            
            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        persona.setId(generatedKeys.getInt(1));
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Error al guardar persona: " + e.getMessage());
        }
        return false;
    }

    @Override
    public Optional<Persona> findById(int id) {
        String sql = "SELECT * FROM personas WHERE id = ?";
        return findPersonaBy(sql, id, null);
    }

    @Override
    public Optional<Persona> findByDocumento(String documentoIdentidad) {
        String sql = "SELECT * FROM personas WHERE documento_identidad = ?";
        return findPersonaBy(sql, null, documentoIdentidad);
    }
    
    private Optional<Persona> findPersonaBy(String sql, Integer id, String doc) {
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            if (id != null) {
                stmt.setInt(1, id);
            } else {
                stmt.setString(1, doc);
            }
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToPersona(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al buscar persona: " + e.getMessage());
        }
        return Optional.empty();
    }

    @Override
    public List<Persona> findAll() {
        List<Persona> personas = new ArrayList<>();
        String sql = "SELECT * FROM personas";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
             
            while (rs.next()) {
                personas.add(mapResultSetToPersona(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error al listar personas: " + e.getMessage());
        }
        return personas;
    }

    @Override
    public boolean update(Persona persona) {
        String sql = "UPDATE personas SET nombre = ?, empresa_id = ?, tipo_persona = ?, url_foto = ? WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, persona.getNombre());
            if (persona.getEmpresaId() > 0) {
                stmt.setInt(2, persona.getEmpresaId());
            } else {
                stmt.setNull(2, Types.INTEGER);
            }
            stmt.setString(3, persona.getTipoPersona());
            stmt.setString(4, persona.getUrlFoto());
            stmt.setInt(5, persona.getId());
            
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error al actualizar persona: " + e.getMessage());
        }
        return false;
    }

    @Override
    public boolean updateEstadoAcceso(int idPersona, int nuevoEstadoId) {
        String sql = "UPDATE personas SET estado_acceso_id = ? WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, nuevoEstadoId);
            stmt.setInt(2, idPersona);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error al cambiar estado de acceso: " + e.getMessage());
        }
        return false;
    }
    
    private Persona mapResultSetToPersona(ResultSet rs) throws SQLException {
        return new Persona(
                rs.getInt("id"),
                rs.getString("nombre"),
                rs.getString("documento_identidad"),
                rs.getInt("empresa_id"),
                rs.getString("tipo_persona"),
                rs.getInt("estado_acceso_id"),
                rs.getString("url_foto")
        );
    }
}
