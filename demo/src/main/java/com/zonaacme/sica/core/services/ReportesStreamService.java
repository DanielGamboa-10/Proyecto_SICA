package com.zonaacme.sica.core.services;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import com.zonaacme.sica.core.domain.EstadoVisita;
import com.zonaacme.sica.core.domain.RegistroAcceso;
import com.zonaacme.sica.core.domain.ResultadoAcceso;
import com.zonaacme.sica.core.domain.SolicitudVisita;
import com.zonaacme.sica.core.domain.Zona;
import com.zonaacme.sica.core.ports.out.PersonaRepositoryPort;
import com.zonaacme.sica.core.ports.out.RegistroAccesoRepositoryPort;
import com.zonaacme.sica.core.ports.out.VisitaRepositoryPort;
import com.zonaacme.sica.core.ports.out.ZonaRepositoryPort;

/**
  Reportes Agregados con Stream API (Top Visitantes / Horas Pico)
 * Utiliza Stream API avanzado de Java e seguridad en tiempo real.
 */
public class ReportesStreamService {

    private final RegistroAccesoRepositoryPort registroAccesoRepo;
    private final VisitaRepositoryPort visitaRepo;
    private final PersonaRepositoryPort personaRepo;
    private final ZonaRepositoryPort zonaRepo;

    public ReportesStreamService(
            RegistroAccesoRepositoryPort registroAccesoRepo,
            VisitaRepositoryPort visitaRepo,
            PersonaRepositoryPort personaRepo,
            ZonaRepositoryPort zonaRepo
    ) {
        this.registroAccesoRepo = registroAccesoRepo;
        this.visitaRepo = visitaRepo;
        this.personaRepo = personaRepo;
        this.zonaRepo = zonaRepo;
    }

    /** sorted por frecuencia descendente.
     */
    public List<Map.Entry<String, Long>> obtenerTopPersonasConMasAccesos(int topN) {
        List<RegistroAcceso> accesos = registroAccesoRepo.findAll();

        return accesos.stream()
                .collect(Collectors.groupingBy(RegistroAcceso::getPersonaId, Collectors.counting()))
                .entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(topN)
                .collect(Collectors.toList());
    }

    /**
     * STREAM_REPORTES: Top N visitantes más frecuentes a partir de solicitudes de visita.
     */
    public static Map<String, Long> calcularTopVisitantes(List<SolicitudVisita> visitas, int limit) {
        return visitas.stream()
                .collect(Collectors.groupingBy(SolicitudVisita::getVisitanteId, Collectors.counting()))
                .entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(limit)
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1,
                        LinkedHashMap::new
                ));
    }

    /**
     * STREAM_REPORTES: Horas pico de accesos
     * Utiliza: Streamorden cronológico.
     */
    public static Map<Integer, Long> calcularAccesosPorHora(List<RegistroAcceso> accesos) {
        return accesos.stream()
                .collect(Collectors.groupingBy(
                        acc -> acc.getFechaHora().getHour(),
                        TreeMap::new,
                        Collectors.counting()
                ));
    }

    public Map<Integer, Long> obtenerHorasPicoAccesos() {
        return calcularAccesosPorHora(registroAccesoRepo.findAll());
    }

    /**
     * STREAM_REPORTES: Distribución porcentual y conteo por Resultado de Acceso 
     * Utiliza: Stream 
     */
    public Map<ResultadoAcceso, Long> obtenerDistribucionResultadosAcceso() {
        return registroAccesoRepo.findAll().stream()
                .collect(Collectors.groupingBy(RegistroAcceso::getResultado, Collectors.counting()));
    }

    /**
     * STREAM_REPORTES: Distribución de visitas por Estado
     */
    public Map<EstadoVisita, Long> obtenerDistribucionEstadosVisitas() {
        return visitaRepo.findAll().stream()
                .collect(Collectors.groupingBy(SolicitudVisita::getEstado, Collectors.counting()));
    }

    /**
     * STREAM_REPORTES: Ocupación actual de visitas activas agrupadas por Zona autorizada.
     */
    public Map<String, Long> obtenerOcupacionVisitasPorZona() {
        return visitaRepo.findAll().stream()
                .filter(v -> v.getEstado() == EstadoVisita.EN_CURSO)
                .flatMap(v -> v.getZonasAutorizadasIds().stream())
                .collect(Collectors.groupingBy(
                        zonaId -> zonaRepo.findZonaById(zonaId).map(Zona::getNombre).orElse(zonaId),
                        Collectors.counting()
                ));
    }
}
