package com.inventario.inventarioapp.excepcionesPersonalizadas;

public class InventarioException extends Exception {
    public InventarioException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
