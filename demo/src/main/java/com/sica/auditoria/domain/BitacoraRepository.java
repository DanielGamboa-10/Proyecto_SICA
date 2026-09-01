package com.sica.auditoria.domain;

import java.util.List;

/**
 * Interfaz del Repositorio para la Bitácora de Auditoría.
 */
public interface BitacoraRepository {
    
    boolean save(Bitacora bitacora);
    
    List<Bitacora> findAll();
    
    List<Bitacora> findByUsuario(int usuarioId);
}
