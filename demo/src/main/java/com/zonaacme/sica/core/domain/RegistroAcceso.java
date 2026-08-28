package com.zonaacme.sica.core.domain;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Entidad inmutable que representa una transacción de paso físico por un punto de control (Check-In / Check-Out).
 *
 * <p><b>Principios de Diseño y SOLID:</b></p>
 * <ul>
 *   <li><b>SRP (Single Responsibility Principle):</b> Modela los datos probatorios de un intento o ingreso físico.</li>
 *   <li><b>Inmutabilidad:</b> Todos sus atributos son finales y sin mutadores para garantizar validez forense.</li>
 * </ul>
 */
public final class RegistroAcceso {

    private final String id;
    private final String personaId;
    private final String puntoControlId;
    private final String zonaId;
    private final TipoAcceso tipoAcceso;
    private final LocalDateTime fechaHora;
    private final ResultadoAcceso resultado;
    private final String observaciones;
    private final String visitaId;

    public RegistroAcceso(String id, String personaId, String puntoControlId, String zonaId,
                          TipoAcceso tipoAcceso, LocalDateTime fechaHora,
                          ResultadoAcceso resultado, String observaciones, String visitaId) {
        this.id = Objects.requireNonNull(id, "ID de registro no puede ser nulo");
        this.personaId = Objects.requireNonNull(personaId, "Persona ID no puede ser nulo");
        this.puntoControlId = Objects.requireNonNull(puntoControlId, "PuntoControl ID no puede ser nulo");
        this.zonaId = Objects.requireNonNull(zonaId, "Zona ID no puede ser nulo");
        this.tipoAcceso = Objects.requireNonNull(tipoAcceso, "TipoAcceso no puede ser nulo");
        this.fechaHora = Objects.requireNonNull(fechaHora, "Fecha y hora no pueden ser nulas");
        this.resultado = Objects.requireNonNull(resultado, "ResultadoAcceso no puede ser nulo");
        this.observaciones = observaciones != null ? observaciones.trim() : "";
        this.visitaId = visitaId;
    }

    public static RegistroAcceso permitido(String personaId, String puntoControlId, String zonaId,
                                           TipoAcceso tipoAcceso, String visitaId, String observaciones) {
        return new RegistroAcceso(
                UUID.randomUUID().toString(),
                personaId,
                puntoControlId,
                zonaId,
                tipoAcceso,
                LocalDateTime.now(),
                ResultadoAcceso.PERMITIDO,
                observaciones,
                visitaId
        );
    }

    public static RegistroAcceso denegado(String personaId, String puntoControlId, String zonaId,
                                          TipoAcceso tipoAcceso, ResultadoAcceso motivo, String observaciones) {
        return new RegistroAcceso(
                UUID.randomUUID().toString(),
                personaId,
                puntoControlId,
                zonaId,
                tipoAcceso,
                LocalDateTime.now(),
                motivo,
                observaciones,
                null
        );
    }

    public boolean esExitoso() {
        return resultado.esPermitido();
    }

    public String getId() {
        return id;
    }

    public String getPersonaId() {
        return personaId;
    }

    public String getPuntoControlId() {
        return puntoControlId;
    }

    public String getZonaId() {
        return zonaId;
    }

    public TipoAcceso getTipoAcceso() {
        return tipoAcceso;
    }

    public LocalDateTime getFechaHora() {
        return fechaHora;
    }

    public ResultadoAcceso getResultado() {
        return resultado;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public String getVisitaId() {
        return visitaId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RegistroAcceso that = (RegistroAcceso) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "RegistroAcceso{" +
                "id='" + id + '\'' +
                ", personaId='" + personaId + '\'' +
                ", punto=" + puntoControlId +
                ", tipo=" + tipoAcceso +
                ", resultado=" + resultado +
                ", fechaHora=" + fechaHora +
                '}';
    }
}
