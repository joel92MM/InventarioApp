package com.inventario.inventarioapp.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductoDTO implements Serializable {
    private String nombre;
    private Double precio;
}