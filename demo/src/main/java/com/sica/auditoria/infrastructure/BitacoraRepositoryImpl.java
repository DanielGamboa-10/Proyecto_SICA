package com.sica.auditoria.infrastructure;

import com.sica.auditoria.domain.Bitacora;
import com.sica.auditoria.domain.BitacoraRepository;
import com.sica.shared.infrastructure.config.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Adaptador de Infraestructura.
 * Implementación JDBC para BitacoraRepository.
 */
public class BitacoraRepositoryImpl implements BitacoraRepository {

    private final Connection connection;

    public BitacoraRepositoryImpl() {
        this.connection = DatabaseConnection.getInstance().getConnection();
    }

    @Override
    public boolean save(Bitacora bitacora) {
        String sql = "INSERT INTO bitacora_auditoria (usuario_id, fecha_hora, accion_realizada, tabla_afectada, registro_id_afectado, detalles) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            if (bitacora.getUsuarioId() > 0) {
                stmt.setInt(1, bitacora.getUsuarioId());
            } else {
                stmt.setNull(1, Types.INTEGER); // Puede ser null si es una acción de sistema anónima
            }
            
            stmt.setTimestamp(2, Timestamp.valueOf(bitacora.getFechaHora()));
            stmt.setString(3, bitacora.getAccionRealizada());
            stmt.setString(4, bitacora.getTablaAfectada());
            
            if (bitacora.getRegistroIdAfectado() > 0) {
                stmt.setInt(5, bitacora.getRegistroIdAfectado());
            } else {
                stmt.setNull(5, Types.INTEGER);
            }
            
            stmt.setString(6, bitacora.getDetalles());
            
            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        bitacora.setId(generatedKeys.getLong(1));
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Error al guardar en bitácora: " + e.getMessage());
        }
        return false;
    }

    @Override
    public List<Bitacora> findAll() {
        List<Bitacora> registros = new ArrayList<>();
        String sql = "SELECT * FROM bitacora_auditoria ORDER BY fecha_hora DESC";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
             
            while (rs.next()) {
                registros.add(mapResultSetToBitacora(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error al listar bitácora: " + e.getMessage());
        }
        return registros;
    }

    @Override
    public List<Bitacora> findByUsuario(int usuarioId) {
        List<Bitacora> registros = new ArrayList<>();
        String sql = "SELECT * FROM bitacora_auditoria WHERE usuario_id = ? ORDER BY fecha_hora DESC";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, usuarioId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    registros.add(mapResultSetToBitacora(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al buscar bitácora por usuario: " + e.getMessage());
        }
        return registros;
    }
    
    private Bitacora mapResultSetToBitacora(ResultSet rs) throws SQLException {
        return new Bitacora(
                rs.getLong("id"),
                rs.getInt("usuario_id"),
                rs.getTimestamp("fecha_hora").toLocalDateTime(),
                rs.getString("accion_realizada"),
                rs.getString("tabla_afectada"),
                rs.getInt("registro_id_afectado"),
                rs.getString("detalles")
        );
    }
}
