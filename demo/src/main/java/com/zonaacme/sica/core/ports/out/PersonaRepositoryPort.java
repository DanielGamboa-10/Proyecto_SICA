package com.zonaacme.sica.core.ports.out;

import com.zonaacme.sica.core.domain.Persona;
import java.util.List;
import java.util.Optional;

/**
 * Puerto Secundario / de Salida para la persistencia y consulta de personas.
 *
 * <p><b>Arquitectura Hexagonal & Principios SOLID:</b></p>
 * <ul>
 *   <li><b>DIP (Dependency Inversion Principle):</b> Desacopla la lógica de negocio del mecanismo de persistencia.</li>
 *   <li><b>LSP (Liskov Substitution Principle):</b> Permite intercambiar almacenamiento en memoria por SQL/NoSQL.</li>
 * </ul>
 */
public interface PersonaRepositoryPort {

    void save(Persona persona);

    Optional<Persona> findById(String id);

    Optional<Persona> findByDocumento(String tipoDocumento, String numeroDocumento);

    List<Persona> findAll();

    boolean existsByDocumento(String tipoDocumento, String numeroDocumento);
}
