package com.sica.empresas.infrastructure;

import com.sica.empresas.domain.Empresa;
import com.sica.empresas.domain.EmpresaRepository;
import com.sica.shared.infrastructure.config.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Adaptador de Infraestructura.
 * Implementación de EmpresaRepository usando JDBC puro (Patrón Repository).
 */
public class EmpresaRepositoryImpl implements EmpresaRepository {

    private final Connection connection;

    public EmpresaRepositoryImpl() {
        this.connection = DatabaseConnection.getInstance().getConnection();
    }

    @Override
    public boolean save(Empresa empresa) {
        String sql = "INSERT INTO empresas (nombre, contacto_principal) VALUES (?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, empresa.getNombre());
            stmt.setString(2, empresa.getContactoPrincipal());
            
            int affectedRows = stmt.executeUpdate();
            
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        empresa.setId(generatedKeys.getInt(1));
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Error al guardar empresa: " + e.getMessage());
        }
        return false;
    }

    @Override
    public Optional<Empresa> findById(int id) {
        String sql = "SELECT * FROM empresas WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Empresa empresa = new Empresa(
                            rs.getInt("id"),
                            rs.getString("nombre"),
                            rs.getString("contacto_principal")
                    );
                    return Optional.of(empresa);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al buscar empresa por ID: " + e.getMessage());
        }
        return Optional.empty();
    }

    @Override
    public List<Empresa> findAll() {
        List<Empresa> empresas = new ArrayList<>();
        String sql = "SELECT * FROM empresas";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
             
            while (rs.next()) {
                empresas.add(new Empresa(
                        rs.getInt("id"),
                        rs.getString("nombre"),
                        rs.getString("contacto_principal")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Error al listar empresas: " + e.getMessage());
        }
        return empresas;
    }

    @Override
    public boolean update(Empresa empresa) {
        String sql = "UPDATE empresas SET nombre = ?, contacto_principal = ? WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, empresa.getNombre());
            stmt.setString(2, empresa.getContactoPrincipal());
            stmt.setInt(3, empresa.getId());
            
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error al actualizar empresa: " + e.getMessage());
        }
        return false;
    }

    @Override
    public boolean delete(int id) {
        String sql = "DELETE FROM empresas WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error al eliminar empresa: " + e.getMessage());
        }
        return false;
    }
}
