package com.sica.reportes.application;

import com.sica.incidentes.domain.Incidente;
import com.sica.personas.domain.Persona;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Servicio de Reportes y Estadísticas.
 * Utiliza intensivamente Java Streams API y Funciones Lambda.
 */
public class ReportesService {

    /**
     * Retorna una lista de personas que tienen un estado de acceso específico.
     * Ejemplo: Filtrar a los que tienen prohibición de ingreso (estado = 2).
     */
    public List<Persona> filtrarPersonasPorEstado(List<Persona> todasLasPersonas, int estadoId) {
        return todasLasPersonas.stream()
                .filter(persona -> persona.getEstadoAccesoId() == estadoId)
                .collect(Collectors.toList());
    }

    /**
     * Retorna la cantidad de incidentes reportados por un usuario específico.
     */
    public long contarIncidentesReportadosPorUsuario(List<Incidente> incidentes, int usuarioId) {
        return incidentes.stream()
                .filter(incidente -> incidente.getReportadoPorId() == usuarioId)
                .count();
    }

    /**
     * Retorna los incidentes que ocurrieron en un rango de fechas determinado.
     */
    public List<Incidente> filtrarIncidentesPorRangoFechas(List<Incidente> incidentes, 
                                                           LocalDateTime inicio, LocalDateTime fin) {
        return incidentes.stream()
                .filter(incidente -> !incidente.getFecha().isBefore(inicio) && !incidente.getFecha().isAfter(fin))
                .collect(Collectors.toList());
    }

    /**
     * Imprime en consola (para fines de reporte rápido) los nombres de todas las personas de una empresa específica.
     */
    public void imprimirPersonasPorEmpresa(List<Persona> todasLasPersonas, int empresaId) {
        todasLasPersonas.stream()
                .filter(p -> p.getEmpresaId() == empresaId)
                .map(Persona::getNombre)
                .forEach(nombre -> System.out.println("- " + nombre));
    }
}
