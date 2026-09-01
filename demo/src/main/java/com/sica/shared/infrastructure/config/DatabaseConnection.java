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

    private final String URL = "jdbc:mysql://localhost:3306/sica_db";
    private final String USER = "root";
    private final String PASSWORD = ""; // Cambiar según configuración local

    // Constructor privado para evitar instanciación externa
    private DatabaseConnection() {
        try {
            // Registrar el driver
            Class.forName("com.mysql.cj.jdbc.Driver");
            this.connection = DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (ClassNotFoundException | SQLException e) {
            System.err.println("Error al conectar a la base de datos: " + e.getMessage());
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
