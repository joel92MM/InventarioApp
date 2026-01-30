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
        Connection conn = DriverManager.getConnection(url);
        conn.setAutoCommit(false);
        return conn;
    }
}