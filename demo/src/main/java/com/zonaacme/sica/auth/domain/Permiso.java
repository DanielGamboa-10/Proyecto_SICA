package com.zonaacme.sica.auth.domain;

import java.util.Objects;

/**
 * Entidad inmutable que representa un permiso atómico granular en el motor RBAC.
 *
 * <p><b>Principio SOLID aplicado:</b></p>
 * <ul>
 *   <li><b>SRP (Single Responsibility Principle):</b> Modela exclusivamente la definición
 *   y comparación de un permiso atómico dentro del sistema de seguridad.</li>
 *   <li><b>Inmutabilidad:</b> Sus campos son inmutables para garantizar que las autorizaciones
 *   sean predecibles y consistentes en memoria.</li>
 * </ul>
 */
public final class Permiso {

    private final String codigo;
    private final String nombre;
    private final String descripcion;

    public Permiso(String codigo, String nombre, String descripcion) {
        this.codigo = Objects.requireNonNull(codigo, "El código del permiso no puede ser nulo").trim().toLowerCase();
        this.nombre = Objects.requireNonNull(nombre, "El nombre del permiso no puede ser nulo").trim();
        this.descripcion = descripcion != null ? descripcion.trim() : "";
    }

    public Permiso(String codigo, String nombre) {
        this(codigo, nombre, "");
    }

    public String getCodigo() {
        return codigo;
    }

    public String getNombre() {
        return nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Permiso permiso = (Permiso) o;
        return Objects.equals(codigo, permiso.codigo);
    }

    @Override
    public int hashCode() {
        return Objects.hash(codigo);
    }

    @Override
    public String toString() {
        return "Permiso{" +
                "codigo='" + codigo + '\'' +
                ", nombre='" + nombre + '\'' +
                '}';
    }
}
