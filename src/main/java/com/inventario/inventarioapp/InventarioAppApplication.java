package com.inventario.inventarioapp;

import com.inventario.inventarioapp.dto.ProductoDTO;
import com.inventario.inventarioapp.servicio.GestorDatos;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class InventarioAppApplication {

    public static void main(String[] args) {
        SpringApplication.run(InventarioAppApplication.class, args);
    }

    @Bean
    CommandLineRunner ejecutarPruebas(GestorDatos gestor) {
        return args -> {
            System.out.println("--- INICIANDO PRUEBAS DE EXAMEN ---");

            try {
                // 1. Prueba de Archivos Convencionales (Lectura de TXT)
                // Asegúrate de que existe Datos/productos.txt
                gestor.cargarDesdeArchivo("productos.txt");
                System.out.println("1. Archivo TXT cargado en DB via JPA.");

                // 2. Prueba de JDBC Manual y Transacciones
                ProductoDTO nuevo = new ProductoDTO("Altavoces", 30.0);
                gestor.insertarProductoJDBC(nuevo);
                System.out.println("2. Producto insertado mediante JDBC manual.");

                // 3. Prueba de XPath
                String precio = gestor.consultarPrecioXPath("Laptop");
                System.out.println("3. Precio encontrado via XPath para 'Laptop': " + precio);

                // 4. Prueba de XSLT (Generar HTML)
                // Usará el productos.xml de la raíz y el GestorDatos.xsl de resources
                gestor.transformarAHtml("productos.xml", "src/main/resources/GestorDatos.xsl", "inventario.html");
                System.out.println("4. Archivo inventario.html generado con éxito.");

            } catch (Exception e) {
                System.err.println("Error durante las pruebas: " + e.getMessage());
            }
        };
    }
}
