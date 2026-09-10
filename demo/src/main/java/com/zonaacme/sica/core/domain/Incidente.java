package com.zonaacme.sica.core.domain;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Entidad de Dominio que representa un Incidente de Seguridad en Zona Acme.
 * Permite registrar eventos anómalos o infracciones y disparar bloqueos preventivos automáticos.
 */
public class Incidente {

    public enum NivelGravedad {
        LEVE,
        MODERADO,
        GRAVE,
        CRITICO
    }

    private final String id;
    private final String visitaId;
    private final String personaId;
    private final String personaNombre;
    private final String reportadoPor;
    private final NivelGravedad nivelGravedad;
    private final LocalDateTime fecha;
    private final String descripcion;
    private final String accionesTomadas;
    private final boolean bloqueoAplicado;

    public Incidente(String id, String visitaId, String personaId, String personaNombre,
                     String reportadoPor, NivelGravedad nivelGravedad, LocalDateTime fecha,
                     String descripcion, String accionesTomadas, boolean bloqueoAplicado) {
        this.id = id != null ? id : "INC_" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        this.visitaId = visitaId;
        this.personaId = personaId;
        this.personaNombre = personaNombre != null ? personaNombre : "No especificado";
        this.reportadoPor = reportadoPor != null ? reportadoPor : "SISTEMA";
        this.nivelGravedad = nivelGravedad != null ? nivelGravedad : NivelGravedad.MODERADO;
        this.fecha = fecha != null ? fecha : LocalDateTime.now();
        this.descripcion = Objects.requireNonNull(descripcion, "La descripción del incidente es obligatoria");
        this.accionesTomadas = accionesTomadas != null ? accionesTomadas : "Sin acciones registradas";
        this.bloqueoAplicado = bloqueoAplicado;
    }

    public static Incidente nuevo(String visitaId, String personaId, String personaNombre,
                                  String reportadoPor, NivelGravedad nivelGravedad,
                                  String descripcion, String accionesTomadas, boolean bloqueoAplicado) {
        return new Incidente(null, visitaId, personaId, personaNombre, reportadoPor, nivelGravedad,
                LocalDateTime.now(), descripcion, accionesTomadas, bloqueoAplicado);
    }

    public String getId() {
        return id;
    }

    public String getVisitaId() {
        return visitaId;
    }

    public String getPersonaId() {
        return personaId;
    }

    public String getPersonaNombre() {
        return personaNombre;
    }

    public String getReportadoPor() {
        return reportadoPor;
    }

    public NivelGravedad getNivelGravedad() {
        return nivelGravedad;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public String getAccionesTomadas() {
        return accionesTomadas;
    }

    public boolean isBloqueoAplicado() {
        return bloqueoAplicado;
    }

    public boolean esGraveOCritico() {
        return nivelGravedad == NivelGravedad.GRAVE || nivelGravedad == NivelGravedad.CRITICO;
    }
}
