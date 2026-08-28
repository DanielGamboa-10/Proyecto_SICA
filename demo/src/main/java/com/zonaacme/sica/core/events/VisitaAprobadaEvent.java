package com.zonaacme.sica.core.events;

import com.zonaacme.sica.common.events.DomainEvent;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Evento emitido cuando un anfitrión o administrador aprueba una visita solicitada.
 *
 * <p><b>Principio SOLID aplicado:</b></p>
 * <ul>
 *   <li><b>SRP (Single Responsibility Principle):</b> Modela los datos de la aprobación formal de acceso.</li>
 * </ul>
 */
public final class VisitaAprobadaEvent implements DomainEvent {

    private final String visitaId;
    private final String anfitrionId;
    private final String observacion;
    private final LocalDateTime ocurridoEn;

    public VisitaAprobadaEvent(String visitaId, String anfitrionId, String observacion) {
        this.visitaId = Objects.requireNonNull(visitaId);
        this.anfitrionId = Objects.requireNonNull(anfitrionId);
        this.observacion = observacion != null ? observacion : "";
        this.ocurridoEn = LocalDateTime.now();
    }

    @Override
    public String getEntidadId() {
        return visitaId;
    }

    @Override
    public String getNombreEvento() {
        return "VISITA_APROBADA";
    }

    @Override
    public LocalDateTime getOcurridoEn() {
        return ocurridoEn;
    }

    public String getVisitaId() {
        return visitaId;
    }

    public String getAnfitrionId() {
        return anfitrionId;
    }

    public String getObservacion() {
        return observacion;
    }

    @Override
    public String toString() {
        return String.format("Visita '%s' ha sido aprobada por el anfitrión '%s'. Observación: %s",
                visitaId, anfitrionId, observacion);
    }
}
