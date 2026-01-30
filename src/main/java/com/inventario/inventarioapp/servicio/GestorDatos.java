package com.inventario.inventarioapp.servicio;

import com.inventario.inventarioapp.dto.ProductoDTO;
import com.inventario.inventarioapp.entity.Producto;
import com.inventario.inventarioapp.excepcionesPersonalizadas.InventarioException;
import com.inventario.inventarioapp.repositorio.ProductoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.w3c.dom.*;
import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.xpath.*;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Componente GestorDatos (JavaBean)
 * Gestiona la persistencia en TXT, XML, JDBC y JPA.
 */
@Service
public class GestorDatos {

    @Autowired
    private ProductoRepository jpaRepository;

    @Autowired
    private ConexionJDBC conexionJDBC;

    private final PropertyChangeSupport support = new PropertyChangeSupport(this);

    public void addPropertyChangeListener(PropertyChangeListener pcl) {support.addPropertyChangeListener(pcl);}

    /**
     * Carga datos desde un archivo de texto y los guarda en la BD usando JPA.
     */
    public void cargarDesdeArchivo(String ruta) throws InventarioException {
        File archivo = new File("Datos/" + ruta);
        try (Scanner sc = new Scanner(archivo)) {
            while (sc.hasNextLine()) {
                String[] partes = sc.nextLine().split(",");
                if (partes.length == 2) {
                    Producto p = new Producto();
                    p.setNombre(partes[0]);
                    p.setPrecio(Double.parseDouble(partes[1]));
                    jpaRepository.save(p);
                }
            }
        } catch (FileNotFoundException e) {
            throw new InventarioException("El archivo de texto no existe.", e);
        }
    }

    /**
     * Guarda una lista de productos en un archivo de texto.
     */
    public void guardarEnArchivoTexto(List<ProductoDTO> productos, String ruta) throws InventarioException {
        File carpeta = new File("Datos");
        if (!carpeta.exists()) carpeta.mkdirs();

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(new File(carpeta, ruta)))) {
            for (ProductoDTO p : productos) {
                bw.write(p.getNombre() + "," + p.getPrecio());
                bw.newLine();
            }
        } catch (IOException e) {
            throw new InventarioException("Error al escribir el archivo TXT.", e);
        }
    }

    /**
     * Exporta la lista de productos a un archivo XML.
     */
    public void guardarEnXML(List<Producto> productos, String rutaXml) throws Exception {
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.newDocument();

        Element rootElement = doc.createElement("inventario");
        doc.appendChild(rootElement);

        for (Producto p : productos) {
            Element producto = doc.createElement("producto");
            rootElement.appendChild(producto);

            Element nombre = doc.createElement("nombre");
            nombre.appendChild(doc.createTextNode(p.getNombre()));
            producto.appendChild(nombre);

            Element precio = doc.createElement("precio");
            precio.appendChild(doc.createTextNode(String.valueOf(p.getPrecio())));
            producto.appendChild(precio);
        }

        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        DOMSource source = new DOMSource(doc);
        StreamResult result = new StreamResult(new File(rutaXml));
        transformer.transform(source, result);
    }

    /**
     * Lee un archivo XML y muestra su contenido en la consola (Requisito: DOM/SAX).
     */
    public void mostrarContenidoXML(String rutaXml) throws Exception {
        File xmlFile = new File(rutaXml);
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.parse(xmlFile);
        doc.getDocumentElement().normalize();

        System.out.println("Elemento raíz: " + doc.getDocumentElement().getNodeName());
        NodeList nList = doc.getElementsByTagName("producto");

        for (int i = 0; i < nList.getLength(); i++) {
            Node nNode = nList.item(i);
            if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                Element eElement = (Element) nNode;
                System.out.println("Producto: " + eElement.getElementsByTagName("nombre").item(0).getTextContent());
                System.out.println("Precio: " + eElement.getElementsByTagName("precio").item(0).getTextContent());
                System.out.println("----------------------------");
            }
        }
    }

    /**
     * Transforma un XML a HTML usando XSLT.
     */
    public void transformarAHtml(String xmlPath, String xslPath, String outHtml) throws InventarioException {
        try {
            TransformerFactory factory = TransformerFactory.newInstance();
            Source xslt = new StreamSource(new File(xslPath));
            Transformer transformer = factory.newTransformer(xslt);
            Source xml = new StreamSource(new File(xmlPath));
            transformer.transform(xml, new StreamResult(new File(outHtml)));
        } catch (TransformerException e) {
            throw new InventarioException("Error en transformación XSLT.", e);
        }
    }

    /**
     * Consulta un valor específico en el XML usando XPath.
     */
    public String consultarPrecioXPath(String nombreProducto) throws Exception {
        Document doc = DocumentBuilderFactory.newInstance()
                .newDocumentBuilder().parse(new File("productos.xml"));
        XPath xPath = XPathFactory.newInstance().newXPath();
        String expression = "/inventario/producto[nombre='" + nombreProducto + "']/precio";
        return xPath.compile(expression).evaluate(doc);
    }

    // --- JDBC OPERATIONS (CRUD) ---

    public void insertarProductoJDBC(ProductoDTO dto) throws SQLException {
        String sql = "INSERT INTO productos (nombre, precio) VALUES (?, ?)";
        Connection conn = null;
        try {
            conn = conexionJDBC.conectar();
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, dto.getNombre());
                pstmt.setDouble(2, dto.getPrecio());
                pstmt.executeUpdate();
                conn.commit();

                support.firePropertyChange("productoInsertado", null, dto);
            }
        } catch (SQLException e) {
            if (conn != null) conn.rollback();
            throw e;
        } finally {
            if (conn != null) conn.close();
        }
    }

    public List<ProductoDTO> listarProductosJDBC() throws SQLException {
        List<ProductoDTO> lista = new ArrayList<>();
        String sql = "SELECT nombre, precio FROM productos";
        try (Connection conn = conexionJDBC.conectar();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                lista.add(new ProductoDTO(rs.getString("nombre"), rs.getDouble("precio")));
            }
        }
        return lista;
    }

    public void actualizarProductoJDBC(String nombre, double nuevoPrecio) throws SQLException {
        String sql = "UPDATE productos SET precio = ? WHERE nombre = ?";
        Connection conn = null;
        try {
            conn = conexionJDBC.conectar();
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setDouble(1, nuevoPrecio);
                pstmt.setString(2, nombre);
                int rows = pstmt.executeUpdate();
                if (rows > 0) {
                    conn.commit();
                } else {
                    conn.rollback(); // O simplemente no hacer commit si no se encontró
                }
            }
        } catch (SQLException e) {
            if (conn != null) conn.rollback();
            throw e;
        } finally {
            if (conn != null) conn.close();
        }
    }

    public void eliminarProductoJDBC(String nombre) throws SQLException {
        String sql = "DELETE FROM productos WHERE nombre = ?";
        Connection conn = null;
        try {
            conn = conexionJDBC.conectar();
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, nombre);
                int rows = pstmt.executeUpdate();
                if (rows > 0) {
                    conn.commit();
                } else {
                    conn.rollback();
                }
            }
        } catch (SQLException e) {
            if (conn != null) conn.rollback();
            throw e;
        } finally {
            if (conn != null) conn.close();
        }
    }

    // --- JPA OPERATIONS ---

    public void insertarConJPA(Producto p) {jpaRepository.save(p);}

    public List<Producto> listarConJPA() {return jpaRepository.findAll();}
}