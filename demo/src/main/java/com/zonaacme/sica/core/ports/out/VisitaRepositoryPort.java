package com.zonaacme.sica.core.ports.out;

import com.zonaacme.sica.core.domain.SolicitudVisita;
import java.util.List;
import java.util.Optional;

/**
 * Puerto Secundario / de Salida para la persistencia y consulta de solicitudes de visita y pases de acceso.
 *
 * <p><b>Arquitectura Hexagonal & Principios SOLID:</b></p>
 * <ul>
 *   <li><b>DIP (Dependency Inversion Principle):</b> Abstrae el almacenamiento de visitas del dominio.</li>
 * </ul>
 */
public interface VisitaRepositoryPort {

    void save(SolicitudVisita visita);

    Optional<SolicitudVisita> findById(String id);

    List<SolicitudVisita> findByVisitanteId(String visitanteId);

    List<SolicitudVisita> findByAnfitrionId(String anfitrionId);

    List<SolicitudVisita> findAll();

    Optional<SolicitudVisita> findVisitaActivaPorVisitante(String visitanteId);
}
