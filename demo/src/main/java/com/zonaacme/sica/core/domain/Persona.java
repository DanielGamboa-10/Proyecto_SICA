package com.zonaacme.sica.core.domain;

import com.zonaacme.sica.common.exceptions.DomainRuleException;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Entidad de dominio que representa a una persona física (visitante, empleado, contratista, proveedor).
 *
 * <p><b>Principios de Diseño y SOLID:</b></p>
 * <ul>
 *   <li><b>SRP (Single Responsibility Principle):</b> Encapsula la identidad, datos de contacto y pertenencia
 *   organizacional de una persona en el sistema.</li>
 *   <li><b>Invariantes de Dominio:</b> Garantiza consistencia estricta en tipo de documento, número de identidad y datos básicos.</li>
 * </ul>
 */
public class Persona {

    private final String id;
    private final String tipoDocumento;
    private final String numeroDocumento;
    private String nombres;
    private String apellidos;
    private String email;
    private String telefono;
    private String empresa;
    private TipoPersona tipoPersona;
    private boolean activo;
    private final LocalDateTime fechaRegistro;

    public Persona(String id, String tipoDocumento, String numeroDocumento,
                   String nombres, String apellidos, String email, String telefono,
                   String empresa, TipoPersona tipoPersona, boolean activo, LocalDateTime fechaRegistro) {
        this.id = Objects.requireNonNull(id, "El ID no puede ser nulo");
        this.tipoDocumento = validarTexto(tipoDocumento, "Tipo de documento").toUpperCase();
        this.numeroDocumento = validarTexto(numeroDocumento, "Número de documento").trim();
        this.nombres = validarTexto(nombres, "Nombres");
        this.apellidos = validarTexto(apellidos, "Apellidos");
        this.email = email != null ? email.trim().toLowerCase() : "";
        this.telefono = telefono != null ? telefono.trim() : "";
        this.empresa = empresa != null ? empresa.trim() : "PARTICULAR";
        this.tipoPersona = Objects.requireNonNull(tipoPersona, "El tipo de persona no puede ser nulo");
        this.activo = activo;
        this.fechaRegistro = fechaRegistro != null ? fechaRegistro : LocalDateTime.now();
    }

    public static Persona nuevo(String tipoDocumento, String numeroDocumento,
                                String nombres, String apellidos, String email,
                                String telefono, String empresa, TipoPersona tipoPersona) {
        return new Persona(
                UUID.randomUUID().toString(),
                tipoDocumento,
                numeroDocumento,
                nombres,
                apellidos,
                email,
                telefono,
                empresa,
                tipoPersona,
                true,
                LocalDateTime.now()
        );
    }

    private static String validarTexto(String valor, String campo) {
        if (valor == null || valor.trim().isEmpty()) {
            throw new DomainRuleException("El campo '" + campo + "' no puede ser nulo o vacío.");
        }
        return valor.trim();
    }

    public String getNombreCompleto() {
        return nombres + " " + apellidos;
    }

    public void actualizarContacto(String email, String telefono, String empresa) {
        this.email = email != null ? email.trim().toLowerCase() : this.email;
        this.telefono = telefono != null ? telefono.trim() : this.telefono;
        this.empresa = empresa != null ? empresa.trim() : this.empresa;
    }

    public void activar() {
        this.activo = true;
    }

    public void desactivar() {
        this.activo = false;
    }

    public void cambiarTipoPersona(TipoPersona nuevoTipo) {
        this.tipoPersona = Objects.requireNonNull(nuevoTipo, "Tipo de persona no puede ser nulo");
    }

    public String getId() {
        return id;
    }

    public String getTipoDocumento() {
        return tipoDocumento;
    }

    public String getNumeroDocumento() {
        return numeroDocumento;
    }

    public String getNombres() {
        return nombres;
    }

    public String getApellidos() {
        return apellidos;
    }

    public String getEmail() {
        return email;
    }

    public String getTelefono() {
        return telefono;
    }

    public String getEmpresa() {
        return empresa;
    }

    public TipoPersona getTipoPersona() {
        return tipoPersona;
    }

    public boolean isActivo() {
        return activo;
    }

    public LocalDateTime getFechaRegistro() {
        return fechaRegistro;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Persona persona = (Persona) o;
        return Objects.equals(tipoDocumento, persona.tipoDocumento) &&
               Objects.equals(numeroDocumento, persona.numeroDocumento);
    }

    @Override
    public int hashCode() {
        return Objects.hash(tipoDocumento, numeroDocumento);
    }

    @Override
    public String toString() {
        return "Persona{" +
                "id='" + id + '\'' +
                ", documento='" + tipoDocumento + " " + numeroDocumento + '\'' +
                ", nombre='" + getNombreCompleto() + '\'' +
                ", tipo=" + tipoPersona +
                ", empresa='" + empresa + '\'' +
                '}';
    }
}
