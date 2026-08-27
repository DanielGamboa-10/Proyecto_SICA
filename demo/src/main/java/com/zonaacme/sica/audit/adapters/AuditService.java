package com.zonaacme.sica.audit.adapters;

import com.zonaacme.sica.audit.domain.BitacoraAuditoria;
import com.zonaacme.sica.audit.ports.in.AuditUseCase;
import com.zonaacme.sica.audit.ports.out.AuditRepositoryPort;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Servicio de aplicación que implementa los casos de uso de auditoría.
 *
 * <p><b>Principios SOLID aplicados:</b></p>
 * <ul>
 *   <li><b>SRP (Single Responsibility Principle):</b> Orquesta exclusivamente la persistencia y consulta
 *   de registros de bitácora.</li>
 *   <li><b>DIP (Dependency Inversion Principle):</b> Recibe por inyección de dependencias el puerto
 *   {@link AuditRepositoryPort}, manteniéndose agnóstico a la tecnología de almacenamiento.</li>
 * </ul>
 */
public class AuditService implements AuditUseCase {

    private final AuditRepositoryPort auditRepository;

    public AuditService(AuditRepositoryPort auditRepository) {
        this.auditRepository = Objects.requireNonNull(auditRepository, "El repositorio de auditoría no puede ser nulo");
    }

    @Override
    public void registrarEvento(BitacoraAuditoria registro) {
        if (registro != null) {
            auditRepository.save(registro);
        }
    }

    @Override
    public List<BitacoraAuditoria> consultarHistorialCompleto() {
        return Collections.unmodifiableList(auditRepository.findAll());
    }

    @Override
    public List<BitacoraAuditoria> consultarPorUsuario(String usuarioId) {
        if (usuarioId == null || usuarioId.trim().isEmpty()) {
            return Collections.emptyList();
        }
        return Collections.unmodifiableList(auditRepository.findByUsuarioId(usuarioId));
    }

    @Override
    public List<BitacoraAuditoria> consultarPorEntidad(String entidadAfectada) {
        if (entidadAfectada == null || entidadAfectada.trim().isEmpty()) {
            return Collections.emptyList();
        }
        return Collections.unmodifiableList(auditRepository.findByEntidadAfectada(entidadAfectada));
    }
}
