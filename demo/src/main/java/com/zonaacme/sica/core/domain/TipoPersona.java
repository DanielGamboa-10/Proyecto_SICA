package com.zonaacme.sica.core.domain;

/**
 * Categoría o tipo de persona en el ecosistema SICA.
 *
 * <p><b>Principio SOLID aplicado:</b></p>
 * <ul>
 *   <li><b>SRP (Single Responsibility Principle):</b> Clasifica los perfiles de individuos que interactúan
 *   con los puntos de acceso físico de la organización.</li>
 * </ul>
 */
public enum TipoPersona {
    VISITANTE("Visitante Externo"),
    EMPLEADO("Empleado de Planta"),
    CONTRATISTA("Contratista Externo"),
    PROVEEDOR("Proveedor de Servicios / Envíos");

    private final String descripcion;

    TipoPersona(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }
}
