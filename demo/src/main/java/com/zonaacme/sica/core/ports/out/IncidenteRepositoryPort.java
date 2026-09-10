package com.zonaacme.sica.core.ports.out;

import com.zonaacme.sica.core.domain.Incidente;
import java.util.List;
import java.util.Optional;

/**
 * Puerto Secundario / de Salida para la persistencia y consulta de incidentes de seguridad.
 */
public interface IncidenteRepositoryPort {

    void save(Incidente incidente);

    Optional<Incidente> findById(String id);

    List<Incidente> findAll();

    List<Incidente> findByPersonaId(String personaId);
}
