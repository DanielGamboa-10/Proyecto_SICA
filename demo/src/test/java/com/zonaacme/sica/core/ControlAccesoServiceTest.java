package com.zonaacme.sica.core;

import com.zonaacme.sica.auth.adapters.AuthService;
import com.zonaacme.sica.auth.adapters.InMemoryUsuarioRepositoryAdapter;
import com.zonaacme.sica.auth.domain.SesionUsuario;
import com.zonaacme.sica.common.events.DomainEventPublisher;
import com.zonaacme.sica.core.adapters.*;
import com.zonaacme.sica.core.domain.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class ControlAccesoServiceTest {

    private ControlAccesoService controlAccesoService;
    private InMemoryPersonaRepositoryAdapter personaRepository;
    private InMemoryZonaRepositoryAdapter zonaRepository;
    private InMemoryVisitaRepositoryAdapter visitaRepository;
    private InMemoryRegistroAccesoRepositoryAdapter registroAccesoRepository;
    private AuthService authService;
    private SesionUsuario sesionGuardia;

    @BeforeEach
    void setUp() {
        DomainEventPublisher eventPublisher = DomainEventPublisher.getInstance();
        eventPublisher.reset();

        InMemoryUsuarioRepositoryAdapter usuarioRepo = new InMemoryUsuarioRepositoryAdapter();
        authService = new AuthService(usuarioRepo, eventPublisher);

        sesionGuardia = authService.autenticar("guardia1", "Guardia123*");

        personaRepository = new InMemoryPersonaRepositoryAdapter();
        zonaRepository = new InMemoryZonaRepositoryAdapter();
        visitaRepository = new InMemoryVisitaRepositoryAdapter();
        registroAccesoRepository = new InMemoryRegistroAccesoRepositoryAdapter();

        controlAccesoService = new ControlAccesoService(
                registroAccesoRepository,
                personaRepository,
                zonaRepository,
                visitaRepository,
                authService,
                eventPublisher
        );
    }

    @Test
    @DisplayName("Empleado de planta ingresa directamente a recepción")
    void empleadoIngresaDirectamente() {
        Persona empleado = personaRepository.findByDocumento("CC", "10102020").orElseThrow();
        PuntoControl torniquete = zonaRepository.findPuntoControlByCodigo("PC_TORN_01").orElseThrow();

        RegistroAcceso acceso = controlAccesoService.registrarIngreso(
                empleado.getId(),
                torniquete.getId(),
                sesionGuardia.getToken()
        );

        assertNotNull(acceso);
        assertTrue(acceso.esExitoso());
        assertEquals(ResultadoAcceso.PERMITIDO, acceso.getResultado());
        assertEquals(TipoAcceso.ENTRADA, acceso.getTipoAcceso());
    }

    @Test
    @DisplayName("Visitante sin visita aprobada es rechazado en portería")
    void visitanteSinVisitaEsRechazado() {
        Persona visitante = personaRepository.findByDocumento("CC", "80809090").orElseThrow();
        PuntoControl torniquete = zonaRepository.findPuntoControlByCodigo("PC_TORN_01").orElseThrow();

        RegistroAcceso acceso = controlAccesoService.registrarIngreso(
                visitante.getId(),
                torniquete.getId(),
                sesionGuardia.getToken()
        );

        assertNotNull(acceso);
        assertFalse(acceso.esExitoso());
        assertEquals(ResultadoAcceso.DENEGADO_SIN_VISITA_APROBADA, acceso.getResultado());
    }

    @Test
    @DisplayName("Visitante con visita aprobada realiza Check-In y Check-Out completo")
    void visitanteConVisitaAprobadaRealizaCheckInYCheckOut() {
        Persona visitante = personaRepository.findByDocumento("CC", "80809090").orElseThrow();
        Persona anfitrion = personaRepository.findByDocumento("CC", "10102020").orElseThrow();
        Zona recepcion = zonaRepository.findZonaByCodigo("ZONA_RECEPCION").orElseThrow();
        PuntoControl torniquete = zonaRepository.findPuntoControlByCodigo("PC_TORN_01").orElseThrow();

        // Crear y aprobar visita
        LocalDateTime ahora = LocalDateTime.now();
        SolicitudVisita visita = SolicitudVisita.crear(
                visitante.getId(),
                anfitrion.getId(),
                "Reunión de Negocios",
                ahora.minusMinutes(15),
                ahora.plusHours(2),
                Set.of(recepcion.getId()),
                null
        );
        visita.aprobar("Aprobado");
        visitaRepository.save(visita);

        // 1. Check-In
        RegistroAcceso checkIn = controlAccesoService.registrarIngreso(
                visitante.getId(),
                torniquete.getId(),
                sesionGuardia.getToken()
        );
        assertTrue(checkIn.esExitoso());
        assertEquals(ResultadoAcceso.PERMITIDO, checkIn.getResultado());

        SolicitudVisita visitaEnCurso = visitaRepository.findById(visita.getId()).orElseThrow();
        assertEquals(EstadoVisita.EN_CURSO, visitaEnCurso.getEstado());
        assertNotNull(visitaEnCurso.getFechaCheckIn());

        // 2. Check-Out
        RegistroAcceso checkOut = controlAccesoService.registrarSalida(
                visitante.getId(),
                torniquete.getId(),
                sesionGuardia.getToken()
        );
        assertTrue(checkOut.esExitoso());
        assertEquals(TipoAcceso.SALIDA, checkOut.getTipoAcceso());

        SolicitudVisita visitaCompletada = visitaRepository.findById(visita.getId()).orElseThrow();
        assertEquals(EstadoVisita.COMPLETADA, visitaCompletada.getEstado());
        assertNotNull(visitaCompletada.getFechaCheckOut());
    }
}
