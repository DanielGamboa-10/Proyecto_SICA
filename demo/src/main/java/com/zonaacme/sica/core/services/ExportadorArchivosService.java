package com.zonaacme.sica.core.services;

import com.zonaacme.sica.audit.domain.BitacoraAuditoria;
import com.zonaacme.sica.audit.ports.out.AuditRepositoryPort;
import com.zonaacme.sica.core.domain.EstadoVisita;
import com.zonaacme.sica.core.domain.Persona;
import com.zonaacme.sica.core.domain.RegistroAcceso;
import com.zonaacme.sica.core.domain.SolicitudVisita;
import com.zonaacme.sica.core.ports.out.PersonaRepositoryPort;
import com.zonaacme.sica.core.ports.out.RegistroAccesoRepositoryPort;
import com.zonaacme.sica.core.ports.out.VisitaRepositoryPort;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * ============================================================================
 * PALABRA CLAVE KEY_EXPORT_CSV 
 * Exportación de Bitácora o Personal Presente a TXT / CSV
 * ============================================================================
 * Implementa I/O en Java con BufferedWriter y UTF-8 para exportar datos
 * de emergencia, personal en planta y auditoría externa.
 */
public class ExportadorArchivosService {

    private final VisitaRepositoryPort visitaRepo;
    private final PersonaRepositoryPort personaRepo;
    private final RegistroAccesoRepositoryPort registroAccesoRepo;
    private final AuditRepositoryPort auditRepo;

    private static final DateTimeFormatter ISO_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public ExportadorArchivosService(
            VisitaRepositoryPort visitaRepo,
            PersonaRepositoryPort personaRepo,
            RegistroAccesoRepositoryPort registroAccesoRepo,
            AuditRepositoryPort auditRepo
    ) {
        this.visitaRepo = visitaRepo;
        this.personaRepo = personaRepo;
        this.registroAccesoRepo = registroAccesoRepo;
        this.auditRepo = auditRepo;
    }

    /**
     * KEY_EXPORT_CSV: Exporta la lista de visitantes y personal presente dentro del complejo a archivo CSV.
     */
    public int exportarPersonalPresenteCSV(File archivoDestino) throws IOException {
        List<SolicitudVisita> activas = visitaRepo.findAll().stream()
                .filter(v -> v.getEstado() == EstadoVisita.EN_CURSO)
                .toList();

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(archivoDestino, StandardCharsets.UTF_8))) {
            writer.write("ID_VISITA,DOCUMENTO,NOMBRE_COMPLETO,EMPRESA,ANFITRION_ID,FECHA_INGRESO,VEHICULO_PLACA\n");
            for (SolicitudVisita v : activas) {
                Persona p = personaRepo.findById(v.getVisitanteId()).orElse(null);
                String doc = p != null ? p.getTipoDocumento() + " " + p.getNumeroDocumento() : "N/A";
                String nombre = p != null ? p.getNombreCompleto() : v.getVisitanteId();
                String empresa = p != null ? p.getEmpresa() : "N/A";
                String ingreso = v.getFechaCheckIn() != null ? v.getFechaCheckIn().format(ISO_FMT) : "N/A";
                String placa = v.getPlacaVehiculo() != null ? v.getPlacaVehiculo() : "PEATONAL";

                writer.write(String.format("\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\"\n",
                        v.getId(), doc, nombre, empresa, v.getAnfitrionId(), ingreso, placa));
            }
        }
        return activas.size();
    }



    /**
     * KEY_EXPORT_CSV: Exporta la planilla de evacuación / personal en sitio en formato TXT.
     */
    public static int exportarPlanillaEvacuacionTxt(List<SolicitudVisita> visitas, List<Persona> personas, Path destino) throws IOException {
        List<SolicitudVisita> activas = visitas.stream()
                .filter(v -> v.getEstado() == EstadoVisita.EN_CURSO)
                .toList();

        try (BufferedWriter writer = Files.newBufferedWriter(destino, StandardCharsets.UTF_8)) {
            writer.write("================================================================================\n");
            writer.write("SICA - PLANILLA DE EVACUACIÓN Y PERSONAL EN SITIO\n");
            writer.write("Fecha y Hora de Emisión: " + LocalDateTime.now().format(ISO_FMT) + "\n");
            writer.write("Total Ocupantes en Sitio: " + activas.size() + "\n");
            writer.write("================================================================================\n\n");

            int idx = 1;
            for (SolicitudVisita v : activas) {
                Persona p = personas.stream().filter(per -> per.getId().equals(v.getVisitanteId())).findFirst().orElse(null);
                String doc = p != null ? p.getTipoDocumento() + " " + p.getNumeroDocumento() : "N/A";
                String nombre = p != null ? p.getNombreCompleto() : v.getVisitanteId();
                String empresa = p != null ? p.getEmpresa() : "N/A";
                String ingreso = v.getFechaCheckIn() != null ? v.getFechaCheckIn().format(ISO_FMT) : "N/A";

                writer.write(String.format("#%02d | %-25s | DOC: %-15s | EMPRESA: %-20s | CHECK-IN: %s\n",
                        idx++, nombre, doc, empresa, ingreso));
            }
            writer.write("\n================================================================================\n");
            writer.write("FIN DE PLANILLA DE EVACUACIÓN SICA\n");
        }
        return activas.size();
    }

    /**
     * KEY_EXPORT_CSV: Exporta el historial completo de accesos a formato CSV.
     */
    public int exportarHistorialAccesosCSV(File archivoDestino) throws IOException {
        List<RegistroAcceso> accesos = registroAccesoRepo.findAll();

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(archivoDestino, StandardCharsets.UTF_8))) {
            writer.write("ID,FECHA_HORA,PERSONA_ID,PUNTO_CONTROL,SENTIDO,RESULTADO,OBSERVACIONES\n");
            for (RegistroAcceso acc : accesos) {
                writer.write(String.format("\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\"\n",
                        acc.getId(),
                        acc.getFechaHora().format(ISO_FMT),
                        acc.getPersonaId(),
                        acc.getPuntoControlId(),
                        acc.getTipoAcceso().name(),
                        acc.getResultado().name(),
                        acc.getObservaciones() != null ? acc.getObservaciones().replace("\"", "'") : ""
                ));
            }
        }
        return accesos.size();
    }

    /**
     * KEY_EXPORT_CSV: Exporta la bitácora inmutable de auditoría a archivo TXT plano formateado.
     */
    public int exportarBitacoraTXT(File archivoDestino) throws IOException {
        List<BitacoraAuditoria> registros = auditRepo.findAll();

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(archivoDestino, StandardCharsets.UTF_8))) {
            writer.write("================================================================================\n");
            writer.write("SICA - REPORTE DE BITÁCORA DE AUDITORÍA Y SEGURIDAD\n");
            writer.write("Generado: " + LocalDateTime.now().format(ISO_FMT) + "\n");
            writer.write("Total Registros: " + registros.size() + "\n");
            writer.write("================================================================================\n\n");

            for (BitacoraAuditoria reg : registros) {
                writer.write(String.format("[%s] ACCION: %-25s | USUARIO: %-15s | ENTIDAD: %s\n  DETALLE: %s\n\n",
                        reg.getFechaHora().format(ISO_FMT),
                        reg.getAccion(),
                        reg.getUsuarioId() != null ? reg.getUsuarioId() : "SISTEMA",
                        reg.getEntidadAfectada(),
                        reg.getDetalle()
                ));
            }
        }
        return registros.size();
    }


}
