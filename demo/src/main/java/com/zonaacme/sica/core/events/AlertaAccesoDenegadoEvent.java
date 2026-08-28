package com.zonaacme.sica.core.events;

import com.zonaacme.sica.common.events.DomainEvent;
import com.zonaacme.sica.core.domain.ResultadoAcceso;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Evento emitido cuando un intento de acceso físico es denegado por violación de reglas de seguridad o parámetros.
 *
 * <p><b>Principio SOLID aplicado:</b></p>
 * <ul>
 *   <li><b>SRP (Single Responsibility Principle):</b> Modela los datos específicos de incidentes de seguridad física.</li>
 * </ul>
 */
public final class AlertaAccesoDenegadoEvent implements DomainEvent {

    private final String personaId;
    private final String puntoControlId;
    private final String zonaId;
    private final ResultadoAcceso motivoDenegacion;
    private final String detalle;
    private final LocalDateTime ocurridoEn;

    public AlertaAccesoDenegadoEvent(String personaId, String puntoControlId, String zonaId,
                                    ResultadoAcceso motivoDenegacion, String detalle) {
        this.personaId = Objects.requireNonNull(personaId);
        this.puntoControlId = Objects.requireNonNull(puntoControlId);
        this.zonaId = Objects.requireNonNull(zonaId);
        this.motivoDenegacion = Objects.requireNonNull(motivoDenegacion);
        this.detalle = detalle != null ? detalle : "";
        this.ocurridoEn = LocalDateTime.now();
    }

    @Override
    public String getEntidadId() {
        return personaId;
    }

    @Override
    public String getNombreEvento() {
        return "ALERTA_ACCESO_DENEGADO";
    }

    @Override
    public LocalDateTime getOcurridoEn() {
        return ocurridoEn;
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

    public ResultadoAcceso getMotivoDenegacion() {
        return motivoDenegacion;
    }

    public String getDetalle() {
        return detalle;
    }

    @Override
    public String toString() {
        return String.format("ALERTA DE SEGURIDAD: Acceso denegado para persona '%s' en punto '%s' (Zona: %s). Motivo: %s - %s",
                personaId, puntoControlId, zonaId, motivoDenegacion, detalle);
    }
}
