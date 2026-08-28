package com.zonaacme.sica.core.ports.in;

import com.zonaacme.sica.core.domain.SolicitudVisita;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Puerto Primario / de Entrada para la gestión del ciclo de vida de visitas y autorizaciones de acceso.
 *
 * <p><b>Principios SOLID aplicados:</b></p>
 * <ul>
 *   <li><b>ISP (Interface Segregation Principle):</b> Expone exclusivamente operaciones relativas al ciclo de vida de solicitudes de visita.</li>
 *   <li><b>DIP (Dependency Inversion Principle):</b> Los controladores y clientes dependen de esta abstracción.</li>
 * </ul>
 */
public interface VisitaUseCase {

    /**
     * Registra una nueva solicitud de visita en el sistema.
     */
    SolicitudVisita solicitarVisita(String visitanteId, String anfitrionId, String motivo,
                                   LocalDateTime inicio, LocalDateTime fin, Set<String> zonasIds,
                                   String placaVehiculo, String token);

    /**
     * Aprueba formalmente una solicitud de visita en estado pendiente.
     */
    void aprobarVisita(String visitaId, String observacion, String token);

    /**
     * Rechaza una solicitud de visita en estado pendiente.
     */
    void rechazarVisita(String visitaId, String motivo, String token);

    /**
     * Cancela una solicitud de visita antes de que sea ejecutada.
     */
    void cancelarVisita(String visitaId, String motivo, String token);

    /**
     * Consulta una solicitud de visita por su identificador único.
     */
    Optional<SolicitudVisita> consultarPorId(String visitaId);

    /**
     * Consulta el listado de visitas asignadas a un anfitrión específico.
     */
    List<SolicitudVisita> consultarPorAnfitrion(String anfitrionId);

    /**
     * Consulta el listado de visitas asociadas a una persona visitante.
     */
    List<SolicitudVisita> consultarPorVisitante(String visitanteId);

    /**
     * Consulta todas las visitas registradas (requiere permiso de consulta).
     */
    List<SolicitudVisita> consultarTodas(String token);
}
