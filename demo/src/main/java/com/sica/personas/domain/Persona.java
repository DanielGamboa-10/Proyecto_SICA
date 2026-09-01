package com.sica.personas.domain;

/**
 * Entidad de Dominio que representa a una Persona (Trabajador o Invitado) en el sistema.
 */
public class Persona {
    private int id;
    private String nombre;
    private String documentoIdentidad;
    private int empresaId;
    private String tipoPersona; // 'Trabajador' o 'Invitado'
    private int estadoAccesoId; // 1 = Activo, 2 = Con Prohibición
    private String urlFoto;

    public Persona() {
    }

    public Persona(int id, String nombre, String documentoIdentidad, int empresaId, 
                   String tipoPersona, int estadoAccesoId, String urlFoto) {
        this.id = id;
        this.nombre = nombre;
        this.documentoIdentidad = documentoIdentidad;
        this.empresaId = empresaId;
        this.tipoPersona = tipoPersona;
        this.estadoAccesoId = estadoAccesoId;
        this.urlFoto = urlFoto;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getDocumentoIdentidad() { return documentoIdentidad; }
    public void setDocumentoIdentidad(String documentoIdentidad) { this.documentoIdentidad = documentoIdentidad; }

    public int getEmpresaId() { return empresaId; }
    public void setEmpresaId(int empresaId) { this.empresaId = empresaId; }

    public String getTipoPersona() { return tipoPersona; }
    public void setTipoPersona(String tipoPersona) { this.tipoPersona = tipoPersona; }

    public int getEstadoAccesoId() { return estadoAccesoId; }
    public void setEstadoAccesoId(int estadoAccesoId) { this.estadoAccesoId = estadoAccesoId; }

    public String getUrlFoto() { return urlFoto; }
    public void setUrlFoto(String urlFoto) { this.urlFoto = urlFoto; }

    @Override
    public String toString() {
        return "Persona{" +
                "id=" + id +
                ", nombre='" + nombre + '\'' +
                ", documento='" + documentoIdentidad + '\'' +
                ", tipo='" + tipoPersona + '\'' +
                ", estadoAcceso=" + estadoAccesoId +
                '}';
    }
}
