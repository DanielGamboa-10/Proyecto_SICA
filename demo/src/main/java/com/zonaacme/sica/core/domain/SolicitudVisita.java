package com.zonaacme.sica.core.domain;

import com.zonaacme.sica.common.exceptions.DomainRuleException;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

/**
 * Entidad de dominio que modela una solicitud de visita o pase de acceso programado.
 *
 * <p><b>Principios de Diseño y SOLID:</b></p>
 * <ul>
 *   <li><b>SRP (Single Responsibility Principle):</b> Encapsula las reglas de negocio, transiciones
 *   de estado del ciclo de vida de la visita y validaciones de vigencia temporal.</li>
 *   <li><b>Invariantes de Dominio:</b> Garantiza que no se aprueben ni registren visitas en estados incompatibles.</li>
 * </ul>
 */
public class SolicitudVisita {

    private final String id;
    private final String visitanteId;
    private final String anfitrionId;
    private final String motivo;
    private final LocalDateTime fechaHoraInicio;
    private final LocalDateTime fechaHoraFin;
    private final Set<String> zonasAutorizadasIds;
    private EstadoVisita estado;
    private String placaVehiculo;
    private String observacionAprobacion;
    private final LocalDateTime fechaCreacion;
    private LocalDateTime fechaCheckIn;
    private LocalDateTime fechaCheckOut;

    public SolicitudVisita(String id, String visitanteId, String anfitrionId, String motivo,
                           LocalDateTime fechaHoraInicio, LocalDateTime fechaHoraFin,
                           Set<String> zonasAutorizadasIds, EstadoVisita estado,
                           String placaVehiculo, String observacionAprobacion,
                           LocalDateTime fechaCreacion, LocalDateTime fechaCheckIn, LocalDateTime fechaCheckOut) {
        this.id = Objects.requireNonNull(id, "ID de solicitud no puede ser nulo");
        this.visitanteId = Objects.requireNonNull(visitanteId, "Visitante ID no puede ser nulo");
        this.anfitrionId = Objects.requireNonNull(anfitrionId, "Anfitrión ID no puede ser nulo");
        this.motivo = Objects.requireNonNull(motivo, "Motivo no puede ser nulo").trim();
        if (fechaHoraInicio == null || fechaHoraFin == null || !fechaHoraFin.isAfter(fechaHoraInicio)) {
            throw new DomainRuleException("La fecha y hora de fin debe ser posterior a la fecha y hora de inicio.");
        }
        this.fechaHoraInicio = fechaHoraInicio;
        this.fechaHoraFin = fechaHoraFin;
        this.zonasAutorizadasIds = new HashSet<>(zonasAutorizadasIds != null ? zonasAutorizadasIds : Collections.emptySet());
        this.estado = estado != null ? estado : EstadoVisita.PENDIENTE;
        this.placaVehiculo = placaVehiculo != null ? placaVehiculo.trim().toUpperCase() : null;
        this.observacionAprobacion = observacionAprobacion;
        this.fechaCreacion = fechaCreacion != null ? fechaCreacion : LocalDateTime.now();
        this.fechaCheckIn = fechaCheckIn;
        this.fechaCheckOut = fechaCheckOut;
    }

    public static SolicitudVisita crear(String visitanteId, String anfitrionId, String motivo,
                                        LocalDateTime inicio, LocalDateTime fin, Set<String> zonas, String placa) {
        return new SolicitudVisita(
                UUID.randomUUID().toString(),
                visitanteId,
                anfitrionId,
                motivo,
                inicio,
                fin,
                zonas,
                EstadoVisita.PENDIENTE,
                placa,
                null,
                LocalDateTime.now(),
                null,
                null
        );
    }

    public void aprobar(String observacion) {
        if (this.estado != EstadoVisita.PENDIENTE) {
            throw new DomainRuleException("Solo se pueden aprobar visitas en estado PENDIENTE. Estado actual: " + this.estado);
        }
        this.estado = EstadoVisita.APROBADA;
        this.observacionAprobacion = observacion != null ? observacion.trim() : "Aprobada por el anfitrión.";
    }

    public void rechazar(String motivoRechazo) {
        if (this.estado != EstadoVisita.PENDIENTE) {
            throw new DomainRuleException("Solo se pueden rechazar visitas en estado PENDIENTE. Estado actual: " + this.estado);
        }
        this.estado = EstadoVisita.RECHAZADA;
        this.observacionAprobacion = motivoRechazo != null ? motivoRechazo.trim() : "Rechazada por el anfitrión.";
    }

    public void registrarIngreso() {
        if (this.estado != EstadoVisita.APROBADA) {
            throw new DomainRuleException("No se puede registrar ingreso para una visita en estado: " + this.estado);
        }
        this.estado = EstadoVisita.EN_CURSO;
        this.fechaCheckIn = LocalDateTime.now();
    }

    public void registrarSalida() {
        if (this.estado != EstadoVisita.EN_CURSO) {
            throw new DomainRuleException("No se puede registrar salida para una visita en estado: " + this.estado);
        }
        this.estado = EstadoVisita.COMPLETADA;
        this.fechaCheckOut = LocalDateTime.now();
    }

    public void cancelar(String motivo) {
        if (this.estado == EstadoVisita.EN_CURSO || this.estado == EstadoVisita.COMPLETADA) {
            throw new DomainRuleException("No se puede cancelar una visita que ya ingresó o fue completada.");
        }
        this.estado = EstadoVisita.CANCELADA;
        this.observacionAprobacion = "Cancelada: " + (motivo != null ? motivo : "");
    }

    public boolean estaVigente(LocalDateTime momento) {
        if (momento == null) return false;
        return !momento.isBefore(fechaHoraInicio) && !momento.isAfter(fechaHoraFin);
    }

    public boolean tieneAccesoAZona(String zonaId) {
        if (zonaId == null) return false;
        return zonasAutorizadasIds.contains(zonaId);
    }

    public String getId() {
        return id;
    }

    public String getVisitanteId() {
        return visitanteId;
    }

    public String getAnfitrionId() {
        return anfitrionId;
    }

    public String getMotivo() {
        return motivo;
    }

    public LocalDateTime getFechaHoraInicio() {
        return fechaHoraInicio;
    }

    public LocalDateTime getFechaHoraFin() {
        return fechaHoraFin;
    }

    public Set<String> getZonasAutorizadasIds() {
        return Collections.unmodifiableSet(zonasAutorizadasIds);
    }

    public EstadoVisita getEstado() {
        return estado;
    }

    public String getPlacaVehiculo() {
        return placaVehiculo;
    }

    public String getObservacionAprobacion() {
        return observacionAprobacion;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public LocalDateTime getFechaCheckIn() {
        return fechaCheckIn;
    }

    public LocalDateTime getFechaCheckOut() {
        return fechaCheckOut;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SolicitudVisita that = (SolicitudVisita) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "SolicitudVisita{" +
                "id='" + id + '\'' +
                ", visitanteId='" + visitanteId + '\'' +
                ", estado=" + estado +
                ", inicio=" + fechaHoraInicio +
                ", fin=" + fechaHoraFin +
                '}';
    }
}
