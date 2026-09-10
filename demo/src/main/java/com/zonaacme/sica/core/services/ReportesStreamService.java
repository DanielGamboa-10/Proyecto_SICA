package com.zonaacme.sica.core.services;

import com.zonaacme.sica.core.domain.*;
import com.zonaacme.sica.core.ports.out.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * ============================================================================
 * PALABRA CLAVE DE BÚSQUEDA EXAMEN: KEY_STREAM_REPORTES
 * FUNCIONALIDAD #4: Reportes Agregados con Stream API (Top Visitantes / Horas Pico)
 * ============================================================================
 * Utiliza Stream API avanzado de Java (Collectors.groupingBy, counting, mapping,
 * sorted, filtering) para generar analítica de seguridad en tiempo real.
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

    /**
     * KEY_STREAM_REPORTES: Top N personas con mayor cantidad de accesos registrados.
     * Utiliza: Stream -> groupingBy(PersonaId, counting()) -> sorted por frecuencia descendente.
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
     * KEY_STREAM_REPORTES: Top N visitantes más frecuentes a partir de solicitudes de visita.
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
     * KEY_STREAM_REPORTES: Horas pico de accesos (agrupación de accesos por hora del día 0 a 23).
     * Utiliza: Stream -> groupingBy(Hora, counting()) -> orden cronológico.
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
     * KEY_STREAM_REPORTES: Distribución porcentual y conteo por Resultado de Acceso (PERMITIDO vs DENEGADO).
     * Utiliza: Stream -> groupingBy(ResultadoAcceso, counting()).
     */
    public Map<ResultadoAcceso, Long> obtenerDistribucionResultadosAcceso() {
        return registroAccesoRepo.findAll().stream()
                .collect(Collectors.groupingBy(RegistroAcceso::getResultado, Collectors.counting()));
    }

    /**
     * KEY_STREAM_REPORTES: Distribución de visitas por Estado (PENDIENTE, APROBADA, EN_CURSO, COMPLETADA, RECHAZADA).
     */
    public Map<EstadoVisita, Long> obtenerDistribucionEstadosVisitas() {
        return visitaRepo.findAll().stream()
                .collect(Collectors.groupingBy(SolicitudVisita::getEstado, Collectors.counting()));
    }

    /**
     * KEY_STREAM_REPORTES: Ocupación actual de visitas activas agrupadas por Zona autorizada.
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
