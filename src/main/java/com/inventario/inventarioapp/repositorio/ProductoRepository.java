package com.inventario.inventarioapp.repositorio;

import com.inventario.inventarioapp.entity.Producto;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductoRepository extends JpaRepository<Producto, Long> { }
