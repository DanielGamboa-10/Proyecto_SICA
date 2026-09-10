package com.sica.shared.infrastructure.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Patrón de diseño: Singleton.
 * Se asegura de que solo exista una única instancia de la conexión a la base de datos
 * en todo el ciclo de vida de la aplicación.
 *
 * Configuración Primaria:
 * - Puerto Host: 3307
 * - Base de Datos: campus
 * - Usuario: campus
 * - Contraseña: campus123
 */
public class DatabaseConnection {
    private static DatabaseConnection instance;
    private Connection connection;

    public static final String DEFAULT_URL = "jdbc:mysql://localhost:3307/campus";
    public static final String DEFAULT_USER = "campus";
    public static final String DEFAULT_PASSWORD = "campus123";
    public static final int DEFAULT_PORT = 3307;

    private Connection conectar() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");

            // 1. Verificar si existen Variables de Entorno configuradas
            String envUrl = System.getenv("DB_URL");
            String envUser = System.getenv("DB_USER");
            String envPass = System.getenv("DB_PASSWORD");
            if (envUrl != null && !envUrl.isBlank()) {
                try {
                    Connection conn = DriverManager.getConnection(envUrl, envUser != null ? envUser : DEFAULT_USER, envPass != null ? envPass : DEFAULT_PASSWORD);
                    if (conn != null && !conn.isClosed()) {
                        System.out.println("[SICA-INFO] Conectado via Variable de Entorno: " + envUrl);
                        return conn;
                    }
                } catch (SQLException ignored) {}
            }

            // 2. Lista de configuraciones en orden de prioridad (Puerto 3307 primero)
            String[][] targets = {
                // Configuración Principal: Docker Compose en Puerto 3307 (BD: campus)
                {DEFAULT_URL, DEFAULT_USER, DEFAULT_PASSWORD},
                {"jdbc:mysql://localhost:3307/campus", "root", "root"},
                {"jdbc:mysql://localhost:3307/campus", "root", "campus123"},
                {"jdbc:mysql://localhost:3307/sica_db", "campus", "campus123"},
                {"jdbc:mysql://localhost:3307/sica_db", "root", "root"},

                // Fallback secundario: Puerto 3306 (Local)
                {"jdbc:mysql://localhost:3306/campus", "campus", "campus123"},
                {"jdbc:mysql://localhost:3306/campus", "root", "root"},
                {"jdbc:mysql://localhost:3306/sica_db", "root", "12345wq"},
                {"jdbc:mysql://localhost:3306/sica_db", "root", "root"},
                {"jdbc:mysql://localhost:3306/sica_db", "root", "1234"},
                {"jdbc:mysql://localhost:3306/sica_db", "root", "123456"},
                {"jdbc:mysql://localhost:3306/sica_db", "root", "admin"},
                {"jdbc:mysql://localhost:3306/sica_db", "root", ""},
                {"jdbc:mysql://localhost:3306/sica_db", "campus", "campus123"}
            };

            for (String[] target : targets) {
                try {
                    Connection conn = DriverManager.getConnection(target[0], target[1], target[2]);
                    if (conn != null && !conn.isClosed()) {
                        System.out.println("[SICA-INFO] Conexion establecida exitosamente con MySQL: " + target[0] + " (Usuario: " + target[1] + ")");
                        return conn;
                    }
                } catch (SQLException ignored) {}
            }
        } catch (Exception e) {
            System.err.println("[SICA-WARN] Error al conectar a MySQL: " + e.getMessage());
        }
        return null;
    }

    private DatabaseConnection() {
        this.connection = conectar();
        if (this.connection == null) {
            System.err.println("[SICA-WARN] No se pudo conectar a MySQL en puerto 3307 automaticamente. Asegurate de que el contenedor de Docker este corriendo con: docker compose up -d");
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

    // Método para obtener la conexión SQL (reconecta si fue cerrada)
    public synchronized Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                connection = conectar();
            }
        } catch (SQLException e) {
            connection = conectar();
        }
        return connection;
    }

    public boolean isConnected() {
        try {
            return connection != null && !connection.isClosed();
        } catch (SQLException e) {
            return false;
        }
    }
}
