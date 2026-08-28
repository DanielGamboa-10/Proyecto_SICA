package com.zonaacme.sica.core.adapters;

import com.zonaacme.sica.auth.ports.in.AuthUseCase;
import com.zonaacme.sica.common.events.DomainEventPublisher;
import com.zonaacme.sica.common.exceptions.EntityNotFoundException;
import com.zonaacme.sica.core.domain.*;
import com.zonaacme.sica.core.events.AccesoRegistradoEvent;
import com.zonaacme.sica.core.events.AlertaAccesoDenegadoEvent;
import com.zonaacme.sica.core.ports.in.ControlAccesoUseCase;
import com.zonaacme.sica.core.ports.out.PersonaRepositoryPort;
import com.zonaacme.sica.core.ports.out.RegistroAccesoRepositoryPort;
import com.zonaacme.sica.core.ports.out.VisitaRepositoryPort;
import com.zonaacme.sica.core.ports.out.ZonaRepositoryPort;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Servicio de aplicación que implementa el caso de uso transaccional {@link ControlAccesoUseCase}.
 *
 * <p><b>Arquitectura Hexagonal & Principios SOLID:</b></p>
 * <ul>
 *   <li><b>SRP (Single Responsibility Principle):</b> Orquesta la validación de reglas físicas de acceso,
 *   horarios de zona, estados de visita y publicación de eventos hacia la bitácora forense de auditoría.</li>
 *   <li><b>DIP (Dependency Inversion Principle):</b> Depende exclusivamente de abstracciones de puertos y del bus de eventos.</li>
 * </ul>
 */
public class ControlAccesoService implements ControlAccesoUseCase {

    private final RegistroAccesoRepositoryPort registroAccesoRepository;
    private final PersonaRepositoryPort personaRepository;
    private final ZonaRepositoryPort zonaRepository;
    private final VisitaRepositoryPort visitaRepository;
    private final AuthUseCase authUseCase;
    private final DomainEventPublisher eventPublisher;

    public ControlAccesoService(RegistroAccesoRepositoryPort registroAccesoRepository,
                                PersonaRepositoryPort personaRepository,
                                ZonaRepositoryPort zonaRepository,
                                VisitaRepositoryPort visitaRepository,
                                AuthUseCase authUseCase,
                                DomainEventPublisher eventPublisher) {
        this.registroAccesoRepository = Objects.requireNonNull(registroAccesoRepository, "RegistroAccesoRepositoryPort no puede ser nulo");
        this.personaRepository = Objects.requireNonNull(personaRepository, "PersonaRepositoryPort no puede ser nulo");
        this.zonaRepository = Objects.requireNonNull(zonaRepository, "ZonaRepositoryPort no puede ser nulo");
        this.visitaRepository = Objects.requireNonNull(visitaRepository, "VisitaRepositoryPort no puede ser nulo");
        this.authUseCase = Objects.requireNonNull(authUseCase, "AuthUseCase no puede ser nulo");
        this.eventPublisher = Objects.requireNonNull(eventPublisher, "DomainEventPublisher no puede ser nulo");
    }

    @Override
    public RegistroAcceso registrarIngreso(String personaId, String puntoControlId, String tokenGuardia) {
        authUseCase.validarPermiso(tokenGuardia, "ACCESO_CHECKIN", "REGISTRAR_INGRESO");

        Optional<Persona> personaOpt = personaRepository.findById(personaId);
        if (personaOpt.isEmpty() || !personaOpt.get().isActivo()) {
            return registrarFallo(personaId, puntoControlId, "ZONA_DESCONOCIDA", TipoAcceso.ENTRADA,
                    ResultadoAcceso.DENEGADO_PERSONA_INACTIVA, "Persona no registrada o inactiva.");
        }
        Persona persona = personaOpt.get();

        Optional<PuntoControl> puntoOpt = zonaRepository.findPuntoControlById(puntoControlId);
        if (puntoOpt.isEmpty() || !puntoOpt.get().isActivo()) {
            return registrarFallo(personaId, puntoControlId, "ZONA_DESCONOCIDA", TipoAcceso.ENTRADA,
                    ResultadoAcceso.DENEGADO_PUNTO_CONTROL_INACTIVO, "Punto de control fuera de servicio.");
        }
        PuntoControl puntoControl = puntoOpt.get();

        Optional<Zona> zonaOpt = zonaRepository.findZonaById(puntoControl.getZonaId());
        if (zonaOpt.isEmpty() || !zonaOpt.get().isActivo()) {
            return registrarFallo(personaId, puntoControlId, puntoControl.getZonaId(), TipoAcceso.ENTRADA,
                    ResultadoAcceso.DENEGADO_ZONA_NO_AUTORIZADA, "Zona no disponible.");
        }
        Zona zona = zonaOpt.get();

        // Validar franja horaria operativa de la zona
        if (!zona.esHorarioPermitido(LocalTime.now())) {
            return registrarFallo(personaId, puntoControlId, zona.getId(), TipoAcceso.ENTRADA,
                    ResultadoAcceso.DENEGADO_FUERA_DE_HORARIO, "Intento de ingreso fuera del horario operativo de la zona.");
        }

        String visitaIdAsociada = null;

        // Si es visitante, contratista o proveedor, requiere visita aprobada y vigente
        if (persona.getTipoPersona() != TipoPersona.EMPLEADO) {
            Optional<SolicitudVisita> visitaOpt = visitaRepository.findVisitaActivaPorVisitante(personaId);
            if (visitaOpt.isEmpty()) {
                return registrarFallo(personaId, puntoControlId, zona.getId(), TipoAcceso.ENTRADA,
                        ResultadoAcceso.DENEGADO_SIN_VISITA_APROBADA, "No existe solicitud de visita aprobada para este visitante.");
            }

            SolicitudVisita visita = visitaOpt.get();
            if (!visita.estaVigente(LocalDateTime.now())) {
                return registrarFallo(personaId, puntoControlId, zona.getId(), TipoAcceso.ENTRADA,
                        ResultadoAcceso.DENEGADO_FUERA_DE_HORARIO, "La visita está fuera de la fecha/hora autorizada.");
            }

            if (!visita.tieneAccesoAZona(zona.getId())) {
                return registrarFallo(personaId, puntoControlId, zona.getId(), TipoAcceso.ENTRADA,
                        ResultadoAcceso.DENEGADO_ZONA_NO_AUTORIZADA, "La visita no autoriza el acceso a la zona " + zona.getNombre());
            }

            visita.registrarIngreso();
            visitaRepository.save(visita);
            visitaIdAsociada = visita.getId();
        }

        // Registrar acceso concedido
        RegistroAcceso registro = RegistroAcceso.permitido(
                personaId,
                puntoControlId,
                zona.getId(),
                TipoAcceso.ENTRADA,
                visitaIdAsociada,
                "Check-In exitoso."
        );
        registroAccesoRepository.save(registro);

        eventPublisher.publish(new AccesoRegistradoEvent(
                registro.getId(),
                persona.getNombreCompleto(),
                puntoControl.getNombre(),
                zona.getNombre(),
                TipoAcceso.ENTRADA,
                ResultadoAcceso.PERMITIDO
        ));

        return registro;
    }

    @Override
    public RegistroAcceso registrarSalida(String personaId, String puntoControlId, String tokenGuardia) {
        authUseCase.validarPermiso(tokenGuardia, "ACCESO_CHECKOUT", "REGISTRAR_SALIDA");

        Persona persona = personaRepository.findById(personaId)
                .orElseThrow(() -> new EntityNotFoundException("Persona", personaId));

        PuntoControl puntoControl = zonaRepository.findPuntoControlById(puntoControlId)
                .orElseThrow(() -> new EntityNotFoundException("PuntoControl", puntoControlId));

        String visitaIdAsociada = null;
        Optional<SolicitudVisita> visitaOpt = visitaRepository.findVisitaActivaPorVisitante(personaId);
        if (visitaOpt.isPresent()) {
            SolicitudVisita visita = visitaOpt.get();
            if (visita.getEstado() == EstadoVisita.EN_CURSO) {
                visita.registrarSalida();
                visitaRepository.save(visita);
                visitaIdAsociada = visita.getId();
            }
        }

        RegistroAcceso registro = RegistroAcceso.permitido(
                personaId,
                puntoControlId,
                puntoControl.getZonaId(),
                TipoAcceso.SALIDA,
                visitaIdAsociada,
                "Check-Out exitoso."
        );
        registroAccesoRepository.save(registro);

        eventPublisher.publish(new AccesoRegistradoEvent(
                registro.getId(),
                persona.getNombreCompleto(),
                puntoControl.getNombre(),
                puntoControl.getZonaId(),
                TipoAcceso.SALIDA,
                ResultadoAcceso.PERMITIDO
        ));

        return registro;
    }

    private RegistroAcceso registrarFallo(String personaId, String puntoControlId, String zonaId,
                                          TipoAcceso tipoAcceso, ResultadoAcceso motivo, String detalle) {
        RegistroAcceso registro = RegistroAcceso.denegado(
                personaId,
                puntoControlId,
                zonaId,
                tipoAcceso,
                motivo,
                detalle
        );
        registroAccesoRepository.save(registro);

        eventPublisher.publish(new AlertaAccesoDenegadoEvent(
                personaId,
                puntoControlId,
                zonaId,
                motivo,
                detalle
        ));

        return registro;
    }

    @Override
    public List<RegistroAcceso> consultarHistorialAccesos(String token) {
        authUseCase.validarPermiso(token, "ACCESO_MONITOREAR", "CONSULTAR_HISTORIAL_ACCESOS");
        return registroAccesoRepository.findAll();
    }

    @Override
    public List<RegistroAcceso> consultarAccesosPorPersona(String personaId, String token) {
        authUseCase.validarPermiso(token, "ACCESO_MONITOREAR", "CONSULTAR_ACCESOS_PERSONA");
        return registroAccesoRepository.findByPersonaId(personaId);
    }

    @Override
    public List<RegistroAcceso> consultarAccesosPorPunto(String puntoControlId, String token) {
        authUseCase.validarPermiso(token, "ACCESO_MONITOREAR", "CONSULTAR_ACCESOS_PUNTO");
        return registroAccesoRepository.findByPuntoControlId(puntoControlId);
    }
}
