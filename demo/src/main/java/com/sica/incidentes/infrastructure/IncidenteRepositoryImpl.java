package com.sica.incidentes.infrastructure;

import com.sica.incidentes.domain.Incidente;
import com.sica.incidentes.domain.IncidenteRepository;
import com.sica.shared.infrastructure.config.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Adaptador de Infraestructura.
 * Implementación JDBC para IncidenteRepository.
 */
public class IncidenteRepositoryImpl implements IncidenteRepository {

    private final Connection connection;

    public IncidenteRepositoryImpl() {
        this.connection = DatabaseConnection.getInstance().getConnection();
    }

    @Override
    public boolean save(Incidente incidente) {
        String sql = "INSERT INTO incidentes (visita_id, reportado_por_id, fecha, descripcion) VALUES (?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, incidente.getVisitaId());
            stmt.setInt(2, incidente.getReportadoPorId());
            stmt.setTimestamp(3, Timestamp.valueOf(incidente.getFecha()));
            stmt.setString(4, incidente.getDescripcion());
            
            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        incidente.setId(generatedKeys.getInt(1));
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Error al guardar incidente: " + e.getMessage());
        }
        return false;
    }

    @Override
    public Optional<Incidente> findById(int id) {
        String sql = "SELECT * FROM incidentes WHERE id = ?";
        return findIncidenteBy(sql, id);
    }

    @Override
    public List<Incidente> findAll() {
        List<Incidente> incidentes = new ArrayList<>();
        String sql = "SELECT * FROM incidentes";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
             
            while (rs.next()) {
                incidentes.add(mapResultSetToIncidente(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error al listar incidentes: " + e.getMessage());
        }
        return incidentes;
    }

    @Override
    public List<Incidente> findByVisitaId(int visitaId) {
        List<Incidente> incidentes = new ArrayList<>();
        String sql = "SELECT * FROM incidentes WHERE visita_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, visitaId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    incidentes.add(mapResultSetToIncidente(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al buscar incidentes por visita: " + e.getMessage());
        }
        return incidentes;
    }

    private Optional<Incidente> findIncidenteBy(String sql, int parametro) {
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, parametro);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToIncidente(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al buscar incidente: " + e.getMessage());
        }
        return Optional.empty();
    }
    
    private Incidente mapResultSetToIncidente(ResultSet rs) throws SQLException {
        return new Incidente(
                rs.getInt("id"),
                rs.getInt("visita_id"),
                rs.getInt("reportado_por_id"),
                rs.getTimestamp("fecha").toLocalDateTime(),
                rs.getString("descripcion")
        );
    }
}
