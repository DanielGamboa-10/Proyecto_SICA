package com.zonaacme.sica.core.adapters;

import com.zonaacme.sica.auth.ports.in.AuthUseCase;
import com.zonaacme.sica.common.events.DomainEventPublisher;
import com.zonaacme.sica.common.exceptions.DomainRuleException;
import com.zonaacme.sica.common.exceptions.EntityNotFoundException;
import com.zonaacme.sica.core.domain.Persona;
import com.zonaacme.sica.core.domain.SolicitudVisita;
import com.zonaacme.sica.core.events.VisitaAprobadaEvent;
import com.zonaacme.sica.core.events.VisitaSolicitadaEvent;
import com.zonaacme.sica.core.ports.in.VisitaUseCase;
import com.zonaacme.sica.core.ports.out.PersonaRepositoryPort;
import com.zonaacme.sica.core.ports.out.VisitaRepositoryPort;
import com.zonaacme.sica.core.ports.out.ZonaRepositoryPort;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * Servicio de aplicación que implementa el caso de uso {@link VisitaUseCase}.
 *
 * <p><b>Arquitectura Hexagonal & Principios SOLID:</b></p>
 * <ul>
 *   <li><b>SRP (Single Responsibility Principle):</b> Coordina el ciclo de vida, autorizaciones RBAC
 *   y notificaciones de las solicitudes de visitas.</li>
 *   <li><b>DIP (Dependency Inversion Principle):</b> Depende de puertos primarios y secundarios desacoplados.</li>
 * </ul>
 */
public class VisitaService implements VisitaUseCase {

    private final VisitaRepositoryPort visitaRepository;
    private final PersonaRepositoryPort personaRepository;
    private final ZonaRepositoryPort zonaRepository;
    private final AuthUseCase authUseCase;
    private final DomainEventPublisher eventPublisher;

    public VisitaService(VisitaRepositoryPort visitaRepository,
                         PersonaRepositoryPort personaRepository,
                         ZonaRepositoryPort zonaRepository,
                         AuthUseCase authUseCase,
                         DomainEventPublisher eventPublisher) {
        this.visitaRepository = Objects.requireNonNull(visitaRepository, "VisitaRepositoryPort no puede ser nulo");
        this.personaRepository = Objects.requireNonNull(personaRepository, "PersonaRepositoryPort no puede ser nulo");
        this.zonaRepository = Objects.requireNonNull(zonaRepository, "ZonaRepositoryPort no puede ser nulo");
        this.authUseCase = Objects.requireNonNull(authUseCase, "AuthUseCase no puede ser nulo");
        this.eventPublisher = Objects.requireNonNull(eventPublisher, "DomainEventPublisher no puede ser nulo");
    }

    @Override
    public SolicitudVisita solicitarVisita(String visitanteId, String anfitrionId, String motivo,
                                           LocalDateTime inicio, LocalDateTime fin, Set<String> zonasIds,
                                           String placaVehiculo, String token) {
        authUseCase.validarPermiso(token, "VISITAS_CREAR", "SOLICITAR_VISITA");

        Persona visitante = personaRepository.findById(visitanteId)
                .orElseThrow(() -> new EntityNotFoundException("Persona/Visitante", visitanteId));
        if (!visitante.isActivo()) {
            throw new DomainRuleException("No se puede solicitar una visita para una persona inactiva.");
        }

        Persona anfitrion = personaRepository.findById(anfitrionId)
                .orElseThrow(() -> new EntityNotFoundException("Persona/Anfitrión", anfitrionId));
        if (!anfitrion.isActivo()) {
            throw new DomainRuleException("El anfitrión seleccionado se encuentra inactivo.");
        }

        if (zonasIds != null) {
            for (String zonaId : zonasIds) {
                if (zonaRepository.findZonaById(zonaId).isEmpty()) {
                    throw new EntityNotFoundException("Zona", zonaId);
                }
            }
        }

        SolicitudVisita visita = SolicitudVisita.crear(
                visitanteId,
                anfitrionId,
                motivo,
                inicio,
                fin,
                zonasIds,
                placaVehiculo
        );

        visitaRepository.save(visita);

        eventPublisher.publish(new VisitaSolicitadaEvent(
                visita.getId(),
                visitante.getNombreCompleto(),
                anfitrion.getNombreCompleto(),
                motivo
        ));

        return visita;
    }

    @Override
    public void aprobarVisita(String visitaId, String observacion, String token) {
        authUseCase.validarPermiso(token, "VISITAS_APROBAR", "APROBAR_VISITA");

        SolicitudVisita visita = visitaRepository.findById(visitaId)
                .orElseThrow(() -> new EntityNotFoundException("SolicitudVisita", visitaId));

        visita.aprobar(observacion);
        visitaRepository.save(visita);

        eventPublisher.publish(new VisitaAprobadaEvent(
                visita.getId(),
                visita.getAnfitrionId(),
                observacion
        ));
    }

    @Override
    public void rechazarVisita(String visitaId, String motivo, String token) {
        authUseCase.validarPermiso(token, "VISITAS_APROBAR", "RECHAZAR_VISITA");

        SolicitudVisita visita = visitaRepository.findById(visitaId)
                .orElseThrow(() -> new EntityNotFoundException("SolicitudVisita", visitaId));

        visita.rechazar(motivo);
        visitaRepository.save(visita);
    }

    @Override
    public void cancelarVisita(String visitaId, String motivo, String token) {
        authUseCase.validarPermiso(token, "VISITAS_CREAR", "CANCELAR_VISITA");

        SolicitudVisita visita = visitaRepository.findById(visitaId)
                .orElseThrow(() -> new EntityNotFoundException("SolicitudVisita", visitaId));

        visita.cancelar(motivo);
        visitaRepository.save(visita);
    }

    @Override
    public Optional<SolicitudVisita> consultarPorId(String visitaId) {
        return visitaRepository.findById(visitaId);
    }

    @Override
    public List<SolicitudVisita> consultarPorAnfitrion(String anfitrionId) {
        return visitaRepository.findByAnfitrionId(anfitrionId);
    }

    @Override
    public List<SolicitudVisita> consultarPorVisitante(String visitanteId) {
        return visitaRepository.findByVisitanteId(visitanteId);
    }

    @Override
    public List<SolicitudVisita> consultarTodas(String token) {
        authUseCase.validarPermiso(token, "VISITAS_CONSULTAR", "CONSULTAR_TODAS_LAS_VISITAS");
        return visitaRepository.findAll();
    }
}
