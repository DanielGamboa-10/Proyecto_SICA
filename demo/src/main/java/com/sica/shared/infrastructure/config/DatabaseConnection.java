package com.sica.shared.infrastructure.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Patrón de diseño: Singleton.
 * Se asegura de que solo exista una única instancia de la conexión a la base de datos
 * en todo el ciclo de vida de la aplicación.
 */
public class DatabaseConnection {
    private static DatabaseConnection instance;
    private Connection connection;

    private final String URL = "jdbc:mysql://localhost:3307/campus";
    private final String USER = "campus";
    private final String PASSWORD = "campus123"; // Cambiar según configuración local

    // Constructor privado para evitar instanciación externa
    private DatabaseConnection() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            String[] claves = {PASSWORD, "root", "1234", "123456", "admin", "12345wq", "mysql", "Password123*"};
            for (String clave : claves) {
                try {
                    this.connection = DriverManager.getConnection(URL, USER, clave);
                    if (this.connection != null && !this.connection.isClosed()) {
                        System.out.println("✅ [SICA] Conexión establecida exitosamente con MySQL (sica_db)");
                        break;
                    }
                } catch (SQLException ignored) {}
            }
            if (this.connection == null || this.connection.isClosed()) {
                System.err.println("⚠️ [SICA] No se pudo conectar a MySQL con las contraseñas estándar. Revisa la clave en DatabaseConnection.java.");
            }
        } catch (ClassNotFoundException e) {
            System.err.println("Error al cargar driver de MySQL: " + e.getMessage());
        } catch (SQLException e) {
            System.err.println("Error en conexión MySQL: " + e.getMessage());
        }
    }

    // Método para obtener la única instancia de DatabaseConnection
    public static DatabaseConnection getInstance() {
        if (instance == null) {
            synchronized (DatabaseConnection.class) {
                if (instance == null) {
                    instance = new DatabaseConnection();
                }
            }
        }
        return instance;
    }

    // Método para obtener la conexión SQL
    public Connection getConnection() {
        return connection;
    }
}
