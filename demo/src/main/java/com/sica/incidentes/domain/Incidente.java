package com.sica.incidentes.domain;

import java.time.LocalDateTime;

/**
 * Entidad de Dominio que representa un Incidente de Seguridad.
 */
public class Incidente {
    private int id;
    private int visitaId;
    private int reportadoPorId;
    private LocalDateTime fecha;
    private String descripcion;

    public Incidente() {
    }

    public Incidente(int id, int visitaId, int reportadoPorId, LocalDateTime fecha, String descripcion) {
        this.id = id;
        this.visitaId = visitaId;
        this.reportadoPorId = reportadoPorId;
        this.fecha = fecha;
        this.descripcion = descripcion;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getVisitaId() { return visitaId; }
    public void setVisitaId(int visitaId) { this.visitaId = visitaId; }

    public int getReportadoPorId() { return reportadoPorId; }
    public void setReportadoPorId(int reportadoPorId) { this.reportadoPorId = reportadoPorId; }

    public LocalDateTime getFecha() { return fecha; }
    public void setFecha(LocalDateTime fecha) { this.fecha = fecha; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    @Override
    public String toString() {
        return "Incidente{" +
                "id=" + id +
                ", visitaId=" + visitaId +
                ", fecha=" + fecha +
                ", descripcion='" + descripcion + '\'' +
                '}';
    }
}
