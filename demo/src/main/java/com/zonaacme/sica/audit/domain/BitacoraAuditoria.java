package com.zonaacme.sica.audit.domain;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Entidad de dominio inmutable que representa un registro en la bitácora de auditoría del sistema SICA.
 *
 * <p><b>Principio de Inmutabilidad y SOLID:</b></p>
 * <ul>
 *   <li><b>SRP (Single Responsibility Principle):</b> Modela exclusivamente la información forense e histórica
 *   de una operación ejecutada en el sistema.</li>
 *   <li><b>Inmutabilidad estricta:</b> Todos los atributos son finales y no existen métodos mutadores (setters),
 *   garantizando la integridad probatoria y no repudio de la bitácora.</li>
 * </ul>
 */
public final class BitacoraAuditoria {

    private final String id;
    private final String usuarioId;
    private final String accion;
    private final String entidadAfectada;
    private final String detalle;
    private final LocalDateTime fechaHora;
    private final String origen;

    /**
     * Constructor completo para la creación de un registro inmutable de auditoría.
     *
     * @param id Identificador único del registro de auditoría.
     * @param usuarioId Identificador del usuario que ejecutó la acción (o "SISTEMA" / "ANONIMO").
     * @param accion Tipo de acción ejecutada (e.g., "LOGIN_EXITOSO", "LOGIN_FALLIDO", "CHECK_IN", "CHECK_OUT", "APROBACION_ACCESO").
     * @param entidadAfectada Entidad de negocio impactada (e.g., "Visita", "Usuario", "Credencial").
     * @param detalle Descripción contextual y datos relevantes del evento.
     * @param fechaHora Estampa de tiempo exacta de la acción.
     * @param origen Canal, componente o terminal de origen (e.g., "CORE_SICA", "TERMINAL_RECEPCION").
     */
    public BitacoraAuditoria(String id, String usuarioId, String accion, String entidadAfectada,
                             String detalle, LocalDateTime fechaHora, String origen) {
        this.id = Objects.requireNonNull(id, "El id de auditoría no puede ser nulo");
        this.usuarioId = usuarioId != null ? usuarioId : "SISTEMA";
        this.accion = Objects.requireNonNull(accion, "La acción de auditoría no puede ser nula");
        this.entidadAfectada = Objects.requireNonNull(entidadAfectada, "La entidad afectada no puede ser nula");
        this.detalle = detalle != null ? detalle : "";
        this.fechaHora = Objects.requireNonNull(fechaHora, "La fecha y hora no pueden ser nulas");
        this.origen = origen != null ? origen : "SICA_CORE";
    }

    /**
     * Método de fábrica para generar un nuevo registro inmutable con identificador y estampa de tiempo automáticos.
     */
    public static BitacoraAuditoria crear(String usuarioId, String accion, String entidadAfectada, String detalle, String origen) {
        return new BitacoraAuditoria(
                UUID.randomUUID().toString(),
                usuarioId,
                accion,
                entidadAfectada,
                detalle,
                LocalDateTime.now(),
                origen
        );
    }

    public String getId() {
        return id;
    }

    public String getUsuarioId() {
        return usuarioId;
    }

    public String getAccion() {
        return accion;
    }

    public String getEntidadAfectada() {
        return entidadAfectada;
    }

    public String getDetalle() {
        return detalle;
    }

    public LocalDateTime getFechaHora() {
        return fechaHora;
    }

    public String getOrigen() {
        return origen;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BitacoraAuditoria that = (BitacoraAuditoria) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "BitacoraAuditoria{" +
                "id='" + id + '\'' +
                ", usuarioId='" + usuarioId + '\'' +
                ", accion='" + accion + '\'' +
                ", entidadAfectada='" + entidadAfectada + '\'' +
                ", detalle='" + detalle + '\'' +
                ", fechaHora=" + fechaHora +
                ", origen='" + origen + '\'' +
                '}';
    }
}
