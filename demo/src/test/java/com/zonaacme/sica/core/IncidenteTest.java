package com.zonaacme.sica.core;

import com.zonaacme.sica.core.adapters.InMemoryIncidenteRepositoryAdapter;
import com.zonaacme.sica.core.domain.Incidente;
import com.zonaacme.sica.core.domain.Persona;
import com.zonaacme.sica.core.domain.TipoPersona;
import com.zonaacme.sica.core.ports.out.IncidenteRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class IncidenteTest {

    private IncidenteRepositoryPort repository;

    @BeforeEach
    void setUp() {
        repository = new InMemoryIncidenteRepositoryAdapter();
    }

    @Test
    @DisplayName("Debe registrar y consultar un incidente grave con bloqueo preventivo")
    void testRegistrarIncidenteGrave() {
        Persona persona = Persona.nuevo("CC", "10987654", "Carlos", "Pérez", "cperez@mail.com", "3001112233", "Acme", TipoPersona.VISITANTE);

        Incidente incidente = Incidente.nuevo(
                "VIS_001",
                persona.getId(),
                persona.getNombreCompleto(),
                "admin",
                Incidente.NivelGravedad.GRAVE,
                "Intento de ingreso no autorizado a cuarto de servidores Tier IV",
                "Retención de badge e informe a jefatura",
                true
        );

        repository.save(incidente);

        List<Incidente> lista = repository.findAll();
        assertEquals(1, lista.size());
        assertTrue(lista.get(0).esGraveOCritico());
        assertTrue(lista.get(0).isBloqueoAplicado());
        assertEquals("admin", lista.get(0).getReportadoPor());
    }

    @Test
    @DisplayName("Debe clasificar incidentes leves sin requerir bloqueo preventivo")
    void testIncidenteLeve() {
        Incidente incidente = Incidente.nuevo(
                "VIS_002",
                "PER_002",
                "Visitante Ocasional",
                "guardia1",
                Incidente.NivelGravedad.LEVE,
                "Retraso de 15 minutos en devolver carnet",
                "Advertencia verbal registrada",
                false
        );

        repository.save(incidente);

        List<Incidente> porPersona = repository.findByPersonaId("PER_002");
        assertEquals(1, porPersona.size());
        assertFalse(porPersona.get(0).esGraveOCritico());
        assertFalse(porPersona.get(0).isBloqueoAplicado());
    }
}
