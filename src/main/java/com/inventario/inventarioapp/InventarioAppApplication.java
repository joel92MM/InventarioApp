package com.inventario.inventarioapp;

import com.inventario.inventarioapp.dto.ProductoDTO;
import com.inventario.inventarioapp.entity.Producto;
import com.inventario.inventarioapp.servicio.GestorDatos;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.beans.factory.annotation.*;

import java.util.List;

@SpringBootApplication
public class InventarioAppApplication {

    @Autowired
    private org.springframework.context.ApplicationContext context;

    public static void main(String[] args) {
        SpringApplication.run(InventarioAppApplication.class, args);
    }

    @Bean
    CommandLineRunner ejecutarPruebas(GestorDatos gestor) {
        return args -> {
            System.out.println("--- INICIANDO PRUEBAS ---");

            try {
                // 1. Manejo de archivos convencionales
                gestor.cargarDesdeArchivo("productos.txt");
                System.out.println("1. Archivo TXT cargado en DB via JPA.");

                // 2. Conexión a base de datos relacional (JDBC) - CRUD
                ProductoDTO nuevo = new ProductoDTO("Altavoces", 30.0);
                gestor.insertarProductoJDBC(nuevo);
                System.out.println("2. Producto insertado mediante JDBC manual.");

                gestor.actualizarProductoJDBC("Altavoces", 35.0);
                System.out.println("2.1. Producto actualizado mediante JDBC.");

                // 3. Procesamiento de ficheros XML
                List<Producto> productos = gestor.listarConJPA();
                gestor.guardarEnXML(productos, "productos.xml");
                System.out.println("3. Datos exportados a productos.xml.");

                System.out.println("3.1. Contenido del XML:");
                gestor.mostrarContenidoXML("productos.xml");

                // Si no existe, devolverá cadena vacía.
                String precio = gestor.consultarPrecioXPath("Laptop");
                System.out.println("3.2. Precio encontrado via XPath para 'Laptop': " + precio);

                gestor.transformarAHtml("productos.xml", "src/main/resources/GestorDatos.xsl", "inventario.html");
                System.out.println("4. Archivo inventario.html generado con éxito.");

            } catch (Exception e) {
                System.err.println("Error durante las pruebas: " + e.getMessage());
                e.printStackTrace();
            } finally {
                System.out.println("Cerrando aplicación...");
                SpringApplication.exit(context, () -> 0);
                System.exit(0);
            }
        };
    }
}