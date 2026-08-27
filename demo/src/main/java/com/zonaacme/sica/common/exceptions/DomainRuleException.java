package com.zonaacme.sica.common.exceptions;

/**
 * Excepción lanzada cuando una regla o invariante de negocio del dominio es violada.
 *
 * <p><b>Principio SOLID aplicado:</b></p>
 * <ul>
 *   <li><b>SRP (Single Responsibility Principle):</b> Representa fallas estrictamente asociadas
 *   a la consistencia y reglas del negocio de acceso y control.</li>
 * </ul>
 */
public class DomainRuleException extends RuntimeException {

    public DomainRuleException(String message) {
        super(message);
    }

    public DomainRuleException(String message, Throwable cause) {
        super(message, cause);
    }
}
