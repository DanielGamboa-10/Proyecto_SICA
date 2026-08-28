package com.zonaacme.sica.core.ports.in;

import com.zonaacme.sica.core.domain.RegistroAcceso;
import java.util.List;

/**
 * Puerto Primario / de Entrada para el control transaccional de accesos físicos (Check-In / Check-Out).
 *
 * <p><b>Principios SOLID aplicados:</b></p>
 * <ul>
 *   <li><b>ISP (Interface Segregation Principle):</b> Expone operaciones dedicadas al paso y monitoreo en puntos de control.</li>
 *   <li><b>DIP (Dependency Inversion Principle):</b> Desacopla la lógica de validación física de los controladores o interfaces.</li>
 * </ul>
 */
public interface ControlAccesoUseCase {

    /**
     * Evalúa las reglas de seguridad y registra el ingreso físico (Check-In) de una persona en un punto de control.
     */
    RegistroAcceso registrarIngreso(String personaId, String puntoControlId, String tokenGuardia);

    /**
     * Evalúa las reglas y registra el egreso físico (Check-Out) de una persona en un punto de control.
     */
    RegistroAcceso registrarSalida(String personaId, String puntoControlId, String tokenGuardia);

    /**
     * Consulta el historial general de transacciones de acceso físico.
     */
    List<RegistroAcceso> consultarHistorialAccesos(String token);

    /**
     * Consulta el historial de accesos de una persona específica.
     */
    List<RegistroAcceso> consultarAccesosPorPersona(String personaId, String token);

    /**
     * Consulta el historial de pasos por un punto de control específico.
     */
    List<RegistroAcceso> consultarAccesosPorPunto(String puntoControlId, String token);
}
