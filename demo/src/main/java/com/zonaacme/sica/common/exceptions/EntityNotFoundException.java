package com.zonaacme.sica.common.exceptions;

/**
 * Excepción lanzada cuando una entidad solicitada por identificador o documento no existe en el sistema.
 *
 * <p><b>Principio SOLID aplicado:</b></p>
 * <ul>
 *   <li><b>SRP (Single Responsibility Principle):</b> Representa el error específico de entidad no encontrada.</li>
 * </ul>
 */
public class EntityNotFoundException extends DomainRuleException {

    private final String entidad;
    private final String identificador;

    public EntityNotFoundException(String entidad, String identificador) {
        super(String.format("La entidad '%s' con identificador '%s' no fue encontrada.", entidad, identificador));
        this.entidad = entidad;
        this.identificador = identificador;
    }

    public String getEntidad() {
        return entidad;
    }

    public String getIdentificador() {
        return identificador;
    }
}
