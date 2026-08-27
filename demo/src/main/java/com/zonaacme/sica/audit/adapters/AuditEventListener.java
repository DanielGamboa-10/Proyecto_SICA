package com.zonaacme.sica.audit.adapters;

import com.zonaacme.sica.audit.domain.BitacoraAuditoria;
import com.zonaacme.sica.audit.ports.in.AuditUseCase;
import com.zonaacme.sica.common.events.DomainEvent;
import com.zonaacme.sica.common.events.DomainEventListener;

import java.util.Objects;
import java.util.UUID;

/**
 * Adaptador Observador que reacciona a los eventos del dominio para registrar la auditoría inmutable
 * de forma completamente desacoplada de la lógica transaccional.
 *
 * <p><b>Patrón de Diseño:</b> Observer (Listener)</p>
 * <p><b>Principios SOLID aplicados:</b></p>
 * <ul>
 *   <li><b>SRP (Single Responsibility Principle):</b> Su única razón de cambio es la transformación y
 *   despacho de eventos de dominio a registros inmutables de auditoría.</li>
 *   <li><b>OCP (Open/Closed Principle):</b> Nuevos eventos del dominio son auditados automáticamente
 *   sin requerir cambios en los casos de uso principales.</li>
 *   <li><b>DIP (Dependency Inversion Principle):</b> Depende de la abstracción {@link AuditUseCase}.</li>
 * </ul>
 */
public class AuditEventListener implements DomainEventListener<DomainEvent> {

    private final AuditUseCase auditUseCase;

    public AuditEventListener(AuditUseCase auditUseCase) {
        this.auditUseCase = Objects.requireNonNull(auditUseCase, "AuditUseCase no puede ser nulo");
    }

    @Override
    public void onEvent(DomainEvent event) {
        if (event == null) {
            return;
        }

        // Construir registro inmutable de auditoría a partir del evento de dominio
        BitacoraAuditoria registro = new BitacoraAuditoria(
                UUID.randomUUID().toString(),
                "SISTEMA", // Se puede complementar con el contexto de seguridad si el evento lo provee
                event.getNombreEvento(),
                obtenerNombreEntidad(event),
                event.toString(),
                event.getOcurridoEn(),
                "EVENT_BUS"
        );

        auditUseCase.registrarEvento(registro);
    }

    private String obtenerNombreEntidad(DomainEvent event) {
        if (event.getEntidadId() != null && !event.getEntidadId().isEmpty()) {
            return event.getClass().getSimpleName().replace("Event", "");
        }
        return "GENERAL";
    }
}
