package com.sica.auditoria.application;

import com.sica.auditoria.domain.Bitacora;
import com.sica.auditoria.domain.BitacoraRepository;

import java.time.LocalDateTime;

/**
 * Servicio de Aplicación para Auditoría (Bitácora).
 */
public class AuditoriaService {

    private final BitacoraRepository repository;

    public AuditoriaService(BitacoraRepository repository) {
        this.repository = repository;
    }

    /**
     * Registra un evento en la bitácora de auditoría.
     * Este método debería ser llamado desde otros servicios después de operaciones CRUD exitosas.
     */
    public void registrarAccion(int usuarioId, String accionRealizada, String tablaAfectada, 
                                int registroIdAfectado, String detalles) {
        
        Bitacora registro = new Bitacora(
                0, 
                usuarioId, 
                LocalDateTime.now(), 
                accionRealizada, 
                tablaAfectada, 
                registroIdAfectado, 
                detalles
        );
        
        boolean guardado = repository.save(registro);
        if (!guardado) {
            System.err.println("ALERTA CRÍTICA: No se pudo registrar la auditoría para la acción: " + accionRealizada);
        }
    }
}
