package com.inventario.inventarioapp.servicio;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

@Component
public class ConexionJDBC {

    @Value("${spring.datasource.url}")
    private String url;

    public Connection conectar() throws SQLException {
        try {
            Class.forName("org.sqlite.JDBC"); // Asegura que el driver esté cargado
        } catch (ClassNotFoundException e) {
            throw new SQLException("Driver SQLite no encontrado", e);
        }
        Connection conn = DriverManager.getConnection(url);
        conn.setAutoCommit(false);
        return conn;
    }
}