package com.inventario.inventarioapp.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Entity
@Table(name = "productos")
@Getter
@Setter
public class Producto implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "nombre")
    private String nombre;

    @Column(name = "precio")
    private double precio;

    public Producto() {}

    public Producto(String nombre, double precio) {
        this.nombre = nombre;
        this.precio = precio;
    }
    @Override
    public String toString() {

        return "Producto: " +
                "\n\tid=" + id +
                "\n\tnombre=" + nombre +
                "\n\tprecio=" + precio ;
    }
}