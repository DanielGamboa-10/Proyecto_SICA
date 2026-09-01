package com.sica.incidentes.domain;

import java.util.List;
import java.util.Optional;

/**
 * Puerto (Port) / Interfaz del Repositorio para la entidad Incidente.
 */
public interface IncidenteRepository {
    
    boolean save(Incidente incidente);
    
    Optional<Incidente> findById(int id);
    
    List<Incidente> findAll();
    
    List<Incidente> findByVisitaId(int visitaId);
}
