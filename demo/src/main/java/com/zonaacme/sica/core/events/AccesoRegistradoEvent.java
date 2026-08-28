package com.zonaacme.sica.core.events;

import com.zonaacme.sica.common.events.DomainEvent;
import com.zonaacme.sica.core.domain.ResultadoAcceso;
import com.zonaacme.sica.core.domain.TipoAcceso;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Evento emitido tras la ejecución de un paso físico en un punto de control (Check-In o Check-Out).
 *
 * <p><b>Principio SOLID aplicado:</b></p>
 * <ul>
 *   <li><b>SRP (Single Responsibility Principle):</b> Modela los datos de una transacción de acceso físico.</li>
 * </ul>
 */
public final class AccesoRegistradoEvent implements DomainEvent {

    private final String registroId;
    private final String personaId;
    private final String puntoControlId;
    private final String zonaId;
    private final TipoAcceso tipoAcceso;
    private final ResultadoAcceso resultado;
    private final LocalDateTime ocurridoEn;

    public AccesoRegistradoEvent(String registroId, String personaId, String puntoControlId,
                                 String zonaId, TipoAcceso tipoAcceso, ResultadoAcceso resultado) {
        this.registroId = Objects.requireNonNull(registroId);
        this.personaId = Objects.requireNonNull(personaId);
        this.puntoControlId = Objects.requireNonNull(puntoControlId);
        this.zonaId = Objects.requireNonNull(zonaId);
        this.tipoAcceso = Objects.requireNonNull(tipoAcceso);
        this.resultado = Objects.requireNonNull(resultado);
        this.ocurridoEn = LocalDateTime.now();
    }

    @Override
    public String getEntidadId() {
        return registroId;
    }

    @Override
    public String getNombreEvento() {
        return "ACCESO_REGISTRADO";
    }

    @Override
    public LocalDateTime getOcurridoEn() {
        return ocurridoEn;
    }

    public String getRegistroId() {
        return registroId;
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

    public ResultadoAcceso getResultado() {
        return resultado;
    }

    @Override
    public String toString() {
        return String.format("Acceso '%s' (%s) en punto '%s' (Zona: %s) para persona '%s'. Resultado: %s",
                tipoAcceso, registroId, puntoControlId, zonaId, personaId, resultado);
    }
}
