package com.sica.personas.domain;

import java.util.List;
import java.util.Optional;

/**
 * Puerto (Port) / Interfaz del Repositorio para la entidad Persona.
 */
public interface PersonaRepository {
    
    boolean save(Persona persona);
    
    Optional<Persona> findById(int id);
    
    Optional<Persona> findByDocumento(String documentoIdentidad);
    
    List<Persona> findAll();
    
    boolean update(Persona persona);
    
    boolean updateEstadoAcceso(int idPersona, int nuevoEstadoId);
}
