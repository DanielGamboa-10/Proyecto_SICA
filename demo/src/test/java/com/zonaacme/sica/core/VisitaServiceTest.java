package com.zonaacme.sica.core;

import com.zonaacme.sica.auth.adapters.AuthService;
import com.zonaacme.sica.auth.adapters.InMemoryUsuarioRepositoryAdapter;
import com.zonaacme.sica.auth.domain.SesionUsuario;
import com.zonaacme.sica.common.events.DomainEventPublisher;
import com.zonaacme.sica.core.adapters.InMemoryPersonaRepositoryAdapter;
import com.zonaacme.sica.core.adapters.InMemoryVisitaRepositoryAdapter;
import com.zonaacme.sica.core.adapters.InMemoryZonaRepositoryAdapter;
import com.zonaacme.sica.core.adapters.VisitaService;
import com.zonaacme.sica.core.domain.EstadoVisita;
import com.zonaacme.sica.core.domain.Persona;
import com.zonaacme.sica.core.domain.SolicitudVisita;
import com.zonaacme.sica.core.domain.Zona;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class VisitaServiceTest {

    private VisitaService visitaService;
    private InMemoryVisitaRepositoryAdapter visitaRepository;
    private InMemoryPersonaRepositoryAdapter personaRepository;
    private InMemoryZonaRepositoryAdapter zonaRepository;
    private AuthService authService;
    private SesionUsuario sesionAnfitrion;

    @BeforeEach
    void setUp() {
        DomainEventPublisher eventPublisher = DomainEventPublisher.getInstance();
        eventPublisher.reset();

        InMemoryUsuarioRepositoryAdapter usuarioRepo = new InMemoryUsuarioRepositoryAdapter();
        authService = new AuthService(usuarioRepo, eventPublisher);

        sesionAnfitrion = authService.autenticar("anfitrion1", "Anfitrion123*");

        personaRepository = new InMemoryPersonaRepositoryAdapter();
        zonaRepository = new InMemoryZonaRepositoryAdapter();
        visitaRepository = new InMemoryVisitaRepositoryAdapter();

        visitaService = new VisitaService(
                visitaRepository,
                personaRepository,
                zonaRepository,
                authService,
                eventPublisher
        );
    }

    @Test
    @DisplayName("Debe registrar y aprobar una solicitud de visita satisfactoriamente")
    void debeRegistrarYAprobarVisita() {
        Persona visitante = personaRepository.findByDocumento("CC", "80809090").orElseThrow();
        Persona anfitrion = personaRepository.findByDocumento("CC", "10102020").orElseThrow();
        Zona recepcion = zonaRepository.findZonaByCodigo("ZONA_RECEPCION").orElseThrow();

        LocalDateTime ahora = LocalDateTime.now();
        SolicitudVisita visita = visitaService.solicitarVisita(
                visitante.getId(),
                anfitrion.getId(),
                "Reunión Comercial Q3",
                ahora.minusMinutes(10),
                ahora.plusHours(2),
                Set.of(recepcion.getId()),
                "ABC-123",
                sesionAnfitrion.getToken()
        );

        assertNotNull(visita);
        assertEquals(EstadoVisita.PENDIENTE, visita.getEstado());

        // Aprobar la visita
        visitaService.aprobarVisita(visita.getId(), "Aprobado para ingreso en portería.", sesionAnfitrion.getToken());

        SolicitudVisita visitaAprobada = visitaRepository.findById(visita.getId()).orElseThrow();
        assertEquals(EstadoVisita.APROBADA, visitaAprobada.getEstado());
        assertTrue(visitaAprobada.estaVigente(LocalDateTime.now()));
    }
}
