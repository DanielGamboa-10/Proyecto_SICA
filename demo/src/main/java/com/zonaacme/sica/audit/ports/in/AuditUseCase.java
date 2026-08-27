package com.zonaacme.sica.audit.ports.in;

import com.zonaacme.sica.audit.domain.BitacoraAuditoria;
import java.util.List;

/**
 * Puerto de Entrada (Primary/Driving Port) para los casos de uso de gestión de auditoría.
 *
 * <p><b>Principio SOLID aplicado:</b></p>
 * <ul>
 *   <li><b>ISP (Interface Segregation Principle):</b> Expone exclusivamente las operaciones requeridas
 *   para registrar y consultar bitácoras.</li>
 *   <li><b>DIP (Dependency Inversion Principle):</b> Define la abstracción a ser invocada por listeners o adaptadores primarios.</li>
 * </ul>
 */
public interface AuditUseCase {

    /**
     * Registra un nuevo evento inmutable en la bitácora de auditoría.
     *
     * @param registro Instancia inmutable de {@link BitacoraAuditoria}.
     */
    void registrarEvento(BitacoraAuditoria registro);

    /**
     * Obtiene el listado completo de registros de auditoría almacenados.
     *
     * @return Lista inmutable de registros de bitácora.
     */
    List<BitacoraAuditoria> consultarHistorialCompleto();

    /**
     * Consulta registros de auditoría filtrados por identificador de usuario.
     *
     * @param usuarioId Identificador del usuario.
     * @return Lista de registros coincidentes.
     */
    List<BitacoraAuditoria> consultarPorUsuario(String usuarioId);

    /**
     * Consulta registros de auditoría filtrados por tipo de entidad afectada.
     *
     * @param entidadAfectada Nombre de la entidad (e.g., "Visita", "Usuario").
     * @return Lista de registros coincidentes.
     */
    List<BitacoraAuditoria> consultarPorEntidad(String entidadAfectada);
}
