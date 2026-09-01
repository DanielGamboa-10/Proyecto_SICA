package com.sica.auditoria.domain;

import java.time.LocalDateTime;

/**
 * Entidad de Dominio que representa un registro en la Bitácora de Auditoría.
 */
public class Bitacora {
    private long id;
    private int usuarioId; // Quien realiza la acción
    private LocalDateTime fechaHora;
    private String accionRealizada; // Ej. 'CREACION_PERSONA'
    private String tablaAfectada;
    private int registroIdAfectado;
    private String detalles;

    public Bitacora() {
    }

    public Bitacora(long id, int usuarioId, LocalDateTime fechaHora, String accionRealizada, 
                    String tablaAfectada, int registroIdAfectado, String detalles) {
        this.id = id;
        this.usuarioId = usuarioId;
        this.fechaHora = fechaHora;
        this.accionRealizada = accionRealizada;
        this.tablaAfectada = tablaAfectada;
        this.registroIdAfectado = registroIdAfectado;
        this.detalles = detalles;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public int getUsuarioId() { return usuarioId; }
    public void setUsuarioId(int usuarioId) { this.usuarioId = usuarioId; }

    public LocalDateTime getFechaHora() { return fechaHora; }
    public void setFechaHora(LocalDateTime fechaHora) { this.fechaHora = fechaHora; }

    public String getAccionRealizada() { return accionRealizada; }
    public void setAccionRealizada(String accionRealizada) { this.accionRealizada = accionRealizada; }

    public String getTablaAfectada() { return tablaAfectada; }
    public void setTablaAfectada(String tablaAfectada) { this.tablaAfectada = tablaAfectada; }

    public int getRegistroIdAfectado() { return registroIdAfectado; }
    public void setRegistroIdAfectado(int registroIdAfectado) { this.registroIdAfectado = registroIdAfectado; }

    public String getDetalles() { return detalles; }
    public void setDetalles(String detalles) { this.detalles = detalles; }

    @Override
    public String toString() {
        return "Bitacora{" +
                "id=" + id +
                ", accion='" + accionRealizada + '\'' +
                ", fechaHora=" + fechaHora +
                '}';
    }
}
