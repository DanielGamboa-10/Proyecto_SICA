package com.zonaacme.sica.core.domain;

import java.util.Objects;
import java.util.UUID;

/**
 * Entidad que representa un punto físico de control o terminal de paso (torniquete, puerta peatonal, barrera vehicular).
 *
 * <p><b>Principio SOLID aplicado:</b></p>
 * <ul>
 *   <li><b>SRP (Single Responsibility Principle):</b> Modela los dispositivos y accesos físicos asociados a una {@link Zona}.</li>
 * </ul>
 */
public class PuntoControl {

    public enum TipoPunto {
        PEATONAL,
        VEHICULAR,
        TORNIQUETE,
        PUERTA_AUTOMATICA
    }

    private final String id;
    private final String codigo;
    private String nombre;
    private final String zonaId;
    private TipoPunto tipo;
    private boolean activo;

    public PuntoControl(String id, String codigo, String nombre, String zonaId, TipoPunto tipo, boolean activo) {
        this.id = Objects.requireNonNull(id, "ID no puede ser nulo");
        this.codigo = Objects.requireNonNull(codigo, "Código no puede ser nulo").trim().toUpperCase();
        this.nombre = Objects.requireNonNull(nombre, "Nombre no puede ser nulo").trim();
        this.zonaId = Objects.requireNonNull(zonaId, "Zona ID no puede ser nulo");
        this.tipo = Objects.requireNonNull(tipo, "Tipo no puede ser nulo");
        this.activo = activo;
    }

    public static PuntoControl nuevo(String codigo, String nombre, String zonaId, TipoPunto tipo) {
        return new PuntoControl(
                UUID.randomUUID().toString(),
                codigo,
                nombre,
                zonaId,
                tipo,
                true
        );
    }

    public void activar() {
        this.activo = true;
    }

    public void desactivar() {
        this.activo = false;
    }

    public String getId() {
        return id;
    }

    public String getCodigo() {
        return codigo;
    }

    public String getNombre() {
        return nombre;
    }

    public String getZonaId() {
        return zonaId;
    }

    public TipoPunto getTipo() {
        return tipo;
    }

    public boolean isActivo() {
        return activo;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PuntoControl that = (PuntoControl) o;
        return Objects.equals(codigo, that.codigo);
    }

    @Override
    public int hashCode() {
        return Objects.hash(codigo);
    }

    @Override
    public String toString() {
        return "PuntoControl{" +
                "codigo='" + codigo + '\'' +
                ", nombre='" + nombre + '\'' +
                ", tipo=" + tipo +
                ", zonaId='" + zonaId + '\'' +
                '}';
    }
}
