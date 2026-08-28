package com.zonaacme.sica.core.ports.out;

import com.zonaacme.sica.core.domain.PuntoControl;
import com.zonaacme.sica.core.domain.Zona;
import java.util.List;
import java.util.Optional;

/**
 * Puerto Secundario / de Salida para la persistencia y consulta de Zonas y Puntos de Control.
 *
 * <p><b>Arquitectura Hexagonal & Principios SOLID:</b></p>
 * <ul>
 *   <li><b>DIP (Dependency Inversion Principle):</b> Abstrae la infraestructura física de zonas y terminales.</li>
 * </ul>
 */
public interface ZonaRepositoryPort {

    void saveZona(Zona zona);

    Optional<Zona> findZonaById(String id);

    Optional<Zona> findZonaByCodigo(String codigo);

    List<Zona> findAllZonas();

    void savePuntoControl(PuntoControl puntoControl);

    Optional<PuntoControl> findPuntoControlById(String id);

    Optional<PuntoControl> findPuntoControlByCodigo(String codigo);

    List<PuntoControl> findAllPuntosControl();

    List<PuntoControl> findPuntosControlByZonaId(String zonaId);
}
