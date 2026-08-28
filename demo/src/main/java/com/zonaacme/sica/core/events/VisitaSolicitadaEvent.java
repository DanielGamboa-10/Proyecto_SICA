package com.zonaacme.sica.core.events;

import com.zonaacme.sica.common.events.DomainEvent;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Evento emitido cuando se radica una nueva solicitud de visita en el sistema.
 *
 * <p><b>Principio SOLID aplicado:</b></p>
 * <ul>
 *   <li><b>SRP (Single Responsibility Principle):</b> Modela los datos del hecho inmutable de registro de visita.</li>
 * </ul>
 */
public final class VisitaSolicitadaEvent implements DomainEvent {

    private final String visitaId;
    private final String visitanteId;
    private final String anfitrionId;
    private final String motivo;
    private final LocalDateTime ocurridoEn;

    public VisitaSolicitadaEvent(String visitaId, String visitanteId, String anfitrionId, String motivo) {
        this.visitaId = Objects.requireNonNull(visitaId);
        this.visitanteId = Objects.requireNonNull(visitanteId);
        this.anfitrionId = Objects.requireNonNull(anfitrionId);
        this.motivo = Objects.requireNonNull(motivo);
        this.ocurridoEn = LocalDateTime.now();
    }

    @Override
    public String getEntidadId() {
        return visitaId;
    }

    @Override
    public String getNombreEvento() {
        return "VISITA_SOLICITADA";
    }

    @Override
    public LocalDateTime getOcurridoEn() {
        return ocurridoEn;
    }

    public String getVisitaId() {
        return visitaId;
    }

    public String getVisitanteId() {
        return visitanteId;
    }

    public String getAnfitrionId() {
        return anfitrionId;
    }

    public String getMotivo() {
        return motivo;
    }

    @Override
    public String toString() {
        return String.format("Visita '%s' solicitada por/para visitante '%s' con anfitrión '%s'. Motivo: %s",
                visitaId, visitanteId, anfitrionId, motivo);
    }
}
