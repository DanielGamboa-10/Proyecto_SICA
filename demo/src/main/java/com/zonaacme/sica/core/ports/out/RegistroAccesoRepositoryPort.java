package com.zonaacme.sica.core.ports.out;

import com.zonaacme.sica.core.domain.RegistroAcceso;
import java.util.List;
import java.util.Optional;

/**
 * Puerto Secundario / de Salida para el almacenamiento inmutable de eventos de acceso físico.
 *
 * <p><b>Arquitectura Hexagonal & Principios SOLID:</b></p>
 * <ul>
 *   <li><b>DIP (Dependency Inversion Principle):</b> Define el contrato de persistencia para transacciones de paso físico.</li>
 * </ul>
 */
public interface RegistroAccesoRepositoryPort {

    void save(RegistroAcceso registro);

    Optional<RegistroAcceso> findById(String id);

    List<RegistroAcceso> findAll();

    List<RegistroAcceso> findByPersonaId(String personaId);

    List<RegistroAcceso> findByPuntoControlId(String puntoControlId);

    List<RegistroAcceso> findByZonaId(String zonaId);
}
