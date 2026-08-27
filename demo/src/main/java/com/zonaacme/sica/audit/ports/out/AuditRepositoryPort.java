package com.zonaacme.sica.audit.ports.out;

import com.zonaacme.sica.audit.domain.BitacoraAuditoria;
import java.util.List;

/**
 * Puerto de Salida (Secondary/Driven Port) para la persistencia inmutable de auditoría.
 *
 * <p><b>Arquitectura Hexagonal & Principios SOLID:</b></p>
 * <ul>
 *   <li><b>DIP (Dependency Inversion Principle):</b> El dominio y la lógica de aplicación dependen
 *   de esta interfaz, sin conocer si el almacenamiento es en base de datos relacional, NoSQL o en memoria.</li>
 *   <li><b>LSP (Liskov Substitution Principle):</b> Cualquier adaptador de persistencia (SQL, Mongo, In-Memory)
 *   puede sustituir a otro cumpliendo este contrato de persistencia inmutable.</li>
 * </ul>
 */
public interface AuditRepositoryPort {

    /**
     * Persiste de manera inmutable un registro de auditoría.
     *
     * @param bitacora Registro a almacenar.
     */
    void save(BitacoraAuditoria bitacora);

    /**
     * Retorna todos los registros de auditoría ordenados por fecha y hora descendente.
     *
     * @return Lista de registros.
     */
    List<BitacoraAuditoria> findAll();

    /**
     * Busca registros asociados a un usuario específico.
     *
     * @param usuarioId Identificador del usuario.
     * @return Lista de registros coincidentes.
     */
    List<BitacoraAuditoria> findByUsuarioId(String usuarioId);

    /**
     * Busca registros asociados a un tipo de entidad afectada.
     *
     * @param entidadAfectada Nombre de la entidad.
     * @return Lista de registros coincidentes.
     */
    List<BitacoraAuditoria> findByEntidadAfectada(String entidadAfectada);
}
